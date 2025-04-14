package Controllers.Forum;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import entite.Forum;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.ForumService;
import service.UserService;

public class ForumModifyController {

    @FXML
    private TextArea Description;

    @FXML
    private ComboBox<String> Subject;

    @FXML
    private TextField Title;

    @FXML
    private TextArea MediaField;

    @FXML
    private Button ReturnButton;

    @FXML
    private Button ModifyButton;

    private Forum forumToModify;
    private ForumService forumService;

    public ForumModifyController() {
        forumService = new ForumService();
    }

    @FXML
    void initialize() {
        assert Description != null : "fx:id=\"Description\" was not injected: check your FXML file.";
        assert Subject != null : "fx:id=\"Subject\" was not injected: check your FXML file.";
        assert Title != null : "fx:id=\"Title\" was not injected: check your FXML file.";
        assert MediaField != null : "fx:id=\"MediaField\" was not injected: check your FXML file.";
        assert ReturnButton != null : "fx:id=\"ReturnButton\" was not injected: check your FXML file.";
        assert ModifyButton != null : "fx:id=\"ModifyButton\" was not injected: check your FXML file.";

        // Initialize ComboBox items if needed
        // Subject.getItems().addAll("Math", "Physics", "Science");
    }

    public void setForumToModify(Forum forum) {
        this.forumToModify = forum;

        // Populate fields with forum data
        Title.setText(forum.getTitle());
        Description.setText(forum.getDescription());
        MediaField.setText(forum.getMedia());
        Subject.setValue(forum.getSubject());
    }

    @FXML
    void handleModify(ActionEvent event) {
        try {
            // Update forum object with form data
            forumToModify.setTitle(Title.getText());
            forumToModify.setDescription(Description.getText());
            forumToModify.setMedia(MediaField.getText());
            forumToModify.setSubject(Subject.getValue());

            // Update in database
            forumService.update(forumToModify);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Discussion modifiée avec succès!");
            alert.showAndWait();

            // Return to the forum view page
            navigateToForumView(event);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Modification échouée");
            alert.setContentText("Une erreur est survenue lors de la modification:\n" + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    void HandleReturn(ActionEvent event) {
        navigateToForumView(event);
    }

    private void navigateToForumView(ActionEvent event) {
        try {
            Parent forumView = FXMLLoader.load(getClass().getResource("/com/example/loe/Forum.fxml"));
            Scene scene = new Scene(forumView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de retourner à la page précédente: " + e.getMessage());
            alert.showAndWait();
        }
    }
}