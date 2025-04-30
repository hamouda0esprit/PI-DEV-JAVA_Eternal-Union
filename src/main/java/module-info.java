module org.example.loelogin {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires java.net.http;
    requires java.desktop;
    requires java.logging;
    requires java.base;
    requires java.prefs;
    requires java.scripting;
    requires java.xml;
    requires java.compiler;
    requires java.management;
    requires java.naming;
    requires java.security.jgss;
    requires java.security.sasl;
    requires java.sql.rowset;
    requires java.transaction.xa;
    requires java.xml.crypto;
    requires jdk.httpserver;
    requires jdk.jsobject;
    requires jdk.unsupported;
    requires jdk.xml.dom;
    requires jdk.zipfs;
    requires javafx.base;
    requires javafx.graphics;
    requires jakarta.json;
    requires java.mail;
    requires opencv;

    opens org.example.loelogin to javafx.fxml;
    exports org.example.loelogin;
    
    // Export the entite and service packages
    exports entite;
    exports service;
    
    // Export the Controllers package to JavaFX
    exports Controllers;
    opens Controllers to javafx.fxml;
}