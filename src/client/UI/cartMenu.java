package client.UI;

import client.clientConnection;
import java.util.Scanner;
import protocol.response;

public class cartMenu {

    private final clientConnection connection;
    private final Scanner scanner;

    private static final String LINE     = "╔══════════════════════════════════════════════╗";
    private static final String LINE_END = "╚══════════════════════════════════════════════╝";
    private static final String MID      = "╠══════════════════════════════════════════════╣";

    // ── Constructor ───────────────────────────────────────────────
    public cartMenu(clientConnection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner    = scanner;
    }

    // ── Main menu loop ────────────────────────────────────────────
    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n" + LINE);
            System.out.println("║          🛒  SHOPPING CART MENU              ║");
            System.out.println(MID);
            System.out.println("║  1. 📋  View cart items                      ║");
            System.out.println("║  2. ➕  Add product to cart                  ║");
            System.out.println("║  3. ➖  Remove item from cart                ║");
            System.out.println("║  4. 💰  View cart total                      ║");
            System.out.println("║  5. 📦  Item count                           ║");
            System.out.println("║  6. 🔍  Get item details                     ║");
            System.out.println("║  7. 🗑️   Clear cart                          ║");
            System.out.println("║  0. 🔙  Back to main menu                    ║");
            System.out.println(LINE_END);
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1 -> handleViewCart();
                    case 2 -> handleAddToCart();
                    case 3 -> handleRemoveFromCart();
                    case 4 -> handleCartTotal();
                    case 5 -> handleItemCount();
                    case 6 -> handleGetItemDetails();
                    case 7 -> handleClearCart();
                    case 0 -> {
                        System.out.println("↩️  Returning to main menu...");
                        running = false;
                    }
                    default -> System.out.println("❌ Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input! Please enter a number.");
            }

            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    // ── 1. View cart items ────────────────────────────────────────
    private void handleViewCart() {
        System.out.println("\n📦 Fetching cart items...");
        response res = connection.getCart();
        if (res.isSuccess()) {
            System.out.println("\n" + LINE);
            System.out.println("║                🛒  MY CART                   ║");
            System.out.println(MID);
            System.out.println(res.getMessage().replace("\\n", "\n"));
            System.out.println(LINE_END);
        } else {
            System.out.println("❌ " + res.getMessage());
        }
    }

    // ── 2. Add product to cart ────────────────────────────────────
    private void handleAddToCart() {
        System.out.println("\n" + LINE);
        System.out.println("║           ➕  ADD PRODUCT TO CART             ║");
        System.out.println(MID);

        System.out.print("║  🔢 Product ID : ");
        String productIdStr = scanner.nextLine().trim();
        if (productIdStr.isEmpty()) {
            System.out.println("❌ Product ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid product ID!");
            System.out.println(LINE_END);
            return;
        }

        System.out.print("║  📦 Quantity   : ");
        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid quantity!");
            System.out.println(LINE_END);
            return;
        }

        // Get product price first
        response productRes = connection.getProduct(productId);
        if (!productRes.isSuccess()) {
            System.out.println("❌ Product not found!");
            System.out.println(LINE_END);
            return;
        }

        // Extract price from product info (simple parsing)
        double price = 0.0;
        String productInfo = productRes.getMessage();
        String[] lines = productInfo.split("\n");
        for (String line : lines) {
            if (line.contains("Price:")) {
                String priceStr = line.replaceAll("[^0-9.]", "");
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    price = 0.0;
                }
                break;
            }
        }

        response res = connection.addToCart(productId, quantity, price);
        System.out.println(res.isSuccess() ? "✅ " + res.getMessage() : "❌ " + res.getMessage());
        System.out.println(LINE_END);
    }

    // ── 3. Remove item from cart ──────────────────────────────────
    private void handleRemoveFromCart() {
        System.out.println("\n" + LINE);
        System.out.println("║        ➖  REMOVE ITEM FROM CART              ║");
        System.out.println(MID);

        System.out.print("║  🆔 Product ID to remove: ");
        String productIdStr = scanner.nextLine().trim();
        if (productIdStr.isEmpty()) {
            System.out.println("❌ Product ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(productIdStr);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid product ID!");
            System.out.println(LINE_END);
            return;
        }

        response res = connection.removeFromCart(productId);
        System.out.println(res.isSuccess() ? "✅ " + res.getMessage() : "❌ " + res.getMessage());
        System.out.println(LINE_END);
    }

    // ── 4. View cart total ────────────────────────────────────────
    private void handleCartTotal() {
        System.out.println("\n" + LINE);
        System.out.println("║            💰  CART TOTAL                    ║");
        System.out.println(MID);

        response res = connection.getCartTotal();
        if (res.isSuccess()) {
            System.out.println(res.getMessage().replace("\\n", "\n"));
        } else {
            System.out.println("❌ " + res.getMessage());
        }
        System.out.println(LINE_END);
    }

    // ── 5. Item count ─────────────────────────────────────────────
    private void handleItemCount() {
        System.out.println("\n" + LINE);
        System.out.println("║           📦  CART ITEM COUNT                ║");
        System.out.println(MID);

        response res = connection.getCartItemCount();
        System.out.println(res.isSuccess() ? res.getMessage() : "❌ " + res.getMessage());
        System.out.println(LINE_END);
    }

    // ── 6. Get item details ───────────────────────────────────────
    private void handleGetItemDetails() {
        System.out.println("\n" + LINE);
        System.out.println("║           🔍  GET ITEM DETAILS               ║");
        System.out.println(MID);

        System.out.print("║  🔢 Product ID: ");
        String productId = scanner.nextLine().trim();
        if (productId.isEmpty()) {
            System.out.println("❌ Product ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        response res = connection.getItemDetails(productId);
        if (res.isSuccess()) {
            System.out.println(res.getMessage().replace("\\n", "\n"));
        } else {
            System.out.println("❌ " + res.getMessage());
        }
        System.out.println(LINE_END);
    }

    // ── 7. Clear cart ─────────────────────────────────────────────
    private void handleClearCart() {
        System.out.println("\n" + LINE);
        System.out.println("║             🗑️   CLEAR CART                  ║");
        System.out.println(MID);

        System.out.print("║  ⚠️  Are you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("❌ Cancelled.");
            System.out.println(LINE_END);
            return;
        }

        response res = connection.clearCart();
        System.out.println(res.isSuccess() ? "✅ " + res.getMessage() : "❌ " + res.getMessage());
        System.out.println(LINE_END);
    }
}