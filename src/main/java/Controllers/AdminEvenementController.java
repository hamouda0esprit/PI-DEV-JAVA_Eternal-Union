package Controllers;

import entite.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.EvenementService;
import service.IEvenementService;
import service.DiscussionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminEvenementController implements Initializable {
    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> titleColumn;
    @FXML private TableColumn<Evenement, String> dateColumn;
    @FXML private TableColumn<Evenement, String> timeColumn;
    @FXML private TableColumn<Evenement, String> locationColumn;
    @FXML private TableColumn<Evenement, String> descriptionColumn;
    @FXML private TableColumn<Evenement, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Label statusLabel;

    private IEvenementService evenementService;
    private DiscussionService discussionService;
    private ObservableList<Evenement> eventsList;
    private FilteredList<Evenement> filteredEvents;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        evenementService = new EvenementService();
        discussionService = new DiscussionService();
        setupTable();
        loadEvents();
        setupSearch();
        setupAddButton();
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateevent"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox buttons = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                buttons.setAlignment(Pos.CENTER);
                
                // Style buttons
                String baseStyle = "-fx-cursor: hand; -fx-min-width: 32; -fx-min-height: 32; -fx-background-radius: 16;";
                
                // View button - Yellow
                viewBtn.setStyle(baseStyle + 
                    "-fx-background-color: #ffeaa7; -fx-text-fill: #fdcb6e;");
                viewBtn.setOnMouseEntered(e -> viewBtn.setStyle(baseStyle + 
                    "-fx-background-color: #fdcb6e; -fx-text-fill: white;"));
                viewBtn.setOnMouseExited(e -> viewBtn.setStyle(baseStyle + 
                    "-fx-background-color: #ffeaa7; -fx-text-fill: #fdcb6e;"));
                
                // Edit button - Blue
                editBtn.setStyle(baseStyle + 
                    "-fx-background-color: #74b9ff; -fx-text-fill: #0984e3;");
                editBtn.setOnMouseEntered(e -> editBtn.setStyle(baseStyle + 
                    "-fx-background-color: #0984e3; -fx-text-fill: white;"));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(baseStyle + 
                    "-fx-background-color: #74b9ff; -fx-text-fill: #0984e3;"));
                
                // Delete button - Red
                deleteBtn.setStyle(baseStyle + 
                    "-fx-background-color: #ff7675; -fx-text-fill: #d63031;");
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(baseStyle + 
                    "-fx-background-color: #d63031; -fx-text-fill: white;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(baseStyle + 
                    "-fx-background-color: #ff7675; -fx-text-fill: #d63031;"));
                
                // Setup actions
                viewBtn.setOnAction(e -> handleView(getTableRow().getItem()));
                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadEvents() {
        eventsList = FXCollections.observableArrayList(evenementService.getAllEvenements());
        filteredEvents = new FilteredList<>(eventsList);
        eventTable.setItems(filteredEvents);
        updateStatusLabel();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredEvents.setPredicate(event -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return event.getName().toLowerCase().contains(lowerCaseFilter) ||
                       event.getLocation().toLowerCase().contains(lowerCaseFilter) ||
                       event.getDescription().toLowerCase().contains(lowerCaseFilter);
            });
            updateStatusLabel();
        });
    }

    private void setupAddButton() {
        addButton.setOnAction(e -> showAddEventDialog());
    }

    private void showAddEventDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddEventDialog.fxml"));
            VBox dialogContent = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Ajouter un événement");
            
            Scene scene = new Scene(dialogContent);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            dialogStage.setScene(scene);
            
            AddEventDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isSaved()) {
                loadEvents();
            }
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre d'ajout d'événement");
        }
    }

    private void handleView(Evenement event) {
        if (event != null) {
            showInfo("Voir l'événement", 
                    String.format("Titre: %s\nDate: %s\nHeure: %s\nLieu: %s\nDescription: %s",
                    event.getName(), event.getDateevent(), event.getTime(), 
                    event.getLocation(), event.getDescription()));
        }
    }

    private void handleEdit(Evenement event) {
        if (event != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddEventDialog.fxml"));
                VBox dialogContent = loader.load();
                
                AddEventDialogController controller = loader.getController();
                controller.setEvent(event);
                
                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.setTitle("Modifier l'événement");
                
                Scene scene = new Scene(dialogContent);
                scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
                
                dialogStage.setScene(scene);
                controller.setDialogStage(dialogStage);
                
                dialogStage.showAndWait();
                
                if (controller.isSaved()) {
                    loadEvents();
                }
            } catch (IOException e) {
                showError("Erreur", "Impossible d'ouvrir la fenêtre de modification");
            }
        }
    }

    private void handleDelete(Evenement event) {
        if (event != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer l'événement: " + event.getName());
            alert.setContentText("Cette action supprimera également toutes les discussions associées à cet événement. Êtes-vous sûr de vouloir continuer ?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // First delete all related discussions
                        discussionService.deleteDiscussionsForEvent(event.getId());
                        // Then delete the event
                        evenementService.delete(event.getId());
                        loadEvents();
                        showInfo("Succès", "L'événement et ses discussions ont été supprimés avec succès.");
                    } catch (Exception e) {
                        showError("Erreur de suppression", 
                                "Une erreur est survenue lors de la suppression: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void updateStatusLabel() {
        int total = (int) filteredEvents.stream().count();
        statusLabel.setText(String.format("Total événements: %d", total));
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 