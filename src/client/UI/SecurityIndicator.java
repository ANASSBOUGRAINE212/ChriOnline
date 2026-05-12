package client.UI;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Frontend UI Client — Indicateur cadenas (🔒/🔓) + statut handshake.
 * Utilisable dans authMenu et adminMenu.
 */
public class SecurityIndicator extends HBox {

    public enum Status {
        UNSECURED,   // 🔓 rouge
        CONNECTING,  // ⏳ orange
        SECURED,     // 🔒 vert
        ERROR        // ❌ rouge vif
    }

    private final Label iconLabel;
    private final Label statusLabel;
    private final Label sessionLabel;
    private Status currentStatus = Status.UNSECURED;

    public SecurityIndicator() {
        super(6);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(4, 10, 4, 10));
        setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");

        iconLabel   = new Label("🔓");
        iconLabel.setFont(Font.font(16));

        statusLabel = new Label("Non sécurisé");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        sessionLabel = new Label("");
        sessionLabel.setFont(Font.font("Monospaced", 10));
        sessionLabel.setTextFill(Color.GRAY);

        getChildren().addAll(iconLabel, statusLabel, sessionLabel);
        applyStyle(Status.UNSECURED);
    }

    // ── Mise à jour statut (thread-safe JavaFX) ──────────────────

    public void setConnecting(String message) {
        Platform.runLater(() -> {
            currentStatus = Status.CONNECTING;
            iconLabel.setText("⏳");
            statusLabel.setText(message != null ? message : "Connexion...");
            statusLabel.setTextFill(Color.ORANGE);
            applyStyle(Status.CONNECTING);
        });
    }

    public void setSecured(String sessionId, String fingerprint) {
        Platform.runLater(() -> {
            currentStatus = Status.SECURED;
            iconLabel.setText("🔒");
            statusLabel.setText("Connexion sécurisée");
            statusLabel.setTextFill(Color.web("#2ecc71"));
            if (sessionId != null) {
                sessionLabel.setText("  Session: " + sessionId);
            }
            applyStyle(Status.SECURED);

            // Tooltip avec fingerprint
            if (fingerprint != null) {
                Tooltip tip = new Tooltip(
                    "🔑 Fingerprint clé publique RSA:\n" + fingerprint +
                    "\n\nAES-256-GCM actif — Session chiffrée"
                );
                Tooltip.install(this, tip);
            }
        });
    }

    public void setError(String reason) {
        Platform.runLater(() -> {
            currentStatus = Status.ERROR;
            iconLabel.setText("❌");
            statusLabel.setText("Erreur sécurité");
            statusLabel.setTextFill(Color.RED);
            sessionLabel.setText(reason != null ? "  " + reason : "");
            applyStyle(Status.ERROR);
        });
    }

    public void setUnsecured() {
        Platform.runLater(() -> {
            currentStatus = Status.UNSECURED;
            iconLabel.setText("🔓");
            statusLabel.setText("Non sécurisé");
            statusLabel.setTextFill(Color.GRAY);
            sessionLabel.setText("");
            applyStyle(Status.UNSECURED);
        });
    }

    private void applyStyle(Status s) {
        switch (s) {
            case SECURED:
                setStyle("-fx-background-color: #eafaf1; -fx-border-color: #2ecc71; " +
                         "-fx-border-width: 1.5; -fx-background-radius: 8; -fx-border-radius: 8;");
                break;
            case CONNECTING:
                setStyle("-fx-background-color: #fef9e7; -fx-border-color: #f39c12; " +
                         "-fx-border-width: 1.5; -fx-background-radius: 8; -fx-border-radius: 8;");
                break;
            case ERROR:
                setStyle("-fx-background-color: #fdedec; -fx-border-color: #e74c3c; " +
                         "-fx-border-width: 1.5; -fx-background-radius: 8; -fx-border-radius: 8;");
                break;
            default:
                setStyle("-fx-background-color: #f2f3f4; -fx-border-color: #aab7b8; " +
                         "-fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        }
    }

    public Status getCurrentStatus() { return currentStatus; }
}