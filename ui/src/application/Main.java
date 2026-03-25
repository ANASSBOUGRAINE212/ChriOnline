package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            primaryStage.setTitle("ChriOnline - E-Commerce Platform");
            
            // Load login screen
            showLoginScreen();
            
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void showLoginScreen() {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(Main.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void showRegisterScreen() {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/fxml/register.fxml"));
            Scene scene = new Scene(root, 800, 650);
            scene.getStylesheets().add(Main.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void showDashboard() {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(root, 1200, 700);
            scene.getStylesheets().add(Main.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
