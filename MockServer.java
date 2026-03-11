package client;

import java.net.*;
import java.io.*;

public class MockServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serveur = new ServerSocket(7010);
        System.out.println("Mock serveur demarre sur port 7010...");

        Socket client = serveur.accept();
        System.out.println("Client connecte !");

        BufferedReader entree = new BufferedReader(
            new InputStreamReader(client.getInputStream())
        );
        DataOutputStream sortie = new DataOutputStream(client.getOutputStream());

        String requete;
        while ((requete = entree.readLine()) != null) {
            System.out.println("Recu: " + requete);
            String reponse;

            if (requete.startsWith("LOGIN"))
                reponse = "SUCCESS:user123";
            else if (requete.startsWith("REGISTER"))
                reponse = "SUCCESS";
            else if (requete.startsWith("GET_PRODUCTS"))
                reponse = "PRODUCT:p1:Laptop:8500:10|PRODUCT:p2:Souris:150:50";
            else if (requete.startsWith("GET_PRODUCT"))
                reponse = "PRODUCT:p1:Laptop:Tres bon laptop:8500:10";
            else if (requete.startsWith("GET_CART"))
                reponse = "ITEM:p1:Laptop:2:8500";
            else if (requete.startsWith("ADD_TO_CART"))
                reponse = "SUCCESS";
            else if (requete.startsWith("REMOVE_FROM_CART"))
                reponse = "SUCCESS";
            else if (requete.startsWith("PLACE_ORDER"))
                reponse = "ORDER_ID:CMD-001:AMOUNT:17000";
            else if (requete.startsWith("PAY"))
                reponse = "SUCCESS:receipt-456";
            else
                reponse = "ERROR:commande inconnue";

            sortie.writeBytes(reponse + "\n");
            System.out.println("Envoye: " + reponse);
        }
        serveur.close();
    }
}