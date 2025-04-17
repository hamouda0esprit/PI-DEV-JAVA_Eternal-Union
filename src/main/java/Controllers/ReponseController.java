package Controllers;

import entite.Question;
import entite.Reponse;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import service.ReponseService;
import service.QuestionService;

public class ReponseController implements Initializable {

    @FXML private TextField reponseField;
    @FXML private CheckBox estCorrecteCheckBox;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private Button ajouterReponseBtn;
    @FXML private Button saveAllButton;
    @FXML private ToggleButton questionsToggle;
    @FXML private ToggleButton consulterToggle;
    @FXML private Label questionLabel;
    @FXML private Label examenTitreLabel;
    @FXML private Label examenDescriptionLabel;
    @FXML private VBox reponsesContainer;
    @FXML private VBox questionCard;
    @FXML private VBox ajouterReponseDialog;

    private ReponseService reponseService;
    private Question selectedQuestion;
    private List<Reponse> reponsesList;
    private int examenId;
    private String examenTitre;
    private String examenDescription;
    private List<Question> questionsList;
    private int currentQuestionIndex = 0;
    private QuestionService questionService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reponseService = new ReponseService();
        questionService = new QuestionService();
        
        // Cache la boîte de dialogue pour ajouter une réponse
        ajouterReponseDialog.setVisible(false);
        ajouterReponseDialog.setManaged(false);
        
        Platform.runLater(() -> {
            updateExamenInfo();
        });
    }

    public void setQuestion(Question question) {
        this.selectedQuestion = question;
        if (question != null) {
            // Charger toutes les questions de l'examen
            loadQuestionsForExamen(question.getExamen_id());
            // Trouver l'index de la question sélectionnée
            findQuestionIndex(question.getId());
            updateQuestionInfo();
            loadReponses();
        }
    }
    
    private void loadQuestionsForExamen(int examenId) {
        this.examenId = examenId;
        questionsList = questionService.recupererParExamen(examenId);
    }
    
    private void findQuestionIndex(int questionId) {
        if (questionsList != null) {
            for (int i = 0; i < questionsList.size(); i++) {
                if (questionsList.get(i).getId() == questionId) {
                    currentQuestionIndex = i;
                    break;
                }
            }
        }
    }

    public void setExamenInfo(int examenId, String titre, String description) {
        this.examenId = examenId;
        this.examenTitre = titre;
        this.examenDescription = description;
        updateExamenInfo();
        
        // Charger toutes les questions de l'examen
        loadQuestionsForExamen(examenId);
        
        // Si des questions existent, sélectionner la première
        if (questionsList != null && !questionsList.isEmpty()) {
            selectedQuestion = questionsList.get(0);
            currentQuestionIndex = 0;
            updateQuestionInfo();
            loadReponses();
        }
    }
    
    private void updateExamenInfo() {
        if (examenTitre != null) {
            examenTitreLabel.setText(examenTitre);
            examenDescriptionLabel.setText(examenDescription);
        }
    }

    private void updateQuestionInfo() {
        if (selectedQuestion != null) {
            int totalQuestions = (questionsList != null) ? questionsList.size() : 0;
            questionLabel.setText("Question " + selectedQuestion.getId() + " (" + (currentQuestionIndex + 1) + "/" + totalQuestions + "): " + selectedQuestion.getQuestion());
        } else {
            questionLabel.setText("Aucune question sélectionnée");
        }
    }
    
    @FXML
    private void handleAjouterReponse() {
        // Réinitialise les champs du formulaire
        reponseField.clear();
        estCorrecteCheckBox.setSelected(false);
        
        // Affiche la boîte de dialogue
        ajouterReponseDialog.setVisible(true);
        ajouterReponseDialog.setManaged(true);
        
        // Focus sur le champ de texte
        Platform.runLater(() -> reponseField.requestFocus());
    }
    
    @FXML
    private void handleSaveReponse() {
        if (selectedQuestion == null) {
            showAlert("Erreur", "Aucune question sélectionnée", Alert.AlertType.ERROR);
            return;
        }

        String reponseText = reponseField.getText().trim();
        if (reponseText.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer une réponse", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 1 pour correcte, 0 pour incorrecte
            int etat = estCorrecteCheckBox.isSelected() ? 1 : 0;
            
            Reponse reponse = new Reponse();
            reponse.setQuestions_id(selectedQuestion.getId());
            reponse.setReponse(reponseText);
            reponse.setEtat(etat);
            
            if (reponseService.ajouter(reponse)) {
                // Cache la boîte de dialogue
                ajouterReponseDialog.setVisible(false);
                ajouterReponseDialog.setManaged(false);
                
                // Recharger la liste des réponses
                loadReponses();
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout de la réponse", Alert.AlertType.ERROR);
            }
            
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout de la réponse: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSaveAll() {
        if (reponsesList != null && !reponsesList.isEmpty()) {
            showAlert("Succès", "Toutes les réponses ont été enregistrées avec succès", Alert.AlertType.INFORMATION);
            goBack();
        } else {
            showAlert("Attention", "Aucune réponse à enregistrer. Veuillez ajouter au moins une réponse.", Alert.AlertType.WARNING);
        }
    }

    private void loadReponses() {
        if (selectedQuestion == null) return;
        
        try {
            reponsesContainer.getChildren().clear();
            reponsesList = reponseService.recupererParQuestion(selectedQuestion.getId());
            
            if (reponsesList.isEmpty()) {
                Label emptyLabel = new Label("Aucune réponse ajoutée pour cette question");
                emptyLabel.setStyle("-fx-padding: 10; -fx-font-style: italic; -fx-text-fill: #757575;");
                reponsesContainer.getChildren().add(emptyLabel);
                return;
            }
            
            for (Reponse reponse : reponsesList) {
                reponsesContainer.getChildren().add(createReponseItem(reponse));
            }
            
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des réponses: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private HBox createReponseItem(Reponse reponse) {
        HBox reponseItem = new HBox();
        reponseItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Fond de couleur verte pour les réponses correctes, gris clair pour les incorrectes
        String backgroundColor = reponse.getEtat() == 1 ? "#E8F5E9" : "#f8f8f8";
        reponseItem.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 4; -fx-padding: 10;");
        
        // Icône de statut (correct ou incorrect)
        Label statusIcon = new Label(reponse.getEtat() == 1 ? "✓" : "○");
        statusIcon.setTextFill(reponse.getEtat() == 1 ? Color.valueOf("#4CAF50") : Color.valueOf("#757575"));
        statusIcon.setStyle(reponse.getEtat() == 1 ? "-fx-font-weight: bold; -fx-font-size: 14;" : "-fx-font-size: 14;");
        
        // Texte de la réponse
        Label reponseLabel = new Label(reponse.getReponse());
        reponseLabel.setWrapText(true);
        
        // Mettre le texte en vert pour les réponses correctes
        if (reponse.getEtat() == 1) {
            reponseLabel.setTextFill(Color.valueOf("#2E7D32"));
            reponseLabel.setStyle("-fx-padding: 0 0 0 10; -fx-font-weight: bold;");
        } else {
            reponseLabel.setStyle("-fx-padding: 0 0 0 10;");
        }
        
        javafx.scene.layout.HBox.setHgrow(reponseLabel, javafx.scene.layout.Priority.ALWAYS);
        
        // Spacer pour pousser les boutons à droite
        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Boutons d'édition et de suppression
        HBox buttonsBox = new HBox(5);
        
        Button editButton = new Button();
        editButton.setStyle("-fx-background-color: #2196F3; -fx-background-radius: 4; -fx-min-width: 30; -fx-min-height: 30; -fx-padding: 0;");
        Label editLabel = new Label("✎");
        editLabel.setTextFill(Color.WHITE);
        editButton.setGraphic(editLabel);
        
        Button deleteButton = new Button();
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-background-radius: 4; -fx-min-width: 30; -fx-min-height: 30; -fx-padding: 0;");
        Label deleteLabel = new Label("✕");
        deleteLabel.setTextFill(Color.WHITE);
        deleteButton.setGraphic(deleteLabel);
        
        editButton.setOnAction(e -> handleEditReponse(reponse));
        deleteButton.setOnAction(e -> handleDeleteReponse(reponse));
        
        buttonsBox.getChildren().addAll(editButton, deleteButton);
        
        reponseItem.getChildren().addAll(statusIcon, reponseLabel, spacer, buttonsBox);
        
        return reponseItem;
    }

    private void handleEditReponse(Reponse reponse) {
        // Créer une boîte de dialogue pour modifier la réponse
        Dialog<Reponse> dialog = new Dialog<>();
        dialog.setTitle("Modifier la réponse");
        dialog.setHeaderText("Modifier la réponse");
        
        // Définir les boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Créer les champs de formulaire
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField reponseEditField = new TextField(reponse.getReponse());
        CheckBox correcteCheckBox = new CheckBox("Réponse correcte");
        correcteCheckBox.setSelected(reponse.getEtat() == 1);
        
        grid.add(new Label("Réponse:"), 0, 0);
        grid.add(reponseEditField, 1, 0);
        grid.add(correcteCheckBox, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir le résultat lorsque le bouton "Save" est cliqué
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Reponse modifiedReponse = new Reponse();
                modifiedReponse.setId(reponse.getId());
                modifiedReponse.setQuestions_id(reponse.getQuestions_id());
                modifiedReponse.setReponse(reponseEditField.getText());
                modifiedReponse.setEtat(correcteCheckBox.isSelected() ? 1 : 0);
                return modifiedReponse;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(modifiedReponse -> {
            try {
                reponseService.modifier(modifiedReponse);
                loadReponses();
                showAlert("Succès", "Réponse modifiée avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la modification de la réponse: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        });
    }

    private void handleDeleteReponse(Reponse reponse) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la réponse");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    reponseService.supprimer(reponse.getId());
                    loadReponses();
                    showAlert("Succès", "Réponse supprimée avec succès", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de la suppression de la réponse: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleCancel() {
        // Si la boîte de dialogue est visible, la cacher
        if (ajouterReponseDialog.isVisible()) {
            ajouterReponseDialog.setVisible(false);
            ajouterReponseDialog.setManaged(false);
        } else {
            // Sinon, revenir à la page précédente
            goBack();
        }
    }

    @FXML
    private void handleQuestionsToggle() {
        try {
            // Naviguer vers la vue des questions
            switchToQuestionView();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la navigation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConsulterQuizToggle() {
        try {
            // Naviguer vers la vue de consultation du quiz
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConsulterQuizView.fxml"));
            Parent root = loader.load();
            
            ConsulterQuizController controller = loader.getController();
            if (controller != null) {
                controller.setExamenId(examenId);
                controller.setExamenInfo(examenTitre, examenDescription);
            }
            
            Scene scene = consulterToggle.getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la navigation vers la vue de consultation du quiz", Alert.AlertType.ERROR);
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuestionView.fxml"));
            Parent root = loader.load();
            
            QuestionController controller = loader.getController();
            if (controller != null) {
                controller.setExamenId(examenId);
                controller.setExamenInfo(examenTitre, examenDescription);
            }
            
            Scene scene = backButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du retour: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void switchToQuestionView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuestionView.fxml"));
            Parent root = loader.load();
            
            QuestionController controller = loader.getController();
            if (controller != null) {
                controller.setExamenId(examenId);
                controller.setExamenInfo(examenTitre, examenDescription);
            }
            
            Scene scene = questionsToggle.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la navigation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Méthode pour naviguer à la question précédente
    @FXML
    private void handlePreviousQuestion() {
        if (questionsList != null && currentQuestionIndex > 0) {
            currentQuestionIndex--;
            selectedQuestion = questionsList.get(currentQuestionIndex);
            updateQuestionInfo();
            loadReponses();
        }
    }
    
    // Méthode pour naviguer à la question suivante
    @FXML
    private void handleNextQuestion() {
        if (questionsList != null && currentQuestionIndex < questionsList.size() - 1) {
            currentQuestionIndex++;
            selectedQuestion = questionsList.get(currentQuestionIndex);
            updateQuestionInfo();
            loadReponses();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 