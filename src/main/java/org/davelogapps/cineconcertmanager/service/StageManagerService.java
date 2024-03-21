package org.davelogapps.cineconcertmanager.service;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.davelogapps.cineconcertmanager.model.VideoFile;

import java.util.ArrayList;
import java.util.List;

public class StageManagerService {
    private final Stage stage;
    private final VideoPlayerService videoPlayerService;
    private final String directoryPath;
    private List<VideoFile> videoFiles = new ArrayList<>();
    private int currentIndex = 0;

    public StageManagerService(Stage stage, VideoPlayerService videoPlayerService, String directoryPath) {
        this.stage = stage;
        this.videoPlayerService = videoPlayerService;
        this.directoryPath = directoryPath;
    }

    public void setupAndShowVideoScene() {
        videoFiles = videoPlayerService.loadVideos(directoryPath);
        final MediaPlayer[] mediaPlayerContainer = {null};

        mediaPlayerContainer[0] = videoPlayerService.loadAndPlayVideo(videoFiles.get(currentIndex));

        if (mediaPlayerContainer[0] != null) {
            MediaView mediaView = new MediaView(mediaPlayerContainer[0]);
            BorderPane root = new BorderPane();
            root.setCenter(mediaView);

            Scene scene = getScene(root, mediaPlayerContainer, mediaView);

            stage.setScene(scene);
            stage.setFullScreen(true);

            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());
            //mediaPlayerContainer[0].play();

            stage.show();
        }
    }

    private Scene getScene(BorderPane root, MediaPlayer[] mediaPlayerContainer, MediaView mediaView) {
        Scene scene = new Scene(root, 800, 600, Color.BLACK);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                currentIndex = (currentIndex + 1) % videoFiles.size();
                mediaPlayerContainer[0].stop();
                mediaPlayerContainer[0].dispose();
                mediaPlayerContainer[0] = videoPlayerService.loadAndPlayVideo(videoFiles.get(currentIndex));
                mediaView.setMediaPlayer(mediaPlayerContainer[0]);
                mediaPlayerContainer[0].play();
            }
        });
        return scene;
    }
}
