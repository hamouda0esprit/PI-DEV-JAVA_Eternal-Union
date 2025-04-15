package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import models.Item;
import services.ItemService;
import java.util.List;
import java.sql.SQLException;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class AjouterItemController {

    @FXML
    private TextField txtLessonId;

    @FXML
    private TextField txtType;

    @FXML
    private TextField txtContent;

    private List<Item> items; // You might want to use an ObservableList<Item> to dynamically update UI.

    // This method will be triggered by the "Add Course" button
    @FXML
    private void Ajouter() {
        try {
            String lessonIdText = txtLessonId.getText();
            String typeText = txtType.getText();
            String contentText = txtContent.getText();

            if (lessonIdText.isEmpty() || typeText.isEmpty() || contentText.isEmpty()) {
                showAlert("Input Error", "All fields must be filled!");
                return;
            }

            int lessonId = Integer.parseInt(lessonIdText);
            Item newItem = new Item(0, lessonId, typeText, contentText);

            ItemService itemService = new ItemService();
            itemService.ajouter(newItem);

            showAlert("Success", "Item added successfully!");

            // ✅ Charger la page AfficherItem.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherItems.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtLessonId.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Lesson ID must be a valid number.");
        } catch (Exception e) {
            showAlert("Error", "An error occurred while adding the item.");
            e.printStackTrace();
        }
    }


    // Show an alert message
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Setter for the items list (or use dependency injection, etc.)
    public void setItems(List<Item> items) {
        this.items = items;
    }
}
