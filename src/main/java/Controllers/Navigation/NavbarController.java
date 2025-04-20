package Controllers.Navigation;

import Controllers.ProfileController;
import Controllers.LoginController;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.image.Image;
import java.io.IOException;
import javafx.scene.control.Alert;

public class NavbarController implements Initializable {
    @FXML
    private Button homeButton;
    @FXML
    private Button coursButton;
    @FXML
    private Button examenButton;
    @FXML
    private Button evenementsButton;
    @FXML
    private Button forumButton;
    @FXML
    private Button userProfileButton;

    private NavigationStateManager navigationState;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navigationState = NavigationStateManager.getInstance();
        currentUser = LoginController.getAuthenticatedUser();
        
        // Listen for changes in the current view
        navigationState.currentViewProperty().addListener((observable, oldValue, newValue) -> {
            updateActiveButton(newValue);
        });

        // Set initial state
        String currentView = navigationState.getCurrentView();
        if (currentView != null) {
            updateActiveButton(currentView);
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void updateActiveButton(String view) {
        // Remove active class from all buttons
        homeButton.getStyleClass().remove("active");
        coursButton.getStyleClass().remove("active");
        examenButton.getStyleClass().remove("active");
        evenementsButton.getStyleClass().remove("active");
        forumButton.getStyleClass().remove("active");

        // Add active class to the appropriate button
        switch (view) {
            case "Home":
                homeButton.getStyleClass().add("active");
                break;
            case "Cours":
                coursButton.getStyleClass().add("active");
                break;
            case "Examen":
                examenButton.getStyleClass().add("active");
                break;
            case "Evenement":
                evenementsButton.getStyleClass().add("active");
                break;
            case "Forum":
                forumButton.getStyleClass().add("active");
                break;
        }
    }

    @FXML
    private void handleHomeNavigation() {
        navigationState.setCurrentView("Home");
        loadView("/view/Home.fxml");
    }

    @FXML
    private void handleCoursNavigation() {
        navigationState.setCurrentView("Cours");
        loadView("/view/AfficherCours.fxml");
    }

    @FXML
    private void handleExamenNavigation() {
        navigationState.setCurrentView("Examen");
        
        // Get current authenticated user
        User user = LoginController.getAuthenticatedUser();
        
        if (user != null) {
            String userType = user.getType();
            System.out.println("Type d'utilisateur pour navigation exam: " + userType);
            
            // Navigate based on user type
            if (userType != null) {
                // Admin
                if (userType.equalsIgnoreCase("admin") || userType.equalsIgnoreCase("administrateur") || userType.equals("2")) {
                    loadView("/view/AdminPanel.fxml");
                    return;
                }
                // Student
                else if (userType.equalsIgnoreCase("Étudiant") || userType.equalsIgnoreCase("student") || userType.equals("0")) {
                    loadViewWithUserID("/view/AccueilEtudiant.fxml", "Espace Étudiant", examenButton);
                    return;
                }
                // Teacher
                else if (userType.equalsIgnoreCase("Professeur") || userType.equalsIgnoreCase("teacher") ||
                        userType.equalsIgnoreCase("prof") || userType.equals("1")) {
                    loadView("/view/AccueilProfesseur.fxml");
                    return;
                }
            }
        }
        
        // If no authenticated user or user type not recognized, show an alert and navigate to Home
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Accès restreint");
        alert.setHeaderText("Connexion requise");
        alert.setContentText("Veuillez vous connecter pour accéder à cette fonctionnalité.");
        alert.showAndWait();
        
        // Redirect to home page
        loadView("/view/Home.fxml");
    }

    @FXML
    private void handleEvenementsNavigation() {
        navigationState.setCurrentView("Evenement");
        loadView("/view/Evenement.fxml");
    }

    @FXML
    private void handleForumNavigation() {
        navigationState.setCurrentView("Forum");
        loadView("/view/Forum.fxml");
    }

    @FXML
    private void handleUserProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Profile.fxml"));
            Parent root = loader.load();
            
            // Get the ProfileController and set the current user
            ProfileController profileController = loader.getController();
            profileController.setCurrentUser(currentUser);
            
            // Get the current stage and set the new scene
            Stage stage = (Stage) userProfileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) homeButton.getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Add the stylesheet
            String css = getClass().getResource("/styles/style.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
            
            // Set the window icon
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            stage.getIcons().clear();
            stage.getIcons().add(icon);
            
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method to load a view and pass the current user ID to controllers that support it
     * @param fxmlPath Path to the FXML file
     * @param windowTitle Window title to set
     * @param sourceButton Button that triggered the navigation (for getting the scene)
     * @return true if successful, false otherwise
     */
    private boolean loadViewWithUserID(String fxmlPath, String windowTitle, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Get the controller
            Object controller = loader.getController();
            
            // If the controller has a setUserId method, call it
            if (currentUser != null && controller != null) {
                System.out.println("Attempting to pass user ID to " + controller.getClass().getSimpleName());
                
                // Try to call setUserId method if it exists
                try {
                    java.lang.reflect.Method setUserIdMethod = controller.getClass().getMethod("setUserId", String.class);
                    setUserIdMethod.invoke(controller, String.valueOf(currentUser.getId()));
                    System.out.println("Successfully passed user ID " + currentUser.getId() + " to " + controller.getClass().getSimpleName());
                } catch (NoSuchMethodException e) {
                    // Controller doesn't have setUserId method - this is okay
                    System.out.println("Controller " + controller.getClass().getSimpleName() + " doesn't have setUserId method");
                } catch (Exception e) {
                    // Other reflection errors
                    System.err.println("Error passing user ID to controller: " + e.getMessage());
                }
            }
            
            // Set the scene
            Stage stage = (Stage) sourceButton.getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Add the stylesheet
            String css = getClass().getResource("/styles/style.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
            
            // Set icon if needed
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            stage.getIcons().clear();
            stage.getIcons().add(icon);
            
            stage.setScene(scene);
            if (windowTitle != null && !windowTitle.isEmpty()) {
                stage.setTitle(windowTitle);
            }
            stage.show();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 