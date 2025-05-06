package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import models.Cours;
import services.CoursService;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;



public class ModifierCoursAdminController {

    @FXML private TextField titleField;
    @FXML private TextField imageField;
    @FXML private TextField subjectField;

    private Cours cours;
    private final CoursService coursService = new CoursService();

    public void setCours(Cours cours) {
        this.cours = cours;
        populateFields();
    }

    private void populateFields() {
        if (cours != null) {
            titleField.setText(cours.getTitle());
            imageField.setText(cours.getImage());
            subjectField.setText(cours.getSubject());
        }
    }

    @FXML
    private void handleSave() {
        try {
            cours.setTitle(titleField.getText());
            cours.setImage(imageField.getText());
            cours.setSubject(subjectField.getText());

            coursService.modifier(cours);

            // Close the window
            titleField.getScene().getWindow().hide();
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Veuillez entrer des valeurs numériques valides pour la note et l'ID utilisateur.");
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur s'est produite lors de la modification du cours.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}