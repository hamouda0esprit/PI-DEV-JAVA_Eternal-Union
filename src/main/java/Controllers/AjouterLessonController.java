package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import models.Lesson;
import services.LessonService;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterLessonController {

    @FXML
    private TextField txtTitle;

    @FXML
    private TextField txtDescription;

    private int currentCourseId; // <-- IMPORTANT

    public void setCurrentCourseId(int courseId) {
        this.currentCourseId = courseId;
    }

    

    @FXML
    void Ajouter(ActionEvent event) {
        try {
            LessonService service = new LessonService();
            Lesson lesson = new Lesson(currentCourseId, txtTitle.getText(), txtDescription.getText());

            service.ajouter(lesson);

            System.out.println("Lesson ajouté !");

            // Après ajout : Revenir à AfficherLesson.fxml et passer le courseId
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherLesson.fxml"));
            Parent root = loader.load();

            // Récupérer le controller
            AfficherLessonController controller = loader.getController();
            controller.setCourseIdFilter(currentCourseId); // <-- Voilà !

            // Changer la scène
            Stage stage = (Stage) txtTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Lessons for Course ID: " + currentCourseId);
            stage.show();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
