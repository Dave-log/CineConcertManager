package org.davelogapps.cineconcertmanager.service;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
    private final String directoryPath;

    private List<VideoFile> videoFiles = new ArrayList<>();
    private int currentIndex = -1;
    private int preloadedIndex = -1;
    private boolean promoVisible = true;

    private MediaPlayer currentMediaPlayer;
    private MediaPlayer preloadedMediaPlayer;
    private MediaView mediaView;
    private BorderPane root;
    private Slider timeSlider;
    private Label fileNameLabel;
    private Rectangle blackOverlay;

    public StageManager(Stage stage, VideoPlayerService videoPlayerService, String directoryPath) {
        this.stage = stage;
        this.videoPlayerService = videoPlayerService;
        this.directoryPath = directoryPath;
        this.promoImageManager = new PromoImageManager(directoryPath);
    }

    public void setupAndShowVideoScene() {
        videoFiles = videoPlayerService.loadVideos(directoryPath);

        showLoadingWindow();
        preloadVideoMetadata(videoFiles);

        if (videoFiles == null || videoFiles.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Aucune vidéo trouvée");
            alert.setContentText("Le dossier sélectionné ne contient aucune vidéo .mp4 valide.");
            alert.showAndWait();
            stage.close();
            return;
        }

        root = new BorderPane();
        root.setStyle("-fx-background-color: black;");
        root.setCenter(promoImageManager.getNextPromoPane());

        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);

        Scene scene = buildScene();

        mediaView.fitWidthProperty().bind(scene.widthProperty());
        mediaView.fitHeightProperty().bind(scene.heightProperty());

        stage.setScene(scene);
        stage.setTitle("Ciné Concert Manager");
        stage.setFullScreen(true);
        stage.setAlwaysOnTop(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.show();
    }

    private Scene buildScene() {
        Scene scene = new Scene(root, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, Color.BLACK);

        fileNameLabel = new Label("En attente...");
        fileNameLabel.setTextFill(Color.WHITE);
        fileNameLabel.setStyle("-fx-font-size: 16px;");

        timeSlider = new Slider();
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

        VBox overlay = new VBox(controlBar);
        overlay.setAlignment(Pos.BOTTOM_CENTER);

        StackPane stack = new StackPane(root, overlay);
        StackPane.setAlignment(overlay, Pos.BOTTOM_CENTER);

        blackOverlay = new Rectangle(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, Color.BLACK);
        blackOverlay.setOpacity(0);
        blackOverlay.setMouseTransparent(true);
        blackOverlay.widthProperty().bind(scene.widthProperty());
        blackOverlay.heightProperty().bind(scene.heightProperty());
        stack.getChildren().add(blackOverlay);

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

        setupSliderBehavior();
        setupKeyboardShortcuts(scene);

        scene.setRoot(stack);
        return scene;
    }

    private void setupSliderBehavior() {
        timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging && currentMediaPlayer != null) {
                Duration total = currentMediaPlayer.getTotalDuration();
                if (total != null && !total.isUnknown()) {
                    double percent = timeSlider.getValue() / 100.0;
                    currentMediaPlayer.seek(total.multiply(percent));
                }
            }
        });
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case SPACE -> handleSpace();
                case P -> togglePause();
                case B -> playPreviousVideo();
                case E -> endCurrentVideo();
                case R -> rewindCurrentVideo();
                case BACK_SPACE -> restartSequence();
                case N -> skipForward10Seconds();
                case ESCAPE -> stage.setFullScreen(!stage.isFullScreen());
            }
        });
    }

    private void handleSpace() {
        if (!promoVisible) {
            return;
        }

        int nextIndex = currentIndex + 1;
        if (nextIndex >= videoFiles.size()) {
            nextIndex = 0;
        }

        playVideoWithFade(nextIndex);
    }

    private void togglePause() {
        if (currentMediaPlayer == null || promoVisible) {
            return;
        }

        MediaPlayer.Status status = currentMediaPlayer.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            currentMediaPlayer.pause();
        } else if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
            currentMediaPlayer.play();
        }
    }

    private void playPreviousVideo() {
        if (videoFiles.isEmpty()) {
            return;
        }

        int previousIndex;
        previousIndex = currentIndex - 1;

        if (previousIndex < 0) {
            return;
        }

        playVideoWithFade(previousIndex);
    }

    private void endCurrentVideo() {
        if (currentMediaPlayer == null || promoVisible) {
            return;
        }

        showPromoWithFade();
    }

    private void rewindCurrentVideo() {
        if (currentMediaPlayer == null || promoVisible || currentIndex < 0) {
            return;
        }

        playVideoWithFade(currentIndex);
    }

    private void restartSequence() {
        stopAndDisposeCurrentPlayer();
        disposePreloadedPlayer();
        currentIndex = -1;
        promoVisible = true;
        fileNameLabel.setText("En attente...");
        timeSlider.setValue(0);
        root.setCenter(promoImageManager.getNextPromoPane());
    }

    private void skipForward10Seconds() {
        if (currentMediaPlayer == null || promoVisible) {
            return;
        }

        Duration current = currentMediaPlayer.getCurrentTime();
        Duration total = currentMediaPlayer.getTotalDuration();
        Duration target = current.add(Duration.seconds(10));

        if (total != null && !total.isUnknown() && target.greaterThan(total)) {
            target = total;
        }

        currentMediaPlayer.seek(target);
    }

    private void playVideoWithFade(int index) {
        FadeTransition fadeToBlack = new FadeTransition(Duration.millis(800), blackOverlay);
        fadeToBlack.setFromValue(0.0);
        fadeToBlack.setToValue(1.0);

        fadeToBlack.setOnFinished(e -> {
            stopAndDisposeCurrentPlayer();

            currentIndex = index;
            promoVisible = false;

            if (preloadedMediaPlayer != null && preloadedIndex == index) {
                currentMediaPlayer = preloadedMediaPlayer;
                preloadedMediaPlayer = null;
                preloadedIndex = -1;
                System.out.println("Lecture depuis préchargement");
            } else {
                currentMediaPlayer = videoPlayerService.loadAndPlayVideo(videoFiles.get(index));
                System.out.println("Lecture normale");
            }

            mediaView.setMediaPlayer(currentMediaPlayer);
            mediaView.setVisible(true);
            mediaView.setOpacity(1.0);
            root.setCenter(mediaView);

            setupMediaPlayerListeners(currentMediaPlayer);

            currentMediaPlayer.play();
            preloadNextVideo();

            FadeTransition fadeFromBlack = new FadeTransition(Duration.millis(800), blackOverlay);
            fadeFromBlack.setFromValue(1.0);
            fadeFromBlack.setToValue(0.0);
            fadeFromBlack.play();
        });

        fadeToBlack.play();
    }

    private void showPromoWithFade() {
        FadeTransition fadeToBlack = new FadeTransition(Duration.millis(800), blackOverlay);
        fadeToBlack.setFromValue(0.0);
        fadeToBlack.setToValue(1.0);

        fadeToBlack.setOnFinished(e -> {
            if (currentMediaPlayer != null) {
                currentMediaPlayer.stop();
            }

            promoVisible = true;
            mediaView.setVisible(false);
            root.setCenter(promoImageManager.getNextPromoPane());
            timeSlider.setValue(0);

            FadeTransition fadeFromBlack = new FadeTransition(Duration.millis(800), blackOverlay);
            fadeFromBlack.setFromValue(1.0);
            fadeFromBlack.setToValue(0.0);
            fadeFromBlack.play();
        });

        fadeToBlack.play();
    }

    private void setupMediaPlayerListeners(MediaPlayer mediaPlayer) {
        String alias = videoFiles.get(currentIndex).getFilename().replace("[mute]", "").trim();
        fileNameLabel.setText(alias);

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            Duration total = mediaPlayer.getTotalDuration();
            if (total != null && !total.isUnknown() && !timeSlider.isValueChanging()) {
                double progress = newTime.toMillis() / total.toMillis();
                timeSlider.setValue(progress * 100);
            }
        });

        mediaPlayer.setOnEndOfMedia(this::showPromoWithFade);

        mediaPlayer.setOnError(() -> {
            System.err.println("Erreur MediaPlayer : " + mediaPlayer.getError());
            showPromoWithFade();
        });

        if (mediaPlayer.getMedia() != null) {
            mediaPlayer.getMedia().setOnError(() -> {
                System.err.println("Erreur Media : " + mediaPlayer.getMedia().getError());
                showPromoWithFade();
            });
        }
    }

    private void stopAndDisposeCurrentPlayer() {
        if (currentMediaPlayer != null) {
            try {
                currentMediaPlayer.stop();
            } catch (Exception ignored) {
            }

            try {
                currentMediaPlayer.dispose();
            } catch (Exception ignored) {
            }

            currentMediaPlayer = null;
        }
    }

    private MediaPlayer createMediaPlayer(VideoFile videoFile) {
        MediaPlayer player = videoPlayerService.loadAndPlayVideo(videoFile);
        player.pause();
        return player;
    }

    private void preloadNextVideo() {
        if (videoFiles.isEmpty()) {
            return;
        }

        int nextIndex = currentIndex + 1;

        if (nextIndex >= videoFiles.size()) {
            nextIndex = 0;
        }

        // already preloaded
        if (preloadedMediaPlayer != null && preloadedIndex == nextIndex) {
            return;
        }

        disposePreloadedPlayer();

        try {
            preloadedMediaPlayer = createMediaPlayer(videoFiles.get(nextIndex));
            preloadedIndex = nextIndex;

            System.out.println("Préchargée : " + videoFiles.get(nextIndex).getFilename());

        } catch (Exception e) {

            System.err.println("Erreur préchargement : " + e.getMessage());
            preloadedMediaPlayer = null;
            preloadedIndex = -1;
        }
    }

    private void disposePreloadedPlayer() {
        if (preloadedMediaPlayer != null) {
            try {
                preloadedMediaPlayer.stop();
            } catch (Exception ignored) {}

            try {
                preloadedMediaPlayer.dispose();
            } catch (Exception ignored) {}

            preloadedMediaPlayer = null;
            preloadedIndex = -1;
        }
    }

    private void preloadVideoMetadata(List<VideoFile> videos) {
        for (VideoFile video : videos) {
            try {
                Media media = new Media(new File(video.getFilePath()).toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setOnReady(() -> {
                    System.out.println("Préchargée : " + video.getFilename() + " durée=" + player.getTotalDuration());
                    player.dispose();
                });
                player.setOnError(() -> {
                    System.err.println("Erreur vidéo : " + video.getFilename());
                    player.dispose();
                });
            } catch (Exception e) {
                System.err.println("Impossible de charger : " + video.getFilename());
            }
        }
    }

    private void showLoadingWindow() {
        Stage loadingStage = new Stage();

        Label label = new Label("Chargement des vidéos...");
        ProgressIndicator indicator = new ProgressIndicator();

        VBox box = new VBox(20, indicator, label);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color:black;-fx-padding:40;");

        Scene scene = new Scene(box, 300, 200);

        loadingStage.setScene(scene);
        loadingStage.setTitle("Chargement");
        loadingStage.show();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                preloadVideoMetadata(videoFiles);
                return null;
            }
        };

        task.setOnSucceeded(e -> loadingStage.close());

        new Thread(task).start();
    }
}
