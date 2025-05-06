package Controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Cours;
import services.CoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


public class AdminCoursController {

    @FXML
    private TableView<Cours> eventTable;

    @FXML
    private TableColumn<Cours, Integer> titleColumn;

    @FXML
    private TableColumn<Cours, Integer> dateColumn;

    @FXML
    private TableColumn<Cours, String> timeColumn;

    @FXML
    private TableColumn<Cours, String> locationColumn;

    @FXML
    private TableColumn<Cours, String> descriptionColumn;

    @FXML
    private TableColumn<Cours, Integer> descriptionColumn1;

    @FXML
    private TableColumn<Cours, String> descriptionColumn11;

    @FXML
    private Button addButton;

    @FXML
    private TableColumn<Cours, Void> actionsColumn;

    @FXML
    private Label statusLabel;

    private final CoursService coursService = new CoursService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        try {
            List<Cours> coursList = coursService.recuperer();
            eventTable.getItems().setAll(coursList);

            // Remplissage des colonnes
            titleColumn.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));

            dateColumn.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getUserId()));

            timeColumn.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getTitle()));

            locationColumn.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getImage()));

            descriptionColumn.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getSubject()));

            descriptionColumn1.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getRate()));

            descriptionColumn11.setCellValueFactory(cellData -> {
                if (cellData.getValue().getLastUpdate() != null) {
                    String formattedDate = cellData.getValue().getLastUpdate()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    return new ReadOnlyObjectWrapper<>(formattedDate);
                } else {
                    return new ReadOnlyObjectWrapper<>("N/A");
                }
            });

            // Actions column
            actionsColumn.setCellFactory(param -> new TableCell<>() {
                private final Button editButton = new Button("Modifier");
                private final Button deleteButton = new Button("Supprimer");
                private final HBox buttons = new HBox(10, editButton, deleteButton);

                {
                    editButton.getStyleClass().add("edit-button");
                    deleteButton.getStyleClass().add("delete-button");

                    deleteButton.setOnAction(e -> {
                        Cours cours = getTableView().getItems().get(getIndex());
                        try {
                            coursService.sipprimer(cours.getId());
                            loadData();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });

                    editButton.setOnAction(e -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCoursAdmin.fxml"));
                            Parent root = loader.load();

                            // Optionnel : passer l'objet cours au contrôleur
                            ModifierCoursAdminController controller = loader.getController();
                            controller.setCours(getTableView().getItems().get(getIndex()));

                            Stage stage = new Stage();
                            stage.setScene(new Scene(root));
                            stage.setTitle("Modifier Cours");
                            stage.show();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });

                    addButton.setOnAction(e -> showAddCourseDialog());

                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            });

            statusLabel.setText("Total Cours: " + coursList.size());

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Erreur lors du chargement des cours.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAddCourseDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCoursAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un nouveau cours");
            stage.showAndWait();

            // Refresh table after adding
            loadData();
        } catch (IOException e) {

            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout.");
            e.printStackTrace();
        }
    }
}
