import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class RegisterGUI {
    private JFrame frame;
    private JTextField nameField, emailField, phoneField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    private JComboBox<Integer> dayCombo, yearCombo;
    private JComboBox<String> monthCombo;

    public RegisterGUI() {
        frame = new JFrame("User Registration");
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(new Color(30, 60, 90));
        frame.setContentPane(contentPane);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(new Color(30, 60, 90));

        JLabel titleLabel = new JLabel("Register New User", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        userTypeCombo = new JComboBox<>(new String[]{"Patient", "Doctor"});
        addField(mainPanel, "User Type:", userTypeCombo);

        nameField = new JTextField();
        addField(mainPanel, "Name:", nameField);

        emailField = new JTextField();
        addField(mainPanel, "Email:", emailField);

        passwordField = new JPasswordField();
        addField(mainPanel, "Password:", passwordField);

        // Date of Birth (ComboBoxes)
        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dobPanel.setBackground(new Color(30, 60, 90));
        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setForeground(Color.WHITE);
        dobLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dobPanel.add(dobLabel);

        dayCombo = new JComboBox<>();
        for (int i = 1; i <= 31; i++) dayCombo.addItem(i);
        dobPanel.add(dayCombo);

        monthCombo = new JComboBox<>(new String[]{
                "01", "02", "03", "04", "05", "06",
                "07", "08", "09", "10", "11", "12"
        });
        dobPanel.add(monthCombo);

        yearCombo = new JComboBox<>();
        for (int i = LocalDate.now().getYear(); i >= 1900; i--) yearCombo.addItem(i);
        dobPanel.add(yearCombo);

        mainPanel.add(dobPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        phoneField = new JTextField();
        addField(mainPanel, "Phone:", phoneField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(30, 60, 90));

        JButton registerButton = new JButton("Register");
        styleButton(registerButton, new Color(0, 150, 136));
        registerButton.addActionListener(e -> processRegistration());
        buttonPanel.add(registerButton);

        JButton backButton = new JButton("Back");
        styleButton(backButton, new Color(150, 0, 0));
        backButton.addActionListener(e -> {
            frame.dispose();
            SwingUtilities.invokeLater(() -> new LoginGUI());
        });
        buttonPanel.add(backButton);

        mainPanel.add(buttonPanel);

        contentPane.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void addField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Tahoma", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 35));
    }

    private void processRegistration() {
        String userType = (String) userTypeCombo.getSelectedItem();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String phone = phoneField.getText().trim();

        int day = (Integer) dayCombo.getSelectedItem();
        String month = (String) monthCombo.getSelectedItem();
        int year = (Integer) yearCombo.getSelectedItem();

        String dob = String.format("%04d-%s-%02d", year, month, day);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(frame, "Invalid email format.\nExample: john.doe@example.com", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidDate(dob)) {
            JOptionPane.showMessageDialog(frame, "Invalid date.\nUse only real calendar dates. Example: 2001-12-25", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(frame, "Invalid phone number.\nOnly digits, 9-11 numbers. Example: 0123456789", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int userId = registerUser(userType, name, email, password, dob, phone);
            if (userId > 0) {
                JOptionPane.showMessageDialog(frame, "Registration successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                SwingUtilities.invokeLater(() -> new LoginGUI());
            } else {
                JOptionPane.showMessageDialog(frame, "Registration failed. Email might already be used.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int registerUser(String userType, String name, String email, String password, String dob, String phone) throws SQLException {
        String table = userType.equals("Patient") ? "patients" : "doctors";
        String sql = "INSERT INTO " + table + " (name, email, password, date_of_birth, phone) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, dob);
            stmt.setString(5, phone);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$", email);
    }

    private boolean isValidPhone(String phone) {
        return Pattern.matches("^\\d{9,11}$", phone);
    }

    private boolean isValidDate(String dob) {
        try {
            LocalDate.parse(dob, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
