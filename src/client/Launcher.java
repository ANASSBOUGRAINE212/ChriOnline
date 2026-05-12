package client;

/**
 * Client Launcher class for JavaFX application.
 * Sets the application mode to CLIENT before launching.
 * This is a plain Java class (not a JavaFX Application subclass) 
 * that allows JavaFX native libraries to load correctly when bundled in a fat JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        // Set client mode flag (regular user)
        System.setProperty("app.mode", "client");
        System.out.println("👤 Starting ChriOnline in CLIENT mode...");
        
        // Launch the client app
        clientApp.main(args);
    }
}
