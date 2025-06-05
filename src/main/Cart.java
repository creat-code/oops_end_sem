import java.io.Serializable;
import java.util.ArrayList;

public class Cart implements Serializable {
    private ArrayList<CartItem> items = new ArrayList<>();

    public ArrayList<CartItem> getItems() { return items; }

    public void addItem(Book book, int quantity) throws Exception {
        for (CartItem item : items) {
            if (item.getBook().equals(book)) {
                int newTotal = item.getQuantity() + quantity;
                if (newTotal > book.getStock()) {
                    throw new Exception("Only " + book.getStock() + " units available for " + book.getTitle());
                }
                item.setQuantity(newTotal);
                return;
            }
        }
        if (quantity > book.getStock()) {
            throw new Exception("Only " + book.getStock() + " units available for " + book.getTitle());
        }
        items.add(new CartItem(book, quantity));
    }

    public void removeItem(Book book) {
        items.removeIf(item -> item.getBook().equals(book));
    }

    public void updateQuantity(Book book, int newQuantity) throws Exception {
        if (newQuantity < 1 || newQuantity > book.getStock()) {
            throw new Exception("Invalid quantity for " + book.getTitle());
        }
        for (CartItem item : items) {
            if (item.getBook().equals(book)) {
                item.setQuantity(newQuantity);
                return;
            }
        }
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getBook().getPrice() * item.getQuantity();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public void clear() {
        items.clear();
    }
}
