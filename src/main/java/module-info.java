module com.example.pijava {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires transitive javafx.graphics;
    requires java.desktop;

    opens com.example.pijava to javafx.fxml;
    opens Controllers to javafx.fxml;
    opens Controllers.Forum to javafx.fxml;
    opens entite to javafx.base, javafx.fxml;
    opens service to javafx.fxml;
    opens utils to javafx.fxml;
    opens Controllers.Navigation to javafx.fxml;

    exports com.example.pijava;
    exports Controllers;
    exports Controllers.Forum;
    exports entite;
    exports service;
    exports utils;
    exports Controllers.Navigation;
}