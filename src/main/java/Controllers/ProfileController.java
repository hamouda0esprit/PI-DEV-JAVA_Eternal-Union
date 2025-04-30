package Controllers;

import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    @FXML
    private VBox profileContainer;
    
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
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Label verificationStatusLabel;
    
    private User currentUser;
    private UserService userService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        
        // Set up button actions
        changeAvatarButton.setOnAction(event -> handleChangeAvatar());
        saveButton.setOnAction(event -> handleSaveChanges());
        cancelButton.setOnAction(event -> handleCancel());
        backButton.setOnAction(event -> goBack());
        logoutButton.setOnAction(event -> logout());
    }
    
    public void setCurrentUser(String username) {
        System.out.println("Setting current user in ProfileController: " + username);
        currentUser = userService.getUserByUsername(username);
        if (currentUser != null) {
            // Update UI with user data
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
            bioTextArea.setText(currentUser.getBio() != null ? currentUser.getBio() : "");
            
            // Set avatar image if available
            String imageUrl = currentUser.getImg();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    // Check if it's a valid URL
                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        Image avatarImage = new Image(imageUrl);
                        if (!avatarImage.isError()) {
                            profileImageView.setImage(avatarImage);
                        } else {
                            loadDefaultAvatar();
                        }
                    } else {
                        // Try to load from resources
                        InputStream imageStream = getClass().getResourceAsStream("/images/" + imageUrl);
                        if (imageStream != null) {
                            Image avatarImage = new Image(imageStream);
                            profileImageView.setImage(avatarImage);
                        } else {
                            loadDefaultAvatar();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile image: " + e.getMessage());
                    loadDefaultAvatar();
                }
            } else {
                loadDefaultAvatar();
            }
            
            // Update verification status
            if ("1".equals(currentUser.getVerified())) {
                verificationStatusLabel.setText("Verified");
                verificationStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                verificationStatusLabel.setText("Not Verified");
                verificationStatusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }
    
    private void updateProfileUI() {
        if (currentUser != null) {
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
            bioTextArea.setText(currentUser.getBio());
            
            if (currentUser.getImg() != null && !currentUser.getImg().isEmpty()) {
                Image avatarImage = new Image(currentUser.getImg());
                profileImageView.setImage(avatarImage);
            }
            
            // Update verification status
            if ("1".equals(currentUser.getVerified())) {
                verificationStatusLabel.setText("Verified");
                verificationStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                verificationStatusLabel.setText("Not Verified");
                verificationStatusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }
    
    private void loadProfileImage() {
        String imageUrl = currentUser.getImg();
        System.out.println("Loading profile image from URL: " + imageUrl);

        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Check if URL is valid
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    // If it's a local file path, try to load from resources
                    imageUrl = "/images/" + imageUrl;
                    System.out.println("Trying to load local image: " + imageUrl);
                }

                // Try to load the image
                Image image = new Image(imageUrl);
                if (!image.isError()) {
                    profileImageView.setImage(image);
                    return;
                }
            }

            // If we get here, either the URL was invalid or the image failed to load
            loadDefaultAvatar();
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
            loadDefaultAvatar();
        }
    }
    
    private void loadDefaultAvatar() {
        try {
            // Try to load default avatar from resources
            InputStream defaultAvatarStream = getClass().getResourceAsStream("/images/default-avatar.png");
            if (defaultAvatarStream != null) {
                Image defaultAvatar = new Image(defaultAvatarStream);
                profileImageView.setImage(defaultAvatar);
            } else {
                System.err.println("Default avatar resource not found");
                // Set a placeholder text or icon
                profileImageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error loading default avatar: " + e.getMessage());
            profileImageView.setImage(null);
        }
    }
    
    private void handleChangeAvatar() {
        // TODO: Implement avatar change functionality
        System.out.println("Change avatar clicked");
    }
    
    private void handleSaveChanges() {
        if (currentUser != null) {
            try {
                // Update user data
                currentUser.setName(nameField.getText());
                currentUser.setEmail(emailField.getText());
                currentUser.setBio(bioTextArea.getText());
                
                // Save changes to database
                userService.updateUser(currentUser);
                
                // Show success message
                showAlert("Success", "Profile updated successfully!");
                
                // Update verification status if needed
                if ("1".equals(currentUser.getVerified())) {
                    verificationStatusLabel.setText("Verified");
                    verificationStatusLabel.setStyle("-fx-text-fill: green;");
                }
            } catch (SQLException e) {
                showAlert("Error", "Failed to update profile: " + e.getMessage());
            }
        }
    }
    
    private boolean validatePasswordChange() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (currentPassword.isEmpty()) {
            showPasswordError("Please enter your current password");
            return false;
        }
        
        if (!currentPassword.equals(currentUser.getPassword())) {
            showPasswordError("Current password is incorrect");
            return false;
        }
        
        if (newPassword.isEmpty()) {
            showPasswordError("Please enter a new password");
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("New passwords do not match");
            return false;
        }
        
        passwordErrorLabel.setVisible(false);
        return true;
    }
    
    private void showPasswordError(String message) {
        passwordErrorLabel.setText(message);
        passwordErrorLabel.setVisible(true);
    }
    
    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        passwordErrorLabel.setVisible(false);
    }
    
    private void handleCancel() {
        updateProfileUI();
        clearPasswordFields();
    }
    
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) profileContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("LOE - Login");
        } catch (Exception e) {
            System.err.println("Error going back to login: " + e.getMessage());
        }
    }
    
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) profileContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("LOE - Login");
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showVerificationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/VerificationDialog.fxml"));
            Parent root = loader.load();
            
            VerificationDialogController controller = loader.getController();
            controller.setUserEmail(currentUser.getEmail());
            
            Stage stage = new Stage();
            stage.setTitle("Email Verification");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh user data after verification
            if (currentUser != null) {
                currentUser = userService.getUserByEmail(currentUser.getEmail());
                if ("1".equals(currentUser.getVerified())) {
                    updateProfileUI();
                }
            }
        } catch (IOException | SQLException e) {
            showAlert("Error", "Failed to show verification dialog: " + e.getMessage());
        }
    }
} 