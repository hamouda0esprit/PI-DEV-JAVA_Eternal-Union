package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import entite.Discussion;
import service.DiscussionService;
import service.GiphyService.GiphyResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

public class DiscussionDialogController {
    @FXML public TextArea descriptionArea;
    @FXML public ImageView uploadedImage;
    @FXML public VBox uploadPrompt;
    @FXML private VBox photoUploadBox;
    @FXML private Button choosePhotoButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Window dialogStage;
    private boolean saveClicked = false;
    public String selectedPhotoPath;
    public String selectedGifUrl;
    private int eventId;
    private DiscussionService discussionService;
    private boolean editMode = false;
    private int editingDiscussionId = -1;

    public void initialize() {
        // Initialize the discussion service
        discussionService = new DiscussionService();
        
        // Add drag and drop support for the photo upload area
        photoUploadBox.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        photoUploadBox.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasFiles()) {
                File file = event.getDragboard().getFiles().get(0);
                if (isImageFile(file)) {
                    handlePhotoFile(file);
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void handleChooseGif() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GifPickerDialog.fxml"));
            VBox dialogContent = loader.load();
            
            Dialog<ButtonType> dialog = new Dialog<>();
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(dialogContent);
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Hide the default buttons since we have custom ones
            dialogPane.lookupButton(ButtonType.OK).setVisible(false);
            dialogPane.lookupButton(ButtonType.CANCEL).setVisible(false);
            
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Choose a GIF");
            
            // Get the controller and set up the dialog
            GifPickerController controller = loader.getController();
            controller.setDialogStage(dialog.getDialogPane().getScene().getWindow());
            
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    GiphyResult selectedGif = controller.getSelectedGif();
                    if (selectedGif != null) {
                        selectedGifUrl = selectedGif.getOriginalUrl();
                        selectedPhotoPath = null; // Clear any selected photo
                        
                        // Show the GIF preview
                        uploadedImage.setImage(new Image(selectedGif.getPreviewUrl()));
                        uploadedImage.setVisible(true);
                        uploadPrompt.setVisible(false);
                    }
                }
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open GIF picker");
        }
    }

    public void setDialogStage(Window dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Photo");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null && isImageFile(file)) {
            handlePhotoFile(file);
        }
    }

    private void handlePhotoFile(File file) {
        try {
            // Create a unique filename
            String uniqueFileName = System.currentTimeMillis() + "_" + file.getName();
            Path uploadDir = Paths.get("uploads", "discussions");
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(uniqueFileName);
            
            // Copy the file to our uploads directory
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            selectedPhotoPath = targetPath.toString();
            selectedGifUrl = null; // Clear any selected GIF

            // Display the image
            Image image = new Image(file.toURI().toString());
            uploadedImage.setImage(image);
            uploadedImage.setVisible(true);
            uploadPrompt.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to upload photo: " + e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        if (isInputValid()) {
            Discussion discussion = new Discussion();
            discussion.setEventId(eventId);
            discussion.setCaption(descriptionArea.getText());
            discussion.setMedia(selectedGifUrl != null ? selectedGifUrl : selectedPhotoPath);
            discussion.setCreatedAt(LocalDateTime.now());
            if (editMode) {
                discussion.setId(editingDiscussionId);
                saveClicked = true;
                // Close dialog logic (same as before)
                Scene scene = dialogStage.getScene();
                if (scene != null && scene.getRoot() instanceof DialogPane) {
                    DialogPane dialogPane = (DialogPane) scene.getRoot();
                    Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                    if (okButton != null) {
                        okButton.fire();
                    }
                } else if (dialogStage instanceof Stage) {
                    ((Stage) dialogStage).close();
                }
            } else {
                try {
                    discussionService.addDiscussion(discussion);
                    saveClicked = true;
                    // Close dialog logic (same as before)
                    Scene scene = dialogStage.getScene();
                    if (scene != null && scene.getRoot() instanceof DialogPane) {
                        DialogPane dialogPane = (DialogPane) scene.getRoot();
                        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                        if (okButton != null) {
                            okButton.fire();
                        }
                    } else if (dialogStage instanceof Stage) {
                        ((Stage) dialogStage).close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to save discussion: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleCancel() {
        // Find the dialog pane
        Scene scene = dialogStage.getScene();
        if (scene != null && scene.getRoot() instanceof DialogPane) {
            DialogPane dialogPane = (DialogPane) scene.getRoot();
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.fire();
            }
        } else if (dialogStage instanceof Stage) {
            ((Stage) dialogStage).close();
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            errorMessage += "Description is required!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert("Validation Error", errorMessage);
            return false;
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || 
               name.endsWith(".jpeg") || name.endsWith(".gif");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Discussion getDiscussion() {
        if (!saveClicked) {
            return null;
        }
        Discussion discussion = new Discussion();
        discussion.setEventId(eventId);
        discussion.setCaption(descriptionArea.getText());
        discussion.setMedia(selectedGifUrl != null ? selectedGifUrl : selectedPhotoPath);
        discussion.setCreatedAt(LocalDateTime.now());
        return discussion;
    }

    public void setEditMode(boolean editMode, int discussionId) {
        this.editMode = editMode;
        this.editingDiscussionId = discussionId;
    }
} 