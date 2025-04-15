module com.example.pijava {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires transitive javafx.graphics;

    opens com.example.pijava to javafx.fxml;
    opens controllers to javafx.fxml;
    opens entities to javafx.base, javafx.fxml;
    opens services to javafx.fxml;
    opens utils to javafx.fxml;

    exports com.example.pijava;
    exports controllers;
    exports entities;
    exports services;
    exports utils;
}