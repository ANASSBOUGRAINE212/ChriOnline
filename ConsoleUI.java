package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleUI {
    private ServerConnection conn;
    private BufferedReader entreeUtilisateur;

    public ConsoleUI() {
        this.conn = new ServerConnection();
        this.entreeUtilisateur = new BufferedReader(
            new InputStreamReader(System.in)
        );
    }

    public void start() throws Exception {
        conn.connect("localhost", 7010); 

        // Authentification
        AuthMenu authMenu = new AuthMenu(conn);
        String userId = authMenu.afficherMenu();

        // Menus apres connexion
        ProductMenu productMenu = new ProductMenu(conn);
        CartMenu cartMenu = new CartMenu(conn, userId);
        OrderMenu orderMenu = new OrderMenu(conn, userId);

        String choix;
        do {
            System.out.println("   CHRIONLINE - MENU PRINCIPAL");
            System.out.println("1. Voir le catalogue");
            System.out.println("2. Detail d'un produit");
            System.out.println("3. Voir mon panier");
            System.out.println("4. Ajouter au panier");
            System.out.println("5. Supprimer du panier");
            System.out.println("6. Valider ma commande");
            System.out.println("7. Se deconnecter");
            System.out.print("Votre choix: ");
            choix = entreeUtilisateur.readLine();

            if (choix.equals("1"))      productMenu.afficherCatalogue();
            else if (choix.equals("2")) productMenu.afficherDetailProduit();
            else if (choix.equals("3")) cartMenu.afficherPanier();
            else if (choix.equals("4")) cartMenu.ajouterAuPanier();
            else if (choix.equals("5")) cartMenu.supprimerDuPanier();
            else if (choix.equals("6")) orderMenu.validerCommande();
            else if (choix.equals("7")) {
                conn.disconnect();
                System.out.println("Deconnecte. Au revoir !");
                break;
            } else {
                System.out.println("Choix invalide.");
            }
        } while (true);
    }
}