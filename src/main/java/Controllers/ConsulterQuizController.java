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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import service.ExamenService;
import service.QuestionService;
import service.ReponseService;
import service.UserService;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ConsulterQuizController implements Initializable {

    @FXML private Label examenTitreLabel;
    @FXML private Label examenDescriptionLabel;
    @FXML private Button backButton;
    @FXML private ToggleButton questionsToggle;
    @FXML private ToggleButton reponsesToggle;
    @FXML private VBox questionsContainer;
    @FXML private VBox questionTemplate;
    @FXML private Label templateQuestionLabel;
    @FXML private VBox templateReponsesContainer;

    private ExamenService examenService;
    private QuestionService questionService;
    private ReponseService reponseService;
    private UserService userService;
    private Examen examen;
    private int examenId;
    private String examenTitre;
    private String examenDescription;
    
    // Map pour stocker les réponses de l'étudiant (question_id -> reponse_id)
    private Map<Integer, Integer> reponsesEtudiant = new HashMap<>();

    // Variables statiques pour stocker les données de démonstration
    private static List<Question> demoQuestions = new ArrayList<>();
    private static List<Reponse> demoReponses = new ArrayList<>();
    private static boolean useDemo = false;
    
    // Variable pour suivre le type d'utilisateur
    private boolean isStudentMode = false;
    private boolean isAdminMode = false;
    
    // Variable pour stocker l'ID de l'utilisateur
    private String userId;
    
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
        ConsulterQuizController.useDemo = useDemo;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        questionService = new QuestionService();
        reponseService = new ReponseService();
        userService = new UserService();
        
        // Utiliser les données de démo si elles ont été définies par RepondreQuizController
        if (useDemo) {
            System.out.println("Utilisation des données de démonstration pour l'affichage des résultats");
        }
        
        // Rendre invisible le template
        if (questionTemplate != null) {
            questionTemplate.setVisible(false);
            questionTemplate.setManaged(false);
        }
        
        Platform.runLater(() -> {
            loadExamen();
            loadQuestions();
            afficherScoreEtudiant(); // Afficher le score après le chargement des questions
        });
    }
    
    public void setExamenId(int examenId) {
        this.examenId = examenId;
        if (examenService != null) {
            examen = examenService.recupererParId(examenId);
            if (examen != null) {
                this.examenTitre = examen.getTitre();
                this.examenDescription = examen.getDescription();
                updateExamenInfo();
            }
        }
    }
    
    public void setExamenInfo(String titre, String description) {
        this.examenTitre = titre;
        this.examenDescription = description;
        updateExamenInfo();
    }
    
    private void loadExamen() {
        if (examen != null) {
            updateExamenInfo();
        }
    }
    
    private void updateExamenInfo() {
        if (examenTitre != null) {
            examenTitreLabel.setText(examenTitre);
            examenDescriptionLabel.setText(examenDescription);
        }
    }
    
    private void loadQuestions() {
        // Vider tout
        questionsContainer.getChildren().clear();
        
        List<Question> questions;
        
        // Vérifier si nous avons un examen valide
        if (examen == null) {
            if (examenId > 0) {
                examen = examenService.recupererParId(examenId);
            }
            
            if (examen == null) {
                // Aucun examen trouvé
                Label emptyLabel = new Label("Aucun examen spécifié.");
                emptyLabel.setStyle("-fx-padding: 20; -fx-font-style: italic;");
                questionsContainer.getChildren().add(emptyLabel);
                return;
            }
        }
        
        // Utiliser les questions de démo si disponibles, sinon charger depuis la BD
        if (useDemo && !demoQuestions.isEmpty()) {
            questions = demoQuestions;
            System.out.println("Chargement de " + questions.size() + " questions de démonstration pour les résultats");
        } else {
            questions = questionService.recupererParExamen(examen.getId());
            System.out.println("Chargement de " + questions.size() + " questions depuis la base de données pour l'examen #" + examen.getId());
        }
        
        if (questions.isEmpty()) {
            Label emptyLabel = new Label("Aucune question disponible pour ce quiz.");
            emptyLabel.setStyle("-fx-padding: 20; -fx-font-style: italic;");
            questionsContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Question question : questions) {
            VBox questionCard = createQuestionCard(question);
            questionsContainer.getChildren().add(questionCard);
        }
    }
    
    private List<Reponse> getReponsesForQuestion(int questionId) {
        if (useDemo) {
            List<Reponse> reponsesForQuestion = new ArrayList<>();
            for (Reponse r : demoReponses) {
                if (r.getQuestions_id() == questionId) {
                    reponsesForQuestion.add(r);
                }
            }
            return reponsesForQuestion;
        } else {
            return reponseService.recupererParQuestion(questionId);
        }
    }
    
    private VBox createQuestionCard(Question question) {
        VBox questionCard = new VBox();
        questionCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-padding: 20;");
        questionCard.setSpacing(20);
        
        // Titre de la question
        Label questionLabel = new Label("Question " + question.getId() + ": " + question.getQuestion());
        questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        questionCard.getChildren().add(questionLabel);
        
        // Container pour les réponses
        VBox reponsesContainer = new VBox();
        reponsesContainer.setSpacing(10);
        
        // Charger les réponses pour cette question
        List<Reponse> reponses = getReponsesForQuestion(question.getId());
        
        if (reponses.isEmpty()) {
            Label emptyLabel = new Label("Aucune réponse disponible pour cette question.");
            emptyLabel.setStyle("-fx-padding: 10; -fx-font-style: italic; -fx-text-fill: #757575;");
            reponsesContainer.getChildren().add(emptyLabel);
        } else {
            for (Reponse reponse : reponses) {
                Node reponseRow = createReponseRow(reponse);
                reponsesContainer.getChildren().add(reponseRow);
            }
        }
        
        questionCard.getChildren().add(reponsesContainer);
        
        return questionCard;
    }
    
    private Node createReponseRow(Reponse reponse) {
        HBox reponseRow = new HBox();
        reponseRow.setSpacing(10);
        reponseRow.setAlignment(Pos.CENTER_LEFT);
        reponseRow.setPadding(new Insets(5, 10, 5, 10));
        reponseRow.setStyle("-fx-background-radius: 5;");
        
        // Check if the response is correct (etat = 1)
        boolean isCorrect = reponse.getEtat() == 1;
        
        // Set background color based on correctness
        if (isCorrect) {
            reponseRow.setStyle("-fx-background-color: #e0f7e0; -fx-background-radius: 5;");
        } else {
            reponseRow.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");
        }
        
        // Add status indicator (✓ for correct, ○ for incorrect)
        Label statusLabel = new Label();
        if (isCorrect) {
            statusLabel.setText("✓");
            statusLabel.setStyle("-fx-text-fill: #2e8b57; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            statusLabel.setText("○");
            statusLabel.setStyle("-fx-text-fill: #a0a0a0; -fx-font-size: 14px;");
        }
        
        // Response text
        Label reponseText = new Label(reponse.getReponse());
        if (isCorrect) {
            reponseText.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e8b57;");
        }
        
        reponseRow.getChildren().addAll(statusLabel, reponseText);
        
        return reponseRow;
    }
    
    @FXML
    private void handleCancel() {
        try {
            // Déterminer la page d'accueil selon le type d'utilisateur
            String fxmlPath;
            String title;
            
            if (isStudentMode) {
                // Rediriger vers l'accueil étudiant
                fxmlPath = "/view/AccueilEtudiant.fxml";
                title = "Espace Étudiant";
                System.out.println("Redirection vers l'accueil étudiant");
            } else if (isAdminMode) {
                // Rediriger vers le panneau d'administration
                fxmlPath = "/view/AdminPanel.fxml";
                title = "Panneau d'administration";
                System.out.println("Redirection vers le panneau d'administration");
            } else {
                // Rediriger vers l'accueil professeur
                fxmlPath = "/view/AccueilProfesseur.fxml";
                title = "Espace Professeur";
                System.out.println("Redirection vers l'accueil professeur");
            }
            
            // Charger la page correspondante
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = backButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle(title);
            
        } catch (IOException e) {
            e.printStackTrace();
            String errorMessage = isStudentMode ? 
                "Erreur lors du retour à l'accueil étudiant" : 
                (isAdminMode ? "Erreur lors de la redirection vers le panneau d'administration" : "Erreur lors du retour à l'accueil professeur");
            showAlert(errorMessage, e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleQuestionsToggle() {
        try {
            // Naviguer vers la vue des questions
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuestionView.fxml"));
            Parent root = loader.load();
            
            QuestionController controller = loader.getController();
            if (controller != null) {
                controller.setExamenId(examen.getId());
                controller.setExamenInfo(examen.getTitre(), examen.getDescription());
            }
            
            Scene scene = questionsToggle.getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la navigation vers la vue des questions", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleReponsesToggle() {
        try {
            // Naviguer vers la vue des réponses
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ReponseView.fxml"));
            Parent root = loader.load();
            
            ReponseController controller = loader.getController();
            if (controller != null && examen != null) {
                // Récupérer la première question de l'examen pour l'afficher
                List<Question> questions = questionService.recupererParExamen(examen.getId());
                if (!questions.isEmpty()) {
                    controller.setQuestion(questions.get(0));
                    controller.setExamenInfo(examen.getId(), examen.getTitre(), examen.getDescription());
                }
            }
            
            Scene scene = reponsesToggle.getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la navigation vers la vue des réponses", Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Configure l'interface en mode étudiant en masquant les options d'édition
     * et en activant les fonctionnalités spécifiques aux étudiants
     */
    public void configurerModeEtudiant() {
        // Activer le mode étudiant
        isStudentMode = true;
        
        // Désactiver les onglets non pertinents pour l'étudiant
        if (questionsToggle != null) {
            questionsToggle.setVisible(false);
            questionsToggle.setManaged(false);
        }
        
        if (reponsesToggle != null) {
            reponsesToggle.setVisible(false);
            reponsesToggle.setManaged(false);
        }
        
        // Configurer le titre du bouton de retour pour indiquer "Retour à l'accueil"
        if (backButton != null) {
            Label label = new Label("← Retour à l'accueil");
            label.setTextFill(javafx.scene.paint.Color.WHITE);
            label.setFont(new javafx.scene.text.Font(14));
            backButton.setGraphic(label);
        }
    }

    /**
     * Définit les réponses de l'étudiant pour calculer son score
     * @param reponsesEtudiant Map des réponses de l'étudiant (question_id -> reponse_id)
     */
    public void setReponsesEtudiant(Map<Integer, Integer> reponsesEtudiant) {
        this.reponsesEtudiant = reponsesEtudiant;
    }

    // Ajout d'une méthode pour afficher le score de l'étudiant
    private void afficherScoreEtudiant() {
        // N'afficher le score que si nous sommes en mode étudiant
        if (!isStudentMode || examen == null) return;
        
        // Calculer le score basé sur les questions et les bonnes réponses
        int totalPoints = 0;
        int pointsObtenus = 0;
        
        List<Question> questions;
        if (useDemo && !demoQuestions.isEmpty()) {
            questions = demoQuestions;
        } else {
            questions = questionService.recupererParExamen(examen.getId());
        }
        
        for (Question question : questions) {
            // Ajouter les points de la question au total
            totalPoints += question.getNbr_points();
            
            // Vérifier si l'étudiant a choisi la bonne réponse pour cette question
            if (reponsesEtudiant.containsKey(question.getId())) {
                // L'étudiant a répondu à cette question
                int reponseId = reponsesEtudiant.get(question.getId());
                
                // Récupérer les réponses pour cette question
                List<Reponse> reponses = getReponsesForQuestion(question.getId());
                
                // Vérifier si la réponse choisie est correcte
                for (Reponse reponse : reponses) {
                    if (reponse.getId() == reponseId && reponse.getEtat() == 1) {
                        // C'est une réponse correcte
                        pointsObtenus += question.getNbr_points();
                        break;
                    }
                }
            }
            // Si l'étudiant n'a pas choisi de réponse ou si la réponse est incorrecte,
            // aucun point n'est ajouté
        }
        
        // Si aucune réponse n'a été définie, utiliser une démo aléatoire
        if (reponsesEtudiant.isEmpty()) {
            // Réinitialiser les points pour la démo
            pointsObtenus = 0;
            
            // Dans cet exemple, nous supposons que l'étudiant a une chance sur deux de répondre correctement
            for (Question question : questions) {
                if (Math.random() > 0.3) {  // 70% de chances de réussir pour la démo
                    pointsObtenus += question.getNbr_points();
                }
            }
        }
        
        // Créer un panneau de résultat
        VBox resultatPanel = creerPanelResultat(pointsObtenus, totalPoints);
        
        // Ajouter le panneau en haut de la liste des questions
        if (!questionsContainer.getChildren().isEmpty()) {
            questionsContainer.getChildren().add(0, resultatPanel);
        } else {
            questionsContainer.getChildren().add(resultatPanel);
        }
    }
    
    private VBox creerPanelResultat(int pointsObtenus, int totalPoints) {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); " +
                        "-fx-padding: 20;");
        panel.setSpacing(15);
        panel.setAlignment(Pos.CENTER);
        
        // Titre du panneau
        Label titreLabel = new Label("Résultat du Quiz");
        titreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20; -fx-text-fill: #2196F3;");
        
        // Calcul du pourcentage
        double pourcentage = (double) pointsObtenus / totalPoints * 100;
        String appreciation;
        String couleur;
        String message;
        
        // Définir l'appréciation et la couleur en fonction du score
        if (pourcentage >= 80) {
            appreciation = "Excellent !";
            couleur = "#4CAF50"; // Vert
            message = "Félicitations ! Vous avez une très bonne maîtrise du sujet.";
        } else if (pourcentage >= 60) {
            appreciation = "Bien !";
            couleur = "#2196F3"; // Bleu
            message = "Bon travail ! Vous avez de bonnes connaissances sur ce sujet.";
        } else if (pourcentage >= 40) {
            appreciation = "Moyen";
            couleur = "#FF9800"; // Orange
            message = "Continuez vos efforts ! Vous avez encore quelques notions à réviser.";
        } else {
            appreciation = "À améliorer";
            couleur = "#F44336"; // Rouge
            message = "N'abandonnez pas ! Revoyez les concepts fondamentaux de ce sujet.";
        }
        
        // Score en grand
        Label scoreLabel = new Label(String.format("%.1f%%", pourcentage));
        scoreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 48; -fx-text-fill: " + couleur + ";");
        
        // Barre de progression
        ProgressBar progressBar = new ProgressBar(pourcentage / 100);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: " + couleur + ";");
        
        // Points
        Label pointsLabel = new Label(pointsObtenus + " sur " + totalPoints + " points");
        pointsLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #757575;");
        
        // Appréciation
        Label appreciationLabel = new Label(appreciation);
        appreciationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: " + couleur + ";");
        
        // Message personnalisé
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #757575; -fx-text-alignment: center;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        
        // Séparateur
        Separator separator = new Separator();
        separator.setPrefWidth(300);
        
        // Bouton pour revoir le cours
        Button revisiterCoursBtn = new Button("Retour à l'accueil");
        revisiterCoursBtn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; " +
                                  "-fx-padding: 10 20; -fx-background-radius: 5;");
        revisiterCoursBtn.setOnAction(e -> handleCancel());
        
        // Ajouter tous les éléments au panneau
        panel.getChildren().addAll(
            titreLabel,
            scoreLabel,
            progressBar,
            pointsLabel,
            appreciationLabel,
            separator,
            messageLabel,
            revisiterCoursBtn
        );
        
        // Ajouter des marges entre les éléments
        VBox.setMargin(progressBar, new Insets(10, 0, 10, 0));
        VBox.setMargin(separator, new Insets(15, 0, 15, 0));
        VBox.setMargin(revisiterCoursBtn, new Insets(15, 0, 0, 0));
        
        return panel;
    }

    /**
     * Définit l'ID de l'utilisateur actuel
     * @param userId ID de l'utilisateur
     */
    public void setUserId(String userId) {
        this.userId = userId;
        
        // Vérifier le type d'utilisateur
        if (userService != null && userId != null) {
            String role = userService.getFastUserRole(userId);
            isStudentMode = role != null && 
                (role.equalsIgnoreCase("Étudiant") || 
                 role.equalsIgnoreCase("student") || 
                 role.equals("0"));
            
            isAdminMode = role != null && 
                (role.equalsIgnoreCase("Administrateur") || 
                 role.equalsIgnoreCase("admin") || 
                 role.equals("2"));
            
            System.out.println("Utilisateur " + userId + " identifié comme: " + 
                              (isStudentMode ? "étudiant" : isAdminMode ? "administrateur" : "professeur/autre"));
        }
    }

    /**
     * Définit si le contrôleur est en mode administrateur
     * @param isAdminMode true si en mode administrateur, false sinon
     */
    public void setAdminMode(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
        
        // Configurer le titre du bouton de retour pour indiquer "Retour au panneau d'administration"
        if (isAdminMode && backButton != null) {
            Label label = new Label("← Retour au panneau d'administration");
            label.setTextFill(javafx.scene.paint.Color.WHITE);
            label.setFont(new javafx.scene.text.Font(14));
            backButton.setGraphic(label);
        }
    }
} 