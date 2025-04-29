package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import models.Cours;
import services.CoursService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AjouterCoursController {

    @FXML
    private TextField txtUserId;

    @FXML
    private TextField txtTitle;

    @FXML
    private TextField txtImage;

    @FXML
    private ComboBox<String> comboSubject;

    @FXML
    private TextField txtRate;

    @FXML
    private DatePicker datePicker;

    @FXML
    void initialize() {
        // Populate the ComboBox with subjects
        ObservableList<String> subjects = FXCollections.observableArrayList("IT", "Robotics", "Math", "History", "Sport", "Business");
        comboSubject.setItems(subjects);
    }

    @FXML
    void Ajouter(ActionEvent event) {
        CoursService cs = new CoursService();

        try {
            // Set userId to 1 and rate to 0
            int userId = 1; // Fixed value
            String title = txtTitle.getText();
            String image = txtImage.getText();
            String subject = comboSubject.getValue();
            int rate = 0; // Fixed value

            // Input validation
            if (title.isEmpty() || image.isEmpty() || subject == null) {
                showAlert("Input Error", "Please fill in all required fields.");
                return; // Exit the method if validation fails
            }

            Cours cours = new Cours(userId, title, image, subject, rate, LocalDateTime.now());
            cs.ajouter(cours);

            System.out.println("Cours ajouté !");

            // Load AfficherCours.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherCours.fxml")); // Change this path if needed
            Parent root = loader.load();

            // Switch scene
            txtTitle.getScene().setRoot(root);

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement de la vue : " + e.getMessage());
        }
    }

    // Method to show alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
