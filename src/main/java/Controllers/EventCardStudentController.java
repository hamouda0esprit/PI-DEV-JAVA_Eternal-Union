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
import service.WeatherService;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.FileOutputStream;
import javafx.stage.FileChooser;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import javax.imageio.ImageIO;
import java.awt.BasicStroke;

public class EventCardStudentController implements Initializable {
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label descriptionLabel;
    
    @FXML
    private Label locationLabel;
    
    @FXML
    private Label weatherLabel;
    
    @FXML
    private Label capacityLabel;
    
    @FXML
    private ImageView eventImage;
    
    @FXML
    private Button discussionButton;
    
    @FXML
    private Button participateButton;
    
    @FXML
    private Button qrCodeButton;

    @FXML private TextField searchField;
    @FXML private Button searchButton;

    private Evenement currentEvent;
    private User currentUser;
    private ParticipationService participationService;
    private EvenementStudentController mainController;
    private WeatherService weatherService;
    private boolean isParticipating;

    public EventCardStudentController() {
        this.participationService = new ParticipationService();
        this.weatherService = new WeatherService();
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
        
        // Set location with icon
        locationLabel.setText("📍 " + event.getLocation());
        
        // Get and set weather information
        String weatherInfo = weatherService.getWeatherForLocation(
            event.getLocation(), 
            event.getDateevent().toLocalDate()
        );
        weatherLabel.setText(weatherInfo);
        
        // Set description (limit to brief preview)
        String description = event.getDescription();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 97) + "...";
        }
        descriptionLabel.setText(description);
        
        // Update capacity information
        updateCapacityInfo();
        
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

        // Update participate button state
        updateParticipateButton();
    }

    private void updateCapacityInfo() {
        if (currentEvent != null) {
            int totalCapacity = currentEvent.getCapacite();
            int currentParticipants = participationService.getParticipantCount(currentEvent.getId());
            int remainingSpots = totalCapacity - currentParticipants;
            
            String capacityText;
            if (remainingSpots <= 0) {
                capacityText = "❌ Complet (" + currentParticipants + "/" + totalCapacity + ")";
                capacityLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
            } else {
                capacityText = "✅ Places disponibles: " + remainingSpots + "/" + totalCapacity;
                capacityLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            }
            capacityLabel.setText(capacityText);
        }
    }

    private void updateParticipateButton() {
        isParticipating = participationService.isParticipating(currentEvent.getId(), currentUser.getId());
        participateButton.setText(isParticipating ? "Annuler" : "Participer");
        participateButton.setStyle(isParticipating ? 
            "-fx-background-color: #e74c3c;" : 
            "-fx-background-color: #2196F3;");
        qrCodeButton.setVisible(isParticipating);
        qrCodeButton.setManaged(isParticipating);
    }

    @FXML
    private void handleParticipateClick() {
        if (currentUser == null || currentEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez vous connecter pour participer aux événements.");
            return;
        }

        boolean isParticipating = participationService.isParticipating(currentEvent.getId(), currentUser.getId());
        int currentParticipants = participationService.getParticipantCount(currentEvent.getId());
        int totalCapacity = currentEvent.getCapacite();

        if (!isParticipating && currentParticipants >= totalCapacity) {
            showAlert(Alert.AlertType.WARNING, "Événement complet", "Désolé, l'événement a atteint sa capacité maximale.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        
        if (isParticipating) {
            confirmation.setTitle("Annuler la participation");
            confirmation.setHeaderText("Annuler la participation à: " + currentEvent.getName());
            confirmation.setContentText("Êtes-vous sûr de vouloir annuler votre participation à cet événement?");
        } else {
            confirmation.setTitle("Participer à l'événement");
            confirmation.setHeaderText("Participer à: " + currentEvent.getName());
            confirmation.setContentText("Voulez-vous participer à cet événement?");
        }

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (isParticipating) {
                    participationService.removeParticipation(currentEvent.getId(), currentUser.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Vous avez annulé votre participation.");
                } else {
                    participationService.addParticipation(currentEvent.getId(), currentUser.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Vous participez maintenant à cet événement!");
                }
                updateParticipateButton();
                updateCapacityInfo();
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
            controller.setCurrentUser(currentUser);
            
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
    private void handleQRCodeClick() {
        if (!isParticipating) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Vous devez d'abord participer à l'événement pour générer un ticket.");
            return;
        }

        try {
            // Create QR code data
            String qrData = String.format("Event: %s\nStudent: %s\nID: %d-%d", 
                currentEvent.getName(), 
                currentUser.getName(),
                currentEvent.getId(),
                currentUser.getId());

            // Create QR code using API
            String qrCodeUrl = String.format("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=%s",
                URLEncoder.encode(qrData, StandardCharsets.UTF_8));

            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le Ticket");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images PNG", "*.png")
            );
            fileChooser.setInitialFileName("ticket_" + currentEvent.getName() + ".png");

            // Show save dialog
            File file = fileChooser.showSaveDialog(qrCodeButton.getScene().getWindow());
            if (file != null) {
                // Create a new image with ticket design
                BufferedImage ticket = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = ticket.createGraphics();

                // Set background to white
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, 600, 400);

                // Add border
                g2d.setColor(new Color(33, 150, 243)); // Blue color
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(10, 10, 580, 380);

                // Add title
                g2d.setColor(new Color(33, 150, 243));
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.drawString("TICKET D'ENTRÉE", 200, 50);

                // Add event name
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.drawString("Événement: " + currentEvent.getName(), 20, 100);

                // Add date
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);
                g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                g2d.drawString("Date: " + dateFormat.format(currentEvent.getDateevent()), 20, 130);

                // Add location
                g2d.drawString("Lieu: " + currentEvent.getLocation(), 20, 160);

                // Add student name
                g2d.drawString("Participant: " + currentUser.getName(), 20, 190);

                // Download and add QR code
                URL url = new URL(qrCodeUrl);
                BufferedImage qrCode = ImageIO.read(url);
                g2d.drawImage(qrCode, 350, 100, 200, 200, null);

                // Add ticket number
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Ticket #" + currentEvent.getId() + "-" + currentUser.getId(), 20, 350);

                // Add disclaimer
                g2d.setFont(new Font("Arial", Font.ITALIC, 12));
                g2d.drawString("Ce ticket est valable uniquement pour le participant mentionné", 20, 380);

                // Save the ticket
                ImageIO.write(ticket, "PNG", file);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ticket généré avec succès!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de générer le ticket: " + e.getMessage());
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