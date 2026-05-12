# Maven Commands for ChriiOnline

## Building Different JARs

### Build Client JAR (default)
```bash
mvn clean package
# or explicitly:
mvn clean package -Pclient
```
**Output:** `target/ChriOnline-Client.jar`

### Build Admin JAR
```bash
mvn clean package -Padmin
```
**Output:** `target/ChriOnline-Admin.jar`

### Build Server JAR
```bash
mvn clean package -Pserver
```
**Output:** `target/ChriOnline-Server.jar`

### Build All Three JARs at Once
```bash
mvn clean package -Pclient && mvn package -Padmin && mvn package -Pserver
```

## Running the JARs

### Run Client
```bash
java -jar target/ChriOnline-Client.jar
```

### Run Admin
```bash
java -jar target/ChriOnline-Admin.jar
```

### Run Server
```bash
java -jar target/ChriOnline-Server.jar
```

## Running with Maven (Development)

### Run Client
```bash
mvn clean javafx:run
```

### Run Server
```bash
mvn clean javafx:run -Pserver
```

## What Was Fixed?

### Problem
JavaFX cannot be bundled into a fat JAR with maven-shade-plugin because:
- JavaFX uses native libraries (.dll on Windows) that are platform-specific
- These native libraries can't be shaded properly
- You get "missing JavaFX runtime components" error

### Solution
1. **Created `Launcher.java`** - A plain Java class (not a JavaFX Application subclass) that allows JavaFX native libraries to load correctly
2. **Added platform-specific JavaFX dependencies** - Added Windows-specific (`win` classifier) JavaFX dependencies for the fat JAR
3. **Updated mainClass** - Changed both `javafx-maven-plugin` and `maven-shade-plugin` to use `client.Launcher` instead of `client.clientApp`

## Platform-Specific Classifiers

If you need to build for other platforms, change the classifier in pom.xml:
- **Windows**: `<classifier>win</classifier>`
- **Linux**: `<classifier>linux</classifier>`
- **macOS**: `<classifier>mac</classifier>`

## Summary

| Method | Command | Works? |
|--------|---------|--------|
| Maven run | `mvn javafx:run` | ✅ Always works |
| Fat JAR (with fix) | `java -jar target\ChriiOnline-0.0.1-SNAPSHOT.jar` | ✅ Works with Launcher + win classifier |
| Fat JAR (old) | `java -jar ...` | ❌ Missing JavaFX runtime |

## Quick Start

For development, just use:
```bash
mvn clean javafx:run
```

For distribution, build the JAR:
```bash
mvn clean package
java -jar target\ChriiOnline-0.0.1-SNAPSHOT.jar
```
