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
        showWelcomeUI();
    }

    private void showWelcomeUI() {
        JFrame welcomeFrame = new JFrame("ReadNest Online Bookstore");
        welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeFrame.setSize(400, 200);
        welcomeFrame.setLayout(new FlowLayout());
        welcomeFrame.setLocationRelativeTo(null);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        welcomeFrame.add(loginButton);
        welcomeFrame.add(registerButton);

        loginButton.addActionListener(e -> {
            welcomeFrame.dispose();
            showLoginUI();
        });

        registerButton.addActionListener(e -> {
            welcomeFrame.dispose();
            showRegisterUI();
        });

        welcomeFrame.setVisible(true);
    }

    private void showLoginUI() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Fields cannot be empty.");
                showWelcomeUI();
                return;
            }

            currentUser = users.stream()
                .filter(u -> u.getName().equalsIgnoreCase(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

            if (currentUser == null) {
                JOptionPane.showMessageDialog(null, "Invalid username or password.");
                showWelcomeUI();
                return;
            }

            system = new BookStoreSystem();
            loadCart();
            createUI();
        } else {
            showWelcomeUI();
        }
    }

    private void showRegisterUI() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Register", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Fields cannot be empty.");
                showWelcomeUI();
                return;
            }

            if (users.stream().anyMatch(u -> u.getName().equalsIgnoreCase(username))) {
                JOptionPane.showMessageDialog(null, "Username already exists.");
                showWelcomeUI();
                return;
            }

            String userId = "U" + (users.size() + 1);
            currentUser = new User(userId, username, "Customer", password);
            users.add(currentUser);
            saveUsers();
            JOptionPane.showMessageDialog(null, "New user registered! Please log in.");
            showLoginUI();
        } else {
            showWelcomeUI();
        }
    }

    private void createUI() {
        frame = new JFrame("ReadNest Online Bookstore - Welcome " + currentUser.getName());
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
            try {
                executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}
            currentUser.getCart().clear();
            refreshCart();
            bookList.repaint();
            JOptionPane.showMessageDialog(frame, "Purchase successful!");
            executor = Executors.newFixedThreadPool(3);
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
        File file = new File("users.dat");
        if (!file.exists()) {
            users = new ArrayList<>();
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof ArrayList) {
                users = (ArrayList<User>) obj;
            } else {
                users = new ArrayList<>();
            }
        } catch (Exception e) {
            users = new ArrayList<>();
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            out.writeObject(users);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            out.flush();
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGUI::new);
    }
}