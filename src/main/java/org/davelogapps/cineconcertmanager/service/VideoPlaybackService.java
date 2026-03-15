package org.davelogapps.cineconcertmanager.service;

import org.davelogapps.cineconcertmanager.data.VideoLoader;
import org.davelogapps.cineconcertmanager.engine.VideoEngine;
import org.davelogapps.cineconcertmanager.engine.VideoEngineFactory;
import org.davelogapps.cineconcertmanager.model.VideoFile;

import java.util.List;

public class VideoPlaybackService {

    private final VideoLoader videoLoader;
    private final VideoEngineFactory videoEngineFactory;

    public VideoPlaybackService(VideoEngineFactory videoEngineFactory) {
        this.videoLoader = new VideoLoader();
        this.videoEngineFactory = videoEngineFactory;
    }

    public List<VideoFile> loadVideos(String directoryPath) {
        return videoLoader.loadVideosFromDirectory(directoryPath);
    }

    public VideoEngine createVideoEngine() {
        return videoEngineFactory.create();
    }
}
