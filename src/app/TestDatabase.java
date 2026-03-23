package app;

import database.DatabaseConnection;
import models.Cart;
import models.Product;
import models.CartItem;

public class TestDatabase {

    private static final String BOLD =
        "╔══════════════════════════════════════════════╗";
    private static final String BOLD_END =
        "╚══════════════════════════════════════════════╝";
    private static final String THIN =
        "------------------------------------------------";

    public static void main(String[] args) {

        System.out.println();
        System.out.println(BOLD);
        System.out.println("║      CHRIONLINE - TEST DES PRODUITS          ║");
        System.out.println(BOLD_END);
        System.out.println();

        // ── Connexion ─────────────────────────────────────────────
        System.out.println("TEST CONNEXION BASE DE DONNEES");
        System.out.println(THIN);
        try {
            DatabaseConnection.getConnection();
            System.out.println("OK - Connexion MySQL reussie !");
        } catch (Exception e) {
            System.out.println("ERREUR - Connexion echouee : " + e.getMessage());
            System.out.println("Arret des tests.");
            return;
        }

        // ── Produit de test ───────────────────────────────────────
        Product p = new Product();
        p.setProductId("p1");

        // ── TEST 1 ────────────────────────────────────────────────
        System.out.println();
        System.out.println("TEST 1 - getProductInfo()");
        System.out.println(THIN);
        String details = p.getProductInfo();
        if (details != null)
            System.out.println(details);
        else
            System.out.println("ERREUR - Aucun detail retourne");

        // ── TEST 2 ────────────────────────────────────────────────
        System.out.println();
        System.out.println("TEST 2 - getPrice()");
        System.out.println(THIN);
        double prix = p.getPrice();
        if (prix >= 0)
            System.out.println("Prix recupere : " + prix + " MAD");
        else
            System.out.println("ERREUR - Prix introuvable");

        // ── TEST 3 ────────────────────────────────────────────────
        System.out.println();
        System.out.println("TEST 3 - checkStock()");
        System.out.println(THIN);
        boolean dispo = p.checkStock();
        System.out.println("Disponible : " + dispo);

        // ── TEST 4 ────────────────────────────────────────────────
        System.out.println();
        System.out.println("TEST 4 - updateStock(-2) : retrait de 2 unites");
        System.out.println(THIN);
        p.updateStock(-2);

        // ── TEST 5 ────────────────────────────────────────────────
        System.out.println();
        System.out.println("TEST 5 - updateStock(+5) : ajout de 5 unites");
        System.out.println(THIN);
        p.updateStock(5);

        // ── TEST 6 ────────────────────────────────────────────────
        System.out.println();
        System.out.println("TEST 6 - updateStock(-9999) : retrait impossible");
        System.out.println(THIN);
        try {
            p.updateStock(-9999);
        } catch (IllegalArgumentException e) {
            System.out.println("Exception capturee correctement : " + e.getMessage());
        }

        // ── TEST 7 ────────────────────────────────────────────────
        System.out.println();
        System.out.println("TEST 7 - addItem()");
        System.out.println(THIN);
        Cart cart = new Cart("u1");
        cart.addItem(p, 2);

        // ── TEST 8 ────────────────────────────────────────────────
        System.out.println();
        System.out.println("TEST 8 - getItemCount()");
        System.out.println(THIN);
        int count = cart.getItemCount();
        System.out.println("Nombre total d'articles : " + count);

        // ── TEST 9 ────────────────────────────────────────────────
        System.out.println();
        System.out.println("TEST 9 - updateQuantity()");
        System.out.println(THIN);
     // ✅ déclaration correcte
        CartItem item = new CartItem("item1", p, 5, p.getPrice());

        System.out.println("Quantite avant : " + item.getQuantity());

        item.updateQuantity(10);
        System.out.println("Quantite apres : " + item.getQuantity());

        try {
            item.updateQuantity(-1);
        } catch (IllegalArgumentException e) {
            System.out.println("Exception capturee : " + e.getMessage());
        }

        // ── VERIFICATION FINALE ───────────────────────────────────
        System.out.println();
        System.out.println("VERIFICATION FINALE - etat du produit apres tests");
        System.out.println(THIN);
        System.out.println(p.getProductInfo());

        System.out.println();
        System.out.println(BOLD);
        System.out.println("║         TOUS LES TESTS TERMINES              ║");
        System.out.println(BOLD_END);
        System.out.println();
    }
}
