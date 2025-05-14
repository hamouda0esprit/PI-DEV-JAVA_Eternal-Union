package Controllers;

import entite.Examen;
import entite.Question;
import entite.Reponse;
import entite.ResultatQuiz;
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
import service.ResultatQuizService;
import service.TranslationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.awt.Desktop;

public class RepondreQuizController implements Initializable {

    @FXML private Label examenTitreLabel;
    @FXML private Label examenDescriptionLabel;
    @FXML private VBox questionsContainer;
    @FXML private Button backButton;
    @FXML private Button submitButton;
    @FXML private Button translateButton;
    @FXML private ComboBox<String> languageSelector;
    @FXML private TextArea contentTextArea;

    private ExamenService examenService;
    private QuestionService questionService;
    private ReponseService reponseService;
    private ResultatQuizService resultatQuizService;
    private TranslationService translationService;
    private Examen examen;
    private Map<Integer, ToggleGroup> questionToggleGroups = new HashMap<>();
    private Map<Integer, List<RadioButton>> questionOptions = new HashMap<>();
    private Map<Integer, List<Reponse>> allReponses = new HashMap<>();
    
    // Pour la synthèse vocale
    private boolean isSpeaking = false;
    private Process currentSpeechProcess;
    private ExecutorService speechExecutor;
    private String currentAudioFile = null;
    // Emplacement temporaire pour sauvegarder les fichiers audio
    private final String tempDir = System.getProperty("java.io.tmpdir");
    
    // For translation
    private Map<String, String> languageCodes;
    private String currentLanguage = "fr";
    private ExecutorService executorService;
    
    // Variable pour stocker l'ID de l'utilisateur
    private String userId;
    
    // Variables statiques pour stocker les données de démonstration
    private static List<Question> demoQuestions = new ArrayList<>();
    private static List<Reponse> demoReponses = new ArrayList<>();
    private static boolean useDemo = false;
    private boolean modeConsultation = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        questionService = new QuestionService();
        reponseService = new ReponseService();
        resultatQuizService = new ResultatQuizService();
        translationService = new TranslationService();
        executorService = Executors.newFixedThreadPool(2);
        speechExecutor = Executors.newSingleThreadExecutor();
        
        // Initialize language codes
        languageCodes = translationService.getSupportedLanguages();
        
        // Initialize language selector if available
        if (languageSelector != null) {
            languageSelector.getItems().add("Français (Original)");
            languageSelector.getItems().addAll(languageCodes.keySet());
            languageSelector.setValue("Français (Original)");
            languageSelector.setOnAction(e -> handleLanguageChange());
        }
        
        // Initialize content area if available
        if (contentTextArea != null) {
            setupContentArea();
        }
        
        // Initialize translate button if available
        if (translateButton != null) {
            translateButton.setOnAction(e -> showLanguageDialog());
            // Traduire le texte du bouton si l'application démarre dans une autre langue
            if (!currentLanguage.equals("fr")) {
                try {
                    String translatedText = translationService.translate("Traduire", currentLanguage, "fr");
                    translateButton.setText(translatedText);
                } catch (Exception ex) {
                    System.err.println("Erreur lors de la traduction du bouton: " + ex.getMessage());
                }
            }
        }
        
        // Traduire les boutons de navigation
        if (backButton != null && !currentLanguage.equals("fr")) {
            try {
                String translatedText = translationService.translate("Retour", currentLanguage, "fr");
                backButton.setText(translatedText);
            } catch (Exception ex) {
                System.err.println("Erreur lors de la traduction du bouton retour: " + ex.getMessage());
            }
        }
        
        if (submitButton != null && !currentLanguage.equals("fr")) {
            try {
                String translatedText = translationService.translate("Soumettre", currentLanguage, "fr");
                submitButton.setText(translatedText);
            } catch (Exception ex) {
                System.err.println("Erreur lors de la traduction du bouton soumettre: " + ex.getMessage());
            }
        }
        
        // Test de la connexion à la base de données
        testDatabaseConnection();
        
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
     * Teste la connexion à la base de données et vérifie que les tables nécessaires existent
     */
    private void testDatabaseConnection() {
        try {
            // Vérifier que la connexion fonctionne
            boolean connexionOk = resultatQuizService.testerConnexion();
            if (connexionOk) {
                System.out.println("Connexion à la base de données réussie");
            } else {
                System.err.println("ERREUR: Impossible de se connecter à la base de données");
            }
            
            // Vérifier ou créer la table resultat_quiz si nécessaire
            boolean tableOk = resultatQuizService.verifierTableResultatQuiz();
            if (tableOk) {
                System.out.println("Table resultat_quiz vérifiée avec succès");
            } else {
                System.err.println("ERREUR: Problème avec la table resultat_quiz");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du test de la base de données: " + e.getMessage());
            e.printStackTrace();
        }
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
        if (userId == null || userId.isEmpty()) {
            System.err.println("AVERTISSEMENT: Tentative de définir un ID utilisateur null ou vide");
            // On ne modifie pas la valeur actuelle si elle est null ou vide
            return;
        }
        
        // Vérifie que l'ID est un nombre entier valide
        try {
            int userIdInt = Integer.parseInt(userId);
        this.userId = userId;
            System.out.println("ID utilisateur défini avec succès dans RepondreQuizController: " + userId);
        } catch (NumberFormatException e) {
            System.err.println("ERREUR: L'ID utilisateur n'est pas un nombre valide: " + userId);
        }
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
                questions = questionService.recupererParExamen(examen.getId());
                System.out.println("Chargement de " + questions.size() + " questions depuis la BD");
            }
            
            if (questions.isEmpty()) {
                Label emptyLabel = new Label("Aucune question disponible pour ce quiz.");
                emptyLabel.setStyle("-fx-padding: 20; -fx-font-style: italic;");
                questionsContainer.getChildren().add(emptyLabel);
                return;
            }
            
            // Numéroter les questions séquentiellement
            int questionNumber = 1;
            
            // Charger chaque question et ses réponses
            for (Question question : questions) {
                VBox questionCard = createQuestionCard(question, questionNumber);
                questionsContainer.getChildren().add(questionCard);
                questionNumber++;
            }
        }
    }
    
    private VBox createQuestionCard(Question question, int questionNumber) {
        VBox questionCard = new VBox();
        questionCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-padding: 20;");
        questionCard.setSpacing(15);
        
        // En-tête de la question avec titre et bouton de lecture
        HBox questionHeader = new HBox();
        questionHeader.setAlignment(Pos.CENTER_LEFT);
        questionHeader.setSpacing(10);
        
        // Titre de la question - traduit le préfixe "Question" si nécessaire
        String questionLabelText;
        try {
            if (!currentLanguage.equals("fr")) {
                String questionPrefix = "Question";
                String translatedPrefix = translationService.translate(questionPrefix, currentLanguage, "fr");
                questionLabelText = translatedPrefix + " " + questionNumber + ": " + question.getQuestion();
            } else {
                questionLabelText = "Question " + questionNumber + ": " + question.getQuestion();
            }
        } catch (Exception e) {
            // En cas d'erreur, utiliser le format standard
            questionLabelText = "Question " + questionNumber + ": " + question.getQuestion();
        }
        
        Label questionLabel = new Label(questionLabelText);
        questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        questionLabel.setWrapText(true);
        HBox.setHgrow(questionLabel, javafx.scene.layout.Priority.ALWAYS);
        
        // Bouton de lecture avec support RTL pour l'arabe
        Button speakButton = new Button();
        speakButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        
        try {
            ImageView speakIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/volume-up.png")));
            speakIcon.setFitHeight(16);
            speakIcon.setFitWidth(16);
            speakButton.setGraphic(speakIcon);
        } catch (Exception e) {
            // Si l'icône n'est pas trouvée, utiliser du texte
            speakButton.setText("🔊");
        }
        
        // Traduire le tooltip si nécessaire
        String tooltipText = "Écouter la question et les réponses";
        if (!currentLanguage.equals("fr")) {
            try {
                tooltipText = translationService.translate(tooltipText, currentLanguage, "fr");
            } catch (Exception e) {
                System.err.println("Erreur lors de la traduction du tooltip: " + e.getMessage());
                // Conserver le texte original en cas d'erreur
            }
        }
        speakButton.setTooltip(new Tooltip(tooltipText));
        
        // Support spécifique pour l'arabe et les langues RTL
        if (currentLanguage.startsWith("ar") || currentLanguage.equals("he") || currentLanguage.equals("fa")) {
            // Définir la direction du texte de droite à gauche
            questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-alignment: right; -fx-alignment: baseline-right;");
            questionHeader.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        }
        
        // Ajouter le gestionnaire d'événements pour la lecture vocale
        speakButton.setOnAction(e -> {
            // Collecter le texte de la question
            String questionText = question.getQuestion();
            
            // Collecter les textes des réponses
            List<Reponse> reponses;
            if (useDemo) {
                reponses = new ArrayList<>();
                for (Reponse r : demoReponses) {
                    if (r.getQuestions_id() == question.getId()) {
                        reponses.add(r);
                    }
                }
            } else {
                reponses = reponseService.recupererParQuestion(question.getId());
            }
            
            StringBuilder textToSpeak = new StringBuilder();
            textToSpeak.append("Question numéro ").append(questionNumber).append(": ").append(questionText);
            textToSpeak.append(". Les réponses proposées sont: ");
            
            for (int i = 0; i < reponses.size(); i++) {
                textToSpeak.append(reponses.get(i).getReponse());
                if (i < reponses.size() - 1) {
                    textToSpeak.append(", ");
                }
            }
            
            speak(textToSpeak.toString());
        });
        
        // Ajouter les éléments à l'en-tête
        questionHeader.getChildren().addAll(questionLabel, speakButton);
        questionCard.getChildren().add(questionHeader);
        
        // Container pour les options de réponses
        VBox reponsesContainer = new VBox();
        reponsesContainer.setSpacing(8);
        
        // Support RTL pour les langues comme l'arabe
        if (currentLanguage.startsWith("ar") || currentLanguage.equals("he") || currentLanguage.equals("fa")) {
            reponsesContainer.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        }
        
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
            System.out.println("Chargement de " + reponses.size() + " réponses de démo pour la question " + questionNumber);
        } else {
            reponses = reponseService.recupererParQuestion(question.getId());
        }
        
        // Stocker toutes les réponses pour cette question
        allReponses.put(question.getId(), reponses);
        
        // Liste pour stocker les boutons radio pour cette question
        List<RadioButton> radioButtons = new ArrayList<>();
        questionOptions.put(question.getId(), radioButtons);
        
        if (reponses.isEmpty()) {
            // Traduire le message d'absence de réponses si nécessaire
            String emptyMessage = "Aucune réponse disponible pour cette question.";
            if (!currentLanguage.equals("fr")) {
                try {
                    emptyMessage = translationService.translate(emptyMessage, currentLanguage, "fr");
                } catch (Exception e) {
                    // En cas d'erreur, utiliser le message par défaut
                    System.err.println("Erreur lors de la traduction du message d'absence de réponses: " + e.getMessage());
                }
            }
            
            Label emptyLabel = new Label(emptyMessage);
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
    
    /**
     * Creates a radio button for an answer option
     */
    private RadioButton createOptionButton(Reponse reponse, ToggleGroup group) {
        RadioButton button = createOptionButton(reponse.getId(), reponse.getReponse(), group);
        
        // Support RTL pour les langues comme l'arabe
        if (currentLanguage.startsWith("ar") || currentLanguage.equals("he") || currentLanguage.equals("fa")) {
            button.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        }
        
        return button;
    }
    
    /**
     * Creates a radio button for an answer option
     */
    private RadioButton createOptionButton(int reponseId, String text, ToggleGroup group) {
        RadioButton option = new RadioButton(text);
        option.setToggleGroup(group);
        option.setUserData(reponseId); // Store the response ID
        option.setStyle("-fx-font-size: 14;");
        option.setPadding(new javafx.geometry.Insets(5));
        
        // Support RTL pour les langues comme l'arabe
        if (currentLanguage.startsWith("ar") || currentLanguage.equals("he") || currentLanguage.equals("fa")) {
            option.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        }
        
        return option;
    }
    
    @FXML
    private void handleBack() {
        try {
            // Revenir à la page d'accueil étudiant
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AccueilEtudiant.fxml"));
            Parent root = loader.load();
            
            // Passer l'ID utilisateur au contrôleur
            AccueilEtudiantController controller = loader.getController();
            if (userId != null && !userId.isEmpty()) {
                controller.setUserId(userId);
            }
            
            Scene scene = backButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à l'accueil: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSubmit() {
        // Debug information
        System.out.println("handleSubmit called with userId: " + userId + ", examen: " + (examen != null ? examen.getId() : "null"));
        
        // Vérifier si l'utilisateur et l'examen sont définis
        if (userId == null || userId.isEmpty()) {
            // Ne pas continuer si l'utilisateur n'est pas identifié
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour soumettre un quiz.");
            return;
        }
        
        if (examen == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun examen n'est chargé.");
            return;
        }
        
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
        if (sauvegarderReponses(reponsesEtudiant)) {
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
     * Sauvegarde les réponses choisies par l'utilisateur et le résultat du quiz
     * @param reponsesEtudiant Map des réponses choisies par l'étudiant (questionId -> reponseId)
     * @return true si les réponses ont été sauvegardées avec succès
     */
    private boolean sauvegarderReponses(Map<Integer, Integer> reponsesEtudiant) {
        System.out.println("sauvegarderReponses appelée avec userId: " + userId + ", examen: " + (examen != null ? examen.getId() : "null"));
        
        if (userId == null || userId.isEmpty()) {
            System.err.println("Erreur: userId null ou vide");
            return false;
        }
        
        if (examen == null) {
            System.err.println("Erreur: examen null");
            return false;
        }
        
        try {
            // Convertir l'ID utilisateur en entier
            int userIdInt;
            try {
                userIdInt = Integer.parseInt(userId);
                System.out.println("ID utilisateur converti: " + userIdInt);
            } catch (NumberFormatException e) {
                System.err.println("Erreur: L'ID utilisateur n'est pas un nombre valide: " + userId);
                return false;
            }
            
            // Calculer le score
            int totalPoints = questionToggleGroups.size(); // 1 point par question
            int score = 0;
            
            System.out.println("Nombre total de questions: " + totalPoints);
            
            // Vérifier les réponses correctes
            for (Map.Entry<Integer, Integer> entry : reponsesEtudiant.entrySet()) {
            int questionId = entry.getKey();
                int reponseId = entry.getValue();
                
                // Récupérer toutes les réponses pour cette question
                List<Reponse> reponses = allReponses.get(questionId);
                if (reponses != null) {
                    // Trouver la réponse sélectionnée
                    boolean reponseFound = false;
                    for (Reponse reponse : reponses) {
                        if (reponse.getId() == reponseId) {
                            reponseFound = true;
                            // Si la réponse est correcte (etat = 1), ajouter un point
                            if (reponse.getEtat() == 1) {
                                score++;
                                System.out.println("Question " + questionId + ": Réponse correcte! +1 point");
                            } else {
                                System.out.println("Question " + questionId + ": Réponse incorrecte");
                            }
                            break;
                        }
                    }
                    
                    if (!reponseFound) {
                        System.out.println("Question " + questionId + ": Réponse " + reponseId + " non trouvée dans la liste des réponses possibles");
                    }
                } else {
                    System.out.println("Question " + questionId + ": Aucune réponse associée trouvée");
                }
            }
            
            System.out.println("Score final: " + score + "/" + totalPoints);
            
            // Vérifier si un résultat existe déjà pour cet utilisateur et cet examen
            ResultatQuiz resultatExistant = resultatQuizService.recupererParUtilisateurEtExamen(userIdInt, examen.getId());
            
            if (resultatExistant == null) {
                // Premier essai - créer un nouveau résultat avec le nombre d'essais initial
                int nbrEssaiInitial = examen.getNbrEssai() > 0 ? examen.getNbrEssai() - 1 : 0;
                System.out.println("Premier essai - Nombre d'essais initial: " + nbrEssaiInitial);
                
                ResultatQuiz resultat = new ResultatQuiz();
                resultat.setExamen_id(examen.getId());
                resultat.setId_user_id(userIdInt);
                resultat.setScore(score);
                resultat.setTotalPoints(totalPoints);
                resultat.setDatePassage(new Date()); // Date actuelle
                resultat.setNbrEssai(nbrEssaiInitial); // définir le nombre d'essais restants
                
                System.out.println("Tentative d'enregistrement du résultat dans la BD avec " + nbrEssaiInitial + " essais restants");
                
                // Sauvegarder le résultat dans la base de données
                boolean success = resultatQuizService.ajouter(resultat);
                if (success) {
                    System.out.println("Résultat enregistré avec succès: " + score + "/" + totalPoints + " pour l'utilisateur " + userId);
                    System.out.println("Essais restants: " + nbrEssaiInitial);
                    return true;
                } else {
                    System.err.println("Erreur lors de l'enregistrement du résultat du quiz");
                    return false;
                }
            } else {
                // Ce n'est pas le premier essai - vérifier s'il reste des essais
                int essaisRestants = resultatExistant.getNbrEssai();
                
                if (essaisRestants > 0 || examen.getNbrEssai() <= 0) {
                    // Il reste des essais ou essais illimités
                    System.out.println("Essai supplémentaire - Essais restants avant cet essai: " + essaisRestants);
                    
                    // Mettre à jour le score si celui-ci est meilleur
                    if (score > resultatExistant.getScore()) {
                        resultatExistant.setScore(score);
                        System.out.println("Nouveau meilleur score: " + score);
                    }
                    
                    // Mettre à jour la date
                    resultatExistant.setDatePassage(new Date());
                    
                    // Diminuer le nombre d'essais si limité
                    if (examen.getNbrEssai() > 0) {
                        resultatExistant.setNbrEssai(essaisRestants - 1);
                        System.out.println("Essais restants après cet essai: " + (essaisRestants - 1));
                    }
                    
                    // Mettre à jour le résultat
                    boolean success = resultatQuizService.modifier(resultatExistant);
                    if (success) {
                        System.out.println("Résultat mis à jour avec succès");
                        return true;
                    } else {
                        System.err.println("Erreur lors de la mise à jour du résultat");
                        return false;
                    }
                } else {
                    System.out.println("Plus d'essais disponibles pour l'utilisateur " + userId + " sur cet examen");
                    return true; // On retourne true car ce n'est pas une erreur
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la sauvegarde des réponses: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Active ou désactive le mode consultation pour afficher les réponses correctes
     * @param modeConsultation true pour activer le mode consultation, false sinon
     */
    public void setModeConsultation(boolean modeConsultation) {
        this.modeConsultation = modeConsultation;
        
        // Si en mode consultation, afficher directement les réponses correctes
        if (modeConsultation && questionToggleGroups != null && !questionToggleGroups.isEmpty()) {
            // Désactiver les boutons de navigation et de validation
            submitButton.setDisable(true);
            
            // Remplacer le texte du header
            examenTitreLabel.setText("Réponses correctes du quiz");
            examenDescriptionLabel.setText("Consultez les réponses correctes pour chaque question.");
            
            // Charger et afficher toutes les questions avec les réponses correctes
            showAllQuestionsWithCorrectAnswers();
        }
    }
    
    /**
     * Affiche toutes les questions avec leurs réponses correctes en mode consultation
     */
    private void showAllQuestionsWithCorrectAnswers() {
        // Vider le conteneur des questions
        questionsContainer.getChildren().clear();
        
        // Parcourir toutes les questions
        for (Map.Entry<Integer, ToggleGroup> entry : questionToggleGroups.entrySet()) {
            int questionId = entry.getKey();
            ToggleGroup optionsGroup = entry.getValue();
            
            // Créer un conteneur pour chaque question
            VBox questionBox = new VBox(10);
            questionBox.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-padding: 15;");
            questionBox.setMaxWidth(Double.MAX_VALUE);
            
            // Ajouter le numéro et le texte de la question
            Label questionLabel = new Label("Question " + questionId + ": " + questionService.recupererParId(questionId).getQuestion());
            questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            questionLabel.setWrapText(true);
            
            // Conteneur pour les options de réponse
            VBox optionsBox = new VBox(8);
            
            // Ajouter les options de réponse
            List<Reponse> reponses = allReponses.get(questionId);
            if (reponses != null) {
                for (Reponse reponse : reponses) {
                    // Créer un élément pour chaque réponse
                    HBox optionBox = new HBox(10);
                    optionBox.setAlignment(Pos.CENTER_LEFT);
                    
                    // Indiquer si la réponse est correcte
                    if (reponse.getEtat() == 1) {
                        optionBox.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 4; -fx-padding: 8;");
                        
                        Label checkIcon = new Label("✓");
                        checkIcon.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 16px;");
                        
                        Label optionLabel = new Label(reponse.getReponse());
                        optionLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                        optionLabel.setWrapText(true);
                        
                        optionBox.getChildren().addAll(checkIcon, optionLabel);
                    } else {
                        Label optionLabel = new Label(reponse.getReponse());
                        optionLabel.setStyle("-fx-text-fill: #757575;");
                        optionLabel.setWrapText(true);
                        
                        optionBox.getChildren().add(optionLabel);
                    }
                    
                    optionsBox.getChildren().add(optionBox);
                }
            }
            
            // Ajouter les éléments au conteneur de question
            questionBox.getChildren().addAll(questionLabel, optionsBox);
            
            // Ajouter un séparateur après chaque question (sauf la dernière)
            if (questionToggleGroups.size() > 1) {
                Separator separator = new Separator();
                separator.setPadding(new Insets(10, 0, 10, 0));
                VBox.setMargin(separator, new Insets(10, 0, 10, 0));
                
                questionsContainer.getChildren().addAll(questionBox, separator);
            } else {
                questionsContainer.getChildren().add(questionBox);
            }
        }
    }
    
    /**
     * Sets up the content text area for displaying translated content
     */
    private void setupContentArea() {
        if (contentTextArea != null) {
            contentTextArea.setWrapText(true);
            contentTextArea.setEditable(false);
            contentTextArea.setText("Chargement en cours...");
        }
    }
    
    /**
     * Shows a dialog to select translation language
     */
    private void showLanguageDialog() {
        // Vérifier si une traduction est déjà en cours
        if (examenTitreLabel.getText().equals("Traduction en cours...")) {
            showAlert(Alert.AlertType.INFORMATION, "Traduction en cours", 
                "Une traduction est déjà en cours. Veuillez patienter.");
            return;
        }
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choisir une langue");
        dialog.setHeaderText("Sélectionnez la langue de traduction");
        
        // Set the button types
        ButtonType translateButtonType = new ButtonType("Traduire", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(translateButtonType, ButtonType.CANCEL);
        
        // Create the language selection combo box
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().add("Français (Original)");
        languageCombo.getItems().addAll(languageCodes.keySet());
        languageCombo.setValue("Français (Original)");
        
        // Layout
        VBox content = new VBox(10);
        content.getChildren().add(new Label("Langue:"));
        content.getChildren().add(languageCombo);
        content.setPadding(new Insets(20));
        
        dialog.getDialogPane().setContent(content);
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == translateButtonType) {
                return languageCombo.getValue();
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(language -> {
            try {
                // Désactiver le bouton de traduction pendant le processus
                translateButton.setDisable(true);
                
                // If French is selected, revert to original language
                if ("Français (Original)".equals(language)) {
                    currentLanguage = "fr";
                    // Afficher un indicateur de chargement
                    examenTitreLabel.setText("Restauration...");
                    examenDescriptionLabel.setText("Veuillez patienter");
                    
                    // Réactiver après un court délai
                    Platform.runLater(() -> {
                        // Restaurer directement le titre et la description d'origine avant de recharger les questions
                        if (examen != null) {
                            examenTitreLabel.setText(examen.getTitre());
                            examenDescriptionLabel.setText(examen.getDescription());
                        }
                        
                        // Recharger les questions dans la langue d'origine
                        loadQuestions();
                        
                        // Réactiver le bouton de traduction
                        translateButton.setDisable(false);
                    });
                } else {
                    // For other languages, translate content
                    String languageCode = languageCodes.get(language);
                    if (languageCode != null) {
                        currentLanguage = languageCode;
                        
                        // Afficher un indicateur de chargement dans l'UI
                        examenTitreLabel.setText("Traduction en cours...");
                        examenDescriptionLabel.setText("Veuillez patienter pendant la traduction vers " + language);
                        
                        // Lancer la traduction
                        translateQuizContent(languageCode);
                        
                        // Réactiver le bouton après un délai pour laisser la traduction démarrer
                        new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    Platform.runLater(() -> translateButton.setDisable(false));
                                }
                            },
                            2000 // 2 secondes
                        );
                    } else {
                        // Langue invalide
                        translateButton.setDisable(false);
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Code de langue invalide.");
                    }
                }
            } catch (Exception e) {
                translateButton.setDisable(false);
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Une erreur est survenue lors de la traduction: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handle language change from the language selector
     */
    private void handleLanguageChange() {
        if (languageSelector == null) return;
        
        String selectedLanguage = languageSelector.getValue();
        
        // If French is selected, revert to original language
        if ("Français (Original)".equals(selectedLanguage)) {
            currentLanguage = "fr";
            
            // Restaurer directement le titre et la description d'origine
            if (examen != null) {
                examenTitreLabel.setText(examen.getTitre());
                examenDescriptionLabel.setText(examen.getDescription());
            }
            
            // Recharger les questions
            loadQuestions();
        } else {
            // For other languages, translate content
            String languageCode = languageCodes.get(selectedLanguage);
            if (languageCode != null) {
                currentLanguage = languageCode;
                translateQuizContent(languageCode);
            }
        }
    }
    
    /**
     * Translate quiz content to the selected language
     * @param targetLanguage The language code to translate to
     */
    private void translateQuizContent(String targetLanguage) {
        if ("fr".equals(targetLanguage)) {
            // Si la langue cible est le français, simplement restaurer le contenu original
            Platform.runLater(() -> {
                if (examen != null) {
                    // Restaurer directement le titre et la description originaux
                    examenTitreLabel.setText(examen.getTitre());
                    examenDescriptionLabel.setText(examen.getDescription());
                    System.out.println("Restauration du titre et de la description originaux");
                }
                
                // Restaurer le texte des boutons
                if (backButton != null) backButton.setText("Retour");
                if (submitButton != null) submitButton.setText("Soumettre");
                if (translateButton != null) translateButton.setText("Traduire");
                
                // Réinitialiser l'orientation pour les langues LTR
                resetRTLLayout(false);
                
                // Recharger les questions dans leur version originale
                loadQuestions();
            });
            return;
        }
        
        // Vérifier si la langue est RTL (arabe, hébreu, persan)
        final boolean isRTL = targetLanguage.startsWith("ar") || 
                              targetLanguage.equals("he") || 
                              targetLanguage.equals("fa");
        
        // Show loading indicator
        Platform.runLater(() -> {
            questionsContainer.getChildren().clear();
            Label loadingLabel = new Label("Traduction en cours...");
            loadingLabel.setStyle("-fx-font-style: italic;");
            questionsContainer.getChildren().add(loadingLabel);
            
            // Mettre à jour l'UI immédiatement pour indiquer que la traduction est en cours
            examenTitreLabel.setText("Traduction en cours...");
            examenDescriptionLabel.setText("Veuillez patienter");
            
            // Configurer l'orientation RTL si nécessaire
            resetRTLLayout(isRTL);
        });
        
        // Use a background thread for translation
        CompletableFuture.runAsync(() -> {
            try {
                // Vérifier si l'examen est disponible
                if (examen == null) {
                    throw new IOException("Aucun examen n'est chargé");
                }
                
                final String examTitle = examen.getTitre();
                final String examDescription = examen.getDescription();
                
                // Translate exam title and description with error handling
                final String translatedTitle;
                try {
                    translatedTitle = translationService.translate(examTitle, targetLanguage, "fr");
                } catch (Exception e) {
                    System.err.println("Erreur de traduction du titre: " + e.getMessage());
                    throw new IOException("Erreur de traduction du titre: " + e.getMessage());
                }
                
                final String translatedDescription;
                try {
                    translatedDescription = translationService.translate(examDescription, targetLanguage, "fr");
                } catch (Exception e) {
                    System.err.println("Erreur de traduction de la description: " + e.getMessage());
                    throw new IOException("Erreur de traduction de la description: " + e.getMessage());
                }
                
                // Traduire les textes des boutons
                final String translatedBackButton = backButton != null ? 
                    translationService.translate("Retour", targetLanguage, "fr") : "";
                final String translatedSubmitButton = submitButton != null ? 
                    translationService.translate("Soumettre", targetLanguage, "fr") : "";
                final String translatedTranslateButton = translateButton != null ? 
                    translationService.translate("Traduire", targetLanguage, "fr") : "";
                
                // Update UI with translated exam info - ensure this happens
                final String finalTitle = translatedTitle;
                final String finalDescription = translatedDescription;
                
                Platform.runLater(() -> {
                    try {
                        examenTitreLabel.setText(finalTitle);
                        examenDescriptionLabel.setText(finalDescription);
                        
                        // Mettre à jour les boutons traduits
                        if (backButton != null) backButton.setText(translatedBackButton);
                        if (submitButton != null) submitButton.setText(translatedSubmitButton);
                        if (translateButton != null) translateButton.setText(translatedTranslateButton);
                        
                        System.out.println("Éléments d'en-tête traduits: " + finalTitle);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la mise à jour de l'interface d'en-tête: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                
                // Get questions for translation
                List<Question> questions;
                if (useDemo && !demoQuestions.isEmpty()) {
                    questions = demoQuestions;
                } else {
                    questions = questionService.recupererParExamen(examen.getId());
                }
                
                // Translate each question and its answers
                Map<Integer, String> translatedQuestions = new HashMap<>();
                Map<Integer, Map<Integer, String>> translatedAnswers = new HashMap<>();
                
                for (Question question : questions) {
                    // Translate question text
                    String translatedQuestionText = translationService.translate(
                            question.getQuestion(), targetLanguage, "fr");
                    translatedQuestions.put(question.getId(), translatedQuestionText);
                    
                    // Get and translate answers
                    List<Reponse> reponses;
                    if (useDemo) {
                        reponses = new ArrayList<>();
                        for (Reponse r : demoReponses) {
                            if (r.getQuestions_id() == question.getId()) {
                                reponses.add(r);
                            }
                        }
                    } else {
                        reponses = reponseService.recupererParQuestion(question.getId());
                    }
                    
                    Map<Integer, String> answersMap = new HashMap<>();
                    for (Reponse reponse : reponses) {
                        String translatedAnswerText = translationService.translate(
                                reponse.getReponse(), targetLanguage, "fr");
                        answersMap.put(reponse.getId(), translatedAnswerText);
                    }
                    
                    translatedAnswers.put(question.getId(), answersMap);
                }
                
                // Final update of UI with translated content
                Platform.runLater(() -> {
                    try {
                        // Double check that the title and description are translated
                        if (examenTitreLabel.getText().equals("Traduction en cours...")) {
                            examenTitreLabel.setText(finalTitle);
                        }
                        
                        if (examenDescriptionLabel.getText().equals("Veuillez patienter")) {
                            examenDescriptionLabel.setText(finalDescription);
                        }
                        
                        // Clear container
                        questionsContainer.getChildren().clear();
                        
                        // Rebuild interface with translated content - with question numbers
                        int questionNumber = 1;
                        for (Question question : questions) {
                            // Create question card with translated text and sequential numbering
                            VBox questionCard = createTranslatedQuestionCard(
                                    question, 
                                    translatedQuestions.get(question.getId()),
                                    translatedAnswers.get(question.getId()),
                                    questionNumber);
                            
                            questionsContainer.getChildren().add(questionCard);
                            questionNumber++;
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la mise à jour finale de l'interface: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                // Show error in UI
                Platform.runLater(() -> {
                    try {
                        questionsContainer.getChildren().clear();
                        Label errorLabel = new Label("Erreur de traduction: " + e.getMessage());
                        errorLabel.setStyle("-fx-text-fill: red;");
                        questionsContainer.getChildren().add(errorLabel);
                        
                        // Reset header elements to original
                        if (examen != null) {
                            examenTitreLabel.setText(examen.getTitre());
                            examenDescriptionLabel.setText(examen.getDescription());
                        } else {
                            examenTitreLabel.setText("Erreur");
                            examenDescriptionLabel.setText("Impossible de charger l'examen");
                        }
                        
                        // Réinitialiser l'orientation pour les langues LTR
                        resetRTLLayout(false);
                        
                        // Reload original content
                        currentLanguage = "fr";
                        loadQuestions();
                    } catch (Exception ex) {
                        System.err.println("Erreur lors de l'affichage de l'erreur: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            }
        }, executorService);
    }
    
    /**
     * Configure l'orientation de l'interface pour les langues RTL (comme l'arabe)
     * @param isRTL true si la langue est RTL (de droite à gauche), false sinon
     */
    private void resetRTLLayout(boolean isRTL) {
        try {
            javafx.geometry.NodeOrientation orientation = isRTL ? 
                javafx.geometry.NodeOrientation.RIGHT_TO_LEFT : 
                javafx.geometry.NodeOrientation.LEFT_TO_RIGHT;
            
            // Appliquer l'orientation sur les éléments principaux de l'interface
            if (examenTitreLabel != null) examenTitreLabel.setNodeOrientation(orientation);
            if (examenDescriptionLabel != null) examenDescriptionLabel.setNodeOrientation(orientation);
            if (questionsContainer != null) questionsContainer.setNodeOrientation(orientation);
            
            // Appliquer un style spécifique pour les langues RTL
            if (isRTL) {
                if (examenTitreLabel != null) {
                    examenTitreLabel.setStyle("-fx-text-alignment: right; -fx-alignment: baseline-right; -fx-font-weight: bold; -fx-font-size: 18;");
                }
                if (examenDescriptionLabel != null) {
                    examenDescriptionLabel.setStyle("-fx-text-alignment: right; -fx-alignment: baseline-right;");
                }
            } else {
                if (examenTitreLabel != null) {
                    examenTitreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18;");
                }
                if (examenDescriptionLabel != null) {
                    examenDescriptionLabel.setStyle("");
                }
            }
            
            System.out.println("Orientation de l'interface mise à jour: " + (isRTL ? "RTL" : "LTR"));
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de l'orientation de l'interface: " + e.getMessage());
        }
    }
    
    /**
     * Creates a question card with translated content
     */
    private VBox createTranslatedQuestionCard(Question question, String translatedQuestionText, 
                                             Map<Integer, String> translatedAnswers,
                                             int questionNumber) {
        VBox questionCard = new VBox();
        questionCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); -fx-padding: 20;");
        questionCard.setSpacing(15);
        
        // En-tête de la question avec titre et bouton de lecture
        HBox questionHeader = new HBox();
        questionHeader.setAlignment(Pos.CENTER_LEFT);
        questionHeader.setSpacing(10);
        
        // Traduire le préfixe "Question" si nécessaire
        String questionLabelText;
        try {
            if (!currentLanguage.equals("fr")) {
                String questionPrefix = "Question";
                String translatedPrefix = translationService.translate(questionPrefix, currentLanguage, "fr");
                questionLabelText = translatedPrefix + " " + questionNumber + ": " + translatedQuestionText;
            } else {
                questionLabelText = "Question " + questionNumber + ": " + translatedQuestionText;
            }
        } catch (Exception e) {
            // En cas d'erreur, utiliser le format standard
            questionLabelText = "Question " + questionNumber + ": " + translatedQuestionText;
        }
        
        // Titre de la question traduit
        Label questionLabel = new Label(questionLabelText);
        questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        questionLabel.setWrapText(true);
        HBox.setHgrow(questionLabel, javafx.scene.layout.Priority.ALWAYS);
        
        // Bouton de lecture
        Button speakButton = new Button();
        speakButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        try {
            ImageView speakIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/volume-up.png")));
            speakIcon.setFitHeight(16);
            speakIcon.setFitWidth(16);
            speakButton.setGraphic(speakIcon);
        } catch (Exception e) {
            // Si l'icône n'est pas trouvée, utiliser du texte
            speakButton.setText("🔊");
        }
        
        // Traduire le tooltip
        String tooltipText = "Écouter la question et les réponses";
        try {
            tooltipText = translationService.translate(tooltipText, currentLanguage, "fr");
        } catch (Exception e) {
            System.err.println("Erreur lors de la traduction du tooltip: " + e.getMessage());
        }
        speakButton.setTooltip(new Tooltip(tooltipText));
        
        // Support spécifique pour l'arabe et les langues RTL
        if (currentLanguage.startsWith("ar") || currentLanguage.equals("he") || currentLanguage.equals("fa")) {
            // Définir la direction du texte de droite à gauche
            questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-alignment: right; -fx-alignment: baseline-right;");
            questionHeader.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        }
        
        // Ajouter le gestionnaire d'événements pour la lecture vocale
        speakButton.setOnAction(e -> {
            // Collecter les réponses traduites
            List<Reponse> reponses;
            if (useDemo) {
                reponses = new ArrayList<>();
                for (Reponse r : demoReponses) {
                    if (r.getQuestions_id() == question.getId()) {
                        reponses.add(r);
                    }
                }
            } else {
                reponses = reponseService.recupererParQuestion(question.getId());
            }
            
            StringBuilder textToSpeak = new StringBuilder();
            textToSpeak.append("Question numéro ").append(questionNumber).append(": ").append(translatedQuestionText);
            textToSpeak.append(". Les réponses proposées sont: ");
            
            for (int i = 0; i < reponses.size(); i++) {
                Reponse reponse = reponses.get(i);
                // Utiliser le texte traduit si disponible
                String answerText = translatedAnswers.get(reponse.getId());
                if (answerText == null) {
                    answerText = reponse.getReponse();
                }
                
                textToSpeak.append(answerText);
                if (i < reponses.size() - 1) {
                    textToSpeak.append(", ");
                }
            }
            
            speak(textToSpeak.toString());
        });
        
        // Ajouter les éléments à l'en-tête
        questionHeader.getChildren().addAll(questionLabel, speakButton);
        questionCard.getChildren().add(questionHeader);
        
        // Container pour les options de réponses
        VBox reponsesContainer = new VBox();
        reponsesContainer.setSpacing(8);
        
        // Support RTL pour les langues comme l'arabe
        if (currentLanguage.startsWith("ar") || currentLanguage.equals("he") || currentLanguage.equals("fa")) {
            reponsesContainer.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        }
        
        // Use existing toggle group or create new one
        ToggleGroup optionsGroup = questionToggleGroups.get(question.getId());
        if (optionsGroup == null) {
            optionsGroup = new ToggleGroup();
            questionToggleGroups.put(question.getId(), optionsGroup);
        }
        
        // Get responses for this question
        List<Reponse> reponses;
        if (useDemo) {
            reponses = new ArrayList<>();
            for (Reponse r : demoReponses) {
                if (r.getQuestions_id() == question.getId()) {
                    reponses.add(r);
                }
            }
        } else {
            reponses = reponseService.recupererParQuestion(question.getId());
        }
        
        // Store all responses for this question
        allReponses.put(question.getId(), reponses);
        
        // Create or update radio buttons list
        List<RadioButton> radioButtons = questionOptions.get(question.getId());
        if (radioButtons == null) {
            radioButtons = new ArrayList<>();
            questionOptions.put(question.getId(), radioButtons);
        } else {
            radioButtons.clear();
        }
        
        if (reponses.isEmpty()) {
            // Traduire le message d'absence de réponses
            String emptyMessage = "Aucune réponse disponible pour cette question.";
            try {
                emptyMessage = translationService.translate(emptyMessage, currentLanguage, "fr");
            } catch (Exception e) {
                // En cas d'erreur, utiliser le message par défaut
                System.err.println("Erreur lors de la traduction du message d'absence de réponses: " + e.getMessage());
            }
            
            Label emptyLabel = new Label(emptyMessage);
            emptyLabel.setStyle("-fx-padding: 10; -fx-font-style: italic; -fx-text-fill: #757575;");
            reponsesContainer.getChildren().add(emptyLabel);
        } else {
            for (Reponse reponse : reponses) {
                // Get translated answer text if available, otherwise use original
                String answerText = translatedAnswers.get(reponse.getId());
                if (answerText == null) {
                    answerText = reponse.getReponse();
                }
                
                RadioButton optionButton = createOptionButton(reponse.getId(), answerText, optionsGroup);
                reponsesContainer.getChildren().add(optionButton);
                radioButtons.add(optionButton);
            }
        }
        
        questionCard.getChildren().add(reponsesContainer);
        
        return questionCard;
    }
    
    /**
     * Lit un texte à haute voix
     * @param text Texte à lire
     */
    private void speak(String text) {
        // Arrêter toute lecture en cours
        stopSpeaking();
        
        // Utiliser un thread séparé pour ne pas bloquer l'interface
        speechExecutor.submit(() -> {
            try {
                isSpeaking = true;
                
                // Texte à prononcer (potentiellement modifié)
                String textToSpeak;
                
                // Vérifier si le texte nécessite une traduction des phrases standards
                if (!currentLanguage.equals("fr") && text.contains("Question numéro") && text.contains("Les réponses proposées sont")) {
                    // Traduire les phrases standards
                    String questionPrefix = "Question numéro";
                    String answersPrefix = "Les réponses proposées sont";
                    
                    try {
                        String translatedQuestionPrefix = translationService.translate(questionPrefix, currentLanguage, "fr");
                        String translatedAnswersPrefix = translationService.translate(answersPrefix, currentLanguage, "fr");
                        
                        // Remplacer les phrases standards par leurs versions traduites
                        textToSpeak = text.replace(questionPrefix, translatedQuestionPrefix)
                                  .replace(answersPrefix, translatedAnswersPrefix);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la traduction des phrases standards: " + e.getMessage());
                        // Continuer avec le texte original en cas d'erreur
                        textToSpeak = text;
                    }
                } else {
                    textToSpeak = text;
                }
                
                // Encoder le texte pour l'URL
                String encodedText = java.net.URLEncoder.encode(textToSpeak, "UTF-8");
                
                // Déterminer le code de langue pour la synthèse vocale
                // Certaines langues ont des codes spécifiques pour la synthèse vocale
                String ttsLang;
                
                switch (currentLanguage) {
                    case "ar":
                        ttsLang = "ar-sa"; // Arabe (Arabie Saoudite) pour une meilleure prise en charge
                        break;
                    case "zh-CN":
                        ttsLang = "zh-cn"; // Chinois simplifié
                        break;
                    case "zh-TW":
                        ttsLang = "zh-tw"; // Chinois traditionnel
                        break;
                    case "ja":
                        ttsLang = "ja-jp"; // Japonais
                        break;
                    case "ru":
                        ttsLang = "ru-ru"; // Russe
                        break;
                    default:
                        ttsLang = currentLanguage; // Utiliser le code de langue tel quel
                }
                
                // Si la langue est "fr", on utilise "fr-fr" pour une meilleure qualité
                if (ttsLang.equals("fr")) {
                    ttsLang = "fr-fr";
                }
                
                System.out.println("Lecture du texte avec la langue: " + ttsLang);
                
                // Utiliser Google Text-to-Speech pour obtenir le fichier audio
                String ttsUrl = "https://translate.google.com/translate_tts?ie=UTF-8&tl=" + 
                               ttsLang + "&client=tw-ob&q=" + encodedText;
                
                System.out.println("URL TTS: " + ttsUrl);
                
                // Préparer le fichier temporaire pour stocker l'audio
                currentAudioFile = tempDir + "speech_" + System.currentTimeMillis() + ".mp3";
                
                System.out.println("Fichier audio temporaire: " + currentAudioFile);

                // Télécharger le fichier audio depuis Google TTS
                downloadFile(ttsUrl, currentAudioFile);
                
                // Convertir le chemin du fichier en URI
                File audioFile = new File(currentAudioFile);
                Media sound = new Media(audioFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(sound);
                
                // Configurer les propriétés du lecteur audio
                mediaPlayer.setOnError(() -> {
                    System.err.println("Erreur MediaPlayer: " + mediaPlayer.getError());
                    finishPlayback();
                    mediaPlayer.dispose();
                });
                
                mediaPlayer.setOnReady(() -> {
                    System.out.println("MediaPlayer prêt à jouer");
                    mediaPlayer.play();
                });
                
                // Écouter les événements du lecteur
                mediaPlayer.setOnEndOfMedia(() -> {
                    System.out.println("Lecture terminée");
                    finishPlayback();
                    mediaPlayer.dispose();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erreur lors de la préparation de l'audio: " + e.getMessage());
                finishPlayback();
                
                // Afficher un message d'erreur
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Impossible de lire le texte: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * Termine proprement la lecture
     */
    private void finishPlayback() {
        Platform.runLater(() -> {
            isSpeaking = false;
            
            // Ne pas supprimer immédiatement le fichier pour permettre la lecture
            // Le fichier sera supprimé au nettoyage ou à la prochaine lecture
        });
    }
    
    /**
     * Télécharge un fichier depuis une URL
     * @param fileURL URL du fichier à télécharger
     * @param destinationPath Emplacement où sauvegarder le fichier
     * @throws IOException En cas d'erreur de téléchargement
     */
    private void downloadFile(String fileURL, String destinationPath) throws IOException {
        URL url = new URL(fileURL);
        
        try (InputStream in = url.openStream();
             FileOutputStream out = new FileOutputStream(destinationPath)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
    
    /**
     * Nettoie le fichier audio temporaire
     */
    private void cleanupAudioFile() {
        if (currentAudioFile != null) {
            try {
                File file = new File(currentAudioFile);
                if (file.exists()) {
                    // Essayer de supprimer, mais ne pas insister si échec
                    // (fichier peut être encore utilisé)
                    file.delete();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression du fichier audio: " + e.getMessage());
            }
        }
    }
    
    /**
     * Arrête la lecture vocale en cours
     */
    private void stopSpeaking() {
        if (isSpeaking) {
            try {
                if (currentSpeechProcess != null) {
                    currentSpeechProcess.destroy();
                    currentSpeechProcess = null;
                }
                
                // Essayer de tuer toute application qui pourrait lire le fichier
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    // Sur Windows, essayer de fermer les lecteurs courants
                    Runtime.getRuntime().exec("taskkill /F /IM wmplayer.exe");
                    Runtime.getRuntime().exec("taskkill /F /IM groove.exe");
                    Runtime.getRuntime().exec("taskkill /F /IM vlc.exe");
                } else if (os.contains("mac")) {
                    // Sur Mac, rien à faire (iTunes ne peut pas être tué facilement)
                } else {
                    // Sur Linux, essayer de tuer les processus audio courants
                    Runtime.getRuntime().exec("killall vlc");
                    Runtime.getRuntime().exec("killall mplayer");
                }
                
                isSpeaking = false;
                
                cleanupAudioFile();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erreur lors de l'arrêt de la lecture: " + e.getMessage());
            }
        }
    }
    
    /**
     * Nettoie les ressources lors de la fermeture
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        if (speechExecutor != null && !speechExecutor.isShutdown()) {
            speechExecutor.shutdown();
        }
        
        stopSpeaking();
        cleanupAudioFile();
    }
}