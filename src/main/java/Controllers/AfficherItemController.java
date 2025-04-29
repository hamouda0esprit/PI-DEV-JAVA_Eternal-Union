package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Item;
import services.ItemService;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherItemController implements Initializable {

    @FXML
    private TableView<Item> tableview;

    @FXML
    private Button addButton;

    @FXML
    private TableColumn<Item, Integer> idCol;

    @FXML
    private TableColumn<Item, Integer> lessonIdCol;

    @FXML
    private TableColumn<Item, String> typeCol;

    @FXML
    private TableColumn<Item, String> contentCol;

    private final ItemService itemService = new ItemService();
    private int currentLessonId = -1;  // <-- this will store the lessonId for use in Add Form

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            List<Item> itemList = itemService.getAll();
            ObservableList<Item> observableItems = FXCollections.observableArrayList(itemList);

            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            lessonIdCol.setCellValueFactory(new PropertyValueFactory<>("lessonId"));
            typeCol.setCellValueFactory(new PropertyValueFactory<>("typeItem"));
            contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));

            tableview.setItems(observableItems);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showForLesson(int lessonId) {
        this.currentLessonId = lessonId; // <-- store it for use in add form
        try {
            List<Item> items = itemService.getItemsByLessonId(lessonId);
            ObservableList<Item> observableList = FXCollections.observableArrayList(items);
            tableview.setItems(observableList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showAddForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterItem.fxml"));
            AnchorPane addItemPane = loader.load();

            AjouterItemController controller = loader.getController();
            controller.setLessonId(currentLessonId); // Pass the lesson ID

            AnchorPane currentRoot = (AnchorPane) addButton.getScene().getRoot();
            currentRoot.getChildren().setAll(addItemPane);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        Item selectedItem = tableview.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateItem.fxml"));
                Parent root = loader.load();

                UpdateItemController controller = loader.getController();
                controller.setItem(selectedItem);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                showAlert("Error", "Error loading update form.");
                e.printStackTrace();
            }
        } else {
            showAlert("Selection Error", "Please select an item to update.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Item selectedItem = tableview.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Confirmation");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Are you sure you want to delete this item?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    try {
                        itemService.delete(selectedItem.getId());
                        tableview.getItems().remove(selectedItem);
                        showAlert("Success", "Item deleted successfully.");
                    } catch (SQLException e) {
                        showAlert("Error", "Failed to delete item.");
                        e.printStackTrace();
                    }
                }
            });

        } else {
            showAlert("Selection Error", "Please select an item to delete.");
        }
    }
}
