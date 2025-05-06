package Controllers;

import Controllers.Navigation.AdminNavigationStateManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.image.Image;

public class AdminSidebarController implements Initializable {
    @FXML private Button dashboardButton;
    @FXML private Button usersButton;
    @FXML private Button forumsButton;
    @FXML private Button eventsButton;
    @FXML private Button examsButton;
    @FXML private Button coursesButton;
    @FXML private Button backButton;

    private List<Button> navButtons;
    private AdminNavigationStateManager navigationState;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the list of navigation buttons
        navButtons = Arrays.asList(
                dashboardButton, usersButton, forumsButton,
                eventsButton, examsButton, coursesButton
        );

        navigationState = AdminNavigationStateManager.getInstance();

        // Listen for changes in the current view
        navigationState.currentViewProperty().addListener((observable, oldValue, newValue) -> {
            updateActiveButton(newValue);
        });

        // Set initial state
        String currentView = navigationState.getCurrentView();
        if (currentView != null) {
            updateActiveButton(currentView);
        }

        // Setup click handlers for navigation
        setupNavigationHandlers();
    }

    private void updateActiveButton(String view) {
        // Remove active class from all buttons
        navButtons.forEach(button -> button.getStyleClass().remove("active"));

        // Add active class to the appropriate button
        switch (view) {
            case "Dashboard":
                dashboardButton.getStyleClass().add("active");
                break;
            case "Users":
                usersButton.getStyleClass().add("active");
                break;
            case "Forum":
                forumsButton.getStyleClass().add("active");
                break;
            case "Evenement":
                eventsButton.getStyleClass().add("active");
                break;
            case "Exams":
                examsButton.getStyleClass().add("active");
                break;
            case "Courses":
                coursesButton.getStyleClass().add("active");
                break;
        }
    }

    private void setupNavigationHandlers() {
        dashboardButton.setOnAction(e -> {
            navigationState.setCurrentView("Dashboard");
            navigateTo("Dashboard");
        });

        usersButton.setOnAction(e -> {
            navigationState.setCurrentView("Users");
            navigateTo("Users");
        });

        forumsButton.setOnAction(e -> {
            navigationState.setCurrentView("Forum");
            navigateTo("Forum");
        });

        eventsButton.setOnAction(e -> {
            navigationState.setCurrentView("Evenement");
            navigateTo("Evenement");
        });

        examsButton.setOnAction(e -> {
            navigationState.setCurrentView("Exams");
            navigateTo("Panel");
        });

        coursesButton.setOnAction(e -> {
            navigationState.setCurrentView("Courses");
            navigateTo("/../../AdminCours");
        });

        backButton.setOnAction(e -> {
            navButtons.forEach(button -> button.getStyleClass().remove("active"));
            returnToMainSite();
        });
    }

    private void navigateTo(String page) {
        try {
            String fxmlPath = "/view/Admin" + page + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            Scene scene = new Scene(root);

            // Add the stylesheet
            String css = getClass().getResource("/styles/admin.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);

            // Set the window icon
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            stage.getIcons().clear();
            stage.getIcons().add(icon);

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

            // Add the stylesheet
            String css = getClass().getResource("/styles/style.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);

            // Set the window icon
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            stage.getIcons().clear();
            stage.getIcons().add(icon);

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