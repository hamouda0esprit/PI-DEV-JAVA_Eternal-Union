package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import entities.Discussion;
import entities.Evenement;
import services.DiscussionService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DiscussionFeedController {
    @FXML
    private ImageView eventCoverImage;
    @FXML
    private Label eventTitleLabel;
    @FXML
    private Label eventDateLabel;
    @FXML
    private Label eventLocationLabel;
    @FXML
    private VBox discussionsContainer;
    @FXML
    private Button addDiscussionButton;

    private Evenement event;
    private DiscussionService discussionService;

    public void initialize() {
        discussionService = new DiscussionService();
        addDiscussionButton.setOnAction(e -> handleAddDiscussion());
    }

    public void setEvent(Evenement event) {
        this.event = event;
        updateEventHeader();
        loadDiscussions();
    }

    private void updateEventHeader() {
        if (event.getPhoto() != null && !event.getPhoto().isEmpty()) {
            File imageFile = new File(event.getPhoto());
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                eventCoverImage.setImage(image);
            }
        }

        eventTitleLabel.setText(event.getName());
        eventDateLabel.setText(event.getDateevent().toString() + " " + event.getTime().toString());
        eventLocationLabel.setText(event.getLocation());
    }

    private void loadDiscussions() {
        discussionsContainer.getChildren().clear();
        List<Discussion> discussions = discussionService.getDiscussionsForEvent(event.getId());
        
        for (Discussion discussion : discussions) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DiscussionCard.fxml"));
                VBox discussionCard = loader.load();
                DiscussionCardController controller = loader.getController();
                controller.setDiscussion(discussion);
                discussionsContainer.getChildren().add(discussionCard);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading discussion card");
            }
        }
    }

    @FXML
    private void handleAddDiscussion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DiscussionDialog.fxml"));
            VBox dialogContent = loader.load();
            
            Dialog<Discussion> dialog = new Dialog<>();
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(dialogContent);
            
            // Create custom buttons with proper styling
            ButtonType publishType = new ButtonType("Publier", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(publishType, cancelType);
            
            // Style the buttons
            Button publishButton = (Button) dialogPane.lookupButton(publishType);
            Button cancelButton = (Button) dialogPane.lookupButton(cancelType);
            publishButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
            cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            
            // Get the controller and set up the dialog
            DiscussionDialogController controller = loader.getController();
            controller.setEventId(event.getId());
            controller.setDialogStage(dialog.getDialogPane().getScene().getWindow());
            
            // Configure the dialog
            dialog.setTitle("Ajouter une discussion");
            dialog.setHeaderText(null);
            
            // Set the result converter
            dialog.setResultConverter(buttonType -> {
                if (buttonType == publishType) {
                    controller.handleSave();
                    if (controller.isSaveClicked()) {
                        loadDiscussions(); // Refresh the discussions list immediately
                        return new Discussion(); // Return a dummy discussion to close the dialog
                    }
                }
                return null;
            });
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening discussion dialog");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 