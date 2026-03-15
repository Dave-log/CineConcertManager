package org.davelogapps.cineconcertmanager.engine;

@FunctionalInterface
public interface VideoProgressListener {
    void onProgress(double progress);
}
