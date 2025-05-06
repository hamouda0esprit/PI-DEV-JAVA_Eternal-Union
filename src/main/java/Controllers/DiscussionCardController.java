package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import entite.Discussion;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class DiscussionCardController {
    @FXML
    private ImageView authorImageView;
    
    @FXML
    private Label authorLabel;
    
    @FXML
    private Label timestampLabel;
    
    @FXML
    private Label contentLabel;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public void setDiscussion(Discussion discussion) {
        // Set author image if available
        if (discussion.getMedia() != null && !discussion.getMedia().isEmpty()) {
            File imageFile = new File(discussion.getMedia());
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                authorImageView.setImage(image);
            }
        }
        
        // Set text content
        authorLabel.setText("User #" + discussion.getEventId()); // Using eventId as temporary user ID
        timestampLabel.setText(discussion.getCreatedAt().format(formatter));
        contentLabel.setText(discussion.getCaption());
    }
} 