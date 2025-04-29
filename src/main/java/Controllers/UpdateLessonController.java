package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.sql.SQLException;
import java.io.IOException;

import models.Lesson;
import services.LessonService;

public class UpdateLessonController {

    @FXML
    private AnchorPane mainPane;  // Reference to the parent pane

    @FXML
    private TextField txtTitle;

    @FXML
    private TextField txtDescription;

    private Lesson lessonToEdit;

    public void setLessonToEdit(Lesson lesson) {
        this.lessonToEdit = lesson;
        txtTitle.setText(lesson.getTitle());
        txtDescription.setText(lesson.getDescription());
    }

    @FXML
    void handleSaveUpdate() {
        if (lessonToEdit != null) {
            lessonToEdit.setTitle(txtTitle.getText());
            lessonToEdit.setDescription(txtDescription.getText());

            try {
                LessonService service = new LessonService();
                service.update(lessonToEdit);
                System.out.println("Lesson updated!");

                // Load the AfficherLesson.fxml view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherLesson.fxml"));
                Parent root = loader.load();

                // Get controller and pass the course ID
                AfficherLessonController controller = loader.getController();
                controller.setCourseIdFilter(lessonToEdit.getCourseId());

                // Replace current view
                Stage stage = (Stage) mainPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Lessons for Course ID: " + lessonToEdit.getCourseId());
                stage.show();

            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void handleSave() {
        try {
            LessonService service = new LessonService();
            service.update(lessonToEdit); // Save to database

            // Load AfficherLesson.fxml and pass courseId back
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherLesson.fxml"));
            Parent root = loader.load();

            AfficherLessonController controller = loader.getController();
            controller.setCourseIdFilter(lessonToEdit.getCourseId()); // Pass the course ID back

            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
