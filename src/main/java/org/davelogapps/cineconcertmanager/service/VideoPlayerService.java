package org.davelogapps.cineconcertmanager.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.davelogapps.cineconcertmanager.data.VideoLoader;
import org.davelogapps.cineconcertmanager.model.VideoFile;

import java.io.File;
import java.util.List;

public class VideoPlayerService {
    private final VideoLoader videoLoader;

    public VideoPlayerService() {
        this.videoLoader = new VideoLoader();
    }

    public List<VideoFile> loadVideos(String directoryPath) {
        return videoLoader.loadVideosFromDirectory(directoryPath);
    }

    public MediaPlayer loadAndPlayVideo(VideoFile videoFile) {
        Media media = new Media(new File(videoFile.getFilePath()).toURI().toString());
        return new MediaPlayer(media);
    }
}
