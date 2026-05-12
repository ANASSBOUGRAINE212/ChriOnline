package client.UI;

import client.clientConnection;
import java.util.Scanner;
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
import org.kordamp.ikonli.javafx.FontIcon;
import protocol.response;

public class productMenu {

    // ── palette (matches authMenu) ─────────────────────────────────────────────
    private static final String BG       = "#080818";
    private static final String CARD_BG  = "#10102a";
    private static final String PANEL_BG = "#0b0b20";
    private static final String ACCENT   = "#7c6ffd";
    private static final String ACCENT2  = "#c084fc";
    private static final String TEXT_PRI = "#f0f0ff";
    private static final String TEXT_SEC = "#8888aa";
    private static final String BORDER   = "#2a2a4a";
    private static final String SUCCESS_C= "#34d399";
    private static final String ERROR_C  = "#fb7185";
    private static final String FIELD_BG = "#0d0d22";
    private static final String GLOW     = "#7c6ffd66";
    private static final String DIVIDER  = "#1e1e3a";

    private Stage            ownerStage;
    private clientConnection connection;

    // ══════════════════════════════════════════════════════════════════════════
    //  Public entry point  (scanner param kept for compatibility — unused)
    // ══════════════════════════════════════════════════════════════════════════
    public void show(clientConnection connection, Scanner scanner, boolean isAdmin) {
        this.connection = connection;

        Stage stage = new Stage();
        this.ownerStage = stage;
        stage.setTitle("Product Management");
        stage.setResizable(true);

        // ── Header ────────────────────────────────────────────────────────────
        FontIcon boxIcon = faIcon("fas-box-open", ACCENT2, 22);
        Label titleLbl   = glowLabel("Product Management", 20, FontWeight.BOLD);
        HBox header      = new HBox(10, boxIcon, titleLbl);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 6, 0));

        Rectangle accentLine = new Rectangle(60, 3);
        accentLine.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ACCENT)), new Stop(1, Color.web(ACCENT2))));
        accentLine.setArcWidth(3); accentLine.setArcHeight(3);

        VBox headerBox = vbox(6, header, accentLine);
        headerBox.setPadding(new Insets(0, 0, 16, 0));

        // ── Action buttons ────────────────────────────────────────────────────
        VBox actionsBox = vbox(10);

        // 1. Get product details — everyone
        Button getBtn = menuRowBtn("fas-search", "Get Product Details", "Look up a product by ID");
        getBtn.setOnAction(e -> { animateButtonPress(getBtn); handleGetProduct(connection, null); });
        actionsBox.getChildren().add(getBtn);

        if (isAdmin) {
            Button addBtn = menuRowBtn("fas-plus-circle", "Add Product", "Create a new product listing");
            addBtn.setOnAction(e -> { animateButtonPress(addBtn); handleAddProduct(connection, null); });

            Button delBtn = menuRowBtnDanger("fas-trash-alt", "Delete Product", "Permanently remove a product");
            delBtn.setOnAction(e -> { animateButtonPress(delBtn); handleDeleteProduct(connection, null); });

            Button updBtn = menuRowBtn("fas-edit", "Update Product", "Modify an existing product");
            updBtn.setOnAction(e -> { animateButtonPress(updBtn); handleUpdateProduct(connection, null); });

            actionsBox.getChildren().addAll(addBtn, delBtn, updBtn);
        }

        // ── Close button ──────────────────────────────────────────────────────
        Button closeBtn = dangerIconBtn("fas-times", "Close");
        closeBtn.setOnAction(e -> stage.close());

        VBox content = vbox(20, headerBox, actionsBox, closeBtn);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        Scene scene = new Scene(animatedRoot(content), 500, isAdmin ? 480 : 280);
        stage.setScene(scene);

        FadeTransition ft = new FadeTransition(Duration.millis(400), content);
        ft.setFromValue(0); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(400), content);
        tt.setFromY(16); tt.setToY(0);
        new ParallelTransition(ft, tt).play();

        stage.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleGetProduct
    // ══════════════════════════════════════════════════════════════════════════
    private void handleGetProduct(clientConnection connection, Scanner scanner) {
        Stage dlg = dialogStage("Get Product Details");

        Label title     = glowLabel("Get Product Details", 16, FontWeight.BOLD);
        TextField idFld = fancyTextField("Product ID", "fas-hashtag");
        Label errLbl    = errorLabel();
        Button searchBtn= gradientIconBtn("fas-search", "Search");

        VBox infoBox = vbox(10);

        searchBtn.setOnAction(e -> {
            String idStr = idFld.getText().trim();
            if (idStr.isEmpty() || !idStr.matches("\\d+")) {
                shakeAndError(errLbl, "Please enter a valid numeric product ID.");
                return;
            }
            searchBtn.setDisable(true);
            updateBtnText(searchBtn, "fas-spinner", "Searching…");
            infoBox.getChildren().clear();

            new Thread(() -> {
                response res = connection.getProduct(Integer.parseInt(idStr));
                Platform.runLater(() -> {
                    searchBtn.setDisable(false);
                    updateBtnText(searchBtn, "fas-search", "Search");
                    if (!res.isSuccess()) {
                        shakeAndError(errLbl, "Not found: " + res.getMessage());
                        return;
                    }
                    errLbl.setVisible(false); errLbl.setManaged(false);
                    String raw = res.getMessage().replace("\\n", "\n");

                    String[][] fields = {
                        {"fas-hashtag",       "ID",          "id"},
                        {"fas-box",           "Name",        "name"},
                        {"fas-align-left",    "Description", "description"},
                        {"fas-dollar-sign",   "Price",       "price"},
                        {"fas-cubes",         "Stock",       "stock"},
                        {"fas-tag",           "Category",    "category"},
                        {"fas-user-shield",   "Created By",  "created by"},
                    };

                    for (String[] f : fields) {
                        String value = "—";
                        for (String line : raw.split("\n")) {
                            String s = line.replaceAll("[^\\x20-\\x7E]", "").trim();
                            if (s.toLowerCase().contains(f[2] + ":")) {
                                int idx = s.indexOf(":"); if (idx >= 0) { value = s.substring(idx + 1).trim(); break; }
                            }
                        }
                        infoBox.getChildren().add(infoRow(f[0], f[1], value));
                    }
                });
            }).start();
        });

        ScrollPane sp = new ScrollPane(infoBox);
        sp.setFitToWidth(true);
        sp.setPrefHeight(260);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button close = gradientIconBtn("fas-times", "Close");
        close.setOnAction(e -> dlg.close());

        VBox root = vbox(16, title,
                fancyFieldGroup("Product ID", idFld),
                errLbl, searchBtn, sp, close);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        dlg.setScene(new Scene(animatedRoot(root), 460, 560));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleAddProduct
    // ══════════════════════════════════════════════════════════════════════════
    private void handleAddProduct(clientConnection connection, Scanner scanner) {
        Stage dlg = dialogStage("Add New Product");

        Label     title    = glowLabel("Add New Product", 16, FontWeight.BOLD);
        TextField nameFld  = fancyTextField("Product name",  "fas-box");
        TextField descFld  = fancyTextField("Description",   "fas-align-left");
        TextField catFld   = fancyTextField("Category",      "fas-tag");
        TextField priceFld = fancyTextField("Price",         "fas-dollar-sign");
        TextField stockFld = fancyTextField("Stock quantity","fas-cubes");
        Label     errLbl   = errorLabel();
        Button    addBtn   = gradientIconBtn("fas-plus-circle", "Add Product");

        addBtn.setOnAction(e -> {
            String name  = nameFld.getText().trim();
            String desc  = descFld.getText().trim();
            String cat   = catFld.getText().trim();
            String price = priceFld.getText().trim();
            String stock = stockFld.getText().trim();

            if (name.isEmpty()) { shakeAndError(errLbl, "Product name is required!"); return; }
            double priceVal;
            int    stockVal;
            try { priceVal = Double.parseDouble(price); }
            catch (NumberFormatException ex) { shakeAndError(errLbl, "Invalid price!"); return; }
            try { stockVal = Integer.parseInt(stock); }
            catch (NumberFormatException ex) { shakeAndError(errLbl, "Invalid stock!"); return; }

            addBtn.setDisable(true);
            updateBtnText(addBtn, "fas-spinner", "Adding…");

            new Thread(() -> {
                response res = connection.addProduct(name, desc, priceVal, stockVal, cat);
                Platform.runLater(() -> {
                    addBtn.setDisable(false);
                    updateBtnText(addBtn, "fas-plus-circle", "Add Product");
                    if (res.isSuccess()) { dlg.close(); showSuccessDialog("Product added successfully!"); }
                    else shakeAndError(errLbl, "Failed: " + res.getMessage());
                });
            }).start();
        });

        Button close = dangerIconBtn("fas-times", "Cancel");
        close.setOnAction(e -> dlg.close());

        ScrollPane sp = new ScrollPane(vbox(16, title,
                fancyFieldGroup("Name",        nameFld),
                fancyFieldGroup("Description", descFld),
                fancyFieldGroup("Category",    catFld),
                fancyFieldGroup("Price",       priceFld),
                fancyFieldGroup("Stock",       stockFld),
                errLbl, addBtn, close));
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox root = new VBox(sp);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        dlg.setScene(new Scene(animatedRoot(root), 460, 560));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleDeleteProduct
    // ══════════════════════════════════════════════════════════════════════════
    private void handleDeleteProduct(clientConnection connection, Scanner scanner) {
        Stage dlg = dialogStage("Delete Product");

        Label     title  = glowLabel("Delete Product", 16, FontWeight.BOLD);
        TextField idFld  = fancyTextField("Product ID to delete", "fas-hashtag");
        Label     errLbl = errorLabel();

        // Warning notice
        HBox warnBox = new HBox(10,
                faIcon("fas-exclamation-triangle", ERROR_C, 14),
                label("This action is permanent and cannot be undone.", 12, FontWeight.NORMAL, ERROR_C));
        warnBox.setAlignment(Pos.CENTER_LEFT);
        warnBox.setPadding(new Insets(10, 14, 10, 14));
        warnBox.setStyle(
            "-fx-background-color: rgba(251,113,133,0.08);" +
            "-fx-border-color: rgba(251,113,133,0.30);" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );

        Button delBtn  = dangerActionBtn("fas-trash-alt", "Delete Product");
        Button cancel  = glassIconBtn("fas-times", "Cancel");
        cancel.setOnAction(e -> dlg.close());

        delBtn.setOnAction(e -> {
            String idStr = idFld.getText().trim();
            if (idStr.isEmpty() || !idStr.matches("\\d+")) {
                shakeAndError(errLbl, "Please enter a valid numeric product ID."); return;
            }
            // Confirmation step
            delBtn.setDisable(true);
            updateBtnText(delBtn, "fas-spinner", "Deleting…");

            new Thread(() -> {
                response res = connection.deleteProduct(Integer.parseInt(idStr));
                Platform.runLater(() -> {
                    delBtn.setDisable(false);
                    updateBtnText(delBtn, "fas-trash-alt", "Delete Product");
                    if (res.isSuccess()) { dlg.close(); showSuccessDialog("Product deleted successfully!"); }
                    else shakeAndError(errLbl, "Failed: " + res.getMessage());
                });
            }).start();
        });

        VBox root = vbox(16, title, warnBox,
                fancyFieldGroup("Product ID", idFld),
                errLbl, delBtn, cancel);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        dlg.setScene(new Scene(animatedRoot(root), 460, 380));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleUpdateProduct
    // ══════════════════════════════════════════════════════════════════════════
    private void handleUpdateProduct(clientConnection connection, Scanner scanner) {
        Stage dlg = dialogStage("Update Product");

        Label     title    = glowLabel("Update Product", 16, FontWeight.BOLD);
        TextField idFld    = fancyTextField("Product ID",       "fas-hashtag");
        TextField nameFld  = fancyTextField("New name",         "fas-box");
        TextField descFld  = fancyTextField("New description",  "fas-align-left");
        TextField priceFld = fancyTextField("New price",        "fas-dollar-sign");
        TextField stockFld = fancyTextField("New stock",        "fas-cubes");
        TextField catFld   = fancyTextField("New category",     "fas-tag");
        Label     errLbl   = errorLabel();
        Button    saveBtn  = gradientIconBtn("fas-save", "Save Changes");

        saveBtn.setOnAction(e -> {
            String idStr = idFld.getText().trim();
            String name  = nameFld.getText().trim();
            String desc  = descFld.getText().trim();
            String price = priceFld.getText().trim();
            String stock = stockFld.getText().trim();
            String cat   = catFld.getText().trim();

            if (idStr.isEmpty() || !idStr.matches("\\d+")) { shakeAndError(errLbl, "Valid product ID is required!"); return; }
            if (name.isEmpty()) { shakeAndError(errLbl, "Product name is required!"); return; }

            saveBtn.setDisable(true);
            updateBtnText(saveBtn, "fas-spinner", "Saving…");

            new Thread(() -> {
                response res = connection.updateProduct(Integer.parseInt(idStr), name, desc, price, stock, cat);
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    updateBtnText(saveBtn, "fas-save", "Save Changes");
                    if (res.isSuccess()) { dlg.close(); showSuccessDialog("Product updated successfully!"); }
                    else shakeAndError(errLbl, "Failed: " + res.getMessage());
                });
            }).start();
        });

        Button cancel = dangerIconBtn("fas-times", "Cancel");
        cancel.setOnAction(e -> dlg.close());

        ScrollPane sp = new ScrollPane(vbox(16, title,
                fancyFieldGroup("Product ID",    idFld),
                fancyFieldGroup("Name",          nameFld),
                fancyFieldGroup("Description",   descFld),
                fancyFieldGroup("Price",         priceFld),
                fancyFieldGroup("Stock",         stockFld),
                fancyFieldGroup("Category",      catFld),
                errLbl, saveBtn, cancel));
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox root = new VBox(sp);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        dlg.setScene(new Scene(animatedRoot(root), 460, 580));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI HELPERS  (mirrors authMenu helpers)
    // ══════════════════════════════════════════════════════════════════════════

    private HBox infoRow(String iconCode, String key, String value) {
        FontIcon iv = faIcon(iconCode, ACCENT2, 14);
        StackPane iconWrap = new StackPane(iv);
        iconWrap.setMinSize(32, 32); iconWrap.setMaxSize(32, 32);
        iconWrap.setStyle("-fx-background-color: rgba(124,111,253,0.12); -fx-background-radius: 8;");

        Label keyLbl = label(key,   11, FontWeight.BOLD,   TEXT_SEC);
        Label valLbl = label(value, 14, FontWeight.BOLD,   TEXT_PRI);
        VBox  col    = vbox(2, keyLbl, valLbl);
        HBox.setHgrow(col, Priority.ALWAYS);

        HBox row = new HBox(12, iconWrap, col);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );
        row.setOnMouseEntered(e -> row.setStyle(row.getStyle().replace("rgba(255,255,255,0.03)", "rgba(124,111,253,0.10)")));
        row.setOnMouseExited (e -> row.setStyle(row.getStyle().replace("rgba(124,111,253,0.10)", "rgba(255,255,255,0.03)")));

        FadeTransition ft = new FadeTransition(Duration.millis(250), row);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
        return row;
    }

    private Button menuRowBtn(String iconCode, String title, String sub) {
        FontIcon iv   = faIcon(iconCode, ACCENT2, 16);
        StackPane ivW = new StackPane(iv); ivW.setMinWidth(28); ivW.setPrefWidth(28);
        Label tLbl    = label(title, 13, FontWeight.BOLD,   TEXT_PRI);
        Label sLbl    = label(sub,   11, FontWeight.NORMAL, TEXT_SEC);
        VBox  txt     = vbox(2, tLbl, sLbl);
        FontIcon arr  = faIcon("fas-angle-right", TEXT_SEC, 16);
        HBox  content = new HBox(14, ivW, txt, arr);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txt, Priority.ALWAYS);

        Button b = new Button();
        b.setGraphic(content);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(58);
        b.setPadding(new Insets(0, 16, 0, 16));
        b.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> { b.setStyle(b.getStyle().replace("rgba(255,255,255,0.03)", "rgba(124,111,253,0.10)")); arr.setIconColor(Color.web(ACCENT)); });
        b.setOnMouseExited (e -> { b.setStyle(b.getStyle().replace("rgba(124,111,253,0.10)", "rgba(255,255,255,0.03)")); arr.setIconColor(Color.web(TEXT_SEC)); });
        return b;
    }

    private Button menuRowBtnDanger(String iconCode, String title, String sub) {
        Button b = menuRowBtn(iconCode, title, sub);
        b.setStyle(b.getStyle().replace(BORDER, "rgba(251,113,133,0.25)"));
        return b;
    }

    private FontIcon faIcon(String code, String color, double size) {
        FontIcon i = new FontIcon(code);
        i.setIconSize((int) size);
        i.setIconColor(Color.web(color));
        return i;
    }

    private HBox iconText(String code, String text, String color, double size) {
        FontIcon iv  = faIcon(code, color, size);
        Label   lbl  = new Label("  " + text);
        lbl.setTextFill(Color.web(TEXT_PRI));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        HBox box = new HBox(6, iv, lbl);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Label glowLabel(String text, double size, FontWeight w) {
        Label l = new Label(text);
        l.setFont(Font.font("System", w, size));
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
        VBox b = new VBox(spacing, nodes); b.setFillWidth(true); return b;
    }

    private VBox vbox(double spacing) {
        VBox b = new VBox(spacing); b.setFillWidth(true); return b;
    }

    private TextField fancyTextField(String prompt, String iconCode) {
        TextField f = new TextField();
        f.setPromptText(prompt);
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
        f.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) { f.setStyle(f.getStyle().replace(BORDER, ACCENT)); f.setEffect(new DropShadow(8, Color.web(GLOW))); }
            else         { f.setStyle(f.getStyle().replace(ACCENT, BORDER)); f.setEffect(null); }
        });
        return f;
    }

    private VBox fancyFieldGroup(String labelText, Control field) {
        Label lbl = label(labelText, 11, FontWeight.BOLD, TEXT_SEC);
        return vbox(5, lbl, field);
    }

    private Button gradientIconBtn(String code, String text) {
        Button b = new Button();
        b.setGraphic(iconText(code, text, "white", 14));
        b.setMaxWidth(Double.MAX_VALUE); b.setPrefHeight(48);
        b.setStyle(
            "-fx-background-color: linear-gradient(to right, " + ACCENT + ", " + ACCENT2 + ");" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + GLOW + ", 12, 0.3, 0, 3);"
        );
        b.setOnMouseEntered(e -> b.setOpacity(0.88));
        b.setOnMouseExited (e -> b.setOpacity(1.0));
        return b;
    }

    private Button dangerActionBtn(String code, String text) {
        Button b = new Button();
        b.setGraphic(iconText(code, text, "white", 14));
        b.setMaxWidth(Double.MAX_VALUE); b.setPrefHeight(48);
        b.setStyle(
            "-fx-background-color: linear-gradient(to right, #e11d48, #fb7185);" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(251,113,133,0.4), 12, 0.3, 0, 3);"
        );
        b.setOnMouseEntered(e -> b.setOpacity(0.88));
        b.setOnMouseExited (e -> b.setOpacity(1.0));
        return b;
    }

    private Button glassIconBtn(String code, String text) {
        Button b = new Button();
        b.setGraphic(iconText(code, text, TEXT_SEC, 13));
        b.setMaxWidth(Double.MAX_VALUE); b.setPrefHeight(44);
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

    private Button dangerIconBtn(String code, String text) {
        Button b = new Button();
        b.setGraphic(iconText(code, text, ERROR_C, 13));
        b.setMaxWidth(Double.MAX_VALUE); b.setPrefHeight(44);
        b.setStyle(
            "-fx-background-color: rgba(124,111,253,0.12);" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("rgba(124,111,253,0.12)", "rgba(251,113,133,0.15)")));
        b.setOnMouseExited (e -> b.setStyle(b.getStyle().replace("rgba(251,113,133,0.15)", "rgba(124,111,253,0.12)")));
        return b;
    }

    private void updateBtnText(Button b, String code, String text) {
        b.setGraphic(iconText(code, text, "white", 14));
    }

    private Label errorLabel() {
        Label l = new Label("");
        l.setTextFill(Color.web(ERROR_C));
        l.setFont(Font.font("System", FontWeight.NORMAL, 12));
        l.setGraphic(faIcon("fas-exclamation-triangle", ERROR_C, 12));
        l.setGraphicTextGap(6);
        l.setWrapText(true);
        l.setVisible(false); l.setManaged(false);
        return l;
    }

    private void shakeAndError(Label lbl, String msg) {
        lbl.setText(msg); lbl.setVisible(true); lbl.setManaged(true);
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
        dlg.initModality(Modality.APPLICATION_MODAL);
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
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        dlg.setScene(new Scene(animatedRoot(content), 400, 240));
        FadeTransition ft = new FadeTransition(Duration.millis(300), dlg.getScene().getRoot());
        ft.setFromValue(0); ft.setToValue(1);
        dlg.show(); ft.play();
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

        TranslateTransition d1 = new TranslateTransition(Duration.seconds(9), orb1);
        d1.setFromX(-80); d1.setToX(-30); d1.setFromY(-80); d1.setToY(-110);
        d1.setAutoReverse(true); d1.setCycleCount(Animation.INDEFINITE); d1.play();

        TranslateTransition d2 = new TranslateTransition(Duration.seconds(11), orb2);
        d2.setFromX(80); d2.setToX(30); d2.setFromY(80); d2.setToY(120);
        d2.setAutoReverse(true); d2.setCycleCount(Animation.INDEFINITE); d2.play();

        sp.getChildren().addAll(orb1, orb2, content);
        return sp;
    }

    private Stage dialogStage(String title) {
        Stage dlg = new Stage();
        dlg.setTitle(title);
        if (ownerStage != null) dlg.initOwner(ownerStage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.setResizable(false);
        return dlg;
    }
}