# Client vs Admin - Feature Comparison

## 🎯 Quick Overview

| Feature | Client JAR | Admin JAR |
|---------|------------|-----------|
| **Browse Products** | ✅ Yes | ✅ Yes |
| **Shopping Cart** | ✅ Yes | ✅ Yes |
| **Place Orders** | ✅ Yes | ✅ Yes |
| **View Payments** | ✅ Yes | ✅ Yes |
| **Update Profile** | ✅ Yes | ✅ Yes |
| **Change Password** | ✅ Yes | ✅ Yes |
| **Product Management** | ❌ No | ✅ Yes |
| **Add Products** | ❌ No | ✅ Yes |
| **Delete Products** | ❌ No | ✅ Yes |
| **Update Products** | ❌ No | ✅ Yes |
| **Security Tests** | ❌ No | ✅ Yes |

## 📱 User Interface Differences

### Client JAR - Sidebar Menu
```
┌─────────────────────┐
│ ChriOnline          │
│ ─────               │
│ Member              │
│                     │
│ 👤 My Profile       │
│ ✏️  Update Profile   │
│ 🔒 Change Password  │
│ 🛍️  Product Catalog  │
│ 🛒 Shopping Cart    │
│ 📦 My Orders        │
│ 💳 Payments         │
│                     │
│ 🚪 Logout           │
└─────────────────────┘
```

### Admin JAR - Sidebar Menu
```
┌─────────────────────┐
│ ChriOnline          │
│ ─────               │
│ ⚡ Admin             │
│                     │
│ 👤 My Profile       │
│ ✏️  Update Profile   │
│ 🔒 Change Password  │
│ 🛍️  Product Catalog  │
│ 🔧 Product Mgmt     │ ← ADMIN ONLY
│ 🛡️  Security Tests   │ ← ADMIN ONLY
│ 🛒 Shopping Cart    │
│ 📦 My Orders        │
│ 💳 Payments         │
│                     │
│ 🚪 Logout           │
└─────────────────────┘
```

## 🔐 How It Works

### Client JAR (`ChriOnline-Client.jar`)
- Uses `client.Launcher` as main class
- Sets system property: `app.mode = "client"`
- Console shows: `👤 Starting ChriOnline in CLIENT mode...`
- Dashboard shows: "Welcome, User!"
- Admin features are hidden

### Admin JAR (`ChriOnline-Admin.jar`)
- Uses `client.AdminLauncher` as main class
- Sets system property: `app.mode = "admin"`
- Console shows: `🔑 Starting ChriOnline in ADMIN mode...`
- Dashboard shows: "Admin Access Granted"
- All features visible including Product Management and Security Tests

## 🚀 Building and Running

### Build Both JARs
```bash
# Build Client JAR
mvn clean package -Pclient

# Build Admin JAR
mvn package -Padmin
```

### Run Client
```bash
java -jar target\ChriOnline-Client.jar
```
**Output:**
```
👤 Starting ChriOnline in CLIENT mode...
Starting ChriOnline Client...
🔐 App Mode: CLIENT | Admin Access: false
```

### Run Admin
```bash
java -jar target\ChriOnline-Admin.jar
```
**Output:**
```
🔑 Starting ChriOnline in ADMIN mode...
Starting ChriOnline Client...
🔐 App Mode: ADMIN | Admin Access: true
```

## 📊 File Sizes

Both JARs are similar in size (~50-60 MB) because they contain the same JavaFX libraries. The only difference is the main class and the features shown in the UI.

## 🎓 For Developers

The mode detection happens in `authMenu.java`:
```java
String appMode = System.getProperty("app.mode", "client");
boolean isAdmin = "admin".equalsIgnoreCase(appMode);
```

This flag controls:
- Which sidebar buttons are shown
- The welcome message on the dashboard
- The role badge (Member vs Admin)
- Access to Product Management
- Access to Security Tests

## 📝 Distribution Notes

- **For regular users:** Distribute `ChriOnline-Client.jar`
- **For administrators:** Distribute `ChriOnline-Admin.jar`
- **For server deployment:** Use `ChriOnline-Server.jar`

Both client and admin JARs need the server to be running to function properly.
