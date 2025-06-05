import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MainGUI {
    private JFrame frame;
    private JList<Book> bookList;
    private DefaultListModel<Book> bookListModel;
    private BookStoreSystem system;
    private User currentUser;
    private ArrayList<User> users;
    private ExecutorService executor = Executors.newFixedThreadPool(3);
    private JLabel cartTotalLabel;
    private JPanel cartItemsPanel;

    public MainGUI() {
        loadUsers();
        showWelcomeUI();
    }

    private void showWelcomeUI() {
        JFrame welcomeFrame = new JFrame("ReadNest Online Bookstore");
        welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeFrame.setSize(400, 200);
        welcomeFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        styleButton(loginButton);
        styleButton(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(loginButton, gbc);
        gbc.gridy = 1;
        panel.add(registerButton, gbc);

        loginButton.addActionListener(e -> {
            welcomeFrame.dispose();
            showLoginUI();
        });

        registerButton.addActionListener(e -> {
            welcomeFrame.dispose();
            showRegisterUI();
        });

        welcomeFrame.add(panel);
        welcomeFrame.setVisible(true);
    }

    private void showLoginUI() {
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                showWelcomeUI();
                return;
            }

            currentUser = users.stream()
                .filter(u -> u.getName().equalsIgnoreCase(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

            if (currentUser == null) {
                JOptionPane.showMessageDialog(null, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
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
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Register", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                showWelcomeUI();
                return;
            }

            if (users.stream().anyMatch(u -> u.getName().equalsIgnoreCase(username))) {
                JOptionPane.showMessageDialog(null, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                showWelcomeUI();
                return;
            }

            String userId = "U" + (users.size() + 1);
            currentUser = new User(userId, username, "Customer", password);
            users.add(currentUser);
            saveUsers();
            JOptionPane.showMessageDialog(null, "New user registered! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            showLoginUI();
        } else {
            showWelcomeUI();
        }
    }

    private void createUI() {
        frame = new JFrame("ReadNest Online Bookstore");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 51, 102)); // Amazon-like dark blue
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("ReadNest Online Bookstore", JLabel.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        JLabel userLabel = new JLabel("Welcome, " + currentUser.getName(), JLabel.RIGHT);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        userLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);

        // Main content
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Book list panel
        bookListModel = new DefaultListModel<>();
        for (Book b : system.getBooks()) bookListModel.addElement(b);
        bookList = new JList<>(bookListModel);
        bookList.setFont(new Font("Arial", Font.PLAIN, 16));
        bookList.setSelectionBackground(new Color(255, 147, 0)); // Amazon orange
        bookList.setSelectionForeground(Color.WHITE);
        JScrollPane bookScroll = new JScrollPane(bookList);
        bookScroll.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        JPanel bookPanel = new JPanel(new BorderLayout());
        bookPanel.setBackground(Color.WHITE);
        JLabel bookLabel = new JLabel("Available Books", JLabel.CENTER);
        bookLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bookPanel.add(bookLabel, BorderLayout.NORTH);
        bookPanel.add(bookScroll, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        JButton addToCart = new JButton("Add to Cart");
        JButton purchase = new JButton("Proceed to Checkout");
        styleButton(addToCart);
        styleButton(purchase);
        buttonPanel.add(addToCart);
        buttonPanel.add(purchase);

        bookPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Cart panel
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBackground(Color.WHITE);
        JLabel cartLabel = new JLabel("Your Cart", JLabel.CENTER);
        cartLabel.setFont(new Font("Arial", Font.BOLD, 18));
        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(Color.WHITE);
        JScrollPane cartScroll = new JScrollPane(cartItemsPanel);
        cartScroll.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        cartTotalLabel = new JLabel("Total: $0.00", JLabel.RIGHT);
        cartTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        cartTotalLabel.setBorder(new EmptyBorder(10, 0, 10, 10));
        cartPanel.add(cartLabel, BorderLayout.NORTH);
        cartPanel.add(cartScroll, BorderLayout.CENTER);
        cartPanel.add(cartTotalLabel, BorderLayout.SOUTH);

        mainPanel.add(bookPanel);
        mainPanel.add(cartPanel);

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);

        // Button actions
        addToCart.addActionListener(e -> {
            Book selected = bookList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(frame, "Please select a book.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String qtyStr = JOptionPane.showInputDialog(frame, "Enter quantity:");
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty < 1) throw new NumberFormatException();
                currentUser.getCart().addItem(selected, qty);
                refreshCart();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        purchase.addActionListener(e -> {
            if (currentUser.getCart().getItems().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Your cart is empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (CartItem item : currentUser.getCart().getItems()) {
                executor.execute(() -> {
                    synchronized (item.getBook()) {
                        try {
                            item.getBook().reduceStock(item.getQuantity());
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(frame, "Purchase successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            executor = Executors.newFixedThreadPool(3);
        });

        frame.setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveUsers();
            saveCart();
        }));

        refreshCart();
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(255, 147, 0)); // Amazon orange
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(new LineBorder(Color.DARK_GRAY, 1));
        button.setFocusPainted(false);
        button.setPreferredSize(button.getText().equals("Delete") ? new Dimension(80, 25) : new Dimension(150, 35));
    }

    private void refreshCart() {
        cartItemsPanel.removeAll();
        double total = 0.0;
        for (CartItem item : currentUser.getCart().getItems()) {
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBackground(Color.WHITE);
            itemPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
            JLabel itemLabel = new JLabel(item.getQuantity() + " " + item.getBook().toString());
            itemLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            JButton deleteButton = new JButton("Delete");
            styleButton(deleteButton);
            deleteButton.addActionListener(e -> {
                currentUser.getCart().removeItem(item.getBook());
                refreshCart();
            });
            itemPanel.add(itemLabel, BorderLayout.CENTER);
            itemPanel.add(deleteButton, BorderLayout.EAST);
            cartItemsPanel.add(itemPanel);
            total += item.getQuantity() * item.getBook().getPrice();
        }
        DecimalFormat df = new DecimalFormat("#.00");
        cartTotalLabel.setText("Total: $" + df.format(total));
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
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