package Controllers.Navigation;

import Controllers.ProfileController;
import Controllers.LoginController;
import Controllers.EvenementController;
import Controllers.EvenementStudentController;
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
        
        if (currentUser == null) {
            System.out.println("Warning: No authenticated user found in NavbarController");
        } else {
            System.out.println("NavbarController initialized with user: " + currentUser.getName());
        }
        
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
        loadView("/view/LoginView.fxml");
    }

    @FXML
    private void handleEvenementsClick() {
        if (currentUser == null) {
            System.out.println("No user logged in");
            return;
        }

        try {
            // Determine which FXML file to load based on user type
            String fxmlPath = currentUser.getType().equalsIgnoreCase("teacher") || currentUser.getType().equals("1")
                ? "/view/Evenement.fxml"
                : "/view/EvenementStudent.fxml";
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent eventRoot = loader.load();
            
            // Get the appropriate controller and set the current user
            Object controller = loader.getController();
            if (controller instanceof EvenementController) {
                ((EvenementController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof EvenementStudentController) {
                ((EvenementStudentController) controller).setCurrentUser(currentUser);
            }
            
            // Get the current stage
            Stage stage = (Stage) evenementsButton.getScene().getWindow();
            
            // Set minimum window size
            stage.setMinWidth(1200);
            stage.setMinHeight(800);
            
            // If current size is smaller than minimum, set to minimum
            if (stage.getWidth() < 1200) stage.setWidth(1200);
            if (stage.getHeight() < 800) stage.setHeight(800);
            
            // Create new scene with current dimensions
            Scene eventScene = new Scene(eventRoot, stage.getWidth(), stage.getHeight());
            stage.setTitle("LOE - Événements");
            stage.setScene(eventScene);
            eventScene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            System.out.println("Successfully navigated to events page for user: " + currentUser.getName() + 
                             " (Type: " + currentUser.getType() + ")");
        } catch (IOException e) {
            System.out.println("Error loading events page: " + e.getMessage());
            e.printStackTrace();
            
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load events page");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
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
} 