package org.davelogapps.cineconcertmanager.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.davelogapps.cineconcertmanager.data.VideoLoader;
import org.davelogapps.cineconcertmanager.model.VideoFile;

import java.io.File;
import java.util.List;

public class VideoPlayerService {
    public MediaPlayer loadAndPlayFirstVideo(String directoryPath) {
        VideoLoader videoLoader = new VideoLoader();
        List<VideoFile> videoFiles = videoLoader.loadVideosFromDirectory(directoryPath);

        VideoFile firstVideo = videoFiles.getFirst();
        Media media = new Media(new File(firstVideo.getFilePath()).toURI().toString());
        return new MediaPlayer(media);
    }
}
