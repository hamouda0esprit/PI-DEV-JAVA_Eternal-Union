package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Cours;
import service.CoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AfficherCoursController {

    @FXML
    private TableView<Cours> tableview;

    @FXML
    private TableColumn<Cours, Integer> idCol;

    @FXML
    private TableColumn<Cours, Integer> userIdCol;

    @FXML
    private TableColumn<Cours, String> titleCol;

    @FXML
    private TableColumn<Cours, String> imageCol;

    @FXML
    private TableColumn<Cours, String> subjectCol;

    @FXML
    private TableColumn<Cours, Integer> rateCol;

    @FXML
    private TableColumn<Cours, LocalDateTime> lastUpdateCol;

    @FXML
    void initialize() throws SQLException {
        CoursService service = new CoursService();
        ObservableList<Cours> obs = FXCollections.observableList(service.recuperer());

        tableview.setItems(obs);

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        imageCol.setCellValueFactory(new PropertyValueFactory<>("image"));

        imageCol.setCellFactory(col -> new TableCell<Cours, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(100);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                } else {
                    imageView.setImage(new Image(imageUrl, true));
                    setGraphic(imageView);
                }
            }
        });

        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        rateCol.setCellValueFactory(new PropertyValueFactory<>("rate"));
        lastUpdateCol.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));
    }

    @FXML
    void OpenAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCours.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.out.println("Error loading FXML: " + e.getMessage());
        }
    }

    @FXML
    void exportToPdfClicked(ActionEvent event) {
        List<Cours> coursList = tableview.getItems();
        PdfExporter.exportToPdf(coursList);
    }

    @FXML
    void exportToExcelClicked(ActionEvent event) {
        List<Cours> coursList = tableview.getItems();
        ExcelExporter.exportToExcel(coursList);
    }

    @FXML
    void deleteSelectedCours(ActionEvent event) {
        Cours selectedCours = tableview.getSelectionModel().getSelectedItem();

        if (selectedCours != null) {
            try {
                // Call your service to delete from DB
                CoursService service = new CoursService();
                service.sipprimer(selectedCours.getId());

                // Remove from TableView
                tableview.getItems().remove(selectedCours);

                System.out.println("Cours supprimé !");
            } catch (SQLException e) {
                System.out.println("Erreur lors de la suppression : " + e.getMessage());
            }
        } else {
            System.out.println("Veuillez sélectionner un cours à supprimer.");
        }
    }

    @FXML
    public void modifierSelectedCours(ActionEvent event) {
        try {
            Cours selectedCours = tableview.getSelectionModel().getSelectedItem();
            if (selectedCours == null) {
                System.out.println("Aucun cours sélectionné.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateCours.fxml"));
            Parent root = loader.load();

            // Pass the selected course to the controller
            UpdateCourseController controller = loader.getController();
            controller.setCours(selectedCours);

            // Switch to UpdateCours.fxml in the same window
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.out.println("Erreur lors du chargement de UpdateCours.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void showLessonsOfSelectedCourse() {
        try {
            Cours selectedCourse = tableview.getSelectionModel().getSelectedItem();
            if (selectedCourse == null) {
                System.out.println("Please select a course first");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AfficherLesson.fxml"));
            Parent root = loader.load();

            AfficherLessonController lessonController = loader.getController();
            lessonController.setCourseIdFilter(selectedCourse.getId()); // No SQLException here

            Stage stage = (Stage) tableview.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Lessons for Course: " + selectedCourse.getTitle());
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading AfficherLesson.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void showLessonsEleve(ActionEvent event) {
        try {
            Cours selectedCourse = tableview.getSelectionModel().getSelectedItem();
            if (selectedCourse == null) {
                System.out.println("Veuillez sélectionner un cours.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherLessonEleve.fxml"));
            Parent root = loader.load();

            // Use AfficherLessonController here
            AfficherLessonController controller = loader.getController();
            controller.setCourseIdFilter(selectedCourse.getId());

            // Replace the current scene
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Leçons Élève - " + selectedCourse.getTitle());
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur chargement AfficherLessonEleve.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }


}
