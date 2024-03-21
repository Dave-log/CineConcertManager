module org.davelogapps.cineconcertmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires lombok;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    requires org.controlsfx.controls;

    opens org.davelogapps.cineconcertmanager to javafx.fxml;
    exports org.davelogapps.cineconcertmanager;
}