package client.UI;

import client.clientConnection;
import org.kordamp.ikonli.javafx.FontIcon;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import protocol.response;

public class orderMenu {

    // ── palette (matches authMenu) ─────────────────────────────────────────
    private static final String BG          = "#080818";
    private static final String CARD_BG     = "#10102a";
    private static final String PANEL_BG    = "#0b0b20";
    private static final String ACCENT      = "#7c6ffd";
    private static final String ACCENT2     = "#c084fc";
    private static final String TEXT_PRI    = "#f0f0ff";
    private static final String TEXT_SEC    = "#8888aa";
    private static final String BORDER      = "#2a2a4a";
    private static final String SUCCESS_C   = "#34d399";
    private static final String ERROR_C     = "#fb7185";
    private static final String FIELD_BG    = "#0d0d22";
    private static final String GLOW        = "#7c6ffd66";

    private final clientConnection connection;
    private Stage ownerStage;

    public orderMenu(clientConnection connection, Stage ownerStage) {
        this.connection  = connection;
        this.ownerStage  = ownerStage;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  show  —  main order management dialog
    // ══════════════════════════════════════════════════════════════════════════
    public void show() {
        Stage dlg = dialogStage("Order Management");
        dlg.setResizable(true);

        // Header
        FontIcon headerIcon = faIcon("fas-cube", ACCENT2, 22);
        Label headerTitle   = glowLabel("Order Management", 20, FontWeight.BOLD);
        Label headerSub     = label("Manage and track your orders", 12, FontWeight.NORMAL, TEXT_SEC);
        HBox headerRow      = new HBox(12, headerIcon, vbox(3, headerTitle, headerSub));
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(0, 0, 20, 0));

        Rectangle accentLine = new Rectangle(60, 3);
        accentLine.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ACCENT)), new Stop(1, Color.web(ACCENT2))));
        accentLine.setArcWidth(3); accentLine.setArcHeight(3);

        // Action grid
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.getColumnConstraints().addAll(colConstraint(), colConstraint());

        String[][] items = {
            {"fas-shopping-cart", "Create Order",      "Create order from cart"},
            {"fas-list-alt",      "My Orders",         "View all your orders"},
            {"fas-search",        "Order Details",     "Look up a specific order"},
            {"fas-times-circle",  "Cancel Order",      "Cancel a pending order"},
            {"fas-credit-card",   "Process Payment",   "Pay for an order"},
            {"fas-receipt",       "Payment Receipt",   "Get a payment receipt"},
        };

        Runnable[] actions = {
            this::handleCreateOrder,
            this::handleListOrders,
            this::handleGetOrder,
            this::handleCancelOrder,
            this::handleProcessPayment,
            this::handleGetReceipt,
        };

        String[] dangerItems = {"fas-times-circle"};

        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            boolean isDanger = items[i][0].equals("fas-times-circle");
            Button card = actionCard(items[i][0], items[i][1], items[i][2], isDanger);
            card.setOnAction(e -> { animateButtonPress(card); actions[idx].run(); });
            grid.add(card, i % 2, i / 2);
        }

        VBox content = vbox(0, headerRow, accentLine, new VBox(16), grid);
        ((VBox) content.getChildren().get(2)).setPrefHeight(4); // spacer
        content = vbox(20, headerRow, accentLine, grid);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        FadeTransition ft = new FadeTransition(Duration.millis(400), content);
        ft.setFromValue(0); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(400), content);
        tt.setFromY(16); tt.setToY(0);
        new ParallelTransition(ft, tt).play();

        dlg.setScene(new Scene(animatedRoot(content), 540, 520));
        dlg.show();
    }

    // ── Action card ───────────────────────────────────────────────────────────
    private Button actionCard(String iconCode, String title, String sub, boolean danger) {
        String iconColor  = danger ? ERROR_C  : ACCENT2;
        String hoverBg    = danger ? "rgba(251,113,133,0.13)" : "rgba(124,111,253,0.13)";
        String defaultBg  = "rgba(255,255,255,0.04)";

        FontIcon icon    = faIcon(iconCode, iconColor, 22);
        StackPane iconBg = new StackPane(icon);
        iconBg.setMinSize(46, 46); iconBg.setMaxSize(46, 46);
        iconBg.setStyle("-fx-background-color: rgba(124,111,253,0.12); -fx-background-radius: 12;");

        Label titleLbl = label(title, 13, FontWeight.BOLD, TEXT_PRI);
        Label subLbl   = label(sub,   11, FontWeight.NORMAL, TEXT_SEC);
        VBox  text     = vbox(4, titleLbl, subLbl);

        VBox cardContent = new VBox(12, iconBg, text);
        cardContent.setPadding(new Insets(18, 16, 18, 16));

        Button b = new Button();
        b.setGraphic(cardContent);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(110);
        b.setStyle(
            "-fx-background-color: " + defaultBg + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> {
            b.setStyle(b.getStyle().replace(defaultBg, hoverBg));
            b.setEffect(new DropShadow(14, Color.web(GLOW)));
            icon.setIconColor(Color.web(iconColor));
        });
        b.setOnMouseExited(e -> {
            b.setStyle(b.getStyle().replace(hoverBg, defaultBg));
            b.setEffect(null);
        });
        GridPane.setHgrow(b, Priority.ALWAYS);
        return b;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleCreateOrder
    // ══════════════════════════════════════════════════════════════════════════
    private void handleCreateOrder() {
        Stage dlg = dialogStage("Create Order");
        Label title    = glowLabel("Create Order from Cart", 18, FontWeight.BOLD);
        Label subtitle = label("This will create a new order from your current cart contents.", 12, FontWeight.NORMAL, TEXT_SEC);
        Label resultLbl = label("", 13, FontWeight.NORMAL, TEXT_SEC);
        resultLbl.setWrapText(true);

        Button confirmBtn = gradientIconBtn("fas-shopping-cart", "Create Order");
        Button closeBtn   = glassIconBtn("fas-times", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, confirmBtn, closeBtn);
        HBox.setHgrow(confirmBtn, Priority.ALWAYS);
        HBox.setHgrow(closeBtn,   Priority.ALWAYS);

        VBox contentBox = vbox(20, title, subtitle, resultLbl, btnRow);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        confirmBtn.setOnAction(e -> {
            confirmBtn.setDisable(true);
            updateBtnText(confirmBtn, "fas-spinner", "Creating…");
            new Thread(() -> {
                response res = connection.createOrder();
                Platform.runLater(() -> {
                    confirmBtn.setDisable(false);
                    updateBtnText(confirmBtn, "fas-shopping-cart", "Create Order");
                    if (res.isSuccess()) {
                        String[] parts = res.getMessage().split("\\|");
                        String msg = parts[0];
                        if (parts.length > 1) msg += "\nOrder ID: " + parts[1];
                        dlg.close();
                        showSuccessDialog(msg + "\n\nProceed to payment to confirm your order!");
                    } else {
                        resultLbl.setText(res.getMessage());
                        resultLbl.setTextFill(Color.web(ERROR_C));
                        shakeNode(resultLbl);
                    }
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(contentBox), 460, 300));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleListOrders
    // ══════════════════════════════════════════════════════════════════════════
    private void handleListOrders() {
        Stage dlg = dialogStage("My Orders");
        Label title      = glowLabel("My Orders", 18, FontWeight.BOLD);
        Label loadingLbl = label("Loading your orders…", 13, FontWeight.NORMAL, TEXT_SEC);

        VBox ordersBox = vbox(10);
        ScrollPane sp  = new ScrollPane(ordersBox);
        sp.setFitToWidth(true);
        sp.setPrefHeight(340);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button closeBtn = gradientIconBtn("fas-check", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        VBox contentBox = vbox(20, title, loadingLbl, sp, closeBtn);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        new Thread(() -> {
            response res = connection.listOrders();
            Platform.runLater(() -> {
                contentBox.getChildren().remove(loadingLbl);
                if (!res.isSuccess()) {
                    ordersBox.getChildren().add(label("Unable to load orders: " + res.getMessage(), 13, FontWeight.NORMAL, ERROR_C));
                    return;
                }
                String raw = res.getMessage().replace("\\n", "\n");
                for (String line : raw.split("\n")) {
                    if (line.isBlank()) continue;
                    Label row = label(line, 12, FontWeight.NORMAL, TEXT_PRI);
                    row.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.03);" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 14;"
                    );
                    row.setMaxWidth(Double.MAX_VALUE);
                    FadeTransition ft = new FadeTransition(Duration.millis(200 + ordersBox.getChildren().size() * 40L), row);
                    ft.setFromValue(0); ft.setToValue(1);
                    ordersBox.getChildren().add(row);
                    ft.play();
                }
                if (ordersBox.getChildren().isEmpty())
                    ordersBox.getChildren().add(label("No orders found.", 13, FontWeight.NORMAL, TEXT_SEC));
            });
        }).start();

        dlg.setScene(new Scene(animatedRoot(contentBox), 520, 500));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleGetOrder
    // ══════════════════════════════════════════════════════════════════════════
    private void handleGetOrder() {
        Stage dlg = dialogStage("Order Details");
        Label title     = glowLabel("Order Details", 18, FontWeight.BOLD);
        TextField idFld = fancyTextField("Enter Order ID", "fas-search");
        Label errLbl    = errorLabel();

        VBox resultBox = vbox(10);
        ScrollPane sp  = new ScrollPane(resultBox);
        sp.setFitToWidth(true);
        sp.setPrefHeight(200);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.setVisible(false); sp.setManaged(false);

        Button lookupBtn = gradientIconBtn("fas-search", "Get Details");
        Button closeBtn  = glassIconBtn("fas-times", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, lookupBtn, closeBtn);
        HBox.setHgrow(lookupBtn, Priority.ALWAYS);
        HBox.setHgrow(closeBtn,  Priority.ALWAYS);

        VBox contentBox = vbox(20, title,
                fancyFieldGroup("Order ID", idFld),
                errLbl, btnRow, sp);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        lookupBtn.setOnAction(e -> {
            String orderId = idFld.getText().trim();
            if (orderId.isEmpty()) { shakeAndError(errLbl, "Order ID cannot be empty."); return; }
            lookupBtn.setDisable(true);
            updateBtnText(lookupBtn, "fas-spinner", "Looking up…");
            resultBox.getChildren().clear();
            new Thread(() -> {
                response res = connection.getOrder(orderId);
                Platform.runLater(() -> {
                    lookupBtn.setDisable(false);
                    updateBtnText(lookupBtn, "fas-search", "Get Details");
                    if (!res.isSuccess()) { shakeAndError(errLbl, res.getMessage()); return; }
                    errLbl.setVisible(false); errLbl.setManaged(false);
                    sp.setVisible(true); sp.setManaged(true);
                    String raw = res.getMessage().replace("\\n", "\n");
                    for (String line : raw.split("\n")) {
                        if (line.isBlank()) continue;
                        Label row = label(line, 12, FontWeight.NORMAL, TEXT_PRI);
                        row.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.03);" +
                            "-fx-border-color: " + BORDER + ";" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 14;"
                        );
                        row.setMaxWidth(Double.MAX_VALUE);
                        resultBox.getChildren().add(row);
                    }
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(contentBox), 500, 460));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleCancelOrder
    // ══════════════════════════════════════════════════════════════════════════
    private void handleCancelOrder() {
        Stage dlg = dialogStage("Cancel Order");
        Label title     = glowLabel("Cancel Order", 18, FontWeight.BOLD);
        Label warning   = label("⚠  This action cannot be undone. The order will be permanently cancelled.", 12, FontWeight.NORMAL, ERROR_C);
        warning.setWrapText(true);
        TextField idFld = fancyTextField("Enter Order ID", "fas-times-circle");
        Label errLbl    = errorLabel();

        Button cancelBtn = dangerIconBtn("fas-times-circle", "Cancel Order");
        Button closeBtn  = glassIconBtn("fas-arrow-left", "Go Back");
        closeBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, cancelBtn, closeBtn);
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);
        HBox.setHgrow(closeBtn,  Priority.ALWAYS);

        // Warning card
        HBox warningCard = new HBox(10, faIcon("fas-exclamation-triangle", ERROR_C, 14), warning);
        warningCard.setAlignment(Pos.CENTER_LEFT);
        warningCard.setPadding(new Insets(14, 16, 14, 16));
        warningCard.setStyle(
            "-fx-background-color: rgba(251,113,133,0.08);" +
            "-fx-border-color: rgba(251,113,133,0.3);" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );

        VBox contentBox = vbox(20, title, warningCard,
                fancyFieldGroup("Order ID", idFld), errLbl, btnRow);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        cancelBtn.setOnAction(e -> {
            String orderId = idFld.getText().trim();
            if (orderId.isEmpty()) { shakeAndError(errLbl, "Order ID cannot be empty."); return; }
            cancelBtn.setDisable(true);
            updateBtnText(cancelBtn, "fas-spinner", "Cancelling…");
            new Thread(() -> {
                response res = connection.cancelOrder(orderId);
                Platform.runLater(() -> {
                    cancelBtn.setDisable(false);
                    updateBtnText(cancelBtn, "fas-times-circle", "Cancel Order");
                    if (res.isSuccess()) { dlg.close(); showSuccessDialog(res.getMessage()); }
                    else shakeAndError(errLbl, res.getMessage());
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(contentBox), 500, 400));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleProcessPayment
    // ══════════════════════════════════════════════════════════════════════════
    private void handleProcessPayment() {
        Stage dlg = dialogStage("Process Payment");
        Label title  = glowLabel("Process Payment", 18, FontWeight.BOLD);
        Label subtitle = label("Select a payment method and confirm your order.", 12, FontWeight.NORMAL, TEXT_SEC);
        TextField idFld = fancyTextField("Enter Order ID", "fas-cube");
        Label errLbl    = errorLabel();

        // Payment method toggle buttons
        String[] methods      = {"CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER", "CASH"};
        String[] methodIcons  = {"fas-credit-card", "fas-credit-card", "fab-paypal", "fas-university", "fas-money-bill-wave"};
        String[] methodLabels = {"Credit Card", "Debit Card", "PayPal", "Bank Transfer", "Cash"};
        final String[] selectedMethod = {methods[0]};

        FlowPane methodPane = new FlowPane(8, 8);
        ToggleGroup tg = new ToggleGroup();

        for (int i = 0; i < methods.length; i++) {
            final String method = methods[i];
            ToggleButton tb = new ToggleButton();
            tb.setToggleGroup(tg);
            tb.setGraphic(iconText(methodIcons[i], methodLabels[i], TEXT_SEC, 12));
            tb.setPrefHeight(38);
            tb.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            );
            tb.selectedProperty().addListener((obs, was, now) -> {
                if (now) {
                    selectedMethod[0] = method;
                    tb.setStyle(
                        "-fx-background-color: rgba(124,111,253,0.20);" +
                        "-fx-border-color: " + ACCENT + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
                    );
                } else {
                    tb.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.04);" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
                    );
                }
            });
            if (i == 0) tb.setSelected(true);
            methodPane.getChildren().add(tb);
        }

        Button payBtn   = gradientIconBtn("fas-credit-card", "Pay Now");
        Button closeBtn = glassIconBtn("fas-times", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, payBtn, closeBtn);
        HBox.setHgrow(payBtn,   Priority.ALWAYS);
        HBox.setHgrow(closeBtn, Priority.ALWAYS);

        VBox contentBox = vbox(20, title, subtitle,
                fancyFieldGroup("Order ID", idFld),
                vbox(8, label("Payment Method", 11, FontWeight.BOLD, TEXT_SEC), methodPane),
                errLbl, btnRow);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        payBtn.setOnAction(e -> {
            String orderId = idFld.getText().trim();
            if (orderId.isEmpty()) { shakeAndError(errLbl, "Order ID cannot be empty."); return; }
            payBtn.setDisable(true);
            updateBtnText(payBtn, "fas-spinner", "Processing…");
            String method = selectedMethod[0];
            new Thread(() -> {
                response res = connection.processPayment(orderId, method);
                Platform.runLater(() -> {
                    payBtn.setDisable(false);
                    updateBtnText(payBtn, "fas-credit-card", "Pay Now");
                    if (res.isSuccess()) {
                        String[] parts = res.getMessage().split("\\|");
                        String msg = parts[0];
                        if (parts.length > 1) msg += "\nPayment ID: " + parts[1];
                        dlg.close();
                        showSuccessDialog(msg);
                    } else {
                        shakeAndError(errLbl, res.getMessage());
                    }
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(contentBox), 520, 480));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleGetReceipt
    // ══════════════════════════════════════════════════════════════════════════
    private void handleGetReceipt() {
        Stage dlg = dialogStage("Payment Receipt");
        Label title      = glowLabel("Payment Receipt", 18, FontWeight.BOLD);
        TextField idFld  = fancyTextField("Enter Payment ID", "fas-receipt");
        Label errLbl     = errorLabel();

        VBox receiptBox = vbox(10);
        ScrollPane sp   = new ScrollPane(receiptBox);
        sp.setFitToWidth(true);
        sp.setPrefHeight(220);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.setVisible(false); sp.setManaged(false);

        Button fetchBtn = gradientIconBtn("fas-receipt", "Get Receipt");
        Button closeBtn = glassIconBtn("fas-times", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, fetchBtn, closeBtn);
        HBox.setHgrow(fetchBtn, Priority.ALWAYS);
        HBox.setHgrow(closeBtn, Priority.ALWAYS);

        VBox contentBox = vbox(20, title,
                fancyFieldGroup("Payment ID", idFld),
                errLbl, btnRow, sp);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        fetchBtn.setOnAction(e -> {
            String paymentId = idFld.getText().trim();
            if (paymentId.isEmpty()) { shakeAndError(errLbl, "Payment ID cannot be empty."); return; }
            fetchBtn.setDisable(true);
            updateBtnText(fetchBtn, "fas-spinner", "Fetching…");
            receiptBox.getChildren().clear();
            new Thread(() -> {
                response res = connection.getReceipt(paymentId);
                Platform.runLater(() -> {
                    fetchBtn.setDisable(false);
                    updateBtnText(fetchBtn, "fas-receipt", "Get Receipt");
                    if (!res.isSuccess()) { shakeAndError(errLbl, res.getMessage()); return; }
                    errLbl.setVisible(false); errLbl.setManaged(false);
                    sp.setVisible(true); sp.setManaged(true);

                    // Receipt header decoration
                    HBox receiptHeader = new HBox(8,
                            faIcon("fas-check-circle", SUCCESS_C, 16),
                            label("Payment Confirmed", 13, FontWeight.BOLD, SUCCESS_C));
                    receiptHeader.setAlignment(Pos.CENTER_LEFT);
                    receiptHeader.setPadding(new Insets(10, 14, 10, 14));
                    receiptHeader.setStyle(
                        "-fx-background-color: rgba(52,211,153,0.08);" +
                        "-fx-border-color: rgba(52,211,153,0.25);" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
                    );
                    receiptBox.getChildren().add(receiptHeader);

                    String raw = res.getMessage().replace("\\n", "\n");
                    for (String line : raw.split("\n")) {
                        if (line.isBlank()) continue;
                        Label row = label(line, 12, FontWeight.NORMAL, TEXT_PRI);
                        row.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.03);" +
                            "-fx-border-color: " + BORDER + ";" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 14;" +
                            "-fx-font-family: monospace;"
                        );
                        row.setMaxWidth(Double.MAX_VALUE);
                        FadeTransition ft = new FadeTransition(Duration.millis(200 + receiptBox.getChildren().size() * 35L), row);
                        ft.setFromValue(0); ft.setToValue(1);
                        receiptBox.getChildren().add(row);
                        ft.play();
                    }
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(contentBox), 520, 520));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI HELPERS  (mirrors authMenu helpers)
    // ══════════════════════════════════════════════════════════════════════════

    private FontIcon faIcon(String iconCode, String color, double size) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize((int) size);
        icon.setIconColor(Color.web(color));
        return icon;
    }

    private HBox iconText(String iconCode, String text, String iconColor, double iconSize) {
        FontIcon iv = faIcon(iconCode, iconColor, iconSize);
        Label lbl = new Label("  " + text);
        lbl.setTextFill(Color.web(TEXT_PRI));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        HBox box = new HBox(6, iv, lbl);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Label glowLabel(String text, double size, FontWeight weight) {
        Label l = new Label(text);
        l.setFont(Font.font("System", weight, size));
        l.setTextFill(Color.web(ACCENT2));
        l.setEffect(new DropShadow(12, Color.web(GLOW)));
        l.setWrapText(true);
        return l;
    }

    private Label label(String text, double size, FontWeight w, String hex) {
        Label l = new Label(text);
        l.setFont(Font.font("System", w, size));
        l.setTextFill(Color.web(hex));
        l.setWrapText(true);
        return l;
    }

    private VBox vbox(double spacing, javafx.scene.Node... nodes) {
        VBox b = new VBox(spacing, nodes);
        b.setFillWidth(true);
        return b;
    }

    private VBox vbox(double spacing) {
        VBox b = new VBox(spacing);
        b.setFillWidth(true);
        return b;
    }

    private StackPane animatedRoot(javafx.scene.Node content) {
        StackPane sp = new StackPane();
        sp.setStyle("-fx-background-color: " + BG + ";");

        Circle orb1 = new Circle(160);
        orb1.setFill(Color.web(ACCENT + "14"));
        orb1.setEffect(new GaussianBlur(80));
        StackPane.setAlignment(orb1, Pos.TOP_LEFT);
        orb1.setTranslateX(-80); orb1.setTranslateY(-80);

        Circle orb2 = new Circle(130);
        orb2.setFill(Color.web(ACCENT2 + "10"));
        orb2.setEffect(new GaussianBlur(70));
        StackPane.setAlignment(orb2, Pos.BOTTOM_RIGHT);
        orb2.setTranslateX(80); orb2.setTranslateY(80);

        TranslateTransition drift1 = new TranslateTransition(Duration.seconds(9), orb1);
        drift1.setFromX(-80); drift1.setToX(-30);
        drift1.setFromY(-80); drift1.setToY(-110);
        drift1.setAutoReverse(true); drift1.setCycleCount(Animation.INDEFINITE); drift1.play();

        TranslateTransition drift2 = new TranslateTransition(Duration.seconds(11), orb2);
        drift2.setFromX(80); drift2.setToX(30);
        drift2.setFromY(80); drift2.setToY(120);
        drift2.setAutoReverse(true); drift2.setCycleCount(Animation.INDEFINITE); drift2.play();

        sp.getChildren().addAll(orb1, orb2, content);
        return sp;
    }

    private TextField fancyTextField(String prompt, String iconCode) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        styleFancyControl(f);
        addFocusGlow(f);
        return f;
    }

    private void styleFancyControl(Control f) {
        f.setStyle(
            "-fx-background-color: " + FIELD_BG + ";" +
            "-fx-text-fill: " + TEXT_PRI + ";" +
            "-fx-prompt-text-fill: " + TEXT_SEC + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12 16;" +
            "-fx-font-size: 13;"
        );
        f.setPrefHeight(44);
        f.setMaxWidth(Double.MAX_VALUE);
    }

    private void addFocusGlow(Control f) {
        f.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                f.setStyle(f.getStyle().replace(BORDER, ACCENT));
                f.setEffect(new DropShadow(8, Color.web(GLOW)));
            } else {
                f.setStyle(f.getStyle().replace(ACCENT, BORDER));
                f.setEffect(null);
            }
        });
    }

    private VBox fancyFieldGroup(String labelText, Control field) {
        Label lbl = label(labelText, 11, FontWeight.BOLD, TEXT_SEC);
        return vbox(5, lbl, field);
    }

    private Button gradientIconBtn(String iconCode, String text) {
        Button b = new Button();
        b.setGraphic(iconText(iconCode, text, "white", 14));
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(48);
        b.setStyle(
            "-fx-background-color: linear-gradient(to right, " + ACCENT + ", " + ACCENT2 + ");" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + GLOW + ", 12, 0.3, 0, 3);"
        );
        b.setOnMouseEntered(e -> b.setOpacity(0.88));
        b.setOnMouseExited (e -> b.setOpacity(1.0));
        return b;
    }

    private Button glassIconBtn(String iconCode, String text) {
        Button b = new Button();
        b.setGraphic(iconText(iconCode, text, TEXT_SEC, 13));
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(44);
        b.setStyle(
            "-fx-background-color: rgba(124,111,253,0.12);" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("rgba(124,111,253,0.12)", "rgba(124,111,253,0.22)")));
        b.setOnMouseExited (e -> b.setStyle(b.getStyle().replace("rgba(124,111,253,0.22)", "rgba(124,111,253,0.12)")));
        return b;
    }

    private Button dangerIconBtn(String iconCode, String text) {
        Button b = new Button();
        b.setGraphic(iconText(iconCode, text, ERROR_C, 13));
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(48);
        b.setStyle(
            "-fx-background-color: rgba(251,113,133,0.15);" +
            "-fx-border-color: rgba(251,113,133,0.35);" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("rgba(251,113,133,0.15)", "rgba(251,113,133,0.28)")));
        b.setOnMouseExited (e -> b.setStyle(b.getStyle().replace("rgba(251,113,133,0.28)", "rgba(251,113,133,0.15)")));
        return b;
    }

    private void updateBtnText(Button b, String iconCode, String text) {
        b.setGraphic(iconText(iconCode, text, "white", 14));
    }

    private Label errorLabel() {
        Label l = new Label("");
        l.setTextFill(Color.web(ERROR_C));
        l.setFont(Font.font("System", FontWeight.NORMAL, 12));
        l.setGraphic(faIcon("fas-exclamation-triangle", ERROR_C, 12));
        l.setGraphicTextGap(6);
        l.setWrapText(true);
        l.setVisible(false);
        l.setManaged(false);
        return l;
    }

    private void shakeAndError(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), lbl);
        shake.setFromX(0); shake.setToX(8);
        shake.setAutoReverse(true); shake.setCycleCount(6);
        shake.play();
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), node);
        shake.setFromX(0); shake.setToX(8);
        shake.setAutoReverse(true); shake.setCycleCount(6);
        shake.play();
    }

    private void animateButtonPress(Button b) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), b);
        st.setFromX(1.0); st.setToX(0.96);
        st.setFromY(1.0); st.setToY(0.96);
        st.setAutoReverse(true); st.setCycleCount(2);
        st.play();
    }

    private void showSuccessDialog(String msg) {
        Stage dlg = new Stage();
        dlg.setTitle("Success");
        if (ownerStage != null) { dlg.initOwner(ownerStage); dlg.initModality(Modality.WINDOW_MODAL); }
        dlg.setResizable(false);
        FontIcon checkIcon = faIcon("fas-check-circle", SUCCESS_C, 36);
        StackPane iconBox  = new StackPane(checkIcon);
        iconBox.setAlignment(Pos.CENTER);
        Label lbl = label(msg, 14, FontWeight.NORMAL, SUCCESS_C);
        lbl.setWrapText(true);
        Button ok = gradientIconBtn("fas-check", "OK");
        ok.setOnAction(e -> dlg.close());
        VBox content = vbox(16, iconBox, lbl, ok);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(36));
        content.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: linear-gradient(to bottom right, " + ACCENT + "88, " + ACCENT2 + "44);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 20;"
        );
        content.setEffect(new DropShadow(30, Color.web(GLOW)));
        FadeTransition ft = new FadeTransition(Duration.millis(300), content);
        ft.setFromValue(0); ft.setToValue(1);
        dlg.setScene(new Scene(animatedRoot(content), 420, 260));
        dlg.show();
        ft.play();
    }

    private Stage dialogStage(String title) {
        Stage dlg = new Stage();
        dlg.setTitle(title);
        if (ownerStage != null) { dlg.initOwner(ownerStage); dlg.initModality(Modality.WINDOW_MODAL); }
        dlg.setResizable(false);
        return dlg;
    }

    private ColumnConstraints colConstraint() {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        cc.setFillWidth(true);
        return cc;
    }
}