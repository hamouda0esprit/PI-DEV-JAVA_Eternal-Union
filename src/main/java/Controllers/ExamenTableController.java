package Controllers;

import entite.Examen;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.ExamenService;
import service.ExcelExportService;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class ExamenTableController implements Initializable {
    
    @FXML private Button accueilButton;
    @FXML private Button creerExamenButton;
    @FXML private Button performanceButton;
    @FXML private Button exportExcelButton;
    
    @FXML private TableView<Examen> examenTable;
    @FXML private TableColumn<Examen, String> matiereColumn;
    @FXML private TableColumn<Examen, String> descriptionColumn;
    @FXML private TableColumn<Examen, Integer> dureeColumn;
    @FXML private TableColumn<Examen, String> dateColumn;
    @FXML private TableColumn<Examen, String> typeColumn;
    @FXML private TableColumn<Examen, Integer> nombreEssaisColumn;
    @FXML private TableColumn<Examen, Void> actionsColumn;
    
    private ExamenService examenService;
    private ObservableList<Examen> examenList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        examenList = FXCollections.observableArrayList();
        
        // Configurer les colonnes
        matiereColumn.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        
        dateColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDate();
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                return new SimpleStringProperty(sdf.format(date));
            }
            return new SimpleStringProperty("");
        });
        
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        nombreEssaisColumn.setCellValueFactory(new PropertyValueFactory<>("nbrEssai"));
        
        // Configurer la colonne des actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final Button addQuestionsButton = new Button("Ajouter Questions");
            
            private final HBox pane = new HBox(5, updateButton, deleteButton, addQuestionsButton);
            
            {
                // Ajouter les classes CSS aux boutons
                updateButton.getStyleClass().add("update-button");
                deleteButton.getStyleClass().add("delete-button");
                addQuestionsButton.getStyleClass().add("add-questions-button");
                
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                
                // Ajouter les actions
                updateButton.setOnAction(event -> {
                    Examen examen = getTableView().getItems().get(getIndex());
                    handleUpdate(examen);
                });
                
                deleteButton.setOnAction(event -> {
                    Examen examen = getTableView().getItems().get(getIndex());
                    handleDelete(examen);
                });
                
                addQuestionsButton.setOnAction(event -> {
                    Examen examen = getTableView().getItems().get(getIndex());
                    handleAddQuestions(examen);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        // Charger les données
        loadExamens();
    }
    
    private void loadExamens() {
        examenList.clear();
        examenList.addAll(examenService.recupererTout());
        examenTable.setItems(examenList);
    }
    
    private void handleUpdate(Examen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ExamenView.fxml"));
            Parent root = loader.load();
            
            ExamenController controller = loader.getController();
            controller.setExamenToUpdate(examen);
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) examenTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue de modification");
        }
    }
    
    private void handleDelete(Examen examen) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'examen");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet examen ?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (examenService.supprimer(examen.getId())) {
                loadExamens();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Examen supprimé avec succès");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'examen");
            }
        }
    }
    
    private void handleAddQuestions(Examen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuestionView.fxml"));
            Parent root = loader.load();
            
            QuestionController controller = loader.getController();
            controller.setExamen(examen);
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) examenTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue d'ajout de questions");
        }
    }
    
    @FXML
    private void handleAccueil() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AccueilProfesseur.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) accueilButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers la page d'accueil");
        }
    }
    
    @FXML
    private void handleCreerExamen() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/ExamenView.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) creerExamenButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue de création d'examen");
        }
    }
    
    @FXML
    private void handlePerformance() {
        // Navigation vers la page de performance
        showAlert(Alert.AlertType.INFORMATION, "Information", "Navigation vers la page de performance à implémenter");
    }
    
    @FXML
    private void handleExportExcel() {
        ExcelExportService exportService = new ExcelExportService();
        boolean success = exportService.exportExamensToExcel(examenList, (Stage) exportExcelButton.getScene().getWindow());
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                "Les examens ont été exportés avec succès au format Excel.\n" +
                "Vous pouvez ouvrir ce fichier directement dans Microsoft Excel.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Information", "L'exportation des examens a été annulée ou a échoué.");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 