package Controllers;

import service.LocationService;
import entite.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;

public class AddEventDialogController implements Initializable {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField timeField;
    @FXML private TextArea locationField;
    @FXML private TextField photoField;
    @FXML private ImageView imageView;
    @FXML private Label placeholderLabel;
    @FXML private Button removePhotoButton;
    @FXML
    private ListView<String> locationSuggestions;
    private File selectedImageFile;
    @FXML private Label dialogTitle;
    
    private Stage dialogStage;
    private Evenement event;
    private boolean saveClicked = false;
    private LocalDate selectedDate;
    private boolean saved = false;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
    }
    
    public void setEvent(Evenement event) {
        this.event = event;
        
        if (event != null) {
            // Update dialog title for edit mode
            dialogTitle.setText("Modification d'evenement");
            
            // Populate fields with event data
            nameField.setText(event.getName());
            descriptionArea.setText(event.getDescription());
            locationField.setText(event.getLocation());
            timeField.setText(event.getTime().toString());
            photoField.setText(event.getPhoto());
            selectedDate = event.getDateevent().toLocalDate();
            
            // Load and display the image if it exists
            if (event.getPhoto() != null && !event.getPhoto().isEmpty()) {
                try {
                    File imageFile = new File(event.getPhoto());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        imageView.setImage(image);
                        placeholderLabel.setVisible(false);
                        removePhotoButton.setVisible(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public boolean isSaveClicked() {
        return saveClicked;
    }
    
    public Evenement getEvent() {
        return event;
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    @FXML
    private void handleSave() {
        try {
            if (isInputValid()) {
                if (event == null) {
                    event = new Evenement();
                }
                event.setName(nameField.getText());
                event.setDescription(descriptionArea.getText());
                event.setDateevent(Date.valueOf(selectedDate));
                event.setLocation(locationField.getText());
                event.setTime(Time.valueOf(LocalTime.parse(timeField.getText())));
                event.setIduser(1); // You might want to get this from logged in user
                event.setPhoto(photoField.getText());
                
                saveClicked = true;
                saved = true;
                dialogStage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save event");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    @FXML
    private void handleBrowsePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selectionner Photo");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        selectedImageFile = fileChooser.showOpenDialog(imageView.getScene().getWindow());
        if (selectedImageFile != null) {
            photoField.setText(selectedImageFile.getAbsolutePath());
            Image image = new Image(selectedImageFile.toURI().toString());
            imageView.setImage(image);
            placeholderLabel.setVisible(false);
            removePhotoButton.setVisible(true);
        }
    }

    @FXML
    private void handleRemovePhoto() {
        imageView.setImage(null);
        photoField.setText("");
        placeholderLabel.setVisible(true);
        removePhotoButton.setVisible(false);
        selectedImageFile = null;
    }
    
    private boolean isInputValid() {
        String errorMessage = "";
        
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "Name is required!\n";
        }
        
        if (timeField.getText() == null || timeField.getText().trim().isEmpty()) {
            errorMessage += "Time is required (HH:mm format)!\n";
        } else {
            try {
                LocalTime.parse(timeField.getText());
            } catch (Exception e) {
                errorMessage += "Invalid time format! Use HH:mm\n";
            }
        }
        
        if (locationField.getText() == null || locationField.getText().trim().isEmpty()) {
            errorMessage += "Location is required!\n";
        }
        
        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(errorMessage);
            return false;
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Fields");
        alert.setHeaderText("Please correct invalid fields");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void setupLocationAutocomplete() {
        // Add listener to location field
        locationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 3) {
                // Get suggestions from the API
                List<String> suggestions = LocationService.searchLocations(newValue);
                
                // Update the suggestions list
                locationSuggestions.getItems().setAll(suggestions);
                locationSuggestions.setVisible(!suggestions.isEmpty());
            } else {
                locationSuggestions.setVisible(false);
            }
        });
        
        // Handle selection from suggestions
        locationSuggestions.setOnMouseClicked(event -> {
            String selectedLocation = locationSuggestions.getSelectionModel().getSelectedItem();
            if (selectedLocation != null) {
                locationField.setText(selectedLocation);
                locationSuggestions.setVisible(false);
            }
        });
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupLocationAutocomplete();
        
        // Add scene listener after the scene is available
        Platform.runLater(() -> {
            Scene scene = locationField.getScene();
            if (scene != null) {
                scene.setOnMouseClicked(event -> {
                    if (!locationField.getBoundsInParent().contains(event.getX(), event.getY()) &&
                        !locationSuggestions.getBoundsInParent().contains(event.getX(), event.getY())) {
                        locationSuggestions.setVisible(false);
                    }
                });
            }
        });
    }
} 