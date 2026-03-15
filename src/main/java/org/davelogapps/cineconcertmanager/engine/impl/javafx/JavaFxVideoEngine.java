package org.davelogapps.cineconcertmanager.engine.impl.javafx;

import javafx.scene.Node;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.davelogapps.cineconcertmanager.engine.VideoEngine;
import org.davelogapps.cineconcertmanager.engine.VideoErrorListener;
import org.davelogapps.cineconcertmanager.engine.VideoProgressListener;
import org.davelogapps.cineconcertmanager.model.VideoFile;

import java.io.File;

public class JavaFxVideoEngine implements VideoEngine {

    private MediaPlayer mediaPlayer;
    private final MediaView mediaView;

    private Runnable onReady;
    private Runnable onEnd;
    private VideoErrorListener onError;
    private VideoProgressListener onProgress;

    private boolean loaded = false;
    private boolean disposed = false;
    private boolean mute = false;

    public JavaFxVideoEngine() {
        this.mediaView = new MediaView();
        this.mediaView.setPreserveRatio(true);
    }

    @Override
    public Node getVisualNode() {
        return mediaView;
    }

    @Override
    public void load(VideoFile videoFile) {
        disposeMediaPlayerOnly();

        try {
            Media media = new Media(new File(videoFile.getFilePath()).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setMute(mute);

            mediaView.setMediaPlayer(mediaPlayer);

            mediaPlayer.setOnReady(() -> {
                loaded = true;
                if (onReady != null) {
                    onReady.run();
                }
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                if (onEnd != null) {
                    onEnd.run();
                }
            });

            mediaPlayer.setOnError(() -> {
                if (onError != null) {
                    onError.onError("MediaPlayer error", mediaPlayer.getError());
                }
            });

            media.setOnError(() -> {
                if (onError != null) {
                    onError.onError("Media error", media.getError());
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (onProgress != null) {
                    Duration total = mediaPlayer.getTotalDuration();
                    if (total != null && !total.isUnknown() && total.toMillis() > 0) {
                        double progress = newTime.toMillis() / total.toMillis();
                        onProgress.onProgress(progress);
                    }
                }
            });
        } catch (Exception e) {
            if (onError != null) {
                onError.onError("Unable to load media", e);
            }
        }
    }

    @Override
    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void rewind() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.ZERO);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        loaded = false;
        disposeMediaPlayerOnly();
        mediaView.setMediaPlayer(null);
    }

    @Override
    public void setMute(boolean mute) {
        this.mute = mute;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(mute);
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded && !disposed;
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    @Override
    public double getProgress() {
        if (mediaPlayer == null) {
            return 0.0;
        }

        Duration total = mediaPlayer.getTotalDuration();
        Duration current = mediaPlayer.getCurrentTime();

        if (total == null || total.isUnknown() || total.toMillis() <= 0) {
            return 0.0;
        }

        return current.toMillis() / total.toMillis();
    }

    @Override
    public void setProgress(double progress) {
        if (mediaPlayer == null) {
            return;
        }

        Duration total = mediaPlayer.getTotalDuration();
        if (total == null || total.isUnknown()) {
            return;
        }

        double clamped = Math.max(0.0, Math.min(1.0, progress));
        mediaPlayer.seek(total.multiply(clamped));
    }

    @Override
    public void setOnReady(Runnable callback) {
        this.onReady = callback;
    }

    @Override
    public void setOnEnd(Runnable callback) {
        this.onEnd = callback;
    }

    @Override
    public void setOnError(VideoErrorListener listener) {
        this.onError = listener;
    }

    @Override
    public void setOnProgress(VideoProgressListener listener) {
        this.onProgress = listener;
    }

    private void disposeMediaPlayerOnly() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception ignored) {
            }

            try {
                mediaPlayer.dispose();
            } catch (Exception ignored) {
            }

            mediaPlayer = null;
        }
    }
}
