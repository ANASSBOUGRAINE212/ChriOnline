package client.UI;

import client.clientConnection;
import protocol.response;
import java.util.Scanner;

public class orderMenu {

    private final clientConnection connection;
    private final Scanner scanner;

    private static final String LINE     = "╔══════════════════════════════════════════════╗";
    private static final String LINE_END = "╚══════════════════════════════════════════════╝";
    private static final String MID      = "╠══════════════════════════════════════════════╣";

    public orderMenu(clientConnection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n" + LINE);
            System.out.println("║          📦  ORDER MANAGEMENT MENU           ║");
            System.out.println(MID);
            System.out.println("║  1. 🛒  Create order from cart               ║");
            System.out.println("║  2. 📋  View my orders                       ║");
            System.out.println("║  3. 🔍  Get order details                    ║");
            System.out.println("║  4. ❌  Cancel order                         ║");
            System.out.println("║  5. 💳  Process payment                      ║");
            System.out.println("║  6. 🧾  Get payment receipt                  ║");
            System.out.println("║  0. 🔙  Back to main menu                    ║");
            System.out.println(LINE_END);
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1 -> handleCreateOrder();
                    case 2 -> handleListOrders();
                    case 3 -> handleGetOrder();
                    case 4 -> handleCancelOrder();
                    case 5 -> handleProcessPayment();
                    case 6 -> handleGetReceipt();
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

    private void handleCreateOrder() {
        System.out.println("\n" + LINE);
        System.out.println("║           🛒  CREATE ORDER FROM CART         ║");
        System.out.println(MID);

        response res = connection.createOrder();
        if (res.isSuccess()) {
            String[] parts = res.getMessage().split("\\|");
            System.out.println("✅ " + parts[0]);
            if (parts.length > 1) {
                System.out.println("📦 Order ID: " + parts[1]);
                System.out.println("\n💡 Proceed to payment to confirm your order!");
            }
        } else {
            System.out.println("❌ " + res.getMessage());
        }
        System.out.println(LINE_END);
    }

    private void handleListOrders() {
        System.out.println("\n" + LINE);
        System.out.println("║             📋  MY ORDERS                    ║");
        System.out.println(MID);

        response res = connection.listOrders();
        if (res.isSuccess()) {
            System.out.println(res.getMessage().replace("\\n", "\n"));
        } else {
            System.out.println("❌ " + res.getMessage());
        }
        System.out.println(LINE_END);
    }

    private void handleGetOrder() {
        System.out.println("\n" + LINE);
        System.out.println("║           🔍  GET ORDER DETAILS              ║");
        System.out.println(MID);

        System.out.print("║  📦 Order ID: ");
        String orderId = scanner.nextLine().trim();
        if (orderId.isEmpty()) {
            System.out.println("❌ Order ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        response res = connection.getOrder(orderId);
        if (res.isSuccess()) {
            System.out.println(res.getMessage().replace("\\n", "\n"));
        } else {
            System.out.println("❌ " + res.getMessage());
        }
        System.out.println(LINE_END);
    }

    private void handleCancelOrder() {
        System.out.println("\n" + LINE);
        System.out.println("║             ❌  CANCEL ORDER                 ║");
        System.out.println(MID);

        System.out.print("║  📦 Order ID to cancel: ");
        String orderId = scanner.nextLine().trim();
        if (orderId.isEmpty()) {
            System.out.println("❌ Order ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        System.out.print("║  ⚠️  Are you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("❌ Cancelled.");
            System.out.println(LINE_END);
            return;
        }

        response res = connection.cancelOrder(orderId);
        System.out.println(res.isSuccess() ? "✅ " + res.getMessage() : "❌ " + res.getMessage());
        System.out.println(LINE_END);
    }

    private void handleProcessPayment() {
        System.out.println("\n" + LINE);
        System.out.println("║           💳  PROCESS PAYMENT                ║");
        System.out.println(MID);

        System.out.print("║  📦 Order ID: ");
        String orderId = scanner.nextLine().trim();
        if (orderId.isEmpty()) {
            System.out.println("❌ Order ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        System.out.println("║  Payment Methods:");
        System.out.println("║    1. CREDIT_CARD");
        System.out.println("║    2. DEBIT_CARD");
        System.out.println("║    3. PAYPAL");
        System.out.println("║    4. BANK_TRANSFER");
        System.out.println("║    5. CASH");
        System.out.print("║  Choose method (1-5): ");

        String methodChoice = scanner.nextLine().trim();
        String method;
        switch (methodChoice) {
            case "1" -> method = "CREDIT_CARD";
            case "2" -> method = "DEBIT_CARD";
            case "3" -> method = "PAYPAL";
            case "4" -> method = "BANK_TRANSFER";
            case "5" -> method = "CASH";
            default -> {
                System.out.println("❌ Invalid payment method.");
                System.out.println(LINE_END);
                return;
            }
        }

        response res = connection.processPayment(orderId, method);
        if (res.isSuccess()) {
            String[] parts = res.getMessage().split("\\|");
            System.out.println("✅ " + parts[0]);
            if (parts.length > 1) {
                System.out.println("💳 Payment ID: " + parts[1]);
            }
        } else {
            System.out.println("❌ " + res.getMessage());
        }
        System.out.println(LINE_END);
    }

    private void handleGetReceipt() {
        System.out.println("\n" + LINE);
        System.out.println("║           🧾  GET PAYMENT RECEIPT            ║");
        System.out.println(MID);

        System.out.print("║  💳 Payment ID: ");
        String paymentId = scanner.nextLine().trim();
        if (paymentId.isEmpty()) {
            System.out.println("❌ Payment ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        response res = connection.getReceipt(paymentId);
        if (res.isSuccess()) {
            System.out.println(res.getMessage().replace("\\n", "\n"));
        } else {
            System.out.println("❌ " + res.getMessage());
        }
        System.out.println(LINE_END);
    }
}
