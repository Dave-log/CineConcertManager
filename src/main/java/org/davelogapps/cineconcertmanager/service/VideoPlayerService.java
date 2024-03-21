package org.davelogapps.cineconcertmanager.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.davelogapps.cineconcertmanager.data.VideoLoader;
import org.davelogapps.cineconcertmanager.data.VideoSettingsLoader;
import org.davelogapps.cineconcertmanager.model.VideoFile;

import java.io.File;
import java.util.List;

public class VideoPlayerService {
    private final VideoLoader videoLoader;
    private final VideoSettingsLoader videoSettingsLoader;

    public VideoPlayerService() {
        this.videoLoader = new VideoLoader();
        this.videoSettingsLoader = new VideoSettingsLoader();
    }

    public List<VideoFile> loadVideos(String directoryPath) {
        List<VideoFile> videoFiles = videoLoader.loadVideosFromDirectory(directoryPath);
        videoSettingsLoader.loadVideoSettings(videoFiles);
        return videoFiles;
    }

    public MediaPlayer loadAndPlayVideo(VideoFile videoFile) {
        Media media = new Media(new File(videoFile.getFilePath()).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setMute(videoFile.isMute());

        return mediaPlayer;
    }
}
