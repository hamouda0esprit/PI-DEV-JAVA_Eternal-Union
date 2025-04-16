package Controllers;

import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.UserService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    
    @FXML
    private Label userCountLabel;
    
    @FXML
    private Label courseCountLabel;
    
    @FXML
    private Label forumCountLabel;
    
    @FXML
    private Label eventCountLabel;
    
    @FXML
    private TableView<?> recentActivityTable;
    
    @FXML
    private TableColumn<?, ?> activityDateColumn;
    
    @FXML
    private TableColumn<?, ?> activityTypeColumn;
    
    @FXML
    private TableColumn<?, ?> activityDescriptionColumn;
    
    @FXML
    private TableColumn<?, ?> activityUserColumn;
    
    @FXML
    private Button refreshButton;
    
    private UserService userService;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = new UserService();
        
        refreshButton.setOnAction(event -> loadDashboardData());
        
        loadDashboardData();
    }
    
    private void loadDashboardData() {
        try {
            // Load user statistics
            int userCount = userService.getAllUsers().size();
            userCountLabel.setText(String.valueOf(userCount));
            
            // For this example, we'll use placeholder data for other stats
            // In a real application, you would fetch this from the appropriate services
            courseCountLabel.setText("26");
            forumCountLabel.setText("15");
            eventCountLabel.setText("8");
            
            // TODO: Load recent activity data when available
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", 
                    "Impossible de charger les données du tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goToUsersAdmin() {
        navigateTo("/view/AdminUser.fxml");
    }
    
    @FXML
    private void goToCoursesAdmin() {
        navigateTo("/view/AdminCourse.fxml");
    }
    
    @FXML
    private void goToForumsAdmin() {
        navigateTo("/view/AdminForum.fxml");
    }
    
    @FXML
    private void goToEventsAdmin() {
        navigateTo("/view/AdminEvenement.fxml");
    }
    
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) refreshButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            System.out.println("Error navigating to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", 
                    "Impossible de naviguer vers " + fxmlPath + ": " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 