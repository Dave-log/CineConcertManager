module org.davelogapps.cineconcertmanager {
    requires javafx.controls;
    requires javafx.media;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    requires org.controlsfx.controls;
    requires java.prefs;
    requires static lombok;

    exports org.davelogapps.cineconcertmanager;
    exports org.davelogapps.cineconcertmanager.data;
    exports org.davelogapps.cineconcertmanager.model;
    exports org.davelogapps.cineconcertmanager.service;
    exports org.davelogapps.cineconcertmanager.util;
}