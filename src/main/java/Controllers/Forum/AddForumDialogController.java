package Controllers.Forum;

import entite.Forum;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.ForumService;
import service.IService;
import service.UserService;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;

public class AddForumDialogController implements Initializable {
    @FXML private TextField titleField;
    @FXML private TextField subjectField;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private Forum forum;
    private boolean saveClicked = false;
    private IService<Forum> forumService;
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        forumService = new ForumService();
        userService = new UserService();

        // Set up event handlers
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage stage to set
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the forum to be edited in the dialog.
     *
     * @param forum the forum to edit
     */
    public void setForum(Forum forum) {
        this.forum = forum;

        // Pre-fill the fields with forum data
        titleField.setText(forum.getTitle());
        subjectField.setText(forum.getSubject());
        descriptionArea.setText(forum.getDescription());
    }

    /**
     * Returns true if the user clicked Save, false otherwise.
     *
     * @return boolean indicating if save was clicked
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    /**
     * Handles the save button action.
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            // Get current user (in a real app, this would come from authentication)
            // For now, we'll get the first user or create a dummy one
            User currentUser = getCurrentUser();

            if (forum == null) {
                // Create new forum
                forum = new Forum();
                forum.setUser(currentUser);
                forum.setDate_time(getCurrentDateTime());
            }

            // Update forum data from form
            forum.setTitle(titleField.getText());
            forum.setSubject(subjectField.getText());
            forum.setDescription(descriptionArea.getText());

            forum.setMedia("test");
            forum.setType_media("test");

            // Save to database
            if (forum.getId() == 0) {
                forumService.create(forum);
            } else {
                forumService.update(forum);
            }

            saveClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Handles the cancel button action.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage += "Titre invalide!\n";
        }

        if (subjectField.getText() == null || subjectField.getText().trim().isEmpty()) {
            errorMessage += "Sujet invalide!\n";
        }

        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            errorMessage += "Description invalide!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Show the error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Champs invalides");
            alert.setHeaderText("Veuillez corriger les champs invalides");
            alert.setContentText(errorMessage);
            alert.showAndWait();

            return false;
        }
    }

    /**
     * Gets the current user for the forum post.
     * In a real application, this would come from an authentication system.
     *
     * @return the current user
     */
    private User getCurrentUser() {
        // In a real application, you would get the currently logged-in user
        // For this example, we'll get the first user from the database or create a dummy one
        User user = userService.readAll().stream().findFirst().orElse(null);

        if (user == null) {
            // Create a dummy user if no users exist
            user = new User();
            user.setName("Admin");
            user.setImg("admin.png");
            // Set other required user properties

            // You might want to save this user depending on your application flow
            // userService.add(user);
        }

        return user;
    }

    /**
     * Gets the current date and time as a formatted string.
     *
     * @return the current date and time string
     */
    private Date getCurrentDateTime() {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        return timestamp;
    }
}