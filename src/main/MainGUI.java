import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;

public class MainGUI {
private JFrame frame;
private JList<Book> bookList;
private JList<CartItem> cartList;
private DefaultListModel<Book> bookListModel;
private DefaultListModel<CartItem> cartListModel;
private BookStoreSystem system;
private User currentUser;
private ArrayList<User> users;
private ExecutorService executor = Executors.newFixedThreadPool(3); 

public MainGUI() {
    loadUsers();
    login();
    if (currentUser != null) {
        system = new BookStoreSystem();
        loadCart();
        createUI();
    }
}

private void login() {
    String name = JOptionPane.showInputDialog("Enter your name:");
    if (name == null) System.exit(0);
    currentUser = users.stream()
            .filter(u -> u.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> {
                User u = new User("U" + (users.size() + 1), name, "Customer", "pass");
                users.add(u);
                return u;
            });
}

private void createUI() {
    frame = new JFrame("ReadNest Online Bookstore");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1000, 600);
    frame.setLayout(new GridLayout(1, 2));

    bookListModel = new DefaultListModel<>();
    cartListModel = new DefaultListModel<>();
    for (Book b : system.getBooks()) bookListModel.addElement(b);

    bookList = new JList<>(bookListModel);
    bookList.setFont(new Font("Arial", Font.PLAIN, 16));
    JScrollPane bookScroll = new JScrollPane(bookList);

    cartList = new JList<>(cartListModel);
    cartList.setFont(new Font("Arial", Font.PLAIN, 16));
    JScrollPane cartScroll = new JScrollPane(cartList);

    JPanel buttonPanel = new JPanel();
    JButton addToCart = new JButton("Add to Cart");
    JButton removeFromCart = new JButton("Remove");
    JButton purchase = new JButton("Purchase");

    buttonPanel.add(addToCart);
    buttonPanel.add(removeFromCart);
    buttonPanel.add(purchase);

    addToCart.addActionListener(e -> {
        Book selected = bookList.getSelectedValue();
        if (selected == null) return;
        String qtyStr = JOptionPane.showInputDialog("Enter quantity:");
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty < 1) throw new NumberFormatException();
            currentUser.getCart().addItem(selected, qty);
            refreshCart();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid quantity.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        }
    });

    removeFromCart.addActionListener(e -> {
        CartItem selected = cartList.getSelectedValue();
        if (selected != null) {
            currentUser.getCart().removeItem(selected.getBook());
            refreshCart();
        }
    });

    purchase.addActionListener(e -> {
        for (CartItem item : currentUser.getCart().getItems()) {
            executor.execute(() -> {
                synchronized (item.getBook()) {
                    try {
                        item.getBook().reduceStock(item.getQuantity());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage());
                    }
                }
            });
        }
        executor.shutdown();
        currentUser.getCart().clear();
        refreshCart();
        bookList.repaint();
        JOptionPane.showMessageDialog(frame, "Purchase successful!");
    });

    JPanel left = new JPanel(new BorderLayout());
    left.add(bookScroll, BorderLayout.CENTER);
    left.add(buttonPanel, BorderLayout.SOUTH);

    frame.add(left);
    frame.add(cartScroll);

    frame.setVisible(true);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        saveUsers();
        saveCart();
    }));

    refreshCart();
}

private void refreshCart() {
    cartListModel.clear();
    for (CartItem item : currentUser.getCart().getItems()) {
        cartListModel.addElement(item);
    }
}

private void loadUsers() {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("users.dat"))) {
        users = (ArrayList<User>) in.readObject();
    } catch (Exception e) {
        users = new ArrayList<>();
    }
}

private void saveUsers() {
    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
        out.writeObject(users);
    } catch (IOException ignored) {}
}

private void loadCart() {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("cart_" + currentUser.getUserId() + ".dat"))) {
        Cart saved = (Cart) in.readObject();
        if (saved != null) currentUser.setCart(saved);
    } catch (Exception ignored) {}
}

private void saveCart() {
    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("cart_" + currentUser.getUserId() + ".dat"))) {
        out.writeObject(currentUser.getCart());
    } catch (IOException ignored) {}
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(MainGUI::new);
}
}