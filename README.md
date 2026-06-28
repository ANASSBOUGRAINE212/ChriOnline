# ChriOnline

A secure e-commerce desktop application built with JavaFX. Includes end-to-end encryption, PBKDF2 password hashing, and audit logging.

## Overview

ChriOnline is a client-server e-commerce platform with separate client and admin applications. Communication between client and server is encrypted end-to-end, and the server includes a security test suite for verifying the implementation.

## Architecture

```
Client / Admin (JavaFX)
        |
        | RSA-2048 handshake -> AES-256-GCM
        v
Server
  - Security layer (handshake, encryption, hashing, replay protection, audit log)
  - Business logic (auth, products, cart, orders, payments)
        |
        v
MySQL
  - Users (PBKDF2 hashed passwords)
  - Products, carts, orders
  - Secure store (AES-256 encrypted payment data)
  - Audit log (hash-chained)
```

## Security

- RSA-2048 key exchange on connection, AES-256-GCM for all subsequent messages
- PBKDF2WithHmacSHA256 password hashing, 65,536 iterations, random 16-byte salt per user
- AES-256-GCM at rest for stored payment data, unique IV per record
- Replay protection via transaction IDs with a 5 minute expiry window
- Parameterized queries everywhere (no string-built SQL)
- Hash-chained audit log for tamper detection
- Built-in security test suite (replay attacks, brute force demo, SQL injection attempts, audit integrity, encryption round-trip)

## Requirements

- Java 17+
- MySQL 8.0+
- Maven 3.6+

## Database setup

```sql
CREATE DATABASE chrii_online;
```

Set credentials in `src/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/chrii_online
db.user=your_username
db.password=your_password
```

## Running

Start the server first, then any client(s):

```bash
java -jar target/ChriOnline-Server.jar
java -jar target/ChriOnline-Client.jar
java -jar target/ChriOnline-Admin.jar
```

## Building from source

```bash
mvn clean package -Pclient
mvn package -Padmin
mvn package -Pserver
```


| Jar | Command | Size |
|---|---|---|
| Client | `mvn clean package -Pclient` | ~50-60 MB |
| Admin | `mvn package -Padmin` | ~50-60 MB |
| Server | `mvn package -Pserver` | ~5-10 MB |

## Usage

**Client** — browse products, manage cart, place orders, pay, view order/payment history, edit profile.

**Admin** — everything in client, plus product management and the security test suite (sidebar, "Security Tests").

## Project structure

```
src/
  app/          application bootstrap
  client/       client UI, Launcher, AdminLauncher
  server/       server entry point, per-connection handlers
  security/     RSA, AES, secure storage, security tests
  database/     DAOs, schema init
  model/        data models
  protocol/     request/response definitions
target/         built jars
pom.xml
```

## Tests

Unit tests live under `src/security/test`. Run via your IDE (right click `test.java` → Run As → Java Application), or interactively from the Admin app under "Security Tests".

| Category | Count | Covers |
|---|---|---|
| Password hashing | 5 | hash generation, verification, salt uniqueness |
| Secure storage | 6 | AES-GCM encrypt/decrypt, key handling |
| Audit logging | 8 | hash chaining, integrity check, ordering |
| Replay protection | 8 | transaction ID validation, expiry, concurrency |
| UDP notifications | 3 | message format, parsing, delivery |
| Secure streams | 5 | encrypted stream I/O, key exchange |

35 tests total.

## Stack

- Java 17, JavaFX 21
- MySQL 8.0+
- Maven
- Java Cryptography Architecture (built-in)
- Ikonli (FontAwesome 5) for icons

## Ports

- TCP 5000 — client-server encrypted communication
- UDP 5001 — order/payment notifications

## Configuration

`src/db.properties` for DB connection.

`src/server/serverApp.java`:
```java
private static final int TCP_PORT = 5000;
```

`src/server/UDPNotificationSender.java`:
```java
private static final int UDP_PORT = 5001;
```

## Protocol

Requests:
```java
request req = new request("ACTION", params);
```

Responses:
```java
response res = new response(success, message);
```

Actions: `LOGIN`, `REGISTER`, `GET_PRODUCTS`, `ADD_TO_CART`, `PLACE_ORDER`, `PROCESS_PAYMENT`, `GET_ORDERS`, `UPDATE_PROFILE`, `CHANGE_PASSWORD`.

## Team

- **Hiba Nahri** — RSA Handshake: RSA key pair generation, public key distribution, AES key decryption, handshake handling, nonce validation, client/admin handshake status UI, RSA test vectors, handshake replay tests
- **Ibtissame Meghraoui** — AES Encryption: AES-GCM/CBC implementation, IV generation, SecureInputStream/SecureOutputStream, encrypted message framing, encryption UI integration, AES test vectors, message tampering tests
- **Anas Bougraine** — Secure Storage: password hashing (PBKDF2), SecureDataStore, encrypted DB fields, anti-replay (transaction ID cache), audit logging, secure logout, hash and storage tests, MITM/replay simulations


## Acknowledgments

- Special thanks to our teacher for guidance and feedback throughout the project
