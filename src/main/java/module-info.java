module com.example.pijava {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires org.apache.poi.ooxml;
    requires itextpdf;
    requires com.github.librepdf.openpdf;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;

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
    exports models;
}