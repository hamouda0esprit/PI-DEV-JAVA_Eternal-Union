package Controllers;

import Controllers.Navigation.NavbarController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML
    private BorderPane mainContainer;
    
    @FXML
    private VBox contentContainer;
    
    @FXML
    private StackPane heroSection;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label subtitleLabel;
    
    @FXML
    private HBox featuresContainer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load the navbar
        try {
            FXMLLoader navbarLoader = new FXMLLoader(getClass().getResource("/view/Navbar.fxml"));
            Parent navbar = navbarLoader.load();
            NavbarController navbarController = navbarLoader.getController();
            
            // Set the navbar at the top of the BorderPane
            mainContainer.setTop(navbar);
            
            // Initialize the home page content
            initializeContent();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializeContent() {
        // Set up the hero section
        heroSection.setStyle("-fx-background-color: linear-gradient(to right, #4a90e2, #63b3ed);");
        
        // Configure welcome text
        welcomeLabel.setText("Welcome to LOE");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
        welcomeLabel.setTextFill(Color.WHITE);
        
        subtitleLabel.setText("Your Learning Journey Starts Here");
        subtitleLabel.setFont(Font.font("System", 24));
        subtitleLabel.setTextFill(Color.WHITE);
        
        // Create feature boxes
        createFeatureBox("Courses", "Access our comprehensive course library", "/images/courses.png");
        createFeatureBox("Exams", "Test your knowledge with our exams", "/images/exams.png");
        createFeatureBox("Events", "Join our learning community events", "/images/events.png");
        createFeatureBox("Forum", "Connect with other learners", "/images/forum.png");
    }
    
    private void createFeatureBox(String title, String description, String imagePath) {
        VBox featureBox = new VBox(10);
        featureBox.setAlignment(Pos.CENTER);
        featureBox.setPadding(new Insets(20));
        featureBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        // Create image
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        imageView.setFitWidth(64);
        imageView.setFitHeight(64);
        
        // Create title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        // Create description
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        
        featureBox.getChildren().addAll(imageView, titleLabel, descLabel);
        featuresContainer.getChildren().add(featureBox);
    }
} 