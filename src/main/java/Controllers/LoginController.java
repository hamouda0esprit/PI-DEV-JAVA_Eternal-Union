package Controllers;

import com.example.pijava.Main;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;

public class LoginController implements Initializable {
    @FXML
    private Button googleSignUpButton;
    
    @FXML
    private Button facebookSignUpButton;
    
    @FXML
    private Button createAccountButton;
    
    @FXML
    private Button connectButton;
    
    @FXML
    private VBox logoContainer;
    
    @FXML
    private ImageView logoImage;
    
    @FXML
    private StackPane mainContainer;
    
    private Stage stage;
    private LoginDialogController lastLoginDialogController;
    
    private static User authenticatedUser;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set action handlers
        setButtonActions();
        
        // Apply responsive design after the stage is set and components are loaded
        javafx.application.Platform.runLater(this::setupResponsiveDesign);
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
        
        // Add listener for window width changes
        stage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            handleResponsiveLayout(newWidth.doubleValue());
        });
    }
    
    private void setupResponsiveDesign() {
        if (stage == null) {
            // Try to get the stage from one of the nodes
            stage = (Stage) googleSignUpButton.getScene().getWindow();
        }
        
        if (stage != null) {
            // Initial responsive setup
            handleResponsiveLayout(stage.getWidth());
            
            // Also listen for height changes to maintain the fixed 150px spacing
            stage.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                // The login container's padding is set in FXML/CSS, so no additional code needed here
                // This ensures the spacing remains at 150px even when height changes
            });
        }
    }
    
    private void handleResponsiveLayout(double width) {
        // Hide logo container on smaller screens, but keep the form static
        if (width < 700) {
            logoContainer.setVisible(false);
            logoContainer.setManaged(false);
        } else {
            logoContainer.setVisible(true);
            logoContainer.setManaged(true);
        }
        
        // Adjust logo size based on available space
        if (width < 900 && width >= 700) {
            logoImage.setFitWidth(300);
            logoImage.setFitHeight(300);
        } else {
            logoImage.setFitWidth(400);
            logoImage.setFitHeight(400);
        }
        
        // If the window gets really small, ensure form stays at minimum size
        // This prevents the form from getting too squished
        if (width < 500) {
            // We'll rely on the CSS min-width instead of dynamically changing the layout
            // The form will be scrollable rather than getting too small
        }
    }
    
    private void setButtonActions() {
        googleSignUpButton.setOnAction(event -> {
            System.out.println("Sign up with Google clicked");
            // Implement Google sign up logic
        });
        
        facebookSignUpButton.setOnAction(event -> {
            System.out.println("Sign up with Facebook clicked");
            // Implement Facebook sign up logic
        });
        
        createAccountButton.setOnAction(this::handleCreateButtonAction);
        
        connectButton.setOnAction(event -> {
            System.out.println("Connect to account clicked");
            showLoginDialog();
        });
    }
    
    @FXML
    public void handleCreateButtonAction(ActionEvent event) {
        try {
            // Create semi-transparent overlay
            Pane overlay = new Pane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.setPrefSize(mainContainer.getWidth(), mainContainer.getHeight());
            
            // Load the first step of registration dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterDialogStep1.fxml"));
            Parent dialogRoot = loader.load();
            
            RegisterDialogController controller = loader.getController();
            
            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            
            Scene scene = new Scene(dialogRoot);
            dialogStage.setScene(scene);
            
            controller.setDialogStage(dialogStage);
            
            // Add overlay to the mainContainer
            mainContainer.getChildren().add(overlay);
            
            // Animations
            FadeTransition fadeInOverlay = new FadeTransition(Duration.millis(300), overlay);
            fadeInOverlay.setFromValue(0);
            fadeInOverlay.setToValue(1);
            
            // Center the dialog properly on the screen
            Stage primaryStage = (Stage) mainContainer.getScene().getWindow();
            double dialogWidth = 500; // Width from FXML
            double dialogHeight = 460; // Height from FXML
            double centerX = primaryStage.getX() + (primaryStage.getWidth() / 2) - (dialogWidth / 2);
            double centerY = primaryStage.getY() + (primaryStage.getHeight() / 2) - (dialogHeight / 2);
            
            dialogStage.setX(centerX);
            dialogStage.setY(centerY);
            
            // Show overlay animation
            fadeInOverlay.play();
            
            // Pass the overlay to the controller so it can be removed when the registration is complete
            controller.setMainContainerOverlay(overlay, mainContainer);
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // When dialog is closed, remove the overlay with animation
            FadeTransition fadeOutOverlay = new FadeTransition(Duration.millis(300), overlay);
            fadeOutOverlay.setFromValue(1);
            fadeOutOverlay.setToValue(0);
            fadeOutOverlay.setOnFinished(e -> mainContainer.getChildren().remove(overlay));
            fadeOutOverlay.play();
            
        } catch (IOException e) {
            System.out.println("Could not load registration dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static User getAuthenticatedUser() {
        return authenticatedUser;
    }
    
    private void showLoginDialog() {
        try {
            // Create semi-transparent overlay
            Pane overlay = new Pane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            overlay.setPrefSize(mainContainer.getWidth(), mainContainer.getHeight());
            
            // Load the login dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginDialog.fxml"));
            AnchorPane loginDialogPane = loader.load();
            loginDialogPane.setMaxWidth(450);
            loginDialogPane.setMaxHeight(350);
            
            // Get the controller
            LoginDialogController controller = loader.getController();
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(mainContainer.getScene().getWindow());
            
            // Set the dialog scene
            Scene scene = new Scene(loginDialogPane);
            dialogStage.setScene(scene);
            
            // Set the stage in the controller
            controller.setDialogStage(dialogStage);
            
            // Store the controller reference for later use
            lastLoginDialogController = controller;
            
            // Add overlay to the mainContainer
            mainContainer.getChildren().add(overlay);
            
            // Animations
            FadeTransition fadeInOverlay = new FadeTransition(Duration.millis(300), overlay);
            fadeInOverlay.setFromValue(0);
            fadeInOverlay.setToValue(1);
            
            // Center the dialog properly
            Stage primaryStage = (Stage) mainContainer.getScene().getWindow();
            double centerX = primaryStage.getX() + (primaryStage.getWidth() / 2) - (loginDialogPane.getMaxWidth() / 2);
            double centerY = primaryStage.getY() + (primaryStage.getHeight() / 2) - (loginDialogPane.getMaxHeight() / 2);
            
            dialogStage.setX(centerX);
            dialogStage.setY(centerY);
            
            // Show dialog and overlay with animations
            fadeInOverlay.play();
            dialogStage.showAndWait();
            
            // When dialog is closed, remove the overlay with animation
            FadeTransition fadeOutOverlay = new FadeTransition(Duration.millis(300), overlay);
            fadeOutOverlay.setFromValue(1);
            fadeOutOverlay.setToValue(0);
            fadeOutOverlay.setOnFinished(e -> mainContainer.getChildren().remove(overlay));
            fadeOutOverlay.play();
            
            // Check if login was successful
            if (controller.isLoginSuccessful()) {
                System.out.println("Login successful!");
                
                authenticatedUser = controller.getAuthenticatedUser();
                if (authenticatedUser != null) {
                    handleLoginSuccess(authenticatedUser);
                } else {
                    // Show error if user is null
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Authentication Error");
                    alert.setContentText("Could not retrieve user information after login.");
                    alert.showAndWait();
                }
            }
            
        } catch (IOException e) {
            System.out.println("Could not load login dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleLoginSuccess(User user) {
        try {
            authenticatedUser = user;
            
            // Determine which view to load based on user type
            String fxmlPath;
            if (user.getType().equalsIgnoreCase("teacher") || 
                user.getType().equalsIgnoreCase("professeur") ||
                user.getType().equalsIgnoreCase("prof") ||
                user.getType().equals("1")) {
                fxmlPath = "/view/Evenement.fxml";
            } else {
                fxmlPath = "/view/EvenementStudent.fxml";
            }
            
            // Load the appropriate view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Set the user in the controller
            if (user.getType().equalsIgnoreCase("teacher") || 
                user.getType().equalsIgnoreCase("professeur") ||
                user.getType().equalsIgnoreCase("prof") ||
                user.getType().equals("1")) {
                EvenementController controller = loader.getController();
                controller.setCurrentUser(user);
            } else {
                EvenementStudentController controller = loader.getController();
                controller.setCurrentUser(user);
            }
            
            // Create and show the new scene
            Stage stage = (Stage) connectButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            
            System.out.println("Successfully logged in as: " + user.getName() + " (Type: " + user.getType() + ")");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load the events view.");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 