package Controllers;

import entite.Examen;
import entite.Question;
import entite.Reponse;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.ExamenService;
import service.QuestionService;
import service.ReponseService;

import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AdminPanelController implements Initializable {

    @FXML private Button dashboardButton;
    @FXML private Button usersButton;
    @FXML private Button forumButton;
    @FXML private Button coursesButton;
    @FXML private Button examsButton;
    @FXML private Button eventsButton;
    @FXML private Button addExamButton;
    
    @FXML private TableView<Examen> examensTable;
    @FXML private TableColumn<Examen, String> titreColumn;
    @FXML private TableColumn<Examen, String> instructeurColumn;
    @FXML private TableColumn<Examen, String> descriptionColumn;
    @FXML private TableColumn<Examen, String> dateCreationColumn;
    @FXML private TableColumn<Examen, HBox> actionsColumn;
    
    private ExamenService examenService;
    private ObservableList<Examen> examens;
    
    // ID de l'utilisateur administrateur
    private String userId;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        examens = FXCollections.observableArrayList();
        
        // Configuration des colonnes du tableau
        setupTableColumns();
        
        // Charger les examens
        loadExamens();
    }
    
    /**
     * Configure les colonnes du tableau des examens
     */
    private void setupTableColumns() {
        // Titre
        titreColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTitre()));
        
        // Instructeur - utilisez une valeur par défaut ou récupérez depuis la BD
        instructeurColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getIdUser() != null) {
                return new SimpleStringProperty("Utilisateur " + cellData.getValue().getIdUser());
            } else {
                return new SimpleStringProperty("Utilisateur inconnu");
            }
        });
        
        // Description
        descriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        
        // Date de création
        dateCreationColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDate();
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                return new SimpleStringProperty(sdf.format(date));
            }
            return new SimpleStringProperty("--");
        });
        
        // Actions
        actionsColumn.setCellValueFactory(cellData -> {
            Examen examen = cellData.getValue();
            
            // Bouton Voir
            Button viewButton = new Button();
            viewButton.getStyleClass().add("view-button");
            viewButton.setText("👁");
            viewButton.setOnAction(e -> viewExam(examen));
            
            // Bouton Modifier
            Button editButton = new Button();
            editButton.getStyleClass().add("edit-button");
            editButton.setText("✏");
            editButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white;");
            editButton.setOnAction(e -> editExam(examen));
            
            // Bouton Supprimer
            Button deleteButton = new Button();
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setText("🗑");
            deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
            deleteButton.setOnAction(e -> deleteExam(examen));
            
            // Bouton Télécharger
            Button downloadButton = new Button();
            downloadButton.getStyleClass().add("download-button");
            downloadButton.setText("⬇");
            downloadButton.setStyle("-fx-background-color: #673AB7; -fx-text-fill: white;");
            downloadButton.setOnAction(e -> downloadExam(examen));
            
            // Container pour les boutons
            HBox actionsBox = new HBox(5);
            actionsBox.getChildren().addAll(viewButton, editButton, deleteButton, downloadButton);
            
            return new SimpleObjectProperty<>(actionsBox);
        });
    }
    
    /**
     * Charge les examens depuis la base de données
     */
    private void loadExamens() {
        examens.clear();
        examens.addAll(examenService.recupererTout());
        examensTable.setItems(examens);
    }
    
    /**
     * Définit l'ID de l'utilisateur connecté
     * @param userId ID de l'utilisateur
     */
    public void setUserId(String userId) {
        this.userId = userId;
        System.out.println("ID utilisateur défini dans AdminPanelController: " + userId);
    }
    
    // Actions des boutons du menu
    
    @FXML
    private void handleDashboard() {
        showNotImplementedFeature("Tableau de bord");
    }
    
    @FXML
    private void handleUsers() {
        showNotImplementedFeature("Gestion des utilisateurs");
    }
    
    @FXML
    private void handleForums() {
        showNotImplementedFeature("Gestion des forums");
    }
    
    @FXML
    private void handleCourses() {
        showNotImplementedFeature("Gestion des cours");
    }
    
    @FXML
    private void handleExams() {
        // Déjà sur cette vue, rien à faire
    }
    
    @FXML
    private void handleEvents() {
        showNotImplementedFeature("Gestion des événements");
    }
    
    @FXML
    private void handleAddExam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ExamenView.fxml"));
            Parent root = loader.load();
            
            // Configurer le contrôleur en mode administrateur
            ExamenController controller = loader.getController();
            if (controller != null) {
                controller.setAdminMode(true);
                if (userId != null) {
                    controller.setUserId(userId);
                }
            }
            
            // Passer à la nouvelle vue
            Scene scene = addExamButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un examen");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue de création d'examen: " + e.getMessage());
        }
    }
    
    // Actions pour les examens
    
    private void viewExam(Examen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConsulterQuizView.fxml"));
            Parent root = loader.load();
            
            // Configurer le contrôleur
            ConsulterQuizController controller = loader.getController();
            if (controller != null) {
                controller.setExamenId(examen.getId());
                controller.setAdminMode(true);
                // Passer l'ID utilisateur
                if (userId != null) {
                    controller.setUserId(userId);
                }
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) examensTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Consulter Examen: " + examen.getTitre());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue de consultation d'examen: " + e.getMessage());
        }
    }
    
    private void editExam(Examen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ExamenView.fxml"));
            Parent root = loader.load();
            
            // Configurer le contrôleur en mode administrateur
            ExamenController controller = loader.getController();
            if (controller != null) {
                controller.setAdminMode(true);
                controller.setExamenToUpdate(examen);
                if (userId != null) {
                    controller.setUserId(userId);
                }
            }
            
            // Passer à la vue d'édition
            Scene scene = examensTable.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'examen: " + examen.getTitre());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue d'édition d'examen: " + e.getMessage());
        }
    }
    
    private void deleteExam(Examen examen) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer l'examen");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer l'examen: " + examen.getTitre() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = examenService.supprimer(examen.getId());
                if (success) {
                    loadExamens(); // Recharger la liste après suppression
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "L'examen a été supprimé avec succès");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'examen");
                }
            }
        });
    }
    
    private void downloadExam(Examen examen) {
        // Configurer le sélecteur de fichier
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le document de l'examen");
        fileChooser.setInitialFileName(examen.getTitre().replaceAll("[^a-zA-Z0-9]", "_") + ".docx");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Documents Word (*.docx)", "*.docx")
        );
        
        // Afficher le dialogue de sauvegarde
        File file = fileChooser.showSaveDialog(examensTable.getScene().getWindow());
        if (file == null) {
            return; // L'utilisateur a annulé
        }
        
        try {
            // Récupérer les questions et réponses
            QuestionService questionService = new QuestionService();
            ReponseService reponseService = new ReponseService();
            
            List<Question> questions = questionService.recupererParExamenId(examen.getId());
            
            // Créer un nouveau document Word
            XWPFDocument document = new XWPFDocument();
            
            // Style du titre
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("EXAMEN: " + examen.getTitre());
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.addBreak();
            
            // Informations générales
            XWPFParagraph infoParagraph = document.createParagraph();
            infoParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun infoTitleRun = infoParagraph.createRun();
            infoTitleRun.setText("Informations générales");
            infoTitleRun.setBold(true);
            infoTitleRun.setFontSize(14);
            infoTitleRun.addBreak();
            
            // Tableau des informations
            XWPFTable infoTable = document.createTable(5, 2);
            infoTable.setWidth("100%");
            
            // Formater la date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateStr = examen.getDate() != null ? dateFormat.format(examen.getDate()) : "Non définie";
            
            // Remplir le tableau d'informations
            addRowToTable(infoTable.getRow(0), "Matière:", examen.getMatiere());
            addRowToTable(infoTable.getRow(1), "Type:", examen.getType());
            addRowToTable(infoTable.getRow(2), "Date:", dateStr);
            addRowToTable(infoTable.getRow(3), "Durée:", examen.getDuree() + " minutes");
            addRowToTable(infoTable.getRow(4), "Nombre d'essais:", String.valueOf(examen.getNbrEssai()));
            
            // Description
            XWPFParagraph descParagraph = document.createParagraph();
            descParagraph.setAlignment(ParagraphAlignment.LEFT);
            descParagraph.setSpacingBefore(20);
            XWPFRun descTitleRun = descParagraph.createRun();
            descTitleRun.setText("Description:");
            descTitleRun.setBold(true);
            descTitleRun.setFontSize(14);
            descTitleRun.addBreak();
            
            XWPFRun descRun = descParagraph.createRun();
            descRun.setText(examen.getDescription());
            descRun.addBreak();
            descRun.addBreak();
            
            // Questions et réponses
            XWPFParagraph questionsParagraph = document.createParagraph();
            questionsParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun questionsTitleRun = questionsParagraph.createRun();
            questionsTitleRun.setText("Questions:");
            questionsTitleRun.setBold(true);
            questionsTitleRun.setFontSize(14);
            questionsTitleRun.addBreak();
            
            if (questions.isEmpty()) {
                XWPFParagraph noQuestionsParagraph = document.createParagraph();
                XWPFRun noQuestionsRun = noQuestionsParagraph.createRun();
                noQuestionsRun.setText("Aucune question n'a été définie pour cet examen.");
                noQuestionsRun.addBreak();
            } else {
                for (int i = 0; i < questions.size(); i++) {
                    Question question = questions.get(i);
                    
                    // Paragraphe de la question
                    XWPFParagraph questionParagraph = document.createParagraph();
                    questionParagraph.setAlignment(ParagraphAlignment.LEFT);
                    questionParagraph.setSpacingBefore(10);
                    
                    XWPFRun questionRun = questionParagraph.createRun();
                    questionRun.setText((i + 1) + ". " + question.getQuestion() + " (" + question.getNbr_points() + " points)");
                    questionRun.setBold(true);
                    questionRun.addBreak();
                    
                    // Récupérer les réponses pour cette question
                    List<Reponse> reponses = reponseService.recupererParQuestionId(question.getId());
                    
                    if (!reponses.isEmpty()) {
                        for (Reponse reponse : reponses) {
                            XWPFParagraph reponseParagraph = document.createParagraph();
                            reponseParagraph.setIndentationLeft(500); // Indentation pour les réponses
                            
                            XWPFRun reponseRun = reponseParagraph.createRun();
                            reponseRun.setText("□ " + reponse.getReponse());
                            reponseRun.addBreak();
                        }
                    } else {
                        XWPFParagraph noResponseParagraph = document.createParagraph();
                        noResponseParagraph.setIndentationLeft(500);
                        
                        XWPFRun noResponseRun = noResponseParagraph.createRun();
                        noResponseRun.setText("Aucune réponse définie.");
                        noResponseRun.addBreak();
                    }
                }
            }
            
            // Pied de page
            XWPFParagraph footerParagraph = document.createParagraph();
            footerParagraph.setAlignment(ParagraphAlignment.CENTER);
            footerParagraph.setSpacingBefore(20);
            
            XWPFRun footerRun = footerParagraph.createRun();
            footerRun.setText("Document généré automatiquement");
            footerRun.setItalic(true);
            footerRun.setColor("999999");
            footerRun.setFontSize(8);
            
            // Écrire le document dans le fichier
            try (FileOutputStream out = new FileOutputStream(file)) {
                document.write(out);
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                "Le document de l'examen a été généré avec succès et enregistré à l'emplacement suivant:\n" + 
                file.getAbsolutePath() + "\n\n" +
                "Vous pouvez l'ouvrir avec Microsoft Word ou un logiciel compatible pour le visualiser, l'imprimer " +
                "ou l'exporter en PDF.");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Erreur lors de la génération du document: " + e.getMessage());
        }
    }
    
    /**
     * Ajoute une ligne à un tableau Word
     */
    private void addRowToTable(XWPFTableRow row, String label, String value) {
        XWPFTableCell cell1 = row.getCell(0);
        XWPFParagraph paragraph1 = cell1.getParagraphs().get(0);
        XWPFRun run1 = paragraph1.createRun();
        run1.setText(label);
        run1.setBold(true);
        
        XWPFTableCell cell2 = row.getCell(1);
        XWPFParagraph paragraph2 = cell2.getParagraphs().get(0);
        XWPFRun run2 = paragraph2.createRun();
        run2.setText(value);
    }
    
    // Méthodes utilitaires
    
    private void showNotImplementedFeature(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité non implémentée");
        alert.setHeaderText(null);
        alert.setContentText("La fonctionnalité '" + feature + "' n'est pas encore implémentée.");
        alert.showAndWait();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 