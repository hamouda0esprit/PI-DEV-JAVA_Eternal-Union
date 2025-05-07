package Controllers;

import entite.Examen;
import service.ExamenService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class ExamenController implements Initializable {
    
    @FXML private TextField titreField;
    @FXML private TextField matiereField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField dureeField;
    @FXML private TextField nbrEssaiField;
    @FXML private TextArea descriptionArea;
    @FXML private Button submitButton;
    @FXML private Button retourAccueilButton;
    
    private ExamenService examenService;
    private Examen examenToUpdate;
    private boolean isUpdateMode = false;
    private boolean isAdminMode = false; // Indique si le contrôleur est en mode administrateur
    private String userId; // ID de l'utilisateur
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        
        // Initialiser le ComboBox avec les types d'examen
        typeComboBox.setItems(FXCollections.observableArrayList(
            "Test", "Quiz", "Quiz généré par IA"
        ));
        
        // Définir la valeur par défaut
        typeComboBox.setValue("Test");
        
        // Définir la date minimum pour le DatePicker (aujourd'hui)
        setupDatePicker();
        
        // Configurer les validations des champs numériques
        setupNumericFields();
    }
    
    /**
     * Configure le DatePicker pour empêcher la sélection de dates antérieures à aujourd'hui
     */
    private void setupDatePicker() {
        // Définir la valeur par défaut à aujourd'hui
        datePicker.setValue(java.time.LocalDate.now());
        
        // Ajouter un filtre pour empêcher la sélection de dates passées
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                
                // Désactiver les dates antérieures à aujourd'hui
                java.time.LocalDate today = java.time.LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
                
                // Ajouter un style pour les dates désactivées
                if (date.compareTo(today) < 0) {
                    setStyle("-fx-background-color: #ffc0cb;"); // Léger rouge pour les dates passées
                }
            }
        });
    }
    
    /**
     * Configure les champs numériques pour accepter uniquement des nombres positifs
     */
    private void setupNumericFields() {
        // Restriction pour le champ de durée (accepte uniquement des nombres positifs)
        dureeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                dureeField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        // Restriction pour le champ de nombre d'essais (accepte uniquement des nombres positifs)
        nbrEssaiField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nbrEssaiField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    
    /**
     * Définit si le contrôleur est en mode administrateur
     * @param isAdminMode true si en mode administrateur, false sinon
     */
    public void setAdminMode(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
        // Mettre à jour le texte du bouton retour si en mode admin
        if (isAdminMode) {
            retourAccueilButton.setText("Retour au Panneau d'Administration");
        }
    }
    
    /**
     * Définit l'ID de l'utilisateur
     * @param userId ID de l'utilisateur
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public void setExamenToUpdate(Examen examen) {
        this.examenToUpdate = examen;
        this.isUpdateMode = true;
        
        // Remplir les champs avec les données de l'examen
        titreField.setText(examen.getTitre());
        matiereField.setText(examen.getMatiere());
        typeComboBox.setValue(examen.getType());
        descriptionArea.setText(examen.getDescription());
        
        if (examen.getDate() != null) {
            datePicker.setValue(examen.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
        }
        
        dureeField.setText(String.valueOf(examen.getDuree()));
        nbrEssaiField.setText(String.valueOf(examen.getNbrEssai()));
        
        // Changer le texte du bouton en mode modification
        submitButton.setText("Modifier");
    }
    
    @FXML
    private void handleSubmit() {
        if (validateInputs()) {
            // If type selected is "Quiz généré par IA", navigate to quiz generation form
            if ("Quiz généré par IA".equals(typeComboBox.getValue())) {
                navigateToGenerateQuizForm();
                return;
            }
            
            Examen examen = getExamenFromFields();
            
            // Définir l'ID de l'utilisateur
            if (userId != null && !userId.isEmpty()) {
                try {
                    examen.setIdUser(Integer.parseInt(userId));
                } catch (NumberFormatException e) {
                    System.err.println("Erreur de conversion de l'ID utilisateur : " + e.getMessage());
                }
            }
            
            boolean success;
            
            if (isUpdateMode) {
                examen.setId(examenToUpdate.getId());
                success = examenService.modifier(examen);
            } else {
                success = examenService.ajouter(examen);
            }
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    isUpdateMode ? "Examen modifié avec succès !" : "Examen ajouté avec succès !");
                
                // En mode admin, revenir au panneau d'administration après la soumission
                if (isAdminMode) {
                    navigateToAdminPanel();
                } else {
                    navigateToTableView();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    isUpdateMode ? "Erreur lors de la modification de l'examen" : "Erreur lors de l'ajout de l'examen");
            }
        }
    }
    
    /**
     * Navigates to the quiz generation form with AI
     */
    private void navigateToGenerateQuizForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GenerateQuiz.fxml"));
            Parent root = loader.load();
            
            GenerateQuizController controller = loader.getController();
            // Pass data to the controller
            controller.setFields(titreField.getText(), matiereField.getText(), userId);
            controller.setAdminMode(isAdminMode);
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Générer un Quiz avec IA");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers le formulaire de génération de quiz");
        }
    }
    
    @FXML
    private void handleRetourAccueil() {
        try {
            if (isAdminMode) {
                // Si en mode admin, revenir au panneau d'administration
                navigateToAdminPanel();
            } else {
                // Sinon, revenir à l'accueil professeur
                Parent root = FXMLLoader.load(getClass().getResource("/view/AccueilProfesseur.fxml"));
                Scene scene = new Scene(root);
                Stage stage = (Stage) retourAccueilButton.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers la page de retour");
        }
    }
    
    private void navigateToAdminPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminPanel.fxml"));
            Parent root = loader.load();
            
            // Transmettre l'ID utilisateur au contrôleur d'administration
            AdminPanelController controller = loader.getController();
            if (controller != null && userId != null) {
                controller.setUserId(userId);
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) retourAccueilButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Panneau d'Administration");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers le panneau d'administration");
        }
    }
    
    private void navigateToTableView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/ExamenTableView.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers la vue du tableau");
        }
    }
    
    private Examen getExamenFromFields() {
        Examen examen = new Examen();
        
        examen.setTitre(titreField.getText());
        examen.setMatiere(matiereField.getText());
        examen.setType(typeComboBox.getValue());
        
        if (datePicker.getValue() != null) {
            examen.setDate(Date.from(datePicker.getValue().atStartOfDay(
                ZoneId.systemDefault()).toInstant()));
        }
        
        examen.setDuree(Integer.parseInt(dureeField.getText()));
        examen.setNbrEssai(Integer.parseInt(nbrEssaiField.getText()));
        examen.setDescription(descriptionArea.getText());
        
        // Valeurs par défaut pour les champs non présents dans le formulaire
        examen.setNote(0.0);
        
        return examen;
    }
    
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (titreField.getText().trim().isEmpty()) {
            errorMessage.append("Le titre est requis\n");
        }
        if (matiereField.getText().trim().isEmpty()) {
            errorMessage.append("La matière est requise\n");
        }
        if (typeComboBox.getValue() == null) {
            errorMessage.append("Le type est requis\n");
        }
        if (datePicker.getValue() == null) {
            errorMessage.append("La date est requise\n");
        } else {
            // Vérifier que la date n'est pas antérieure à aujourd'hui
            java.time.LocalDate today = java.time.LocalDate.now();
            if (datePicker.getValue().compareTo(today) < 0) {
                errorMessage.append("La date ne peut pas être antérieure à aujourd'hui\n");
            }
        }
        
        try {
            if (!dureeField.getText().trim().isEmpty()) {
                int duree = Integer.parseInt(dureeField.getText());
                if (duree <= 0) {
                    errorMessage.append("La durée doit être supérieure à 0\n");
                }
            } else {
                errorMessage.append("La durée est requise\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("La durée doit être un nombre entier positif\n");
        }
        
        try {
            if (!nbrEssaiField.getText().trim().isEmpty()) {
                int nbrEssai = Integer.parseInt(nbrEssaiField.getText());
                if (nbrEssai < 0) {
                    errorMessage.append("Le nombre d'essais doit être supérieur ou égal à 0\n");
                }
            } else {
                errorMessage.append("Le nombre d'essais est requis\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Le nombre d'essais doit être un nombre entier positif\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    private void clearFields() {
        titreField.clear();
        matiereField.clear();
        typeComboBox.setValue("Test");
        datePicker.setValue(null);
        dureeField.clear();
        nbrEssaiField.clear();
        descriptionArea.clear();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 