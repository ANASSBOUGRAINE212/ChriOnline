package client.UI;

import client.clientConnection;
import java.util.Scanner;
import protocol.response;

public class authMenu {

    public void show(clientConnection connection, Scanner scanner) {
        System.out.println("\n🌟 Welcome to ChriOnline! 🌟");
        System.out.println("═══════════════════════════════════════");
        if (!showAuthMenu(connection, scanner)) return;
        showMainMenu(connection, scanner);
    }

    private boolean showAuthMenu(clientConnection connection, Scanner scanner) {
        int choice = 0;
        do {
            System.out.println("\n🔐 Authentication Menu");
            System.out.println("─────────────────────");
            System.out.println("1. 🔑 Login");
            System.out.println("2. 📝 Register");
            System.out.println("3. 🛍️  View Products (Guest)");
            System.out.println("0. 🚪 Exit");
            System.out.println("─────────────────────");
            System.out.print("Please select an option (0-3): ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1: if (handleLogin(connection, scanner)) return true; break;
                    case 2: handleRegister(connection, scanner); break;
                    case 3: showGuestProducts(connection, scanner); break;
                    case 0: System.out.println("\n👋 Goodbye!"); return false;
                    default: System.out.println("\n❌ Invalid choice! Please select 0-3.");
                }
            } catch (Exception e) {
                System.out.println("\n❌ Invalid input! Please enter a number.");
                scanner.nextLine();
                choice = -1;
            }
        } while (choice != 0);
        return false;
    }

    private boolean handleLogin(clientConnection connection, Scanner scanner) {
        System.out.println("\n🔑 Login to Your Account");
        System.out.println("═══════════════════════");

        System.out.print("📧 Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("🔒 Password: ");
        String password = scanner.nextLine().trim();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("\n❌ Email and password cannot be empty!");
            return false;
        }

        System.out.println("\n🔄 Authenticating...");
        response res = connection.login(email, password);

        if (res.isSuccess()) {
            String[] parts = res.getMessage().split("\\|");
            if (parts.length > 1) connection.setSessionToken(parts[1]);
            if (parts.length > 2) connection.setUserRole(parts[2]);

            if ("ADMIN".equals(connection.getUserRole())) {
                System.out.println("✅ " + parts[0]);
                System.out.println("🎉 Welcome back! You have ADMIN privileges 🔑");
            } else {
                System.out.println("✅ " + parts[0]);
                System.out.println("🎉 Welcome back!");
            }
            return true;
        } else {
            System.out.println("❌ Login failed: " + res.getMessage());
            return false;
        }
    }

    private void handleRegister(clientConnection connection, Scanner scanner) {
        System.out.println("\n📝 Create New Account");
        System.out.println("═══════════════════════");

        System.out.print("👤 Choose a username: ");       String username = scanner.nextLine().trim();
        System.out.print("📧 Email address: ");           String email    = scanner.nextLine().trim();
        System.out.print("🔒 Password (min 6 chars): ");  String password = scanner.nextLine().trim();
        System.out.print("✅ Confirm password: ");         String confirm  = scanner.nextLine().trim();
        System.out.print("📍 Address (optional): ");      String address  = scanner.nextLine().trim();
        System.out.print("📞 Phone (optional): ");        String phone    = scanner.nextLine().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("\n❌ Username, email, and password are required!"); return;
        }
        if (!password.equals(confirm)) {
            System.out.println("\n❌ Passwords don't match!"); return;
        }

        System.out.println("\n🔄 Creating account...");
        response res = connection.register(username, email, password, address, phone);

        if (res.isSuccess()) {
            System.out.println("✅ " + res.getMessage());
            System.out.println("🎉 Account created successfully!");
        } else {
            System.out.println("❌ Registration failed: " + res.getMessage());
        }
    }

    private void showMainMenu(clientConnection connection, Scanner scanner) {
        System.out.println("\n🌟 Welcome to ChriOnline User Portal! 🌟");
        System.out.println("═══════════════════════════════════════");

        boolean isAdmin = connection.isAdmin();
        int choice = 0;
        do {
            System.out.println("\n📋 Main Menu");
            System.out.println("─────────────");
            System.out.println("1. 👤 View my info");
            System.out.println("2. 📊 Get my profile");
            System.out.println("3. ✏️  Update my profile");
            System.out.println("4. 🔒 Change password");
            System.out.println("5. 🛍️  View Products");
            if (isAdmin) System.out.println("6. 🔧 Product Management (Admin)");
            System.out.println("7. 🛒 Shopping Cart");
            System.out.println("8. 🚪 Logout");
            System.out.println("0. 🚪 Exit");
            System.out.println("─────────────");
            System.out.print("Please select an option: ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1: showUserInfo(connection); break;
                    case 2: getProfile(connection); break;
                    case 3: updateProfile(connection, scanner); break;
                    case 4: changePassword(connection, scanner); break;
                    case 5: showProductList(connection, scanner); break;
                    case 6:
                        if (isAdmin) new productMenu().show(connection, scanner, true);
                        else System.out.println("\n❌ Invalid choice!");
                        break;
                    case 7: new cartMenu(connection, scanner).show(); break;
                    case 8: handleLogout(connection); return;
                    case 0: System.out.println("\n👋 Thank you for using ChriOnline! Goodbye!"); break;
                    default: System.out.println("\n❌ Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("\n❌ Invalid input! Please enter a number.");
                scanner.nextLine();
                choice = -1;
            }

            if (choice != 0 && choice != 8) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        } while (choice != 0);
    }

    private void handleLogout(clientConnection connection) {
        System.out.println("\n🔄 Logging out...");
        response res = connection.logout();
        if (res.isSuccess()) {
            System.out.println("✅ " + res.getMessage());
            System.out.println("👋 See you next time!");
        } else {
            System.out.println("⚠️ Logout warning: " + res.getMessage());
        }
    }

    private void showUserInfo(clientConnection connection) {
        System.out.println("\n📊 Loading your user information...");
        response res = connection.getUserInfo();
        if (res.isSuccess()) {
            System.out.println("\n═══════════════════════════════════════");
            System.out.println("           👤 USER INFORMATION");
            System.out.println("═══════════════════════════════════════");
            System.out.println(res.getMessage().replace("\\n", "\n"));
            System.out.println("═══════════════════════════════════════");
        } else {
            System.out.println("\n❌ Unable to load your user information.");
            System.out.println("   Error: " + res.getMessage());
        }
    }

    private void getProfile(clientConnection connection) {
        System.out.println("\n📊 Loading your profile...");
        response res = connection.getProfile();
        if (res.isSuccess()) {
            System.out.println("\n═══════════════════════════════════════");
            System.out.println("           📊 YOUR PROFILE");
            System.out.println("═══════════════════════════════════════");
            System.out.println(res.getMessage().replace("\\n", "\n"));
            System.out.println("═══════════════════════════════════════");
        } else {
            System.out.println("\n❌ Unable to load your profile.");
            System.out.println("   Error: " + res.getMessage());
        }
    }

    private void updateProfile(clientConnection connection, Scanner scanner) {
        System.out.println("\n✏️  Update Your Profile");
        System.out.println("═══════════════════════");
        System.out.print("📍 Enter your new address: ");    String address = scanner.nextLine().trim();
        System.out.print("📞 Enter your new phone number: "); String phone = scanner.nextLine().trim();
        if (address.isEmpty() && phone.isEmpty()) {
            System.out.println("\n⚠️  No changes made - both fields are empty."); return;
        }
        System.out.println("\n💾 Updating your profile...");
        response res = connection.updateProfile(address, phone);
        if (res.isSuccess()) {
            System.out.println("✅ Profile updated successfully!");
        } else {
            System.out.println("❌ Failed to update profile.\n   Error: " + res.getMessage());
        }
    }

    private void changePassword(clientConnection connection, Scanner scanner) {
        System.out.println("\n🔒 Change Your Password");
        System.out.println("═══════════════════════");
        System.out.print("🔑 Current password: ");  String oldPass = scanner.nextLine();
        System.out.print("🆕 New password: ");       String newPass = scanner.nextLine();
        System.out.print("✅ Confirm new password: ");String confirm = scanner.nextLine();
        if (oldPass.isEmpty() || newPass.isEmpty()) {
            System.out.println("\n❌ Password cannot be empty!"); return;
        }
        if (newPass.length() < 6) {
            System.out.println("\n❌ New password must be at least 6 characters!"); return;
        }
        if (!newPass.equals(confirm)) {
            System.out.println("\n❌ Passwords don't match!"); return;
        }
        System.out.println("\n🔄 Changing your password...");
        response res = connection.changePassword(oldPass, newPass);
        if (res.isSuccess()) {
            System.out.println("✅ Password changed successfully!");
        } else {
            System.out.println("❌ Failed: " + res.getMessage());
        }
    }

    private void showGuestProducts(clientConnection connection, Scanner scanner) {
        System.out.println("\n🛍️  Product Catalog (Guest Mode)");
        System.out.println("═══════════════════════════════════");
        response res = connection.listProducts();
        if (res.isSuccess()) {
            System.out.println(res.getMessage().replace("\\n", "\n"));
            System.out.println("\n🔒 Login or register to access more features!");
        } else {
            System.out.println("❌ Unable to load products: " + res.getMessage());
        }
    }

    private void showProductList(clientConnection connection, Scanner scanner) {
        System.out.println("\n🛍️  Product Catalog");
        System.out.println("═══════════════════════════════════");
        response res = connection.listProducts();
        if (res.isSuccess()) {
            System.out.println(res.getMessage().replace("\\n", "\n"));
        } else {
            System.out.println("❌ Unable to load products: " + res.getMessage());
        }
    }
}