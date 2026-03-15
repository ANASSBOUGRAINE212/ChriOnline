package client.UI;

import java.util.Scanner;

import client.clientConnection;
import protocol.response;

public class authMenu {
    
    public void show(clientConnection connection, Scanner scanner) {
        System.out.println("\n Welcome to ChriOnline");
        
        int choice = 0;
        do {
            System.out.println("\n📋 Main Menu");
            System.out.println(" ");
            System.out.println("1.  View my profile");
            System.out.println("2.   Update my profile");
            System.out.println("3.  Change password");
            System.out.println("0.  Exit");
            System.out.println("");
            System.out.print("Please select an option (0-3): ");
            
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                
                switch (choice) {
                    case 1:
                        showUserInfo(connection);
                        break;
                    case 2:
                        updateProfile(connection, scanner);
                        break;
                    case 3:
                        changePassword(connection, scanner);
                        break;
                    case 0:
                        System.out.println("\n Thank you for using ChriOnline! Goodbye!");
                        break;
                    default:
                        System.out.println("\n Invalid choice! Please select a number between 0-3.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("\n Invalid input! Please enter a number.");
                scanner.nextLine();
                choice = -1; 
            }
            
            if (choice != 0) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        } while (choice != 0);
    }

    private void showUserInfo(clientConnection connection) {
        System.out.println("\n Loading your profile...");
        response res = connection.getUserInfo();
        
        if (res.isSuccess()) {
            System.out.println("\n");
            System.out.println("           👤 YOUR PROFILE");
            System.out.println("");
            System.out.println(res.getMessage());
        } else {
            System.out.println("\n Oops! Unable to load your profile.");
            System.out.println("   Error: " + res.getMessage());
        }
    }

    private void updateProfile(clientConnection connection, Scanner scanner) {
        System.out.println("\n  Update Your Profile");
        
        System.out.print(" Enter your new address: ");
        String address = scanner.nextLine().trim();
        
        System.out.print(" Enter your new phone number: ");
        String phone = scanner.nextLine().trim();
        
        if (address.isEmpty() && phone.isEmpty()) {
            System.out.println("\n  No changes made - both fields are empty.");
            return;
        }
        
        System.out.println("\n Updating your profile...");
        response res = connection.updateProfile(address, phone);
        
        if (res.isSuccess()) {
            System.out.println(" Profile updated successfully!");
            System.out.println("   Your information has been saved.");
        } else {
            System.out.println(" Failed to update profile.");
            System.out.println("   Error: " + res.getMessage());
        }
    }

    private void changePassword(clientConnection connection, Scanner scanner) {
        System.out.println("\n Change Your Password");
        System.out.println("");
        
        System.out.print(" Enter your current password: ");
        String oldPass = scanner.nextLine();
        
        System.out.print(" Enter your new password: ");
        String newPass = scanner.nextLine();
        
        System.out.print(" Confirm your new password: ");
        String confirm = scanner.nextLine();

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            System.out.println("\n Password cannot be empty!");
            return;
        }
        
        if (newPass.length() < 6) {
            System.out.println("\n New password must be at least 6 characters long!");
            return;
        }

        if (!newPass.equals(confirm)) {
            System.out.println("\n Password confirmation doesn't match!");
            System.out.println("   Please make sure both passwords are identical.");
            return;
        }
        
        System.out.println("\n🔄 Changing your password...");
        response res = connection.changePassword(oldPass, newPass);
        
        if (res.isSuccess()) {
            System.out.println(" Password changed successfully!");
            System.out.println("   Your account is now more secure.");
        } else {
            System.out.println(" Failed to change password.");
            System.out.println("   " + res.getMessage());
            System.out.println("   Please check your current password and try again.");
        }
    }
}