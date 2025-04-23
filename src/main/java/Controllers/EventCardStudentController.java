package Controllers;

import entite.Evenement;
import entite.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import service.ParticipationService;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.io.File;
import java.io.IOException;

public class EventCardStudentController {
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label descriptionLabel;
    
    @FXML
    private Label locationLabel;
    
    @FXML
    private ImageView eventImage;
    
    @FXML
    private Button discussionButton;
    
    @FXML
    private Button participateButton;

    private Evenement currentEvent;
    private User currentUser;
    private ParticipationService participationService;
    private EvenementStudentController mainController;

    public EventCardStudentController() {
        this.participationService = new ParticipationService();
    }

    public void setMainController(EvenementStudentController controller) {
        this.mainController = controller;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setEventData(Evenement event) {
        this.currentEvent = event;
        
        // Set title
        titleLabel.setText(event.getName());
        
        // Format the date in French
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);
        dateLabel.setText(dateFormat.format(event.getDateevent()));
        
        // Set location with icon (using Unicode character for location pin)
        locationLabel.setText("📍 " + event.getLocation());
        
        // Set description (limit to brief preview)
        String description = event.getDescription();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 97) + "...";
        }
        descriptionLabel.setText(description);
        
        // Load and set the image
        String photoPath = event.getPhoto();
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                File file = new File(photoPath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    eventImage.setImage(image);
                } else {
                    setDefaultImage();
                }
            } catch (Exception e) {
                System.err.println("Error loading event image: " + e.getMessage());
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }

        // Update participate button state
        updateParticipateButton();
    }

    private void updateParticipateButton() {
        if (currentUser != null && currentEvent != null) {
            boolean isParticipating = participationService.isParticipating(currentEvent.getId(), currentUser.getId());
            if (isParticipating) {
                participateButton.setText("Cancel Participation");
                participateButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
            } else {
                participateButton.setText("Participate");
                participateButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        }
    }

    @FXML
    private void handleParticipateClick() {
        if (currentUser == null || currentEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "Please log in to participate in events.");
            return;
        }

        boolean isParticipating = participationService.isParticipating(currentEvent.getId(), currentUser.getId());
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        
        if (isParticipating) {
            confirmation.setTitle("Cancel Participation");
            confirmation.setHeaderText("Cancel participation in: " + currentEvent.getName());
            confirmation.setContentText("Are you sure you want to cancel your participation in this event?");
        } else {
            confirmation.setTitle("Participate in Event");
            confirmation.setHeaderText("Participate in: " + currentEvent.getName());
            confirmation.setContentText("Would you like to participate in this event?");
        }

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (isParticipating) {
                    participationService.removeParticipation(currentEvent.getId(), currentUser.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "You have cancelled your participation.");
                } else {
                    participationService.addParticipation(currentEvent.getId(), currentUser.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "You are now participating in this event!");
                }
                updateParticipateButton();
                if (mainController != null) {
                    mainController.refreshEvents();
                }
            }
        });
    }

    @FXML
    private void handleDiscussionClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DiscussionFeed.fxml"));
            VBox feedContent = loader.load();
            
            DiscussionFeedController controller = loader.getController();
            controller.setEvent(currentEvent);
            
            Stage feedStage = new Stage();
            feedStage.initModality(Modality.APPLICATION_MODAL);
            feedStage.setTitle("Event Discussions");
            
            Scene scene = new Scene(feedContent);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            feedStage.setScene(scene);
            feedStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open discussion feed: " + e.getMessage());
        }
    }

    private void setDefaultImage() {
        try {
            Image defaultImage = new Image("https://via.placeholder.com/300x200?text=Event");
            eventImage.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Error loading default image: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 