package org.davelogapps.cineconcertmanager.engine;

import javafx.scene.Node;
import org.davelogapps.cineconcertmanager.model.VideoFile;

public interface VideoEngine {

    Node getVisualNode();

    void load(VideoFile videoFile);

    void play();

    void pause();

    void stop();

    void rewind();

    void dispose();

    void setMute(boolean mute);

    boolean isLoaded();

    boolean isPlaying();

    double getProgress(); // 0.0 -> 1.0

    void setProgress(double progress); // 0.0 -> 1.0

    void setOnReady(Runnable callback);

    void setOnEnd(Runnable callback);

    void setOnError(VideoErrorListener listener);

    void setOnProgress(VideoProgressListener listener);
}
