package client.UI;

import client.clientConnection;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;
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
import javafx.scene.layout.FlowPane;
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

    // ── Shared reference to dashboard main content scroll ────────────────────
    private ScrollPane mainContentScroll;

    // ── Ikonli icon helper ────────────────────────────────────────────────────
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
        FontIcon starIcon = faIcon("fas-star", ACCENT2, 26);
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
        Button guestBtn = glassIconBtn("fas-shopping-bag", "Browse as Guest");
        guestBtn.setOnAction(e -> showGuestProducts());
        Button exitBtn  = dangerIconBtn("fas-sign-out-alt", "Exit");
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
        TextField     emailFld = fancyTextField("Email address",   "fas-envelope");
        PasswordField passFld  = fancyPassField("Password",        "fas-lock");
        Label         errLbl   = errorLabel();
        Button        loginBtn = gradientIconBtn("fas-key", "Login");

        loginBtn.setOnAction(e -> {
            String email = emailFld.getText().trim();
            String pass  = passFld.getText().trim();
            loginBtn.setDisable(true);
            updateBtnText(loginBtn, "fas-spinner", "Authenticating…");
            animateButtonPress(loginBtn);
            new Thread(() -> {
                boolean ok = handleLogin(email, pass);
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    updateBtnText(loginBtn, "fas-key", "Login");
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
        TextField     userFld    = fancyTextField("Username",               "fas-user");
        TextField     emailFld   = fancyTextField("Email address",          "fas-envelope");
        PasswordField passFld    = fancyPassField("Password (min 6 chars)", "fas-lock");
        PasswordField confirmFld = fancyPassField("Confirm password",       "fas-check");
        TextField     addrFld    = fancyTextField("Address (optional)",     "fas-map-marker-alt");
        TextField     phoneFld   = fancyTextField("Phone (optional)",       "fas-phone");
        Label         errLbl     = errorLabel();
        Button        regBtn     = gradientIconBtn("fas-pencil-alt", "Create Account");

        regBtn.setOnAction(e -> {
            regBtn.setDisable(true);
            updateBtnText(regBtn, "fas-spinner", "Creating account…");
            String user  = userFld.getText().trim();
            String email = emailFld.getText().trim();
            String pass  = passFld.getText().trim();
            String conf  = confirmFld.getText().trim();
            String addr  = addrFld.getText().trim();
            String phone = phoneFld.getText().trim();

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                shakeAndError(errLbl, "Username, email, and password are required!");
                regBtn.setDisable(false);
                updateBtnText(regBtn, "fas-pencil-alt", "Create Account");
                return;
            }
            if (!pass.equals(conf)) {
                shakeAndError(errLbl, "Passwords don't match!");
                regBtn.setDisable(false);
                updateBtnText(regBtn, "fas-pencil-alt", "Create Account");
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
            updateBtnText(regBtn, "fas-pencil-alt", "Create Account");
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
        FontIcon sidebarStar = faIcon("fas-star", ACCENT2, 18);
        Label sidebarLogo = label("ChriOnline", 16, FontWeight.BOLD, TEXT_PRI);
        HBox sidebarBrand = new HBox(8, sidebarStar, sidebarLogo);
        sidebarBrand.setAlignment(Pos.CENTER_LEFT);
        sidebarBrand.setPadding(new Insets(0, 0, 16, 0));

        Rectangle sidebarLine = new Rectangle(40, 2);
        sidebarLine.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ACCENT)), new Stop(1, Color.web(ACCENT2))));

        HBox roleRow = new HBox(6,
                faIcon(isAdmin ? "fas-bolt" : "fas-hand-paper",
                        isAdmin ? ACCENT2 : SUCCESS_C, 11),
                label(isAdmin ? "Admin" : "Member", 11, FontWeight.BOLD,
                        isAdmin ? ACCENT2 : SUCCESS_C));
        roleRow.setAlignment(Pos.CENTER_LEFT);
        roleRow.setPadding(new Insets(8, 0, 20, 0));

        VBox sidebarMenu = vbox(4);

        addSidebarBtn(sidebarMenu, "fas-user",         "View My Info",      () -> showUserInfo());
        addSidebarBtn(sidebarMenu, "fas-chart-bar",    "My Profile",        () -> getProfile());
        addSidebarBtn(sidebarMenu, "fas-edit",         "Update Profile",    () -> updateProfile());
        addSidebarBtn(sidebarMenu, "fas-lock",         "Change Password",   () -> changePassword());
        addSidebarBtn(sidebarMenu, "fas-shopping-bag", "Product Catalog",   () -> showProductList());

        if (isAdmin) {
            Button adminBtn = sidebarAdminBtn("fas-wrench", "Product Mgmt");
            adminBtn.setOnAction(e -> new productMenu().show(connection, null, true));
            sidebarMenu.getChildren().add(adminBtn);
        }

        addSidebarBtn(sidebarMenu, "fas-shopping-cart", "Shopping Cart",   () -> new cartMenu(connection, null).show());
        addSidebarBtn(sidebarMenu, "fas-cube",           "My Orders",      () -> new orderMenu(connection, null).show());
        addSidebarBtn(sidebarMenu, "fas-credit-card",    "Payments",       () -> new paymentMenu(connection, null).show());

        VBox.setVgrow(sidebarMenu, Priority.ALWAYS);

        Button logoutBtn = sidebarDangerBtn("fas-sign-out-alt", "Logout");
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
        // ── Assign the shared scroll pane so showProductList() can update it ──
        mainContentScroll = new ScrollPane(buildDashboardContent(isAdmin));
        mainContentScroll.setFitToWidth(true);
        mainContentScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        HBox dashLayout = new HBox(sidebar, mainContentScroll);
        HBox.setHgrow(mainContentScroll, Priority.ALWAYS);
        dashLayout.setFillHeight(true);

        FadeTransition ft = new FadeTransition(Duration.millis(400), dashLayout);
        ft.setFromValue(0); ft.setToValue(1);

        stage.setMinWidth(820);
        stage.setMinHeight(560);
        stage.setScene(new Scene(animatedRoot(dashLayout), 920, 640));
        ft.play();
    }

    // Quick-action card for dashboard grid
    private Button quickActionCard(String iconCode, String title, String sub) {
        FontIcon iv = faIcon(iconCode, ACCENT2, 22);
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
            iv.setIconColor(Color.web(ACCENT));
        });
        b.setOnMouseExited(e -> {
            b.setStyle(b.getStyle().replace("rgba(124,111,253,0.12)", "rgba(255,255,255,0.03)"));
            iv.setIconColor(Color.web(ACCENT2));
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
    private void addSidebarBtn(VBox parent, String iconCode, String title, Runnable action) {
        Button b = sidebarBtn(iconCode, title);
        b.setOnAction(e -> { animateButtonPress(b); action.run(); });
        parent.getChildren().add(b);
    }

    private Button sidebarBtn(String iconCode, String title) {
        FontIcon iv = faIcon(iconCode, TEXT_SEC, 13);
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
            iv.setIconColor(Color.web(ACCENT2));
            lbl.setTextFill(Color.web(TEXT_PRI));
        });
        b.setOnMouseExited(e -> {
            b.setStyle(b.getStyle().replace("rgba(124,111,253,0.12);-fx-border", "transparent;-fx-border"));
            iv.setIconColor(Color.web(TEXT_SEC));
            lbl.setTextFill(Color.web(TEXT_SEC));
        });
        return b;
    }

    private Button sidebarAdminBtn(String iconCode, String title) {
        Button b = sidebarBtn(iconCode, title);
        b.setStyle(b.getStyle() + "-fx-border-color: " + ACCENT + "44; -fx-border-radius: 8;");
        return b;
    }

    private Button sidebarDangerBtn(String iconCode, String title) {
        FontIcon iv = faIcon(iconCode, ERROR_C, 13);
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
        Label title = glowLabel("USER INFORMATION", 16, FontWeight.BOLD);
        Label loadingLbl = label("Loading…", 13, FontWeight.NORMAL, TEXT_SEC);

        VBox infoBox = vbox(10);

        Button ok = gradientIconBtn("fas-check", "Close");
        ok.setOnAction(e -> dlg.close());

        VBox contentBox = vbox(20, title, loadingLbl, ok);
        contentBox.setPadding(new Insets(28));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        new Thread(() -> {
            response res = connection.getUserInfo();
            Platform.runLater(() -> {
                contentBox.getChildren().remove(loadingLbl);
                contentBox.getChildren().add(1, infoBox);

                if (!res.isSuccess()) {
                    infoBox.getChildren().add(label("Unable to load user info: " + res.getMessage(), 13, FontWeight.NORMAL, ERROR_C));
                    return;
                }

                String[][] fieldDefs = {
                    {"fas-user",        "Username",  "username"},
                    {"fas-envelope",    "Email",     "email"},
                    {"fas-map-marker-alt","Address", "address"},
                    {"fas-phone",       "Phone",     "phone"},
                    {"fas-shield-alt",  "Role",      "role"},
                };

                String raw = res.getMessage().replace("\\n", "\n");

                for (String[] def : fieldDefs) {
                    String iconCode = def[0];
                    String label    = def[1];
                    String key      = def[2];

                    // Find value: look for "Key: value" pattern (case-insensitive)
                    String value = "—";
                    for (String line : raw.split("\n")) {
                        String stripped = line.replaceAll("[^\\x20-\\x7E]", "").trim();
                        if (stripped.toLowerCase().contains(label.toLowerCase() + ":")) {
                            int idx = stripped.indexOf(":");
                            if (idx >= 0) { value = stripped.substring(idx + 1).trim(); break; }
                        }
                    }

                    boolean isRole = key.equals("role");
                    String valueColor = isRole
                        ? (value.equalsIgnoreCase("ADMIN") ? ACCENT2 : SUCCESS_C)
                        : TEXT_PRI;
                    String iconColor = isRole
                        ? (value.equalsIgnoreCase("ADMIN") ? ACCENT2 : SUCCESS_C)
                        : ACCENT2;

                    FontIcon iv = faIcon(iconCode, iconColor, 14);
                    StackPane iconWrap = new StackPane(iv);
                    iconWrap.setMinSize(32, 32);
                    iconWrap.setMaxSize(32, 32);
                    iconWrap.setStyle(
                        "-fx-background-color: rgba(124,111,253,0.12);" +
                        "-fx-background-radius: 8;"
                    );

                    Label keyLbl = label(label, 11, FontWeight.BOLD, TEXT_SEC);
                    Label valLbl = label(value, 14, FontWeight.BOLD, valueColor);

                    VBox textCol = vbox(2, keyLbl, valLbl);
                    HBox.setHgrow(textCol, Priority.ALWAYS);

                    HBox row = new HBox(12, iconWrap, textCol);
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

                    FadeTransition ft = new FadeTransition(Duration.millis(250 + infoBox.getChildren().size() * 60L), row);
                    ft.setFromValue(0); ft.setToValue(1);
                    infoBox.getChildren().add(row);
                    ft.play();
                }
            });
        }).start();

        dlg.setScene(new Scene(animatedRoot(contentBox), 480, 460));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  getProfile
    // ══════════════════════════════════════════════════════════════════════════
    private void getProfile() {
        Stage dlg = dialogStage("Your Profile");
        Label title = glowLabel("YOUR PROFILE", 16, FontWeight.BOLD);
        Label loadingLbl = label("Loading…", 13, FontWeight.NORMAL, TEXT_SEC);

        VBox infoBox = vbox(10);

        Button ok = gradientIconBtn("fas-check", "Close");
        ok.setOnAction(e -> dlg.close());

        VBox contentBox = vbox(20, title, loadingLbl, ok);
        contentBox.setPadding(new Insets(28));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        new Thread(() -> {
            response res = connection.getProfile();
            Platform.runLater(() -> {
                contentBox.getChildren().remove(loadingLbl);
                contentBox.getChildren().add(1, infoBox);

                if (!res.isSuccess()) {
                    infoBox.getChildren().add(label("Unable to load profile: " + res.getMessage(), 13, FontWeight.NORMAL, ERROR_C));
                    return;
                }

                String[][] fieldDefs = {
                    {"fas-user",          "Username",  "username"},
                    {"fas-envelope",      "Email",     "email"},
                    {"fas-map-marker-alt","Address",   "address"},
                    {"fas-phone",         "Phone",     "phone"},
                    {"fas-shield-alt",    "Role",      "role"},
                };

                String raw = res.getMessage().replace("\\n", "\n");

                for (String[] def : fieldDefs) {
                    String iconCode = def[0];
                    String labelTxt = def[1];
                    String key      = def[2];

                    String value = "—";
                    for (String line : raw.split("\n")) {
                        String stripped = line.replaceAll("[^\\x20-\\x7E]", "").trim();
                        if (stripped.toLowerCase().contains(labelTxt.toLowerCase() + ":")) {
                            int idx = stripped.indexOf(":");
                            if (idx >= 0) { value = stripped.substring(idx + 1).trim(); break; }
                        }
                    }

                    boolean isRole = key.equals("role");
                    String valueColor = isRole ? (value.equalsIgnoreCase("ADMIN") ? ACCENT2 : SUCCESS_C) : TEXT_PRI;
                    String iconColor  = isRole ? (value.equalsIgnoreCase("ADMIN") ? ACCENT2 : SUCCESS_C) : ACCENT2;

                    FontIcon iv = faIcon(iconCode, iconColor, 14);
                    StackPane iconWrap = new StackPane(iv);
                    iconWrap.setMinSize(32, 32);
                    iconWrap.setMaxSize(32, 32);
                    iconWrap.setStyle("-fx-background-color: rgba(124,111,253,0.12); -fx-background-radius: 8;");

                    Label keyLbl = label(labelTxt, 11, FontWeight.BOLD, TEXT_SEC);
                    Label valLbl = label(value,    14, FontWeight.BOLD, valueColor);

                    VBox textCol = vbox(2, keyLbl, valLbl);
                    HBox.setHgrow(textCol, Priority.ALWAYS);

                    HBox row = new HBox(12, iconWrap, textCol);
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

                    FadeTransition ft = new FadeTransition(Duration.millis(250 + infoBox.getChildren().size() * 60L), row);
                    ft.setFromValue(0); ft.setToValue(1);
                    infoBox.getChildren().add(row);
                    ft.play();
                }
            });
        }).start();

        dlg.setScene(new Scene(animatedRoot(contentBox), 480, 460));
        dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  updateProfile
    // ══════════════════════════════════════════════════════════════════════════
    private void updateProfile() {
        Stage dlg = dialogStage("Update Profile");
        TextField addrFld  = fancyTextField("New address",      "fas-map-marker-alt");
        TextField phoneFld = fancyTextField("New phone number", "fas-phone");
        Label     errLbl   = errorLabel();
        Button    saveBtn  = gradientIconBtn("fas-save", "Save Changes");

        saveBtn.setOnAction(e -> {
            String addr  = addrFld.getText().trim();
            String phone = phoneFld.getText().trim();
            if (addr.isEmpty() && phone.isEmpty()) {
                shakeAndError(errLbl, "Both fields are empty — nothing to update."); return;
            }
            saveBtn.setDisable(true);
            updateBtnText(saveBtn, "fas-spinner", "Saving…");
            new Thread(() -> {
                response res = connection.updateProfile(addr, phone);
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    updateBtnText(saveBtn, "fas-save", "Save Changes");
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
        PasswordField oldFld  = fancyPassField("Current password",     "fas-key");
        PasswordField newFld  = fancyPassField("New password",         "fas-lock");
        PasswordField confFld = fancyPassField("Confirm new password",  "fas-check");
        Label         errLbl  = errorLabel();
        Button        saveBtn = gradientIconBtn("fas-lock", "Change Password");

        saveBtn.setOnAction(e -> {
            String oldPass = oldFld.getText();
            String newPass = newFld.getText();
            String confirm = confFld.getText();
            if (oldPass.isEmpty() || newPass.isEmpty()) { shakeAndError(errLbl, "Password cannot be empty!"); return; }
            if (newPass.length() < 6) { shakeAndError(errLbl, "New password must be at least 6 characters!"); return; }
            if (!newPass.equals(confirm)) { shakeAndError(errLbl, "Passwords don't match!"); return; }
            saveBtn.setDisable(true);
            updateBtnText(saveBtn, "fas-sync", "Changing…");
            new Thread(() -> {
                response res = connection.changePassword(oldPass, newPass);
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    updateBtnText(saveBtn, "fas-lock", "Change Password");
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
                faIcon("fas-lock", ACCENT2, 13),
                label("Login to unlock all features", 13, FontWeight.NORMAL, ACCENT2));
        noticeRow.setAlignment(Pos.CENTER_LEFT);

        TextArea area = fancyReadonlyArea("Loading products…");

        Button backBtn = glassIconBtn("fas-arrow-left", "Back to Login");
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
    //  buildDashboardContent  —  default dashboard home content
    // ══════════════════════════════════════════════════════════════════════════
    private VBox buildDashboardContent(boolean isAdmin) {
        Label dashTitle = glowLabel("Dashboard", 26, FontWeight.BOLD);
        Label dashSub   = label("Manage your account and orders", 13, FontWeight.NORMAL, TEXT_SEC);
        VBox dashHeader = vbox(4, dashTitle, dashSub);
        dashHeader.setPadding(new Insets(0, 0, 24, 0));

        HBox welcomeCard = new HBox(14,
                faIcon(isAdmin ? "fas-bolt" : "fas-hand-paper",
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

        GridPane quickGrid = new GridPane();
        quickGrid.setHgap(12); quickGrid.setVgap(12);
        quickGrid.getColumnConstraints().addAll(colConstraint(), colConstraint());

        String[][] quickItems = {
            {"fas-shopping-bag",  "Catalog",  "Browse products"},
            {"fas-shopping-cart", "Cart",     "View your basket"},
            {"fas-cube",          "Orders",   "Track your orders"},
            {"fas-credit-card",   "Payments", "Billing history"},
        };
        Runnable[] quickActions = {
            () -> showProductList(),
            () -> new cartMenu(connection, null).show(),
            () -> new orderMenu(connection, null).show(),
            () -> new paymentMenu(connection, null).show(),
        };
        String[] quickIcons = {
            "fas-shopping-bag", "fas-shopping-cart",
            "fas-cube", "fas-credit-card",
        };

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Button card = quickActionCard(quickIcons[i], quickItems[i][1], quickItems[i][2]);
            card.setOnAction(e -> quickActions[idx].run());
            quickGrid.add(card, i % 2, i / 2);
        }

        VBox content = vbox(20, dashHeader, welcomeCard, quickGrid);
        content.setPadding(new Insets(36, 36, 36, 32));
        return content;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  showProductList  —  renders inline in the dashboard main content area
    // ══════════════════════════════════════════════════════════════════════════
    private void showProductList() {
        // Guard: if the dashboard scroll pane isn't set yet, do nothing
        if (mainContentScroll == null) return;

        Label title      = glowLabel("Product Catalog", 20, FontWeight.BOLD);
        Label dashSub    = label("Browse all available products", 13, FontWeight.NORMAL, TEXT_SEC);

        Button backBtn = glassIconBtn("fas-arrow-left", "Back to Dashboard");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> {
            boolean isAdmin = connection.isAdmin();
            VBox dashContent = buildDashboardContent(isAdmin);
            FadeTransition backFt = new FadeTransition(Duration.millis(250), dashContent);
            backFt.setFromValue(0); backFt.setToValue(1);
            mainContentScroll.setContent(dashContent);
            mainContentScroll.setVvalue(0);
            backFt.play();
        });

        VBox  pageHeader = vbox(8, backBtn, title, dashSub);
        pageHeader.setPadding(new Insets(0, 0, 16, 0));

        Label loadingLbl = label("Loading products…", 13, FontWeight.NORMAL, TEXT_SEC);

        FlowPane cardsPane = new FlowPane();
        cardsPane.setHgap(14);
        cardsPane.setVgap(14);
        cardsPane.setPadding(new Insets(4, 0, 4, 0));
        cardsPane.setPrefWrapLength(600);

        VBox contentBox = vbox(16, pageHeader, loadingLbl, cardsPane);
        contentBox.setPadding(new Insets(36, 36, 36, 32));

        // Swap main content inline — no dialog
        mainContentScroll.setContent(contentBox);
        mainContentScroll.setVvalue(0);

        FadeTransition ft = new FadeTransition(Duration.millis(300), contentBox);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();

        new Thread(() -> {
            response res = connection.listProducts();
            Platform.runLater(() -> {
                contentBox.getChildren().remove(loadingLbl);

                if (!res.isSuccess()) {
                    cardsPane.getChildren().add(label("Unable to load products: " + res.getMessage(), 13, FontWeight.NORMAL, ERROR_C));
                    return;
                }

                String raw = res.getMessage().replace("\\n", "\n");
                String[] lines = raw.split("\n");

                for (String line : lines) {
                    // Skip header/divider lines
                    if (!line.contains("|")) continue;
                    String stripped = line.replaceAll("[^\\x20-\\x7E]", " ").replaceAll("\\s+", " ").trim();
                    String[] parts = stripped.split("\\|");
                    if (parts.length < 4) continue;

                    String fId    = parts[0].replaceAll("[^0-9]", "").trim();
                    if (fId.isEmpty()) continue;
                    String fName  = parts[1].trim();
                    String fPrice = parts[2].replaceAll("[^0-9.,]", "").trim();
                    String fCat   = parts[3].trim();
                    String fStock = parts.length > 4 ? parts[4].replaceAll("[^0-9]", "").trim() : "?";

                    String[] catKeys  = {"smartphone","laptop","audio","keyboard","mouse","accessori"};
                    String[] catIcons = {"fas-mobile-alt","fas-laptop","fas-headphones","fas-keyboard","fas-mouse-pointer","fas-box"};
                    String iconCode = "fas-box";
                    for (int k = 0; k < catKeys.length; k++) {
                        if (fCat.toLowerCase().contains(catKeys[k]) || fName.toLowerCase().contains(catKeys[k])) {
                            iconCode = catIcons[k]; break;
                        }
                    }

                    FontIcon icon = faIcon(iconCode, ACCENT2, 28);
                    StackPane iconBg = new StackPane(icon);
                    iconBg.setMinSize(52, 52);
                    iconBg.setMaxSize(52, 52);
                    iconBg.setStyle("-fx-background-color: rgba(124,111,253,0.15); -fx-background-radius: 14;");

                    Label nameLbl  = label(fName,        13, FontWeight.BOLD,   TEXT_PRI);
                    Label catLbl   = label(fCat,         11, FontWeight.NORMAL, TEXT_SEC);
                    Label priceLbl = label("$" + fPrice, 16, FontWeight.BOLD,   ACCENT2);
                    priceLbl.setEffect(new DropShadow(8, Color.web(GLOW)));

                    HBox stockRow = new HBox(5, faIcon("fas-cubes", SUCCESS_C, 10),
                            label("Stock: " + fStock, 11, FontWeight.NORMAL, SUCCESS_C));
                    stockRow.setAlignment(Pos.CENTER_LEFT);

                    HBox idRow = new HBox(4, faIcon("fas-tag", TEXT_SEC, 10),
                            label("ID " + fId, 10, FontWeight.NORMAL, TEXT_SEC));
                    idRow.setAlignment(Pos.CENTER_LEFT);

                    VBox info = vbox(4, nameLbl, catLbl, priceLbl, stockRow, idRow);
                    HBox.setHgrow(info, Priority.ALWAYS);

                    HBox cardContent = new HBox(14, iconBg, info);
                    cardContent.setAlignment(Pos.CENTER_LEFT);
                    cardContent.setPadding(new Insets(16));

                    VBox card = new VBox(cardContent);
                    card.setPrefWidth(220);
                    card.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.04);" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;" +
                        "-fx-cursor: hand;"
                    );
                    card.setOnMouseEntered(ev -> {
                        card.setStyle(card.getStyle().replace("rgba(255,255,255,0.04)", "rgba(124,111,253,0.13)"));
                        card.setEffect(new DropShadow(16, Color.web(GLOW)));
                    });
                    card.setOnMouseExited(ev -> {
                        card.setStyle(card.getStyle().replace("rgba(124,111,253,0.13)", "rgba(255,255,255,0.04)"));
                        card.setEffect(null);
                    });

                    final int productId = Integer.parseInt(fId);
                    card.setOnMouseClicked(ev -> showProductDetailDialog(productId, fName));

                    FadeTransition cardFt = new FadeTransition(Duration.millis(300 + cardsPane.getChildren().size() * 60L), card);
                    cardFt.setFromValue(0); cardFt.setToValue(1);
                    cardsPane.getChildren().add(card);
                    cardFt.play();
                }
                if (cardsPane.getChildren().isEmpty()) {
                    cardsPane.getChildren().add(label("No products found.", 13, FontWeight.NORMAL, TEXT_SEC));
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  showProductDetailDialog  —  popup with full product info on card click
    // ══════════════════════════════════════════════════════════════════════════
    private void showProductDetailDialog(int productId, String productName) {
        Stage dlg = dialogStage("Product Details");

        // ── Header strip ──────────────────────────────────────────────────────
        FontIcon headerIcon = faIcon("fas-box-open", ACCENT2, 20);
        Label title = glowLabel(productName, 16, FontWeight.BOLD);
        HBox titleRow = new HBox(10, headerIcon, title);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Rectangle accentLine = new Rectangle(50, 2);
        accentLine.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ACCENT)), new Stop(1, Color.web(ACCENT2))));

        Label loadingLbl = label("Loading details…", 12, FontWeight.NORMAL, TEXT_SEC);

        // ── 2-column grid for fields ──────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        Button closeBtn = gradientIconBtn("fas-check", "Close");
        closeBtn.setOnAction(e -> dlg.close());

        VBox contentBox = vbox(12, titleRow, accentLine, loadingLbl, closeBtn);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        new Thread(() -> {
            response res = connection.getProduct(productId);
            Platform.runLater(() -> {
                contentBox.getChildren().remove(loadingLbl);
                contentBox.getChildren().add(2, grid);

                if (!res.isSuccess()) {
                    grid.add(label("Unable to load details: " + res.getMessage(), 12, FontWeight.NORMAL, ERROR_C), 0, 0, 2, 1);
                    return;
                }

                String raw = res.getMessage().replace("\\n", "\n");

                // fields: {icon, label, search-key}
                // Description and Name span full width (col-span 2); others go in pairs
                String[][] fields = {
                    {"fas-hashtag",     "ID",          "id"},
                    {"fas-dollar-sign", "Price",       "price"},
                    {"fas-cubes",       "Stock",       "stock"},
                    {"fas-tag",         "Category",    "category"},
                    {"fas-user-shield", "Created By",  "created by"},
                    {"fas-box",         "Name",        "name"},          // full-width
                    {"fas-align-left",  "Description", "description"},  // full-width
                };

                // Resolve all values first
                String[] values = new String[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    values[i] = "—";
                    for (String line : raw.split("\n")) {
                        String s = line.replaceAll("[^\\x20-\\x7E]", "").trim();
                        if (s.toLowerCase().contains(fields[i][2] + ":")) {
                            int idx = s.indexOf(":");
                            if (idx >= 0) { values[i] = s.substring(idx + 1).trim(); break; }
                        }
                    }
                }

                // First 5 fields → 2-column grid (rows 0-2)
                int gridRow = 0, gridCol = 0;
                for (int i = 0; i < 5; i++) {
                    VBox cell = miniFieldCell(fields[i][0], fields[i][1], values[i]);
                    FadeTransition ft = new FadeTransition(Duration.millis(180 + i * 40L), cell);
                    ft.setFromValue(0); ft.setToValue(1); ft.play();
                    grid.add(cell, gridCol, gridRow);
                    gridCol++;
                    if (gridCol == 2) { gridCol = 0; gridRow++; }
                }
                // If the last row was only partially filled, move to the next row
                if (gridCol != 0) gridRow++;

                // Name + Description → full-width rows
                for (int i = 5; i < fields.length; i++) {
                    VBox cell = miniFieldCell(fields[i][0], fields[i][1], values[i]);
                    cell.setMaxWidth(Double.MAX_VALUE);
                    FadeTransition ft = new FadeTransition(Duration.millis(180 + i * 40L), cell);
                    ft.setFromValue(0); ft.setToValue(1); ft.play();
                    grid.add(cell, 0, gridRow, 2, 1);
                    gridRow++;
                }
            });
        }).start();

        dlg.setScene(new Scene(animatedRoot(contentBox), 480, 520));
        dlg.show();
    }

    // compact cell used inside the 2-column product detail grid
    private VBox miniFieldCell(String iconCode, String key, String value) {
        FontIcon iv = faIcon(iconCode, ACCENT2, 12);
        Label keyLbl = label(key,   10, FontWeight.BOLD,  TEXT_SEC);
        Label valLbl = label(value, 12, FontWeight.BOLD,  TEXT_PRI);
        valLbl.setWrapText(true);

        HBox keyRow = new HBox(5, iv, keyLbl);
        keyRow.setAlignment(Pos.CENTER_LEFT);

        VBox cell = vbox(3, keyRow, valLbl);
        cell.setPadding(new Insets(8, 10, 8, 10));
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        cell.setOnMouseEntered(e -> cell.setStyle(cell.getStyle().replace("rgba(255,255,255,0.03)", "rgba(124,111,253,0.10)")));
        cell.setOnMouseExited (e -> cell.setStyle(cell.getStyle().replace("rgba(124,111,253,0.10)", "rgba(255,255,255,0.03)")));
        return cell;
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
    private TextField fancyTextField(String prompt, String iconCode) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        styleFancyControl(f);
        addFocusGlow(f);
        return f;
    }

    private PasswordField fancyPassField(String prompt, String iconCode) {
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

    private void updateBtnText(Button b, String iconCode, String text) {
        b.setGraphic(iconText(iconCode, text, "white", 14));
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
    private void addFancyMenuBtn(VBox parent, String iconCode, String title, String sub, Runnable action) {
        Button b = fancyMenuBtn(iconCode, title, sub);
        b.setOnAction(e -> { animateButtonPress(b); action.run(); });
        parent.getChildren().add(b);
    }

    private Button fancyMenuBtn(String iconCode, String title, String sub) {
        FontIcon iconView = faIcon(iconCode, ACCENT2, 16);
        StackPane iconWrapper = new StackPane(iconView);
        iconWrapper.setMinWidth(28);
        iconWrapper.setPrefWidth(28);
        Label titleLbl = label(title, 13, FontWeight.BOLD, TEXT_PRI);
        Label subLbl   = label(sub,   11, FontWeight.NORMAL, TEXT_SEC);
        VBox  text     = vbox(2, titleLbl, subLbl);
        FontIcon arrow = faIcon("fas-angle-right", TEXT_SEC, 16);
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
            arrow.setIconColor(Color.web(ACCENT));
        });
        b.setOnMouseExited(e -> {
            b.setStyle(b.getStyle().replace("rgba(124,111,253,0.10)", "rgba(255,255,255,0.03)"));
            arrow.setIconColor(Color.web(TEXT_SEC));
        });
        return b;
    }

    private Button adminMenuBtn(String iconCode, String title, String sub) {
        Button b = fancyMenuBtn(iconCode, title, sub);
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
        FontIcon checkIcon = faIcon("fas-check-circle", SUCCESS_C, 36);
        StackPane iconBox = new StackPane(checkIcon);
        iconBox.setAlignment(Pos.CENTER);
        Label lbl = label(msg, 14, FontWeight.NORMAL, SUCCESS_C);
        lbl.setWrapText(true);
        Button ok = gradientIconBtn("fas-check", "OK");
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