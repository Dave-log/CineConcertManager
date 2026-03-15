package org.davelogapps.cineconcertmanager.engine.impl.vlcj;

import org.davelogapps.cineconcertmanager.engine.VideoEngine;
import org.davelogapps.cineconcertmanager.engine.VideoEngineFactory;

public class VlcjVideoEngineFactory implements VideoEngineFactory {

    @Override
    public VideoEngine create() {
        return new VlcjVideoEngine();
    }
}
