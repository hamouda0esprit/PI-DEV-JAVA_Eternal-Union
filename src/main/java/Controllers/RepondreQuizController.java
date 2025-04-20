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
    private ResultatQuizService resultatQuizService;
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
        resultatQuizService = new ResultatQuizService();
        
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
            reponses = reponseService.recupererParQuestion(question.getId());
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
} 