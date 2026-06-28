package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import protocol.request;
import protocol.response;
import security.RSA.AESSessionKey;
import security.aes.SecureInputStream;
import security.aes.SecureOutputStream;
import server.handlers.authHandler;
import server.handlers.cartHandler;
import server.handlers.orderHandler;
import server.handlers.paymentHandler;
import server.handlers.productHandler;

public class clientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecureInputStream secureIn;
    private SecureOutputStream secureOut;
    private final HandshakeHandler handshakeHandler;
    private AESSessionKey sessionKey;

    public clientHandler(Socket socket, HandshakeHandler handshakeHandler) {
        this.clientSocket     = socket;
        this.handshakeHandler = handshakeHandler;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in  = new ObjectInputStream(socket.getInputStream());
            System.out.println("👤 Client session started for: " + socket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.out.println("❌ Error setting up client connection: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // ── Handshake RSA AVANT tout échange ──────────────────────
        sessionKey = handshakeHandler.performHandshake(in, out);
        if (sessionKey == null) {
            System.out.println("❌ Handshake échoué — connexion fermée: " + clientSocket.getRemoteSocketAddress());
            cleanup();
            return;
        }
        System.out.println("🔒 Canal sécurisé établi pour: " + clientSocket.getRemoteSocketAddress());

        // ── Wrap streams with AES-256-GCM encryption ──────────────
        try {
            this.secureOut = new SecureOutputStream(clientSocket.getOutputStream(), sessionKey.getSecretKey());
            this.secureIn = new SecureInputStream(clientSocket.getInputStream(), sessionKey.getSecretKey());
            System.out.println("🔐 AES-256-GCM encryption activated");
        } catch (IOException e) {
            System.out.println("❌ Error setting up encrypted streams: " + e.getMessage());
            cleanup();
            return;
        }

        // ── Boucle de traitement des requêtes chiffrées ───────────
        try {
            while (true) {
                // Receive and decrypt request
                byte[] decryptedRequest = secureIn.readSecure();
                
                // Deserialize request object
                ByteArrayInputStream bais = new ByteArrayInputStream(decryptedRequest);
                ObjectInputStream ois = new ObjectInputStream(bais);
                request clientRequest = (request) ois.readObject();
                
                System.out.println("📨 Received encrypted request: " + clientRequest.getType());

                String requestType = clientRequest.getType();
                response serverResponse;

                if (requestType.equals(request.ADD_PRODUCT)    ||
                    requestType.equals(request.DELETE_PRODUCT)  ||
                    requestType.equals(request.GET_PRODUCT)     ||
                    requestType.equals(request.UPDATE_PRODUCT)  ||
                    requestType.equals(request.LIST_PRODUCTS)) {
                    serverResponse = productHandler.handle(clientRequest);
                } else if (requestType.equals(request.ADD_TO_CART)      ||
                           requestType.equals(request.GET_CART_ITEMS)   ||
                           requestType.equals(request.REMOVE_FROM_CART) ||
                           requestType.equals(request.GET_CART_TOTAL)) {
                    serverResponse = cartHandler.handle(clientRequest);
                } else if (requestType.equals(request.CREATE_ORDER)        ||
                           requestType.equals(request.GET_ORDER)           ||
                           requestType.equals(request.LIST_ORDERS)         ||
                           requestType.equals(request.CANCEL_ORDER)        ||
                           requestType.equals(request.UPDATE_ORDER_STATUS)) {
                    serverResponse = orderHandler.handle(clientRequest);
                } else if (requestType.equals(request.PROCESS_PAYMENT) ||
                           requestType.equals(request.GET_PAYMENT)     ||
                           requestType.equals(request.REFUND_PAYMENT)  ||
                           requestType.equals(request.GET_RECEIPT)) {
                    serverResponse = paymentHandler.handle(clientRequest);
                } else {
                    serverResponse = authHandler.handle(clientRequest);
                }

                System.out.println("📤 Sending encrypted response: " + (serverResponse.isSuccess() ? "SUCCESS" : "ERROR"));

                // Serialize response object
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(serverResponse);
                oos.flush();
                byte[] serialized = baos.toByteArray();
                
                // Encrypt and send response
                secureOut.writeSecure(serialized);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Client disconnected: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        try {
            if (in  != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.out.println("⚠️ Error during cleanup: " + e.getMessage());
        }
    }
}