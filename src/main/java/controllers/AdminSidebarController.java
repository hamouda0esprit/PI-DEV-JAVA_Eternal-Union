package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AdminSidebarController {
    @FXML private Button dashboardButton;
    @FXML private Button usersButton;
    @FXML private Button forumsButton;
    @FXML private Button eventsButton;
    @FXML private Button examsButton;
    @FXML private Button coursesButton;
    @FXML private Button backButton;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        // Initialize the list of navigation buttons
        navButtons = Arrays.asList(
            dashboardButton, usersButton, forumsButton,
            eventsButton, examsButton, coursesButton
        );

        // Setup click handlers for navigation
        setupNavigationHandlers();

        // Set initial active state for events button
        //eventsButton.getStyleClass().add("active");
    }

    public void setActiveSection(String section) {
        switch (section) {
            case "Dashboard":
                setActiveButton(dashboardButton);
                break;
            case "Users":
                setActiveButton(usersButton);
                break;
            case "Forum":
                setActiveButton(forumsButton);
                break;
            case "Evenement":
                setActiveButton(eventsButton);
                break;
            case "Exams":
                setActiveButton(examsButton);
                break;
            case "Courses":
                setActiveButton(coursesButton);
                break;
        }
    }


    private void setupNavigationHandlers() {
        dashboardButton.setOnAction(e -> {
            // Don't set active state, just navigate
            navigateTo("Dashboard");
        });
        
        usersButton.setOnAction(e -> {
            // Don't set active state, just navigate
            navigateTo("Users");
        });
        
        forumsButton.setOnAction(e -> {
            // Toggle the forums button
            setActiveButton(forumsButton);
            navigateTo("Forum");
        });
        
        eventsButton.setOnAction(e -> {
            // Toggle the events button
            setActiveButton(eventsButton);
            navigateTo("Evenement");
            // No need to navigate, we're already on the events page
        });
        
        examsButton.setOnAction(e -> {
            // Don't set active state, just navigate
            navigateTo("Exams");
        });
        
        coursesButton.setOnAction(e -> {
            // Don't set active state, just navigate
            navigateTo("Courses");
        });
        
        backButton.setOnAction(e -> {
            // Remove active state from all buttons when returning to main site
            navButtons.forEach(button -> button.getStyleClass().remove("active"));
            returnToMainSite();
        });
    }

    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        navButtons.forEach(button -> button.getStyleClass().remove("active"));
        // Add active class to clicked button
        activeButton.getStyleClass().add("active");
    }

    private void navigateTo(String page) {
        try {
            String fxmlPath = "/view/Admin" + page + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get the sidebar controller and set the active section
            //AdminSidebarController sidebarController = loader.getController();

            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not navigate to " + page + " page.");
        }
    }


    private void returnToMainSite() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Forum.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not return to main site.");
        }
    }

    private void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 