package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OrderMenu {
    private ServerConnection conn;
    private BufferedReader entreeUtilisateur;
    private String userId;

    public OrderMenu(ServerConnection conn, String userId) {
        this.conn = conn;
        this.userId = userId;
        this.entreeUtilisateur = new BufferedReader(
            new InputStreamReader(System.in)
        );
    }

    public void validerCommande() throws Exception {
        System.out.println("\n--- VALIDATION DE COMMANDE ---");

        // Etape 1 : choix du paiement
        System.out.println("Methode de paiement:");
        System.out.println("1. Carte de credit (CREDIT_CARD)");
        System.out.println("2. Carte de debit  (DEBIT_CARD)");
        System.out.println("3. Simule          (SIMULATED)");
        System.out.print("Votre choix: ");
        String choix = entreeUtilisateur.readLine();

        String methode;
        if (choix.equals("1"))      methode = "CREDIT_CARD";
        else if (choix.equals("2")) methode = "DEBIT_CARD";
        else                        methode = "SIMULATED";

        // Etape 2 : placer la commande
        String reponse = conn.sendRequest("PLACE_ORDER:" + userId + ":" + methode);
        System.out.println("Recu du serveur: " + reponse);

        if (reponse.startsWith("ERROR")) {
            System.out.println("Commande echouee: " + reponse);
            return;
        }

        // Format: ORDER_ID:xxxxx:AMOUNT:montant
        String[] parts = reponse.split(":");
        String orderId = parts[1];
        String montant = parts.length >= 4 ? parts[3] : "0";

        System.out.println("Commande creee ! ID: " + orderId);

        // Etape 3 : paiement
        String numeroCarte = "";
        if (!methode.equals("SIMULATED")) {
            System.out.print("Numero de carte (fictif): ");
            numeroCarte = entreeUtilisateur.readLine();
        }

        String reponsePaiement = conn.sendRequest(
            "PAY:" + orderId + ":" + numeroCarte + ":" + montant
        );
        System.out.println("Recu du serveur: " + reponsePaiement);

        // Etape 4 : confirmation
        if (reponsePaiement.startsWith("SUCCESS")) {
            System.out.println("\n=============================");
            System.out.println("  COMMANDE CONFIRMEE !");
            System.out.println("  ID Commande : " + orderId);
            System.out.println("  Montant     : " + montant + " MAD");
            System.out.println("  Methode     : " + methode);
            System.out.println("  Merci pour votre achat !");
            System.out.println("=============================");
        } else {
            System.out.println("Paiement echoue: " + reponsePaiement);
        }
    }
}