# ChriOnline JavaFX UI

Modern JavaFX user interface for the ChriOnline e-commerce application.

## Features

- User authentication (Login/Register)
- Product browsing with search functionality
- Shopping cart management
- Order creation and tracking
- Payment processing
- Real-time server communication

## Prerequisites

- Java 11 or higher
- JavaFX SDK
- ChriOnline server running on localhost:5000

## Project Structure

```
ui/
├── src/
│   └── application/
│       ├── Main.java                    # Application entry point
│       ├── controllers/                 # FXML controllers
│       │   ├── LoginController.java
│       │   ├── RegisterController.java
│       │   ├── DashboardController.java
│       │   ├── ProductController.java
│       │   ├── CartController.java
│       │   ├── OrderController.java
│       │   └── PaymentController.java
│       └── utils/
│           └── ConnectionManager.java   # Server connection wrapper
├── resources/
│   ├── fxml/                           # FXML view files
│   │   ├── login.fxml
│   │   ├── register.fxml
│   │   ├── dashboard.fxml
│   │   ├── products.fxml
│   │   ├── cart.fxml
│   │   ├── orders.fxml
│   │   └── payment.fxml
│   └── css/
│       └── styles.css                  # Application styles
└── bin/                                # Compiled classes
```

## Running the Application

### From Eclipse

1. Make sure the ChriOnline server is running
2. Open the ChriOnline-UI project in Eclipse
3. Right-click on `Main.java` → Run As → Java Application

### From Command Line

```bash
# Compile (if needed)
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -d bin src/application/**/*.java

# Run
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp bin application.Main
```

## Usage

1. **Login/Register**: Start by creating an account or logging in
2. **Browse Products**: View available products in the dashboard
3. **Add to Cart**: Click "Add to Cart" on any product
4. **View Cart**: Click the Cart button to see your items
5. **Checkout**: Click "Checkout" to create an order
6. **Payment**: Select a payment method and complete the transaction
7. **View Orders**: Check your order history in the Orders section

## Features Implemented

### Authentication
- ✅ User registration with validation
- ✅ User login with session management
- ✅ Logout functionality

### Products
- ✅ List all products from server
- ✅ Search products by name
- ✅ View product details
- ✅ Add products to cart

### Shopping Cart
- ✅ View cart items
- ✅ Remove items from cart
- ✅ Clear entire cart
- ✅ View cart total
- ✅ Checkout to create order

### Orders
- ✅ Create orders from cart
- ✅ List user orders
- ✅ View order details
- ✅ Cancel orders

### Payments
- ✅ Process payments for orders
- ✅ Multiple payment methods (Credit Card, Debit Card, PayPal, Bank Transfer, Cash)
- ✅ Payment confirmation

## Connection Configuration

The UI connects to the server at `localhost:5000` by default. To change this, modify the `ConnectionManager.java` file:

```java
connection = new clientConnection("your-host", your-port);
```

## Troubleshooting

### Module javafx.controls not found
Make sure JavaFX is properly configured in your Eclipse project:
1. Right-click project → Properties → Java Build Path
2. Add JavaFX SDK as a library
3. Configure VM arguments: `--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml`

### Connection refused
Make sure the ChriOnline server is running before starting the UI application.

### Database errors
Ensure the database is properly initialized and the server can connect to it.
