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
            
            if (authenticatedUser != null) {
                // Check verification status
                if (!"1".equals(authenticatedUser.getVerified())) {
                    // User is not verified, show verification dialog
                    showVerificationDialog();
                } else {
                    // User is verified, proceed with login
                    loginSuccessful = true;
                    closeDialog();
                }
            } else {
                // Show error message for invalid credentials
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText("Authentication Error");
                alert.setContentText("Invalid email or password. Please try again.");
                alert.showAndWait();
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
            
            // Create a SQL query to check email and password directly
            String query = "SELECT * FROM user WHERE email = ? AND password = ?";
            java.sql.PreparedStatement stmt = userService.getConnection().prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            java.sql.ResultSet rs = stmt.executeQuery();
            
            // If a row is returned, authentication is successful
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setType(rs.getString("type"));
                user.setPhone(rs.getInt("phone"));
                user.setRate(rs.getDouble("rate"));
                user.setDate_of_birth(rs.getDate("date_of_birth"));
                user.setPassword(rs.getString("password"));
                user.setImg(rs.getString("img"));
                user.setScore(rs.getInt("score"));
                user.setBio(rs.getString("bio"));
                
                System.out.println("Authentication successful for user: " + user.getName());
                
                return user;
            } else {
                System.out.println("No matching user found with provided email and password");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Database error during authentication: " + e.getMessage());
            e.printStackTrace();
            return null;
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