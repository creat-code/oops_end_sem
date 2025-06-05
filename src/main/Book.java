import java.io.Serializable;

public class Book implements Serializable {
    private String title;
    private String category;
    private double price;
    private int stock;

    public Book(String title, String category, double price, int stock) {
        this.title = title;
        this.category = category;
        this.price = Math.round(price * 100.0) / 100.0;
        this.stock = stock;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public synchronized void reduceStock(int quantity) throws Exception {
        if (quantity > stock) throw new Exception("Insufficient stock for " + title);
        stock -= quantity;
    }

    public synchronized void increaseStock(int quantity) {
        stock += quantity;
    }

    @Override
    public String toString() {
        return title + " (" + category + ") ₹" + String.format("%.2f", price) + " [Stock: " + stock + "]";
    }
}
