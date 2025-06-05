import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String name;
    private String role;
    private String password;
    private Cart cart;

    public User(String userId, String name, String role, String password) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.password = password;
        this.cart = new Cart();
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getPassword() { return password; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
}
