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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText(null);
            alert.setContentText("Vous devez d'abord participer à l'événement pour générer un QR code.");
            alert.showAndWait();
            return;
        }

        try {
            // Create QR code data
            String qrData = String.format("Event: %s\nStudent: %s\nEvent ID: %d\nStudent ID: %d",
                    currentEvent.getName(),
                    currentUser.getName(),
                    currentEvent.getId(),
                    currentUser.getId());

            // Generate QR code
            String qrCodeUrl = String.format("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=%s",
                    URLEncoder.encode(qrData, StandardCharsets.UTF_8));

            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le QR Code");
            fileChooser.setInitialFileName("qr_code_" + currentEvent.getName() + ".png");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images PNG", "*.png")
            );

            // Show save dialog
            File file = fileChooser.showSaveDialog(qrCodeButton.getScene().getWindow());
            if (file != null) {
                // Download and save the QR code
                URL url = new URL(qrCodeUrl);
                try (InputStream in = url.openStream();
                     FileOutputStream out = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("QR Code généré et enregistré avec succès !");
                successAlert.showAndWait();
            }
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Erreur lors de la génération du QR Code : " + e.getMessage());
            errorAlert.showAndWait();
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