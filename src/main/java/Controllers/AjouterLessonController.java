package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Lesson;
import service.LessonService;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterLessonController {

    @FXML
    private TextField txtTitle;

    @FXML
    private TextField txtDescription;

    @FXML
    private TextField txtCourseId;

    @FXML
    void Ajouter(ActionEvent event) {
        String title = txtTitle.getText();
        String description = txtDescription.getText();
        int courseId;

        try {
            courseId = Integer.parseInt(txtCourseId.getText());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Course ID");
            return;
        }

        Lesson lesson = new Lesson(courseId, title, description);
        LessonService service = new LessonService();

        try {
            service.ajouter(lesson);
            System.out.println("Lesson added!");

            // Optionally return to AfficherLesson
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AfficherLesson.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (SQLException | IOException e) {
            System.out.println("Error adding lesson: " + e.getMessage());
        }
    }
}
