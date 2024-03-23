package org.davelogapps.cineconcertmanager.service;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.davelogapps.cineconcertmanager.model.VideoFile;
import org.davelogapps.cineconcertmanager.util.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StageManager {
    private final Stage stage;
    private final VideoPlayerService videoPlayerService;
    private final PromoImageManager promoImageManager;
    private final ImageView promoImageView;
    private final String directoryPath;
    private List<VideoFile> videoFiles = new ArrayList<>();
    private int currentIndex = 0;

    public StageManager(Stage stage, VideoPlayerService videoPlayerService, String directoryPath) {
        this.stage = stage;
        this.videoPlayerService = videoPlayerService;
        this.directoryPath = directoryPath;
        this.promoImageManager = new PromoImageManager("habillage.png");
        this.promoImageView = promoImageManager.getPromoImageView();
    }

    public void setupAndShowVideoScene() {
        videoFiles = videoPlayerService.loadVideos(directoryPath);
        MediaPlayer[] mediaPlayerContainer = {null};

        currentIndex = -1;

        mediaPlayerContainer[0] = videoPlayerService.loadAndPlayVideo(videoFiles.getFirst());

        if (mediaPlayerContainer[0] != null) {
            MediaView mediaView = new MediaView(mediaPlayerContainer[0]);
            BorderPane root = new BorderPane();

            root.setCenter(promoImageManager.getImagePane());

            Scene scene = getScene(root, mediaPlayerContainer, mediaView);

            stage.setScene(scene);
            stage.setFullScreen(true);

            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());

            promoImageView.fitWidthProperty().bind(scene.widthProperty());
            promoImageView.fitHeightProperty().bind(scene.heightProperty());

            stage.show();
        }
    }

    private Scene getScene(BorderPane root, MediaPlayer[] mediaPlayerContainer, MediaView mediaView) {
        Scene scene = new Scene(root, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, Color.BLACK);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                // NEXT VIDEO
                currentIndex = (currentIndex + 1) % videoFiles.size();
                mediaPlayerContainer[0].stop();
                mediaPlayerContainer[0].dispose();
                mediaPlayerContainer[0] = videoPlayerService.loadAndPlayVideo(videoFiles.get(currentIndex));
                mediaView.setMediaPlayer(mediaPlayerContainer[0]);
                mediaPlayerContainer[0].play();
                promoImageView.setVisible(false);
                mediaView.setVisible(true);
                root.setCenter(mediaView);

            } else if (event.getCode() == KeyCode.P) {
                // PAUSE OR UNPAUSE VIDEO
                if (mediaPlayerContainer[0].getStatus() == MediaPlayer.Status.PAUSED ||
                        mediaPlayerContainer[0].getCurrentTime().equals(Duration.ZERO))  {
                    mediaPlayerContainer[0].play();
                } else {
                    mediaPlayerContainer[0].pause();
                }

            } else if (event.getCode() == KeyCode.B) {
                // PREVIOUS VIDEO
                if (currentIndex > 0) {
                    currentIndex--;
                    mediaPlayerContainer[0].stop();
                    mediaPlayerContainer[0].dispose();
                    mediaPlayerContainer[0] = videoPlayerService.loadAndPlayVideo(videoFiles.get(currentIndex));
                    mediaView.setMediaPlayer(mediaPlayerContainer[0]);
                    mediaPlayerContainer[0].play();
                    mediaPlayerContainer[0].pause();
                }

            } else if (event.getCode() == KeyCode.E) {
                // END VIDEO
                mediaPlayerContainer[0].seek(mediaPlayerContainer[0].getStopTime());
                promoImageView.setVisible(true);
                mediaView.setVisible(false);
                root.setCenter(promoImageManager.getImagePane());

            } else if (event.getCode() == KeyCode.R) {
                // REWIND VIDEO
                mediaPlayerContainer[0].seek(Duration.ZERO);
                mediaPlayerContainer[0].pause();

            } else if (event.getCode() == KeyCode.I) {
                // SHOW IMAGE
                if (mediaPlayerContainer[0].getCurrentTime().equals(mediaPlayerContainer[0].getTotalDuration())) {
                    mediaPlayerContainer[0].stop();
                    mediaView.setVisible(false);
                    promoImageView.setVisible(true);
                    root.setCenter(promoImageManager.getImagePane());
                }

            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                // RESTART SEQUENCE
                currentIndex = 0;
                mediaPlayerContainer[0].stop();
                mediaPlayerContainer[0].dispose();
                mediaPlayerContainer[0] = videoPlayerService.loadAndPlayVideo(videoFiles.get(currentIndex));
                mediaView.setMediaPlayer(mediaPlayerContainer[0]);
                mediaPlayerContainer[0].play();
                mediaPlayerContainer[0].pause();

            } else if (event.getCode() == KeyCode.N) {
                mediaPlayerContainer[0].seek(mediaPlayerContainer[0].getCurrentTime().add(Duration.millis(10000)));
            }
        });
        return scene;
    }
}
