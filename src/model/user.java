package model;

public class user {
    public enum Role {
        CLIENT, ADMIN
    }
    
    private String userId;
    private String username;
    private String email;
    private String passwordHash;
    private String address;
    private String phone;
    private Role role;

    public user(String userId, String username, String email, 
    		String passwordHash,String address, String phone, Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.address = address;
        this.phone=phone;
        this.role=role;
    }
    
    public user() {
        // Default constructor
    }

    public String getUserId(){return userId;}
    public String getUsername(){return username;}
    public String getEmail(){return email;}
    public String getPasswordHash(){return passwordHash;}
    public String getAddress(){return address;}
    public String getPhone(){return phone;}
    public Role getRole(){return role;}

    public void setUserId(String idUser){this.userId= idUser;}
    public void setUsername(String username){this.username = username;}
    public void setEmail(String email){this.email = email;}
    public void setPasswordHash(String password){this.passwordHash = password;}
    public void setAddress(String address){this.address = address;}
    public void setPhone(String phone){this.phone = phone;}
    public void setRole(Role role){this.role = role;}
    
    public String getUserInfo() {
        return String.format(
            "👤 Username: %s\n" +
            "📧 Email: %s\n" +
            "📍 Address: %s\n" +
            "📞 Phone: %s\n" +
            "🏷️  Role: %s",
            username != null ? username : "Not set",
            email != null ? email : "Not set", 
            address != null ? address : "Not set",
            phone != null ? phone : "Not set",
            role != null ? role : "Not set"
        );
    }
    
    // DTO methods
    public static class UserDTO implements java.io.Serializable {
        public String userId;
        public String username;
        public String email;
        public String passwordHash;
        public String address;
        public String phone;
        public String role;
    }
    
    public UserDTO toDTO() {
        UserDTO dto = new UserDTO();
        dto.userId = this.userId;
        dto.username = this.username;
        dto.email = this.email;
        dto.passwordHash = this.passwordHash;
        dto.address = this.address;
        dto.phone = this.phone;
        dto.role = this.role != null ? this.role.name() : null;
        return dto;
    }
    
    public static user fromDTO(UserDTO dto) {
        Role role = dto.role != null ? Role.valueOf(dto.role) : Role.CLIENT;
        return new user(dto.userId, dto.username, dto.email, dto.passwordHash, dto.address, dto.phone, role);
    }
}
