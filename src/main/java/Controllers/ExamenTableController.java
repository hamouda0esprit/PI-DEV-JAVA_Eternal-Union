package Controllers;

import entite.Examen;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.ExamenService;
import service.ExcelExportService;
import entite.Question;
import entite.Reponse;
import service.QuestionService;
import service.ReponseService;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    @FXML private TableColumn<Examen, HBox> actionsColumn;
    
    // Composants pour la recherche et le tri
    @FXML private TextField searchField;
    @FXML private ComboBox<String> matiereFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private DatePicker dateFilter;
    
    private ExamenService examenService;
    private QuestionService questionService;
    private ReponseService reponseService;
    private ObservableList<Examen> examens;
    private FilteredList<Examen> filteredExamens;
    
    // ID utilisateur
    private Integer userId;
    private boolean adminMode = false;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        questionService = new QuestionService();
        reponseService = new ReponseService();
        examens = FXCollections.observableArrayList();
        
        // Configuration des colonnes
        setupTableColumns();
        
        // Initialisation des filtres
        initializeFilters();
        
        // Configuration de la recherche et du tri
        setupSearchAndSort();
        
        // Chargement initial des examens
        loadExamens();
        
        // Affichage du compte des examens
        System.out.println("Nombre d'examens chargés: " + examens.size());
    }
    
    /**
     * Configure les colonnes du tableau
     */
    private void setupTableColumns() {
        matiereColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMatiere()));
        matiereColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        
        descriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        descriptionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        
        dureeColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDuree()));
        dureeColumn.setStyle("-fx-alignment: CENTER;");
        
        // Configuration de la colonne date avec tri personnalisé
        dateColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDate();
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                return new SimpleStringProperty(sdf.format(date));
            }
            return new SimpleStringProperty("--");
        });
        dateColumn.setStyle("-fx-alignment: CENTER;");
        
        // Définition d'un comparateur personnalisé pour trier les dates correctement
        dateColumn.setComparator((date1, date2) -> {
            if (date1.equals("--")) return 1;
            if (date2.equals("--")) return -1;
            
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date d1 = sdf.parse(date1);
                Date d2 = sdf.parse(date2);
                return d1.compareTo(d2);
            } catch (ParseException e) {
                return date1.compareTo(date2);
            }
        });
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType()));
        typeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        
        // Configuration de la colonne nombre d'essais avec centrage
        nombreEssaisColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getNbrEssai()));
        nombreEssaisColumn.setStyle("-fx-alignment: CENTER;");
        
        // Configuration des boutons d'action avec centrage
        actionsColumn.setStyle("-fx-alignment: CENTER;");
        actionsColumn.setCellFactory(column -> new TableCell<Examen, HBox>() {
            private final Button viewButton = new Button("👁");
            private final Button editButton = new Button("✏");
            private final Button deleteButton = new Button("🗑");
            private final Button addQuestionsButton = new Button("➕");
            
            private final HBox pane = new HBox(5);
            
            {
                viewButton.getStyleClass().add("view-button");
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                addQuestionsButton.getStyleClass().add("add-questions-button");
                
                // Ajouter un tooltip pour expliquer les actions
                viewButton.setTooltip(new Tooltip("Voir le quiz et ses questions/réponses"));
                editButton.setTooltip(new Tooltip("Modifier l'examen"));
                deleteButton.setTooltip(new Tooltip("Supprimer l'examen"));
                addQuestionsButton.setTooltip(new Tooltip("Ajouter des questions"));
                
                pane.getChildren().addAll(viewButton, editButton, deleteButton, addQuestionsButton);
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                
                viewButton.setOnAction(e -> {
                    Examen examen = getTableView().getItems().get(getIndex());
                    viewExam(examen);
                });
                
                editButton.setOnAction(e -> {
                    Examen examen = getTableView().getItems().get(getIndex());
                    editExam(examen);
                });
                
                deleteButton.setOnAction(e -> {
                    Examen examen = getTableView().getItems().get(getIndex());
                    deleteExam(examen);
                });
                
                addQuestionsButton.setOnAction(e -> {
                    Examen examen = getTableView().getItems().get(getIndex());
                    addQuestionsToExam(examen);
                });
            }
            
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        // Activation du tri sur les colonnes
        matiereColumn.setSortable(true);
        dureeColumn.setSortable(true);
        dateColumn.setSortable(true);
        typeColumn.setSortable(true);
        nombreEssaisColumn.setSortable(true);
    }
    
    /**
     * Initialise les filtres (matières et types)
     */
    private void initializeFilters() {
        // Chargement des matières distinctes
        List<String> matieres = new ArrayList<>();
        matieres.add("Toutes les matières");
        matieres.addAll(examenService.recupererMatieresDistinctes());
        matiereFilter.setItems(FXCollections.observableArrayList(matieres));
        matiereFilter.setValue("Toutes les matières");
        
        // Chargement des types distincts
        List<String> types = new ArrayList<>();
        types.add("Tous les types");
        types.addAll(examenService.recupererTypesDistincts());
        typeFilter.setItems(FXCollections.observableArrayList(types));
        typeFilter.setValue("Tous les types");
    }
    
    /**
     * Configure la recherche et le tri
     */
    private void setupSearchAndSort() {
        // Configuration de la liste filtrée
        filteredExamens = new FilteredList<>(examens, p -> true);
        
        // Action du bouton de recherche
        searchButton.setOnAction(e -> applyFilters());
        
        // Action du bouton de réinitialisation
        resetButton.setOnAction(e -> resetFilters());
        
        // Configuration de la liste triée basée sur la liste filtrée
        SortedList<Examen> sortedExamens = new SortedList<>(filteredExamens);
        sortedExamens.comparatorProperty().bind(examenTable.comparatorProperty());
        
        // Attribution de la liste triée au TableView
        examenTable.setItems(sortedExamens);
    }
    
    /**
     * Applique les filtres de recherche
     */
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedMatiere = matiereFilter.getValue();
        String selectedType = typeFilter.getValue();
        java.time.LocalDate selectedDate = dateFilter.getValue();
        
        filteredExamens.setPredicate(examen -> {
            boolean matchesSearchText = searchText.isEmpty() ||
                    (examen.getTitre() != null && examen.getTitre().toLowerCase().contains(searchText)) ||
                    (examen.getDescription() != null && examen.getDescription().toLowerCase().contains(searchText)) ||
                    (examen.getMatiere() != null && examen.getMatiere().toLowerCase().contains(searchText));
            
            boolean matchesMatiere = "Toutes les matières".equals(selectedMatiere) ||
                    (examen.getMatiere() != null && examen.getMatiere().equals(selectedMatiere));
            
            boolean matchesType = "Tous les types".equals(selectedType) ||
                    (examen.getType() != null && examen.getType().equals(selectedType));
            
            // Vérification de la date si une date est sélectionnée
            boolean matchesDate = true;
            if (selectedDate != null && examen.getDate() != null) {
                java.time.LocalDate examenLocalDate = examen.getDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
                
                matchesDate = examenLocalDate.equals(selectedDate);
            }
            
            return matchesSearchText && matchesMatiere && matchesType && matchesDate;
        });
    }
    
    /**
     * Réinitialise tous les filtres
     */
    private void resetFilters() {
        searchField.clear();
        matiereFilter.setValue("Toutes les matières");
        typeFilter.setValue("Tous les types");
        dateFilter.setValue(null);
        
        filteredExamens.setPredicate(p -> true);
    }
    
    /**
     * Charge les examens depuis la base de données
     */
    private void loadExamens() {
        examens.clear();
        
        if (adminMode) {
            // En mode admin, charger tous les examens
            examens.addAll(examenService.recupererTout());
        } else if (userId != null) {
            // Sinon, charger uniquement les examens de l'utilisateur
            examens.addAll(examenService.rechercherExamens(null, userId, null, null));
        } else {
            // Si aucun utilisateur n'est défini, charger tous les examens (pour le test)
            examens.addAll(examenService.recupererTout());
        }
    }
    
    /**
     * Définit le mode administrateur
     * @param adminMode true si l'utilisateur est un administrateur
     */
    public void setAdminMode(boolean adminMode) {
        this.adminMode = adminMode;
    }
    
    /**
     * Définit l'ID de l'utilisateur actuel
     * @param userId ID de l'utilisateur
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
        // Rechargement des examens après définition de l'utilisateur
        if (examens != null) {
            loadExamens();
        }
    }
    
    /**
     * Définit l'ID de l'utilisateur actuel (version String)
     * @param userId ID de l'utilisateur en format String
     */
    public void setUserId(String userId) {
        try {
            this.userId = Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            System.err.println("ID utilisateur invalide: " + userId);
        }
        
        // Rechargement des examens après définition de l'utilisateur
        if (examens != null) {
            loadExamens();
        }
    }
    
    // Méthodes d'action
    
    /**
     * Affiche les détails d'un examen avec ses questions et réponses
     * @param examen L'examen à consulter
     */
    private void viewExam(Examen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConsulterQuizView.fxml"));
            Parent root = loader.load();
            
            ConsulterQuizController controller = loader.getController();
            controller.setExamenId(examen.getId());
            
            // Configurer le mode de consultation (pour afficher toutes les réponses)
            if (adminMode) {
                controller.setAdminMode(true);
            }
            
            if (userId != null) {
                controller.setUserId(userId.toString());
            }
            
            // Pré-charger les questions et réponses pour un affichage plus rapide
            preloadQuestionsAndResponses(controller, examen.getId());
            
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Consulter Quiz: " + examen.getTitre());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue de consultation du quiz");
        }
    }
    
    /**
     * Précharge les questions et réponses pour un affichage plus rapide
     * @param controller Le contrôleur de consultation du quiz
     * @param examenId L'ID de l'examen
     */
    private void preloadQuestionsAndResponses(ConsulterQuizController controller, int examenId) {
        // Récupérer les questions de l'examen
        List<Question> questions = questionService.recupererParExamen(examenId);
        
        // Récupérer les réponses pour chaque question
        List<Reponse> allResponses = new ArrayList<>();
        for (Question question : questions) {
            List<Reponse> reponses = reponseService.recupererParQuestion(question.getId());
            allResponses.addAll(reponses);
        }
        
        // Activer le mode démo avec les questions et réponses préchargées, même si l'une des listes est vide
        // C'est nécessaire pour les quiz générés par IA qui pourraient ne pas avoir de questions encore
        ConsulterQuizController.setDemoQuestionsAndResponses(questions, allResponses);
        ConsulterQuizController.setUseDemo(true);
        
        // Log pour débogage
        System.out.println("Préchargement : " + questions.size() + " questions et " + allResponses.size() + " réponses pour l'examen #" + examenId);
    }
    
    private void editExam(Examen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ExamenView.fxml"));
            Parent root = loader.load();
            
            ExamenController controller = loader.getController();
            controller.setExamenToUpdate(examen);
            if (userId != null) {
                controller.setUserId(userId.toString());
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) examenTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue de modification de l'examen");
        }
    }
    
    private void deleteExam(Examen examen) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer l'examen");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer l'examen: " + examen.getTitre() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (examenService.supprimer(examen.getId())) {
                    loadExamens();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "L'examen a été supprimé avec succès");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'examen");
                }
            }
        });
    }
    
    private void addQuestionsToExam(Examen examen) {
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
    
    // Méthodes de navigation
    
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue d'accueil");
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
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité à implémenter");
    }
    
    @FXML
    private void handleExportExcel() {
        ExcelExportService exportService = new ExcelExportService();
        boolean success = exportService.exportExamensToExcel(examens, (Stage) exportExcelButton.getScene().getWindow());
        
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