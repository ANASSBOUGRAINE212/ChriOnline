package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProductMenu {
    private ServerConnection conn;
    private BufferedReader entreeUtilisateur;

    public ProductMenu(ServerConnection conn) {
        this.conn = conn;
        this.entreeUtilisateur = new BufferedReader(
            new InputStreamReader(System.in)
        );
    }

    public void afficherCatalogue() throws Exception {
        System.out.println("\n--- CATALOGUE DES PRODUITS ---");
        String reponse = conn.sendRequest("GET_PRODUCTS");
        System.out.println("Recu du serveur: " + reponse);

        // Format attendu: PRODUCT:id:nom:prix:stock|PRODUCT:id:nom:prix:stock
        if (reponse.startsWith("ERROR")) {
            System.out.println("Erreur: " + reponse);
            return;
        }

        String[] produits = reponse.split("\\|");
        System.out.println("\n ID       | NOM                  | PRIX    | STOCK");
        System.out.println("---------|----------------------|---------|-------");
        for (String p : produits) {
            String[] parts = p.split(":");
            // parts[0]=PRODUCT, parts[1]=id, parts[2]=nom, parts[3]=prix, parts[4]=stock
            if (parts.length >= 5) {
                System.out.printf(" %-8s| %-21s| %-8s| %s%n",
                    parts[1], parts[2], parts[3] + " MAD", parts[4]);
            }
        }
    }

    public void afficherDetailProduit() throws Exception {
        System.out.print("\nEntrez l'ID du produit: ");
        String productId = entreeUtilisateur.readLine();

        String reponse = conn.sendRequest("GET_PRODUCT:" + productId);
        System.out.println("Recu du serveur: " + reponse);

        if (reponse.startsWith("ERROR")) {
            System.out.println("Produit introuvable.");
            return;
        }

        // Format: PRODUCT:id:nom:description:prix:stock
        String[] parts = reponse.split(":");
        if (parts.length >= 6) {
            System.out.println("ID          : " + parts[1]);
            System.out.println("Nom         : " + parts[2]);
            System.out.println("Description : " + parts[3]);
            System.out.println("Prix        : " + parts[4] + " MAD");
            System.out.println("Stock       : " + parts[5] + " unites");
        }
    }
}