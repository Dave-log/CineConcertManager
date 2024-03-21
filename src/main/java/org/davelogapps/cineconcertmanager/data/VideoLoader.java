package org.davelogapps.cineconcertmanager.data;

import org.davelogapps.cineconcertmanager.model.VideoFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
    This class will be responsible to load videos from a directory specified by user. It includes methods which can
    scan the directory and retrieve video files.
 */

public class VideoLoader {
    public List<VideoFile> loadVideosFromDirectory(String directoryPath) {
        List<VideoFile> videoFiles = new ArrayList<>();
        File directory = new File(directoryPath);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isVideoFile(file.getName())) {
                        String filename = file.getName();
                        String filePath = file.getAbsolutePath();
                        String alias = filename.substring(0, filename.lastIndexOf("."));
                        videoFiles.add(new VideoFile(filename, filePath, alias));
                    }
                }
            }
        }
        return videoFiles;
    }

    private boolean isVideoFile(String filename) {
        return filename.endsWith(".mp4");
    }
}
