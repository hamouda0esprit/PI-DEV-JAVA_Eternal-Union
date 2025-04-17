package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import models.Item;
import service.ItemService;
import java.sql.SQLException;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class UpdateItemController {

    @FXML
    private TextField txtLessonId;

    @FXML
    private TextField txtType;

    @FXML
    private TextField txtContent;

    private Item selectedItem; // The item that will be updated

    // This method will be triggered to load the selected item data into the fields
    public void setItem(Item item) {
        this.selectedItem = item;
        txtLessonId.setText(String.valueOf(item.getLessonId()));
        txtType.setText(item.getTypeItem());
        txtContent.setText(item.getContent());
    }

    @FXML
    private void updateItem() {
        try {
            String lessonIdText = txtLessonId.getText();
            String typeText = txtType.getText();
            String contentText = txtContent.getText();

            if (lessonIdText.isEmpty() || typeText.isEmpty() || contentText.isEmpty()) {
                showAlert("Input Error", "All fields must be filled!");
                return;
            }

            int lessonId = Integer.parseInt(lessonIdText);

            // Update the selected item with new values
            selectedItem.setLessonId(lessonId);
            selectedItem.setTypeItem(typeText);
            selectedItem.setContent(contentText);

            ItemService itemService = new ItemService();
            itemService.update(selectedItem); // Assuming you have an update method in the service

            showAlert("Success", "Item updated successfully!");

            // Optionally: After successful update, you can navigate back to a previous page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AfficherItems.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtLessonId.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Lesson ID must be a valid number.");
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while updating the item.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "An error occurred while updating the item.");
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
}
