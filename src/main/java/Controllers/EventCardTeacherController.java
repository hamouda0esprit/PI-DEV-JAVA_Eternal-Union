package Controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import entite.Evenement;
import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.EvenementService;
import service.ParticipationService;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class EventCardTeacherController {
    @FXML
    private AnchorPane rootPane;
    @FXML
    private ImageView eventImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label weatherLabel;
    @FXML
    private Label capacityLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private HBox actionButtons;
    @FXML
    private Button modifyButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button participantsButton;

    private Evenement currentEvent;
    private Object mainController;
    private EvenementService evenementService;
    private ParticipationService participationService;

    public EventCardTeacherController() {
        this.evenementService = new EvenementService();
        this.participationService = new ParticipationService();
    }

    public void setEventData(Evenement event) {
        this.currentEvent = event;
        titleLabel.setText(event.getName());
        dateLabel.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(event.getDateevent()));
        locationLabel.setText(event.getLocation());
        capacityLabel.setText("Capacité: " + event.getCapacite());
        descriptionLabel.setText(event.getDescription());
        
        // Set default image if no image is provided
        if (event.getPhoto() != null && !event.getPhoto().isEmpty()) {
            try {
                // Try to load as a file first
                File file = new File(event.getPhoto());
                if (file.exists()) {
                    eventImage.setImage(new Image(file.toURI().toString()));
                } else {
                    // If not a file, try to load as a resource
                    eventImage.setImage(new Image(getClass().getResourceAsStream(event.getPhoto())));
                }
            } catch (Exception e) {
                // If both attempts fail, use default image
                eventImage.setImage(new Image(getClass().getResourceAsStream("/images/default-event.png")));
            }
        } else {
            eventImage.setImage(new Image(getClass().getResourceAsStream("/images/default-event.png")));
        }
    }

    public void setMainController(Object controller) {
        this.mainController = controller;
    }

    public void setCurrentUser(User user) {
        // No need to do anything special for teachers since all buttons should be visible
    }

    @FXML
    private void handleModifyClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventDialog.fxml"));
            VBox dialogPane = loader.load();
            EventDialogController controller = loader.getController();
            controller.setEvent(currentEvent);
            controller.setMode(EventDialogController.Mode.EDIT);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier l'événement");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new javafx.scene.Scene(dialogPane));
            dialogStage.showAndWait();

            // Refresh the event list
            if (mainController != null) {
                // Assuming mainController has a refresh method
                mainController.getClass().getMethod("refreshEvents").invoke(mainController);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'événement");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet événement ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                evenementService.delete(currentEvent.getId());
                // Refresh the event list
                if (mainController != null) {
                    mainController.getClass().getMethod("refreshEvents").invoke(mainController);
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'événement: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleParticipantsClick() {
        if (currentEvent == null) return;

        try {
            // Get participants for this event
            List<User> participants = participationService.getParticipantsForEvent(currentEvent.getId());
            
            // Create PDF document
            Document document = new Document();
            String fileName = "participants_" + currentEvent.getName() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Liste des participants - " + currentEvent.getName(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            // Add event details
            Font detailsFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Date: " + currentEvent.getDateevent().toString(), detailsFont));
            document.add(new Paragraph("Lieu: " + currentEvent.getLocation(), detailsFont));
            document.add(new Paragraph(" "));
            
            // Add participants table
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            
            // Add table headers
            table.addCell(new Phrase("Nom", detailsFont));
            table.addCell(new Phrase("Email", detailsFont));
            table.addCell(new Phrase("Téléphone", detailsFont));
            
            // Add participants data
            for (User participant : participants) {
                table.addCell(new Phrase(participant.getName(), detailsFont));
                table.addCell(new Phrase(participant.getEmail(), detailsFont));
                table.addCell(new Phrase(String.valueOf(participant.getPhone()), detailsFont));
            }
            
            document.add(table);
            document.close();
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Liste des participants générée");
            alert.setContentText("La liste des participants a été enregistrée dans le fichier : " + fileName);
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la génération de la liste");
            alert.setContentText("Une erreur est survenue : " + e.getMessage());
            alert.showAndWait();
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
 