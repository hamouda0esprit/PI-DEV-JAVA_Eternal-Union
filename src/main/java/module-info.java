module com.example.pijava {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires transitive javafx.graphics;
    requires org.apache.poi.ooxml;
    requires itextpdf;
    requires com.github.librepdf.openpdf;
    requires java.desktop;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires javafx.web;
    requires jakarta.json;
    requires java.mail;
    requires opencv;
    requires okhttp3;
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

    requires javafx.base;

    requires java.compiler;
    requires com.google.gson;

    exports models;
    requires org.json;


}