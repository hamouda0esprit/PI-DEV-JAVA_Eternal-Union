package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Window;
import service.GiphyService;
import service.GiphyService.GiphyResult;
import javafx.application.Platform;
import javafx.concurrent.Task;
import java.util.List;

public class GifPickerController {
    @FXML private TextField searchField;
    @FXML private FlowPane gifGrid;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;

    private Window dialogStage;
    private GiphyService giphyService;
    private GiphyResult selectedGif;
    private static final int SEARCH_LIMIT = 20;

    public void initialize() {
        giphyService = new GiphyService();
        
        // Add enter key listener to search field
        searchField.setOnAction(event -> handleSearch());
    }

    public void setDialogStage(Window dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showStatus("Please enter a search term");
            return;
        }

        // Clear previous results
        gifGrid.getChildren().clear();
        showLoading(true);

        Task<List<GiphyResult>> searchTask = new Task<>() {
            @Override
            protected List<GiphyResult> call() throws Exception {
                return giphyService.searchGifs(query, SEARCH_LIMIT);
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<GiphyResult> results = searchTask.getValue();
            Platform.runLater(() -> {
                showLoading(false);
                if (results.isEmpty()) {
                    showStatus("No GIFs found");
                } else {
                    displayResults(results);
                }
            });
        });

        searchTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                showLoading(false);
                showStatus("Error searching GIFs");
                event.getSource().getException().printStackTrace();
            });
        });

        new Thread(searchTask).start();
    }

    private void displayResults(List<GiphyResult> results) {
        gifGrid.getChildren().clear();
        
        for (GiphyResult gif : results) {
            ImageView imageView = new ImageView(new Image(gif.getPreviewUrl(), 
                true)); // Load image asynchronously
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
            
            // Add hover effect
            imageView.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
            
            // Add click handler
            imageView.setOnMouseClicked(event -> {
                selectedGif = gif;
                // Find the dialog through the scene
                if (dialogStage != null && dialogStage.getScene() != null 
                    && dialogStage.getScene().getRoot() instanceof DialogPane) {
                    DialogPane dialogPane = (DialogPane) dialogStage.getScene().getRoot();
                    Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                    if (okButton != null) {
                        okButton.fire();
                    }
                }
            });
            
            gifGrid.getChildren().add(imageView);
        }
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        statusLabel.setVisible(false);
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        loadingIndicator.setVisible(false);
    }

    public GiphyResult getSelectedGif() {
        return selectedGif;
    }
} 