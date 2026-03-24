package client.UI;

import client.clientConnection;
import java.util.Scanner;
import protocol.response;

public class productMenu {

    public void show(clientConnection connection, Scanner scanner, boolean isAdmin) {
        int choice = 0;
        do {
            System.out.println("\n📦 Product Management Menu");
            System.out.println("─────────────────────────────");
            System.out.println("1. 🔍 Get product details");
            if (isAdmin) {
                System.out.println("2. ➕ Add product");
                System.out.println("3. 🗑️  Delete product");
                System.out.println("4. 📝 Update product");
            }
            System.out.println("0. 🔙 Back to main menu");
            System.out.println("─────────────────────────────");
            System.out.print("Please select an option: ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1: handleGetProduct(connection, scanner); break;
                    case 2: if (isAdmin) handleAddProduct(connection, scanner);
                            else System.out.println("\n❌ Invalid choice!"); break;
                    case 3: if (isAdmin) handleDeleteProduct(connection, scanner);
                            else System.out.println("\n❌ Invalid choice!"); break;
                    case 4: if (isAdmin) handleUpdateProduct(connection, scanner);
                            else System.out.println("\n❌ Invalid choice!"); break;
                    case 0: System.out.println("\n🔙 Returning to main menu..."); break;
                    default: System.out.println("\n❌ Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("\n❌ Invalid input! Please enter a number.");
                scanner.nextLine();
                choice = -1;
            }

            if (choice != 0) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        } while (choice != 0);
    }

    private void handleGetProduct(clientConnection connection, Scanner scanner) {
        System.out.println("\n🔍 Get Product Details");
        System.out.println("═══════════════════════");
        System.out.print("🔢 Enter product ID: ");
        try {
            int productId = scanner.nextInt();
            scanner.nextLine();
            response res = connection.getProduct(productId);
            if (res.isSuccess()) {
                System.out.println("\n═══════════════════════════════════════");
                System.out.println("           📦 PRODUCT DETAILS");
                System.out.println("═══════════════════════════════════════");
                System.out.println(res.getMessage().replace("\\n", "\n"));
                System.out.println("═══════════════════════════════════════");
            } else {
                System.out.println("❌ " + res.getMessage());
            }
        } catch (Exception e) {
            System.out.println("❌ Invalid product ID!");
            scanner.nextLine();
        }
    }

    private void handleAddProduct(clientConnection connection, Scanner scanner) {
        System.out.println("\n➕ Add New Product");
        System.out.println("═══════════════════════");
        System.out.print("📦 Product name: ");      String name        = scanner.nextLine().trim();
        System.out.print("📝 Description: ");       String description = scanner.nextLine().trim();
        System.out.print("🗂️  Category: ");          String category    = scanner.nextLine().trim();
        System.out.print("💰 Price: ");
        double price;
        try { price = Double.parseDouble(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("❌ Invalid price!"); return; }
        System.out.print("🏷️  Stock quantity: ");
        int stock;
        try { stock = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("❌ Invalid stock!"); return; }
        if (name.isEmpty()) { System.out.println("❌ Product name is required!"); return; }
        response res = connection.addProduct(name, description, price, stock, category);
        System.out.println(res.isSuccess() ? "✅ " + res.getMessage() : "❌ " + res.getMessage());
    }

    private void handleDeleteProduct(clientConnection connection, Scanner scanner) {
        System.out.println("\n🗑️  Delete Product");
        System.out.println("═══════════════════════");
        System.out.print("🔢 Enter product ID to delete: ");
        try {
            int productId = scanner.nextInt();
            scanner.nextLine();
            System.out.print("⚠️  Are you sure? (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("❌ Deletion cancelled."); return;
            }
            response res = connection.deleteProduct(productId);
            System.out.println(res.isSuccess() ? "✅ " + res.getMessage() : "❌ " + res.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Invalid product ID!");
            scanner.nextLine();
        }
    }

    private void handleUpdateProduct(clientConnection connection, Scanner scanner) {
        System.out.println("\n📝 Update Product");
        System.out.println("═══════════════════════");
        System.out.print("🔢 Product ID to update: ");
        int productId;
        try { productId = scanner.nextInt(); scanner.nextLine(); }
        catch (Exception e) { System.out.println("❌ Invalid ID!"); scanner.nextLine(); return; }
        System.out.print("📦 New name: ");           String name        = scanner.nextLine().trim();
        System.out.print("📝 New description: ");    String description = scanner.nextLine().trim();
        System.out.print("💰 New price: ");          String price       = scanner.nextLine().trim();
        System.out.print("🏷️  New stock: ");          String stock       = scanner.nextLine().trim();
        System.out.print("🗂️  New category: ");       String category    = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("❌ Product name is required!"); return; }
        response res = connection.updateProduct(productId, name, description, price, stock, category);
        if (res.isSuccess()) {
            System.out.println("✅ " + res.getMessage().replace("\\n", "\n"));
        } else {
            System.out.println("❌ " + res.getMessage());
        }
    }
}