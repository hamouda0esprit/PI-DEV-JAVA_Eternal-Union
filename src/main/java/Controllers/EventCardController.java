package Controllers;

import entite.Evenement;
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
import service.EvenementService;
import service.IEvenementService;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.io.File;
import java.io.IOException;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class EventCardController implements Initializable {
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
    private Button updateButton;
    
    @FXML
    private Button deleteButton;

    @FXML private TextField searchField;
    @FXML private Button searchButton;

    private Evenement currentEvent;
    private IEvenementService evenementService;
    private EvenementController mainController;

    public EventCardController() {
        this.evenementService = new EvenementService();
    }

    public void setMainController(EvenementController controller) {
        this.mainController = controller;
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
                    if (image != null && !image.isError()) {
                        eventImage.setImage(image);
                    } else {
                        setDefaultImage();
                    }
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
    }

    private void setDefaultImage() {
        try {
            // Use a local resource instead of a URL
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-event.png"));
            if (defaultImage != null) {
                eventImage.setImage(defaultImage);
            } else {
                System.err.println("Failed to load default image");
            }
        } catch (Exception e) {
            System.err.println("Error loading default image: " + e.getMessage());
        }
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

    @FXML
    private void handleUpdateClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddEventDialog.fxml"));
            AnchorPane dialogPane = loader.load();
            
            AddEventDialogController controller = loader.getController();
            controller.setEvent(currentEvent);
            
            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Update Event");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(updateButton.getScene().getWindow());
            
            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);
            
            // Set the dialog stage in the controller
            controller.setDialogStage(dialogStage);
            
            // Show the dialog and wait for the user response
            dialogStage.showAndWait();
            
            if (controller.isSaved()) {
                // Update the event in the database
                Evenement updatedEvent = controller.getEvent();
                if (updatedEvent != null) {
                    evenementService.update(updatedEvent);
                    // Refresh the views through the main controller
                    if (mainController != null) {
                        mainController.refreshViews();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load the dialog");
            alert.setContentText("An error occurred while loading the update event dialog.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDeleteClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Event");
        alert.setHeaderText("Delete Event: " + currentEvent.getName());
        alert.setContentText("Are you sure you want to delete this event?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                evenementService.delete(currentEvent.getId());
                if (mainController != null) {
                    mainController.refreshViews();
                }
            }
        });
    }

    public Button getUpdateButton() {
        return updateButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEvents(newValue);
        });
    }
    
    @FXML
    private void toggleSearch() {
        boolean isVisible = searchField.isVisible();
        searchField.setVisible(!isVisible);
        searchField.setManaged(!isVisible);
        
        if (!isVisible) {
            searchField.requestFocus();
        } else {
            searchField.clear();
            filterEvents("");
        }
    }
    
    public void filterEvents(String searchText) {
        if (mainController != null) {
            mainController.filterEvents(searchText);
        }
    }
} 