package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import models.Cours;
import service.CoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class UpdateCourseController {

    @FXML
    private TextField txtUserId;
    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtImage;
    @FXML
    private TextField txtSubject;
    @FXML
    private TextField txtRate;
    @FXML
    private DatePicker datePicker;

    private Cours coursToUpdate;

    public void setCours(Cours cours) {
        this.coursToUpdate = cours;
        txtUserId.setText(String.valueOf(cours.getUserId()));
        txtTitle.setText(cours.getTitle());
        txtImage.setText(cours.getImage());
        txtSubject.setText(cours.getSubject());
        txtRate.setText(String.valueOf(cours.getRate()));
        if (cours.getLastUpdate() != null && datePicker != null) {
            datePicker.setValue(cours.getLastUpdate().toLocalDate());
        }
    }


    @FXML
    void Update(ActionEvent event) {
        try {
            int userId = Integer.parseInt(txtUserId.getText());
            String title = txtTitle.getText();
            String image = txtImage.getText();
            String subject = txtSubject.getText();
            int rate = Integer.parseInt(txtRate.getText());
            LocalDateTime lastUpdate = LocalDateTime.of(datePicker.getValue(), LocalTime.now());

            coursToUpdate.setUserId(userId);
            coursToUpdate.setTitle(title);
            coursToUpdate.setImage(image);
            coursToUpdate.setSubject(subject);
            coursToUpdate.setRate(rate);
            coursToUpdate.setLastUpdate(lastUpdate);

            CoursService service = new CoursService();
            service.modifier(coursToUpdate);

            // Reload AfficherCours.fxml after update
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AfficherCours.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException | SQLException | NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }


}
