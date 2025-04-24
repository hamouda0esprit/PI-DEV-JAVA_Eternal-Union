package Controllers.Forum;

import entite.Forum;
import entite.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import service.ForumService;
import service.UserService;
import utils.FileUtils;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ForumListController {

    @FXML
    private Button AddForumButton;
    @FXML
    private Button ViewForumButton;
    @FXML
    private VBox forumPostsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;
    @FXML
    private Label pageInfoLabel;

    private List<Forum> allForums;
    private List<Forum> filteredForums;
    private int currentPage = 1;
    private static final int ITEMS_PER_PAGE = 5;

    @FXML
    void initialize() throws SQLException {
        loadForums();
        updatePaginationControls();
    }

    private void loadForums() throws SQLException {
        ForumService service = new ForumService();
        allForums = service.readAll();
        filteredForums = allForums;

        UserService userService = new UserService();

        for (Forum forum : allForums) {
            if (forum.getUser() != null && forum.getUser().getId() > 0) {
                User completeUser = userService.readById(forum.getUser().getId());
                if (completeUser != null) {
                    forum.setUser(completeUser);
                }
            }
        }

        displayCurrentPage();
    }

    @FXML
    void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            filteredForums = allForums;
        } else {
            filteredForums = allForums.stream()
                    .filter(forum -> forum.getTitle().toLowerCase().contains(searchText) ||
                            forum.getDescription().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }
        currentPage = 1;
        displayCurrentPage();
        updatePaginationControls();
    }

    @FXML
    void handlePreviousPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            displayCurrentPage();
            updatePaginationControls();
        }
    }

    @FXML
    void handleNextPage(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) filteredForums.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages) {
            currentPage++;
            displayCurrentPage();
            updatePaginationControls();
        }
    }

    private void displayCurrentPage() {
        forumPostsContainer.getChildren().clear();
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredForums.size());

        for (int i = startIndex; i < endIndex; i++) {
            VBox ticket = createForumTicket(filteredForums.get(i));
            forumPostsContainer.getChildren().add(ticket);
        }
    }

    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil((double) filteredForums.size() / ITEMS_PER_PAGE);
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    @FXML
    void HandleAddForum(ActionEvent event) {
        try {
            Parent ajoutForumView = FXMLLoader.load(getClass().getResource("/view/AddForum.fxml"));
            Scene scene = new Scene(ajoutForumView );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void HandleViewForum(ActionEvent event) {
        try {
            Parent ajoutForumView = FXMLLoader.load(getClass().getResource("/view/ListForum.fxml"));
            Scene scene = new Scene(ajoutForumView );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
                Image img = new Image(getClass().getResourceAsStream("/images/ForumUser.jpg"));
                userImage.setImage(img);
            } catch (Exception e) {
                System.out.println("Could not load user image: " + e.getMessage());
            }
        }

        // User info
        VBox userInfo = new VBox();
        Label username = new Label((forum.getUser() != null && forum.getUser().getName() != null) ?
                forum.getUser().getName() : "Unknown user");
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

        // Add header, title, and description first
        ticket.getChildren().addAll(header, title, description);

        if (forum.getMedia() != null && !forum.getMedia().isEmpty()) {
            try {
                if ("image".equals(forum.getType_media())) {
                    ImageView mediaView = new ImageView();
                    mediaView.setFitWidth(600);
                    mediaView.setPreserveRatio(true);

                    String fileUrl = FileUtils.getForumMediaUrl(forum.getMedia());
                    Image mediaImg = new Image(fileUrl);
                    mediaView.setImage(mediaImg);

                    HBox mediaContainer = new HBox(mediaView);
                    mediaContainer.setAlignment(Pos.CENTER);
                    mediaContainer.setPadding(new Insets(10, 0, 10, 0));

                    ticket.getChildren().add(mediaContainer);
                } else if ("video".equals(forum.getType_media())) {
                    MediaView mediaView = new MediaView();
                    mediaView.setFitWidth(600);
                    mediaView.setPreserveRatio(true);

                    String fileUrl = FileUtils.getForumMediaUrl(forum.getMedia());
                    Media media = new Media(fileUrl);
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaPlayer.setAutoPlay(false);
                    mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

                    // Add video controls
                    HBox controls = new HBox();
                    controls.setAlignment(Pos.CENTER);
                    controls.setSpacing(10);
                    controls.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-background-radius: 5;");

                    // Create styled buttons
                    Button playButton = new Button("▶");
                    playButton.setStyle("-fx-background-color: #42a5f5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 15; -fx-font-weight: bold;");

                    Button pauseButton = new Button("⏸");
                    pauseButton.setStyle("-fx-background-color: #42a5f5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 15; -fx-font-weight: bold;");

                    Button stopButton = new Button("⏹");
                    stopButton.setStyle("-fx-background-color: #42a5f5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 15; -fx-font-weight: bold;");

                    // Add actions
                    playButton.setOnAction(e -> mediaPlayer.play());
                    pauseButton.setOnAction(e -> mediaPlayer.pause());
                    stopButton.setOnAction(e -> mediaPlayer.stop());

                    // Add buttons to controls
                    controls.getChildren().addAll(playButton, pauseButton, stopButton);

                    VBox videoContainer = new VBox(mediaView, controls);
                    videoContainer.setAlignment(Pos.CENTER);
                    videoContainer.setPadding(new Insets(10, 0, 10, 0));

                    ticket.getChildren().add(videoContainer);
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

        Label commentIcon = new Label("🗨️");
        commentIcon.setStyle("-fx-text-fill: black;");

        Label discussLabel = new Label("Discuter");
        discussLabel.setStyle("-fx-text-fill: #54a2e0;-fx-text-fill: black;");

        discussButtonContent.getChildren().addAll(commentIcon, discussLabel);
        discussButton.setGraphic(discussButtonContent);

        // Add event handler to discuss button
        discussButton.setOnAction(event -> {
            try {
                // Load the FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ForumDiscussion.fxml"));
                Parent discussionView = loader.load();

                // Get the controller and pass the forum data
                ForumDiscussionController controller = loader.getController();
                controller.setForum(forum);

                // Set the new scene
                Scene scene = new Scene(discussionView);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                System.err.println("Error loading discussion view: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Add discuss button at the bottom
        ticket.getChildren().add(discussButton);

        return ticket;
    }
}