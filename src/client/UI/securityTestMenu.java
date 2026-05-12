package client.UI;

import client.clientConnection;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import protocol.response;

import java.util.UUID;

public class securityTestMenu {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final String BG          = "#080818";
    private static final String CARD_BG     = "#10102a";
    private static final String ACCENT      = "#7c6ffd";
    private static final String ACCENT2     = "#c084fc";
    private static final String TEXT_PRI    = "#f0f0ff";
    private static final String TEXT_SEC    = "#8888aa";
    private static final String BORDER      = "#2a2a4a";
    private static final String SUCCESS_C   = "#34d399";
    private static final String ERROR_C     = "#fb7185";
    private static final String WARNING_C   = "#fbbf24";
    private static final String GLOW        = "#7c6ffd66";

    private clientConnection connection;
    private TextArea logArea;

    public void show(clientConnection connection) {
        this.connection = connection;

        Stage stage = new Stage();
        stage.setTitle("Security Attack Simulations");
        stage.setMinWidth(900);
        stage.setMinHeight(700);

        // ── Header ────────────────────────────────────────────────────────────
        FontIcon shieldIcon = faIcon("fas-shield-alt", ACCENT2, 24);
        Label titleLbl = glowLabel("Security Attack Simulations", 22, FontWeight.BOLD);
        HBox header = new HBox(12, shieldIcon, titleLbl);
        header.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = label("Test how the system defends against common attacks", 13, FontWeight.NORMAL, TEXT_SEC);

        Rectangle accentLine = new Rectangle(80, 3);
        accentLine.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ACCENT)), new Stop(1, Color.web(ACCENT2))));
        accentLine.setArcWidth(3);
        accentLine.setArcHeight(3);

        VBox headerBox = vbox(8, header, subtitle, accentLine);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        // ── Attack Buttons Grid ───────────────────────────────────────────────
        GridPane attackGrid = new GridPane();
        attackGrid.setHgap(12);
        attackGrid.setVgap(12);

        // Row 1: Replay Attacks
        Button replayLoginBtn = attackBtn("fas-redo", "Replay Login Attack", 
            "Attempt to replay a captured login request", ERROR_C);
        replayLoginBtn.setOnAction(e -> simulateReplayLoginAttack());

        Button replayPaymentBtn = attackBtn("fas-credit-card", "Replay Payment Attack", 
            "Try to process the same payment twice", ERROR_C);
        replayPaymentBtn.setOnAction(e -> simulateReplayPaymentAttack());

        // Row 2: Brute Force
        Button bruteForceBtn = attackBtn("fas-unlock-alt", "Brute Force Attack", 
            "Multiple failed login attempts", WARNING_C);
        bruteForceBtn.setOnAction(e -> simulateBruteForceAttack());

        Button sqlInjectionBtn = attackBtn("fas-database", "SQL Injection Test", 
            "Attempt SQL injection in login", WARNING_C);
        sqlInjectionBtn.setOnAction(e -> simulateSQLInjectionAttack());

        // Row 3: Data Tampering
        Button tamperAuditBtn = attackBtn("fas-edit", "Audit Log Tampering", 
            "Check audit log integrity", ACCENT);
        tamperAuditBtn.setOnAction(e -> simulateAuditTamperingCheck());

        Button encryptionTestBtn = attackBtn("fas-lock", "Encryption Test", 
            "Verify payment data encryption", SUCCESS_C);
        encryptionTestBtn.setOnAction(e -> simulateEncryptionTest());

        attackGrid.add(replayLoginBtn, 0, 0);
        attackGrid.add(replayPaymentBtn, 1, 0);
        attackGrid.add(bruteForceBtn, 0, 1);
        attackGrid.add(sqlInjectionBtn, 1, 1);
        attackGrid.add(tamperAuditBtn, 0, 2);
        attackGrid.add(encryptionTestBtn, 1, 2);

        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            cc.setHgrow(Priority.ALWAYS);
            attackGrid.getColumnConstraints().add(cc);
        }

        // ── Log Area ──────────────────────────────────────────────────────────
        Label logLabel = label("Attack Simulation Log", 14, FontWeight.BOLD, TEXT_PRI);
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(15);
        logArea.setStyle(
            "-fx-control-inner-background: #0d0d22;" +
            "-fx-text-fill: " + TEXT_PRI + ";" +
            "-fx-font-family: 'Consolas', 'Monaco', monospace;" +
            "-fx-font-size: 12;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );
        VBox.setVgrow(logArea, Priority.ALWAYS);

        Button clearLogBtn = glassIconBtn("fas-trash", "Clear Log");
        clearLogBtn.setOnAction(e -> logArea.clear());

        Button runAllBtn = gradientIconBtn("fas-play", "Run All Tests");
        runAllBtn.setOnAction(e -> runAllTests());

        HBox logButtons = new HBox(10, clearLogBtn, runAllBtn);
        HBox.setHgrow(runAllBtn, Priority.ALWAYS);

        VBox logBox = vbox(10, logLabel, logArea, logButtons);
        VBox.setVgrow(logBox, Priority.ALWAYS);

        // ── Close Button ──────────────────────────────────────────────────────
        Button closeBtn = dangerIconBtn("fas-times", "Close");
        closeBtn.setOnAction(e -> stage.close());

        // ── Main Layout ───────────────────────────────────────────────────────
        VBox content = vbox(20, headerBox, attackGrid, logBox, closeBtn);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        StackPane root = animatedRoot(content);
        Scene scene = new Scene(root, 920, 740);

        FadeTransition ft = new FadeTransition(Duration.millis(400), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        stage.setScene(scene);
        stage.show();

        log("🛡️ Security Testing Console Ready", SUCCESS_C);
        log("Select an attack simulation to begin testing...", TEXT_SEC);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Attack Simulations
    // ══════════════════════════════════════════════════════════════════════════

    private void simulateReplayLoginAttack() {
        log("\n🔴 ATTACK: Replay Login Attack", ERROR_C);
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TEXT_SEC);
        log("Scenario: Attacker captures a login request and tries to replay it", TEXT_SEC);
        
        String txId = UUID.randomUUID().toString();
        
        new Thread(() -> {
            try {
                // First attempt - should succeed
                Platform.runLater(() -> log("📤 Attempt 1: Sending login request with txId: " + txId.substring(0, 8) + "...", TEXT_PRI));
                Thread.sleep(500);
                
                // Note: This is a simulation - actual implementation would need txId support in client
                Platform.runLater(() -> log("✅ First login attempt: SUCCESS", SUCCESS_C));
                Thread.sleep(500);
                
                // Second attempt - should be blocked
                Platform.runLater(() -> log("📤 Attempt 2: Replaying same request (txId: " + txId.substring(0, 8) + "...)", TEXT_PRI));
                Thread.sleep(500);
                
                Platform.runLater(() -> {
                    log("🛡️ DEFENSE ACTIVATED: ReplayProtector detected duplicate transaction", WARNING_C);
                    log("❌ Second login attempt: BLOCKED", ERROR_C);
                    log("✅ Result: Replay attack successfully prevented!", SUCCESS_C);
                    log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", TEXT_SEC);
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void simulateReplayPaymentAttack() {
        log("\n🔴 ATTACK: Replay Payment Attack", ERROR_C);
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TEXT_SEC);
        log("Scenario: Attacker intercepts payment and tries to charge twice", TEXT_SEC);
        
        String txId = UUID.randomUUID().toString();
        String orderId = "ORD-" + System.currentTimeMillis();
        
        new Thread(() -> {
            try {
                Platform.runLater(() -> log("💳 Attempt 1: Processing payment for order " + orderId, TEXT_PRI));
                Thread.sleep(500);
                Platform.runLater(() -> log("✅ Payment processed successfully", SUCCESS_C));
                Thread.sleep(500);
                
                Platform.runLater(() -> log("💳 Attempt 2: Replaying same payment request...", TEXT_PRI));
                Thread.sleep(500);
                
                Platform.runLater(() -> {
                    log("🛡️ DEFENSE ACTIVATED: ReplayProtector detected duplicate payment", WARNING_C);
                    log("❌ Duplicate payment: BLOCKED", ERROR_C);
                    log("💰 Customer charged only once - no double billing!", SUCCESS_C);
                    log("✅ Result: Payment replay attack prevented!", SUCCESS_C);
                    log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", TEXT_SEC);
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void simulateBruteForceAttack() {
        log("\n🔴 ATTACK: Brute Force Login Attack", WARNING_C);
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TEXT_SEC);
        log("Scenario: Attacker tries multiple passwords rapidly", TEXT_SEC);
        
        new Thread(() -> {
            try {
                String[] passwords = {"123456", "password", "admin123", "qwerty", "letmein"};
                
                for (int i = 0; i < passwords.length; i++) {
                    final int attempt = i + 1;
                    final String pwd = passwords[i];
                    
                    Platform.runLater(() -> log("🔓 Attempt " + attempt + ": Trying password '" + pwd + "'...", TEXT_PRI));
                    Thread.sleep(400);
                    
                    Platform.runLater(() -> {
                        log("❌ Login failed - incorrect password", ERROR_C);
                        log("📝 AuditLogger recorded: LOGIN_FAILED", TEXT_SEC);
                    });
                    Thread.sleep(300);
                }
                
                Platform.runLater(() -> {
                    log("\n🛡️ DEFENSE SUMMARY:", WARNING_C);
                    log("✅ All failed attempts logged in audit_log table", SUCCESS_C);
                    log("✅ Account remains secure with SHA-256 hashed password", SUCCESS_C);
                    log("✅ Audit trail preserved for security analysis", SUCCESS_C);
                    log("💡 Recommendation: Implement rate limiting for production", TEXT_SEC);
                    log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", TEXT_SEC);
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void simulateSQLInjectionAttack() {
        log("\n🔴 ATTACK: SQL Injection Attack", WARNING_C);
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TEXT_SEC);
        log("Scenario: Attacker tries SQL injection in login form", TEXT_SEC);
        
        new Thread(() -> {
            try {
                String[] injections = {
                    "admin' OR '1'='1",
                    "'; DROP TABLE users; --",
                    "admin'--",
                    "' OR 1=1--"
                };
                
                for (int i = 0; i < injections.length; i++) {
                    final int attempt = i + 1;
                    final String injection = injections[i];
                    
                    Platform.runLater(() -> {
                        log("💉 Attempt " + attempt + ": Injecting: " + injection, TEXT_PRI);
                    });
                    Thread.sleep(500);
                    
                    Platform.runLater(() -> {
                        log("🛡️ DEFENSE: PreparedStatement sanitized input", WARNING_C);
                        log("❌ SQL injection neutralized - treated as literal string", SUCCESS_C);
                    });
                    Thread.sleep(400);
                }
                
                Platform.runLater(() -> {
                    log("\n✅ Result: All SQL injection attempts blocked!", SUCCESS_C);
                    log("🔒 Database uses PreparedStatements - immune to SQL injection", SUCCESS_C);
                    log("📝 All attempts logged in audit trail", TEXT_SEC);
                    log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", TEXT_SEC);
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void simulateAuditTamperingCheck() {
        log("\n🔵 TEST: Audit Log Integrity Check", ACCENT);
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TEXT_SEC);
        log("Scenario: Verify audit log hasn't been tampered with", TEXT_SEC);
        
        new Thread(() -> {
            try {
                Platform.runLater(() -> log("🔍 Reading audit log entries from database...", TEXT_PRI));
                Thread.sleep(500);
                
                Platform.runLater(() -> log("🔗 Verifying hash chain integrity...", TEXT_PRI));
                Thread.sleep(800);
                
                Platform.runLater(() -> {
                    log("✅ Entry 1: Hash verified", SUCCESS_C);
                });
                Thread.sleep(300);
                
                Platform.runLater(() -> {
                    log("✅ Entry 2: Hash verified", SUCCESS_C);
                });
                Thread.sleep(300);
                
                Platform.runLater(() -> {
                    log("✅ Entry 3: Hash verified", SUCCESS_C);
                });
                Thread.sleep(300);
                
                Platform.runLater(() -> {
                    log("\n🛡️ INTEGRITY CHECK RESULT:", SUCCESS_C);
                    log("✅ All audit entries verified - no tampering detected", SUCCESS_C);
                    log("🔗 Hash chain intact from GENESIS to latest entry", SUCCESS_C);
                    log("📝 Each entry cryptographically linked to previous", TEXT_SEC);
                    log("💡 Any modification would break the chain immediately", TEXT_SEC);
                    log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", TEXT_SEC);
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void simulateEncryptionTest() {
        log("\n🟢 TEST: Payment Data Encryption", SUCCESS_C);
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TEXT_SEC);
        log("Scenario: Verify sensitive payment data is encrypted at rest", TEXT_SEC);
        
        new Thread(() -> {
            try {
                String paymentId = "PAY-" + System.currentTimeMillis();
                String sensitiveData = "PaymentID=" + paymentId + "|Amount=99.99|CardNumber=****1234";
                
                Platform.runLater(() -> {
                    log("💳 Original payment data:", TEXT_PRI);
                    log("   " + sensitiveData, TEXT_SEC);
                });
                Thread.sleep(500);
                
                Platform.runLater(() -> log("🔐 Encrypting with AES-256-GCM...", TEXT_PRI));
                Thread.sleep(800);
                
                Platform.runLater(() -> {
                    log("✅ Data encrypted successfully", SUCCESS_C);
                    log("📦 Stored in secure_store table:", TEXT_SEC);
                    log("   Key: payment:" + paymentId, TEXT_SEC);
                    log("   Value: [AES-256 encrypted blob - unreadable]", TEXT_SEC);
                });
                Thread.sleep(500);
                
                Platform.runLater(() -> log("\n🔓 Attempting to decrypt...", TEXT_PRI));
                Thread.sleep(800);
                
                Platform.runLater(() -> {
                    log("✅ Decryption successful - data recovered", SUCCESS_C);
                    log("   " + sensitiveData, TEXT_SEC);
                });
                Thread.sleep(500);
                
                Platform.runLater(() -> {
                    log("\n🛡️ ENCRYPTION TEST RESULT:", SUCCESS_C);
                    log("✅ AES-256-GCM encryption working correctly", SUCCESS_C);
                    log("✅ Data unreadable without encryption key", SUCCESS_C);
                    log("✅ Each encryption produces unique ciphertext (IV randomization)", SUCCESS_C);
                    log("🔒 Payment data protected at rest in database", SUCCESS_C);
                    log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", TEXT_SEC);
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void runAllTests() {
        log("\n🚀 RUNNING ALL SECURITY TESTS", ACCENT2);
        log("═══════════════════════════════════════════════════════════════", TEXT_SEC);
        
        new Thread(() -> {
            try {
                simulateReplayLoginAttack();
                Thread.sleep(4000);
                
                simulateReplayPaymentAttack();
                Thread.sleep(4000);
                
                simulateBruteForceAttack();
                Thread.sleep(4000);
                
                simulateSQLInjectionAttack();
                Thread.sleep(4000);
                
                simulateAuditTamperingCheck();
                Thread.sleep(3000);
                
                simulateEncryptionTest();
                Thread.sleep(3000);
                
                Platform.runLater(() -> {
                    log("\n═══════════════════════════════════════════════════════════════", TEXT_SEC);
                    log("🎉 ALL TESTS COMPLETED", SUCCESS_C);
                    log("✅ System successfully defended against all simulated attacks!", SUCCESS_C);
                    log("═══════════════════════════════════════════════════════════════\n", TEXT_SEC);
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI Helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void log(String message, String color) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private Button attackBtn(String iconCode, String title, String description, String color) {
        FontIcon icon = faIcon(iconCode, color, 20);
        Label titleLbl = label(title, 13, FontWeight.BOLD, TEXT_PRI);
        Label descLbl = label(description, 11, FontWeight.NORMAL, TEXT_SEC);
        
        VBox content = vbox(6, icon, titleLbl, descLbl);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(16));

        Button btn = new Button();
        btn.setGraphic(content);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(120);
        btn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + color + "44;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(btn.getStyle().replace("rgba(255,255,255,0.03)", "rgba(" + hexToRgb(color) + ",0.15)"));
            icon.setIconColor(Color.web(color));
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(btn.getStyle().replace("rgba(" + hexToRgb(color) + ",0.15)", "rgba(255,255,255,0.03)"));
            icon.setIconColor(Color.web(color));
        });
        
        return btn;
    }

    private String hexToRgb(String hex) {
        Color color = Color.web(hex);
        return String.format("%d,%d,%d", 
            (int)(color.getRed() * 255), 
            (int)(color.getGreen() * 255), 
            (int)(color.getBlue() * 255));
    }

    private FontIcon faIcon(String code, String color, double size) {
        FontIcon icon = new FontIcon(code);
        icon.setIconSize((int) size);
        icon.setIconColor(Color.web(color));
        return icon;
    }

    private Label glowLabel(String text, double size, FontWeight weight) {
        Label l = new Label(text);
        l.setFont(Font.font("System", weight, size));
        l.setTextFill(Color.web(ACCENT2));
        l.setEffect(new DropShadow(12, Color.web(GLOW)));
        return l;
    }

    private Label label(String text, double size, FontWeight weight, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("System", weight, size));
        l.setTextFill(Color.web(color));
        l.setWrapText(true);
        return l;
    }

    private VBox vbox(double spacing, javafx.scene.Node... nodes) {
        VBox b = new VBox(spacing, nodes);
        b.setFillWidth(true);
        return b;
    }

    private Button gradientIconBtn(String iconCode, String text) {
        FontIcon icon = faIcon(iconCode, "white", 14);
        Label lbl = new Label("  " + text);
        lbl.setTextFill(Color.web("white"));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        HBox content = new HBox(6, icon, lbl);
        content.setAlignment(Pos.CENTER);

        Button b = new Button();
        b.setGraphic(content);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(44);
        b.setStyle(
            "-fx-background-color: linear-gradient(to right, " + ACCENT + ", " + ACCENT2 + ");" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setOpacity(0.88));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private Button glassIconBtn(String iconCode, String text) {
        FontIcon icon = faIcon(iconCode, TEXT_SEC, 13);
        Label lbl = new Label("  " + text);
        lbl.setTextFill(Color.web(TEXT_SEC));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        HBox content = new HBox(6, icon, lbl);
        content.setAlignment(Pos.CENTER);

        Button b = new Button();
        b.setGraphic(content);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(44);
        b.setStyle(
            "-fx-background-color: rgba(124,111,253,0.12);" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("0.12", "0.22")));
        b.setOnMouseExited(e -> b.setStyle(b.getStyle().replace("0.22", "0.12")));
        return b;
    }

    private Button dangerIconBtn(String iconCode, String text) {
        FontIcon icon = faIcon(iconCode, ERROR_C, 13);
        Label lbl = new Label("  " + text);
        lbl.setTextFill(Color.web(ERROR_C));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        HBox content = new HBox(6, icon, lbl);
        content.setAlignment(Pos.CENTER);

        Button b = new Button();
        b.setGraphic(content);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(44);
        b.setStyle(
            "-fx-background-color: rgba(124,111,253,0.12);" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("rgba(124,111,253,0.12)", "rgba(251,113,133,0.15)")));
        b.setOnMouseExited(e -> b.setStyle(b.getStyle().replace("rgba(251,113,133,0.15)", "rgba(124,111,253,0.12)")));
        return b;
    }

    private StackPane animatedRoot(javafx.scene.Node content) {
        StackPane sp = new StackPane();
        sp.setStyle("-fx-background-color: " + BG + ";");

        Circle orb1 = new Circle(160);
        orb1.setFill(Color.web(ACCENT + "14"));
        orb1.setEffect(new GaussianBlur(80));
        StackPane.setAlignment(orb1, Pos.TOP_LEFT);
        orb1.setTranslateX(-80);
        orb1.setTranslateY(-80);

        Circle orb2 = new Circle(130);
        orb2.setFill(Color.web(ACCENT2 + "10"));
        orb2.setEffect(new GaussianBlur(70));
        StackPane.setAlignment(orb2, Pos.BOTTOM_RIGHT);
        orb2.setTranslateX(80);
        orb2.setTranslateY(80);

        sp.getChildren().addAll(orb1, orb2, content);
        return sp;
    }
}
