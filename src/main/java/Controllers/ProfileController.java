package Controllers;

import entite.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.UserService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    @FXML
    private ImageView profileImageView;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextArea bioTextArea;
    
    @FXML
    private PasswordField currentPasswordField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label passwordErrorLabel;
    
    @FXML
    private Button changeAvatarButton;
    
    @FXML
    private Button uploadImageButton;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button logoutButton;
    
    private User currentUser;
    private UserService userService;
    private String selectedAvatarPath;
    private boolean passwordChanged = false;
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            loadUserData();
        } else {
            // If no user is set, redirect to login
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
                    Parent loginRoot = loader.load();
                    Stage stage = (Stage) profileImageView.getScene().getWindow();
                    stage.setScene(new Scene(loginRoot));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = new UserService();
        
        // Set up button actions
        saveButton.setOnAction(e -> saveChanges());
        cancelButton.setOnAction(e -> cancelChanges());
        changeAvatarButton.setOnAction(e -> showAvatarSelectionDialog());
        uploadImageButton.setOnAction(e -> uploadCustomImage());
        backButton.setOnAction(e -> goBack());
        logoutButton.setOnAction(e -> logout());
        
        // Set up password field listeners
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordChanged = true;
            validatePassword();
        });
        
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordChanged = true;
            validatePassword();
        });
    }
    
    private void loadUserData() {
        if (currentUser == null) {
            System.out.println("ERROR: No user set for profile page");
            return;
        }
        
        // Set basic info fields
        nameField.setText(currentUser.getName());
        emailField.setText(currentUser.getEmail());
        bioTextArea.setText(currentUser.getBio());
        
        // Load profile image
        loadProfileImage();
    }
    
    private void loadProfileImage() {
        try {
            if (selectedAvatarPath != null && !selectedAvatarPath.isEmpty()) {
                Image avatarImage = null;
                
                // Check if it's a default avatar
                if (selectedAvatarPath.startsWith("A")) {
                    // Load from resources
                    String imagePath = "/Images/" + selectedAvatarPath;
                    System.out.println("Loading avatar from resources: " + imagePath);
                    avatarImage = new Image(getClass().getResourceAsStream(imagePath));
                } else {
                    // Load from file system
                    File file = new File(selectedAvatarPath);
                    if (file.exists()) {
                        avatarImage = new Image(file.toURI().toString());
                    } else {
                        // Default avatar if not found
                        avatarImage = new Image(getClass().getResourceAsStream("/Images/A1.png"));
                    }
                }
                
                profileImageView.setImage(avatarImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading profile image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveChanges() {
        try {
            boolean hasChanges = false;
            
            // Check for name changes
            if (!nameField.getText().equals(currentUser.getName())) {
                currentUser.setName(nameField.getText());
                hasChanges = true;
            }
            
            // Check for email changes
            if (!emailField.getText().equals(currentUser.getEmail())) {
                // Validate email format
                if (!emailField.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
                    return;
                }
                
                // Check if email is already in use
                try {
                    User existingUser = userService.getUserByEmail(emailField.getText());
                    if (existingUser != null && existingUser.getId() != currentUser.getId()) {
                        showAlert(Alert.AlertType.ERROR, "Email Already in Use", 
                                "This email address is already associated with another account.");
                        return;
                    }
                } catch (SQLException ex) {
                    System.out.println("Error checking email: " + ex.getMessage());
                }
                
                currentUser.setEmail(emailField.getText());
                hasChanges = true;
            }
            
            // Check for bio changes
            if (!bioTextArea.getText().equals(currentUser.getBio())) {
                currentUser.setBio(bioTextArea.getText());
                hasChanges = true;
            }
            
            // Check for image changes
            if (!selectedAvatarPath.equals(currentUser.getImg())) {
                currentUser.setImg(selectedAvatarPath);
                hasChanges = true;
            }
            
            // Handle password change
            if (passwordChanged) {
                if (currentPasswordField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Password Required", 
                             "Please enter your current password to confirm changes.");
                    return;
                }
                
                // Verify current password
                if (!currentPasswordField.getText().equals(currentUser.getPassword())) {
                    showAlert(Alert.AlertType.ERROR, "Incorrect Password", 
                             "The current password you entered is incorrect.");
                    return;
                }
                
                // Validate new password
                if (!validatePassword()) {
                    return;
                }
                
                currentUser.setPassword(newPasswordField.getText());
                hasChanges = true;
            }
            
            // Save changes to database if there are any
            if (hasChanges) {
                userService.updateUser(currentUser);
                showAlert(Alert.AlertType.INFORMATION, "Profile Updated", 
                         "Your profile has been updated successfully!");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "No Changes", 
                         "No changes were made to your profile.");
            }
            
        } catch (Exception e) {
            System.out.println("Error saving changes: " + e.getMessage());
            e.printStackTrace();
            
            showAlert(Alert.AlertType.ERROR, "Error Saving Changes", 
                     "An error occurred while saving your changes: " + e.getMessage());
        }
    }
    
    private void cancelChanges() {
        loadUserData(); // Reload the original data
        
        // Clear password fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        passwordChanged = false;
        
        // Hide error labels
        passwordErrorLabel.setVisible(false);
    }
    
    private boolean validatePassword() {
        String password = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        boolean isValid = true;
        
        // Clear previous error
        passwordErrorLabel.setVisible(false);
        
        // Check if password is empty
        if (password.isEmpty()) {
            passwordErrorLabel.setText("New password is required");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        } 
        // Check password length
        else if (password.length() < 6) {
            passwordErrorLabel.setText("Password must be at least 6 characters");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        }
        // Check if password contains a number and a letter
        else if (!password.matches(".*\\d.*") || !password.matches(".*[a-z].*")) {
            passwordErrorLabel.setText("Password must contain a number and a lowercase letter");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        }
        // Check if passwords match
        else if (!password.equals(confirmPassword)) {
            passwordErrorLabel.setText("Passwords do not match");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        }
        
        return isValid;
    }
    
    private void showAvatarSelectionDialog() {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select Avatar");
            selectedAvatarPath = "A" + (Math.random() * 6 + 1);
            selectedAvatarPath = "A" + (int)Math.ceil(Math.random() * 6) + ".png";
            
            loadProfileImage();
            
            showAlert(Alert.AlertType.INFORMATION, "Avatar Changed", 
                     "Your avatar has been changed. Save your profile to apply changes.");
            
        } catch (Exception e) {
            System.out.println("Error showing avatar dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void uploadCustomImage() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            
            Stage stage = (Stage) uploadImageButton.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);
            
            if (selectedFile != null) {
                selectedAvatarPath = selectedFile.getAbsolutePath();
                loadProfileImage();
                
                showAlert(Alert.AlertType.INFORMATION, "Image Uploaded", 
                         "Your custom image has been uploaded. Save your profile to apply changes.");
            }
        } catch (Exception e) {
            System.out.println("Error uploading image: " + e.getMessage());
            e.printStackTrace();
            
            showAlert(Alert.AlertType.ERROR, "Upload Error", 
                     "An error occurred while uploading the image: " + e.getMessage());
        }
    }
    
    private void goBack() {
        // For now, just go back to the login screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent loginRoot = loader.load();
            
            LoginController controller = loader.getController();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(loginRoot, 800, 600);
            
            controller.setStage(stage);
            
            stage.setTitle("LOE - Start your learning journey");
            stage.setScene(scene);
            
        } catch (IOException e) {
            System.out.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void logout() {
        // Just go back to login screen
        goBack();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 