package Controllers;

import entite.Examen;
import entite.Question;
import entite.Reponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import service.ExamenService;
import service.QuestionService;
import service.ReponseService;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RepondreQuizController implements Initializable {

    @FXML private Label examenTitreLabel;
    @FXML private Label examenDescriptionLabel;
    @FXML private VBox questionsContainer;
    @FXML private Button backButton;
    @FXML private Button submitButton;

    private ExamenService examenService;
    private QuestionService questionService;
    private ReponseService reponseService;
    private Examen examen;
    private Map<Integer, ToggleGroup> questionToggleGroups = new HashMap<>();
    private Map<Integer, List<RadioButton>> questionOptions = new HashMap<>();
    private Map<Integer, List<Reponse>> allReponses = new HashMap<>();
    
    // Variable pour stocker l'ID de l'utilisateur
    private String userId;
    
    // Variables statiques pour stocker les données de démonstration
    private static List<Question> demoQuestions = new ArrayList<>();
    private static List<Reponse> demoReponses = new ArrayList<>();
    private static boolean useDemo = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        questionService = new QuestionService();
        reponseService = new ReponseService();
        
        // Vider le conteneur d'exemples
        Platform.runLater(() -> {
            questionsContainer.getChildren().clear();
            if (examen != null) {
                loadExamenInfo();
                loadQuestions();
            }
        });
    }
    
    /**
     * Méthode statique pour définir les questions et réponses de démonstration
     * @param questions Liste des questions de démonstration
     * @param reponses Liste des réponses de démonstration
     */
    public static void setDemoQuestionsAndResponses(List<Question> questions, List<Reponse> reponses) {
        demoQuestions = questions;
        demoReponses = reponses;
        useDemo = true;
    }
    
    /**
     * Méthode statique pour activer ou désactiver le mode démo
     * @param useDemo true pour utiliser le mode démo, false sinon
     */
    public static void setUseDemo(boolean useDemo) {
        RepondreQuizController.useDemo = useDemo;
    }
    
    public void setExamen(Examen examen) {
        this.examen = examen;
        if (examen != null && examenTitreLabel != null) {
            loadExamenInfo();
            loadQuestions();
        }
    }
    
    public void setExamenId(int examenId) {
        if (examenService != null) {
            this.examen = examenService.recupererParId(examenId);
            if (this.examen != null && examenTitreLabel != null) {
                loadExamenInfo();
                loadQuestions();
            }
        }
    }
    
    /**
     * Définit l'ID de l'utilisateur actuel
     * @param userId ID de l'utilisateur
     */
    public void setUserId(String userId) {
        this.userId = userId;
        System.out.println("ID utilisateur défini dans RepondreQuizController: " + userId);
    }
    
    private void loadExamenInfo() {
        if (examen != null) {
            examenTitreLabel.setText(examen.getTitre());
            examenDescriptionLabel.setText(examen.getDescription());
        }
    }
    
    private void loadQuestions() {
        if (examen != null) {
            // Vider tout
            questionsContainer.getChildren().clear();
            questionToggleGroups.clear();
            questionOptions.clear();
            allReponses.clear();
            
            List<Question> questions;
            
            // Utiliser les questions de démo si disponibles, sinon charger depuis la BD
            if (useDemo && !demoQuestions.isEmpty()) {
                questions = demoQuestions;
                System.out.println("Chargement de " + questions.size() + " questions de démonstration");
            } else {
                questions = questionService.recupererParExamenId(examen.getId());
                System.out.println("Chargement de " + questions.size() + " questions depuis la BD");
            }
            
            if (questions.isEmpty()) {
                Label emptyLabel = new Label("Aucune question disponible pour ce quiz.");
                emptyLabel.setStyle("-fx-padding: 20; -fx-font-style: italic;");
                questionsContainer.getChildren().add(emptyLabel);
                return;
            }
            
            // Charger chaque question et ses réponses
            for (Question question : questions) {
                VBox questionCard = createQuestionCard(question);
                questionsContainer.getChildren().add(questionCard);
            }
        }
    }
    
    private VBox createQuestionCard(Question question) {
        VBox questionCard = new VBox();
        questionCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-padding: 20;");
        questionCard.setSpacing(15);
        
        // Titre de la question
        Label questionLabel = new Label("Question " + question.getId() + ": " + question.getQuestion());
        questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        questionCard.getChildren().add(questionLabel);
        
        // Container pour les options de réponses
        VBox reponsesContainer = new VBox();
        reponsesContainer.setSpacing(8);
        
        // Créer un groupe de boutons radio pour cette question
        ToggleGroup optionsGroup = new ToggleGroup();
        questionToggleGroups.put(question.getId(), optionsGroup);
        
        // Charger les réponses pour cette question (soit de démo, soit de la BD)
        List<Reponse> reponses;
        if (useDemo) {
            reponses = new ArrayList<>();
            for (Reponse r : demoReponses) {
                if (r.getQuestions_id() == question.getId()) {
                    reponses.add(r);
                }
            }
            System.out.println("Chargement de " + reponses.size() + " réponses de démo pour la question " + question.getId());
        } else {
            reponses = reponseService.recupererParQuestionId(question.getId());
        }
        
        // Stocker toutes les réponses pour cette question
        allReponses.put(question.getId(), reponses);
        
        // Liste pour stocker les boutons radio pour cette question
        List<RadioButton> radioButtons = new ArrayList<>();
        questionOptions.put(question.getId(), radioButtons);
        
        if (reponses.isEmpty()) {
            Label emptyLabel = new Label("Aucune réponse disponible pour cette question.");
            emptyLabel.setStyle("-fx-padding: 10; -fx-font-style: italic; -fx-text-fill: #757575;");
            reponsesContainer.getChildren().add(emptyLabel);
        } else {
            for (Reponse reponse : reponses) {
                RadioButton optionButton = createOptionButton(reponse, optionsGroup);
                reponsesContainer.getChildren().add(optionButton);
                radioButtons.add(optionButton);
            }
        }
        
        questionCard.getChildren().add(reponsesContainer);
        
        return questionCard;
    }
    
    private RadioButton createOptionButton(Reponse reponse, ToggleGroup group) {
        RadioButton option = new RadioButton(reponse.getReponse());
        option.setToggleGroup(group);
        option.setUserData(reponse.getId()); // Stocker l'ID de la réponse
        option.setStyle("-fx-font-size: 14;");
        option.setPadding(new javafx.geometry.Insets(5));
        return option;
    }
    
    @FXML
    private void handleBack() {
        try {
            // Revenir à la page d'accueil étudiant
            Parent root = FXMLLoader.load(getClass().getResource("/view/AccueilEtudiant.fxml"));
            Scene scene = backButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à l'accueil: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSubmit() {
        // Vérifier si toutes les questions ont une réponse
        List<Integer> questionsNonRepondues = new ArrayList<>();
        
        for (Map.Entry<Integer, ToggleGroup> entry : questionToggleGroups.entrySet()) {
            if (entry.getValue().getSelectedToggle() == null) {
                // Cette question n'a pas de réponse sélectionnée
                questionsNonRepondues.add(entry.getKey());
            }
        }
        
        if (!questionsNonRepondues.isEmpty()) {
            // Il y a des questions sans réponse
            StringBuilder message = new StringBuilder("Veuillez répondre à toutes les questions suivantes: ");
            for (int i = 0; i < questionsNonRepondues.size(); i++) {
                message.append("Question ").append(questionsNonRepondues.get(i));
                if (i < questionsNonRepondues.size() - 1) {
                    message.append(", ");
                }
            }
            showAlert(Alert.AlertType.WARNING, "Questions non répondues", message.toString());
            return;
        }
        
        // Collecter les réponses de l'utilisateur
        Map<Integer, Integer> reponsesEtudiant = new HashMap<>();
        for (Map.Entry<Integer, ToggleGroup> entry : questionToggleGroups.entrySet()) {
            int questionId = entry.getKey();
            Toggle selectedToggle = entry.getValue().getSelectedToggle();
            if (selectedToggle != null) {
                int reponseId = (int) selectedToggle.getUserData();
                reponsesEtudiant.put(questionId, reponseId);
            }
        }
        
        // Enregistrer les réponses de l'utilisateur
        if (sauvegarderReponses()) {
            // Toutes les questions ont une réponse, naviguer vers la page de résultats
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConsulterQuizView.fxml"));
                Parent root = loader.load();
                
                ConsulterQuizController controller = loader.getController();
                if (controller != null) {
                    controller.setExamenId(examen.getId());
                    // Passer les réponses de l'étudiant
                    controller.setReponsesEtudiant(reponsesEtudiant);
                    // Passer l'ID de l'utilisateur
                    if (userId != null) {
                        controller.setUserId(userId);
                    }
                    // Désactiver les onglets d'édition pour l'étudiant
                    controller.configurerModeEtudiant();
                }
                
                Scene scene = submitButton.getScene();
                scene.setRoot(root);
                
                // Afficher un message de succès
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Vos réponses ont été soumises avec succès!");
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la soumission des réponses: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement de vos réponses.");
        }
    }
    
    /**
     * Sauvegarde les réponses choisies par l'utilisateur
     * @return true si les réponses ont été sauvegardées avec succès
     */
    private boolean sauvegarderReponses() {
        boolean success = true;
        
        // Dans une application réelle, on enregistrerait les réponses de l'utilisateur dans la base de données
        // Pour cette démonstration, nous allons simplement simuler la sauvegarde
        
        for (Map.Entry<Integer, ToggleGroup> entry : questionToggleGroups.entrySet()) {
            int questionId = entry.getKey();
            ToggleGroup group = entry.getValue();
            
            if (group.getSelectedToggle() != null) {
                // Récupérer la réponse choisie
                int reponseId = (int) group.getSelectedToggle().getUserData();
                System.out.println("Question " + questionId + " -> Réponse " + reponseId);
                
                // NOTE: Ici on pourrait sauvegarder dans la base de données
                // Par exemple: responseService.enregistrerReponseUtilisateur(userId, questionId, reponseId);
            }
        }
        
        return success;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 