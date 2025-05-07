package Controllers;

import entite.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.EvenementService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class EventDialogController {
    public enum Mode {
        CREATE, EDIT
    }

    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField capacityField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField timeField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Evenement currentEvent;
    private Mode mode;
    private EvenementService evenementService;

    public EventDialogController() {
        this.evenementService = new EvenementService();
    }

    public void setEvent(Evenement event) {
        this.currentEvent = event;
        if (event != null) {
            nameField.setText(event.getName());
            descriptionField.setText(event.getDescription());
            locationField.setText(event.getLocation());
            capacityField.setText(String.valueOf(event.getCapacite()));
            datePicker.setValue(event.getDateevent().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            timeField.setText(String.format("%02d:%02d", 
                event.getDateevent().getHours(), 
                event.getDateevent().getMinutes()));
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        saveButton.setText(mode == Mode.CREATE ? "Créer" : "Modifier");
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText();
            String description = descriptionField.getText();
            String location = locationField.getText();
            int capacity = Integer.parseInt(capacityField.getText());
            LocalDateTime dateTime = datePicker.getValue().atTime(
                Integer.parseInt(timeField.getText().split(":")[0]),
                Integer.parseInt(timeField.getText().split(":")[1])
            );

            if (mode == Mode.CREATE) {
                Evenement newEvent = new Evenement();
                newEvent.setName(name);
                newEvent.setDescription(description);
                newEvent.setLocation(location);
                newEvent.setCapacite(capacity);
                newEvent.setDateevent(new java.sql.Date(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime()));
                evenementService.add(newEvent);
            } else {
                currentEvent.setName(name);
                currentEvent.setDescription(description);
                currentEvent.setLocation(location);
                currentEvent.setCapacite(capacity);
                currentEvent.setDateevent(new java.sql.Date(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime()));
                evenementService.update(currentEvent);
            }

            closeDialog();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder l'événement: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 