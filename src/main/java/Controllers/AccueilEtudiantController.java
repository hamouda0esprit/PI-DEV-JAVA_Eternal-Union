package Controllers;

import entite.Examen;
import entite.ResultatQuiz;
import entite.Feedback;
import entite.Rating;
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
import service.FeedbackService;
import service.RatingService;
import entite.Question;
import entite.Reponse;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Date;

public class AccueilEtudiantController implements Initializable {
    
    @FXML private FlowPane quizContainer;
    
    private ExamenService examenService;
    private ResultatQuizService resultatQuizService;
    private FeedbackService feedbackService;
    private RatingService ratingService;
    
    // Variable pour stocker l'ID de l'utilisateur
    private String userId;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examenService = new ExamenService();
        resultatQuizService = new ResultatQuizService();
        feedbackService = new FeedbackService();
        ratingService = new RatingService();
        
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
        
        // Conteneur pour le titre et l'icône œil (si applicable)
        HBox titleContainer = new HBox();
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        titleContainer.setSpacing(10);
        HBox.setHgrow(titleContainer, Priority.ALWAYS);

        // Titre du quiz
        Label titleLabel = new Label(examen.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #333333;");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Ajouter le titre au conteneur
        titleContainer.getChildren().add(titleLabel);

        // Ajouter un espaceur pour pousser l'icône à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleContainer.getChildren().add(spacer);
        
        // Vérifier s'il faut afficher l'icône d'œil (quand nbr_essai = 0)
        boolean showEyeIcon = false;
        if (examen.getNbrEssai() > 0 && userId != null && !userId.isEmpty()) {
            try {
                int userIdInt = Integer.parseInt(userId);
                ResultatQuiz resultatQuiz = resultatQuizService.recupererParUtilisateurEtExamen(userIdInt, examen.getId());
                if (resultatQuiz != null && resultatQuiz.getNbrEssai() <= 0) {
                    showEyeIcon = true;
                }
            } catch (NumberFormatException e) {
                System.err.println("Erreur lors de la conversion de l'ID utilisateur: " + e.getMessage());
            }
        }

        // Ajouter l'icône d'œil si nécessaire
        if (showEyeIcon) {
            Button eyeButton = new Button();
            eyeButton.setTooltip(new Tooltip("Consulter les réponses correctes"));
            eyeButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

            // Créer une icône SVG pour l'œil
            SVGPath eyeIcon = new SVGPath();
            eyeIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
            eyeIcon.setFill(Color.web("#1976d2"));
            eyeIcon.setStroke(Color.TRANSPARENT);

            // Définir la taille de l'icône
            eyeButton.setGraphic(eyeIcon);
            eyeButton.setPrefSize(24, 24);
            eyeButton.setMinSize(24, 24);
            eyeButton.setMaxSize(24, 24);

            // Action du bouton - consulter les réponses correctes
            eyeButton.setOnAction(e -> consulterReponsesQuiz(examen.getId(), examen.getTitre()));
            
            // Ajouter le bouton à droite du conteneur
            titleContainer.getChildren().add(eyeButton);
        }

        // Description
        Label descriptionLabel = new Label(examen.getDescription());
        descriptionLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 14px;");
        descriptionLabel.setWrapText(true);
        VBox.setMargin(descriptionLabel, new Insets(0, 0, 10, 0));
        
        // Afficher l'évaluation moyenne pour cet examen
        HBox ratingInfoContainer = new HBox(5);
        ratingInfoContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Récupérer la moyenne des évaluations et le nombre d'évaluations
        double moyenneEvaluations = ratingService.calculerMoyenneExamen(examen.getId());
        int nombreEvaluations = ratingService.compterEvaluationsExamen(examen.getId());
        
        // Arrondir la moyenne à 1 décimale
        double moyenneArrondie = Math.round(moyenneEvaluations * 10.0) / 10.0;
        
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
        
        // Afficher les étoiles uniquement s'il y a des évaluations
        if (nombreEvaluations > 0) {
            HBox ratingBox = new HBox(5);
            ratingBox.setAlignment(Pos.CENTER_LEFT);
            
            Label ratingValue = new Label(String.format("%.1f", moyenneArrondie));
            ratingValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF9800;");
            
            Label starIcon = new Label("★");
            starIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700;");
            
            Label ratingCount = new Label("(" + nombreEvaluations + ")");
            ratingCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
            
            ratingBox.getChildren().addAll(ratingValue, starIcon, ratingCount);
            
            // Ajouter l'info de rating à côté des autres informations
            infoContainer.getChildren().add(ratingBox);
        }
        
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
        
        // Créer buttonBox pour tous les cas
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        VBox.setMargin(buttonBox, new Insets(10, 0, 0, 0));
        
        if (!noAttemptsLeft) {
            // Il reste des essais, ajouter le bouton Commencer
            Button startButton = new Button("Commencer");
            startButton.setPrefWidth(120);
            startButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 4;");
            
            // Action du bouton - passer le titre de l'examen
            startButton.setOnAction(e -> startQuiz(examen.getTitre()));
            
            // Ajouter le bouton à buttonBox
            buttonBox.getChildren().add(startButton);
        } else {
            // Aucun essai restant, vérifier si l'utilisateur a déjà fourni un feedback et une évaluation
            boolean feedbackExists = false;
            boolean ratingExists = false;
            String feedbackContenu = "";
            Integer ratingStars = null;
            
            if (userId != null && !userId.isEmpty()) {
                try {
                    int userIdInt = Integer.parseInt(userId);
                    
                    // Vérifier si un feedback existe déjà
                    feedbackExists = feedbackService.verifierExistenceFeedback(userIdInt, examen.getId());
                    
                    // Vérifier si une évaluation existe déjà
                    ratingExists = ratingService.verifierExistenceRating(userIdInt, examen.getId());
                    
                    // Si feedback existe, récupérer son contenu
                    if (feedbackExists) {
                        List<Feedback> feedbacks = feedbackService.recupererParExamen(examen.getId());
                        for (Feedback f : feedbacks) {
                            if (f.getUser_id() != null && f.getUser_id() == userIdInt) {
                                feedbackContenu = f.getContenu();
                                break;
                            }
                        }
                    }
                    
                    // Si rating existe, récupérer le nombre d'étoiles
                    if (ratingExists) {
                        Rating rating = ratingService.recupererParUtilisateurEtExamen(userIdInt, examen.getId());
                        if (rating != null) {
                            ratingStars = rating.getStars();
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Erreur lors de la conversion de l'ID utilisateur: " + e.getMessage());
                }
            }
            
            // Conteneur pour le feedback et la notation
            VBox feedbackRatingBox = new VBox(10);
            feedbackRatingBox.setAlignment(Pos.CENTER_LEFT);
            
            // Afficher le feedback existant ou le bouton de feedback
            if (feedbackExists) {
                // Afficher le feedback existant
                VBox feedbackBox = new VBox(5);
                feedbackBox.setAlignment(Pos.CENTER_LEFT);
                
                Label ruckMeldungLabel = new Label("Feedback:");
                ruckMeldungLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50;");
                
                // Afficher le contenu du feedback avec une puce
                HBox feedbackContent = new HBox(5);
                feedbackContent.setAlignment(Pos.CENTER_LEFT);
                
                Label bulletLabel = new Label("•");
                bulletLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575;");
                
                Label contentLabel = new Label(feedbackContenu);
                contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575; -fx-font-style: italic;");
                contentLabel.setWrapText(true);
                
                feedbackContent.getChildren().addAll(bulletLabel, contentLabel);
                feedbackBox.getChildren().addAll(ruckMeldungLabel, feedbackContent);
                
                // Ajouter le feedbackBox
                feedbackRatingBox.getChildren().add(feedbackBox);
            } else {
                // Aucun feedback, ajouter le bouton de feedback
                Button feedbackButton = new Button("Donner feedback");
                feedbackButton.setPrefWidth(140);
                feedbackButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 4;");
                
                // Action du bouton - ouvrir une fenêtre de feedback
                feedbackButton.setOnAction(e -> openFeedbackDialog(examen.getId(), examen.getTitre(), quizCard, feedbackRatingBox));
                
                // Ajouter le bouton au feedbackRatingBox
                feedbackRatingBox.getChildren().add(feedbackButton);
            }
            
            // Afficher la notation existante ou ajouter le système de notation
            if (ratingExists && ratingStars != null) {
                // Afficher la notation existante
                VBox ratingBox = new VBox(5);
                ratingBox.setAlignment(Pos.CENTER_LEFT);
                
                Label ratingLabel = new Label("Votre évaluation:");
                ratingLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF9800;");
                
                // Afficher les étoiles
                HBox starsBox = new HBox(2);
                starsBox.setAlignment(Pos.CENTER_LEFT);
                
                for (int i = 1; i <= 5; i++) {
                    Label starLabel = new Label(i <= ratingStars ? "★" : "☆");
                    starLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: " + (i <= ratingStars ? "#FFD700" : "#DDDDDD") + ";");
                    starsBox.getChildren().add(starLabel);
                }
                
                ratingBox.getChildren().addAll(ratingLabel, starsBox);
                
                // Ajouter le ratingBox
                feedbackRatingBox.getChildren().add(ratingBox);
            } else {
                // Aucune notation, ajouter le système de notation
                VBox ratingBox = new VBox(5);
                ratingBox.setAlignment(Pos.CENTER_LEFT);
                
                Label ratingLabel = new Label("Évaluer cet examen:");
                ratingLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF9800;");
                
                // Système d'étoiles interactif
                HBox starsBox = new HBox(2);
                starsBox.setAlignment(Pos.CENTER_LEFT);
                
                // La valeur sélectionnée (initialement 0)
                final int[] selectedRating = {0};
                
                // Créer 5 étoiles
                for (int i = 1; i <= 5; i++) {
                    final int rating = i;
                    Label starLabel = new Label("☆");
                    starLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #DDDDDD; -fx-cursor: hand;");
                    
                    // Effet hover
                    starLabel.setOnMouseEntered(event -> {
                        highlightStars(starsBox, rating);
                    });
                    
                    // Retour à l'état précédent quand la souris quitte
                    starLabel.setOnMouseExited(event -> {
                        highlightStars(starsBox, selectedRating[0]);
                    });
                    
                    // Sélection
                    starLabel.setOnMouseClicked(event -> {
                        selectedRating[0] = rating;
                        highlightStars(starsBox, rating);
                        
                        // Enregistrer l'évaluation
                        saveRating(examen.getId(), rating);
                    });
                    
                    starsBox.getChildren().add(starLabel);
                }
                
                ratingBox.getChildren().addAll(ratingLabel, starsBox);
                
                // Ajouter le ratingBox
                feedbackRatingBox.getChildren().add(ratingBox);
            }
            
            // Ajouter le feedbackRatingBox au buttonBox
            buttonBox.getChildren().add(feedbackRatingBox);
            buttonBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(feedbackRatingBox, Priority.ALWAYS);
        }
        
        // Ajouter le buttonBox à la carte dans tous les cas
        quizCard.getChildren().addAll(
            titleContainer,
            descriptionLabel,
            infoContainer,
            essaisContainer,
            buttonBox
        );
        
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
    
    /**
     * Ouvre une boîte de dialogue pour recueillir les commentaires sur le quiz
     * @param examenId L'identifiant de l'examen
     * @param quizTitle Le titre du quiz
     * @param quizCard La carte du quiz à mettre à jour
     * @param container Le conteneur à mettre à jour après l'ajout du feedback
     */
    private void openFeedbackDialog(Integer examenId, String quizTitle, VBox quizCard, VBox container) {
        // Vérifier si l'utilisateur a déjà donné un feedback pour ce quiz
        if (userId != null && !userId.isEmpty()) {
            try {
                int userIdInt = Integer.parseInt(userId);
                
                // Vérifier si un feedback existe déjà
                if (feedbackService.verifierExistenceFeedback(userIdInt, examenId)) {
                    showAlert(Alert.AlertType.INFORMATION, 
                        "Feedback déjà envoyé", 
                        "Vous avez déjà donné votre avis sur ce quiz. Merci pour votre participation !");
                    return;
                }
            } catch (NumberFormatException e) {
                System.err.println("Erreur lors de la conversion de l'ID utilisateur: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, 
                "Connexion requise", 
                "Vous devez être connecté pour donner votre avis.");
            return;
        }
        
        // Créer une nouvelle fenêtre de dialogue
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Feedback pour " + quizTitle);
        dialog.setHeaderText("Partagez votre avis sur ce quiz");
        
        // Configurer les boutons
        ButtonType submitButtonType = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        
        // Créer la zone de texte pour les commentaires
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));
        
        TextArea feedbackText = new TextArea();
        feedbackText.setPromptText("Entrez votre feedback ici...");
        feedbackText.setPrefHeight(150);
        
        content.getChildren().addAll(
            new Label("Vos commentaires :"),
            feedbackText
        );
        
        dialog.getDialogPane().setContent(content);
        
        // Activer/désactiver le bouton OK selon si le texte est vide
        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.setDisable(true);
        
        // Vérifier si le feedback a du contenu et activer le bouton si c'est le cas
        feedbackText.textProperty().addListener((observable, oldValue, newValue) -> {
            submitButton.setDisable(newValue.trim().isEmpty());
        });
        
        // Définir le résultat quand l'utilisateur clique sur Envoyer
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return feedbackText.getText();
            }
            return null;
        });
        
        // Afficher la boîte de dialogue et traiter le résultat
        dialog.showAndWait().ifPresent(commentaire -> {
            if (commentaire != null && !commentaire.isEmpty()) {
                saveFeedback(examenId, commentaire, quizCard, container);
            }
        });
    }
    
    /**
     * Enregistre le feedback de l'utilisateur pour un quiz
     * @param examenId L'identifiant de l'examen
     * @param commentaire Les commentaires de l'utilisateur
     * @param quizCard La carte du quiz à mettre à jour
     * @param container Le conteneur à mettre à jour après l'ajout du feedback
     */
    private void saveFeedback(Integer examenId, String commentaire, VBox quizCard, VBox container) {
        try {
            // Convertir l'ID utilisateur en entier
            int userIdInt = Integer.parseInt(userId);
            
            // Créer un nouvel objet Feedback
            Feedback feedback = new Feedback();
            feedback.setExamen_id(examenId);
            feedback.setUser_id(userIdInt);
            feedback.setContenu(commentaire);
            feedback.setDate_creation(new Date());
            
            // Enregistrer le feedback dans la base de données
            boolean success = feedbackService.ajouter(feedback);
            
            if (success) {
                System.out.println("Feedback enregistré avec succès pour l'examen #" + examenId);
                
                // Mettre à jour l'interface immédiatement
                container.getChildren().clear(); // Supprimer les éléments existants
                
                // Créer une zone d'affichage du feedback
                VBox feedbackBox = new VBox(5);
                feedbackBox.setAlignment(Pos.CENTER_LEFT);
                
                Label ruckMeldungLabel = new Label("Feedback:");
                ruckMeldungLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50;");
                
                // Afficher le contenu du feedback avec une puce
                HBox feedbackContent = new HBox(5);
                feedbackContent.setAlignment(Pos.CENTER_LEFT);
                
                Label bulletLabel = new Label("•");
                bulletLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575;");
                
                Label contentLabel = new Label(commentaire);
                contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575; -fx-font-style: italic;");
                contentLabel.setWrapText(true);
                
                feedbackContent.getChildren().addAll(bulletLabel, contentLabel);
                feedbackBox.getChildren().addAll(ruckMeldungLabel, feedbackContent);
                
                // Ajouter le feedbackBox au conteneur
                container.getChildren().add(feedbackBox);
                
                // Rafraîchir les évaluations
                Platform.runLater(() -> {
                    loadQuizzes();
                });
                
                showAlert(Alert.AlertType.INFORMATION, 
                          "Feedback envoyé", 
                          "Merci pour votre feedback ! Votre avis nous est précieux.");
            } else {
                System.err.println("Erreur lors de l'enregistrement du feedback");
                showAlert(Alert.AlertType.ERROR, 
                          "Erreur", 
                          "Une erreur est survenue lors de l'enregistrement du feedback.");
            }
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                      "Erreur", 
                      "Une erreur est survenue lors de la conversion de l'ID utilisateur: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                      "Erreur", 
                      "Une erreur est survenue lors de l'envoi du feedback: " + e.getMessage());
        }
    }
    
    /**
     * Ouvre une fenêtre pour consulter les réponses correctes d'un quiz
     * @param examenId L'identifiant de l'examen
     * @param quizTitle Le titre du quiz
     */
    private void consulterReponsesQuiz(Integer examenId, String quizTitle) {
        try {
            System.out.println("Consultation des réponses pour l'examen #" + examenId + " : " + quizTitle);
            
            // Récupérer l'examen correspondant à l'ID
            Examen examen = examenService.recupererParId(examenId);
            
            if (examen == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de trouver l'examen avec l'ID " + examenId);
                return;
            }
            
            // Créer une nouvelle fenêtre distincte pour afficher les réponses
            Stage correctionStage = new Stage();
            correctionStage.setTitle("Réponses correctes - " + quizTitle);
            
            // Créer le conteneur principal
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefWidth(600);
            scrollPane.setPrefHeight(500);
            
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(20));
            mainContainer.setStyle("-fx-background-color: #f5f5f5;");
            
            // En-tête
            Label titleLabel = new Label("Réponses correctes pour: " + quizTitle);
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label infoLabel = new Label("Voici les réponses correctes pour toutes les questions de ce quiz.");
            infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
            
            mainContainer.getChildren().addAll(titleLabel, infoLabel);
            
            // Charger les questions et réponses
            QuestionService questionService = new QuestionService();
            ReponseService reponseService = new ReponseService();
            
            List<Question> questions = questionService.recupererParExamen(examenId);
            
            if (questions.isEmpty()) {
                Label emptyLabel = new Label("Aucune question disponible pour ce quiz.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575; -fx-font-style: italic;");
                mainContainer.getChildren().add(emptyLabel);
            } else {
                // Afficher chaque question et ses réponses correctes
                for (int i = 0; i < questions.size(); i++) {
                    Question question = questions.get(i);
                    
                    // Carte pour la question
                    VBox questionCard = new VBox(10);
                    questionCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); " +
                                   "-fx-padding: 15;");
                    
                    // Numéro et texte de la question
                    Label questionLabel = new Label("Question " + (i + 1) + ": " + question.getQuestion());
                    questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
                    questionLabel.setWrapText(true);
                    
                    questionCard.getChildren().add(questionLabel);
                    
                    // Récupérer les réponses pour cette question
                    List<Reponse> reponses = reponseService.recupererParQuestion(question.getId());
                    
                    // Conteneur pour les réponses
                    VBox reponsesBox = new VBox(8);
                    reponsesBox.setPadding(new Insets(5, 0, 0, 0));
                    
                    boolean hasCorrectAnswer = false;
                    
                    // Afficher uniquement les réponses correctes
                    for (Reponse reponse : reponses) {
                        if (reponse.getEtat() == 1) { // Réponse correcte
                            hasCorrectAnswer = true;
                            
                            // Conteneur pour la réponse correcte
                            HBox correctAnswerBox = new HBox(10);
                            correctAnswerBox.setAlignment(Pos.CENTER_LEFT);
                            correctAnswerBox.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 4; -fx-padding: 8;");
                            
                            // Icône de validation
                            Label checkIcon = new Label("✓");
                            checkIcon.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 16px;");
                            
                            // Texte de la réponse
                            Label answerLabel = new Label(reponse.getReponse());
                            answerLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                            answerLabel.setWrapText(true);
                            
                            correctAnswerBox.getChildren().addAll(checkIcon, answerLabel);
                            reponsesBox.getChildren().add(correctAnswerBox);
                        }
                    }
                    
                    // Si aucune réponse correcte trouvée
                    if (!hasCorrectAnswer) {
                        Label noCorrectLabel = new Label("Aucune réponse correcte définie pour cette question.");
                        noCorrectLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #757575;");
                        reponsesBox.getChildren().add(noCorrectLabel);
                    }
                    
                    questionCard.getChildren().add(reponsesBox);
                    mainContainer.getChildren().add(questionCard);
                }
            }
            
            // Ajouter un bouton Fermer
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 4;");
            closeButton.setPrefWidth(100);
            closeButton.setOnAction(e -> correctionStage.close());
            
            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));
            buttonBox.getChildren().add(closeButton);
            
            mainContainer.getChildren().add(buttonBox);
            
            scrollPane.setContent(mainContainer);
            
            Scene scene = new Scene(scrollPane);
            correctionStage.setScene(scene);
            correctionStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                      "Erreur", 
                      "Impossible d'afficher les réponses correctes: " + e.getMessage());
        }
    }
    
    /**
     * Met en évidence les étoiles jusqu'à l'indice spécifié
     * @param starsBox Le conteneur d'étoiles
     * @param rating Le nombre d'étoiles à mettre en évidence
     */
    private void highlightStars(HBox starsBox, int rating) {
        for (int i = 0; i < starsBox.getChildren().size(); i++) {
            Label starLabel = (Label) starsBox.getChildren().get(i);
            if (i < rating) {
                starLabel.setText("★");
                starLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFD700; -fx-cursor: hand;");
            } else {
                starLabel.setText("☆");
                starLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #DDDDDD; -fx-cursor: hand;");
            }
        }
    }
    
    /**
     * Enregistre l'évaluation de l'utilisateur pour un examen
     * @param examenId L'identifiant de l'examen
     * @param stars Le nombre d'étoiles
     */
    private void saveRating(Integer examenId, int stars) {
        if (userId == null || userId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, 
                "Connexion requise", 
                "Vous devez être connecté pour évaluer cet examen.");
            return;
        }
        
        try {
            // Convertir l'ID utilisateur en entier
            int userIdInt = Integer.parseInt(userId);
            
            // Vérifier si une évaluation existe déjà
            Rating existingRating = ratingService.recupererParUtilisateurEtExamen(userIdInt, examenId);
            
            boolean success;
            if (existingRating != null) {
                // Mettre à jour l'évaluation existante
                existingRating.setStars(stars);
                success = ratingService.mettreAJour(existingRating);
            } else {
                // Créer une nouvelle évaluation
                Rating rating = new Rating();
                rating.setExamen_id(examenId);
                rating.setUser_id(userIdInt);
                rating.setStars(stars);
                rating.setCreated_at(new Date());
                
                // Enregistrer l'évaluation dans la base de données
                success = ratingService.ajouter(rating);
            }
            
            if (success) {
                System.out.println("Évaluation enregistrée avec succès pour l'examen #" + examenId + ": " + stars + " étoiles");
                
                // Rafraîchir les cartes de quiz pour afficher la nouvelle évaluation
                Platform.runLater(() -> {
                    loadQuizzes();
                });
                
                showAlert(Alert.AlertType.INFORMATION, 
                          "Évaluation enregistrée", 
                          "Merci pour votre évaluation !");
            } else {
                System.err.println("Erreur lors de l'enregistrement de l'évaluation");
                showAlert(Alert.AlertType.ERROR, 
                          "Erreur", 
                          "Une erreur est survenue lors de l'enregistrement de l'évaluation.");
            }
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                      "Erreur", 
                      "Une erreur est survenue lors de la conversion de l'ID utilisateur: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                      "Erreur", 
                      "Une erreur est survenue lors de l'envoi de l'évaluation: " + e.getMessage());
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