package com.example.pijava;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Get the FXML loader
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/AdminEvenement.fxml"));
            Parent root = loader.load();
            
            // Create and set the scene
            Scene scene = new Scene(root);
            
            // Add CSS
            scene.getStylesheets().add(Main.class.getResource("/styles/style.css").toExternalForm());
            
            // Set the window icon
            Image icon = new Image(Main.class.getResourceAsStream("/images/logo.png"));
            primaryStage.getIcons().add(icon);
            
            primaryStage.setTitle("Event Management System");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 