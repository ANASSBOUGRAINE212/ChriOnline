package server.handlers;

import database.dao.userDao;
import java.util.UUID;
import model.user;
import protocol.request;
import protocol.response;
import security.storage.AuditLogger;
import security.storage.PasswordHasher;
import security.storage.ReplayProtector;
import server.sessionManager;

public class authHandler {
    private final userDao userDAO = new userDao();
    private final sessionManager sessionMgr = sessionManager.getInstance();
    private final AuditLogger auditLogger = new AuditLogger();
    private final ReplayProtector replayProtector = new ReplayProtector();

    public static response handle(request request) {
        authHandler handler = new authHandler();
        return handler.handleRequest(request);
    }
    
    private response handleRequest(request request) {
        String type = request.getType();
        
        if (type.equals(request.LOGIN)) {
            return handleLogin(request);
        } else if (type.equals(request.REGISTER)) {
            return handleRegister(request);
        } else if (type.equals(request.LOGOUT)) {
            return handleLogout(request);
        } else if (type.equals(request.GET_USER_INFO)) {
            return handleGetUserInfo(request);
        } else if (type.equals(request.GET_PROFILE)) {
            return handleGetProfile(request);
        } else if (type.equals(request.UPDATE_PROFILE)) {
            return handleUpdateProfile(request);
        } else if (type.equals(request.CHANGE_PASSWORD)) {
            return handleChangePassword(request);
        } else {
            return new response(false, "Unknown command: " + type);
        }
    }

    private response handleLogin(request request) {
        String email    = request.getParam("email");
        String password = request.getParam("password");
        String txId     = request.getParam("txId"); // Transaction ID for replay protection

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            auditLogger.logAction("anonymous", "LOGIN_FAILED", "Missing email or password");
            return new response(false, "Email and password are required");
        }

        // 🔒 Replay Protection: Check if this login attempt was already processed
        if (txId != null && !txId.isEmpty()) {
            if (replayProtector.isReplay(txId)) {
                auditLogger.logAction(email.trim(), "LOGIN_REPLAY_BLOCKED", "Duplicate login attempt: " + txId);
                return new response(false, "Duplicate request detected. Please try again.");
            }
            replayProtector.registerTransaction(txId);
        }

        try {
            user userObj = userDAO.getUserByEmail(email.trim());
            if (userObj == null) {
                auditLogger.logAction(email.trim(), "LOGIN_FAILED", "User not found");
                return new response(false, "Invalid email or password");
            }

            // Use PBKDF2 password verification
            if (!PasswordHasher.verifyPassword(password, userObj.getPasswordHash())) {
                auditLogger.logAction(userObj.getUsername(), "LOGIN_FAILED", "Invalid password");
                return new response(false, "Invalid email or password");
            }

            String token = sessionMgr.createSession(userObj.getUserId());
            
            // 🔒 Audit log successful login
            auditLogger.logAction(userObj.getUsername(), "LOGIN_SUCCESS", 
                String.format("Logged in from session: %s", token.substring(0, 8) + "..."));
            
            System.out.println("✅ User logged in: " + userObj.getUsername());
            return new response(true, "Login successful|" + token + "|" + userObj.getRole().toString());

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            auditLogger.logAction(email.trim(), "LOGIN_ERROR", "Exception: " + e.getMessage());
            return new response(false, "Server error during login");
        }
    }
    
    private response handleRegister(request request) {
        String username = request.getParam("username");
        String email    = request.getParam("email");
        String password = request.getParam("password");
        String address  = request.getParam("address");
        String phone    = request.getParam("phone");
        
        if (username == null || email == null || password == null || 
            username.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            auditLogger.logAction("anonymous", "REGISTER_FAILED", "Missing required fields");
            return new response(false, "Username, email, and password are required");
        }
        
        username = username.trim();
        email    = email.trim();
        
        if (address == null) address = "";
        if (phone == null)   phone   = "";
        
        if (password.length() < 6) {
            auditLogger.logAction(username, "REGISTER_FAILED", "Password too short");
            return new response(false, "Password must be at least 6 characters long");
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            auditLogger.logAction(username, "REGISTER_FAILED", "Invalid email format");
            return new response(false, "Invalid email format");
        }
        
        try {
            if (userDAO.getUserByUsername(username) != null) {
                auditLogger.logAction(username, "REGISTER_FAILED", "Username already exists");
                return new response(false, "Username already exists");
            }
            
            if (userDAO.getUserByEmail(email) != null) {
                auditLogger.logAction(email, "REGISTER_FAILED", "Email already registered");
                return new response(false, "Email already registered");
            }
            
            String userId = UUID.randomUUID().toString();
            
            // Use PBKDF2 password hashing
            String hashedPassword = PasswordHasher.hashPassword(password);
            
            user newUser = new user(userId, username, email, hashedPassword,
                                    address.trim(), phone.trim(), user.Role.CLIENT);
            
            if (userDAO.createUser(newUser)) {
                // 🔒 Audit log successful registration
                auditLogger.logAction(username, "REGISTER_SUCCESS", 
                    String.format("New account created: %s (%s)", username, email));
                
                System.out.println("✅ New user registered: " + username);
                return new response(true, "Registration successful! You can now login. Your account has CLIENT privileges.");
            } else {
                auditLogger.logAction(username, "REGISTER_FAILED", "Database error");
                return new response(false, "Registration failed. Please try again.");
            }
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            auditLogger.logAction(username, "REGISTER_ERROR", "Exception: " + e.getMessage());
            return new response(false, "Server error during registration");
        }
    }
    
    private response handleLogout(request request) {
        String token = request.getToken();
        if (token == null || token.trim().isEmpty()) {
            return new response(false, "Invalid session");
        }
        try {
            String userId = sessionMgr.getUserIdFromToken(token);
            if (userId != null) {
                user userObj = userDAO.getUserById(userId);
                String username = userObj != null ? userObj.getUsername() : userId;
                
                // 🔒 Audit log logout
                auditLogger.logAction(username, "LOGOUT_SUCCESS", 
                    String.format("Logged out from session: %s", token.substring(0, 8) + "..."));
            }
            
            sessionMgr.removeSession(token);
            return new response(true, "Logged out successfully");
        } catch (Exception e) {
            auditLogger.logAction("unknown", "LOGOUT_ERROR", "Exception: " + e.getMessage());
            return new response(false, "Logout error");
        }
    }

    private response handleGetUserInfo(request request) {
        String token  = request.getToken();
        String userId = sessionMgr.getUserIdFromToken(token);
        if (userId == null) {
            return new response(false, "Not authenticated");
        }
        try {
            user userObj = userDAO.getUserById(userId);
            if (userObj == null) {
                return new response(false, "User not found");
            }
            return new response(true, userObj.getUserInfo());
        } catch (Exception e) {
            System.err.println("Get user info error: " + e.getMessage());
            return new response(false, "Error retrieving user information");
        }
    }
    
    private response handleGetProfile(request request) {
        return handleGetUserInfo(request);
    }

    private response handleUpdateProfile(request request) {
        String token  = request.getToken();
        String userId = sessionMgr.getUserIdFromToken(token);
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        String address = request.getParam("address");
        String phone   = request.getParam("phone");
        
        if (address == null) address = "";
        if (phone == null)   phone   = "";

        try {
            user userObj = userDAO.getUserById(userId);
            String username = userObj != null ? userObj.getUsername() : userId;
            
            boolean success = userDAO.updateProfile(userId, address, phone);
            if (success) {
                // 🔒 Audit log profile update
                auditLogger.logAction(username, "PROFILE_UPDATED", 
                    String.format("Updated profile: address=%s, phone=%s", 
                        address.isEmpty() ? "unchanged" : "changed",
                        phone.isEmpty() ? "unchanged" : "changed"));
                
                return new response(true, "Profile updated successfully");
            } else {
                return new response(false, "Failed to update profile");
            }
        } catch (Exception e) {
            System.err.println("Update profile error: " + e.getMessage());
            return new response(false, "Error updating profile");
        }
    }

    private response handleChangePassword(request request) {
        String token  = request.getToken();
        String userId = sessionMgr.getUserIdFromToken(token);
        if (userId == null) {
            return new response(false, "Not authenticated");
        }

        String oldPassword = request.getParam("oldPassword");
        String newPassword = request.getParam("newPassword");
        
        if (oldPassword == null || newPassword == null ||
            oldPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return new response(false, "Both old and new passwords are required");
        }
        
        if (newPassword.length() < 6) {
            return new response(false, "New password must be at least 6 characters long");
        }

        try {
            user userObj = userDAO.getUserById(userId);
            if (userObj == null) {
                return new response(false, "User not found");
            }
            
            // Verify old password with PBKDF2
            if (!PasswordHasher.verifyPassword(oldPassword, userObj.getPasswordHash())) {
                auditLogger.logAction(userObj.getUsername(), "PASSWORD_CHANGE_FAILED", "Incorrect old password");
                return new response(false, "Current password is incorrect");
            }
            
            // Hash new password with PBKDF2
            String newHashedPassword = PasswordHasher.hashPassword(newPassword);

            boolean success = userDAO.updatePasswordHash(userId, newHashedPassword);
            if (success) {
                // 🔒 Audit log password change
                auditLogger.logAction(userObj.getUsername(), "PASSWORD_CHANGED", "Password updated successfully");
                
                System.out.println("✅ Password changed for user: " + userObj.getUsername());
                return new response(true, "Password changed successfully");
            } else {
                auditLogger.logAction(userObj.getUsername(), "PASSWORD_CHANGE_FAILED", "Database update failed");
                return new response(false, "Failed to update password");
            }
        } catch (Exception e) {
            System.err.println("Change password error: " + e.getMessage());
            auditLogger.logAction(userId, "PASSWORD_CHANGE_ERROR", "Exception: " + e.getMessage());
            return new response(false, "Error changing password");
        }
    }

}