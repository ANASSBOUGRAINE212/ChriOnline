package client;

/**
 * Admin Launcher class for JavaFX application.
 * Sets the application mode to ADMIN before launching.
 */
public class AdminLauncher {
    public static void main(String[] args) {
        // Set admin mode flag
        System.setProperty("app.mode", "admin");
        System.out.println("🔑 Starting ChriOnline in ADMIN mode...");
        
        // Launch the client app
        clientApp.main(args);
    }
}
