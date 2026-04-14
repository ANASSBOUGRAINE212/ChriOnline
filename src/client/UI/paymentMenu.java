package client.UI;

import client.clientConnection;
import protocol.response;
import java.util.Scanner;

public class paymentMenu {

    private final clientConnection connection;
    private final Scanner scanner;

    private static final String LINE     = "╔══════════════════════════════════════════════╗";
    private static final String LINE_END = "╚══════════════════════════════════════════════╝";
    private static final String MID      = "╠══════════════════════════════════════════════╣";

    public paymentMenu(clientConnection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n" + LINE);
            System.out.println("║          💳  PAYMENT MANAGEMENT MENU         ║");
            System.out.println(MID);
            System.out.println("║  1. 💰  Process payment for order            ║");
            System.out.println("║  2. 🔍  View payment details                 ║");
            System.out.println("║  3. 🧾  Get payment receipt                  ║");
            System.out.println("║  4. 💸  Request refund                       ║");
            System.out.println("║  0. 🔙  Back to main menu                    ║");
            System.out.println(LINE_END);
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1 -> handleProcessPayment();
                    case 2 -> handleViewPayment();
                    case 3 -> handleGetReceipt();
                    case 4 -> handleRefund();
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

    private void handleProcessPayment() {
        System.out.println("\n" + LINE);
        System.out.println("║           💰  PROCESS PAYMENT                ║");
        System.out.println(MID);

        System.out.print("║  📦 Order ID: ");
        String orderId = scanner.nextLine().trim();
        if (orderId.isEmpty()) {
            System.out.println("❌ Order ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        System.out.println("║");
        System.out.println("║  💳 Payment Methods:");
        System.out.println("║    1. CREDIT_CARD");
        System.out.println("║    2. DEBIT_CARD");
        System.out.println("║    3. PAYPAL");
        System.out.println("║    4. BANK_TRANSFER");
        System.out.println("║    5. CASH");
        System.out.println("║");
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

        System.out.println("║");
        System.out.println("║  🔄 Processing payment...");
        
        response res = connection.processPayment(orderId, method);
        System.out.println("║");
        if (res.isSuccess()) {
            String[] parts = res.getMessage().split("\\|");
            System.out.println("║  ✅ " + parts[0]);
            if (parts.length > 1) {
                System.out.println("║  💳 Payment ID: " + parts[1]);
                System.out.println("║");
                System.out.println("║  💡 Save this Payment ID for your records!");
            }
        } else {
            System.out.println("║  ❌ " + res.getMessage());
        }
        System.out.println(LINE_END);
    }

    private void handleViewPayment() {
        System.out.println("\n" + LINE);
        System.out.println("║           🔍  VIEW PAYMENT DETAILS           ║");
        System.out.println(MID);

        System.out.print("║  💳 Payment ID: ");
        String paymentId = scanner.nextLine().trim();
        if (paymentId.isEmpty()) {
            System.out.println("❌ Payment ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        response res = connection.getPayment(paymentId);
        System.out.println("║");
        if (res.isSuccess()) {
            String[] lines = res.getMessage().split("\n");
            for (String line : lines) {
                System.out.println("║  " + line);
            }
        } else {
            System.out.println("║  ❌ " + res.getMessage());
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
            System.out.println("║  ❌ " + res.getMessage());
            System.out.println(LINE_END);
        }
    }

    private void handleRefund() {
        System.out.println("\n" + LINE);
        System.out.println("║           💸  REQUEST REFUND                 ║");
        System.out.println(MID);

        System.out.print("║  💳 Payment ID: ");
        String paymentId = scanner.nextLine().trim();
        if (paymentId.isEmpty()) {
            System.out.println("❌ Payment ID cannot be empty.");
            System.out.println(LINE_END);
            return;
        }

        System.out.println("║");
        System.out.print("║  ⚠️  Are you sure you want to request a refund? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("║  ❌ Refund request cancelled.");
            System.out.println(LINE_END);
            return;
        }

        System.out.println("║");
        System.out.println("║  🔄 Processing refund...");
        
        response res = connection.refundPayment(paymentId);
        System.out.println("║");
        if (res.isSuccess()) {
            System.out.println("║  ✅ " + res.getMessage());
            System.out.println("║");
            System.out.println("║  💡 The order has been cancelled and payment refunded.");
        } else {
            System.out.println("║  ❌ " + res.getMessage());
        }
        System.out.println(LINE_END);
    }
}

