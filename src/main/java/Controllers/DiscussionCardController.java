package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import entite.Discussion;
import java.io.File;
import java.time.format.DateTimeFormatter;
import entite.User;

public class DiscussionCardController {
    @FXML private Label timestampLabel;
    @FXML private Label contentLabel;
    @FXML private VBox imageContainer;
    @FXML private ImageView discussionImageView;
    @FXML private Button modifyButton;
    @FXML private Button deleteButton;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy à HH:mm");
    private static final double MAX_IMAGE_WIDTH = 600;
    private static final double MAX_IMAGE_HEIGHT = 800;

    private Runnable onDelete;
    private Runnable onModify;
    private User currentUser;
    private Discussion currentDiscussion;

    public void setOnDelete(Runnable onDelete) {
        this.onDelete = onDelete;
    }
    public void setOnModify(Runnable onModify) {
        this.onModify = onModify;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        // Show buttons for all users
        if (modifyButton != null) {
            modifyButton.setVisible(true);
            modifyButton.setManaged(true);
        }
        if (deleteButton != null) {
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        }
    }

    @FXML
    private void handleDelete() {
        if (onDelete != null && currentDiscussion != null) {
            onDelete.run();
        }
    }

    @FXML
    private void handleModify() {
        if (onModify != null && currentDiscussion != null) {
            onModify.run();
        }
    }

    public void setDiscussion(Discussion discussion) {
        this.currentDiscussion = discussion;
        // Set text content
        timestampLabel.setText(discussion.getCreatedAt().format(formatter));
        contentLabel.setText(discussion.getCaption());

        String media = discussion.getMedia();
        if (media != null && !media.isEmpty()) {
            try {
                Image image;
                if (media.startsWith("http")) {
                    // It's a GIF or image URL
                    image = new Image(media, true);
                } else {
                    // It's a local file path (fix slashes for Windows)
                    File imageFile = new File(media.replace("\\", File.separator));
                    image = new Image(imageFile.toURI().toString());
                }
                discussionImageView.setImage(image);
                discussionImageView.setPreserveRatio(true);
                discussionImageView.setVisible(true);
                imageContainer.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                discussionImageView.setVisible(false);
                imageContainer.setVisible(false);
            }
        } else {
            discussionImageView.setVisible(false);
            imageContainer.setVisible(false);
        }
    }
} 