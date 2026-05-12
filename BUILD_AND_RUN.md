# ChriOnline - Build and Run Guide

## Quick Start

### Option 1: Use the Build Script (Easiest)
Double-click `build-all.bat` to build all three JARs at once.

### Option 2: Build Individually

**Build Client JAR:**
```bash
mvn clean package -Pclient
```

**Build Admin JAR:**
```bash
mvn clean package -Padmin
```

**Build Server JAR:**
```bash
mvn clean package -Pserver
```

## Running the Applications

### Using the Run Scripts (Easiest)
- Double-click `run-client.bat` to start the client
- Double-click `run-admin.bat` to start the admin client
- Double-click `run-server.bat` to start the server

### Using Command Line
```bash
# Run Client
java -jar target\ChriOnline-Client.jar

# Run Admin
java -jar target\ChriOnline-Admin.jar

# Run Server
java -jar target\ChriOnline-Server.jar
```

## What's the Difference?

| JAR | Main Class | Purpose | Admin Features? | Includes JavaFX? |
|-----|------------|---------|-----------------|------------------|
| **ChriOnline-Client.jar** | `client.Launcher` | Regular user client | ❌ No | ✅ Yes |
| **ChriOnline-Admin.jar** | `client.AdminLauncher` | Admin client with full access | ✅ Yes | ✅ Yes |
| **ChriOnline-Server.jar** | `server.serverApp` | Server application | N/A | ❌ No |

### Client Features (Regular Users):
- ✅ Browse products
- ✅ Shopping cart
- ✅ Place orders
- ✅ View payments
- ✅ Update profile
- ❌ **NO** Product Management
- ❌ **NO** Security Tests

### Admin Features (Administrators):
- ✅ Everything clients can do
- ✅ **Product Management** (Add/Delete/Update products)
- ✅ **Security Tests** menu
- ✅ Full system access

## Output Files

After building, you'll find these JARs in the `target` folder:
- `ChriOnline-Client.jar` (~50-60 MB with JavaFX)
- `ChriOnline-Admin.jar` (~50-60 MB with JavaFX)
- `ChriOnline-Server.jar` (~5-10 MB, no JavaFX)

## Distribution

To distribute your application:
1. Copy the JAR file(s) you need
2. Make sure Java 17+ is installed on the target machine
3. Run with: `java -jar [jar-name].jar`

That's it! No additional dependencies needed - everything is bundled in the JAR.

## Troubleshooting

**"Cannot resolve 'fas-star'" error:**
- Make sure you rebuilt with the updated pom.xml that includes the Ikonli transformers

**"Missing JavaFX runtime components" error:**
- Make sure you're using the Launcher class (already configured)
- The platform-specific JavaFX dependencies should be included

**Server won't start:**
- Make sure port 5000 is not already in use
- Check if another instance of the server is running

**Client can't connect:**
- Make sure the server is running first
- Check the connection settings (localhost:5000 by default)
