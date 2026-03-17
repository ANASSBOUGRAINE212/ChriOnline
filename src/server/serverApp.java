package server;

import database.databaseInitializer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class serverApp {
    public static void main(String[] args) {
        System.out.println("Starting ChriOnline Server...");
        
        System.out.println("Initializing database");
        databaseInitializer.init();       
        sessionManager sm = sessionManager.getInstance();
        System.out.println("Database ready!");

        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Listening on port 5000");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new Thread(new clientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}