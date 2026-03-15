package client;

import java.util.Scanner;

public class clientApp {
    public static void main(String[] args) {
        System.out.println("Starting ChriOnline Client...");
        
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Connecting to server...");
            clientConnection connection = new clientConnection("localhost", 5000);
            System.out.println("Connected successfully!");

            client.UI.authMenu menu = new client.UI.authMenu();
            menu.show(connection, scanner);
            
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
            System.out.println("Please make sure the server is running and try again.");
        }
        
        System.out.println("Client disconnected.");
    }
}