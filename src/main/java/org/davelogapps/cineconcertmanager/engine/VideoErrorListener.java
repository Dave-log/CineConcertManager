package org.davelogapps.cineconcertmanager.engine;

@FunctionalInterface
public interface VideoErrorListener {
    void onError(String message, Throwable throwable);
}
