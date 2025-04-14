package Controllers.Forum;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import entite.Forum;
import entite.User;
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

import javax.print.attribute.standard.Media;

public class ForumAddController {

    public ForumAddController() {}

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
    void HandleReturn(ActionEvent event) {
        try {
            Parent ajoutForumView = FXMLLoader.load(getClass().getResource("/com/example/loe/Forum.fxml"));
            Scene scene = new Scene(ajoutForumView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void add(ActionEvent event) {
        ForumService forumService = new ForumService();
        Forum forum = new Forum();
        UserService userService = new UserService();

        try {
            forum.setTitle(Title.getText());
            forum.setDescription(Description.getText());
            forum.setMedia(MediaField.getText());
            forum.setSubject(Subject.getValue());
            forum.setDate_time(Timestamp.valueOf(LocalDateTime.now()));

            // Temp
            forum.setType_media("Image");
            forum.setAiprompt_responce("Prompt result or whatever");

            forum.setUser(userService.readById(2));

            forumService.createPst(forum);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Forum added successfully!");
            alert.showAndWait();

            try {
                Parent ajoutForumView = FXMLLoader.load(getClass().getResource("/com/example/loe/Forum.fxml"));
                Scene scene = new Scene(ajoutForumView);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Forum not added");
            alert.setContentText("Something went wrong:\n" + e.getMessage());
            alert.showAndWait();

            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        assert Description != null : "fx:id=\"Description\" was not injected: check your FXML file.";
        assert Subject != null : "fx:id=\"Subject\" was not injected: check your FXML file.";
        assert Title != null : "fx:id=\"Title\" was not injected: check your FXML file.";
        assert MediaField != null : "fx:id=\"MediaField\" was not injected: check your FXML file.";
        assert ReturnButton != null : "fx:id=\"ReturnButton\" was not injected: check your FXML file.";

        // Initialize ComboBox items if needed
        //Subject.getItems().addAll("Math", "Physics", "Science");
    }

}
