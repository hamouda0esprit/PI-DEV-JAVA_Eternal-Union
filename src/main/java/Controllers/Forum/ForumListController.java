package Controllers.Forum;

import entite.Forum;
import entite.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.ForumService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ForumListController {

    @FXML
    private Button AddForumButton;

    @FXML
    void HandleAddForum(ActionEvent event) {
        try {
            Parent ajoutForumView = FXMLLoader.load(getClass().getResource("/com/example/loe/AddForum.fxml"));
            Scene scene = new Scene(ajoutForumView );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Button ViewForumButton;


    @FXML
    void HandleViewForum(ActionEvent event) {
        try {
            Parent ajoutForumView = FXMLLoader.load(getClass().getResource("/com/example/loe/ListForum.fxml"));
            Scene scene = new Scene(ajoutForumView );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private VBox forumPostsContainer; // This will hold all our forum posts

    @FXML
    void initialize() throws SQLException {
        ForumService service = new ForumService();
        List<Forum> forums = service.readAll();

        UserService userService = new UserService();

        for (Forum forum : forums) {
            // Load the complete user data for this forum
            if (forum.getUser() != null && forum.getUser().getId() > 0) {
                // Assuming you have a readById method in UserService
                User completeUser = userService.readById(forum.getUser().getId());
                if (completeUser != null) {
                    forum.setUser(completeUser);
                }
            }

            // Create a VBox for each forum post (ticket)
            VBox ticket = createForumTicket(forum);
            forumPostsContainer.getChildren().add(ticket);
        }
    }

    private VBox createForumTicket(Forum forum) {
        // Create a VBox container for the forum post
        VBox ticket = new VBox();
        ticket.setSpacing(10);
        ticket.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 15;");
        ticket.setPrefWidth(600);

        // Create header with user info and date
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setSpacing(10);

        // User image
        ImageView userImage = new ImageView();
        userImage.setFitHeight(40);
        userImage.setFitWidth(40);
        userImage.setPreserveRatio(true);

        // Set the user image if available
        if (forum.getUser() != null && forum.getUser().getImg() != null) {
            try {
                Image img = new Image(forum.getUser().getImg());
                userImage.setImage(img);
            } catch (Exception e) {
                System.out.println("Could not load user image: " + e.getMessage());
            }
        }

        // User info
        VBox userInfo = new VBox();
        // Username handling with better null checks
        Label username = new Label((forum.getUser() != null && forum.getUser().getName() != null) ?
                forum.getUser().getName() : "Unknown user");

        // Same for images
        if (forum.getUser() != null && forum.getUser().getImg() != null && !forum.getUser().getImg().isEmpty()) {
            try {
                Image img = new Image(forum.getUser().getImg());
                userImage.setImage(img);
            } catch (Exception e) {
                System.out.println("Could not load user image: " + e.getMessage());
                // Set a default image here
            }
        }
        username.setPrefHeight(40.0);
        username.setPrefWidth(100.0);
        username.setStyle("-fx-font-weight: bold;-fx-text-fill: black;");
        userInfo.getChildren().add(username);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Date
        Label datetime = new Label(forum.getDate_time() != null ? forum.getDate_time().toString() : "No date");
        datetime.setStyle("-fx-text-fill: #888888;-fx-text-fill: black;");

        header.getChildren().addAll(userImage, userInfo, spacer, datetime);

        // Title
        Label title = new Label(forum.getTitle());
        title.setStyle("-fx-font-weight: bold;-fx-text-fill: black;");

        // Description
        Label description = new Label(forum.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-font-weight: bold;-fx-text-fill: black;");

        // Media (if available)
        if (forum.getMedia() != null && !forum.getMedia().isEmpty()) {
            ImageView mediaView = new ImageView();
            mediaView.setFitWidth(600);
            mediaView.setPreserveRatio(true);
            try {
                if ("image".equals(forum.getType_media())) {
                    Image mediaImg = new Image(forum.getMedia());
                    mediaView.setImage(mediaImg);
                    ticket.getChildren().add(mediaView);
                }
            } catch (Exception e) {
                System.out.println("Could not load media: " + e.getMessage());
            }
        }

        // Discuss button
        Button discussButton = new Button();
        discussButton.setStyle("-fx-background-color: transparent;");

        HBox discussButtonContent = new HBox();
        discussButtonContent.setAlignment(javafx.geometry.Pos.CENTER);
        discussButtonContent.setSpacing(5);

        ImageView commentIcon = new ImageView();
        commentIcon.setFitHeight(16);
        commentIcon.setFitWidth(16);

        Label discussLabel = new Label("Discuter");
        discussLabel.setStyle("-fx-text-fill: #54a2e0;-fx-text-fill: black;");

        discussButtonContent.getChildren().addAll(commentIcon, discussLabel);
        discussButton.setGraphic(discussButtonContent);

        // Add event handler to discuss button
        discussButton.setOnAction(event -> {
            // Here you can add code to open the discussion for this specific forum post
            System.out.println("Open discussion for forum #" + forum.getId());
        });

        // Add all components to the ticket
        ticket.getChildren().addAll(header, title, description);

        // Add discuss button at the bottom
        ticket.getChildren().add(discussButton);

        return ticket;
    }
}