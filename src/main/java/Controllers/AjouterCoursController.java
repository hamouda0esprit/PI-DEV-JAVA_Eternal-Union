package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import models.Cours;
import services.CoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AjouterCoursController {

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

    @FXML
    void Ajouter(ActionEvent event) {
        CoursService cs = new CoursService();

        try {
            int userId = Integer.parseInt(txtUserId.getText());
            String title = txtTitle.getText();
            String image = txtImage.getText();
            String subject = txtSubject.getText();
            int rate = Integer.parseInt(txtRate.getText());

            Cours cours = new Cours(userId, title, image, subject, rate, LocalDateTime.now());
            cs.ajouter(cours);

            System.out.println("Cours ajouté !");

            // Load AfficherCours.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherCours.fxml")); // Change this path if needed
            Parent root = loader.load();

            // Switch scene
            txtUserId.getScene().setRoot(root);

        } catch (NumberFormatException e) {
            System.out.println("Erreur de conversion : " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement de la vue : " + e.getMessage());
        }
    }



}
