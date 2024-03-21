package org.davelogapps.cineconcertmanager;

import javafx.application.Application;
import javafx.stage.Stage;
import org.davelogapps.cineconcertmanager.service.StageManagerService;
import org.davelogapps.cineconcertmanager.service.VideoPlayerService;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ciné Concert Manager");

        VideoPlayerService videoPlayerService = new VideoPlayerService();
        String directoryPath = "E:\\Documents\\Videos\\CineConcertManager_videos\\cine-concert-1ere-partie-decouverte_2024-03-21_0006";

        StageManagerService stageManager = new StageManagerService(primaryStage, videoPlayerService, directoryPath);
        stageManager.setupAndShowVideoScene();
    }

    public static void main(String[] args) {
        launch();
    }
}