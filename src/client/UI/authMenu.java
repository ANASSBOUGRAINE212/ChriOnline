package client.UI;

import client.clientConnection;
import java.util.Scanner;
import protocol.response;

public class authMenu {
    
    public void show(clientConnection connection, Scanner scanner) {
        System.out.println("\n🌟 Welcome to ChriOnline! 🌟");
        System.out.println("═══════════════════════════════════════");
        
        // First show login/register menu
        if (!showAuthMenu(connection, scanner)) {
            return; // User chose to exit
        }
        
        // Then show main menu after successful login
        showMainMenu(connection, scanner);
    }
    
    private boolean showAuthMenu(clientConnection connection, Scanner scanner) {
        int choice = 0;
        do {
            System.out.println("\n🔐 Authentication Menu");
            System.out.println("─────────────────────");
            System.out.println("1. 🔑 Login");
            System.out.println("2. 📝 Register");
            System.out.println("0. 🚪 Exit");
            System.out.println("─────────────────────");
            System.out.print("Please select an option (0-2): ");
            
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                switch (choice) {
                    case 1:
                        if (handleLogin(connection, scanner)) {
                            return true; // Login successful
                        }
                        break;
                    case 2:
                        handleRegister(connection, scanner);
                        break;
                    case 0:
                        System.out.println("\n👋 Goodbye!");
                        return false;
                    default:
                        System.out.println("\n❌ Invalid choice! Please select 0-2.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("\n❌ Invalid input! Please enter a number.");
                scanner.nextLine(); // clear invalid input
                choice = -1; // continue loop
            }
        } while (choice != 0);
        
        return false;
    }
    
    private boolean handleLogin(clientConnection connection, Scanner scanner) {
        System.out.println("\n🔑 Login to Your Account");
        System.out.println("═══════════════════════");
        
        System.out.print("👤 Username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("🔒 Password: ");
        String password = scanner.nextLine().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("\n❌ Username and password cannot be empty!");
            return false;
        }
        
        System.out.println("\n🔄 Authenticating...");
        response res = connection.login(username, password);
        
        if (res.isSuccess()) {
            String[] parts = res.getMessage().split("\\|");
            if (parts.length > 1) {
                connection.setSessionToken(parts[1]); // Set the received token
            }
            System.out.println("✅ " + parts[0]);
            System.out.println("🎉 Welcome back, " + username + "!");
            return true;
        } else {
            System.out.println("❌ Login failed: " + res.getMessage());
            return false;
        }
    }
    
    private void handleRegister(clientConnection connection, Scanner scanner) {
        System.out.println("\n📝 Create New Account");
        System.out.println("═══════════════════════");
        
        System.out.print("👤 Choose a username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("📧 Email address: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("🔒 Choose a password (min 6 characters): ");
        String password = scanner.nextLine().trim();
        
        System.out.print("✅ Confirm password: ");
        String confirmPassword = scanner.nextLine().trim();
        
        System.out.print("📍 Address (optional): ");
        String address = scanner.nextLine().trim();
        
        System.out.print("📞 Phone number (optional): ");
        String phone = scanner.nextLine().trim();
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("\n❌ Username, email, and password are required!");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            System.out.println("\n❌ Passwords don't match!");
            return;
        }
        
        System.out.println("\n🔄 Creating account...");
        response res = connection.register(username, email, password, address, phone);
        
        if (res.isSuccess()) {
            System.out.println("✅ " + res.getMessage());
            System.out.println("🎉 Account created successfully!");
            System.out.println("👤 Your role: CLIENT (can be upgraded to ADMIN by database administrator)");
        } else {
            System.out.println("❌ Registration failed: " + res.getMessage());
        }
    }
    
    private void showMainMenu(clientConnection connection, Scanner scanner) {
        System.out.println("\n🌟 Welcome to ChriOnline User Portal! 🌟");
        System.out.println("═══════════════════════════════════════");
        
        int choice = 0;
        do {
            System.out.println("\n📋 Main Menu");
            System.out.println("─────────────");
            System.out.println("1. 👤 View my info");
            System.out.println("2. 📊 Get my profile");
            System.out.println("3. ✏️  Update my profile");
            System.out.println("4. 🔒 Change password");
            System.out.println("5. 🚪 Logout");
            System.out.println("0. 🚪 Exit");
            System.out.println("─────────────");
            System.out.print("Please select an option (0-5): ");
            
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                switch (choice) {
                    case 1:
                        showUserInfo(connection);
                        break;
                    case 2:
                        getProfile(connection);
                        break;
                    case 3:
                        updateProfile(connection, scanner);
                        break;
                    case 4:
                        changePassword(connection, scanner);
                        break;
                    case 5:
                        handleLogout(connection);
                        return; // Return to auth menu
                    case 0:
                        System.out.println("\n👋 Thank you for using ChriOnline! Goodbye!");
                        break;
                    default:
                        System.out.println("\n❌ Invalid choice! Please select a number between 0-5.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("\n❌ Invalid input! Please enter a number.");
                scanner.nextLine(); // clear invalid input
                choice = -1; // continue loop
            }
            
            if (choice != 0 && choice != 5) {
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
        
        System.out.print("📍 Enter your new address: ");
        String address = scanner.nextLine().trim();
        
        System.out.print("📞 Enter your new phone number: ");
        String phone = scanner.nextLine().trim();
        
        if (address.isEmpty() && phone.isEmpty()) {
            System.out.println("\n⚠️  No changes made - both fields are empty.");
            return;
        }
        
        System.out.println("\n💾 Updating your profile...");
        response res = connection.updateProfile(address, phone);
        
        if (res.isSuccess()) {
            System.out.println("✅ Profile updated successfully!");
            System.out.println(res.getMessage().replace("\\n", "\n"));
        } else {
            System.out.println("❌ Failed to update profile.");
            System.out.println("   Error: " + res.getMessage());
        }
    }

    private void changePassword(clientConnection connection, Scanner scanner) {
        System.out.println("\n🔒 Change Your Password");
        System.out.println("═══════════════════════");
        System.out.println("⚠️  For security, your password won't be visible while typing.");
        
        System.out.print("🔑 Enter your current password: ");
        String oldPass = scanner.nextLine();
        
        System.out.print("🆕 Enter your new password: ");
        String newPass = scanner.nextLine();
        
        System.out.print("✅ Confirm your new password: ");
        String confirm = scanner.nextLine();

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            System.out.println("\n❌ Password cannot be empty!");
            return;
        }
        
        if (newPass.length() < 6) {
            System.out.println("\n❌ New password must be at least 6 characters long!");
            return;
        }

        if (!newPass.equals(confirm)) {
            System.out.println("\n❌ Password confirmation doesn't match!");
            System.out.println("   Please make sure both passwords are identical.");
            return;
        }
        
        System.out.println("\n🔄 Changing your password...");
        response res = connection.changePassword(oldPass, newPass);
        
        if (res.isSuccess()) {
            System.out.println("✅ Password changed successfully!");
            System.out.println("   Your account is now more secure.");
        } else {
            System.out.println("❌ Failed to change password.");
            System.out.println("   " + res.getMessage());
            System.out.println("   Please check your current password and try again.");
        }
    }
}