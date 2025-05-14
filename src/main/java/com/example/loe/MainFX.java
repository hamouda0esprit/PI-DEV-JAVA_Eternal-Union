package com.example.loe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminForum.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Hello World");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch(Exception e) {
            System.out.println("Erreur lors du der=marrage de l'application " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

}
