package Controllers.Forum;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import entite.Forum;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.AI21Service;
import service.ForumService;
import service.ProfanityFilterService;
import service.UserService;
import utils.FileUtils;

public class ForumModifyController {

    @FXML
    private TextArea Description;

    @FXML
    private ComboBox<String> Subject;

    @FXML
    private TextField Title;

    @FXML
    private Button ReturnButton;

    @FXML
    private Button uploadMediaButton;

    @FXML
    private ImageView imagePreview;

    @FXML
    private MediaView videoPreview;

    @FXML
    private StackPane mediaPreviewContainer;

    private Forum forumToModify;
    private ForumService forumService;
    private File selectedMediaFile;
    private String mediaType;
    private MediaPlayer mediaPlayer;
    private boolean isMediaLoading = false;
    private final ProfanityFilterService profanityFilterService;

    public ForumModifyController() {
        forumService = new ForumService();
        this.profanityFilterService = new ProfanityFilterService();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (Title.getText() == null || Title.getText().trim().isEmpty()) {
            errorMessage += "Titre invalide!\n";
        }

        if (Subject.getValue() == null || Subject.getValue().trim().isEmpty()) {
            errorMessage += "Sujet invalide!\n";
        }

        if (Description.getText() == null || Description.getText().trim().isEmpty()) {
            errorMessage += "Description invalide!\n";
        }

        if (isMediaLoading) {
            errorMessage += "Veuillez attendre que le média soit complètement chargé!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Champs invalides");
            alert.setHeaderText("Veuillez corriger les champs invalides");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    private boolean isValidMediaFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || 
            fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
            return true;
        } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || 
                   fileName.endsWith(".mov")) {
            // For video files, check if the file is readable
            try {
                Path path = Paths.get(file.getAbsolutePath());
                return Files.isReadable(path);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private void loadImage(String url) {
        try {
            // Validate URL
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("URL d'image invalide");
            }

            // Create image with error handling
            Image image = new Image(url, true);
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    Platform.runLater(() -> {
                        if (!image.isError()) {
                            imagePreview.setImage(image);
                            imagePreview.setVisible(true);
                            videoPreview.setVisible(false);
                            if (mediaPlayer != null) {
                                mediaPlayer.stop();
                            }
                            isMediaLoading = false;
                        } else {
                            showMediaError("Erreur lors du chargement de l'image");
                            isMediaLoading = false;
                            clearMediaPreview();
                        }
                    });
                }
            });
            image.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Platform.runLater(() -> {
                        showMediaError("Erreur lors du chargement de l'image");
                        isMediaLoading = false;
                        clearMediaPreview();
                    });
                }
            });
        } catch (Exception e) {
            showMediaError("Erreur lors du chargement de l'image: " + e.getMessage());
            isMediaLoading = false;
            clearMediaPreview();
        }
    }

    private void loadVideo(String url) {
        try {
            // Validate URL
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("URL de vidéo invalide");
            }

            // Clean up existing media player
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            // Create new media and player
            Media media = new Media(url);
            mediaPlayer = new MediaPlayer(media);
            
            // Add error listener
            mediaPlayer.setOnError(() -> {
                Platform.runLater(() -> {
                    String errorMessage = "Erreur lors du chargement de la vidéo";
                    if (mediaPlayer.getError() != null) {
                        errorMessage += ": " + mediaPlayer.getError().getMessage();
                    }
                    showMediaError(errorMessage);
                    isMediaLoading = false;
                    clearMediaPreview();
                });
            });

            // Add ready listener
            mediaPlayer.setOnReady(() -> {
                Platform.runLater(() -> {
                    if (mediaPlayer.getError() == null) {
                        videoPreview.setMediaPlayer(mediaPlayer);
                        videoPreview.setVisible(true);
                        imagePreview.setVisible(false);
                        isMediaLoading = false;
                    } else {
                        showMediaError("Erreur lors du chargement de la vidéo: " + mediaPlayer.getError().getMessage());
                        isMediaLoading = false;
                        clearMediaPreview();
                    }
                });
            });

            // Add status change listener
            mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                if (newStatus == MediaPlayer.Status.HALTED) {
                    Platform.runLater(() -> {
                        showMediaError("Erreur lors de la lecture de la vidéo");
                        isMediaLoading = false;
                        clearMediaPreview();
                    });
                }
            });
        } catch (Exception e) {
            showMediaError("Erreur lors du chargement de la vidéo: " + e.getMessage());
            isMediaLoading = false;
            clearMediaPreview();
        }
    }

    private void clearMediaPreview() {
        Platform.runLater(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
            imagePreview.setImage(null);
            videoPreview.setMediaPlayer(null);
            imagePreview.setVisible(false);
            videoPreview.setVisible(false);
        });
    }

    private String formatMathematicalSymbols(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Replace mathematical symbols
        return text.replaceAll("×", "*")
                .replaceAll("÷", "/")
                .replaceAll("−", "-")
                .replaceAll("\\$", "")  // Remove $ symbols
                .replaceAll("\\{", "")  // Remove { symbols
                .replaceAll("\\}", "")  // Remove } symbols
                .replaceAll("\\[", "")  // Remove [ symbols
                .replaceAll("\\]", "")  // Remove ] symbols
                .replaceAll("\\(", "")  // Remove ( symbols
                .replaceAll("\\)", ""); // Remove ) symbols
    }

    @FXML
    void handleMediaUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Media File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov")
        );

        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            if (!isValidMediaFile(file)) {
                showMediaError("Le fichier média sélectionné est invalide ou inaccessible");
                return;
            }

            isMediaLoading = true;
            selectedMediaFile = file;
            String fileName = file.getName().toLowerCase();
            
            try {
                String fileUrl = file.toURI().toString();
                if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || 
                    fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                    mediaType = "image";
                    loadImage(fileUrl);
                } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || 
                           fileName.endsWith(".mov")) {
                    mediaType = "video";
                    loadVideo(fileUrl);
                }
            } catch (Exception e) {
                showMediaError("Erreur lors du chargement du média: " + e.getMessage());
                isMediaLoading = false;
                clearMediaPreview();
            }
        }
    }

    private void showMediaError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de média");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    void initialize() {
        assert Description != null : "fx:id=\"Description\" was not injected: check your FXML file.";
        assert Subject != null : "fx:id=\"Subject\" was not injected: check your FXML file.";
        assert Title != null : "fx:id=\"Title\" was not injected: check your FXML file.";
        assert ReturnButton != null : "fx:id=\"ReturnButton\" was not injected: check your FXML file.";
        assert uploadMediaButton != null : "fx:id=\"uploadMediaButton\" was not injected: check your FXML file.";
        assert imagePreview != null : "fx:id=\"imagePreview\" was not injected: check your FXML file.";
        assert videoPreview != null : "fx:id=\"videoPreview\" was not injected: check your FXML file.";
        assert mediaPreviewContainer != null : "fx:id=\"mediaPreviewContainer\" was not injected: check your FXML file.";
    }

    public void setForumToModify(Forum forum) {
        this.forumToModify = forum;
        // Populate fields with forum data
        Title.setText(forum.getTitle());
        Description.setText(forum.getDescription());
        Subject.setValue(forum.getSubject());

        // Handle media preview
        if (forum.getMedia() != null && !forum.getMedia().isEmpty()) {
            isMediaLoading = true;
            try {
                // Convert relative path to full URL for display
                String fileUrl = FileUtils.getMediaUrl(forum.getMedia());
                if (fileUrl == null || fileUrl.isEmpty()) {
                    showMediaError("Le chemin du média est invalide");
                    isMediaLoading = false;
                    return;
                }

                if ("image".equals(forum.getType_media())) {
                    loadImage(fileUrl);
                } else if ("video".equals(forum.getType_media())) {
                    loadVideo(fileUrl);
                } else {
                    showMediaError("Type de média non supporté: " + forum.getType_media());
                    isMediaLoading = false;
                }
            } catch (Exception e) {
                showMediaError("Erreur lors du chargement du média: " + e.getMessage());
                isMediaLoading = false;
                clearMediaPreview();
            }
        }
    }

    @FXML
    void handleModify(ActionEvent event) {

        String OldMedia = forumToModify.getMedia();

        System.out.println(OldMedia);

        if (isInputValid()) {
            try {
                // Update forum object with form data
                forumToModify.setSubject(Subject.getValue());

                String filteredTitle = profanityFilterService.filterText(Title.getText());
                String filteredDescription = profanityFilterService.filterText(Description.getText());

                forumToModify.setTitle(filteredTitle);
                forumToModify.setDescription(filteredDescription);

                String aiResponse = AI21Service.getInstance().generateText(Description.getText());
                forumToModify.setAiprompt_responce(formatMathematicalSymbols(aiResponse));

                // Handle media
                if (selectedMediaFile != null) {
                    try {
                        // Store only the relative path in the database
                        String relativePath = FileUtils.copyForumMedia(selectedMediaFile, mediaType);
                        forumToModify.setMedia(relativePath);
                        forumToModify.setType_media(mediaType);
                    } catch (Exception e) {
                        showMediaError("Erreur lors de la sauvegarde du média: " + e.getMessage());
                        return;
                    }

                    if (OldMedia != null && !OldMedia.isEmpty()) {
                        try {
                            // Get the user's documents folder
                            String documentsPath = System.getProperty("user.home") + "\\Documents";
                            // Construct the full path to the media file
                            String fullMediaPath = documentsPath + OldMedia;

                            // Delete the media file
                            Path mediaPath = Paths.get(fullMediaPath);
                            if (Files.exists(mediaPath)) {
                                Files.delete(mediaPath);
                            }
                        } catch (IOException e) {
                            // Log the error but continue with forum deletion
                            System.err.println("Error deleting media file: " + e.getMessage());
                        }
                    }

                }

                // Update in database
                forumService.update(forumToModify);

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Discussion modifiée avec succès!");
                alert.showAndWait();

                // Return to the forum view page
                navigateToForumView(event);

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Modification échouée");
                alert.setContentText("Une erreur est survenue lors de la modification:\n" + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    @FXML
    void HandleReturn(ActionEvent event) {
        navigateToForumView(event);
    }

    private void navigateToForumView(ActionEvent event) {
        try {
            Parent forumView = FXMLLoader.load(getClass().getResource("/view/Forum.fxml"));
            Scene scene = new Scene(forumView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de retourner à la page précédente: " + e.getMessage());
            alert.showAndWait();
        }
    }
}