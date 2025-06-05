import java.awt.*;
import java.awt.event.*;
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
        styleButton(loginButton, false);
        styleButton(registerButton, false);

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
        styleButton(addToCart, false);
        styleButton(purchase, false);
        buttonPanel.add(addToCart);
        buttonPanel.add(purchase);

        bookPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Cart panel
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBackground(Color.WHITE);
        JLabel cartLabel = new JLabel("Your Cart", JLabel.CENTER);
        cartLabel.setFont(new Font("Arial", Font.BOLD, 18));
        cartLabel.setBorder(new EmptyBorder(0, 0, 0, 0)); // No padding to touch the top border
        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(Color.WHITE);
        cartItemsPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); // No padding
        JScrollPane cartScroll = new JScrollPane(cartItemsPanel);
        cartScroll.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        cartScroll.setViewportBorder(new EmptyBorder(0, 0, 0, 0)); // No padding in viewport
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
            Cart cart = currentUser.getCart();
            try {
                boolean found = false;
                for (CartItem item : cart.getItems()) {
                    if (item.getBook().equals(selected)) {
                        int newQty = item.getQuantity() + 1;
                        if (newQty <= selected.getStock()) {
                            cart.removeItem(selected);
                            cart.addItem(selected, newQty);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Cannot exceed stock limit.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    cart.addItem(selected, 1);
                }
                refreshCart();
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

    private void styleButton(JButton button, boolean isSmall) {
        if (button.getText().equals("Del")) {
            button.setBackground(new Color(255, 0, 0)); // Red for delete button
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setBorder(new LineBorder(Color.DARK_GRAY, 1));
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(40, 25));
        } else {
            button.setBackground(isSmall ? new Color(200, 200, 200) : new Color(255, 147, 0)); // Amazon orange for main buttons, gray for small buttons
            button.setForeground(isSmall ? Color.BLACK : Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, isSmall ? 12 : 14));
            button.setBorder(new LineBorder(Color.DARK_GRAY, 1));
            button.setFocusPainted(false);
            if (isSmall) {
                button.setPreferredSize(new Dimension(25, 25));
            } else {
                button.setPreferredSize(new Dimension(150, 35));
            }
        }
    }

    private void refreshCart() {
        cartItemsPanel.removeAll();
        double total = 0.0;
        for (CartItem item : currentUser.getCart().getItems()) {
            JPanel itemPanel = new JPanel(new GridBagLayout());
            itemPanel.setBackground(Color.WHITE);
            itemPanel.setBorder(new EmptyBorder(2, 5, 2, 5));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 3, 0, 3);
            gbc.anchor = GridBagConstraints.WEST;

            // Decrease button
            JButton decreaseButton = new JButton("-");
            styleButton(decreaseButton, true);
            decreaseButton.addActionListener(e -> {
                int newQty = item.getQuantity() - 1;
                if (newQty <= 0) {
                    currentUser.getCart().removeItem(item.getBook());
                } else {
                    currentUser.getCart().removeItem(item.getBook());
                    try {
                        currentUser.getCart().addItem(item.getBook(), newQty);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                refreshCart();
            });
            gbc.gridx = 0;
            gbc.gridy = 0;
            itemPanel.add(decreaseButton, gbc);

            // Quantity text field
            JTextField qtyField = new JTextField(String.valueOf(item.getQuantity()), 3);
            qtyField.setFont(new Font("Arial", Font.PLAIN, 14));
            qtyField.setHorizontalAlignment(JTextField.CENTER);
            qtyField.setPreferredSize(new Dimension(40, 25));
            qtyField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateQuantityFromTextField(qtyField, item);
                }
            });
            qtyField.addActionListener(e -> updateQuantityFromTextField(qtyField, item));
            gbc.gridx = 1;
            itemPanel.add(qtyField, gbc);

            // Increase button
            JButton increaseButton = new JButton("+");
            styleButton(increaseButton, true);
            increaseButton.addActionListener(e -> {
                int newQty = item.getQuantity() + 1;
                if (newQty <= item.getBook().getStock()) {
                    currentUser.getCart().removeItem(item.getBook());
                    try {
                        currentUser.getCart().addItem(item.getBook(), newQty);
                        refreshCart();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Cannot exceed stock limit.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            gbc.gridx = 2;
            itemPanel.add(increaseButton, gbc);

            // Book info
            String bookInfo = String.format("%s (%s) $%.2f [Stock: %d]",
                item.getBook().toString(),
                item.getBook().getCategory(),
                item.getBook().getPrice(),
                item.getBook().getStock());
            JLabel itemLabel = new JLabel(bookInfo);
            itemLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 3;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            itemPanel.add(itemLabel, gbc);

            // Delete button
            JButton deleteButton = new JButton("Del");
            styleButton(deleteButton, true);
            deleteButton.addActionListener(e -> {
                currentUser.getCart().removeItem(item.getBook());
                refreshCart();
            });
            gbc.gridx = 4;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            itemPanel.add(deleteButton, gbc);

            cartItemsPanel.add(itemPanel);
            total += item.getQuantity() * item.getBook().getPrice();
        }
        DecimalFormat df = new DecimalFormat("#.00");
        cartTotalLabel.setText("Total: $" + df.format(total));
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    private void updateQuantityFromTextField(JTextField qtyField, CartItem item) {
        try {
            int newQty = Integer.parseInt(qtyField.getText().trim());
            if (newQty <= 0) {
                currentUser.getCart().removeItem(item.getBook());
            } else if (newQty <= item.getBook().getStock()) {
                currentUser.getCart().removeItem(item.getBook());
                currentUser.getCart().addItem(item.getBook(), newQty);
            } else {
                qtyField.setText(String.valueOf(item.getQuantity())); // Revert to original quantity
                JOptionPane.showMessageDialog(frame, "Cannot exceed stock limit.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshCart();
        } catch (NumberFormatException ex) {
            qtyField.setText(String.valueOf(item.getQuantity())); // Revert to original quantity
            JOptionPane.showMessageDialog(frame, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            qtyField.setText(String.valueOf(item.getQuantity())); // Revert to original quantity
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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