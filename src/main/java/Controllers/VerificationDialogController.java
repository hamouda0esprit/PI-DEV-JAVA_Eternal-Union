package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserService;
import util.EmailUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class VerificationDialogController implements Initializable {
    @FXML
    private TextField verificationCodeField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button verifyButton;
    
    @FXML
    private Button resendButton;
    
    private String userEmail;
    private String expectedCode;
    private UserService userService;
    private EmailUtil emailUtil;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        emailUtil = new EmailUtil();
        
        // Generate initial verification code
        expectedCode = generateVerificationCode();
        
        // Set up button actions
        verifyButton.setOnAction(event -> handleVerification());
        resendButton.setOnAction(event -> handleResendCode());
    }
    
    public void setUserEmail(String email) {
        this.userEmail = email;
        // Send initial verification email
        sendVerificationEmail();
    }
    
    @FXML
    private void handleVerification() {
        String enteredCode = verificationCodeField.getText();
        
        if (enteredCode.equals(expectedCode)) {
            try {
                userService.updateUserVerification(userEmail, "1");  
                closeDialog();
            } catch (SQLException e) {
                showError("Error updating verification status: " + e.getMessage());
            }
        } else {
            showError("Invalid verification code. Please try again.");
        }
    }
    
    private void handleResendCode() {
        // Generate new verification code
        expectedCode = generateVerificationCode();
        sendVerificationEmail();
        showError("New verification code sent to your email");
    }
    
    private void sendVerificationEmail() {
        try {
            String subject = "LOE - Email Verification";
            String content = "Your verification code is: " + expectedCode;
            emailUtil.sendEmail(userEmail, subject, content);
        } catch (Exception e) {
            showError("Error sending verification email: " + e.getMessage());
        }
    }
    
    private String generateVerificationCode() {
        // Generate a 6-digit verification code
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void closeDialog() {
        Stage stage = (Stage) verificationCodeField.getScene().getWindow();
        stage.close();
    }
} 