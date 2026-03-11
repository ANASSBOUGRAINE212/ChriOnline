package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CartMenu {
    private ServerConnection conn;
    private BufferedReader entreeUtilisateur;
    private String userId;

    public CartMenu(ServerConnection conn, String userId) {
        this.conn = conn;
        this.userId = userId;
        this.entreeUtilisateur = new BufferedReader(
            new InputStreamReader(System.in)
        );
    }

    public void afficherPanier() throws Exception {
        System.out.println("\n--- MON PANIER ---");
        String reponse = conn.sendRequest("GET_CART:" + userId);
        System.out.println("Recu du serveur: " + reponse);

        if (reponse.startsWith("ERROR") || reponse.equals("EMPTY")) {
            System.out.println("Votre panier est vide.");
            return;
        }

        // Format: ITEM:productId:nom:qty:prixUnit|ITEM:...
        String[] items = reponse.split("\\|");
        double total = 0;
        System.out.println("\n NOM                  | QTE | PRIX UNIT | SOUS-TOTAL");
        System.out.println("----------------------|-----|-----------|----------");
        for (String item : items) {
            String[] parts = item.split(":");
            // parts[0]=ITEM, parts[1]=id, parts[2]=nom, parts[3]=qty, parts[4]=prix
            if (parts.length >= 5) {
                double sousTotal = Integer.parseInt(parts[3]) * Double.parseDouble(parts[4]);
                total += sousTotal;
                System.out.printf(" %-21s| %-4s| %-10s| %.2f MAD%n",
                    parts[2], parts[3], parts[4] + " MAD", sousTotal);
            }
        }
        System.out.println("----------------------------------------------");
        System.out.printf(" TOTAL: %.2f MAD%n", total);
    }

    public void ajouterAuPanier() throws Exception {
        System.out.print("ID du produit a ajouter: ");
        String productId = entreeUtilisateur.readLine();
        System.out.print("Quantite: ");
        String quantite = entreeUtilisateur.readLine();

        String reponse = conn.sendRequest(
            "ADD_TO_CART:" + userId + ":" + productId + ":" + quantite
        );
        System.out.println("Recu du serveur: " + reponse);

        if (reponse.equals("SUCCESS")) {
            System.out.println("Produit ajoute au panier !");
        } else {
            System.out.println("Erreur: " + reponse);
        }
    }

    public void supprimerDuPanier() throws Exception {
        System.out.print("ID du produit a supprimer: ");
        String productId = entreeUtilisateur.readLine();

        String reponse = conn.sendRequest(
            "REMOVE_FROM_CART:" + userId + ":" + productId
        );
        System.out.println("Recu du serveur: " + reponse);

        if (reponse.equals("SUCCESS")) {
            System.out.println("Produit supprime du panier.");
        } else {
            System.out.println("Erreur: " + reponse);
        }
    }
}