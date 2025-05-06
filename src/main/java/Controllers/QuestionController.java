package Controllers;

import entite.Examen;
import entite.Question;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.ExamenService;
import service.QuestionService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuestionController implements Initializable {
    
    @FXML private Label examenTitreLabel;
    @FXML private Label examenDescriptionLabel;
    @FXML private TextArea questionArea;
    @FXML private TextField nbPointsField;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private ToggleButton reponsesToggle;
    
    @FXML private VBox questionsListContainer;
    @FXML private HBox questionTemplate;
    @FXML private Label templateQuestionLabel;
    @FXML private Button templateEditButton;
    @FXML private Button templateDeleteButton;
    
    private QuestionService questionService;
    private ExamenService examenService;
    private Examen examen;
    private ObservableList<Question> questionsList;
    private int questionCounter = 1;
    private Question selectedQuestion;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        questionService = new QuestionService();
        examenService = new ExamenService();
        questionsList = FXCollections.observableArrayList();
        
        // S'assurer que le template est invisible
        if (questionTemplate != null) {
            questionTemplate.setVisible(false);
            questionTemplate.setManaged(false);
        }
        
        // Supprimer tout exemple de la liste
        if (questionsListContainer != null && questionsListContainer.getChildren().size() > 0) {
            questionsListContainer.getChildren().clear();
        }
    }
    
    public void setExamen(Examen examen) {
        this.examen = examen;
        
        if (examen != null) {
            examenTitreLabel.setText(examen.getTitre());
            examenDescriptionLabel.setText(examen.getDescription());
            
            // Charger les questions existantes pour cet examen
            loadQuestions();
        }
    }
    
    public void setExamenId(int examenId) {
        if (examenService != null) {
            this.examen = examenService.recupererParId(examenId);
            if (this.examen != null) {
                setExamen(this.examen);
            }
        }
    }
    
    public void setExamenInfo(String titre, String description) {
        examenTitreLabel.setText(titre);
        examenDescriptionLabel.setText(description);
    }
    
    private void loadQuestions() {
        if (examen != null) {
            List<Question> questions = questionService.recupererParExamenId(examen.getId());
            questionsList.clear();
            questionsList.addAll(questions);
            
            // Vider le conteneur avant d'ajouter les questions
            questionsListContainer.getChildren().clear();
            questionCounter = 1;
            
            // Ajouter chaque question comme une ligne dans la liste
            for (Question question : questionsList) {
                addQuestionRow(question);
            }
        }
    }
    
    private void addQuestionRow(Question question) {
        try {
            // Créer une nouvelle ligne de question basée sur le modèle
            HBox questionRow = new HBox();
            questionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            questionRow.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-padding: 15;");
            
            // Label pour afficher la question
            Label questionLabel = new Label("Question " + questionCounter + ": " + question.getQuestion());
            questionLabel.setWrapText(true);
            HBox.setHgrow(questionLabel, javafx.scene.layout.Priority.ALWAYS);
            
            // Pane spacer pour pousser les boutons vers la droite
            javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            // Conteneur pour les boutons
            HBox buttonsBox = new HBox(8);
            buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            
            // Bouton d'édition
            Button editButton = new Button();
            editButton.setStyle("-fx-background-color: #2196F3; -fx-background-radius: 4;");
            Label editLabel = new Label("✎");
            editLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            editButton.setGraphic(editLabel);
            editButton.setOnAction(e -> handleEdit(question));
            
            // Bouton de suppression
            Button deleteButton = new Button();
            deleteButton.setStyle("-fx-background-color: #F44336; -fx-background-radius: 4;");
            Label deleteLabel = new Label("🗑");
            deleteLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            deleteButton.setGraphic(deleteLabel);
            deleteButton.setOnAction(e -> handleDelete(question));
            
            // Bouton pour ajouter des réponses
            Button addResponses = new Button();
            addResponses.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 4;");
            Label responsesLabel = new Label("📝");
            responsesLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            addResponses.setGraphic(responsesLabel);
            addResponses.setOnAction(e -> openReponsesView(question));
            
            // Assembler la ligne
            buttonsBox.getChildren().addAll(addResponses, editButton, deleteButton);
            questionRow.getChildren().addAll(questionLabel, spacer, buttonsBox);
            
            // Ajouter à la liste
            questionsListContainer.getChildren().add(questionRow);
            questionCounter++;
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'affichage de la question");
        }
    }
    
    @FXML
    private void handleSaveAndAdd() {
        if (validateInputs()) {
            if (saveQuestion()) {
                // Vider les champs pour une nouvelle question
                clearFields();
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        // Rediriger vers la liste des examens
        navigateToExamenList();
    }
    
    @FXML
    private void handleReponsesToggle() {
        // Ouvrir la vue des réponses avec la question sélectionnée
        if (selectedQuestion != null) {
            openReponsesView(selectedQuestion);
        } else if (!questionsList.isEmpty()) {
            // Si aucune question n'est sélectionnée mais qu'il y a des questions, on prend la première
            openReponsesView(questionsList.get(0));
        } else {
            // Si pas de question, demander à l'utilisateur d'en créer une d'abord
            showAlert(Alert.AlertType.INFORMATION, "Information", "Veuillez d'abord créer une question avant d'ajouter des réponses.");
        }
    }
    
    @FXML
    private void handleConsulterQuizToggle() {
        try {
            // Naviguer vers la vue de consultation du quiz
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConsulterQuizView.fxml"));
            Parent root = loader.load();
            
            ConsulterQuizController controller = loader.getController();
            if (controller != null && examen != null) {
                controller.setExamenId(examen.getId());
                controller.setExamenInfo(examen.getTitre(), examen.getDescription());
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers la vue de consultation du quiz: " + e.getMessage());
        }
    }
    
    private void openReponsesView(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ReponseView.fxml"));
            Parent root = loader.load();
            
            ReponseController controller = loader.getController();
            controller.setQuestion(question);
            controller.setExamenInfo(examen.getId(), examen.getTitre(), examen.getDescription());
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture de la vue des réponses");
        }
    }
    
    private void handleEdit(Question question) {
        // Remplir les champs avec les données de la question sélectionnée
        questionArea.setText(question.getQuestion());
        nbPointsField.setText(String.valueOf(question.getNbr_points()));
        selectedQuestion = question;
        
        // Modifier le comportement du bouton save pour mettre à jour la question
        saveButton.setOnAction(event -> {
            if (validateInputs()) {
                question.setQuestion(questionArea.getText());
                try {
                    question.setNbr_points(Integer.parseInt(nbPointsField.getText()));
                    
                    if (questionService.modifier(question)) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Question modifiée avec succès !");
                        loadQuestions();
                        clearFields();
                        
                        // Remettre le gestionnaire d'événements par défaut
                        saveButton.setOnAction(e -> handleSaveAndAdd());
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification de la question");
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre de points doit être un nombre entier");
                }
            }
        });
    }
    
    private void handleDelete(Question question) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la question");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette question ?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (questionService.supprimer(question.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Question supprimée avec succès !");
                loadQuestions();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la question");
            }
        }
    }
    
    private boolean saveQuestion() {
        try {
            Question question = new Question();
            question.setExamen_id(examen.getId());
            question.setQuestion(questionArea.getText());
            question.setNbr_points(Integer.parseInt(nbPointsField.getText()));
            
            if (questionService.ajouter(question)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Question ajoutée avec succès !");
                loadQuestions();
                return true;
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de la question");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre de points doit être un nombre entier");
            return false;
        }
    }
    
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (questionArea.getText().trim().isEmpty()) {
            errorMessage.append("La question est requise\n");
        }
        
        if (nbPointsField.getText().trim().isEmpty()) {
            errorMessage.append("Le nombre de points est requis\n");
        } else {
            try {
                int points = Integer.parseInt(nbPointsField.getText().trim());
                if (points <= 0) {
                    errorMessage.append("Le nombre de points doit être supérieur à 0\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Le nombre de points doit être un nombre entier\n");
            }
        }
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    private void clearFields() {
        questionArea.clear();
        nbPointsField.clear();
        questionArea.requestFocus();
        selectedQuestion = null;
    }
    
    private void navigateToExamenList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/ExamenTableView.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers la liste des examens");
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