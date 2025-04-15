package Controllers.Navigation;

import com.example.pijava.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class NavbarController {
    @FXML
    private Button homeButton;
    @FXML
    private Button coursButton;
    @FXML
    private Button examenButton;
    @FXML
    private Button evenementsButton;
    @FXML
    private Button forumButton;
    @FXML
    private Button userProfileButton;

    @FXML
    private void handleHomeNavigation() {
        loadView("/view/Home.fxml");
    }

    @FXML
    private void handleCoursNavigation() {
        loadView("/view/Cours.fxml");
    }

    @FXML
    private void handleExamenNavigation() {
        loadView("/view/Examen.fxml");
    }

    @FXML
    private void handleEvenementsNavigation() {
        loadView("/view/Evenement.fxml");
    }

    @FXML
    private void handleForumNavigation() {
        loadView("/view/Forum.fxml");
    }

    @FXML
    private void handleUserProfile() {
        loadView("/view/UserProfile.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) homeButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/styles/style.css").toExternalForm());


            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 