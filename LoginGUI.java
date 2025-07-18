import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class LoginGUI {
    private JFrame frame;
    private JPanel panel;
    private JComboBox<String> userTypeCombo;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginGUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Clinic Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);

        // Main panel with background
        panel = new JPanel(new GridBagLayout()) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(5, 30, 60));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("CLINIC MANAGEMENT SYSTEM", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(200, 230, 255));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(titleLabel, gbc);

        // Subtitle
        gbc.gridy++;
        JLabel subtitleLabel = new JLabel("Secure Login Portal", SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(150, 200, 255));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(subtitleLabel, gbc);

        // Add Image Icon
        gbc.gridy++;
        gbc.gridwidth = 2;
        try {
            // Ensure this path is correct or place the image in the project root
            ImageIcon icon = new ImageIcon("Icon.jpg"); // Assuming Icon.jpg is in the project root
            Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(iconLabel, gbc);
        } catch (Exception ex) {
            System.err.println("Failed to load icon: " + ex.getMessage());
            // Optionally, add a placeholder or log the error to the user
        }


        // User Type
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel userTypeLabel = new JLabel("User Type:");
        styleLabel(userTypeLabel);
        panel.add(userTypeLabel, gbc);

        gbc.gridx = 1;
        userTypeCombo = new JComboBox<>(new String[]{"Patient", "Doctor"});
        styleComboBox(userTypeCombo);
        panel.add(userTypeCombo, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel emailLabel = new JLabel("Email:");
        styleLabel(emailLabel);
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField();
        styleTextField(emailField);
        panel.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password:");
        styleLabel(passwordLabel);
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField();
        passwordField.setEchoChar('•');
        stylePasswordField(passwordField);

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setOpaque(false);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        JButton showPasswordButton = new JButton("👁");
        showPasswordButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordButton.setBackground(new Color(10, 50, 100));
        showPasswordButton.setForeground(Color.WHITE);
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        showPasswordButton.setFocusPainted(false);
        showPasswordButton.addActionListener(e -> togglePasswordVisibility());
        passwordPanel.add(showPasswordButton, BorderLayout.EAST);

        panel.add(passwordPanel, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        loginButton = new JButton("Login");
        styleButton(loginButton, new Color(0, 120, 215));
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);

        registerButton = new JButton("Register");
        styleButton(registerButton, new Color(70, 70, 100));
        registerButton.addActionListener(e -> handleRegister());
        buttonPanel.add(registerButton);

        panel.add(buttonPanel, gbc);

        // Footer
        gbc.gridy++;
        JLabel footerLabel = new JLabel("© 2025 Clinic Management System v2.0", SwingConstants.CENTER);
        footerLabel.setForeground(new Color(150, 180, 220));
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        panel.add(footerLabel, gbc);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private void styleLabel(JLabel label) {
        label.setForeground(new Color(200, 230, 255));
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(15, 50, 90));
        field.setForeground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 100, 150), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void stylePasswordField(JPasswordField field) {
        styleTextField(field);
        field.setEchoChar('•');
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(15, 50, 90));
        combo.setForeground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(new Color(50, 100, 150), 1));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(0, 100, 180) : new Color(15, 50, 90));
                setForeground(Color.WHITE);
                return this;
            }
        });
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 200), 1),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void togglePasswordVisibility() {
        if (passwordField.getEchoChar() == 0) {
            passwordField.setEchoChar('•');
        } else {
            passwordField.setEchoChar((char) 0);
        }
    }

    private void handleLogin() {
        String userType = (String) userTypeCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Invalid email format");
            return;
        }

        try {
            int userId = -1;

            if (userType.equals("Patient")) {
                userId = authenticatePatient(email, password);
            } else {
                userId = authenticateDoctor(email, password);
            }

            if (userId > 0) {
                showSuccess("Login successful!");
                frame.dispose();

                if (userType.equals("Patient")) {
                    PatientGUI.show(userId);
                } else {
                    new DoctorGUI(userId);
                }
            } else {
                showError("Invalid credentials");
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }
    
    private void handleRegister() {
        frame.dispose(); // Close login frame
        SwingUtilities.invokeLater(() -> new RegisterGUI());
    }


    private int authenticatePatient(String email, String password) throws SQLException {
        String sql = "SELECT id FROM patients WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            stmt.setString(2, password.trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

    private int authenticateDoctor(String email, String password) throws SQLException {
        String sql = "SELECT id FROM doctors WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            stmt.setString(2, password.trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DBConnection.initializeDatabase();
                new LoginGUI();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to initialize database: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
