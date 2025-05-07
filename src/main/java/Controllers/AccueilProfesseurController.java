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
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import service.ExamenService;
import service.QuestionService;
import service.ReponseService;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AccueilProfesseurController implements Initializable {

    @FXML private Button creerExamenButton;
    @FXML private Button consulterExamenButton;
    @FXML private Button performanceButton;
    @FXML private Button voirStatistiquesButton;
    @FXML private Button ouvrirCalendrierButton;
    
    @FXML private TableView<Examen> examensTable;
    @FXML private TableColumn<Examen, Pane> statutColumn;
    @FXML private TableColumn<Examen, String> titreColumn;
    @FXML private TableColumn<Examen, String> dateColumn;
    @FXML private TableColumn<Examen, Button> actionsColumn;
    
    private ExamenService examenService;
    private QuestionService questionService;
    private ReponseService reponseService;
    private ObservableList<Examen> examens;
    
    // Variable pour stocker l'ID de l'utilisateur
    private String userId;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        questionService = new QuestionService();
        reponseService = new ReponseService();
        examens = FXCollections.observableArrayList();
        
        // Configuration des colonnes
        statutColumn.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getType();
            Date date = cellData.getValue().getDate();
            String statut = determinerStatut(type, date);
            
            Label label = new Label(statut);
            label.setTextFill(Color.WHITE);
            
            Pane pane = new Pane();
            pane.getChildren().add(label);
            pane.setPrefHeight(25);
            pane.setPrefWidth(80);
            pane.setStyle("-fx-background-radius: 15; -fx-alignment: center;");
            
            switch (statut) {
                case "Terminé":
                    pane.setStyle(pane.getStyle() + "-fx-background-color: #2ecc71;");
                    break;
                case "Imminent":
                    pane.setStyle(pane.getStyle() + "-fx-background-color: #e74c3c;");
                    break;
                case "Planifié":
                    pane.setStyle(pane.getStyle() + "-fx-background-color: #f39c12;");
                    break;
                default:
                    pane.setStyle(pane.getStyle() + "-fx-background-color: #95a5a6;");
                    break;
            }
            
            return new SimpleObjectProperty<>(pane);
        });
        
        titreColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTitre()));
        
        dateColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDate();
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                return new SimpleStringProperty(sdf.format(date));
            }
            return new SimpleStringProperty("");
        });
        
        actionsColumn.setCellValueFactory(cellData -> {
            Button viewButton = new Button("Voir");
            viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            viewButton.setTooltip(new Tooltip("Voir le quiz et ses questions/réponses"));
            viewButton.setOnAction(event -> voirExamen(cellData.getValue()));
            
            return new SimpleObjectProperty<>(viewButton);
        });
        
        // Chargement des examens
        loadExamens();
    }
    
    private String determinerStatut(String type, Date date) {
        if (date == null) return "Planifié";
        
        Date currentDate = new Date();
        // Si la date est passée de plus de 2 jours
        if (date.getTime() < currentDate.getTime() - 2 * 24 * 60 * 60 * 1000) {
            return "Terminé";
        }
        // Si la date est à venir dans les 2 prochains jours
        else if (date.getTime() > currentDate.getTime() && 
                date.getTime() < currentDate.getTime() + 2 * 24 * 60 * 60 * 1000) {
            return "Imminent";
        }
        // Si la date est future mais pas imminente
        else if (date.getTime() > currentDate.getTime()) {
            return "Planifié";
        }
        // Date passée mais récente (moins de 2 jours)
        else {
            return "Terminé";
        }
    }
    
    private void loadExamens() {
        examens.clear();
        examens.addAll(examenService.recupererTout());
        examensTable.setItems(examens);
    }
    
    private void voirExamen(Examen examen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConsulterQuizView.fxml"));
            Parent root = loader.load();
            
            ConsulterQuizController controller = loader.getController();
            controller.setExamenId(examen.getId());
            
            // Configurer le mode professeur (pas le mode admin)
            controller.setAdminMode(false);
            
            if (userId != null) {
                controller.setUserId(userId);
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
    private void handleConsulterExamen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ExamenTableView.fxml"));
            Parent root = loader.load();
            
            // Passer l'ID utilisateur au contrôleur de la table des examens
            ExamenTableController controller = loader.getController();
            if (controller != null && userId != null) {
                controller.setUserId(userId);
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) consulterExamenButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue de consultation des examens");
        }
    }
    
    @FXML
    private void handlePerformance() {
        showAlert(Alert.AlertType.INFORMATION, "Performance", 
                "Fonctionnalité pour visualiser les performances à implémenter");
    }
    
    @FXML
    private void handleVoirStatistiques() {
        showAlert(Alert.AlertType.INFORMATION, "Statistiques", 
                "Fonctionnalité pour visualiser les statistiques à implémenter");
    }
    
    @FXML
    private void handleOuvrirCalendrier() {
        showAlert(Alert.AlertType.INFORMATION, "Calendrier", 
                "Fonctionnalité pour ouvrir le calendrier à implémenter");
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Définit l'ID de l'utilisateur actuel
     * @param userId ID de l'utilisateur
     */
    public void setUserId(String userId) {
        this.userId = userId;
        System.out.println("ID utilisateur défini dans AccueilProfesseurController: " + userId);
    }
} 