package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import entite.Discussion;
import entite.Evenement;
import entite.User;
import service.DiscussionService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class DiscussionFeedController {
    @FXML private ImageView eventCoverImage;
    @FXML private Label eventTitleLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventLocationLabel;
    @FXML private VBox discussionsContainer;
    @FXML private Button addDiscussionButton;

    private Evenement event;
    private DiscussionService discussionService;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy à HH:mm");
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Setting current user: " + (user != null ? user.getType() : "null"));
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (addDiscussionButton != null) {
            System.out.println("Updating button visibility for user: " + (currentUser != null ? currentUser.getType() : "null"));
            boolean isTeacher = currentUser != null && 
                (currentUser.getType().equalsIgnoreCase("teacher") || 
                 currentUser.getType().equalsIgnoreCase("professeur") ||
                 currentUser.getType().equalsIgnoreCase("prof") ||
                 currentUser.getType().equals("1"));
            addDiscussionButton.setVisible(isTeacher);
        } else {
            System.out.println("addDiscussionButton is null!");
        }
    }

    @FXML
    private void handleModify() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DiscussionDialog.fxml"));
            VBox dialogContent = loader.load();
            
            Dialog<ButtonType> dialog = new Dialog<>();
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(dialogContent);
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Hide the default buttons since we have custom ones
            dialogPane.lookupButton(ButtonType.OK).setVisible(false);
            dialogPane.lookupButton(ButtonType.CANCEL).setVisible(false);
            
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifier la discussion");
            
            // Get the controller and set up the dialog
            DiscussionDialogController controller = loader.getController();
            controller.setEventId(event.getId());
            controller.setDialogStage(dialog.getDialogPane().getScene().getWindow());
            controller.setEditMode(true, -1); // -1 indicates new discussion
            
            // Show the dialog and handle the result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK && controller.isSaveClicked()) {
                    return ButtonType.OK;
                }
                return ButtonType.CANCEL;
            });
            
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Discussion updatedDiscussion = controller.getDiscussion();
                    if (updatedDiscussion != null) {
                        discussionService.updateDiscussion(updatedDiscussion);
                        loadDiscussions(); // Refresh the discussions list
                    }
                }
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture de la boîte de dialogue de modification");
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette discussion ?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Get the selected discussion ID from the UI
                    // This would need to be implemented based on your UI selection mechanism
                    int discussionId = getSelectedDiscussionId();
                    if (discussionId != -1) {
                        discussionService.removeDiscussion(discussionId);
                        loadDiscussions(); // Refresh the discussions list
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur lors de la suppression de la discussion");
                }
            }
        });
    }

    private int getSelectedDiscussionId() {
        // This method should return the ID of the currently selected discussion
        // You'll need to implement this based on how you track the selected discussion
        // For now, returning -1 as a placeholder
        return -1;
    }

    public void initialize() {
        discussionService = new DiscussionService();
        addDiscussionButton.setOnAction(e -> handleAddDiscussion());
        System.out.println("Initialize - Current user: " + (currentUser != null ? currentUser.getType() : "null"));
        updateButtonVisibility();
    }

    public void setEvent(Evenement event) {
        this.event = event;
        updateEventHeader();
        loadDiscussions();
    }

    private void updateEventHeader() {
        if (event.getPhoto() != null && !event.getPhoto().isEmpty()) {
            try {
                File imageFile = new File(event.getPhoto());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    eventCoverImage.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Error loading event image: " + e.getMessage());
            }
        }

        eventTitleLabel.setText(event.getName());
        LocalDateTime eventDateTime = event.getDateevent().toLocalDate().atStartOfDay();
        String formattedDate = eventDateTime.format(dateFormatter);
        eventDateLabel.setText(formattedDate);
        eventLocationLabel.setText(event.getLocation());
    }

    private void loadDiscussions() {
        discussionsContainer.getChildren().clear();
        List<Discussion> discussions = discussionService.getDiscussionsForEvent(event.getId());
        
        for (Discussion discussion : discussions) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DiscussionCard.fxml"));
                VBox discussionCard = loader.load();
                DiscussionCardController controller = loader.getController();
                controller.setDiscussion(discussion);
                controller.setCurrentUser(currentUser);

                // Set delete callback
                controller.setOnDelete(() -> {
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmation.setTitle("Confirmation de suppression");
                    confirmation.setHeaderText(null);
                    confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette discussion ?");
                    
                    confirmation.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                discussionService.removeDiscussion(discussion.getId());
                                loadDiscussions(); // Refresh the discussions list
                            } catch (Exception e) {
                                e.printStackTrace();
                                showError("Erreur lors de la suppression de la discussion");
                            }
                        }
                    });
                });

                // Set modify callback
                controller.setOnModify(() -> {
                    try {
                        FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("/view/DiscussionDialog.fxml"));
                        VBox dialogContent = dialogLoader.load();
                        Dialog<ButtonType> dialog = new Dialog<>();
                        DialogPane dialogPane = new DialogPane();
                        dialogPane.setContent(dialogContent);
                        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                        dialogPane.lookupButton(ButtonType.OK).setVisible(false);
                        dialogPane.lookupButton(ButtonType.CANCEL).setVisible(false);
                        dialog.setDialogPane(dialogPane);
                        dialog.setTitle("Modifier la discussion");
                        DiscussionDialogController dialogController = dialogLoader.getController();
                        dialogController.setEventId(event.getId());
                        dialogController.setDialogStage(dialog.getDialogPane().getScene().getWindow());
                        dialogController.setEditMode(true, discussion.getId());
                        
                        // Pre-fill dialog with existing data
                        dialogController.descriptionArea.setText(discussion.getCaption());
                        if (discussion.getMedia() != null && !discussion.getMedia().isEmpty()) {
                            dialogController.uploadedImage.setImage(new Image(
                                discussion.getMedia().startsWith("http") ? discussion.getMedia() : new File(discussion.getMedia().replace("\\", File.separator)).toURI().toString()
                            ));
                            dialogController.uploadedImage.setVisible(true);
                            dialogController.uploadPrompt.setVisible(false);
                            dialogController.selectedPhotoPath = discussion.getMedia().startsWith("http") ? null : discussion.getMedia();
                            dialogController.selectedGifUrl = discussion.getMedia().startsWith("http") ? discussion.getMedia() : null;
                        }
                        
                        dialog.setResultConverter(dialogButton -> {
                            if (dialogButton == ButtonType.OK && dialogController.isSaveClicked()) {
                                return ButtonType.OK;
                            }
                            return ButtonType.CANCEL;
                        });
                        
                        dialog.showAndWait().ifPresent(buttonType -> {
                            if (buttonType == ButtonType.OK) {
                                Discussion updated = dialogController.getDiscussion();
                                if (updated != null) {
                                    updated.setId(discussion.getId());
                                    discussionService.updateDiscussion(updated);
                                    loadDiscussions();
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Erreur lors de l'ouverture de la boîte de dialogue de modification");
                    }
                });

                discussionsContainer.getChildren().add(discussionCard);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur lors du chargement de la discussion");
            }
        }
    }

    @FXML
    private void handleAddDiscussion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DiscussionDialog.fxml"));
            VBox dialogContent = loader.load();
            
            Dialog<ButtonType> dialog = new Dialog<>();
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(dialogContent);
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Hide the default buttons since we have custom ones
            dialogPane.lookupButton(ButtonType.OK).setVisible(false);
            dialogPane.lookupButton(ButtonType.CANCEL).setVisible(false);
            
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter un commentaire");
            
            // Get the controller and set up the dialog
            DiscussionDialogController controller = loader.getController();
            controller.setEventId(event.getId());
            controller.setDialogStage(dialog.getDialogPane().getScene().getWindow());
            
            // Show the dialog and handle the result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK && controller.isSaveClicked()) {
                    return ButtonType.OK;
                }
                return ButtonType.CANCEL;
            });
            
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Discussion newDiscussion = controller.getDiscussion();
                    if (newDiscussion != null) {
                        loadDiscussions(); // Refresh the discussions list immediately
                    }
                }
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening discussion dialog");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 