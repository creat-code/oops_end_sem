import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.border.*;

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
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 102, 204), 0, getHeight(), new Color(0, 51, 102));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel("ReadNest Online Bookstore", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);

        // User panel with welcome label and buttons
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        JLabel userLabel = new JLabel("Welcome, " + currentUser.getName());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userLabel.setForeground(Color.WHITE);
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton, false);
        logoutButton.setPreferredSize(new Dimension(100, 30));
        JButton deleteAccountButton = new JButton("Delete Account");
        styleButton(deleteAccountButton, false);
        deleteAccountButton.setPreferredSize(new Dimension(130, 30));
        userPanel.add(userLabel);
        userPanel.add(logoutButton);
        userPanel.add(deleteAccountButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);

        // Main content
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Book list panel
        JPanel bookPanel = new JPanel(new BorderLayout());
        bookPanel.setBackground(Color.WHITE);
        bookPanel.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200), 1), new EmptyBorder(10, 10, 10, 10)));
        JLabel bookLabel = new JLabel("Available Books", JLabel.CENTER);
        bookLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        bookLabel.setForeground(new Color(0, 51, 102));
        bookLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        bookListModel = new DefaultListModel<>();
        for (Book b : system.getBooks()) bookListModel.addElement(b);
        bookList = new JList<>(bookListModel);
        bookList.setCellRenderer(new BookListRenderer());
        bookList.setBackground(Color.WHITE);
        bookList.setSelectionBackground(new Color(0, 102, 204));
        bookList.setSelectionForeground(Color.WHITE);
        JScrollPane bookScroll = new JScrollPane(bookList);
        bookScroll.setBorder(BorderFactory.createEmptyBorder());

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        JButton addToCart = new JButton("Add to Cart");
        JButton purchase = new JButton("Proceed to Checkout");
        styleButton(addToCart, false);
        styleButton(purchase, false);
        buttonPanel.add(addToCart);
        buttonPanel.add(purchase);

        bookPanel.add(bookLabel, BorderLayout.NORTH);
        bookPanel.add(bookScroll, BorderLayout.CENTER);
        bookPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Cart panel
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBackground(Color.WHITE);
        cartPanel.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200), 1), new EmptyBorder(10, 10, 10, 10)));
        JLabel cartLabel = new JLabel("Your Cart", JLabel.CENTER);
        cartLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        cartLabel.setForeground(new Color(0, 51, 102));
        cartLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(Color.WHITE);
        cartItemsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        cartItemsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        JScrollPane cartScroll = new JScrollPane(cartItemsPanel);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartScroll.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
        cartTotalLabel = new JLabel("Total: $0.00", JLabel.RIGHT);
        cartTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cartTotalLabel.setForeground(Color.WHITE);
        cartTotalLabel.setOpaque(true);
        cartTotalLabel.setBackground(new Color(0, 102, 204));
        cartTotalLabel.setBorder(new EmptyBorder(10, 15, 10, 15));
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
                JOptionPane.showMessageDialog(null, "Please select a book.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Cart cart = currentUser.getCart();
            try {
                Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getBook().equals(selected))
                    .findFirst();
                if (existingItem.isPresent()) {
                    int newQty = existingItem.get().getQuantity() + 1;
                    if (newQty <= selected.getStock()) {
                        existingItem.get().setQuantity(newQty);
                    } else {
                        JOptionPane.showMessageDialog(null, "Cannot exceed stock limit.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    cart.addItem(selected, 1);
                }
                refreshCart();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        purchase.addActionListener(e -> {
            if (currentUser.getCart().getItems().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Your cart is empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (CartItem item : currentUser.getCart().getItems()) {
                executor.execute(() -> {
                    synchronized (item.getBook()) {
                        try {
                            item.getBook().reduceStock(item.getQuantity());
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(null, "Purchase successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            executor = Executors.newFixedThreadPool(3);
        });

        logoutButton.addActionListener(e -> {
            saveCart();
            frame.dispose();
            currentUser = null;
            showWelcomeUI();
        });

        deleteAccountButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete your account? This action cannot be undone.",
                "Confirm Account Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                File cartFile = new File("cart_" + currentUser.getUserId() + ".dat");
                if (cartFile.exists()) {
                    cartFile.delete();
                }
                users.remove(currentUser);
                saveUsers();
                frame.dispose();
                currentUser = null;
                JOptionPane.showMessageDialog(null, "Account deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                showWelcomeUI();
            }
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
            button.setBackground(new Color(255, 0, 0)); 
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setBorder(new LineBorder(Color.DARK_GRAY, 1));
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(40, 25));
        } else {
            button.setBackground(isSmall ? new Color(200, 200, 200) : new Color(255, 147, 0)); 
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

    private class BookListRenderer extends JPanel implements ListCellRenderer<Book> {
        private JLabel titleLabel;
        private JLabel detailsLabel;

        public BookListRenderer() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(10, 10, 10, 10));

            titleLabel = new JLabel();
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(new Color(0, 51, 102));

            detailsLabel = new JLabel();
            detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            detailsLabel.setForeground(Color.GRAY);

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            textPanel.setOpaque(false);
            textPanel.add(titleLabel);
            textPanel.add(detailsLabel);

            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Book> list, Book book, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            titleLabel.setText(book.toString());
            detailsLabel.setText(String.format("%s | $%.2f | Stock: %d", book.getCategory(), book.getPrice(), book.getStock()));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                titleLabel.setForeground(Color.WHITE);
                detailsLabel.setForeground(Color.WHITE);
            } else {
                setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                setForeground(list.getForeground());
                titleLabel.setForeground(new Color(0, 51, 102));
                detailsLabel.setForeground(Color.GRAY);
            }
            return this;
        }
    }

    private void refreshCart() {
        cartItemsPanel.removeAll();
        double total = 0.0;
        for (CartItem item : currentUser.getCart().getItems()) {
            JPanel itemPanel = new JPanel(new GridBagLayout());
            itemPanel.setBackground(new Color(250, 250, 250));
            itemPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Decrease button
            JButton decreaseButton = new JButton("-");
            styleButton(decreaseButton, true);
            decreaseButton.addActionListener(e -> {
                int newQty = item.getQuantity() - 1;
                if (newQty <= 0) {
                    currentUser.getCart().removeItem(item.getBook());
                } else {
                    item.setQuantity(newQty);
                }
                refreshCart();
            });
            gbc.gridx = 0;
            gbc.gridy = 0;
            itemPanel.add(decreaseButton, gbc);

            // Quantity text field
            JTextField qtyField = new JTextField(String.valueOf(item.getQuantity()), 3);
            qtyField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            qtyField.setHorizontalAlignment(JTextField.CENTER);
            qtyField.setPreferredSize(new Dimension(40, 25));
            qtyField.setBorder(new LineBorder(new Color(200, 200, 200), 1));
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
                    item.setQuantity(newQty);
                    refreshCart();
                } else {
                    JOptionPane.showMessageDialog(null, "Cannot exceed stock limit.", "Error", JOptionPane.ERROR_MESSAGE);
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
            JLabel bookLabel = new JLabel(bookInfo);
            bookLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            bookLabel.setForeground(new Color(50, 50, 50));
            gbc.gridx = 3;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 10, 5, 10);
            itemPanel.add(bookLabel, gbc);

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
            gbc.insets = new Insets(5, 5, 5, 5);
            itemPanel.add(deleteButton, gbc);

            cartItemsPanel.add(itemPanel);
            total += item.getQuantity() * item.getBook().getPrice();
        }
        DecimalFormat df = new DecimalFormat("#0.00");
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
                item.setQuantity(newQty);
            } else {
                qtyField.setText(String.valueOf(item.getQuantity()));
                JOptionPane.showMessageDialog(null, "Cannot exceed stock limit.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshCart();
        } catch (NumberFormatException ex) {
            qtyField.setText(String.valueOf(item.getQuantity()));
            JOptionPane.showMessageDialog(null, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            qtyField.setText(String.valueOf(item.getQuantity()));
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGUI::new);
    }
}