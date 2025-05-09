package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminSidebarController implements Initializable {
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button usersButton;
    
    @FXML
    private Button forumsButton;
    
    @FXML
    private Button coursesButton;
    
    @FXML
    private Button examsButton;
    
    @FXML
    private Button eventsButton;
    
    @FXML
    private Button backButton;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupButtons();
    }
    
    private void setupButtons() {
        dashboardButton.setOnAction(event -> navigateTo("/view/AdminDashboard.fxml"));
        usersButton.setOnAction(event -> navigateTo("/view/AdminUser.fxml"));
        forumsButton.setOnAction(event -> navigateTo("/view/AdminForum.fxml"));
        coursesButton.setOnAction(event -> navigateTo("/AdminCours.fxml"));
        examsButton.setOnAction(event -> navigateTo("/view/AdminPanel.fxml"));
        eventsButton.setOnAction(event -> navigateTo("/view/AdminEvenement.fxml"));
        
        backButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) backButton.getScene().getWindow();
                LoginController controller = loader.getController();
                controller.setStage(stage);
                
                Scene scene = new Scene(root);
                stage.setTitle("LOE - Start your learning journey");
                stage.setScene(scene);
            } catch (IOException e) {
                System.out.println("Error returning to login: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            System.out.println("Error navigating to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
} 