package ChriOnline.Model;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String sessionToken;

    public User(int id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.sessionToken = null;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getSessionToken() { return sessionToken; }

    // Setter token
    public void setSessionToken(String token) { this.sessionToken = token; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username=" + username + ", email=" + email + "}";
    }
}