package client;

import client.UI.authMenu;
import javafx.application.Application;
import javafx.stage.Stage;

public class clientApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Starting ChriOnline Client...");

        try {
            clientConnection connection = new clientConnection("localhost", 5000);
            System.out.println("Connected successfully!");

            authMenu menu = new authMenu();
            menu.show(connection, primaryStage);

        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args); 
    }
}