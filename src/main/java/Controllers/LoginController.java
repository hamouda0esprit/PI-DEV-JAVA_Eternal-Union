package Controllers;

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
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

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
    
    private static final String CLIENT_ID = "66301700661-aihhgo3hni766b8o7nv1auo88u8q7jd3.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String SCOPE = "email profile";
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set action handlers
        setButtonActions();
        
        // Apply responsive design after the stage is set and components are loaded
        javafx.application.Platform.runLater(this::setupResponsiveDesign);
        
        // Add face recognition button handler
        facebookSignUpButton.setOnAction(event -> handleFaceRecognitionSignUp());
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
            handleGoogleSignIn();
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
                
                User authenticatedUser = controller.getAuthenticatedUser();
                if (authenticatedUser != null) {
                    // Load the profile page
                    try {
                        FXMLLoader profileLoader = new FXMLLoader(getClass().getResource("/view/Profile.fxml"));
                        Parent profileRoot = profileLoader.load();
                        
                        // Get the profile controller and set the authenticated user
                        ProfileController profileController = profileLoader.getController();
                        profileController.setCurrentUser(authenticatedUser.getName());
                        
                        // Make sure the stage is available
                        if (stage == null) {
                            stage = (Stage) mainContainer.getScene().getWindow();
                        }
                        
                        // Replace the current scene with the profile page
                        Scene profileScene = new Scene(profileRoot);
                        stage.setTitle("LOE - My Profile");
                        stage.setScene(profileScene);
                        
                        System.out.println("Successfully redirected to profile page for user: " + authenticatedUser.getName());
                    } catch (IOException e) {
                        System.out.println("Error loading profile page: " + e.getMessage());
                        e.printStackTrace();
                        
                        // Show error alert
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Could not load profile page");
                        alert.setContentText("An error occurred: " + e.getMessage());
                        alert.showAndWait();
                    }
                } else {
                    // Fallback if user is null (should not happen)
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Login Successful");
                    alert.setHeaderText("Welcome back!");
                    alert.setContentText("You are now connected to your account.");
                    alert.showAndWait();
                }
            }
            
        } catch (IOException e) {
            System.out.println("Could not load login dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateRandomPassword() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String allChars = upperCaseLetters + lowerCaseLetters + numbers;
        
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one of each type
        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        
        // Add remaining characters
        for (int i = 0; i < 9; i++) { // Total length will be 12 characters
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = passwordArray[index];
            passwordArray[index] = passwordArray[i];
            passwordArray[i] = temp;
        }
        
        return new String(passwordArray);
    }

    private void handleGoogleSignIn() {
        try {
            // Create the authorization URL
            String authUrl = String.format("%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s",
                    AUTH_URL, CLIENT_ID, REDIRECT_URI, SCOPE);

            // Create WebView to show Google Sign-In page
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            
            // Create a new stage for the WebView
            Stage webStage = new Stage();
            webStage.setTitle("Google Sign-In");
            webStage.setScene(new Scene(new StackPane(webView), 600, 600));
            
            // Handle URL changes to capture the authorization code
            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.startsWith(REDIRECT_URI)) {
                    // Extract the authorization code from the URL
                    String code = newValue.split("code=")[1].split("&")[0];
                    
                    // Exchange the code for tokens
                    CompletableFuture.runAsync(() -> {
                        try {
                            // Create the token request
                            String tokenRequestBody = String.format(
                                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                                code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);

                            HttpClient client = HttpClient.newHttpClient();
                            HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(TOKEN_URL))
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                                .build();

                            // Send the request and get the response
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            
                            // Parse the response to get the access token
                            String responseBody = response.body();
                            System.out.println("Raw response body: " + responseBody); // Debug print
                            
                            String accessToken = null;
                            
                            // Parse the JSON response
                            try {
                                // Create a JSON parser
                                JsonReader jsonReader = Json.createReader(new StringReader(responseBody));
                                JsonObject jsonObject = jsonReader.readObject();
                                
                                // Get the access token from the JSON object
                                accessToken = jsonObject.getString("access_token");
                                System.out.println("Successfully extracted access token: " + accessToken);
                                
                                if (accessToken == null || accessToken.isEmpty()) {
                                    throw new Exception("Access token is null or empty in JSON response");
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing access token: " + e.getMessage());
                                throw e;
                            }

                            if (accessToken != null && !accessToken.isEmpty()) {
                                // Successfully got the access token
                                System.out.println("Access token received successfully");
                                
                                // Get user info using the access token
                                HttpRequest userInfoRequest = HttpRequest.newBuilder()
                                    .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                                    .header("Authorization", "Bearer " + accessToken)
                                    .GET()
                                    .build();
                                
                                HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
                                String userInfoJson = userInfoResponse.body();
                                System.out.println("User info response: " + userInfoJson);
                                
                                // Parse user info
                                JsonReader userInfoReader = Json.createReader(new StringReader(userInfoJson));
                                JsonObject userInfo = userInfoReader.readObject();
                                
                                // Extract user information
                                String googleId = userInfo.getString("id");
                                String email = userInfo.getString("email");
                                String name = userInfo.getString("name");
                                String pictureUrl = userInfo.getString("picture");
                                boolean emailVerified = userInfo.getBoolean("verified_email");
                                
                                System.out.println("User Information:");
                                System.out.println("Google ID: " + googleId);
                                System.out.println("Name: " + name);
                                System.out.println("Email: " + email);
                                System.out.println("Email Verified: " + emailVerified);
                                System.out.println("Profile Picture URL: " + pictureUrl);
                                
                                // Check if user exists in database
                                service.UserService userService = new service.UserService();
                                User existingUser = userService.getUserByEmail(email);
                                
                                if (existingUser != null) {
                                    // User exists, update their information
                                    existingUser.setName(name);
                                    existingUser.setImg(pictureUrl);
                                    existingUser.setGoogle_id(googleId);
                                    existingUser.setVerified("1");  // Using "1" for verified status
                                    userService.updateUser(existingUser);
                                    
                                    // Close the WebView stage and redirect to profile
                                    javafx.application.Platform.runLater(() -> {
                                        webStage.close();
                                        try {
                                            // Load the profile page
                                            FXMLLoader profileLoader = new FXMLLoader(getClass().getResource("/view/Profile.fxml"));
                                            Parent profileRoot = profileLoader.load();
                                            
                                            // Get the profile controller and set the authenticated user
                                            ProfileController profileController = profileLoader.getController();
                                            profileController.setCurrentUser(existingUser.getName());
                                            
                                            // Replace the current scene with the profile page
                                            Scene profileScene = new Scene(profileRoot);
                                            stage.setTitle("LOE - My Profile");
                                            stage.setScene(profileScene);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load profile page: " + e.getMessage());
                                        }
                                    });
                                } else {
                                    // User doesn't exist, create new user
                                    User newUser = new User();
                                    newUser.setName(userInfo.get("name").toString());
                                    newUser.setEmail(userInfo.get("email").toString());
                                    newUser.setPhone(0);
                                    newUser.setType("user");
                                    newUser.setDate_of_birth(new Date());
                                    newUser.setPassword(null);
                                    newUser.setImg(userInfo.get("picture").toString());
                                    newUser.setScore(0);
                                    newUser.setBio("No bio yet");
                                    newUser.setVerified("1");  // Using "1" for verified status
                                    newUser.setGoogle_id(userInfo.get("sub").toString());
                                    newUser.setRate(0.0);
                                    
                                    try {
                                        userService.addUser(newUser);
                                        System.out.println("New user created successfully");
                                        
                                        // Close the WebView stage and redirect to profile
                                        javafx.application.Platform.runLater(() -> {
                                            webStage.close();
                                            try {
                                                // Load the profile page
                                                FXMLLoader profileLoader = new FXMLLoader(getClass().getResource("/view/Profile.fxml"));
                                                Parent profileRoot = profileLoader.load();
                                                
                                                // Get the profile controller and set the authenticated user
                                                ProfileController profileController = profileLoader.getController();
                                                profileController.setCurrentUser(newUser.getName());
                                                
                                                // Replace the current scene with the profile page
                                                Scene profileScene = new Scene(profileRoot);
                                                stage.setTitle("LOE - My Profile");
                                                stage.setScene(profileScene);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load profile page: " + e.getMessage());
                                            }
                                        });
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        javafx.application.Platform.runLater(() -> {
                                            webStage.close();
                                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create user: " + e.getMessage());
                                        });
                                    }
                                }
                            } else {
                                // Failed to get access token
                                throw new Exception("Failed to get access token from response: " + responseBody);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            javafx.application.Platform.runLater(() -> {
                                webStage.close();
                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to sign in with Google: " + e.getMessage());
                            });
                        }
                    });
                }
            });

            // Load the authorization URL
            webEngine.load(authUrl);
            webStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to initialize Google Sign-In: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleFaceRecognitionSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/WebcamCaptureDialog.fxml"));
            Parent root = loader.load();
            
            WebcamCaptureController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.setTitle("Face Recognition");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Clean up after dialog closes
            controller.stopCamera();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open webcam");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
} 