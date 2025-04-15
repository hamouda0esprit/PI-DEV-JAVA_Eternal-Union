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

public class AfficherLessonController implements Initializable {

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
        try {
            loadAllLessons();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAllLessons() throws SQLException {
        LessonService service = new LessonService();
        List<Lesson> lessons = service.recuperer();
        ObservableList<Lesson> obs = FXCollections.observableList(lessons);

        tableview.setItems(obs);

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        courseIdCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadLessonsByCourse() throws SQLException {
        LessonService service = new LessonService();
        List<Lesson> lessons = service.getLessonsByCourse(courseIdFilter);
        ObservableList<Lesson> obs = FXCollections.observableList(lessons);
        tableview.setItems(obs);
    }

    public void setCourseIdFilter(int courseId) {
        this.courseIdFilter = courseId;
        try {
            loadLessonsByCourse();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}