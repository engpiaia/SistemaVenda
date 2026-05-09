module CadastroProdutos {
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;

    opens view to javafx.graphics;
    opens model to javafx.base;

    exports view;
    exports model;
    exports dao;
    exports database;
}
