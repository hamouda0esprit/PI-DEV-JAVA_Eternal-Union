package Controllers;

import entite.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.control.Label;

public class AddEventDialogController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField timeField;
    @FXML private TextField locationField;
    @FXML private TextField photoField;
    @FXML private Label dialogTitle;
    
    private Stage dialogStage;
    private Evenement event;
    private boolean saveClicked = false;
    private LocalDate selectedDate;
    
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
        }
    }
    
    public boolean isSaveClicked() {
        return saveClicked;
    }
    
    public Evenement getEvent() {
        return event;
    }
    
    @FXML
    private void handleSave() {
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
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    @FXML
    private void handleBrowsePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            photoField.setText(file.getPath());
        }
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Invalid Fields");
        alert.setHeaderText("Please correct invalid fields");
        alert.setContentText(message);
        alert.showAndWait();
    }
} 