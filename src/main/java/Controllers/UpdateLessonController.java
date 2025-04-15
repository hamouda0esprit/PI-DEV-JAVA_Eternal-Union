package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import models.Lesson;
import services.LessonService;
import javafx.fxml.FXMLLoader;

import java.sql.SQLException;
import java.io.IOException;

public class UpdateLessonController {

    @FXML
    private AnchorPane mainPane;  // reference to the parent pane

    @FXML
    private TextField txtCourseId;

    @FXML
    private TextField txtTitle;

    @FXML
    private TextField txtDescription;

    private Lesson lessonToEdit;

    public void setLessonToEdit(Lesson lesson) {
        this.lessonToEdit = lesson;
        txtCourseId.setText(String.valueOf(lesson.getCourseId()));
        txtTitle.setText(lesson.getTitle());
        txtDescription.setText(lesson.getDescription());
    }

    @FXML
    void handleSaveUpdate() {
        if (lessonToEdit != null) {
            lessonToEdit.setCourseId(Integer.parseInt(txtCourseId.getText()));
            lessonToEdit.setTitle(txtTitle.getText());
            lessonToEdit.setDescription(txtDescription.getText());

            try {
                LessonService service = new LessonService();
                service.update(lessonToEdit);
                System.out.println("Lesson updated!");

                // Now load the AfficherLesson.fxml back into the main pane
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherLesson.fxml"));

                // Check if the resource loading fails
                if (loader.getLocation() == null) {
                    throw new IOException("FXML file not found");
                }

                Parent root = loader.load();
                mainPane.getChildren().setAll(root); // Replace current pane with AfficherLesson
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
