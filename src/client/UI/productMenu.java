package client.UI;

import client.clientConnection;
import java.util.Scanner;
import protocol.response;

public class productMenu {
    
    public void show(clientConnection connection, Scanner scanner) {
        int choice = 0;
        do {
            System.out.println("\n🛍️  Product Management Menu");
            System.out.println("═══════════════════════════");
            System.out.println("1. 📝 Update Product (Admin Only)");
            System.out.println("0. 🔙 Back to Main Menu");
            System.out.println("═══════════════════════════");
            System.out.print("Please select an option (0-1): ");
            
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                switch (choice) {
                    case 1:
                        updateProduct(connection, scanner);
                        break;
                    case 0:
                        System.out.println("\n🔙 Returning to main menu...");
                        break;
                    default:
                        System.out.println("\n❌ Invalid choice! Please select 0 or 1.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("\n❌ Invalid input! Please enter a number.");
                scanner.nextLine(); // clear invalid input
                choice = -1; // continue loop
            }
            
            if (choice == 1) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        } while (choice != 0);
    }
    
    private void updateProduct(clientConnection connection, Scanner scanner) {
        System.out.println("\n🛍️  Update Product (Admin Only)");
        System.out.println("═══════════════════════════════════");
        System.out.println("⚠️  This feature requires ADMIN privileges");
        
        System.out.print("🆔 Product ID: ");
        String productId = scanner.nextLine().trim();
        
        System.out.print("📦 Product Name: ");
        String productName = scanner.nextLine().trim();
        
        System.out.print("📝 Description (optional): ");
        String description = scanner.nextLine().trim();
        
        System.out.print("💰 Price (optional): ");
        String price = scanner.nextLine().trim();
        
        System.out.print("📊 Stock quantity (optional): ");
        String stock = scanner.nextLine().trim();
        
        System.out.print("🏷️  Category (optional): ");
        String category = scanner.nextLine().trim();
        
        if (productId.isEmpty() || productName.isEmpty()) {
            System.out.println("\n❌ Product ID and Product Name are required!");
            return;
        }
        
        System.out.println("\n🔄 Updating product...");
        response res = connection.updateProduct(productId, productName, description, price, stock, category);
        
        if (res.isSuccess()) {
            System.out.println("✅ Product update successful!");
            System.out.println("\n" + res.getMessage().replace("\\n", "\n"));
        } else {
            System.out.println("❌ Product update failed.");
            System.out.println("   " + res.getMessage());
        }
    }

}
