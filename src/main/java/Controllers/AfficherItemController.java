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

import java.io.IOException;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            List<Item> itemList = itemService.getAll(); // ❗ appel correct
            ObservableList<Item> observableItems = FXCollections.observableArrayList(itemList);

            // Liaison des colonnes avec les propriétés du modèle
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
            // Load the new FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterItem.fxml"));
            AnchorPane addItemPane = loader.load();

            // Replace current root with the new content
            // Assumes addButton is inside an AnchorPane (the root layout of your main view)
            AnchorPane currentRoot = (AnchorPane) addButton.getScene().getRoot();
            currentRoot.getChildren().setAll(addItemPane);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
