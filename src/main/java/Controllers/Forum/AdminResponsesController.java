package Controllers.Forum;

import entite.Forum;
import entite.Responces;
import entite.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.ResponcesService;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class AdminResponsesController implements Initializable {
    @FXML private Label forumTitleLabel;
    @FXML private TableView<Responces> responsesTable;
    @FXML private TableColumn<Responces, String> contentColumn;
    @FXML private TableColumn<Responces, String> authorColumn;
    @FXML private TableColumn<Responces, Date> dateColumn;
    @FXML private TableColumn<Responces, Void> actionsColumn;
    @FXML private Label statusLabel;
    @FXML private TextArea newResponseArea;
    @FXML private Button addResponseButton;
    @FXML private Button closeButton;

    private Forum currentForum;
    private ResponcesService responcesService;
    private ObservableList<Responces> responsesList;
    private FilteredList<Responces> filteredResponses;
    private User currentUser; // This would normally come from a session or auth service

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        responcesService = new ResponcesService();

        // Setup table columns
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        authorColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue().getUser();
            if (user != null) {
                return javafx.beans.binding.Bindings.createStringBinding(() -> user.getName());
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "Unknown");
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_time"));

        setupActionsColumn();

        // Setup event handlers
        addResponseButton.setOnAction(e -> handleAddResponse());
        closeButton.setOnAction(e -> closeDialog());

        // For testing purposes, create a mock user (this should be replaced with actual login logic)
        currentUser = new User();
        currentUser.setId(2);
        currentUser.setName("Admin");
    }

    public void setForum(Forum forum) {
        this.currentForum = forum;
        forumTitleLabel.setText("Réponses pour: " + forum.getTitle());
        loadResponses();
    }

    private void loadResponses() {
        if (currentForum != null) {
            responsesList = FXCollections.observableArrayList(responcesService.readByForumId(currentForum.getId()));

            // Ensure all loaded responses have the forum object set
            for (Responces response : responsesList) {
                if (response.getForum() == null) {
                    response.setForum(currentForum);
                }
            }

            filteredResponses = new FilteredList<>(responsesList);
            responsesTable.setItems(filteredResponses);
            updateStatusLabel();
        }
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                buttons.setAlignment(Pos.CENTER);

                // Style buttons
                String buttonStyle = "-fx-background-color: transparent; -fx-cursor: hand;";
                editBtn.setStyle(buttonStyle);
                deleteBtn.setStyle(buttonStyle);

                // Add hover effect
                editBtn.setOnMouseEntered(e -> editBtn.setStyle(buttonStyle + "-fx-text-fill: #5B9BD5;"));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(buttonStyle));
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(buttonStyle + "-fx-text-fill: #dc3545;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(buttonStyle));

                // Setup actions
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

    private void handleAddResponse() {
        String comment = newResponseArea.getText().trim();

        if (comment.isEmpty()) {
            showError("Erreur", "Le contenu de la réponse ne peut pas être vide");
            return;
        }

        try {
            Responces newResponse = new Responces();
            newResponse.setForum(currentForum);
            newResponse.setUser(currentUser);
            newResponse.setComment(comment);
            newResponse.setMedia(""); // No media by default
            newResponse.setType_media(""); // No media type by default
            newResponse.setDate_time(new Date()); // Current date/time

            responcesService.createPst(newResponse);

            newResponseArea.clear();
            loadResponses();
            showInfo("Succès", "Réponse ajoutée avec succès");
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ajouter la réponse: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEdit(Responces response) {
        if (response != null) {
            // Make sure forum is set before proceeding
            if (response.getForum() == null) {
                response.setForum(currentForum);
            }

            // Open a dialog to edit the response
            TextInputDialog dialog = new TextInputDialog(response.getComment());
            dialog.setTitle("Modifier la réponse");
            dialog.setHeaderText("Modifier le contenu de la réponse");
            dialog.setContentText("Contenu:");

            dialog.showAndWait().ifPresent(result -> {
                if (!result.trim().isEmpty()) {
                    try {
                        // Make sure forum is still set
                        if (response.getForum() == null) {
                            response.setForum(currentForum);
                        }
                        response.setComment(result.trim());
                        responcesService.update(response);
                        loadResponses();
                    } catch (Exception e) {
                        showError("Erreur", "Impossible de modifier la réponse: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void handleDelete(Responces response) {
        if (response != null) {
            // Make sure forum is set before proceeding
            if (response.getForum() == null) {
                response.setForum(currentForum);
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer cette réponse");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    try {
                        responcesService.delete(response);
                        loadResponses();
                    } catch (Exception e) {
                        showError("Erreur", "Impossible de supprimer la réponse: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void updateStatusLabel() {
        int total = responsesList.size();
        statusLabel.setText(String.format("Total réponses: %d", total));
    }

    private void closeDialog() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
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