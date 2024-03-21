package org.davelogapps.cineconcertmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
    This class represents an individual video. It contains information about video file.
 */
@Data
@AllArgsConstructor
public class VideoFile {
    private String filename;
    private String filePath;
    private String alias;
}
