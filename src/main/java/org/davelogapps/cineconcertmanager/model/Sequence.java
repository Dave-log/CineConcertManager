package org.davelogapps.cineconcertmanager.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Sequence {
    private String name;
    private List<Video> videos;
}
