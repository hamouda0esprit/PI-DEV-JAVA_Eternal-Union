package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import models.Item;
import services.ItemService;

public class AddYoutubePopupController {

    @FXML
    private TextField youtubeLinkField;

    private int lessonId;

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    @FXML
    private void handleSubmit() {
        String link = youtubeLinkField.getText().trim();

        if (link.isEmpty()) {
            showAlert("Validation Error", "YouTube link cannot be empty.");
            return;
        }

        try {
            ItemService itemService = new ItemService();
            Item newItem = new Item();
            newItem.setLessonId(lessonId);
            newItem.setTypeItem("video");
            newItem.setContent(link);

            itemService.ajouter(newItem);

            showAlert("Success", "YouTube video added.");
            ((Stage) youtubeLinkField.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add YouTube video.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
