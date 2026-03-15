package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class clientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private DataOutputStream out;
    
    public clientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("📨 Received: " + clientMessage);
                
                // Parse the message (format: "ACTION|token|param1|param2")
                String[] parts = clientMessage.split("\\|");
                String action = parts[0];
                
                String response = "";
                
                switch (action) {
                    case "GET_USER_INFO":
                        if (parts.length >= 2) {
                            response = handleGetUserInfo(parts[1]);
                        } else {
                            response = "ERROR|Missing token";
                        }
                        break;
                    case "UPDATE_PROFILE":
                        if (parts.length >= 4) {
                            response = handleUpdateProfile(parts[1], parts[2], parts[3]);
                        } else {
                            response = "ERROR|Missing parameters";
                        }
                        break;
                    case "CHANGE_PASSWORD":
                        if (parts.length >= 4) {
                            response = handleChangePassword(parts[1], parts[2], parts[3]);
                        } else {
                            response = "ERROR|Missing parameters";
                        }
                        break;
                    default:
                        response = "ERROR|Unknown command";
                        break;
                }
                
                System.out.println("📤 Sending: " + response);
                out.writeBytes(response + "\n");
            }
        } catch (IOException e) {
            System.out.println("🧹 Client disconnected: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String handleGetUserInfo(String token) {
        try {
            // Simple mock response for now
            if ("test-token-alice".equals(token)) {
                return "SUCCESS|Username: alice\nEmail: alice@test.com\nAddress: 123 Main St\nPhone: 555-1234\nRole: CLIENT";
            } else {
                return "ERROR|Invalid token";
            }
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }
    
    private String handleUpdateProfile(String token, String address, String phone) {
        try {
            if ("test-token-alice".equals(token)) {
                return "SUCCESS|Profile updated successfully";
            } else {
                return "ERROR|Invalid token";
            }
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }
    
    private String handleChangePassword(String token, String oldPass, String newPass) {
        try {
            if ("test-token-alice".equals(token)) {
                if ("hashedpass".equals(oldPass)) {
                    return "SUCCESS|Password changed successfully";
                } else {
                    return "ERROR|Old password incorrect";
                }
            } else {
                return "ERROR|Invalid token";
            }
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }
}