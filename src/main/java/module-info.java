module com.example.loe {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires java.desktop;

    opens com.example.loe to javafx.fxml;
    opens Controllers.Forum to javafx.fxml;
    opens entite to javafx.base;

    exports com.example.loe;
    exports Controllers.Forum to javafx.fxml;

}