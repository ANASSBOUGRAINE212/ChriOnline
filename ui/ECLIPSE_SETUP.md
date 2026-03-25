# Eclipse Setup Guide for ChriOnline JavaFX UI

## Problem: Buttons Not Working / Products Not Loading

This is typically caused by:
1. JavaFX modules not properly configured
2. VM arguments not set in Eclipse run configuration
3. Server not running

## Solution: Configure Eclipse Run Configuration

### Step 1: Open Run Configurations

1. Right-click on `Main.java` in Eclipse
2. Select **Run As** → **Run Configurations...**
3. Select **Java Application** → **Main** (or create new if doesn't exist)

### Step 2: Set VM Arguments

In the **Arguments** tab, add these VM arguments:

```
--module-path "C:\Users\dell\Downloads\openjfx-21.0.10_windows-x64_bin-sdk\javafx-sdk-21.0.10\lib" --add-modules javafx.controls,javafx.fxml
```

**Important:** Adjust the path to match your JavaFX SDK location!

### Step 3: Verify Classpath

In the **Classpath** tab, ensure:
- **User Entries** includes:
  - ChriOnline-UI project
  - ChriOnline project (referenced)
  - JavaFX JARs (should be there from .classpath)

### Step 4: Apply and Run

1. Click **Apply**
2. Click **Run**

## Verify Server is Running

Before starting the UI, make sure:

1. The ChriOnline server is running
2. Server is listening on `localhost:5000`
3. Database is initialized and accessible

To start the server:
```
cd C:\Users\dell\eclipse-workspace\ChriOnline
java -cp "bin;src\mysql-connector-j-9.6.0.jar" server.serverApp
```

## Testing the Connection

When you run the UI:

1. **Login Screen** should appear
2. Check Eclipse Console for connection messages:
   - `🔌 Connecting to localhost:5000`
   - `✅ Connected successfully!`

3. If you see connection errors:
   - Verify server is running
   - Check firewall settings
   - Verify port 5000 is not blocked

## Debugging

### Enable Debug Output

The controllers have debug `System.out.println()` statements. Check Eclipse Console for:

```
Products response: [product list]
Processing line: [each product line]
Parsed: ID=1, Name=iPhone 14, Price=999.99, Stock=10
```

### Common Issues

**Issue:** `Module javafx.controls not found`
**Solution:** Add VM arguments (see Step 2)

**Issue:** `Connection refused`
**Solution:** Start the server first

**Issue:** `Cannot find symbol: class response`
**Solution:** Ensure ChriOnline project is in build path

**Issue:** Buttons don't respond
**Solution:** 
- Check FXML files have correct `onAction` attributes
- Verify controller methods are not private (should be package-private or public with @FXML)
- Check Eclipse Console for exceptions

### Verify FXML Controllers

Each FXML file should have the correct controller:

- `login.fxml` → `application.controllers.LoginController`
- `register.fxml` → `application.controllers.RegisterController`
- `dashboard.fxml` → `application.controllers.DashboardController`
- `products.fxml` → `application.controllers.ProductController`
- `cart.fxml` → `application.controllers.CartController`
- `orders.fxml` → `application.controllers.OrderController`
- `payment.fxml` → `application.controllers.PaymentController`

## Alternative: Run from Command Line

If Eclipse configuration is problematic, use the provided `run.bat`:

```cmd
cd ui
run.bat
```

## Project Structure Verification

Ensure your workspace looks like this:

```
C:\Users\dell\eclipse-workspace\
├── ChriOnline\              (Main project)
│   ├── src\
│   │   ├── client\
│   │   ├── server\
│   │   ├── model\
│   │   ├── protocol\
│   │   └── database\
│   └── bin\
│
└── ChriOnline\ui\           (UI project folder)
    ├── src\
    │   └── application\
    │       ├── Main.java
    │       ├── controllers\
    │       └── utils\
    ├── resources\
    │   ├── fxml\
    │   └── css\
    └── bin\
```

## Eclipse Project References

The UI project (ChriOnline-UI in Eclipse) should reference:
- ChriOnline project (for client, protocol, model classes)
- JavaFX SDK libraries

Check in Eclipse:
1. Right-click ChriOnline-UI project
2. Properties → Java Build Path → Projects tab
3. Ensure "ChriOnline" is checked

## Still Having Issues?

1. Clean and rebuild both projects:
   - Project → Clean → Select both projects
   
2. Refresh projects:
   - Right-click each project → Refresh (F5)

3. Check for compilation errors:
   - Problems view should show no errors
   - If there are errors in controllers, JavaFX might not be configured

4. Verify JavaFX installation:
   - The path in .classpath should exist
   - All JavaFX JARs should be present

5. Check Console output:
   - Look for stack traces
   - Connection messages
   - Debug output from controllers
