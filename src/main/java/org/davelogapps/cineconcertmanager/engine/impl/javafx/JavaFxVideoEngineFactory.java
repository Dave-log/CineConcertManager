package org.davelogapps.cineconcertmanager.engine.impl.javafx;

import org.davelogapps.cineconcertmanager.engine.VideoEngine;
import org.davelogapps.cineconcertmanager.engine.VideoEngineFactory;

public class JavaFxVideoEngineFactory implements VideoEngineFactory {

    @Override
    public VideoEngine create() {
        return new JavaFxVideoEngine();
    }
}
