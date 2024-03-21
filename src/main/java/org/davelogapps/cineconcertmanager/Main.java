package org.davelogapps.cineconcertmanager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.davelogapps.cineconcertmanager.service.VideoPlayerService;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ciné Concert Manager");

        VideoPlayerService videoPlayerService = new VideoPlayerService();
        MediaPlayer mediaPlayer = videoPlayerService.loadAndPlayFirstVideo("E:\\Documents\\Videos\\CineConcertManager_videos");

        if (mediaPlayer != null) {
            MediaView mediaView = new MediaView(mediaPlayer);

            BorderPane root = new BorderPane();
            root.setCenter(mediaView);

            Scene scene = new Scene(root, 800, 600);

            primaryStage.setScene(scene);
            primaryStage.setFullScreen(true);

            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());
            mediaPlayer.play();
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}