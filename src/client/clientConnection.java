package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import protocol.response;

public class clientConnection {
    private Socket socket;
    private BufferedReader in;
    private DataOutputStream out;
    private String sessionToken;

    public clientConnection(String host, int port) {
        System.out.println("🔌 Connecting to " + host + ":" + port);
        try {
            this.socket = new Socket(host, port);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new DataOutputStream(socket.getOutputStream());
            this.sessionToken = "test-token-alice";
            System.out.println("✅ Connected successfully!");
        } catch (IOException e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
            throw new RuntimeException("Could not connect to server", e);
        }
    }
    
    private response sendRequest(String message) {
        try {
            System.out.println("📤 Sending: " + message);
            out.writeBytes(message + "\n");
            
            String serverResponse = in.readLine();
            System.out.println("📥 Received: " + serverResponse);
            
            if (serverResponse.startsWith("SUCCESS|")) {
                return new response(true, serverResponse.substring(8));
            } else if (serverResponse.startsWith("ERROR|")) {
                return new response(false, serverResponse.substring(6));
            } else {
                return new response(false, "Unknown response format");
            }
        } catch (IOException e) {
            return new response(false, "Connection error: " + e.getMessage());
        }
    }
    
    public response getUserInfo() {
        return sendRequest("GET_USER_INFO|" + sessionToken);
    }

    public response updateProfile(String address, String phone) {
        return sendRequest("UPDATE_PROFILE|" + sessionToken + "|" + address + "|" + phone);
    }

    public response changePassword(String oldPassword, String newPassword) {
        return sendRequest("CHANGE_PASSWORD|" + sessionToken + "|" + oldPassword + "|" + newPassword);
    }
}