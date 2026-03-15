package app;

import database.DatabaseConnection;

public class TestDatabase {

    public static void main(String[] args) {

        System.out.println("Initialisation de la base de donnees...");

        DatabaseConnection.initializeDatabase();

        System.out.println("Termine !");
    }
}
