package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import models.Item;
import services.ItemService;

import java.util.List;
import java.sql.SQLException;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.event.ActionEvent;

public class AjouterItemController {

    @FXML
    private TextField txtLessonId;

    @FXML
    private ComboBox<String> comboType;

    @FXML
    private TextField txtContent;

    private List<Item> items;

    private int lessonId;

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
        System.out.println("Lesson ID set to: " + lessonId);
    }

    @FXML
    public void initialize() {
        comboType.getItems().addAll("paragraphe", "title", "image");
    }

    @FXML
    private void Ajouter(ActionEvent event) {
        String typeText = comboType.getValue();
        String contentText = txtContent.getText();

        if (typeText == null || contentText.isEmpty()) {
            showAlert("Input Error", "All fields must be filled!");
            return;
        }

        System.out.println("Current Lesson ID: " + lessonId);

        Item newItem = new Item(lessonId, typeText, contentText);
        ItemService itemService = new ItemService();

        try {
            itemService.ajouter(newItem);
            showAlert("Success", "Item added successfully!");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherItems.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtContent.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while adding the item.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "An error occurred while adding the item.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
