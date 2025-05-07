module com.example.pijava {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.json;
    requires java.net.http;
    requires transitive javafx.graphics;
    requires javafx.media;
    requires itextpdf;
    requires com.github.librepdf.openpdf;
    requires java.desktop;
    requires org.apache.poi.ooxml;

    opens com.example.pijava to javafx.fxml;
    opens Controllers to javafx.fxml;
    opens Controllers.Forum to javafx.fxml;
    opens entite to javafx.base, javafx.fxml;
    opens service to javafx.fxml;
    opens utils to javafx.fxml;
    opens Controllers.Navigation to javafx.fxml;
    opens models to javafx.base, javafx.fxml;
    exports com.example.pijava;
    exports Controllers;
    exports Controllers.Forum;
    exports entite;
    exports service;
    exports utils;
    exports Controllers.Navigation;
}