
package server.handlers;

import protocol.request;
import protocol.response;
import database.dao.userDao;
import model.user;
import server.sessionManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class authHandler {
    private final userDao userDAO = new userDao();
    private final sessionManager sessionMgr = sessionManager.getInstance();

    public static response handle(request request) {
        authHandler handler = new authHandler();
        return handler.handleRequest(request);
    }
    
    private response handleRequest(request request) {
        String type = request.getType();
        if (type.equals(request.GET_USER_INFO)) {
            return handleGetUserInfo(request);
        } else if (type.equals(request.UPDATE_PROFILE)) {
            return handleUpdateProfile(request);
        } else if (type.equals(request.CHANGE_PASSWORD)) {
            return handleChangePassword(request);
        } else {
            return new response(false, "Unknown auth command");
        }
    }

    private response handleGetUserInfo(request request) {
        String token = request.getToken();
        String userId = sessionMgr.getUserIdFromToken(token);
        if (userId == null) return new response(false, "Not authenticated");

        user userObj = userDAO.getUserById(userId);
        if (userObj == null) return new response(false, "User not found");

        String info = userObj.getUserInfo();
        return new response(true, info);
    }

    private response handleUpdateProfile(request request) {
        String token = request.getToken();
        String userId = sessionMgr.getUserIdFromToken(token);
        if (userId == null) return new response(false, "Not authenticated");

        String address = request.getParam("address");
        String phone   = request.getParam("phone");

        boolean ok = userDAO.updateProfile(userId, address, phone);
        return new response(ok, ok ? "Profile updated" : "Update failed");
    }

    private response handleChangePassword(request request) {
        String token = request.getToken();
        String userId = sessionMgr.getUserIdFromToken(token);
        if (userId == null) return new response(false, "Not authenticated");

        String oldPass = hash(request.getParam("oldPassword"));
        String newPass = hash(request.getParam("newPassword"));

        boolean ok = userDAO.changePassword(userId, oldPass, newPass);
        return new response(ok, ok ? "Password changed" : "Old password incorrect");
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
