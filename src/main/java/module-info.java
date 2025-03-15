module com.example.hospitalassessment {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.dotenv;

    exports com.example.hospitalassessment;
    opens com.example.hospitalassessment to javafx.fxml;
    opens com.example.hospitalassessment.models to javafx.base;
    exports com.example.hospitalassessment.controllers;
    opens com.example.hospitalassessment.controllers to javafx.fxml;
}