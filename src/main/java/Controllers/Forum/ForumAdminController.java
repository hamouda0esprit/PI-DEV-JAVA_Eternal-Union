package Controllers.Forum;

import entite.Forum;
import entite.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ForumService;
import service.IService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;

public class ForumAdminController implements Initializable {
    @FXML private TableView<Forum> forumTable;
    @FXML private TableColumn<Forum, String> titleColumn;
    @FXML private TableColumn<Forum, String> subjectColumn;
    @FXML private TableColumn<Forum, String> authorColumn;
    @FXML private TableColumn<Forum, String> dateColumn;
    @FXML private TableColumn<Forum, String> descriptionColumn;
    @FXML private TableColumn<Forum, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Label statusLabel;

    // Sidebar buttons
    @FXML private Button dashboardButton;
    @FXML private Button usersButton;
    @FXML private Button forumsButton;
    @FXML private Button coursesButton;
    @FXML private Button eventsButton;
    @FXML private Button examensButton;
    @FXML private Button backButton;

    private IService<Forum> forumService;
    private ObservableList<Forum> forumsList;
    private FilteredList<Forum> filteredForums;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        forumService = new ForumService();
        setupTable();
        loadForums();
        setupSearch();
        setupAddButton();
        setupSidebarNavigation();
    }

    private void setupSidebarNavigation() {
        // Already defined in the FXML with onAction handlers
    }

    private void navigateTo(String destination) {
        try {
            String fxmlPath = switch (destination) {
                case "Dashboard" -> "/view/AdminDashboard.fxml";
                case "Users" -> "/view/AdminUsers.fxml";
                case "Events" -> "/view/AdminEvenement.fxml";
                case "Exams" -> "/view/AdminExams.fxml";
                case "Courses" -> "/view/AdminCourses.fxml";
                default -> throw new IllegalArgumentException("Unknown destination: " + destination);
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = forumTable.getScene();
            scene.setRoot(loader.load());

        } catch (IOException e) {
            showError("Navigation Error", "Unable to load " + destination + " view");
        }
    }

    private void returnToMainSite() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/loe/Forum.fxml"));
            Scene scene = forumTable.getScene();
            scene.setRoot(loader.load());
        } catch (IOException e) {
            showError("Navigation Error", "Unable to return to main site");
        }
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        authorColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue().getUser();
            if (user != null) {
                return javafx.beans.binding.Bindings.createStringBinding(() -> user.getName());
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "Unknown");
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_time"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final Button responsesBtn = new Button("💬");
            private final HBox buttons = new HBox(5, viewBtn, editBtn, responsesBtn, deleteBtn);

            {
                buttons.setAlignment(Pos.CENTER);

                // Style buttons
                String buttonStyle = "-fx-background-color: transparent; -fx-cursor: hand;";
                viewBtn.setStyle(buttonStyle);
                editBtn.setStyle(buttonStyle);
                deleteBtn.setStyle(buttonStyle);
                responsesBtn.setStyle(buttonStyle);

                // Add hover effect
                viewBtn.setOnMouseEntered(e -> viewBtn.setStyle(buttonStyle + "-fx-text-fill: #5B9BD5;"));
                viewBtn.setOnMouseExited(e -> viewBtn.setStyle(buttonStyle));
                editBtn.setOnMouseEntered(e -> editBtn.setStyle(buttonStyle + "-fx-text-fill: #5B9BD5;"));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(buttonStyle));
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(buttonStyle + "-fx-text-fill: #dc3545;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(buttonStyle));
                responsesBtn.setOnMouseEntered(e -> responsesBtn.setStyle(buttonStyle + "-fx-text-fill: #28a745;"));
                responsesBtn.setOnMouseExited(e -> responsesBtn.setStyle(buttonStyle));

                // Setup actions
                viewBtn.setOnAction(e -> handleView(getTableRow().getItem()));
                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
                responsesBtn.setOnAction(e -> handleResponses(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadForums() {
        forumsList = FXCollections.observableArrayList(((ForumService)forumService).readAll());
        filteredForums = new FilteredList<>(forumsList);
        forumTable.setItems(filteredForums);
        updateStatusLabel();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredForums.setPredicate(forum -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return forum.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        forum.getSubject().toLowerCase().contains(lowerCaseFilter) ||
                        forum.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                        (forum.getUser() != null && forum.getUser().getName().toLowerCase().contains(lowerCaseFilter));
            });
            updateStatusLabel();
        });
    }

    private void setupAddButton() {
        addButton.setOnAction(e -> showAddForumDialog());
    }

    private void showAddForumDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddForumDialog.fxml"));
            VBox dialogContent = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Ajouter un forum");

            Scene scene = new Scene(dialogContent);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            dialogStage.setScene(scene);

            AddForumDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                loadForums();
            }
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre d'ajout de forum");
        }
    }

    private void handleView(Forum forum) {
        if (forum != null) {
            // Implement view logic
            StringBuilder content = new StringBuilder();
            content.append(String.format("Titre: %s\n", forum.getTitle()));
            content.append(String.format("Sujet: %s\n", forum.getSubject()));
            content.append(String.format("Description: %s\n", forum.getDescription()));
            content.append(String.format("Date: %s\n", forum.getDate_time()));

            if (forum.getUser() != null) {
                content.append(String.format("Auteur: %s\n", forum.getUser().getName()));
            }

            if (forum.getAiprompt_responce() != null && !forum.getAiprompt_responce().isEmpty()) {
                content.append("\nRéponse IA:\n").append(forum.getAiprompt_responce());
            }

            showInfo("Voir le forum", content.toString());
        }
    }

    private void handleEdit(Forum forum) {
        if (forum != null) {
            try {
                // Check if resource exists first
                URL fxmlUrl = getClass().getResource("/com/example/loe/AddForumDialog.fxml");
                if (fxmlUrl == null) {
                    showError("Erreur", "Le fichier FXML n'a pas été trouvé");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                VBox dialogContent = loader.load();

                // Rest of your code...
                AddForumDialogController controller = loader.getController();
                controller.setForum(forum);

                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.setTitle("Modifier le forum");

                Scene scene = new Scene(dialogContent);

                dialogStage.setScene(scene);
                controller.setDialogStage(dialogStage);

                dialogStage.showAndWait();

                if (controller.isSaveClicked()) {
                    loadForums();
                }
            } catch (IOException e) {
                showError("Erreur", "Impossible d'ouvrir la fenêtre de modification");
                System.err.println("Exception details: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                showError("Erreur", "Une erreur inattendue s'est produite");
                System.err.println("Unexpected exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleResponses(Forum forum) {
        if (forum != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/loe/AdminResponces.fxml"));
                VBox dialogContent = loader.load();

                AdminResponsesController controller = loader.getController();
                controller.setForum(forum);

                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.setTitle("Réponses du forum: " + forum.getTitle());

                Scene scene = new Scene(dialogContent);
                //scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

                dialogStage.setScene(scene);

                dialogStage.showAndWait();
            } catch (IOException e) {
                showError("Erreur", "Impossible d'ouvrir la fenêtre des réponses");
            }
        }
    }

    private void handleDelete(Forum forum) {
        if (forum != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le forum: " + forum.getTitle());
            alert.setContentText("Êtes-vous sûr de vouloir supprimer ce forum et toutes ses réponses ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    forumService.delete(forum);
                    loadForums();
                }
            });
        }
    }

    private void updateStatusLabel() {
        int total = (int) filteredForums.stream().count();
        statusLabel.setText(String.format("Total forums: %d", total));
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

    @FXML
    public void handleCourses(ActionEvent actionEvent) {
        navigateTo("Courses");
    }

    @FXML
    public void handleExamens(ActionEvent actionEvent) {
        navigateTo("Exams");
    }

    @FXML
    public void handleEvents(ActionEvent actionEvent) {
        navigateTo("Events");
    }

    @FXML
    public void handleBack(ActionEvent actionEvent) {
        returnToMainSite();
    }

    @FXML
    public void handleUsers(ActionEvent actionEvent) {
        navigateTo("Users");
    }

    @FXML
    public void handleForums(ActionEvent actionEvent) {
        // We're already on the Forums page
    }

    @FXML
    public void handleDashboard(ActionEvent actionEvent) {
        navigateTo("Dashboard");
    }
}