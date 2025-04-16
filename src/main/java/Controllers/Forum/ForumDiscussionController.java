package Controllers.Forum;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import entite.Forum;
import entite.Responces;
import entite.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.ResponcesService;
import service.ForumService;
import service.UserService;

public class ForumDiscussionController {

    @FXML
    private Button backButton;

    @FXML
    private Label forumTitle;

    @FXML
    private Label forumDescription;

    @FXML
    private Label authorName;

    @FXML
    private Label postDate;

    @FXML
    private ImageView authorPhoto;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private Button submitButton;

    @FXML
    private VBox commentsContainer;

    @FXML
    private ScrollPane commentsScrollPane; // Reference to the ScrollPane if you named it in FXML

    private Forum currentForum;
    private User currentUser;
    private ForumService forumService;
    private ResponcesService commentService;
    private UserService userService;

    public ForumDiscussionController() {
        forumService = new ForumService();
        commentService = new ResponcesService();
        userService = new UserService();
    }

    @FXML
    void initialize() {
        try {
            // Initialize services
            forumService = new ForumService();
            commentService = new ResponcesService();
            userService = new UserService();

            // Initialize current user - Get from session or use a default ID (2 in this case)
            currentUser = userService.readById(2); // Replace with actual session user ID when available

            if (currentUser == null) {
                System.err.println("Warning: Current user could not be loaded. Delete functionality may not work correctly.");
            }

            // If you've added a fx:id to the ScrollPane
            if (commentsScrollPane != null) {
                commentsScrollPane.setFitToWidth(true);
            }
        } catch (Exception e) {
            System.err.println("Error initializing controller: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setForum(Forum forum) {
        this.currentForum = forum;

        // Display forum details
        forumTitle.setText(forum.getTitle());
        forumDescription.setText(forum.getDescription());

        // Set author information
        User author = forum.getUser();
        authorName.setText(author.getName());

        // Format and set date
        String dateStr = forum.getDate_time().toString();
        // You can format this date better if needed
        postDate.setText(dateStr);

        // Set author photo (if available)
        try {
            // This is a placeholder. You may need to adjust based on your actual implementation
            if (author.getImg() != null && !author.getImg().isEmpty()) {
                //Image image = new Image(author.getImg());

                Image img = new Image(getClass().getResourceAsStream("/images/ForumUser.jpg"));

                authorPhoto.setImage(img);
            }
        } catch (Exception e) {
            System.err.println("Error loading user image: " + e.getMessage());
        }

        // Load comments for this forum
        loadComments();
    }

    private void loadComments() {
        try {
            // Clear existing comments
            commentsContainer.getChildren().clear();

            // Get comments for current forum
            List<Responces> comments = commentService.readByForumId(currentForum.getId());

            if (comments.isEmpty()) {
                Label emptyLabel = new Label("Soyez le premier à commenter cette discussion !");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
                commentsContainer.getChildren().add(emptyLabel);
            } else {
                // Add each comment to the container
                for (Responces comment : comments) {
                    commentsContainer.getChildren().add(createCommentItem(comment));
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les commentaires.", e.getMessage());
            e.printStackTrace();
        }
    }

    private Node createCommentItem(Responces comment) {
        VBox commentBox = new VBox();
        commentBox.setSpacing(5);
        commentBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

        // Get comment author
        User author = comment.getUser();

        // Create top row with author info and delete button
        HBox topRow = new HBox(10);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Author header with avatar and name
        HBox authorHeader = new HBox(10);
        authorHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(authorHeader, javafx.scene.layout.Priority.ALWAYS);

        // User avatar (if available)
        ImageView avatar = new ImageView();
        avatar.setFitHeight(25);
        avatar.setFitWidth(25);
        avatar.setPreserveRatio(true);

        try {
            if (author.getImg() != null && !author.getImg().isEmpty()) {
                //Image img = new Image(author.getImg());

                Image img = new Image(getClass().getResourceAsStream("/images/ForumUser.jpg"));

                avatar.setImage(img);
            }
        } catch (Exception e) {
            System.out.println("Could not load user image: " + e.getMessage());
        }

        // Create author label
        Label authorLabel = new Label(author.getName());
        authorLabel.setStyle("-fx-font-weight: bold;");

        authorHeader.getChildren().addAll(avatar, authorLabel);

        // Add delete button only if this comment belongs to the current user
        if (currentUser != null && author.getId() == currentUser.getId()) {
            Button deleteButton = new Button("×");
            deleteButton.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-weight: bold; "
                    + "-fx-padding: 0 5 0 5; -fx-background-radius: 3;");

            deleteButton.setOnAction(event -> {
                try {
                    // Confirm deletion
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Supprimer le commentaire");
                    confirmAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");
                    confirmAlert.setContentText("Cette action ne peut pas être annulée.");

                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            // Delete the comment
                            commentService.delete(comment);

                            // Reload comments
                            loadComments();

                            // Show success message
                            showAlert("Succès", "Commentaire supprimé avec succès.", null);
                        }
                    });
                } catch (Exception e) {
                    showAlert("Erreur", "Impossible de supprimer le commentaire.", e.getMessage());
                    e.printStackTrace();
                }
            });

            topRow.getChildren().addAll(authorHeader, deleteButton);
        } else {
            topRow.getChildren().add(authorHeader);
        }

        // Create content label with word wrap
        Label contentLabel = new Label(comment.getComment());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);

        // Create date label
        Label dateLabel = new Label(comment.getDate_time().toString());
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #9e9e9e;");

        // Add all to container
        commentBox.getChildren().addAll(topRow, contentLabel, dateLabel);
        commentBox.setMaxWidth(Double.MAX_VALUE);

        return commentBox;
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            Parent forumView = FXMLLoader.load(getClass().getResource("/view/Forum.fxml"));
            Scene scene = new Scene(forumView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de revenir à la page précédente.", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleSubmitComment(ActionEvent event) {
        String commentText = commentTextArea.getText().trim();

        if (commentText.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide.", null);
            return;
        }

        try {
            // Create new comment
            Responces comment = new Responces();
            comment.setComment(commentText);
            comment.setDate_time(Timestamp.valueOf(LocalDateTime.now()));
            comment.setForum(currentForum);
            comment.setUser(currentUser);  // Use the currentUser that was initialized in initialize()

            // Save comment using createPst method since create() method seems to have issues
            commentService.createPst(comment);

            // Clear text area
            commentTextArea.clear();

            // Reload comments
            loadComments();

            showAlert("Succès", "Commentaire ajouté avec succès.", null);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ajouter le commentaire.", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}