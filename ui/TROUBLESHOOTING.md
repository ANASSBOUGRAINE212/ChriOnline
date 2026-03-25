# Troubleshooting Guide

## Issue: Buttons Not Working

### Symptoms
- Clicking buttons does nothing
- No response when clicking "Login", "Add to Cart", etc.
- No errors in console

### Causes & Solutions

#### 1. FXML Controller Not Linked
**Check:** Open each FXML file and verify the `fx:controller` attribute

```xml
<!-- login.fxml should have: -->
<VBox ... fx:controller="application.controllers.LoginController">
```

**Fix:** Ensure all FXML files have the correct controller class name

#### 2. Method Not Accessible
**Check:** Controller methods must be:
- Annotated with `@FXML`
- Package-private or public (not private)

```java
// CORRECT:
@FXML
private void handleLogin() { ... }

// WRONG:
private void handleLogin() { ... }  // Missing @FXML
```

#### 3. FXML onAction Not Set
**Check:** Buttons in FXML must have `onAction` attribute

```xml
<!-- CORRECT: -->
<Button text="Login" onAction="#handleLogin"/>

<!-- WRONG: -->
<Button text="Login"/>  <!-- Missing onAction -->
```

#### 4. JavaFX Not Configured
**Check:** Eclipse Console shows: `Module javafx.controls not found`

**Fix:** Add VM arguments in Run Configuration:
```
--module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml
```

---

## Issue: Products Not Loading

### Symptoms
- Products page is empty
- "Loading products..." message stays
- No products displayed

### Diagnostic Steps

#### 1. Check Server Connection
**Look for in Console:**
```
🔌 Initializing ConnectionManager...
✅ Connected to server successfully!
```

**If you see:**
```
❌ Failed to connect to server: Connection refused
```

**Fix:**
- Start the server first: `java -cp "bin;src\mysql-connector-j-9.6.0.jar" server.serverApp`
- Verify server is on port 5000
- Check firewall settings

#### 2. Check Product Loading
**Look for in Console:**
```
📦 Fetching product list...
✅ Products loaded successfully
Products response: [product data]
```

**If you see:**
```
❌ Failed to load products: [error]
```

**Fix:**
- Check database has products
- Verify server can connect to database
- Check `db.properties` configuration

#### 3. Check Product Parsing
**Look for in Console:**
```
Processing line: 📦 [ID: 1] iPhone 14 - $999.99 (Stock: 10)
Parsed: ID=1, Name=iPhone 14, Price=999.99, Stock=10
```

**If parsing fails:**
- Server response format might have changed
- Check `productHandler.java` response format
- Verify product data in database

#### 4. Verify Database Has Products
Run in MySQL:
```sql
SELECT * FROM products;
```

If empty, run the product initializer or add products manually.

---

## Issue: Login Not Working

### Symptoms
- Login button does nothing
- Error message appears
- Can't proceed to dashboard

### Diagnostic Steps

#### 1. Check Login Attempt
**Look for in Console:**
```
🔐 Attempting login for: user@example.com
✅ Login successful!
🎫 Session token set
👤 User role: USER
```

#### 2. Check Credentials
- Verify user exists in database
- Check password is correct
- Ensure email format is valid

#### 3. Check Server Response
**If you see:**
```
❌ Login failed: Invalid credentials
```

**Fix:**
- Register a new account first
- Verify database connection
- Check `authHandler.java` logic

#### 4. Check Navigation
After successful login, should navigate to dashboard.

**If stuck on login screen:**
- Check `Main.showDashboard()` is called
- Verify `dashboard.fxml` exists
- Check for exceptions in console

---

## Issue: Cart Operations Fail

### Symptoms
- "Add to Cart" doesn't work
- Cart shows empty
- Remove from cart fails

### Diagnostic Steps

#### 1. Check Authentication
Cart operations require login. Verify:
- User is logged in
- Session token is set
- Token is being sent with requests

#### 2. Check Cart Creation
**Look for in Console:**
```
🛒 Cart handler processing: ADD_TO_CART
✅ Cart created/found
```

**If you see:**
```
❌ Error getting/creating cart: Table 'chrionline.carts' doesn't exist
```

**Fix:**
- Run database initializer
- Verify `carts` table exists (not `cart`)
- Check `databaseInitializer.java`

#### 3. Check Product ID
When adding to cart, product ID must be valid:
- Product must exist in database
- ID must be an integer
- Product must have stock

---

## Issue: Orders Not Showing

### Symptoms
- Orders page is empty
- "No orders yet" message
- Can't create orders

### Diagnostic Steps

#### 1. Check Order Creation
**Look for in Console:**
```
📦 Order handler processing: CREATE_ORDER
✅ Order created successfully!
```

#### 2. Verify Cart Has Items
Orders are created from cart items. Ensure:
- Cart is not empty
- Cart items are valid
- Products have sufficient stock

#### 3. Check Database
```sql
SELECT * FROM orders WHERE user_id = 'your-user-id';
SELECT * FROM order_items;
```

---

## General Debugging Tips

### 1. Enable All Console Output
Check Eclipse Console for:
- Connection messages (🔌, ✅, ❌)
- Request/response logs
- Stack traces
- Debug output

### 2. Clean and Rebuild
```
Project → Clean → Select All Projects → Clean
```

### 3. Refresh Projects
```
Right-click project → Refresh (F5)
```

### 4. Check Dependencies
Verify in Eclipse:
- ChriOnline-UI references ChriOnline project
- JavaFX JARs are in build path
- MySQL connector is accessible

### 5. Verify File Paths
Ensure FXML and CSS files are in correct locations:
```
ui/resources/fxml/*.fxml
ui/resources/css/styles.css
```

### 6. Test Server Independently
Before running UI, test server with console client:
```
java -cp "bin;src\mysql-connector-j-9.6.0.jar" client.clientApp
```

If console client works but UI doesn't, issue is in UI configuration.

---

## Quick Checklist

Before running the UI:

- [ ] Server is running on localhost:5000
- [ ] Database is initialized and accessible
- [ ] JavaFX VM arguments are set in Run Configuration
- [ ] ChriOnline project is referenced in UI project
- [ ] All projects are built without errors
- [ ] FXML files have correct controller attributes
- [ ] Controller methods have @FXML annotations

---

## Still Stuck?

1. Check Eclipse Problems view for compilation errors
2. Review all console output carefully
3. Test each component independently (server, database, client)
4. Verify all file paths and configurations
5. Compare with working console UI logic in `src/client/UI/`
