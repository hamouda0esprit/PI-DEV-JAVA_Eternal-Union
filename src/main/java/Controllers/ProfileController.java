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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.UUID;

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

            // Load profile image
            loadProfileImage();

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
                // Check if it's a default avatar (starts with A)
                if (imageUrl.startsWith("A")) {
                    // Load default avatar from Images folder
                    InputStream imageStream = getClass().getResourceAsStream("/Images/" + imageUrl);
                    if (imageStream != null) {
                        Image image = new Image(imageStream);
                        profileImageView.setImage(image);
                        return;
                    }
                } else if (imageUrl.startsWith("ProfilesIMG/")) {
                    // Handle old path format for backward compatibility
                    InputStream imageStream = getClass().getResourceAsStream("/" + imageUrl);
                    if (imageStream != null) {
                        Image image = new Image(imageStream);
                        profileImageView.setImage(image);
                        return;
                    }
                } else if (imageUrl.startsWith("uploads/images/")) {
                    // Load from the new Symfony uploads directory
                    File imageFile = new File("C:\\Users\\WorKenX\\Desktop\\Engineering\\Third year\\Project PI\\PiDev-EternalUnion-main\\public\\" + imageUrl);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        profileImageView.setImage(image);
                        return;
                    }
                } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    // Handle URL
                    Image image = new Image(imageUrl);
                    if (!image.isError()) {
                        profileImageView.setImage(image);
                        return;
                    }
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
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );

            File selectedFile = fileChooser.showOpenDialog(profileContainer.getScene().getWindow());
            if (selectedFile != null) {
                // Validate file extension
                String fileName = selectedFile.getName().toLowerCase();
                if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                    showAlert("Error", "Invalid image format. Please use PNG, JPG, or JPEG");
                    return;
                }

                // Try to load the image to validate it's a proper image file
                try {
                    Image image = new Image(selectedFile.toURI().toString());
                    if (image.isError()) {
                        showAlert("Error", "Invalid image file. Please select a valid image.");
                        return;
                    }

                    // Copy the image to ProfilesIMG folder
                    String newImagePath = copyImageToProfilesFolder(selectedFile);
                    if (newImagePath == null) {
                        showAlert("Error", "Failed to save the image. Please try again.");
                        return;
                    }

                    // Update the user's image path
                    currentUser.setImg(newImagePath);

                    // Update the profile image view
                    profileImageView.setImage(image);

                    // Save changes to database
                    userService.updateUser(currentUser);

                    showAlert("Success", "Profile picture updated successfully!");

                } catch (Exception e) {
                    showAlert("Error", "Invalid image file. Please select a valid image.");
                    System.err.println("Error loading custom image: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error in handleChangeAvatar: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to change profile picture: " + e.getMessage());
        }
    }

    private String copyImageToProfilesFolder(File sourceFile) {
        try {
            // Create ProfilesIMG directory if it doesn't exist
            File profilesDir = new File("C:\\Users\\WorKenX\\Desktop\\Engineering\\Third year\\Project PI\\PiDev-EternalUnion-main\\public\\uploads\\images");
            if (!profilesDir.exists()) {
                profilesDir.mkdirs();
            }

            // Generate a unique filename using UUID
            String extension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + extension;
            File destinationFile = new File(profilesDir, newFileName);

            // Copy the file
            java.nio.file.Files.copy(
                    sourceFile.toPath(),
                    destinationFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // Return the relative path to be stored in the database
            return "uploads/images/" + newFileName;
        } catch (IOException e) {
            System.err.println("Error copying image file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
            Parent root = loader.load();

            // Get the current stage from any available node
            Stage stage = null;
            if (profileContainer != null && profileContainer.getScene() != null) {
                stage = (Stage) profileContainer.getScene().getWindow();
            } else if (profileImageView != null && profileImageView.getScene() != null) {
                stage = (Stage) profileImageView.getScene().getWindow();
            } else if (nameField != null && nameField.getScene() != null) {
                stage = (Stage) nameField.getScene().getWindow();
            }

            if (stage != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("LOE - Home");
            } else {
                System.err.println("Could not find stage to update scene");
            }
        } catch (Exception e) {
            System.err.println("Error going back to home: " + e.getMessage());
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
            Parent root = loader.load();

            // Get the current stage from any available node
            Stage stage = null;
            if (profileContainer != null && profileContainer.getScene() != null) {
                stage = (Stage) profileContainer.getScene().getWindow();
            } else if (profileImageView != null && profileImageView.getScene() != null) {
                stage = (Stage) profileImageView.getScene().getWindow();
            } else if (nameField != null && nameField.getScene() != null) {
                stage = (Stage) nameField.getScene().getWindow();
            }

            if (stage != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("LOE - Home");
            } else {
                System.err.println("Could not find stage to update scene");
            }
        } catch (Exception e) {
            System.err.println("Error going back to home: " + e.getMessage());
        }
    }

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();

            // Get the current stage from any available node
            Stage stage = null;
            if (profileContainer != null && profileContainer.getScene() != null) {
                stage = (Stage) profileContainer.getScene().getWindow();
            } else if (profileImageView != null && profileImageView.getScene() != null) {
                stage = (Stage) profileImageView.getScene().getWindow();
            } else if (nameField != null && nameField.getScene() != null) {
                stage = (Stage) nameField.getScene().getWindow();
            }

            if (stage != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("LOE - Login");
            } else {
                System.err.println("Could not find stage to update scene");
            }
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