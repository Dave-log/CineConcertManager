package org.davelogapps.cineconcertmanager;

import javafx.application.Application;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.davelogapps.cineconcertmanager.service.StageManager;
import org.davelogapps.cineconcertmanager.service.VideoPlayerService;

import java.io.File;
import java.util.prefs.Preferences;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ciné Concert Creator");

        Preferences prefs = Preferences.userNodeForPackage(App.class);
        String lastDir = prefs.get("lastDirectory", System.getProperty("user.home"));

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionner le dossier des vidéos");
        directoryChooser.setInitialDirectory(new File(lastDir));
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            prefs.put("lastDirectory", selectedDirectory.getAbsolutePath());
            String directoryPath = selectedDirectory.getAbsolutePath();

            VideoPlayerService videoPlayerService = new VideoPlayerService();
            StageManager stageManager = new StageManager(primaryStage, videoPlayerService, directoryPath);
            stageManager.setupAndShowVideoScene();
        } else {
            System.out.println("Aucun dossier sélectionné.");
        }
    }
}