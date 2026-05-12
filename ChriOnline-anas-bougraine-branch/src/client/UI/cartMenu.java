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

public class cartMenu {

    // ── palette (matches authMenu) ─────────────────────────────────────────
    private static final String BG        = "#080818";
    private static final String CARD_BG   = "#10102a";
    private static final String ACCENT    = "#7c6ffd";
    private static final String ACCENT2   = "#c084fc";
    private static final String TEXT_PRI  = "#f0f0ff";
    private static final String TEXT_SEC  = "#8888aa";
    private static final String BORDER    = "#2a2a4a";
    private static final String SUCCESS_C = "#34d399";
    private static final String ERROR_C   = "#fb7185";
    private static final String FIELD_BG  = "#0d0d22";
    private static final String GLOW      = "#7c6ffd66";

    private final clientConnection connection;
    private Stage ownerStage;

    // ── Constructor ───────────────────────────────────────────────────────────
    public cartMenu(clientConnection connection, Stage ownerStage) {
        this.connection  = connection;
        this.ownerStage  = ownerStage;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  show  —  main cart dialog
    // ══════════════════════════════════════════════════════════════════════════
    public void show() {
        Stage dlg = dialogStage("Shopping Cart");
        dlg.setResizable(true);

        // Header
        FontIcon headerIcon = faIcon("fas-shopping-cart", ACCENT2, 22);
        Label headerTitle   = glowLabel("Shopping Cart", 20, FontWeight.BOLD);
        Label headerSub     = label("Manage your cart items", 12, FontWeight.NORMAL, TEXT_SEC);
        HBox headerRow      = new HBox(12, headerIcon, vbox(3, headerTitle, headerSub));
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Rectangle accentLine = new Rectangle(60, 3);
        accentLine.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ACCENT)), new Stop(1, Color.web(ACCENT2))));
        accentLine.setArcWidth(3); accentLine.setArcHeight(3);

        // Action grid (2 columns)
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.getColumnConstraints().addAll(colConstraint(), colConstraint());

        String[][] items = {
            {"fas-list-alt",        "View Cart",       "See all cart items"},
            {"fas-plus-circle",     "Add to Cart",     "Add a product"},
            {"fas-minus-circle",    "Remove Item",     "Remove a product"},
            {"fas-dollar-sign",     "Cart Total",      "View total price"},
            {"fas-cubes",           "Item Count",      "Number of items"},
            {"fas-search",          "Item Details",    "Look up a cart item"},
            {"fas-trash-alt",       "Clear Cart",      "Remove everything"},
        };

        Runnable[] actions = {
            this::handleViewCart,
            this::handleAddToCart,
            this::handleRemoveFromCart,
            this::handleCartTotal,
            this::handleItemCount,
            this::handleGetItemDetails,
            this::handleClearCart,
        };

        boolean[] isDanger = {false, false, false, false, false, false, true};

        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            Button card = actionCard(items[i][0], items[i][1], items[i][2], isDanger[i]);
            card.setOnAction(e -> { animateButtonPress(card); actions[idx].run(); });
            grid.add(card, i % 2, i / 2);
        }

        VBox content = vbox(20, headerRow, accentLine, grid);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        FadeTransition ft = new FadeTransition(Duration.millis(400), content);
        ft.setFromValue(0); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(400), content);
        tt.setFromY(16); tt.setToY(0);
        new ParallelTransition(ft, tt).play();

        dlg.setScene(new Scene(animatedRoot(content), 540, 560));
        dlg.show();
    }

    // ── Action card ───────────────────────────────────────────────────────────
    private Button actionCard(String iconCode, String title, String sub, boolean danger) {
        String iconColor = danger ? ERROR_C : ACCENT2;
        String hoverBg   = danger ? "rgba(251,113,133,0.13)" : "rgba(124,111,253,0.13)";
        String defaultBg = "rgba(255,255,255,0.04)";

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
        });
        b.setOnMouseExited(e -> {
            b.setStyle(b.getStyle().replace(hoverBg, defaultBg));
            b.setEffect(null);
        });
        GridPane.setHgrow(b, Priority.ALWAYS);
        return b;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleViewCart
    // ══════════════════════════════════════════════════════════════════════════
    private void handleViewCart() {
        Stage dlg = dialogStage("My Cart");
        Label title      = glowLabel("My Cart", 18, FontWeight.BOLD);
        Label loadingLbl = label("Fetching cart items…", 13, FontWeight.NORMAL, TEXT_SEC);

        VBox itemsBox = vbox(10);
        ScrollPane sp = new ScrollPane(itemsBox);
        sp.setFitToWidth(true);
        sp.setPrefHeight(340);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button closeBtn = gradientIconBtn("fas-check", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        VBox contentBox = vbox(20, title, loadingLbl, sp, closeBtn);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        new Thread(() -> {
            response res = connection.getCart();
            Platform.runLater(() -> {
                contentBox.getChildren().remove(loadingLbl);
                if (!res.isSuccess()) {
                    itemsBox.getChildren().add(label("Unable to load cart: " + res.getMessage(), 13, FontWeight.NORMAL, ERROR_C));
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
                    FadeTransition ft = new FadeTransition(Duration.millis(200 + itemsBox.getChildren().size() * 40L), row);
                    ft.setFromValue(0); ft.setToValue(1);
                    itemsBox.getChildren().add(row);
                    ft.play();
                }
                if (itemsBox.getChildren().isEmpty())
                    itemsBox.getChildren().add(label("Your cart is empty.", 13, FontWeight.NORMAL, TEXT_SEC));
            });
        }).start();

        dlg.setScene(new Scene(animatedRoot(contentBox), 520, 500));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleAddToCart
    // ══════════════════════════════════════════════════════════════════════════
    private void handleAddToCart() {
        Stage dlg = dialogStage("Add to Cart");
        Label title    = glowLabel("Add Product to Cart", 18, FontWeight.BOLD);
        Label subtitle = label("Enter the product ID and quantity you want to add.", 12, FontWeight.NORMAL, TEXT_SEC);

        TextField productIdFld = fancyTextField("Product ID", "fas-tag");
        TextField quantityFld  = fancyTextField("Quantity",   "fas-cubes");
        Label errLbl           = errorLabel();

        Button addBtn   = gradientIconBtn("fas-plus-circle", "Add to Cart");
        Button closeBtn = glassIconBtn("fas-times", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, addBtn, closeBtn);
        HBox.setHgrow(addBtn,   Priority.ALWAYS);
        HBox.setHgrow(closeBtn, Priority.ALWAYS);

        VBox contentBox = vbox(20, title, subtitle,
                fancyFieldGroup("Product ID", productIdFld),
                fancyFieldGroup("Quantity",   quantityFld),
                errLbl, btnRow);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        addBtn.setOnAction(e -> {
            String productIdStr = productIdFld.getText().trim();
            String quantityStr  = quantityFld.getText().trim();

            if (productIdStr.isEmpty()) { shakeAndError(errLbl, "Product ID cannot be empty."); return; }

            int productId;
            try { productId = Integer.parseInt(productIdStr); }
            catch (NumberFormatException ex) { shakeAndError(errLbl, "Invalid product ID!"); return; }

            int quantity;
            try { quantity = Integer.parseInt(quantityStr); }
            catch (NumberFormatException ex) { shakeAndError(errLbl, "Invalid quantity!"); return; }

            addBtn.setDisable(true);
            updateBtnText(addBtn, "fas-spinner", "Looking up product…");

            final int finalProductId = productId;
            final int finalQuantity  = quantity;

            new Thread(() -> {
                // Get product price first
                response productRes = connection.getProduct(finalProductId);
                Platform.runLater(() -> {
                    if (!productRes.isSuccess()) {
                        addBtn.setDisable(false);
                        updateBtnText(addBtn, "fas-plus-circle", "Add to Cart");
                        shakeAndError(errLbl, "Product not found!");
                        return;
                    }

                    // Extract price from product info
                    double price = 0.0;
                    String[] lines = productRes.getMessage().split("\n");
                    for (String line : lines) {
                        if (line.contains("Price:")) {
                            String priceStr = line.replaceAll("[^0-9.]", "");
                            try { price = Double.parseDouble(priceStr); } catch (NumberFormatException ex) { price = 0.0; }
                            break;
                        }
                    }

                    final double finalPrice = price;
                    updateBtnText(addBtn, "fas-spinner", "Adding…");

                    new Thread(() -> {
                        response res = connection.addToCart(finalProductId, finalQuantity, finalPrice);
                        Platform.runLater(() -> {
                            addBtn.setDisable(false);
                            updateBtnText(addBtn, "fas-plus-circle", "Add to Cart");
                            if (res.isSuccess()) { dlg.close(); showSuccessDialog(res.getMessage()); }
                            else shakeAndError(errLbl, res.getMessage());
                        });
                    }).start();
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(contentBox), 480, 420));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleRemoveFromCart
    // ══════════════════════════════════════════════════════════════════════════
    private void handleRemoveFromCart() {
        Stage dlg = dialogStage("Remove from Cart");
        Label title    = glowLabel("Remove Item from Cart", 18, FontWeight.BOLD);
        Label subtitle = label("Enter the product ID to remove it from your cart.", 12, FontWeight.NORMAL, TEXT_SEC);

        TextField productIdFld = fancyTextField("Product ID", "fas-minus-circle");
        Label errLbl           = errorLabel();

        Button removeBtn = dangerIconBtn("fas-minus-circle", "Remove Item");
        Button closeBtn  = glassIconBtn("fas-times", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, removeBtn, closeBtn);
        HBox.setHgrow(removeBtn, Priority.ALWAYS);
        HBox.setHgrow(closeBtn,  Priority.ALWAYS);

        VBox contentBox = vbox(20, title, subtitle,
                fancyFieldGroup("Product ID", productIdFld),
                errLbl, btnRow);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        removeBtn.setOnAction(e -> {
            String productIdStr = productIdFld.getText().trim();
            if (productIdStr.isEmpty()) { shakeAndError(errLbl, "Product ID cannot be empty."); return; }

            int productId;
            try { productId = Integer.parseInt(productIdStr); }
            catch (NumberFormatException ex) { shakeAndError(errLbl, "Invalid product ID!"); return; }

            removeBtn.setDisable(true);
            updateBtnText(removeBtn, "fas-spinner", "Removing…");

            final int finalProductId = productId;
            new Thread(() -> {
                response res = connection.removeFromCart(finalProductId);
                Platform.runLater(() -> {
                    removeBtn.setDisable(false);
                    updateBtnText(removeBtn, "fas-minus-circle", "Remove Item");
                    if (res.isSuccess()) { dlg.close(); showSuccessDialog(res.getMessage()); }
                    else shakeAndError(errLbl, res.getMessage());
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(contentBox), 480, 360));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleCartTotal
    // ══════════════════════════════════════════════════════════════════════════
    private void handleCartTotal() {
        Stage dlg = dialogStage("Cart Total");
        Label title      = glowLabel("Cart Total", 18, FontWeight.BOLD);
        Label loadingLbl = label("Calculating total…", 13, FontWeight.NORMAL, TEXT_SEC);

        VBox totalBox = vbox(12);

        Button closeBtn = gradientIconBtn("fas-check", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        VBox contentBox = vbox(20, title, loadingLbl, totalBox, closeBtn);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        new Thread(() -> {
            response res = connection.getCartTotal();
            Platform.runLater(() -> {
                contentBox.getChildren().remove(loadingLbl);
                if (!res.isSuccess()) {
                    totalBox.getChildren().add(label("Unable to get total: " + res.getMessage(), 13, FontWeight.NORMAL, ERROR_C));
                    return;
                }

                // Total highlight card
                String raw = res.getMessage().replace("\\n", "\n");
                HBox totalCard = new HBox(12,
                        faIcon("fas-dollar-sign", SUCCESS_C, 22),
                        vbox(3,
                                label("Cart Total", 11, FontWeight.BOLD, TEXT_SEC),
                                label(raw.trim(), 22, FontWeight.BOLD, SUCCESS_C)));
                totalCard.setAlignment(Pos.CENTER_LEFT);
                totalCard.setPadding(new Insets(20, 24, 20, 24));
                totalCard.setStyle(
                    "-fx-background-color: rgba(52,211,153,0.08);" +
                    "-fx-border-color: rgba(52,211,153,0.25);" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;"
                );

                FadeTransition ft = new FadeTransition(Duration.millis(350), totalCard);
                ft.setFromValue(0); ft.setToValue(1);
                totalBox.getChildren().add(totalCard);
                ft.play();
            });
        }).start();

        dlg.setScene(new Scene(animatedRoot(contentBox), 440, 300));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleItemCount
    // ══════════════════════════════════════════════════════════════════════════
    private void handleItemCount() {
        Stage dlg = dialogStage("Item Count");
        Label title      = glowLabel("Cart Item Count", 18, FontWeight.BOLD);
        Label loadingLbl = label("Counting items…", 13, FontWeight.NORMAL, TEXT_SEC);

        VBox countBox = vbox(12);

        Button closeBtn = gradientIconBtn("fas-check", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        VBox contentBox = vbox(20, title, loadingLbl, countBox, closeBtn);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        new Thread(() -> {
            response res = connection.getCartItemCount();
            Platform.runLater(() -> {
                contentBox.getChildren().remove(loadingLbl);

                String msg = res.isSuccess() ? res.getMessage() : "Error: " + res.getMessage();
                String color = res.isSuccess() ? ACCENT2 : ERROR_C;

                HBox countCard = new HBox(12,
                        faIcon("fas-cubes", color, 22),
                        vbox(3,
                                label("Items in Cart", 11, FontWeight.BOLD, TEXT_SEC),
                                label(msg.trim(), 22, FontWeight.BOLD, color)));
                countCard.setAlignment(Pos.CENTER_LEFT);
                countCard.setPadding(new Insets(20, 24, 20, 24));
                countCard.setStyle(
                    "-fx-background-color: rgba(124,111,253,0.08);" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;"
                );

                FadeTransition ft = new FadeTransition(Duration.millis(350), countCard);
                ft.setFromValue(0); ft.setToValue(1);
                countBox.getChildren().add(countCard);
                ft.play();
            });
        }).start();

        dlg.setScene(new Scene(animatedRoot(contentBox), 440, 300));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleGetItemDetails
    // ══════════════════════════════════════════════════════════════════════════
    private void handleGetItemDetails() {
        Stage dlg = dialogStage("Item Details");
        Label title       = glowLabel("Item Details", 18, FontWeight.BOLD);
        TextField idFld   = fancyTextField("Product ID", "fas-search");
        Label errLbl      = errorLabel();

        VBox detailBox = vbox(10);
        ScrollPane sp  = new ScrollPane(detailBox);
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
                fancyFieldGroup("Product ID", idFld),
                errLbl, btnRow, sp);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        lookupBtn.setOnAction(e -> {
            String productId = idFld.getText().trim();
            if (productId.isEmpty()) { shakeAndError(errLbl, "Product ID cannot be empty."); return; }
            lookupBtn.setDisable(true);
            updateBtnText(lookupBtn, "fas-spinner", "Looking up…");
            detailBox.getChildren().clear();
            new Thread(() -> {
                response res = connection.getItemDetails(productId);
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
                        FadeTransition ft = new FadeTransition(Duration.millis(200 + detailBox.getChildren().size() * 40L), row);
                        ft.setFromValue(0); ft.setToValue(1);
                        detailBox.getChildren().add(row);
                        ft.play();
                    }
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(contentBox), 500, 460));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleClearCart
    // ══════════════════════════════════════════════════════════════════════════
    private void handleClearCart() {
        Stage dlg = dialogStage("Clear Cart");
        Label title   = glowLabel("Clear Cart", 18, FontWeight.BOLD);
        Label warning = label("This will permanently remove all items from your cart. This action cannot be undone.", 12, FontWeight.NORMAL, ERROR_C);
        warning.setWrapText(true);

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

        Label errLbl = errorLabel();

        Button clearBtn = dangerIconBtn("fas-trash-alt", "Clear Cart");
        Button closeBtn = glassIconBtn("fas-arrow-left", "Go Back");
        closeBtn.setOnAction(e -> dlg.close());

        HBox btnRow = new HBox(10, clearBtn, closeBtn);
        HBox.setHgrow(clearBtn, Priority.ALWAYS);
        HBox.setHgrow(closeBtn, Priority.ALWAYS);

        VBox contentBox = vbox(20, title, warningCard, errLbl, btnRow);
        contentBox.setPadding(new Insets(32));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        clearBtn.setOnAction(e -> {
            clearBtn.setDisable(true);
            updateBtnText(clearBtn, "fas-spinner", "Clearing…");
            new Thread(() -> {
                response res = connection.clearCart();
                Platform.runLater(() -> {
                    clearBtn.setDisable(false);
                    updateBtnText(clearBtn, "fas-trash-alt", "Clear Cart");
                    if (res.isSuccess()) { dlg.close(); showSuccessDialog(res.getMessage()); }
                    else shakeAndError(errLbl, res.getMessage());
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(contentBox), 480, 340));
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