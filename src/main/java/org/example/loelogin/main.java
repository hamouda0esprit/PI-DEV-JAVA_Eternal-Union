package org.example.loelogin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import Controllers.LoginController;
import utils.DataSource;

public class main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource("/view/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        
        // Get controller and set stage for responsive design
        LoginController controller = fxmlLoader.getController();
        controller.setStage(stage);
        
        stage.setTitle("LOE - Start your learning journey");
        stage.setScene(scene);
        stage.setMinWidth(450);
        stage.setMinHeight(650);
        
        // Optimal starting size for the application
        stage.setWidth(1000);
        stage.setHeight(800);
        
        stage.show();
    }

    public static void main(String[] args) {
        try {
            System.out.println("Initializing application...");
            
            // Initialize database connection
            System.out.println("Setting up database connection...");
            DataSource.getInstance();
            
            // Load icons
            System.out.println("Loading icons...");

            
            // Launch the application
            System.out.println("Starting application...");
            launch();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

}
