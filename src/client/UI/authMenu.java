package client.UI;

import client.clientConnection;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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

public class authMenu {

    // ── palette ───────────────────────────────────────────────────────────────
    private static final String BG          = "#080818";
    private static final String CARD_BG     = "#10102a";
    private static final String PANEL_BG    = "#0b0b20";
    private static final String ACCENT      = "#7c6ffd";
    private static final String ACCENT2     = "#c084fc";
    private static final String ACCENT_DARK = "#5a50e0";
    private static final String TEXT_PRI    = "#f0f0ff";
    private static final String TEXT_SEC    = "#8888aa";
    private static final String BORDER      = "#2a2a4a";
    private static final String SUCCESS_C   = "#34d399";
    private static final String ERROR_C     = "#fb7185";
    private static final String FIELD_BG    = "#0d0d22";
    private static final String GLOW        = "#7c6ffd66";
    private static final String DIVIDER     = "#1e1e3a";

    private Stage            stage;
    private clientConnection connection;

    // ── FA icon helper ────────────────────────────────────────────────────────
    private FontAwesomeIconView faIcon(FontAwesomeIcon icon, String color, double size) {
        FontAwesomeIconView v = new FontAwesomeIconView(icon);
        v.setGlyphSize(size);
        v.setFill(Color.web(color));
        return v;
    }

    private HBox iconText(FontAwesomeIcon icon, String text, String iconColor, double iconSize) {
        FontAwesomeIconView iv = faIcon(icon, iconColor, iconSize);
        Label lbl = new Label("  " + text);
        lbl.setTextFill(Color.web(TEXT_PRI));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        HBox box = new HBox(6, iv, lbl);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // ── public entry point ────────────────────────────────────────────────────
    public void show(clientConnection connection, Stage stage) {
        this.connection = connection;
        this.stage      = stage;
        stage.setTitle("ChriOnline");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        if (!showAuthMenu()) return;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  showAuthMenu  —  Side-by-side desktop layout
    // ══════════════════════════════════════════════════════════════════════════
    private boolean showAuthMenu() {

        // ── LEFT PANEL: branding + login ──────────────────────────────────────
        FontAwesomeIconView starIcon = faIcon(FontAwesomeIcon.STAR, ACCENT2, 26);
        Label logoText = glowLabel("ChriOnline", 32, FontWeight.BOLD);
        HBox logo = new HBox(10, starIcon, logoText);
        logo.setAlignment(Pos.CENTER_LEFT);

        Label tagLine = label("Your premium online shopping portal", 13, FontWeight.NORMAL, TEXT_SEC);

        // Pulse animation on logo
        ScaleTransition pulse = new ScaleTransition(Duration.millis(2200), logo);
        pulse.setFromX(1.0); pulse.setToX(1.03);
        pulse.setFromY(1.0); pulse.setToY(1.03);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        // Thin accent line under branding
        Rectangle accentLine = new Rectangle(60, 3);
        accentLine.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ACCENT)),
                new Stop(1, Color.web(ACCENT2))));
        accentLine.setArcWidth(3); accentLine.setArcHeight(3);

        VBox brandBox = vbox(8, logo, tagLine, accentLine);
        brandBox.setPadding(new Insets(0, 0, 28, 0));

        // Login form
        Label loginTitle = label("Sign In", 20, FontWeight.BOLD, TEXT_PRI);
        Label loginSub   = label("Welcome back", 12, FontWeight.NORMAL, TEXT_SEC);
        VBox loginHeader = vbox(3, loginTitle, loginSub);

        VBox loginForm = buildLoginTab();

        VBox leftContent = vbox(24, brandBox, loginHeader, loginForm);
        leftContent.setPadding(new Insets(44, 40, 44, 44));

        // Bottom action buttons for left panel
        Button guestBtn = glassIconBtn(FontAwesomeIcon.SHOPPING_BAG, "Browse as Guest");
        guestBtn.setOnAction(e -> showGuestProducts());
        Button exitBtn  = dangerIconBtn(FontAwesomeIcon.SIGN_OUT, "Exit");
        exitBtn.setOnAction(e -> stage.close());

        HBox bottomBtns = new HBox(12, guestBtn, exitBtn);
        bottomBtns.setPadding(new Insets(0, 40, 32, 44));
        HBox.setHgrow(guestBtn, Priority.ALWAYS);
        HBox.setHgrow(exitBtn,  Priority.ALWAYS);

        VBox leftPanel = new VBox();
        VBox.setVgrow(leftContent, Priority.ALWAYS);
        leftPanel.getChildren().addAll(leftContent, bottomBtns);
        leftPanel.setStyle(
            "-fx-background-color: " + PANEL_BG + ";" +
            "-fx-border-color: transparent " + DIVIDER + " transparent transparent;" +
            "-fx-border-width: 0 1 0 0;"
        );
        leftPanel.setMinWidth(430);
        leftPanel.setMaxWidth(460);

        // ── RIGHT PANEL: register ─────────────────────────────────────────────
        Label regTitle = label("Create Account", 20, FontWeight.BOLD, TEXT_PRI);
        Label regSub   = label("Join ChriOnline today", 12, FontWeight.NORMAL, TEXT_SEC);
        VBox regHeader = vbox(3, regTitle, regSub);

        // Thin accent line
        Rectangle accentLine2 = new Rectangle(60, 3);
        accentLine2.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ACCENT2)),
                new Stop(1, Color.web(ACCENT))));
        accentLine2.setArcWidth(3); accentLine2.setArcHeight(3);

        VBox regHeaderFull = vbox(8, regHeader, accentLine2);

        VBox registerForm = buildRegisterTab();
        VBox.setVgrow(registerForm, Priority.ALWAYS);

        VBox rightContent = vbox(24, regHeaderFull, registerForm);
        rightContent.setPadding(new Insets(44, 44, 44, 40));

        VBox rightPanel = new VBox(rightContent);
        VBox.setVgrow(rightContent, Priority.ALWAYS);
        rightPanel.setStyle("-fx-background-color: " + CARD_BG + ";");

        // ── Split layout ──────────────────────────────────────────────────────
        HBox splitLayout = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        splitLayout.setFillHeight(true);

        // Outer window chrome
        StackPane root = animatedRoot(splitLayout);

        Scene scene = new Scene(root, 920, 640);
        scene.getStylesheets().add(buildCSS());

        // Entrance animation
        FadeTransition ft = new FadeTransition(Duration.millis(500), splitLayout);
        ft.setFromValue(0); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), splitLayout);
        tt.setFromY(20); tt.setToY(0);
        new ParallelTransition(ft, tt).play();

        stage.setScene(scene);
        stage.show();
        return true;
    }

    // ── Login tab ─────────────────────────────────────────────────────────────
    private VBox buildLoginTab() {
        TextField     emailFld = fancyTextField("Email address",   FontAwesomeIcon.ENVELOPE);
        PasswordField passFld  = fancyPassField("Password",        FontAwesomeIcon.LOCK);
        Label         errLbl   = errorLabel();
        Button        loginBtn = gradientIconBtn(FontAwesomeIcon.KEY, "Login");

        loginBtn.setOnAction(e -> {
            String email = emailFld.getText().trim();
            String pass  = passFld.getText().trim();
            loginBtn.setDisable(true);
            updateBtnText(loginBtn, FontAwesomeIcon.SPINNER, "Authenticating…");
            animateButtonPress(loginBtn);
            new Thread(() -> {
                boolean ok = handleLogin(email, pass);
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    updateBtnText(loginBtn, FontAwesomeIcon.KEY, "Login");
                    if (!ok) shakeAndError(errLbl, "Login failed. Check your credentials.");
                });
            }).start();
        });

        VBox form = vbox(14,
                fancyFieldGroup("Email",    emailFld),
                fancyFieldGroup("Password", passFld),
                errLbl, loginBtn);
        return form;
    }

    // ── Register tab ──────────────────────────────────────────────────────────
    private VBox buildRegisterTab() {
        TextField     userFld    = fancyTextField("Username",               FontAwesomeIcon.USER);
        TextField     emailFld   = fancyTextField("Email address",          FontAwesomeIcon.ENVELOPE);
        PasswordField passFld    = fancyPassField("Password (min 6 chars)", FontAwesomeIcon.LOCK);
        PasswordField confirmFld = fancyPassField("Confirm password",       FontAwesomeIcon.CHECK);
        TextField     addrFld    = fancyTextField("Address (optional)",     FontAwesomeIcon.MAP_MARKER);
        TextField     phoneFld   = fancyTextField("Phone (optional)",       FontAwesomeIcon.PHONE);
        Label         errLbl     = errorLabel();
        Button        regBtn     = gradientIconBtn(FontAwesomeIcon.PENCIL, "Create Account");

        regBtn.setOnAction(e -> {
            regBtn.setDisable(true);
            updateBtnText(regBtn, FontAwesomeIcon.SPINNER, "Creating account…");
            String user  = userFld.getText().trim();
            String email = emailFld.getText().trim();
            String pass  = passFld.getText().trim();
            String conf  = confirmFld.getText().trim();
            String addr  = addrFld.getText().trim();
            String phone = phoneFld.getText().trim();

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                shakeAndError(errLbl, "Username, email, and password are required!");
                regBtn.setDisable(false);
                updateBtnText(regBtn, FontAwesomeIcon.PENCIL, "Create Account");
                return;
            }
            if (!pass.equals(conf)) {
                shakeAndError(errLbl, "Passwords don't match!");
                regBtn.setDisable(false);
                updateBtnText(regBtn, FontAwesomeIcon.PENCIL, "Create Account");
                return;
            }
            new Thread(() -> handleRegister(user, email, pass, addr, phone, errLbl, regBtn)).start();
        });

        VBox form = vbox(10,
                fancyFieldGroup("Username",         userFld),
                fancyFieldGroup("Email",            emailFld),
                fancyFieldGroup("Password",         passFld),
                fancyFieldGroup("Confirm password", confirmFld),
                fancyFieldGroup("Address",          addrFld),
                fancyFieldGroup("Phone",            phoneFld),
                errLbl, regBtn);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        return new VBox(sp);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleLogin
    // ══════════════════════════════════════════════════════════════════════════
    private boolean handleLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) return false;
        response res = connection.login(email, password);
        if (res.isSuccess()) {
            String[] parts = res.getMessage().split("\\|");
            if (parts.length > 1) connection.setSessionToken(parts[1]);
            if (parts.length > 2) connection.setUserRole(parts[2]);
            Platform.runLater(this::showMainMenu);
            return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleRegister
    // ══════════════════════════════════════════════════════════════════════════
    private void handleRegister(String username, String email, String password,
                                 String address, String phone,
                                 Label errLbl, Button regBtn) {
        response res = connection.register(username, email, password, address, phone);
        Platform.runLater(() -> {
            regBtn.setDisable(false);
            updateBtnText(regBtn, FontAwesomeIcon.PENCIL, "Create Account");
            if (res.isSuccess()) showSuccessDialog("Account created! Please log in.");
            else shakeAndError(errLbl, "Registration failed: " + res.getMessage());
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  showMainMenu
    // ══════════════════════════════════════════════════════════════════════════
    private void showMainMenu() {
        boolean isAdmin = connection.isAdmin();

        // ── Sidebar ───────────────────────────────────────────────────────────
        FontAwesomeIconView sidebarStar = faIcon(FontAwesomeIcon.STAR, ACCENT2, 18);
        Label sidebarLogo = label("ChriOnline", 16, FontWeight.BOLD, TEXT_PRI);
        HBox sidebarBrand = new HBox(8, sidebarStar, sidebarLogo);
        sidebarBrand.setAlignment(Pos.CENTER_LEFT);
        sidebarBrand.setPadding(new Insets(0, 0, 16, 0));

        Rectangle sidebarLine = new Rectangle(40, 2);
        sidebarLine.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ACCENT)), new Stop(1, Color.web(ACCENT2))));

        HBox roleRow = new HBox(6,
                faIcon(isAdmin ? FontAwesomeIcon.BOLT : FontAwesomeIcon.HAND_PAPER_ALT,
                        isAdmin ? ACCENT2 : SUCCESS_C, 11),
                label(isAdmin ? "Admin" : "Member", 11, FontWeight.BOLD,
                        isAdmin ? ACCENT2 : SUCCESS_C));
        roleRow.setAlignment(Pos.CENTER_LEFT);
        roleRow.setPadding(new Insets(8, 0, 20, 0));

        VBox sidebarMenu = vbox(4);

        addSidebarBtn(sidebarMenu, FontAwesomeIcon.USER,         "View My Info",      () -> showUserInfo());
        addSidebarBtn(sidebarMenu, FontAwesomeIcon.BAR_CHART,    "My Profile",        () -> getProfile());
        addSidebarBtn(sidebarMenu, FontAwesomeIcon.EDIT,         "Update Profile",    () -> updateProfile());
        addSidebarBtn(sidebarMenu, FontAwesomeIcon.LOCK,         "Change Password",   () -> changePassword());
        addSidebarBtn(sidebarMenu, FontAwesomeIcon.SHOPPING_BAG, "Product Catalog",   () -> showProductList());

        if (isAdmin) {
            Button adminBtn = sidebarAdminBtn(FontAwesomeIcon.WRENCH, "Product Mgmt");
            adminBtn.setOnAction(e -> new productMenu().show(connection, null, true));
            sidebarMenu.getChildren().add(adminBtn);
        }

        addSidebarBtn(sidebarMenu, FontAwesomeIcon.SHOPPING_CART, "Shopping Cart",   () -> new cartMenu(connection, null).show());
        addSidebarBtn(sidebarMenu, FontAwesomeIcon.CUBE,           "My Orders",      () -> new orderMenu(connection, null).show());
        addSidebarBtn(sidebarMenu, FontAwesomeIcon.CREDIT_CARD,    "Payments",       () -> new paymentMenu(connection, null).show());

        VBox.setVgrow(sidebarMenu, Priority.ALWAYS);

        Button logoutBtn = sidebarDangerBtn(FontAwesomeIcon.SIGN_OUT, "Logout");
        logoutBtn.setOnAction(e -> handleLogout());

        VBox sidebar = new VBox(sidebarBrand, sidebarLine, roleRow, sidebarMenu, logoutBtn);
        sidebar.setPadding(new Insets(28, 16, 24, 20));
        sidebar.setStyle(
            "-fx-background-color: " + PANEL_BG + ";" +
            "-fx-border-color: transparent " + DIVIDER + " transparent transparent;" +
            "-fx-border-width: 0 1 0 0;"
        );
        sidebar.setMinWidth(200);
        sidebar.setMaxWidth(220);
        VBox.setVgrow(sidebarMenu, Priority.ALWAYS);

        // ── Main content area ─────────────────────────────────────────────────
        Label dashTitle = glowLabel("Dashboard", 26, FontWeight.BOLD);
        Label dashSub   = label("Manage your account and orders", 13, FontWeight.NORMAL, TEXT_SEC);
        VBox dashHeader = vbox(4, dashTitle, dashSub);
        dashHeader.setPadding(new Insets(0, 0, 24, 0));

        // Welcome card
        HBox welcomeCard = new HBox(14,
                faIcon(isAdmin ? FontAwesomeIcon.BOLT : FontAwesomeIcon.HAND_PAPER_ALT,
                        isAdmin ? ACCENT2 : SUCCESS_C, 22),
                vbox(3,
                        label(isAdmin ? "Admin Access Granted" : "Welcome back!",
                                15, FontWeight.BOLD, isAdmin ? ACCENT2 : SUCCESS_C),
                        label(isAdmin ? "You have full system access." : "Browse, shop, and manage your orders.",
                                12, FontWeight.NORMAL, TEXT_SEC)));
        welcomeCard.setAlignment(Pos.CENTER_LEFT);
        welcomeCard.setPadding(new Insets(18, 20, 18, 20));
        welcomeCard.setStyle(
            "-fx-background-color: rgba(124,111,253,0.08);" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );

        // Quick-action grid (2 columns)
        GridPane quickGrid = new GridPane();
        quickGrid.setHgap(12); quickGrid.setVgap(12);
        quickGrid.getColumnConstraints().addAll(
                colConstraint(), colConstraint());

        String[][] quickItems = {
            {"SHOPPING_BAG",  "Catalog",      "Browse products"},
            {"SHOPPING_CART", "Cart",         "View your basket"},
            {"CUBE",          "Orders",       "Track your orders"},
            {"CREDIT_CARD",   "Payments",     "Billing history"},
        };
        Runnable[] quickActions = {
            () -> showProductList(),
            () -> new cartMenu(connection, null).show(),
            () -> new orderMenu(connection, null).show(),
            () -> new paymentMenu(connection, null).show(),
        };
        FontAwesomeIcon[] quickIcons = {
            FontAwesomeIcon.SHOPPING_BAG, FontAwesomeIcon.SHOPPING_CART,
            FontAwesomeIcon.CUBE, FontAwesomeIcon.CREDIT_CARD,
        };

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Button card = quickActionCard(quickIcons[i], quickItems[i][1], quickItems[i][2]);
            card.setOnAction(e -> quickActions[idx].run());
            quickGrid.add(card, i % 2, i / 2);
        }

        VBox mainContent = vbox(20, dashHeader, welcomeCard, quickGrid);
        mainContent.setPadding(new Insets(36, 36, 36, 32));

        ScrollPane mainScroll = new ScrollPane(mainContent);
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        HBox dashLayout = new HBox(sidebar, mainScroll);
        HBox.setHgrow(mainScroll, Priority.ALWAYS);
        dashLayout.setFillHeight(true);

        FadeTransition ft = new FadeTransition(Duration.millis(400), dashLayout);
        ft.setFromValue(0); ft.setToValue(1);

        stage.setMinWidth(820);
        stage.setMinHeight(560);
        stage.setScene(new Scene(animatedRoot(dashLayout), 920, 640));
        ft.play();
    }

    // Quick-action card for dashboard grid
    private Button quickActionCard(FontAwesomeIcon icon, String title, String sub) {
        FontAwesomeIconView iv = faIcon(icon, ACCENT2, 22);
        Label titleLbl = label(title, 14, FontWeight.BOLD, TEXT_PRI);
        Label subLbl   = label(sub,   11, FontWeight.NORMAL, TEXT_SEC);
        VBox  content  = vbox(6, iv, titleLbl, subLbl);
        content.setPadding(new Insets(20, 16, 20, 16));

        Button b = new Button();
        b.setGraphic(content);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(110);
        b.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> {
            b.setStyle(b.getStyle().replace("rgba(255,255,255,0.03)", "rgba(124,111,253,0.12)"));
            iv.setFill(Color.web(ACCENT));
        });
        b.setOnMouseExited(e -> {
            b.setStyle(b.getStyle().replace("rgba(124,111,253,0.12)", "rgba(255,255,255,0.03)"));
            iv.setFill(Color.web(ACCENT2));
        });
        GridPane.setHgrow(b, Priority.ALWAYS);
        return b;
    }

    private ColumnConstraints colConstraint() {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        cc.setFillWidth(true);
        return cc;
    }

    // ── Sidebar buttons ───────────────────────────────────────────────────────
    private void addSidebarBtn(VBox parent, FontAwesomeIcon icon, String title, Runnable action) {
        Button b = sidebarBtn(icon, title);
        b.setOnAction(e -> { animateButtonPress(b); action.run(); });
        parent.getChildren().add(b);
    }

    private Button sidebarBtn(FontAwesomeIcon icon, String title) {
        FontAwesomeIconView iv = faIcon(icon, TEXT_SEC, 13);
        Label lbl = label("  " + title, 13, FontWeight.NORMAL, TEXT_SEC);
        HBox content = new HBox(10, iv, lbl);
        content.setAlignment(Pos.CENTER_LEFT);

        Button b = new Button();
        b.setGraphic(content);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(40);
        b.setPadding(new Insets(0, 12, 0, 12));
        b.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> {
            b.setStyle(b.getStyle().replace("transparent;-fx-border", "rgba(124,111,253,0.12);-fx-border"));
            iv.setFill(Color.web(ACCENT2));
            lbl.setTextFill(Color.web(TEXT_PRI));
        });
        b.setOnMouseExited(e -> {
            b.setStyle(b.getStyle().replace("rgba(124,111,253,0.12);-fx-border", "transparent;-fx-border"));
            iv.setFill(Color.web(TEXT_SEC));
            lbl.setTextFill(Color.web(TEXT_SEC));
        });
        return b;
    }

    private Button sidebarAdminBtn(FontAwesomeIcon icon, String title) {
        Button b = sidebarBtn(icon, title);
        b.setStyle(b.getStyle() + "-fx-border-color: " + ACCENT + "44; -fx-border-radius: 8;");
        return b;
    }

    private Button sidebarDangerBtn(FontAwesomeIcon icon, String title) {
        FontAwesomeIconView iv = faIcon(icon, ERROR_C, 13);
        Label lbl = label("  " + title, 13, FontWeight.NORMAL, ERROR_C);
        HBox content = new HBox(10, iv, lbl);
        content.setAlignment(Pos.CENTER_LEFT);

        Button b = new Button();
        b.setGraphic(content);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(40);
        b.setPadding(new Insets(0, 12, 0, 12));
        b.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("transparent", "rgba(251,113,133,0.10)")));
        b.setOnMouseExited (e -> b.setStyle(b.getStyle().replace("rgba(251,113,133,0.10)", "transparent")));
        return b;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  handleLogout
    // ══════════════════════════════════════════════════════════════════════════
    private void handleLogout() {
        new Thread(() -> {
            response res = connection.logout();
            Platform.runLater(() -> {
                if (res.isSuccess()) {
                    showSuccessDialog(res.getMessage() + "\nSee you next time!");
                    showAuthMenu();
                } else {
                    showSuccessDialog("Logout warning: " + res.getMessage());
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  showUserInfo
    // ══════════════════════════════════════════════════════════════════════════
    private void showUserInfo() {
        Stage dlg = dialogStage("User Information");
        Label title  = glowLabel("USER INFORMATION", 16, FontWeight.BOLD);
        TextArea area = fancyReadonlyArea("Loading…");
        new Thread(() -> {
            response res = connection.getUserInfo();
            Platform.runLater(() ->
                area.setText(res.isSuccess()
                    ? res.getMessage().replace("\\n", "\n")
                    : "Unable to load user info.\nError: " + res.getMessage()));
        }).start();
        Button ok = gradientIconBtn(FontAwesomeIcon.CHECK, "Close");
        ok.setOnAction(e -> dlg.close());
        dlg.setScene(new Scene(animatedRoot(fancyCard(vbox(16, title, area, ok))), 520, 440));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  getProfile
    // ══════════════════════════════════════════════════════════════════════════
    private void getProfile() {
        Stage dlg = dialogStage("Your Profile");
        Label title  = glowLabel("YOUR PROFILE", 16, FontWeight.BOLD);
        TextArea area = fancyReadonlyArea("Loading…");
        new Thread(() -> {
            response res = connection.getProfile();
            Platform.runLater(() ->
                area.setText(res.isSuccess()
                    ? res.getMessage().replace("\\n", "\n")
                    : "Unable to load profile.\nError: " + res.getMessage()));
        }).start();
        Button ok = gradientIconBtn(FontAwesomeIcon.CHECK, "Close");
        ok.setOnAction(e -> dlg.close());
        dlg.setScene(new Scene(animatedRoot(fancyCard(vbox(16, title, area, ok))), 520, 440));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  updateProfile
    // ══════════════════════════════════════════════════════════════════════════
    private void updateProfile() {
        Stage dlg = dialogStage("Update Profile");
        TextField addrFld  = fancyTextField("New address",      FontAwesomeIcon.MAP_MARKER);
        TextField phoneFld = fancyTextField("New phone number", FontAwesomeIcon.PHONE);
        Label     errLbl   = errorLabel();
        Button    saveBtn  = gradientIconBtn(FontAwesomeIcon.SAVE, "Save Changes");

        saveBtn.setOnAction(e -> {
            String addr  = addrFld.getText().trim();
            String phone = phoneFld.getText().trim();
            if (addr.isEmpty() && phone.isEmpty()) {
                shakeAndError(errLbl, "Both fields are empty — nothing to update."); return;
            }
            saveBtn.setDisable(true);
            updateBtnText(saveBtn, FontAwesomeIcon.SPINNER, "Saving…");
            new Thread(() -> {
                response res = connection.updateProfile(addr, phone);
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    updateBtnText(saveBtn, FontAwesomeIcon.SAVE, "Save Changes");
                    if (res.isSuccess()) { dlg.close(); showSuccessDialog("Profile updated!"); }
                    else shakeAndError(errLbl, "Failed: " + res.getMessage());
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(fancyCard(vbox(20,
                glowLabel("Update Profile", 18, FontWeight.BOLD),
                fancyFieldGroup("New address",      addrFld),
                fancyFieldGroup("New phone number", phoneFld),
                errLbl, saveBtn))), 500, 400));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  changePassword
    // ══════════════════════════════════════════════════════════════════════════
    private void changePassword() {
        Stage dlg = dialogStage("Change Password");
        PasswordField oldFld  = fancyPassField("Current password",     FontAwesomeIcon.KEY);
        PasswordField newFld  = fancyPassField("New password",         FontAwesomeIcon.LOCK);
        PasswordField confFld = fancyPassField("Confirm new password",  FontAwesomeIcon.CHECK);
        Label         errLbl  = errorLabel();
        Button        saveBtn = gradientIconBtn(FontAwesomeIcon.LOCK, "Change Password");

        saveBtn.setOnAction(e -> {
            String oldPass = oldFld.getText();
            String newPass = newFld.getText();
            String confirm = confFld.getText();
            if (oldPass.isEmpty() || newPass.isEmpty()) { shakeAndError(errLbl, "Password cannot be empty!"); return; }
            if (newPass.length() < 6) { shakeAndError(errLbl, "New password must be at least 6 characters!"); return; }
            if (!newPass.equals(confirm)) { shakeAndError(errLbl, "Passwords don't match!"); return; }
            saveBtn.setDisable(true);
            updateBtnText(saveBtn, FontAwesomeIcon.REFRESH, "Changing…");
            new Thread(() -> {
                response res = connection.changePassword(oldPass, newPass);
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    updateBtnText(saveBtn, FontAwesomeIcon.LOCK, "Change Password");
                    if (res.isSuccess()) { dlg.close(); showSuccessDialog("Password changed!"); }
                    else shakeAndError(errLbl, "Failed: " + res.getMessage());
                });
            }).start();
        });

        dlg.setScene(new Scene(animatedRoot(fancyCard(vbox(20,
                glowLabel("Change Password", 18, FontWeight.BOLD),
                fancyFieldGroup("Current password",  oldFld),
                fancyFieldGroup("New password",      newFld),
                fancyFieldGroup("Confirm password",  confFld),
                errLbl, saveBtn))), 500, 480));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  showGuestProducts
    // ══════════════════════════════════════════════════════════════════════════
    private void showGuestProducts() {
        Label title  = glowLabel("Product Catalog", 20, FontWeight.BOLD);

        HBox noticeRow = new HBox(6,
                faIcon(FontAwesomeIcon.LOCK, ACCENT2, 13),
                label("Login to unlock all features", 13, FontWeight.NORMAL, ACCENT2));
        noticeRow.setAlignment(Pos.CENTER_LEFT);

        TextArea area = fancyReadonlyArea("Loading products…");

        Button backBtn = glassIconBtn(FontAwesomeIcon.ARROW_LEFT, "Back to Login");
        backBtn.setOnAction(e -> showAuthMenu());

        new Thread(() -> {
            response res = connection.listProducts();
            Platform.runLater(() ->
                area.setText(res.isSuccess()
                    ? res.getMessage().replace("\\n", "\n")
                    : "Unable to load products: " + res.getMessage()));
        }).start();

        VBox card = fancyCard(vbox(16, title, noticeRow, area, backBtn));
        card.setMaxWidth(540);

        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0); ft.setToValue(1);
        stage.setScene(new Scene(animatedRoot(card), 920, 640));
        ft.play();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  showProductList
    // ══════════════════════════════════════════════════════════════════════════
    private void showProductList() {
        Stage dlg = dialogStage("Product Catalog");
        Label title  = glowLabel("Product Catalog", 16, FontWeight.BOLD);
        TextArea area = fancyReadonlyArea("Loading products…");
        new Thread(() -> {
            response res = connection.listProducts();
            Platform.runLater(() ->
                area.setText(res.isSuccess()
                    ? res.getMessage().replace("\\n", "\n")
                    : "Unable to load products: " + res.getMessage()));
        }).start();
        Button ok = gradientIconBtn(FontAwesomeIcon.CHECK, "Close");
        ok.setOnAction(e -> dlg.close());
        dlg.setScene(new Scene(animatedRoot(fancyCard(vbox(16, title, area, ok))), 540, 480));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════════════════════════

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

    private VBox fancyCard(javafx.scene.Node content) {
        VBox c = new VBox(content);
        c.setPadding(new Insets(36));
        c.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: linear-gradient(to bottom right, " + ACCENT + "88, " + ACCENT2 + "44);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 20;"
        );
        DropShadow glow = new DropShadow(30, Color.web(GLOW));
        glow.setSpread(0.05);
        c.setEffect(glow);
        return c;
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
        drift1.setAutoReverse(true); drift1.setCycleCount(Animation.INDEFINITE);
        drift1.play();

        TranslateTransition drift2 = new TranslateTransition(Duration.seconds(11), orb2);
        drift2.setFromX(80); drift2.setToX(30);
        drift2.setFromY(80); drift2.setToY(120);
        drift2.setAutoReverse(true); drift2.setCycleCount(Animation.INDEFINITE);
        drift2.play();

        sp.getChildren().addAll(orb1, orb2, content);
        return sp;
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private TextField fancyTextField(String prompt, FontAwesomeIcon icon) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        styleFancyControl(f);
        addFocusGlow(f);
        return f;
    }

    private PasswordField fancyPassField(String prompt, FontAwesomeIcon icon) {
        PasswordField f = new PasswordField();
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

    // ── Buttons ───────────────────────────────────────────────────────────────
    private Button gradientIconBtn(FontAwesomeIcon icon, String text) {
        Button b = new Button();
        b.setGraphic(iconText(icon, text, "white", 14));
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

    private void updateBtnText(Button b, FontAwesomeIcon icon, String text) {
        b.setGraphic(iconText(icon, text, "white", 14));
    }

    private Button glassIconBtn(FontAwesomeIcon icon, String text) {
        Button b = new Button();
        b.setGraphic(iconText(icon, text, TEXT_SEC, 13));
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

    private Button dangerIconBtn(FontAwesomeIcon icon, String text) {
        Button b = new Button();
        b.setGraphic(iconText(icon, text, ERROR_C, 13));
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
        b.setOnMouseExited (e -> b.setStyle(b.getStyle().replace("rgba(251,113,133,0.15)", "rgba(124,111,253,0.12)")));
        return b;
    }

    // kept for compatibility with other menus
    private void addFancyMenuBtn(VBox parent, FontAwesomeIcon icon, String title, String sub, Runnable action) {
        Button b = fancyMenuBtn(icon, title, sub);
        b.setOnAction(e -> { animateButtonPress(b); action.run(); });
        parent.getChildren().add(b);
    }

    private Button fancyMenuBtn(FontAwesomeIcon icon, String title, String sub) {
        FontAwesomeIconView iconView = faIcon(icon, ACCENT2, 16);
        StackPane iconWrapper = new StackPane(iconView);
        iconWrapper.setMinWidth(28);   
        iconWrapper.setPrefWidth(28);
        Label titleLbl = label(title, 13, FontWeight.BOLD, TEXT_PRI);
        Label subLbl   = label(sub,   11, FontWeight.NORMAL, TEXT_SEC);
        VBox  text     = vbox(2, titleLbl, subLbl);
        FontAwesomeIconView arrow = faIcon(FontAwesomeIcon.ANGLE_RIGHT, TEXT_SEC, 16);
        HBox content = new HBox(14, iconWrapper, text, arrow);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);
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
        b.setOnMouseEntered(e -> {
            b.setStyle(b.getStyle().replace("rgba(255,255,255,0.03)", "rgba(124,111,253,0.10)"));
            arrow.setFill(Color.web(ACCENT));
        });
        b.setOnMouseExited(e -> {
            b.setStyle(b.getStyle().replace("rgba(124,111,253,0.10)", "rgba(255,255,255,0.03)"));
            arrow.setFill(Color.web(TEXT_SEC));
        });
        return b;
    }

    private Button adminMenuBtn(FontAwesomeIcon icon, String title, String sub) {
        Button b = fancyMenuBtn(icon, title, sub);
        b.setStyle(b.getStyle() + "-fx-border-color: " + ACCENT + "66;");
        return b;
    }

    private void animateButtonPress(Button b) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), b);
        st.setFromX(1.0); st.setToX(0.96);
        st.setFromY(1.0); st.setToY(0.96);
        st.setAutoReverse(true); st.setCycleCount(2);
        st.play();
    }

    private Label errorLabel() {
        Label l = new Label("");
        l.setTextFill(Color.web(ERROR_C));
        l.setFont(Font.font("System", FontWeight.NORMAL, 12));
        l.setGraphic(faIcon(FontAwesomeIcon.EXCLAMATION_TRIANGLE, ERROR_C, 12));
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

    private TextArea fancyReadonlyArea(String initial) {
        TextArea a = new TextArea(initial);
        a.setEditable(false);
        a.setWrapText(true);
        a.setPrefRowCount(14);
        a.setStyle(
            "-fx-control-inner-background: " + FIELD_BG + ";" +
            "-fx-text-fill: " + TEXT_PRI + ";" +
            "-fx-font-family: monospace;" +
            "-fx-font-size: 12;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );
        return a;
    }

    private void showSuccessDialog(String msg) {
        Stage dlg = dialogStage("Success");
        FontAwesomeIconView checkIcon = faIcon(FontAwesomeIcon.CHECK_CIRCLE, SUCCESS_C, 36);
        StackPane iconBox = new StackPane(checkIcon);
        iconBox.setAlignment(Pos.CENTER);
        Label lbl = label(msg, 14, FontWeight.NORMAL, SUCCESS_C);
        lbl.setWrapText(true);
        Button ok = gradientIconBtn(FontAwesomeIcon.CHECK, "OK");
        ok.setOnAction(e -> dlg.close());
        VBox content = vbox(16, iconBox, lbl, ok);
        content.setAlignment(Pos.CENTER);
        dlg.setScene(new Scene(animatedRoot(fancyCard(content)), 420, 260));
        FadeTransition ft = new FadeTransition(Duration.millis(300), dlg.getScene().getRoot());
        ft.setFromValue(0); ft.setToValue(1);
        dlg.show();
        ft.play();
    }

    private Stage dialogStage(String title) {
        Stage dlg = new Stage();
        dlg.setTitle(title);
        dlg.initOwner(stage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.setResizable(false);
        return dlg;
    }

    private String buildCSS() {
        return "";
    }
}