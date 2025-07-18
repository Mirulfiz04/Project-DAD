import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Date; // Import Date class

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PatientGUI {
    private static JFrame frame;
    private static int currentPatientId;
    private static Map<String, Integer> doctorMap = new HashMap<>();
    private static JTextField patientIdField;
    private static JComboBox<String> doctorComboBox;
    private static JComboBox<String> dateComboBox;
    private static JComboBox<String> timeComboBox;
    private static final HttpClient client = HttpClient.newHttpClient();


    public static void show(int patientId) {
        currentPatientId = patientId;
        frame = new JFrame("Patient Portal - Clinic Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);

        // Main panel with background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(5, 30, 60));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("PATIENT PORTAL", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(200, 230, 255));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitleLabel = new JLabel("Appointment Booking System", SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(150, 200, 255));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(50, 120, 180), 1),
                        "Appointment Details",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(180, 220, 255)
                ),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        patientIdField = new JTextField(String.valueOf(patientId));
        patientIdField.setEditable(false);
        styleTextField(patientIdField);

        doctorComboBox = new JComboBox<>();
        styleComboBox(doctorComboBox);

        dateComboBox = new JComboBox<>(getNext7Days());
        styleComboBox(dateComboBox);

        timeComboBox = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00"});
        styleComboBox(timeComboBox);

        formPanel.add(createLabel("Patient ID:"));
        formPanel.add(patientIdField);
        formPanel.add(createLabel("Select Doctor:"));

        JPanel doctorPanel = new JPanel(new BorderLayout(5, 5));
        doctorPanel.setOpaque(false);
        doctorPanel.add(doctorComboBox, BorderLayout.CENTER);

        JButton refreshButton = createButton("⟳", new Color(0, 100, 180));
        refreshButton.setPreferredSize(new Dimension(50, 30));
        refreshButton.addActionListener(e -> loadDoctors());
        doctorPanel.add(refreshButton, BorderLayout.EAST);

        formPanel.add(doctorPanel);
        formPanel.add(createLabel("Select Date:"));
        formPanel.add(dateComboBox);
        formPanel.add(createLabel("Select Time:"));
        formPanel.add(timeComboBox);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 20, 10)); // Adjusted for 6 buttons
        buttonPanel.setOpaque(false);

        JButton bookButton = createButton("Book Appointment", new Color(0, 120, 215));
        JButton viewButton = createButton("View My Appointments", new Color(0, 150, 180));
        JButton viewRecordsButton = createButton("View My Medical Records", new Color(0, 180, 150)); // New button
        JButton myProfileButton = createButton("My Profile", new Color(80, 120, 200)); // New button
        JButton mapButton = createButton("Find Nearby Pharmacies", new Color(80, 120, 200));
        JButton logoutButton = createButton("Logout", new Color(100, 30, 30));

        buttonPanel.add(bookButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(viewRecordsButton); // Add new button
        buttonPanel.add(myProfileButton); // Add new button
        buttonPanel.add(mapButton);
        buttonPanel.add(logoutButton);

        // Load doctors
        loadDoctors();

        bookButton.addActionListener(e -> bookAppointment());
        viewButton.addActionListener(e -> viewAppointments());
        viewRecordsButton.addActionListener(e -> viewMedicalRecords()); // Action for new button
        myProfileButton.addActionListener(e -> viewMyProfile()); // Action for new button
        mapButton.addActionListener(e -> openMap());
        logoutButton.addActionListener(e -> {
            frame.dispose();
            new LoginGUI();
        });

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    private static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(200, 230, 255));
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private static JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 200), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(15, 50, 90));
        field.setForeground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 100, 150), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private static void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(15, 50, 90));
        combo.setForeground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(new Color(50, 100, 150), 1));
        combo.setRenderer(new DefaultListCellRenderer() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(0, 100, 180) : new Color(15, 50, 90));
                setForeground(Color.WHITE);
                return this;
            }
        });
    }

    private static void loadDoctors() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, name, specialization FROM doctors";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                // Store results before invoking UI thread
                java.util.List<String> doctorNames = new java.util.ArrayList<>();
                java.util.Map<String, Integer> doctorIdMap = new java.util.HashMap<>();

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    int doctorId = rs.getInt("id");
                    String doctorName = rs.getString("name");
                    String specialization = rs.getString("specialization");
                    String displayName = doctorName + " (" + specialization + ")";

                    System.out.println("Doctor: " + doctorName + ", Specialization: " + specialization);

                    doctorNames.add(displayName);
                    doctorIdMap.put(displayName, doctorId);
                }

                if (!found) {
                    throw new Exception("No doctors found in database.");
                }

                // Now safely update GUI on EDT
                SwingUtilities.invokeLater(() -> {
                    doctorComboBox.removeAllItems();
                    doctorMap.clear();

                    for (String displayName : doctorNames) {
                        doctorComboBox.addItem(displayName);
                        doctorMap.put(displayName, doctorIdMap.get(displayName));
                    }
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame,
                        "Failed to load doctors:\n" + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            });
        }
    }


    private static String[] getNext7Days() {
        String[] dates = new String[7];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            dates[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DATE, 1);
        }

        return dates;
    }

    private static void bookAppointment() {
        try {
            String doctorName = (String) doctorComboBox.getSelectedItem();
            if (doctorName == null || doctorMap.get(doctorName) == null) {
                JOptionPane.showMessageDialog(frame, "Please select a doctor.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int doctorId = doctorMap.get(doctorName);
            int patientId = Integer.parseInt(patientIdField.getText());
            String date = (String) dateComboBox.getSelectedItem();
            String time = (String) timeComboBox.getSelectedItem();

            // Check for existing appointment (conflict)
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest checkRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/appointments?doctorId=" + doctorId + "&date=" + date + "&time=" + time))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> checkResponse = client.send(checkRequest, HttpResponse.BodyHandlers.ofString());

            if (checkResponse.statusCode() == 200 && new JSONArray(checkResponse.body()).length() > 0) {
                JOptionPane.showMessageDialog(frame, "This timeslot is already booked with the doctor. Please choose another.");
                return;
            }

            // Proceed with booking
            JSONObject json = new JSONObject();
            json.put("patientId", patientId);
            json.put("doctorId", doctorId);
            json.put("date", date);
            json.put("time", time);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/appointments"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                JOptionPane.showMessageDialog(frame,
                        "Appointment Booked:\nDoctor: " + doctorName + "\nDate: " + date + "\nTime: " + time,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Error: " + response.body(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private static void viewAppointments() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/appointments?patientId=" + currentPatientId))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());

                // Filter out cancelled appointments
                JSONArray filteredArray = new JSONArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String status = obj.optString("status", "Unknown").toUpperCase();
                    if (!status.equals("CANCELLED")) {
                        filteredArray.put(obj);
                    }
                }

                if (filteredArray.length() == 0) {
                    JOptionPane.showMessageDialog(frame,
                            "You have no upcoming appointments.",
                            "Your Appointments", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JDialog dialog = new JDialog(frame, "Your Appointments", true);
                dialog.setSize(800, 500);
                dialog.setLocationRelativeTo(frame);

                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(new Color(10, 40, 80));
                panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                String[] columnNames = {"Selected", "Appointment ID", "Doctor", "Date", "Time", "Status"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Class<?> getColumnClass(int column) {
                        return column == 0 ? Boolean.class : Object.class;
                    }

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return column == 0;
                    }
                };

                for (int i = 0; i < filteredArray.length(); i++) {
                    JSONObject obj = filteredArray.getJSONObject(i);
                    model.addRow(new Object[]{
                            false,
                            obj.optInt("id", 0),
                            obj.optString("doctorName", "Unknown"),
                            obj.optString("date", "Unknown"),
                            obj.optString("time", "Unknown"),
                            obj.optString("status", "Unknown").toUpperCase()
                    });
                }

                JTable table = new JTable(model);
                styleAppointmentsTable(table);

                JScrollPane scrollPane = new JScrollPane(table);
                panel.add(scrollPane, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
                buttonPanel.setOpaque(false);

                JButton rescheduleButton = createButton("Reschedule Selected", new Color(30, 100, 160));
                rescheduleButton.addActionListener(e -> handleReschedule(dialog, table));

                JButton cancelButton = createButton("Cancel Selected", new Color(200, 50, 50));
                cancelButton.addActionListener(e -> handleCancelSelected(dialog, table, model));

                JButton exportButton = createButton("Export to Text File", new Color(60, 140, 60));
                exportButton.addActionListener(e -> exportAppointments(table));

                JButton closeButton = createButton("Close", new Color(100, 30, 30));
                closeButton.addActionListener(e -> dialog.dispose());

                buttonPanel.add(rescheduleButton);
                buttonPanel.add(cancelButton);
                buttonPanel.add(exportButton);
                buttonPanel.add(closeButton);

                panel.add(buttonPanel, BorderLayout.SOUTH);
                dialog.add(panel);
                dialog.setVisible(true);
            } else {
                throw new Exception("Server returned status: " + response.statusCode());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Error viewing appointments: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    
    private static void handleCancelSelected(JDialog dialog, JTable table, DefaultTableModel model) {
        int selectedRow = -1;
        for (int i = 0; i < table.getRowCount(); i++) {
            Boolean isSelected = (Boolean) table.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                if (selectedRow != -1) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please select only one appointment to cancel.",
                            "Multiple Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                selectedRow = i;
            }
        }

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(dialog,
                    "Please select one appointment to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(dialog,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int appointmentId = (int) table.getValueAt(selectedRow, 1);

            try {
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8000/api/appointments/" + appointmentId))
                        .header("Content-Type", "application/json")
                        .DELETE()
                        .build();

                HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

                if (deleteResponse.statusCode() == 200 || deleteResponse.statusCode() == 204) {
                	System.out.println("Deleted appointment ID: " + appointmentId);
                    JOptionPane.showMessageDialog(dialog,
                            "Appointment cancelled successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    model.removeRow(selectedRow);
                } else {
                    throw new Exception("Server responded with: " + deleteResponse.statusCode());
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to cancel appointment: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }


    // Helper method to style the appointments table
    private static void styleAppointmentsTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setBackground(new Color(15, 50, 90));
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.GRAY);
        table.setSelectionBackground(new Color(30, 80, 150));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(0, 100, 180));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

 // Handle reschedule operation
    private static void handleReschedule(JDialog dialog, JTable table) {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(dialog,
                "Please select an appointment to reschedule",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appointmentId = (int) table.getValueAt(selectedRow, 1);
        String currentStatus = ((String) table.getValueAt(selectedRow, 5)).toLowerCase();

        // Check if appointment can be rescheduled
        if ("completed".equals(currentStatus) || "cancelled".equals(currentStatus)) {
            JOptionPane.showMessageDialog(dialog,
                "Cannot reschedule a " + currentStatus + " appointment.",
                "Invalid Operation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show reschedule dialog
        JPanel reschedulePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        reschedulePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> dateCombo = new JComboBox<>(getNext7Days());
        JComboBox<String> timeCombo = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00"});

        reschedulePanel.add(new JLabel("New Date:"));
        reschedulePanel.add(dateCombo);
        reschedulePanel.add(new JLabel("New Time:"));
        reschedulePanel.add(timeCombo);

        int result = JOptionPane.showConfirmDialog(dialog, reschedulePanel,
                "Reschedule Appointment", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String newDate = (String) dateCombo.getSelectedItem();
            String newTime = (String) timeCombo.getSelectedItem();

            try {
                JSONObject json = new JSONObject();
                json.put("date", newDate); // Ensure newDate is not null/empty
                json.put("time", newTime); // Ensure newTime is not null/empty
                // This URI is correct for the server context change in Step 1
                HttpRequest rescheduleRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8000/api/appointments/" + appointmentId + "/reschedule"))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json.toString())) // This must be PUT
                        .build();
                HttpResponse<String> rescheduleResponse = client.send(rescheduleRequest,
                        HttpResponse.BodyHandlers.ofString());
                if (rescheduleResponse.statusCode() == 200) {
                    JOptionPane.showMessageDialog(dialog,
                            "Appointment rescheduled successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    viewAppointments(); // Refresh the view
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to reschedule: " + rescheduleResponse.body(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error rescheduling: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }


    // Handle cancel operation
    private static void handleCancel(JDialog dialog, JTable table) {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(dialog,
                    "Please select an appointment to cancel",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appointmentId = (int) table.getValueAt(selectedRow, 1);
        String currentStatus = ((String) table.getValueAt(selectedRow, 5)).toLowerCase();

        // Check if appointment can be cancelled
        if ("completed".equals(currentStatus) || "cancelled".equals(currentStatus)) {
            JOptionPane.showMessageDialog(dialog,
                    "Cannot cancel a " + currentStatus + " appointment.",
                    "Invalid Operation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(dialog,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Send DELETE request with just the appointment ID in URL
                HttpRequest cancelRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8000/api/appointments/" + appointmentId))
                        .DELETE()  // No request body needed
                        .build();

                HttpResponse<String> cancelResponse = client.send(cancelRequest,
                        HttpResponse.BodyHandlers.ofString());

                // Debugging output
                System.out.println("Cancel response status: " + cancelResponse.statusCode());
                System.out.println("Cancel response body: " + cancelResponse.body());

                if (cancelResponse.statusCode() == 200) {
                    JOptionPane.showMessageDialog(dialog,
                            "Appointment cancelled successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    viewAppointments(); // Refresh the view
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to cancel appointment. Server responded with: " + 
                            cancelResponse.statusCode() + " - " + cancelResponse.body(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error cancelling appointment: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }



    // Export appointments to text file
    private static void exportAppointments(JTable table) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("MEDICAL CLINIC - APPOINTMENT HISTORY\n");
            sb.append("PATIENT ID: ").append(currentPatientId).append("\n\n");
            sb.append(String.format("%-10s %-20s %-12s %-8s %-12s\n",
                    "ID", "DOCTOR", "DATE", "TIME", "STATUS"));
            sb.append("-".repeat(70)).append("\n");

            for (int i = 0; i < table.getRowCount(); i++) {
                sb.append(String.format("%-10d %-20s %-12s %-8s %-12s\n",
                    table.getValueAt(i, 1),
                    table.getValueAt(i, 2),
                    table.getValueAt(i, 3),
                    table.getValueAt(i, 4),
                    table.getValueAt(i, 5)
                ));
            }

            // Generate a more recognizable filename
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String currentDate = dateFormat.format(new Date());
            String filename = "appointments_patient_" + currentPatientId + "_" + currentDate + ".txt";

            Files.write(Paths.get(filename), sb.toString().getBytes());

            JOptionPane.showMessageDialog(frame,
                    "Appointments exported to: " + filename,
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Failed to export: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private static void viewMedicalRecords() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/records?patientId=" + currentPatientId))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());

                if (jsonArray.length() == 0) {
                    JOptionPane.showMessageDialog(frame,
                            "You have no medical records available.",
                            "Your Medical Records", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JDialog dialog = new JDialog(frame, "Your Medical Records", true);
                dialog.setSize(900, 500);
                dialog.setLocationRelativeTo(frame);

                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(new Color(10, 40, 80));
                panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                String[] columnNames = {"Selected", "Date", "Time", "Doctor", "Diagnosis", "Treatment Type", "Medication", "Notes"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                    /**
					 *
					 */
					private static final long serialVersionUID = 1L;

					@Override
                    public Class<?> getColumnClass(int column) {
                        return column == 0 ? Boolean.class : Object.class;
                    }

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return column == 0; // Only checkbox is editable
                    }
                };

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    model.addRow(new Object[]{
                            false, // Checkbox
                            obj.optString("date", "N/A"),
                            obj.optString("time", "N/A"),
                            obj.optString("doctorName", "N/A"),
                            obj.optString("diagnosis", "N/A"),
                            obj.optString("treatmentType", "N/A"),
                            obj.optString("medication", "N/A"),
                            obj.optString("notes", "")
                    });
                }

                JTable table = new JTable(model);
                table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                table.setRowHeight(25);
                table.setBackground(new Color(15, 50, 90));
                table.setForeground(Color.WHITE);
                table.setGridColor(Color.GRAY);
                table.setSelectionBackground(new Color(30, 80, 150));
                table.getTableHeader().setReorderingAllowed(false);
                table.getTableHeader().setBackground(new Color(0, 100, 180));
                table.getTableHeader().setForeground(Color.WHITE);
                table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

                JScrollPane scrollPane = new JScrollPane(table);
                panel.add(scrollPane, BorderLayout.CENTER);

                // Buttons
                JButton exportButton = createButton("Export Selected", new Color(60, 140, 60));
                exportButton.addActionListener(e -> {
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("MEDICAL CLINIC - SELECTED MEDICATION RECORDS\n");
                        sb.append("PATIENT ID: ").append(currentPatientId).append("\n\n");
                        sb.append(String.format("%-12s %-8s %-20s %-15s %-18s %-15s %-20s\n",
                                "DATE", "TIME", "DOCTOR", "DIAGNOSIS", "TREATMENT", "MEDICATION", "NOTES"));
                        sb.append("-".repeat(110)).append("\n");

                        boolean hasSelected = false;

                        for (int i = 0; i < table.getRowCount(); i++) {
                            boolean selected = (Boolean) table.getValueAt(i, 0);
                            if (selected) {
                                hasSelected = true;
                                sb.append(String.format("%-12s %-8s %-20s %-15s %-18s %-15s %-20s\n",
                                        table.getValueAt(i, 1),
                                        table.getValueAt(i, 2),
                                        table.getValueAt(i, 3),
                                        table.getValueAt(i, 4),
                                        table.getValueAt(i, 5),
                                        table.getValueAt(i, 6),
                                        table.getValueAt(i, 7)
                                ));
                            }
                        }

                        if (!hasSelected) {
                            JOptionPane.showMessageDialog(dialog, "No records selected for export.",
                                    "Export Failed", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        // Generate a more recognizable filename
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                        String currentDate = dateFormat.format(new Date());
                        String filename = "medical_records_patient_" + currentPatientId + "_" + currentDate + ".txt";

                        Files.write(Paths.get(filename), sb.toString().getBytes());

                        JOptionPane.showMessageDialog(dialog, "Selected records exported to: " + filename,
                                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error exporting: " + ex.getMessage(),
                                "Export Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                });

                JButton closeButton = createButton("Close", new Color(100, 30, 30));
                closeButton.addActionListener(e -> dialog.dispose());

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setOpaque(false);
                buttonPanel.add(exportButton);
                buttonPanel.add(closeButton);

                panel.add(buttonPanel, BorderLayout.SOUTH);
                dialog.add(panel);
                dialog.setVisible(true);

            } else {
                throw new Exception("Server returned status: " + response.statusCode());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Error viewing medical records: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private static void viewMyProfile() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/patients?id=" + currentPatientId))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject patientData = new JSONObject(response.body());

                JDialog dialog = new JDialog(frame, "My Profile", true);
                dialog.setSize(400, 350);
                dialog.setLocationRelativeTo(frame);

                JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                panel.setBackground(new Color(10, 40, 80));

                JTextField nameField = new JTextField(patientData.optString("name", ""));
                JTextField emailField = new JTextField(patientData.optString("email", ""));
                JTextField phoneField = new JTextField(patientData.optString("phone", ""));
                JTextField dobField = new JTextField(patientData.optString("date_of_birth", ""));

                styleTextField(nameField);
                styleTextField(emailField);
                styleTextField(phoneField);
                styleTextField(dobField);

                panel.add(createLabel("Name:"));
                panel.add(nameField);
                panel.add(createLabel("Email:"));
                panel.add(emailField);
                panel.add(createLabel("Phone:"));
                panel.add(phoneField);
                panel.add(createLabel("Date of Birth (YYYY-MM-DD):"));
                panel.add(dobField);

                JButton saveButton = createButton("Save Changes", new Color(0, 150, 0));
                saveButton.addActionListener(e -> {
                    try {
                        JSONObject updatedData = new JSONObject();
                        updatedData.put("name", nameField.getText());
                        updatedData.put("email", emailField.getText());
                        updatedData.put("phone", phoneField.getText());
                        updatedData.put("date_of_birth", dobField.getText());

                        HttpRequest updateRequest = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8000/api/patients/" + currentPatientId + "/profile"))
                                .header("Content-Type", "application/json")
                                .PUT(HttpRequest.BodyPublishers.ofString(updatedData.toString()))
                                .build();

                        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());

                        if (updateResponse.statusCode() == 200) {
                            JOptionPane.showMessageDialog(dialog, "Profile updated successfully!");
                            dialog.dispose();
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Failed to update profile: " + updateResponse.body());
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error saving profile: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });

                JButton closeButton = createButton("Close", new Color(100, 30, 30));
                closeButton.addActionListener(e -> dialog.dispose());

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
                buttonPanel.setOpaque(false);
                buttonPanel.add(saveButton);
                buttonPanel.add(closeButton);

                dialog.add(panel, BorderLayout.CENTER);
                dialog.add(buttonPanel, BorderLayout.SOUTH);
                dialog.setVisible(true);

            } else {
                throw new Exception("Server returned status: " + response.statusCode());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Error loading profile: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private static void openMap() {
        try {
            String query = "pharmacy near Pusat Kesihatan UTeM, 76100 Durian Tunggal, Malacca";
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String mapUrl = "https://www.google.com/maps/search/?api=1&query=" + encodedQuery;
            Desktop.getDesktop().browse(new URI(mapUrl));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Failed to open Google Maps: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
