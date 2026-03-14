package org.davelogapps.cineconcertmanager.service;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.davelogapps.cineconcertmanager.model.VideoFile;
import org.davelogapps.cineconcertmanager.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class StageManager {
    private final Stage stage;
    private final VideoPlayerService videoPlayerService;
    private final PromoImageManager promoImageManager;
    private final String directoryPath;
    private List<VideoFile> videoFiles = new ArrayList<>();
    private int currentIndex = 0;

    public StageManager(Stage stage, VideoPlayerService videoPlayerService, String directoryPath) {
        this.stage = stage;
        this.videoPlayerService = videoPlayerService;
        this.directoryPath = directoryPath;
        this.promoImageManager = new PromoImageManager(directoryPath);
    }

    public void setupAndShowVideoScene() {
        videoFiles = videoPlayerService.loadVideos(directoryPath);
        MediaPlayer[] mediaPlayerContainer = {null};

        currentIndex = -1;

        mediaPlayerContainer[0] = videoPlayerService.loadAndPlayVideo(videoFiles.getFirst());

        if (mediaPlayerContainer[0] != null) {
            MediaView mediaView = new MediaView(mediaPlayerContainer[0]);
            BorderPane root = new BorderPane();

            root.setStyle("-fx-background-color: black;");
            root.setCenter(promoImageManager.getNextPromoPane());

            Scene scene = getScene(root, mediaPlayerContainer, mediaView);

            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setAlwaysOnTop(true);
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());

            stage.show();
        }
    }

    private Scene getScene(BorderPane root, MediaPlayer[] mediaPlayerContainer, MediaView mediaView) {
        Scene scene = new Scene(root, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, Color.BLACK);

        // ----- Barre d'info : label + slider -----
        Label fileNameLabel = new Label();
        fileNameLabel.setTextFill(Color.WHITE);
        fileNameLabel.setStyle("-fx-font-size: 16px;");

        Slider timeSlider = new Slider();
        timeSlider.setMin(0);
        timeSlider.setMax(100);
        timeSlider.setValue(0);
        timeSlider.setPrefWidth(400);

        HBox controlBar = new HBox(20, fileNameLabel, timeSlider);
        controlBar.setAlignment(Pos.CENTER_LEFT);
        controlBar.setPadding(new Insets(10));
        controlBar.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.6), CornerRadii.EMPTY, Insets.EMPTY)));
        controlBar.setVisible(false);
        controlBar.setOpacity(0);

        VBox overlay = new VBox();
        overlay.setAlignment(Pos.BOTTOM_CENTER);
        overlay.getChildren().add(controlBar);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(root, overlay);
        StackPane.setAlignment(overlay, Pos.BOTTOM_CENTER);

        Rectangle blackOverlay = new Rectangle(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, Color.BLACK);
        blackOverlay.setOpacity(0); // transparent au départ
        blackOverlay.setMouseTransparent(true);
        blackOverlay.widthProperty().bind(scene.widthProperty());
        blackOverlay.heightProperty().bind(scene.heightProperty());

        stack.getChildren().add(blackOverlay);

        // Transition pour afficher/masquer la barre
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), controlBar);
        fadeIn.setToValue(1.0);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), controlBar);
        fadeOut.setToValue(0.0);

        PauseTransition autoHide = new PauseTransition(Duration.seconds(3));
        autoHide.setOnFinished(e -> {
            fadeOut.play();
            fadeOut.setOnFinished(ev -> controlBar.setVisible(false));
        });

        scene.setOnMouseMoved(event -> {
            if (!controlBar.isVisible()) {
                controlBar.setVisible(true);
                fadeIn.playFromStart();
            }
            autoHide.playFromStart();
        });

        // --- Synchronisation du slider avec la vidéo ---
        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeSlider.isValueChanging() && mediaPlayerContainer[0] != null) {
                Duration duration = mediaPlayerContainer[0].getTotalDuration();
                if (!duration.isUnknown()) {
                    double percent = newVal.doubleValue() / 100.0;
                    mediaPlayerContainer[0].seek(duration.multiply(percent));
                }
            }
        });

        // Mise à jour automatique du slider pendant lecture
        mediaPlayerContainer[0].currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            Duration total = mediaPlayerContainer[0].getTotalDuration();
            if (total != null && !total.isUnknown()) {
                double progress = newTime.toMillis() / total.toMillis();
                timeSlider.setValue(progress * 100);
            }
        });

        // Mise à jour du nom de fichier
        fileNameLabel.setText(videoFiles.get(currentIndex + 1).getFilename());

        scene.setRoot(stack);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                // NEXT VIDEO
                currentIndex = (currentIndex + 1) % videoFiles.size();
                playVideoWithFade(currentIndex, mediaPlayerContainer, mediaView, timeSlider, fileNameLabel, root, blackOverlay);

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
                    playVideoWithFade(currentIndex, mediaPlayerContainer, mediaView, timeSlider, fileNameLabel, root, blackOverlay);;
                }

            } else if (event.getCode() == KeyCode.E) {
                // END VIDEO
                mediaPlayerContainer[0].seek(mediaPlayerContainer[0].getStopTime());
                mediaView.setVisible(false);
                root.setCenter(promoImageManager.getNextPromoPane());

            } else if (event.getCode() == KeyCode.R) {
                // REWIND VIDEO
                playVideoWithFade(currentIndex, mediaPlayerContainer, mediaView, timeSlider, fileNameLabel, root, blackOverlay);;

            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                // RESTART SEQUENCE
                currentIndex = 0;
                playVideoWithFade(currentIndex, mediaPlayerContainer, mediaView, timeSlider, fileNameLabel, root, blackOverlay);

            } else if (event.getCode() == KeyCode.N) {
                mediaPlayerContainer[0].seek(mediaPlayerContainer[0].getCurrentTime().add(Duration.millis(10000)));
            } else if (event.getCode() == KeyCode.ESCAPE) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
        return scene;
    }

    private void setupMediaPlayerListeners(
            MediaPlayer mediaPlayer,
            Slider timeSlider,
            Label fileNameLabel,
            BorderPane root,
            MediaView mediaView,
            Rectangle blackOverlay) {

        String alias = videoFiles.get(currentIndex).getFilename().replace("[mute]", "").trim();
        fileNameLabel.setText(alias);

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            Duration total = mediaPlayer.getTotalDuration();
            if (total != null && !total.isUnknown()) {
                double progress = newTime.toMillis() / total.toMillis();
                timeSlider.setValue(progress * 100);
            }
        });

        timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.READY ||
                        mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED ||
                        mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {

                    Duration total = mediaPlayer.getTotalDuration();
                    if (total != null && !total.isUnknown()) {
                        double percent = timeSlider.getValue() / 100.0;
                        mediaPlayer.seek(total.multiply(percent));
                    }
                }
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.stop();

            // Fade au noir
            FadeTransition fadeToBlack = new FadeTransition(Duration.millis(800), blackOverlay);
            fadeToBlack.setFromValue(0.0);
            fadeToBlack.setToValue(1.0);
            fadeToBlack.setOnFinished(e -> {
                // Masquer la vidéo
                mediaView.setVisible(false);

                // Afficher l'habillage
                Pane nextPromoPane = promoImageManager.getNextPromoPane();
                root.setCenter(nextPromoPane);

                // Fade vers transparent
                FadeTransition fadeFromBlack = new FadeTransition(Duration.millis(800), blackOverlay);
                fadeFromBlack.setFromValue(1.0);
                fadeFromBlack.setToValue(0.0);
                fadeFromBlack.play();
            });
            fadeToBlack.play();
        });
    }

    private void playVideoWithFade(
            int index,
            MediaPlayer[] mediaPlayerContainer,
            MediaView mediaView,
            Slider timeSlider,
            Label fileNameLabel,
            BorderPane root,
            Rectangle blackOverlay) {

        // Fade to black
        FadeTransition fadeToBlack = new FadeTransition(Duration.millis(800), blackOverlay);
        fadeToBlack.setFromValue(0.0);
        fadeToBlack.setToValue(1.0);

        fadeToBlack.setOnFinished(e -> {
            // Stop ancienne vidéo si existante
            if (mediaPlayerContainer[0] != null) {
                mediaPlayerContainer[0].stop();
                mediaPlayerContainer[0].dispose();
            }

            // Charger nouvelle vidéo
            mediaPlayerContainer[0] = videoPlayerService.loadAndPlayVideo(videoFiles.get(index));
            mediaView.setMediaPlayer(mediaPlayerContainer[0]);
            mediaView.setVisible(true);
            mediaView.setOpacity(1.0);
            root.setCenter(mediaView);

            // Mise à jour du label + slider + listeners
            setupMediaPlayerListeners(mediaPlayerContainer[0], timeSlider, fileNameLabel, root, mediaView, blackOverlay);

            mediaPlayerContainer[0].play();

            // Fade from black
            FadeTransition fadeFromBlack = new FadeTransition(Duration.millis(800), blackOverlay);
            fadeFromBlack.setFromValue(1.0);
            fadeFromBlack.setToValue(0.0);
            fadeFromBlack.play();
        });

        fadeToBlack.play();
    }
}
