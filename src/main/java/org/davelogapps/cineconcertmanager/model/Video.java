package org.davelogapps.cineconcertmanager.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Video {
    private String path;
    private int duration;
    private boolean isMuted;
    private boolean hasPlaceholderImage;
}
