package server.handlers;

import database.dao.userDao;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import model.user;
import protocol.request;
import protocol.response;
import server.sessionManager;

public class authHandler {
    private final userDao userDAO = new userDao();
    private final sessionManager sessionMgr = sessionManager.getInstance();

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

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            return new response(false, "Email and password are required");
        }

        try {
            user userObj = userDAO.getUserByEmail(email.trim());
            if (userObj == null) {
                return new response(false, "Invalid email or password");
            }

            String hashedPassword = hash(password);
            if (!userObj.getPasswordHash().equals(hashedPassword)) {
                return new response(false, "Invalid email or password");
            }

            String token = sessionMgr.createSession(userObj.getUserId());
            return new response(true, "Login successful|" + token + "|" + userObj.getRole().toString());

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
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
            return new response(false, "Username, email, and password are required");
        }
        
        username = username.trim();
        email    = email.trim();
        
        if (address == null) address = "";
        if (phone == null)   phone   = "";
        
        if (password.length() < 6) {
            return new response(false, "Password must be at least 6 characters long");
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            return new response(false, "Invalid email format");
        }
        
        try {
            if (userDAO.getUserByUsername(username) != null) {
                return new response(false, "Username already exists");
            }
            
            if (userDAO.getUserByEmail(email) != null) {
                return new response(false, "Email already registered");
            }
            
            String userId         = UUID.randomUUID().toString();
            String hashedPassword = hash(password);
            
            user newUser = new user(userId, username, email, hashedPassword,
                                    address.trim(), phone.trim(), user.Role.CLIENT);
            
            if (userDAO.createUser(newUser)) {
                return new response(true, "Registration successful! You can now login. Your account has CLIENT privileges.");
            } else {
                return new response(false, "Registration failed. Please try again.");
            }
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return new response(false, "Server error during registration");
        }
    }
    
    private response handleLogout(request request) {
        String token = request.getToken();
        if (token == null || token.trim().isEmpty()) {
            return new response(false, "Invalid session");
        }
        try {
            sessionMgr.removeSession(token);
            return new response(true, "Logged out successfully");
        } catch (Exception e) {
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
            boolean success = userDAO.updateProfile(userId, address, phone);
            if (success) {
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
            String oldHashedPassword = hash(oldPassword);
            String newHashedPassword = hash(newPassword);

            boolean success = userDAO.changePassword(userId, oldHashedPassword, newHashedPassword);
            if (success) {
                return new response(true, "Password changed successfully");
            } else {
                return new response(false, "Current password is incorrect");
            }
        } catch (Exception e) {
            System.err.println("Change password error: " + e.getMessage());
            return new response(false, "Error changing password");
        }
    }

    private String hash(String password) {
        try {
            MessageDigest md     = MessageDigest.getInstance("SHA-256");
            byte[] digest        = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb     = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}