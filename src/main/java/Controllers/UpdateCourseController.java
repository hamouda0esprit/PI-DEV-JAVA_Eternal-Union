package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Cours;
import services.CoursService;

import java.io.IOException;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UpdateCourseController {

    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtImage;
    @FXML
    private ComboBox<String> comboSubject;

    private Cours coursToUpdate;

    public void setCours(Cours cours) {
        this.coursToUpdate = cours;
        txtTitle.setText(cours.getTitle());
        txtImage.setText(cours.getImage());
        comboSubject.setValue(cours.getSubject());
    }

    @FXML
    void initialize() {
        ObservableList<String> subjects = FXCollections.observableArrayList("IT", "Robotics", "Math", "History", "Sport", "Business");
        comboSubject.setItems(subjects);
    }

    @FXML
    void Update(ActionEvent event) {
        try {
            String title = txtTitle.getText();
            String image = txtImage.getText();
            String subject = comboSubject.getValue();

            coursToUpdate.setTitle(title);
            coursToUpdate.setImage(image);
            coursToUpdate.setSubject(subject);

            CoursService service = new CoursService();
            service.modifier(coursToUpdate);

            // Reload AfficherCours.fxml after update
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherCours.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }
}