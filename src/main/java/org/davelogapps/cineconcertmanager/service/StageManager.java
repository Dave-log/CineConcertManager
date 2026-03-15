package org.davelogapps.cineconcertmanager.service;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.davelogapps.cineconcertmanager.engine.VideoEngine;
import org.davelogapps.cineconcertmanager.model.VideoFile;
import org.davelogapps.cineconcertmanager.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class StageManager {

    private final Stage stage;
    private final VideoPlaybackService videoPlaybackService;
    private final PromoImageManager promoImageManager;
    private final String directoryPath;

    private List<VideoFile> videoFiles = new ArrayList<>();
    private int currentIndex = -1;
    private boolean promoVisible = true;

    private VideoEngine currentVideoEngine;

    private BorderPane root;
    private Slider timeSlider;
    private Label fileNameLabel;
    private Rectangle blackOverlay;

    public StageManager(Stage stage, VideoPlaybackService videoPlaybackService, String directoryPath) {
        this.stage = stage;
        this.videoPlaybackService = videoPlaybackService;
        this.directoryPath = directoryPath;
        this.promoImageManager = new PromoImageManager(directoryPath);
    }

    public void setupAndShowVideoScene() {
        videoFiles = videoPlaybackService.loadVideos(directoryPath);

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

        Scene scene = buildScene();

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

            scene.setCursor(Cursor.NONE);
        });

        scene.setOnMouseMoved(event -> {
            scene.setCursor(Cursor.DEFAULT);

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
            if (!isChanging && currentVideoEngine != null) {
                double percent = timeSlider.getValue() / 100.0;
                currentVideoEngine.setProgress(percent);
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
        if (currentVideoEngine == null || promoVisible) {
            return;
        }

        if (currentVideoEngine.isPlaying()) {
            currentVideoEngine.pause();
        } else {
            currentVideoEngine.play();
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
        if (currentVideoEngine == null || promoVisible) {
            return;
        }

        showPromoWithFade();
    }

    private void rewindCurrentVideo() {
        if (currentVideoEngine == null || promoVisible || currentIndex < 0) {
            return;
        }

        playVideoWithFade(currentIndex);
    }

    private void restartSequence() {
        disposeCurrentVideoEngine();
        currentIndex = -1;
        promoVisible = true;
        fileNameLabel.setText("En attente...");
        timeSlider.setValue(0);
        root.setCenter(promoImageManager.getNextPromoPane());
    }

    private void playVideoWithFade(int index) {
        FadeTransition fadeToBlack = new FadeTransition(Duration.millis(800), blackOverlay);
        fadeToBlack.setFromValue(0.0);
        fadeToBlack.setToValue(1.0);

        fadeToBlack.setOnFinished(e -> {
            disposeCurrentVideoEngine();

            currentIndex = index;
            promoVisible = false;

            VideoFile videoFile = videoFiles.get(index);
            currentVideoEngine = videoPlaybackService.createVideoEngine();

            fileNameLabel.setText(videoFile.getFilename().replace("[mute]", "").trim());

            currentVideoEngine.setMute(videoFile.isMute());

            currentVideoEngine.setOnReady(() -> {
                FadeTransition fadeFromBlack = new FadeTransition(Duration.millis(800), blackOverlay);
                fadeFromBlack.setFromValue(1.0);
                fadeFromBlack.setToValue(0.0);
                fadeFromBlack.play();
            });

            currentVideoEngine.setOnEnd(this::showPromoWithFade);

            currentVideoEngine.setOnError((message, throwable) -> {
                System.err.println("Video error: " + message);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                showPromoWithFade();
            });

            currentVideoEngine.setOnProgress(progress -> {
                if (!timeSlider.isValueChanging()) {
                    timeSlider.setValue(progress * 100.0);
                }
            });

            Node visualNode = currentVideoEngine.getVisualNode();
            root.setCenter(visualNode);

            if (visualNode instanceof ImageView imageView) {
                imageView.fitWidthProperty().bind(stage.getScene().widthProperty());
                imageView.fitHeightProperty().bind(stage.getScene().heightProperty());
            }

            if (currentVideoEngine.getVisualNode() instanceof MediaView mediaView) {
                mediaView.fitWidthProperty().bind(stage.getScene().widthProperty());
                mediaView.fitHeightProperty().bind(stage.getScene().heightProperty());
            }

            currentVideoEngine.load(videoFile);
        });

        fadeToBlack.play();
    }

    private void showPromoWithFade() {
        FadeTransition fadeToBlack = new FadeTransition(Duration.millis(800), blackOverlay);
        fadeToBlack.setFromValue(0.0);
        fadeToBlack.setToValue(1.0);

        fadeToBlack.setOnFinished(e -> {
            disposeCurrentVideoEngine();

            promoVisible = true;
            root.setCenter(promoImageManager.getNextPromoPane());
            timeSlider.setValue(0);
            fileNameLabel.setText("En attente...");

            FadeTransition fadeFromBlack = new FadeTransition(Duration.millis(800), blackOverlay);
            fadeFromBlack.setFromValue(1.0);
            fadeFromBlack.setToValue(0.0);
            fadeFromBlack.play();
        });

        fadeToBlack.play();
    }

    private void disposeCurrentVideoEngine() {
        if (currentVideoEngine != null) {
            try {
                currentVideoEngine.stop();
            } catch (Exception ignored) {
            }

            try {
                currentVideoEngine.dispose();
            } catch (Exception ignored) {
            }

            currentVideoEngine = null;
        }
    }

}
