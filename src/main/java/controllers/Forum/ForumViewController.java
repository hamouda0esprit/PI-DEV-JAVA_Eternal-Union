package Controllers.Forum;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import entite.Forum;
import entite.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.ForumService;
import service.UserService;

public class ForumViewController {

    @FXML
    private ImageView logoImage;

    @FXML
    private Button accueilButton;

    @FXML
    private Button coursButton;

    @FXML
    private Button examenButton;

    @FXML
    private Button evenementsButton;

    @FXML
    private Button forumButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button returnButton;

    @FXML
    private VBox discussionsContainer;

    private ForumService forumService;
    private UserService userService;
    private User currentUser;

    @FXML
    void initialize() {
        // Initialize services
        forumService = new ForumService();
        userService = new UserService();

        // Set current user (for demo purposes, you might get this from a session or other mechanism)
        currentUser = userService.readById(2); // Assuming user ID 2 is the current user

        // Load forum discussions
        loadDiscussions();
    }

    private void loadDiscussions() {
        try {
            // Clear existing discussions
            discussionsContainer.getChildren().clear();

            // Get discussions for the current user
            List<Forum> userForums = forumService.readByUserId(2);

            // If no discussions, show a message
            if (userForums.isEmpty()) {
                Label emptyLabel = new Label("Vous n'avez pas encore créé de discussions.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
                discussionsContainer.getChildren().add(emptyLabel);
            } else {
                // Add each forum discussion to the container
                for (Forum forum : userForums) {
                    discussionsContainer.getChildren().add(createDiscussionItem(forum));
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les discussions.", e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createDiscussionItem(Forum forum) {
        // Create container for discussion item
        VBox discussionBox = new VBox();
        discussionBox.getStyleClass().add("discussion-item");
        discussionBox.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5;");
        discussionBox.setPadding(new Insets(15));

        // Create title label
        Label titleLabel = new Label(forum.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Create description label (using a part of the description)
        String descText = forum.getDescription();
        if (descText.length() > 50) {
            descText = descText.substring(0, 47) + "...";
        }
        Label descLabel = new Label(descText);
        descLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #757575;");

        // Create buttons container
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));

        // Create modify button
        Button modifyButton = new Button("Modifier");
        modifyButton.getStyleClass().add("action-button");
        modifyButton.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #42a5f5;");
        modifyButton.setOnAction(e -> handleModify(forum));

        // Create delete button
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("action-button");
        deleteButton.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #f44336;");
        deleteButton.setOnAction(e -> handleDelete(forum));

        // Add buttons to the container
        buttonsBox.getChildren().addAll(modifyButton, deleteButton);

        // Add all elements to the discussion box
        discussionBox.getChildren().addAll(titleLabel, descLabel, buttonsBox);

        return discussionBox;
    }

    @FXML
    void handleAccueil(ActionEvent event) {
        navigateTo("/com/example/loe/Accueil.fxml", event);
    }

    @FXML
    void handleCours(ActionEvent event) {
        navigateTo("/com/example/loe/Cours.fxml", event);
    }

    @FXML
    void handleExamen(ActionEvent event) {
        navigateTo("/com/example/loe/Examen.fxml", event);
    }

    @FXML
    void handleEvenements(ActionEvent event) {
        navigateTo("/view/Evenement.fxml", event);
    }

    @FXML
    void handleForum(ActionEvent event) {
        navigateTo("/view/Forum.fxml", event);
    }

    @FXML
    void handleReturn(ActionEvent event) {
        navigateTo("/view/Forum.fxml", event);
    }

    // Update the handleModify method in ForumViewController.java

    private void handleModify(Forum forum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ModifyForum.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the forum to edit
            ForumModifyController controller = loader.getController();
            controller.setForumToModify(forum);

            Scene scene = new Scene(root);
            Stage stage = (Stage) discussionsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de modifier la discussion.", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDelete(Forum forum) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la discussion");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette discussion ?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                forumService.delete(forum);
                loadDiscussions(); // Refresh the list
                showAlert("Succès", "Discussion supprimée avec succès.", null);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de supprimer la discussion.", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(view);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur de navigation", "Impossible de charger la page.", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}