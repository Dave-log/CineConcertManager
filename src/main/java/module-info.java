module org.davelogapps.cineconcertmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires lombok;

    requires org.controlsfx.controls;

    opens org.davelogapps.cineconcertmanager to javafx.fxml;
    exports org.davelogapps.cineconcertmanager;
}