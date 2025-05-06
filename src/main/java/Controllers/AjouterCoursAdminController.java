package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Cours;
import services.CoursService;
import java.time.LocalDateTime;

public class AjouterCoursAdminController {

    @FXML private TextField titleField;
    @FXML private TextField imageField;
    @FXML private TextField subjectField;

    private final CoursService coursService = new CoursService();

    @FXML
    private void handleAdd() {
        try {
            // Validate required fields
            if (titleField.getText().isEmpty() ||
                    subjectField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants",
                        "Veuillez remplir tous les champs obligatoires marqués d'un *");
                return;
            }

            // Create new course
            Cours newCours = new Cours();
            newCours.setTitle(titleField.getText());
            newCours.setImage(imageField.getText());
            newCours.setSubject(subjectField.getText());
            newCours.setRate(0);
            newCours.setUserId(1);
            newCours.setLastUpdate(LocalDateTime.now());

            // Save to database
            coursService.ajouter(newCours);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Le cours a été ajouté avec succès");

            // Close the window
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "Veuillez entrer des valeurs numériques valides");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur s'est produite lors de l'ajout du cours");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}