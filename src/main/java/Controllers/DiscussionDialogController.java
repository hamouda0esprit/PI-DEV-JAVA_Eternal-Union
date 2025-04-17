package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import entite.Discussion;
import service.DiscussionService;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

public class DiscussionDialogController {
    @FXML private TextArea descriptionArea;
    @FXML private ImageView uploadedImage;
    @FXML private VBox uploadPrompt;
    @FXML private VBox photoUploadBox;
    @FXML private Button choosePhotoButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Window dialogStage;
    private boolean saveClicked = false;
    private String selectedPhotoPath;
    private int eventId;
    private DiscussionService discussionService;

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

            // Display the image
            Image image = new Image(file.toURI().toString());
            uploadedImage.setImage(image);
            uploadedImage.setVisible(true);
            uploadPrompt.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert
            showAlert("Error", "Failed to upload photo: " + e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        if (isInputValid()) {
            Discussion discussion = new Discussion();
            discussion.setEventId(eventId);
            discussion.setDescription(descriptionArea.getText());
            discussion.setPhotoPath(selectedPhotoPath);
            discussion.setCreatedAt(LocalDateTime.now());
            
            try {
                discussionService.addDiscussion(discussion);
                saveClicked = true;
                if (dialogStage instanceof Stage) {
                    ((Stage) dialogStage).close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to save discussion: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage instanceof Stage) {
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
} 