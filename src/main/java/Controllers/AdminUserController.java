package Controllers;

import entite.User;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import service.UserService;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AdminUserController implements Initializable {

    @FXML
    private TableView<User> usersTableView;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> typeColumn;

    @FXML
    private TableColumn<User, Date> dobColumn;

    @FXML
    private TableColumn<User, Integer> phoneColumn;

    @FXML
    private TableColumn<User, Integer> scoreColumn;

    @FXML
    private TableColumn<User, String> verifiedColumn;

    @FXML
    private TableColumn<User, Void> actionColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button deleteSelectedButton;

    @FXML
    private Pagination usersPagination;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label studentCountLabel;

    @FXML
    private Label teacherCountLabel;

    private UserService userService;
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;

    private static final int ITEMS_PER_PAGE = 10;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = new UserService();
        setupTableColumns();
        loadUsers();
        setupSearch();
        setupPagination();
        setupButtonActions();
        updateStats();
    }

    private void setupTableColumns() {
        // Set up the columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        // Custom cell factory for date formatting
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("date_of_birth"));
        dobColumn.setCellFactory(column -> new TableCell<User, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });

        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Custom cell factory for boolean values (verified)
        verifiedColumn.setCellValueFactory(new PropertyValueFactory<>("verified"));
        verifiedColumn.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("1".equals(item) ? "Oui" : "Non");
                }
            }
        });

        // Setup action column with Delete button
        actionColumn.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<User, Void>, TableCell<User, Void>> createActionCellFactory() {
        return param -> new TableCell<User, Void>() {
            private final Button deleteButton = new Button("Supprimer");

            {
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(deleteButton);
                    hbox.setAlignment(Pos.CENTER);
                    hbox.setSpacing(5);
                    setGraphic(hbox);
                }
            }
        };
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            usersList.clear();
            usersList.addAll(users);

            filteredUsers = new FilteredList<>(usersList, p -> true);
            usersTableView.setItems(filteredUsers);

            updateTableView();
            updateStats();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger les utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchButton.setOnAction(event -> {
            String searchText = searchField.getText().toLowerCase().trim();
            if (searchText.isEmpty()) {
                filteredUsers.setPredicate(user -> true);
            } else {
                filteredUsers.setPredicate(user ->
                        user.getName().toLowerCase().contains(searchText) ||
                                user.getEmail().toLowerCase().contains(searchText) ||
                                user.getType().toLowerCase().contains(searchText)
                );
            }
            updateTableView();
        });

        // Also enable search on typing
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                filteredUsers.setPredicate(user -> true);
                updateTableView();
            }
        });
    }

    private void setupPagination() {
        usersPagination.setPageCount(calculatePageCount());
        usersPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
                updateTableView());
    }

    private int calculatePageCount() {
        if (filteredUsers == null || filteredUsers.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) filteredUsers.size() / ITEMS_PER_PAGE);
    }

    private void updateTableView() {
        int currentPage = usersPagination.getCurrentPageIndex();
        int fromIndex = currentPage * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredUsers.size());

        if (filteredUsers.isEmpty()) {
            usersTableView.setItems(FXCollections.observableArrayList());
            usersPagination.setPageCount(1);
        } else {
            ObservableList<User> pageItems = FXCollections.observableArrayList(
                    filteredUsers.subList(fromIndex, toIndex));
            usersTableView.setItems(pageItems);
            usersPagination.setPageCount(calculatePageCount());
        }
    }

    private void setupButtonActions() {
        refreshButton.setOnAction(event -> loadUsers());

        deleteSelectedButton.setOnAction(event -> {
            User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                handleDeleteUser(selectedUser);
            } else {
                showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                        "Veuillez sélectionner un utilisateur à supprimer.");
            }
        });
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'utilisateur " + user.getName());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur? Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deleteUser(user.getId());
                usersList.remove(user);
                updateTableView();
                updateStats();

                showAlert(Alert.AlertType.INFORMATION, "Suppression réussie",
                        "L'utilisateur a été supprimé avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                        "Impossible de supprimer l'utilisateur: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateStats() {
        totalUsersLabel.setText(String.valueOf(usersList.size()));

        long studentCount = usersList.stream()
                .filter(user -> "STUDENT".equalsIgnoreCase(user.getType()))
                .count();
        studentCountLabel.setText(String.valueOf(studentCount));

        long teacherCount = usersList.stream()
                .filter(user -> "TEACHER".equalsIgnoreCase(user.getType()))
                .count();
        teacherCountLabel.setText(String.valueOf(teacherCount));
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 