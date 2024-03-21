package org.davelogapps.cineconcertmanager;

import javafx.application.Application;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.davelogapps.cineconcertmanager.service.StageManagerService;
import org.davelogapps.cineconcertmanager.service.VideoPlayerService;

import java.io.File;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ciné Concert Manager");

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionner le dossier des vidéos");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            String directoryPath = selectedDirectory.getAbsolutePath();

            VideoPlayerService videoPlayerService = new VideoPlayerService();
            StageManagerService stageManager = new StageManagerService(primaryStage, videoPlayerService, directoryPath);
            stageManager.setupAndShowVideoScene();
        } else {
            System.out.println("Aucun dossier sélectionné.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}