module ChriiOnline {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.sql;
    requires java.desktop;  // For Swing components in CryptoDemoPanel
    
    opens client to javafx.graphics;
    opens client.UI to javafx.graphics;
    opens server to javafx.graphics;
    
    exports client;
    exports client.UI;
    exports server;
}
