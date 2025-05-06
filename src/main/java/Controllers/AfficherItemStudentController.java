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
import services.GeminiAIService;
import javafx.scene.Parent;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.scene.Node;

import javafx.scene.control.ScrollPane;


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

import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.Comparator;




public class AfficherItemStudentController implements Initializable {


    private int currentLessonId; // Add this field

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

    @FXML
    private TextFlow textFlow;

    //@FXML
    //private ImageView imageView;

    private final ItemService itemService = new ItemService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // No need to set contentArea, as it has been removed
    }

    public void showForLesson(int lessonId) {
        this.currentLessonId = lessonId; // Store the lesson ID
        System.out.println("Showing items for lesson ID: " + lessonId); // Debugging output
        try {
            List<Item> items = itemService.getItemsByLessonId(lessonId);

            // Sort items by ID
            items.sort(Comparator.comparingInt(Item::getId));

            textFlow.getChildren().clear(); // Clear previous content
            //imageView.setVisible(false); // Hide the image view initially

            for (Item item : items) {
    System.out.println("Item type: " + item.getTypeItem());
    if ("title".equals(item.getTypeItem())) {
        Text titleText = new Text(item.getContent() + "\n");
        titleText.getStyleClass().add("title");
        textFlow.getChildren().add(titleText);
    } else if ("paragraphe".equals(item.getTypeItem())) {
        String paragraphContent = item.getContent();

        Text paragraphText = new Text(paragraphContent + "\n");
        paragraphText.setStyle("-fx-font-size: 14px;");

        Button explainButton = new Button("Explain with AI");
        explainButton.setOnAction(e -> showExplanationPopup(paragraphContent));

        // Add both paragraph and button to the text flow
        textFlow.getChildren().add(paragraphText);
        textFlow.getChildren().add(explainButton);
        textFlow.getChildren().add(new Text("\n"));  // spacing
    } else if ("image".equals(item.getTypeItem())) {
        try {
            Image image = new Image(item.getContent());
            ImageView newImageView = new ImageView(image);
            newImageView.setFitWidth(200);  // Or your preferred dimensions
            newImageView.setPreserveRatio(true);
            textFlow.getChildren().add(new Text("\n"));
            textFlow.getChildren().add(newImageView);
            textFlow.getChildren().add(new Text("\n"));
        } catch (Exception e) {
            e.printStackTrace();  // Handle case where image URL is bad
        }
    } else if ("video".equals(item.getTypeItem())) {
        Text videoText = new Text("Video: " + item.getContent() + "\n");
        textFlow.getChildren().add(videoText);
    }
}

        } catch (SQLException e) {
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

    @FXML
    private void handleDelete(ActionEvent event) {
        Item selectedItem = tableview.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            // Optional confirmation
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Confirmation");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Are you sure you want to delete this item?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    try {
                        // Delete from DB
                        itemService.delete(selectedItem.getId());

                        // Remove from TableView
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
private void showExplanationPopup(String paragraph) {
  /*  Stage popupStage = new Stage();
    popupStage.setTitle("AI Explanation");

    TextFlow explanationFlow = new TextFlow();
    explanationFlow.setPrefWidth(480);
    explanationFlow.setLineSpacing(5);

    Text loading = new Text("Asking AI for explanation...");
    explanationFlow.getChildren().add(loading);

    // Wrap in ScrollPane
    ScrollPane scrollPane = new ScrollPane(explanationFlow);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefSize(500, 300);

    StackPane root = new StackPane(scrollPane);
    Scene scene = new Scene(root, 500, 300);
    popupStage.setScene(scene);
    popupStage.show();

    // Call Gemini in a background thread to avoid freezing UI
    new Thread(() -> {
        try {
            String aiResponse = GeminiAIService.explainText(paragraph);
            Text explanation = new Text(aiResponse);
            explanation.setWrappingWidth(460); // Adjust to fit inside ScrollPane

            // Update UI on JavaFX Application Thread
            javafx.application.Platform.runLater(() -> {
                explanationFlow.getChildren().clear();
                explanationFlow.getChildren().add(explanation);
            });

        } catch (IOException e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                explanationFlow.getChildren().clear();
                explanationFlow.getChildren().add(new Text("Failed to get explanation from AI."));
            });
        }
    }).start();

   */
}



}
   