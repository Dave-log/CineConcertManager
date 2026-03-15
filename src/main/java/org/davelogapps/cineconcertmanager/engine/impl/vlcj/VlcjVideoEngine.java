package org.davelogapps.cineconcertmanager.engine.impl.vlcj;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import org.davelogapps.cineconcertmanager.engine.VideoEngine;
import org.davelogapps.cineconcertmanager.engine.VideoErrorListener;
import org.davelogapps.cineconcertmanager.engine.VideoProgressListener;
import org.davelogapps.cineconcertmanager.model.VideoFile;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.util.Objects;

public class VlcjVideoEngine implements VideoEngine {

    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer mediaPlayer;

    private final ImageView imageView;

    private Runnable onReady;
    private Runnable onEnd;
    private VideoErrorListener onError;
    private VideoProgressListener onProgress;

    private volatile boolean loaded;
    private volatile boolean disposed;
    private volatile boolean playing;
    private volatile boolean readyNotified;

    public VlcjVideoEngine() {
        this.imageView = new ImageView();
        this.imageView.setPreserveRatio(true);
        this.imageView.setSmooth(true);

        this.mediaPlayerFactory = new MediaPlayerFactory("--avcodec-hw=none", "--no-video-title-show");
        this.mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        this.mediaPlayer.videoSurface().set(new ImageViewVideoSurface(this.imageView));

        registerListeners();
    }

    @Override
    public Node getVisualNode() {
        return this.imageView;
    }

    @Override
    public void load(VideoFile videoFile) {
        Objects.requireNonNull(videoFile, "videoFile must not be null");

        System.out.println(">>> load: " + videoFile.getFilePath());

        loaded = false;
        playing = false;
        readyNotified = false;

        setMute(videoFile.isMute());

        boolean started = mediaPlayer.media().play(videoFile.getFilePath());

        if (!started && onError != null) {
            Platform.runLater(() ->
                    onError.onError("Impossible to load media: " + videoFile.getFilePath(), null)
            );
        }
    }

    @Override
    public void play() {
        System.out.println(">>> play()");
        if (!disposed) {
            System.out.println("VLC PLAY");
            mediaPlayer.controls().play();
        }
    }

    @Override
    public void pause() {
        if (!disposed) mediaPlayer.controls().pause();
    }

    @Override
    public void stop() {
        if (!disposed) {
            mediaPlayer.controls().stop();
            playing = false;
        }
    }

    @Override
    public void rewind() {
        if (!disposed) mediaPlayer.controls().setTime(0);
    }

    @Override
    public void dispose() {
        if (disposed) return;

        disposed = true;
        loaded = false;
        playing = false;

        try { mediaPlayer.controls().stop(); } catch (Exception ignored) {}
        try { mediaPlayer.release(); } catch (Exception ignored) {}
        try { mediaPlayerFactory.release(); } catch (Exception ignored) {}
    }

    @Override
    public void setMute(boolean mute) {
        if (!disposed) mediaPlayer.audio().setMute(mute);
    }

    @Override
    public boolean isLoaded() {
        return loaded && !disposed;
    }

    @Override
    public boolean isPlaying() {
        return playing && !disposed;
    }

    @Override
    public double getProgress() {
        if (disposed) return 0.0;

        long length = mediaPlayer.status().length();
        long time = mediaPlayer.status().time();

        if (length <= 0) return 0.0;

        return  (double)time / (double)length;
    }

    @Override
    public void setProgress(double progress) {
        if (disposed) return;

        long length = mediaPlayer.status().length();
        if (length <= 0) return;

        long target = (long)(length * progress);
        mediaPlayer.controls().setTime(target);
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

    private void registerListeners() {
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

            @Override
            public void opening(MediaPlayer mediaPlayer) {
                loaded = false;
                readyNotified = false;
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                playing = true;
                loaded = true;

                if (!readyNotified) {
                    readyNotified = true;

                    if (onReady != null) Platform.runLater(onReady);
                }
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                playing = false;
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                playing = false;
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                playing = false;

                if (onEnd != null) {
                    Platform.runLater(onEnd);
                }
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                playing = false;

                if (onError != null) {
                    Platform.runLater(() ->
                            onError.onError("VLC error", null)
                    );
                }
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                if (onProgress != null) {
                    long length = mediaPlayer.status().length();

                    if (length > 0) {
                        double progress = Math.max(0.0, Math.min(1.0, (double) newTime / (double) length));

                        Platform.runLater(() -> onProgress.onProgress(progress));
                    }
                }
            }
        });
    }
}
