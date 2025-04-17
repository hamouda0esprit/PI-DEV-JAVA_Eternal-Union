package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import entite.Examen;
import entite.Question;
import entite.Reponse;
import service.ExamenService;
import service.GeminiService;
import service.QuestionService;
import service.ReponseService;
import utils.AppConfig;

public class GenerateQuizController implements Initializable {
    @FXML private TextField subjectField;
    @FXML private TextField titreField;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private Spinner<Integer> questionCountSpinner;
    @FXML private TextArea additionalInstructionsArea;
    @FXML private Button generateButton;
    @FXML private Button cancelButton;
    @FXML private VBox previewBox;
    @FXML private Button saveQuizButton;
    
    private String userId;
    private boolean isAdminMode = false;
    private GeminiService geminiService;
    private ExamenService examenService;
    private QuestionService questionService;
    private ReponseService reponseService;
    private JSONObject quizData;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        geminiService = new GeminiService(AppConfig.getGeminiApiKey());
        examenService = new ExamenService();
        questionService = new QuestionService();
        reponseService = new ReponseService();
        
        // Set up difficulty dropdown
        difficultyComboBox.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        difficultyComboBox.setValue("Moyen");
        
        // Set up question count spinner
        SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5);
        questionCountSpinner.setValueFactory(valueFactory);
        
        // Hide preview and save button initially
        previewBox.setVisible(false);
        saveQuizButton.setVisible(false);
    }
    
    /**
     * Set the fields with data from the previous screen
     * @param titre Title of the exam
     * @param matiere Subject of the exam
     * @param userId User ID
     */
    public void setFields(String titre, String matiere, String userId) {
        this.titreField.setText(titre);
        this.subjectField.setText(matiere);
        this.userId = userId;
    }
    
    /**
     * Set admin mode
     * @param isAdminMode True if admin mode
     */
    public void setAdminMode(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
    }
    
    @FXML
    private void handleGenerateQuiz() {
        String subject = subjectField.getText().trim();
        String titre = titreField.getText().trim();
        String difficulty = difficultyComboBox.getValue();
        int questionCount = questionCountSpinner.getValue();
        String additionalInstructions = additionalInstructionsArea.getText().trim();
        
        if (subject.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le sujet du quiz est requis.");
            return;
        }
        
        if (titre.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre du quiz est requis.");
            return;
        }
        
        // Show loading indicator
        generateButton.setDisable(true);
        generateButton.setText("Génération en cours...");
        
        // Create a new thread for API call to avoid freezing the UI
        new Thread(() -> {
            try {
                quizData = geminiService.generateQuizWithGemini(
                        titre, difficulty, questionCount, additionalInstructions);
                
                javafx.application.Platform.runLater(() -> {
                    displayQuizPreview(quizData);
                    generateButton.setDisable(false);
                    generateButton.setText("Générer Quiz");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Erreur lors de la génération du quiz: " + e.getMessage());
                    generateButton.setDisable(false);
                    generateButton.setText("Générer Quiz");
                });
            }
        }).start();
    }
    
    private void displayQuizPreview(JSONObject quizData) {
        previewBox.getChildren().clear();
        
        // Check if quizData contains questions
        if (!quizData.has("questions")) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Format de réponse invalide: pas de questions.");
            return;
        }
        
        // Add a title
        Label previewTitle = new Label("Aperçu du Quiz");
        previewTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        previewBox.getChildren().add(previewTitle);
        
        // Add a separator
        previewBox.getChildren().add(new Separator());
        
        // Loop through questions and add them to the preview
        JSONArray questions = quizData.getJSONArray("questions");
        for (int i = 0; i < questions.length(); i++) {
            JSONObject questionObj = questions.getJSONObject(i);
            
            VBox questionBox = new VBox(5);
            questionBox.setStyle("-fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-radius: 5px;");
            
            // Question text
            Label questionLabel = new Label((i + 1) + ". " + questionObj.getString("question"));
            questionLabel.setStyle("-fx-font-weight: bold;");
            questionBox.getChildren().add(questionLabel);
            
            // Options
            JSONArray options = questionObj.getJSONArray("options");
            ToggleGroup optionsGroup = new ToggleGroup();
            
            for (int j = 0; j < options.length(); j++) {
                RadioButton option = new RadioButton(options.getString(j));
                option.setToggleGroup(optionsGroup);
                
                // Mark correct answer
                if (j == questionObj.getInt("correctIndex")) {
                    option.setStyle("-fx-text-fill: #008800;");
                }
                
                questionBox.getChildren().add(option);
            }
            
            // Explanation
            if (questionObj.has("explanation")) {
                TitledPane explanationPane = new TitledPane("Explication", 
                        new Label(questionObj.getString("explanation")));
                explanationPane.setExpanded(false);
                questionBox.getChildren().add(explanationPane);
            }
            
            previewBox.getChildren().add(questionBox);
            
            // Add some spacing
            if (i < questions.length() - 1) {
                previewBox.getChildren().add(new Separator());
            }
        }
        
        // Show save button
        previewBox.setVisible(true);
        saveQuizButton.setVisible(true);
    }
    
    @FXML
    private void handleSaveQuiz() {
        if (quizData == null || !quizData.has("questions")) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Pas de données de quiz à enregistrer.");
            return;
        }
        
        try {
            // Create exam
            Examen examen = new Examen();
            examen.setTitre(titreField.getText());
            examen.setMatiere(subjectField.getText());
            examen.setDescription("Quiz généré par IA sur: " + titreField.getText());
            examen.setDate(new Date());
            examen.setDuree(30); // Default duration
            examen.setType("Quiz généré par IA");
            examen.setNbrEssai(3); // Default attempts
            examen.setNote(0.0); // Default score
            
            // Set user ID
            if (userId != null && !userId.isEmpty()) {
                try {
                    examen.setIdUser(Integer.parseInt(userId));
                } catch (NumberFormatException e) {
                    System.err.println("Erreur de conversion de l'ID utilisateur: " + e.getMessage());
                }
            }
            
            // Save exam
            boolean examSuccess = examenService.ajouter(examen);
            
            if (!examSuccess) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement de l'examen.");
                return;
            }
            
            // Get the ID of the saved exam
            List<Examen> allExams = examenService.recupererTout();
            Examen savedExam = null;
            
            for (Examen e : allExams) {
                if (e.getTitre().equals(examen.getTitre()) && 
                    e.getMatiere().equals(examen.getMatiere()) &&
                    e.getType().equals(examen.getType())) {
                    savedExam = e;
                    break;
                }
            }
            
            if (savedExam == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de récupérer l'examen enregistré.");
                return;
            }
            
            // Create questions and answers
            JSONArray questions = quizData.getJSONArray("questions");
            for (int i = 0; i < questions.length(); i++) {
                JSONObject questionObj = questions.getJSONObject(i);
                
                Question question = new Question();
                question.setQuestion(questionObj.getString("question"));
                question.setNbr_points(1); // Default points
                question.setExamen_id(savedExam.getId());
                
                boolean questionSuccess = questionService.ajouter(question);
                
                if (!questionSuccess) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Erreur lors de l'enregistrement de la question " + (i + 1));
                    continue;
                }
                
                // Get the saved question ID
                int questionId = questionService.getLastInsertedId();
                
                // Create options
                JSONArray options = questionObj.getJSONArray("options");
                int correctIndex = questionObj.getInt("correctIndex");
                
                for (int j = 0; j < options.length(); j++) {
                    Reponse reponse = new Reponse();
                    reponse.setReponse(options.getString(j));
                    reponse.setEtat(j == correctIndex ? 1 : 0); // 1 = correct, 0 = incorrect
                    reponse.setQuestions_id(questionId);
                    
                    reponseService.ajouter(reponse);
                }
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Quiz enregistré avec succès ! ID: " + savedExam.getId());
            
            // Navigate to the questions management page or back to exams list
            navigateBack();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de l'enregistrement du quiz: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        navigateBack();
    }
    
    private void navigateBack() {
        try {
            Parent root;
            if (isAdminMode) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminPanel.fxml"));
                root = loader.load();
                
                AdminPanelController controller = loader.getController();
                if (controller != null && userId != null) {
                    controller.setUserId(userId);
                }
            } else {
                root = FXMLLoader.load(getClass().getResource("/view/ExamenTableView.fxml"));
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers la page précédente");
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