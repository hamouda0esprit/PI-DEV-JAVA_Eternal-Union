package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField idField;
    @FXML private Button fastLoginButton;
    @FXML private Label fastLoginErrorLabel;
    
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser le message d'erreur
        fastLoginErrorLabel.setVisible(false);
        
        // Initialiser le service utilisateur
        userService = new UserService();
        System.out.println("LoginController initialisé");
    }
    
    @FXML
    private void handleFastLogin() {
        // Cacher le message d'erreur
        fastLoginErrorLabel.setVisible(false);
        
        // Récupérer l'ID rapide
        String fastId = idField.getText().trim();
        System.out.println("Tentative de connexion avec ID: " + fastId);
        
        // Vérifier que l'ID est rempli
        if (fastId.isEmpty()) {
            showError("Veuillez entrer votre identifiant");
            return;
        }
        
        // Vérifier l'authentification rapide
        boolean isAuthenticated = userService.authenticateFast(fastId);
        System.out.println("Authentification: " + (isAuthenticated ? "réussie" : "échec"));
        
        if (isAuthenticated) {
            // Récupérer le rôle et rediriger
            String role = userService.getFastUserRole(fastId);
            System.out.println("Rôle récupéré: " + role);
            
            if (role == null) {
                showError("Erreur: Rôle utilisateur non trouvé");
                return;
            }
            
            // Vérifier si l'utilisateur est un administrateur
            if (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("administrateur") || role.equals("2")) {
                System.out.println("Redirection vers l'interface administrateur");
                navigateToAdminPanel();
            }
            // Vérifier les rôles étudiant
            else if (role.equalsIgnoreCase("Étudiant") || role.equalsIgnoreCase("student") || role.equals("0")) {
                System.out.println("Redirection vers l'interface étudiant");
                navigateToStudentHome();
            } 
            // Vérifier les rôles professeur
            else if (role.equalsIgnoreCase("Professeur") || role.equalsIgnoreCase("teacher") || 
                      role.equalsIgnoreCase("prof") || role.equals("1")) {
                System.out.println("Redirection vers l'interface professeur");
                navigateToTeacherHome();
            } else {
                System.out.println("Rôle non reconnu: " + role);
                showError("Erreur: Rôle non reconnu - " + role);
            }
        } else {
            showError("Identifiant invalide");
        }
    }
    
    private void navigateToAdminPanel() {
        try {
            String fxmlPath = "/view/AdminPanel.fxml";
            System.out.println("Chargement du fichier FXML: " + fxmlPath);
            
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("ERREUR: Fichier FXML non trouvé: " + fxmlPath);
                showError("Fichier d'interface administrateur non trouvé");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            
            // Passer l'ID utilisateur au contrôleur
            AdminPanelController controller = loader.getController();
            if (controller != null) {
                controller.setUserId(idField.getText().trim());
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) fastLoginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Panneau d'Administration");
            stage.show();
            System.out.println("Interface administrateur chargée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface administrateur:");
            e.printStackTrace();
            showError("Erreur lors de la navigation vers le panneau d'administration: " + e.getMessage());
        }
    }
    
    private void navigateToStudentHome() {
        try {
            String fxmlPath = "/view/AccueilEtudiant.fxml";
            System.out.println("Chargement du fichier FXML: " + fxmlPath);
            
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("ERREUR: Fichier FXML non trouvé: " + fxmlPath);
                showError("Fichier d'interface étudiant non trouvé");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            
            // Passer l'ID utilisateur au contrôleur
            AccueilEtudiantController controller = loader.getController();
            if (controller != null) {
                controller.setUserId(idField.getText().trim());
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) fastLoginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Espace Étudiant");
            stage.show();
            System.out.println("Interface étudiant chargée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface étudiant:");
            e.printStackTrace();
            showError("Erreur lors de la navigation vers l'accueil étudiant: " + e.getMessage());
        }
    }
    
    private void navigateToTeacherHome() {
        try {
            String fxmlPath = "/view/AccueilProfesseur.fxml";
            System.out.println("Chargement du fichier FXML: " + fxmlPath);
            
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("ERREUR: Fichier FXML non trouvé: " + fxmlPath);
                showError("Fichier d'interface professeur non trouvé");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            
            // Passer l'ID utilisateur au contrôleur
            try {
                // Vérifier si le contrôleur a une méthode pour définir l'ID utilisateur
                Object controller = loader.getController();
                if (controller != null && controller.getClass().getMethod("setUserId", String.class) != null) {
                    controller.getClass().getMethod("setUserId", String.class).invoke(controller, idField.getText().trim());
                    System.out.println("ID utilisateur défini dans le contrôleur professeur");
                }
            } catch (Exception ex) {
                // Si la méthode n'existe pas, enregistrer dans les logs mais continuer
                System.out.println("Méthode setUserId non disponible dans le contrôleur professeur: " + ex.getMessage());
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) fastLoginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Espace Professeur");
            stage.show();
            System.out.println("Interface professeur chargée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface professeur:");
            e.printStackTrace();
            showError("Erreur lors de la navigation vers l'accueil professeur: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        System.err.println("ERREUR AFFICHÉE: " + message);
        fastLoginErrorLabel.setText(message);
        fastLoginErrorLabel.setVisible(true);
    }
} 