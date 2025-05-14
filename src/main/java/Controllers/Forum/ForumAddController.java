package Controllers.Forum;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import Controllers.LoginController;
import entite.Forum;
import entite.User;
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
import service.UserService;
import utils.FileUtils;
import service.ProfanityFilterService;
import service.LanguageToolService;

public class ForumAddController {

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

    private File selectedMediaFile;
    private String mediaType;
    private MediaPlayer mediaPlayer;
    private final ProfanityFilterService profanityFilterService;
    public final AI21Service ai21Service;
    private final LanguageToolService languageToolService;
    private User currentUser;
    private UserService userService;

    public ForumAddController() {
        this.ai21Service = new AI21Service();
        this.profanityFilterService = new ProfanityFilterService();
        this.languageToolService = new LanguageToolService();
        this.currentUser = LoginController.getAuthenticatedUser();
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

    @FXML
    void handleMediaUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Media File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov")
        );

        selectedMediaFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedMediaFile != null) {
            String fileName = selectedMediaFile.getName().toLowerCase();
            if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mov")) {
                mediaType = "video";
                // Hide image preview and show video preview
                imagePreview.setVisible(false);
                videoPreview.setVisible(true);
                
                // Stop any existing media player
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                
                // Create new media player
                Media media = new Media(selectedMediaFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                videoPreview.setMediaPlayer(mediaPlayer);
                mediaPlayer.setAutoPlay(false);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } else {
                mediaType = "image";
                // Hide video preview and show image preview
                videoPreview.setVisible(false);
                imagePreview.setVisible(true);
                
                // Stop any existing media player
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                
                // Load and display image
                Image image = new Image(selectedMediaFile.toURI().toString());
                imagePreview.setImage(image);
            }
        }
    }

    void logout(ActionEvent event) {
        try {
            Parent forumView = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Scene scene = new Scene(forumView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void add(ActionEvent event) {
        if (isInputValid()) {
            ForumService forumService = new ForumService();
            Forum forum = new Forum();
            UserService userService = new UserService();
            try {
                // Filter title and description for profanity
                String titleText = Title.getText().trim();
                String descriptionText = Description.getText().trim();
                
                if (titleText.isEmpty() || descriptionText.isEmpty()) {
                    throw new IllegalArgumentException("Title and description cannot be empty");
                }

                String filteredTitle = profanityFilterService.filterText(titleText);
                String filteredDescription = profanityFilterService.filterText(descriptionText);

                if (!filteredTitle.equals(titleText)){
                    currentUser.setWarnings(currentUser.getWarnings()+1);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("WARNING !");
                    alert.setHeaderText("Warnings : " + currentUser.getWarnings());
                    alert.setContentText("Your have been warned for typing an innapropriate title !");
                    alert.showAndWait();

                    userService.updateUser(currentUser);
                }

                if (!filteredDescription.equals(descriptionText)){
                    currentUser.setWarnings(currentUser.getWarnings()+1);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("WARNING !");
                    alert.setHeaderText("Warnings : " + currentUser.getWarnings());
                    alert.setContentText("Your have been warned for typing an innapropriate description !");
                    alert.showAndWait();

                    userService.updateUser(currentUser);
                }

                // Check grammar and spelling using LanguageTool
                /*String languageToolResponse = languageToolService.checkText(filteredDescription);
                System.out.println("LanguageTool Response for Description:");
                System.out.println(languageToolResponse);*/

                // Additional validation after filtering
                if (filteredTitle == null || filteredTitle.isEmpty()) {
                    throw new IllegalArgumentException("Filtered title cannot be null or empty");
                }
                if (filteredDescription == null || filteredDescription.isEmpty()) {
                    throw new IllegalArgumentException("Filtered description cannot be null or empty");
                }

                forum.setTitle(filteredTitle);
                forum.setDescription(filteredDescription);
                forum.setSubject(Subject.getValue());
                forum.setDate_time(Timestamp.valueOf(LocalDateTime.now()));

                // Handle media
                if (selectedMediaFile != null) {
                    // Copy the file to the Documents folder and get the relative path
                    String relativePath = FileUtils.copyForumMedia(selectedMediaFile, mediaType);
                    forum.setMedia(relativePath);
                    forum.setType_media(mediaType);
                } else {
                    forum.setMedia("");
                    forum.setType_media("");
                }

                String aiResponse = AI21Service.getInstance().generateText(filteredDescription);
                forum.setAiprompt_responce(formatMathematicalSymbols(aiResponse));

                forum.setUser(LoginController.getAuthenticatedUser());
                forumService.createPst(forum);

                // Stop video playback if active
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Forum added successfully!");
                alert.showAndWait();

                if (currentUser.getWarnings()>=3){
                    alert.setTitle("Terminating session");
                    alert.setHeaderText("Locking account");
                    alert.setContentText("You have been logged out due to reaching 3 or more warnings.");
                    alert.showAndWait();
                    logout(event);
                }else{
                    navigateToForumView(event);
                }

            } catch (IllegalArgumentException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText("Invalid Input");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Forum not added");
                alert.setContentText("Something went wrong:\n" + e.getMessage());
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
            Parent ajoutForumView = FXMLLoader.load(getClass().getResource("/view/Forum.fxml"));
            Scene scene = new Scene(ajoutForumView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        currentUser = LoginController.getAuthenticatedUser();
        
        // Initialize media previews
        imagePreview.setVisible(false);
        videoPreview.setVisible(false);
        
        // Set up video preview
        videoPreview.setFitWidth(400);
        videoPreview.setPreserveRatio(true);
        
        // Set up image preview
        imagePreview.setFitWidth(400);
        imagePreview.setPreserveRatio(true);
    }
}
