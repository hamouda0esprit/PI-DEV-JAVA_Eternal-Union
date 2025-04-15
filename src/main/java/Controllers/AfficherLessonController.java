package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import models.Lesson;
import services.LessonService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AfficherLessonController implements Initializable {

    private int currentCourseId = -1; // Default: show all lessons
    @FXML
    private AnchorPane mainPane;

    @FXML
    private TableView<Lesson> tableview;

    @FXML
    private TableColumn<Lesson, Integer> idCol;

    @FXML
    private TableColumn<Lesson, Integer> courseIdCol;

    @FXML
    private TableColumn<Lesson, String> titleCol;

    @FXML
    private TableColumn<Lesson, String> descriptionCol;

    private int courseIdFilter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ensure tableview is not null and initialize columns
        if (tableview != null) {
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            courseIdCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
    }

    private void loadAllLessons() throws SQLException {
        LessonService service = new LessonService();
        List<Lesson> lessons = service.recuperer();
        ObservableList<Lesson> obs = FXCollections.observableList(lessons);
        tableview.setItems(obs);
    }

    private void loadLessonsByCourse() throws SQLException {
        LessonService service = new LessonService();
        List<Lesson> lessons = service.getLessonsByCourse(courseIdFilter);
        ObservableList<Lesson> obs = FXCollections.observableList(lessons);
        tableview.setItems(obs);
    }

    @FXML
    void deleteSelectedLesson(ActionEvent event) {
        Lesson selectedLesson = tableview.getSelectionModel().getSelectedItem();
        if (selectedLesson != null) {
            try {
                LessonService service = new LessonService();
                service.supprimer(selectedLesson.getId());
                tableview.getItems().remove(selectedLesson);
                System.out.println("Lesson supprimé !");
            } catch (SQLException e) {
                System.out.println("Erreur lors de la suppression : " + e.getMessage());
            }
        } else {
            System.out.println("Veuillez sélectionner un lesson à supprimer.");
        }
    }

    @FXML
    void OpenAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterLessons.fxml"));
            Parent root = loader.load();
            mainPane.getChildren().setAll(root);
        } catch (IOException e) {
            System.out.println("Error loading AjouterLesson.fxml: " + e.getMessage());
        }
    }

    @FXML
    void handleUpdateLesson(ActionEvent event) {
        Lesson selectedLesson = tableview.getSelectionModel().getSelectedItem();
        if (selectedLesson != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateLesson.fxml"));
                AnchorPane updatePane = loader.load();

                // Get controller & pass lesson to edit
                UpdateLessonController controller = loader.getController();
                controller.setLessonToEdit(selectedLesson);

                // Replace current UI
                mainPane.getChildren().setAll(updatePane);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Veuillez sélectionner un lesson à modifier.");
        }
    }

    public void setCourseIdFilter(int courseId) {
        this.currentCourseId = courseId;
        refreshLessons(); // No SQLException here
    }

    private void refreshLessons() {
        try {
            LessonService service = new LessonService();
            List<Lesson> lessons;

            if (currentCourseId == -1) {
                lessons = service.recuperer(); // All lessons
            } else {
                lessons = service.getLessonsByCourse(currentCourseId); // Filtered
            }

            ObservableList<Lesson> obs = FXCollections.observableList(lessons);
            tableview.setItems(obs);
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error to user (optional)
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database error!");
            alert.show();
            // Return empty list on error
            tableview.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    public void handleShowItems(ActionEvent event) {
        // Récupère la leçon sélectionnée dans la TableView
        Lesson selectedLesson = tableview.getSelectionModel().getSelectedItem();
        if (selectedLesson != null) {
            try {
                // Crée un FXMLLoader avec le chemin du fichier FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherItems.fxml"));

                // Charge le fichier FXML
                Parent root = loader.load();

                // Récupère le contrôleur de AfficherItemController
                AfficherItemController itemController = loader.getController();

                // Appelle la méthode de AfficherItemController pour afficher les items de la leçon sélectionnée
                itemController.showForLesson(selectedLesson.getId());

                // Remplace le contenu de mainPane par la nouvelle vue
                mainPane.getChildren().setAll(root);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Erreur de chargement du fichier FXML");
            }
        } else {
            System.out.println("Veuillez sélectionner une leçon.");
        }
    }


    @FXML
    public void handleShowItemsEleve(ActionEvent event) {
        Lesson selectedLesson = tableview.getSelectionModel().getSelectedItem();
        if (selectedLesson != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherItemEleve.fxml"));
                Parent root = loader.load();

                // Get controller
                AfficherItemController itemController = loader.getController();

                // Pass lesson id to item controller
                itemController.showForLesson(selectedLesson.getId());

                // Set scene
                mainPane.getChildren().setAll(root);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Erreur de chargement du fichier FXML");
            }
        } else {
            System.out.println("Veuillez sélectionner une leçon.");
        }
    }


}
