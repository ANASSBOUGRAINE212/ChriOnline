package server;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import protocol.request;
import protocol.response;
import server.handlers.authHandler;
import server.handlers.cartHandler;
import server.handlers.orderHandler;
import server.handlers.paymentHandler;
import server.handlers.productHandler;

public class clientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public clientHandler(Socket socket) {
        this.clientSocket = socket;
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
        try {
            while (true) {
                request clientRequest = (request) in.readObject();
                System.out.println("📨 Received request: " + clientRequest.getType());

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

                System.out.println("📤 Sending response: " + (serverResponse.isSuccess() ? "SUCCESS" : "ERROR"));
                out.writeObject(serverResponse);
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("🧹 Client session cleaned up: " + clientSocket.getRemoteSocketAddress());
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