package Controllers;

import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.UserService;
import utils.PasswordUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;

public class LoginDialogController implements Initializable {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button closeButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button connectButton;

    private Stage dialogStage;
    private UserService userService;
    private boolean loginSuccessful = false;
    private User authenticatedUser = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = new UserService();
        closeButton.setOnAction(e -> closeDialog());
        cancelButton.setOnAction(e -> closeDialog());
        connectButton.setOnAction(e -> handleLogin());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            // Show error message for empty fields
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText("Missing Information");
            alert.setContentText("Please enter both email and password.");
            alert.showAndWait();
            return;
        }

        try {
            // Verify credentials against database
            authenticatedUser = authenticateUser(email, password);
            
            if (authenticatedUser == null) {
                // No user found with this email
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText("Authentication Error");
                alert.setContentText("No account found with this email. Please check your email or register.");
                alert.showAndWait();
                return;
            }
            
            // Check if the account is locked
            if (authenticatedUser.getWarnings() >= 3) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText("Account locked");
                alert.setContentText("Your account has been locked due to multiple failed login attempts.");
                alert.showAndWait();
                return;
            }
            
            // Check if password verification was successful
            // If warnings were just incremented, it means password was wrong
            if (authenticatedUser.getWarnings() > 0 && !PasswordUtils.verifyPassword(password, authenticatedUser.getPassword())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText("Authentication Error");
                alert.setContentText("Invalid password. Please try again.");
                alert.showAndWait();
                return;
            }
            
            // At this point login was successful, check verification status
            String verifiedStatus = authenticatedUser.getVerified();
            System.out.println("Checking verification status: " + verifiedStatus);
            
            if (verifiedStatus == null || !"1".equals(verifiedStatus.trim())) {
                System.out.println("User is not verified, showing verification dialog");
                // User is not verified, show verification dialog
                showVerificationDialog();
            } else {
                System.out.println("User is verified, proceeding with login");
                // User is verified, proceed with login
                loginSuccessful = true;
                closeDialog();
            }
            
        } catch (Exception e) {
            // Show error message for database errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Connection Problem");
            alert.setContentText("Could not connect to the database: " + e.getMessage());
            alert.showAndWait();

            System.out.println("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private User authenticateUser(String email, String password) {
        try {
            System.out.println("Attempting to authenticate user with email: " + email);

            // First, get the user by email
            String query = "SELECT * FROM user WHERE email = ?";
            java.sql.PreparedStatement stmt = userService.getConnection().prepareStatement(query);
            stmt.setString(1, email);

            java.sql.ResultSet rs = stmt.executeQuery();

            // If a row is returned, check the password
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setType(rs.getString("type"));
                user.setPhone(rs.getInt("phone"));
                user.setRate(rs.getDouble("rate"));
                user.setDate_of_birth(rs.getDate("date_of_birth"));
                
                // Get the stored hashed password
                String storedHashedPassword = rs.getString("password");
                user.setPassword(storedHashedPassword);
                
                user.setImg(rs.getString("img"));
                user.setScore(rs.getInt("score"));
                user.setBio(rs.getString("bio"));
                user.setVerified(rs.getString("verified"));
                user.setGoogle_id(rs.getString("google_id"));
                user.setWarnings(rs.getInt("warnings"));

                // Verify the password using PasswordUtils
                boolean passwordMatches = PasswordUtils.verifyPassword(password, storedHashedPassword);
                
                if (passwordMatches) {
                    System.out.println("Authentication successful for user: " + user.getName());
                    System.out.println("User verification status: " + user.getVerified());
                    return user;
                } else {
                    System.out.println("Password does not match for user: " + user.getName());
                    // Increment warning counter for failed login attempts
                    user.setWarnings(user.getWarnings() + 1);
                    // Update warning count in database
                    updateWarningCount(user.getId(), user.getWarnings());
                    return user; // Return user with incremented warnings
                }
            } else {
                System.out.println("No user found with email: " + email);
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Database error during authentication: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private void updateWarningCount(int userId, int warnings) {
        try {
            String query = "UPDATE user SET warnings = ? WHERE id = ?";
            java.sql.PreparedStatement stmt = userService.getConnection().prepareStatement(query);
            stmt.setInt(1, warnings);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("Updated warning count for user ID " + userId + " to " + warnings);
        } catch (SQLException e) {
            System.out.println("Error updating warning count: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showVerificationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/VerificationDialog.fxml"));
            Parent root = loader.load();

            VerificationDialogController controller = loader.getController();
            controller.setUserEmail(authenticatedUser.getEmail());

            Stage stage = new Stage();
            stage.setTitle("Email Verification");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // After verification dialog closes, check if user is now verified
            if (authenticatedUser != null) {
                authenticatedUser = userService.getUserByEmail(authenticatedUser.getEmail());
                if ("1".equals(authenticatedUser.getVerified())) {
                    loginSuccessful = true;
                    closeDialog();
                }
            }
        } catch (IOException | SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Verification Error");
            alert.setContentText("Failed to show verification dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 