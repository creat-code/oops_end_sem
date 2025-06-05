import java.io.Serializable;
import java.util.ArrayList;

public class BookStoreSystem implements Serializable {
    private ArrayList<Book> books;

    public BookStoreSystem() {
        books = new ArrayList<>();
        loadSampleBooks();
    }

    private void loadSampleBooks() {
        books.add(new Book("Java Basics", "Technology", 399.99, 10));
        books.add(new Book("Clean Code", "Technology", 549.99, 8));
        books.add(new Book("The Great Gatsby", "Fiction", 299.00, 5));
        books.add(new Book("Educated", "Memoir", 349.50, 7));
        books.add(new Book("Design Patterns", "Technology", 599.99, 4));
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    public Book findByTitle(String title) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;
    }
}
