package Controllers;

import entite.Examen;
import entite.ResultatQuiz;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import service.ExamenService;
import service.QuestionService;
import service.ReponseService;
import service.ResultatQuizService;
import entite.Question;
import entite.Reponse;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;

public class AccueilEtudiantController implements Initializable {
    
    @FXML private FlowPane quizContainer;
    
    private ExamenService examenService;
    private ResultatQuizService resultatQuizService;
    
    // Variable pour stocker l'ID de l'utilisateur
    private String userId;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        resultatQuizService = new ResultatQuizService();
        
        // Charger les examens et créer les cartes
        Platform.runLater(() -> {
            loadQuizzes();
        });
    }
    
    /**
     * Définit l'ID de l'utilisateur actuel
     * @param userId ID de l'utilisateur
     */
    public void setUserId(String userId) {
        this.userId = userId;
        System.out.println("ID utilisateur défini dans AccueilEtudiantController: " + userId);
    }
    
    private void loadQuizzes() {
        // Effacer les quiz existants
        quizContainer.getChildren().clear();
        
        // Récupérer les examens depuis la base de données
        List<Examen> examens = examenService.recupererTout();
        
        if (examens.isEmpty()) {
            showNoQuizzesMessage();
        } else {
            // Afficher les examens disponibles
            for (Examen examen : examens) {
                createQuizCard(examen);
            }
        }
    }
    
    private void showNoQuizzesMessage() {
        Label emptyLabel = new Label("Aucun quiz disponible pour le moment");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-padding: 20px;");
        quizContainer.getChildren().add(emptyLabel);
    }
    
    /**
     * Crée une carte pour afficher un examen
     * @param examen L'examen à afficher
     */
    private void createQuizCard(Examen examen) {
        // Créer la carte
        VBox quizCard = new VBox();
        quizCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                      "-fx-pref-width: 270; -fx-pref-height: 220;"); // Augmenter la hauteur pour ajouter l'info
        quizCard.setPadding(new Insets(20));
        quizCard.setSpacing(10);
        
        // Titre du quiz
        Label titleLabel = new Label(examen.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #333333;");
        titleLabel.setWrapText(true);
        
        // Description
        Label descriptionLabel = new Label(examen.getDescription());
        descriptionLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 14px;");
        descriptionLabel.setWrapText(true);
        VBox.setMargin(descriptionLabel, new Insets(0, 0, 10, 0));
        
        // Conteneur pour les informations du bas
        HBox infoContainer = new HBox();
        infoContainer.setSpacing(15);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Icône de matière avec étiquette
        HBox categoryBox = new HBox(5);
        categoryBox.setAlignment(Pos.CENTER_LEFT);
        
        Label categoryIcon = new Label("📚");
        categoryIcon.setStyle("-fx-font-size: 14px;");
        
        Label categoryLabel = new Label(examen.getMatiere());
        categoryLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 14px;");
        
        categoryBox.getChildren().addAll(categoryIcon, categoryLabel);
        
        // Durée
        HBox durationBox = new HBox(5);
        durationBox.setAlignment(Pos.CENTER_LEFT);
        
        Label durationIcon = new Label("⏱");
        durationIcon.setStyle("-fx-font-size: 14px;");
        
        Label durationLabel = new Label(examen.getDuree() + " minutes");
        durationLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 14px;");
        
        durationBox.getChildren().addAll(durationIcon, durationLabel);
        
        // Ajout des boîtes d'informations au conteneur
        infoContainer.getChildren().addAll(categoryBox, durationBox);
        
        // Conteneur pour les essais et autres informations
        VBox essaisContainer = new VBox();
        essaisContainer.setSpacing(5);
        
        // Nombre d'essais
        HBox essaisBox = new HBox(5);
        essaisBox.setAlignment(Pos.CENTER_LEFT);
        
        Label essaisIcon = new Label("🔄");
        essaisIcon.setStyle("-fx-font-size: 14px;");
        
        // Déterminer le nombre d'essais restants
        int essaisRestants = examen.getNbrEssai();
        boolean premierEssai = true;
        ResultatQuiz resultatQuiz = null;
        
        if (userId != null && !userId.isEmpty()) {
            try {
                int userIdInt = Integer.parseInt(userId);
                
                // Vérifier si l'utilisateur a déjà tenté cet examen
                resultatQuiz = resultatQuizService.recupererParUtilisateurEtExamen(userIdInt, examen.getId());
                
                if (resultatQuiz != null) {
                    // L'utilisateur a déjà fait au moins une tentative
                    premierEssai = false;
                    
                    // Vérifier si la colonne nbr_essai existe dans la base de données
                    try {
                        // Essayer de récupérer nbr_essai
                        essaisRestants = resultatQuiz.getNbrEssai();
                        System.out.println("Examen: " + examen.getTitre() + " - Essais restants dans BD: " + essaisRestants);
                    } catch (Exception e) {
                        // Si une erreur se produit (par exemple, la colonne n'existe pas),
                        // calculer en fonction du nombre d'essais par défaut et des tentatives
                        System.out.println("Erreur lors de l'accès à nbr_essai, calcul alternatif des essais");
                        essaisRestants = Math.max(0, examen.getNbrEssai() - 1);
                    }
                } else {
                    System.out.println("Examen: " + examen.getTitre() + " - Premier essai - Total: " + examen.getNbrEssai());
                }
            } catch (NumberFormatException e) {
                System.err.println("Erreur lors de la conversion de l'ID utilisateur: " + e.getMessage());
            }
        }
        
        // Essais illimités si nbrEssai <= 0
        if (examen.getNbrEssai() <= 0) {
            essaisRestants = Integer.MAX_VALUE;
        }
        
        // Affichage personnalisé selon le nombre d'essais
        String essaisText;
        if (examen.getNbrEssai() == 1) {
            essaisText = "1 essai autorisé";
        } else if (examen.getNbrEssai() <= 0) {
            essaisText = "Essais illimités";
        } else {
            essaisText = examen.getNbrEssai() + " essais autorisés";
        }
        
        Label essaisLabel = new Label(essaisText);
        essaisLabel.setStyle("-fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        essaisBox.getChildren().addAll(essaisIcon, essaisLabel);
        
        // Ajouter le nombre d'essais utilisés/restants si l'utilisateur est connecté
        if (userId != null && !userId.isEmpty() && examen.getNbrEssai() > 0) {
            HBox essaisUtilisesBox = new HBox(5);
            essaisUtilisesBox.setAlignment(Pos.CENTER_LEFT);
            
            // Afficher les essais restants au lieu des essais utilisés
            String messEssais = premierEssai ? 
                "Aucun essai utilisé" : 
                "Essais restants: " + essaisRestants + "/" + examen.getNbrEssai();
            
            Label essaisUtilisesLabel = new Label(messEssais);
            essaisUtilisesLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 12px;");
            
            essaisUtilisesBox.getChildren().add(essaisUtilisesLabel);
            
            // Barre de progression pour visualiser les essais
            ProgressBar progressBar = new ProgressBar();
            progressBar.setPrefWidth(230);
            progressBar.setPrefHeight(10);
            double progress = 0;
            
            if (premierEssai) {
                // Aucun essai utilisé
                progress = 0;
            } else if (examen.getNbrEssai() > 0) {
                // Calculer la progression basée sur les essais restants
                progress = 1.0 - ((double) essaisRestants / examen.getNbrEssai());
            }
            
            progressBar.setProgress(progress);
            
            // Couleur de la barre en fonction du nombre d'essais restants
            if (essaisRestants <= 0 && examen.getNbrEssai() > 0) {
                progressBar.setStyle("-fx-accent: #F44336;"); // Rouge si plus d'essais
            } else if (essaisRestants <= examen.getNbrEssai() * 0.3) {
                progressBar.setStyle("-fx-accent: #FF9800;"); // Orange si peu d'essais restants
            } else {
                progressBar.setStyle("-fx-accent: #4CAF50;"); // Vert si beaucoup d'essais restants
            }
            
            // Ajouter les éléments au conteneur d'essais
            essaisContainer.getChildren().addAll(essaisBox, essaisUtilisesBox, progressBar);
        } else {
            // Si l'utilisateur n'est pas connecté ou si les essais sont illimités
            essaisContainer.getChildren().add(essaisBox);
        }
        
        // Vérifier s'il reste des essais pour l'utilisateur
        boolean noAttemptsLeft = false;
        if (examen.getNbrEssai() > 0) {  // Seulement si les essais sont limités
            if (premierEssai) {
                // Premier essai - toujours des essais disponibles
                noAttemptsLeft = false;
            } else if (resultatQuiz != null) {
                // Essais suivants - vérifier le nombre d'essais restants
                noAttemptsLeft = (essaisRestants <= 0);
            }
        }
        System.out.println("Quiz: " + examen.getTitre() + " - noAttemptsLeft: " + noAttemptsLeft);
        
        // Créer buttonBox uniquement s'il reste des essais
        if (!noAttemptsLeft) {
            // Il reste des essais, ajouter le bouton Commencer
            Button startButton = new Button("Commencer");
            startButton.setPrefWidth(120);
            startButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 4;");
            
            // Pour centrer le bouton
            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().add(startButton);
            VBox.setMargin(buttonBox, new Insets(10, 0, 0, 0));
            
            // Action du bouton - passer le titre de l'examen
            startButton.setOnAction(e -> startQuiz(examen.getTitre()));
            
            // Ajouter le buttonBox à la carte
            quizCard.getChildren().addAll(
                titleLabel,
                descriptionLabel,
                infoContainer,
                essaisContainer,
                buttonBox
            );
        } else {
            // Aucun essai restant, ne pas ajouter le bouton
            quizCard.getChildren().addAll(
                titleLabel,
                descriptionLabel,
                infoContainer,
                essaisContainer
            );
        }
        
        // Ajouter la carte au conteneur de quiz
        quizContainer.getChildren().add(quizCard);
    }
    
    private void startQuiz(String quizTitle) {
        System.out.println("Démarrage du quiz: " + quizTitle);
        
        try {
            // Récupérer l'examen correspondant au titre
            ExamenService examenService = new ExamenService();
            List<Examen> examens = examenService.recupererTout();
            Examen examen = null;
            
            // Trouver l'examen correspondant au titre
            for (Examen e : examens) {
                if (e.getTitre().equals(quizTitle)) {
                    examen = e;
                    break;
                }
            }
            
            if (examen == null) {
                // Si l'examen n'est pas trouvé, afficher un message d'erreur
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de trouver l'examen avec le titre " + quizTitle);
                return;
            }
            
            // Charger la vue de réponse au quiz
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RepondreQuizView.fxml"));
            Parent root = loader.load();
            
            // Passer l'examen au contrôleur
            RepondreQuizController controller = loader.getController();
            controller.setExamen(examen);
            
            // Passer l'ID utilisateur au contrôleur
            if (userId != null) {
                controller.setUserId(userId);
            }
            
            // Désactiver le mode démo
            RepondreQuizController.setUseDemo(false);
            ConsulterQuizController.setUseDemo(false);
            
            // Afficher la nouvelle vue
            Scene scene = new Scene(root);
            Stage stage = (Stage) quizContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface de réponse au quiz: " + e.getMessage());
        }
    }
    
    /**
     * Crée des questions et réponses de démonstration pour un examen donné
     * @param examen L'examen
     */
    private void createDemoQuestionsAndResponses(Examen examen) {
        // Cette méthode n'est plus utilisée car nous récupérons les données de la base de données
        // Elle est conservée à titre de référence
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 