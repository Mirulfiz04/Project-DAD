// FileName: MultipleFiles/DoctorGUI.java
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.JDialog; // Added for patient details dialog

import org.json.JSONArray;
import org.json.JSONObject;

public class DoctorGUI extends JFrame {
    private int currentDoctorId;
    private Map<String, Integer> appointmentMap = new HashMap<>();
    private Map<Integer, Integer> appointmentIdToPatientIdMap = new HashMap<>(); // To store appointment ID to patient ID mapping

    private static final String[] TREATMENT_TYPES = {
            "Medication", "Physical Therapy", "Surgery", "Counseling", "Observation",
            "Vaccination", "Check-up", "Blood Test", "MRI Scan", "X-Ray", "Dental Cleaning"
    };

    private static final String[] MEDICATIONS = {
            "Paracetamol", "Ibuprofen", "Amoxicillin", "Lisinopril", "Atorvastatin",
            "Metformin", "Albuterol", "Omeprazole", "Losartan", "Simvastatin",
            "Panadol", "Cetirizine", "Azithromycin", "Salbutamol", "Doxycycline",
            "Vitamin D", "Hydrochlorothiazide", "Levothyroxine", "Amlodipine"
    };

    private JComboBox<String> appointmentCombo;
    private JTextArea diagnosisArea;
    private JComboBox<String> treatmentCombo;
    private JComboBox<String> medicationCombo;
    private JTextArea notesArea;
    private JTable recordsTable;
    private JComboBox<String> appointmentStatusFilterCombo; // New: Filter appointments by status

    // Color scheme
    private final Color DARK_BLUE = new Color(0, 71, 135);  // Darker, richer blue
    private final Color MEDIUM_BLUE = new Color(0, 122, 204); // Brighter medium blue
    private final Color LIGHT_BLUE = new Color(200, 230, 255); // Very light blue
    private final Color WHITE = Color.WHITE;
    private final Color LIGHT_GRAY = new Color(245, 245, 245); // Near-white gray
    private final Color TEXT_COLOR = Color.BLACK;

    public DoctorGUI(int doctorId) {
        this.currentDoctorId = doctorId;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Doctor Portal - Clinic Management System");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Customize the look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TabbedPane.background", DARK_BLUE);
            UIManager.put("TabbedPane.foreground", WHITE);
            UIManager.put("TabbedPane.selected", MEDIUM_BLUE);
            UIManager.put("TabbedPane.borderHightlightColor", MEDIUM_BLUE);
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 1, 1, 1));
            UIManager.put("TabbedPane.tabsOverlapBorder", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BLUE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel label = new JLabel("DOCTOR PORTAL", SwingConstants.CENTER);
        label.setForeground(WHITE); // Keep white for header
        label.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JSeparator separator = new JSeparator();
        separator.setForeground(MEDIUM_BLUE);

        panel.add(label, BorderLayout.CENTER);
        panel.add(separator, BorderLayout.SOUTH);
        return panel;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new CustomTabbedPaneUI());  // ← Add this line

        tabbedPane.setBackground(DARK_BLUE);
        tabbedPane.setForeground(WHITE); // This is overridden by the custom UI
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel appointmentsPanel = createAppointmentPanel();
        JPanel recordsPanel = createRecordsPanel();

        appointmentsPanel.setBackground(LIGHT_BLUE);
        recordsPanel.setBackground(LIGHT_BLUE);

        tabbedPane.addTab("Appointments", appointmentsPanel);
        tabbedPane.addTab("Patient Records", recordsPanel);

        return tabbedPane;
    }


    class CustomTabbedPaneUI extends BasicTabbedPaneUI {
        private final Color selectedColor = Color.WHITE;
        private final Color unselectedColor = Color.BLACK;

        protected void paintText(Graphics g, int tabPlacement, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            g.setFont(metrics.getFont());
            g.setColor(isSelected ? selectedColor : unselectedColor);
            g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }
    }

    private JPanel createAppointmentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 20, 20, 20));
        mainPanel.setBackground(LIGHT_BLUE); // Lighter background

        // Appointment selection panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        topPanel.setBackground(LIGHT_BLUE);

        appointmentCombo = new JComboBox<>();
        appointmentCombo.setPreferredSize(new Dimension(500, 30));
        appointmentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        appointmentCombo.setForeground(TEXT_COLOR); // Black text

        JButton refreshBtn = new JButton("Refresh Appointments");
        styleButton(refreshBtn, MEDIUM_BLUE);
        refreshBtn.setPreferredSize(new Dimension(200, 40));  // Line ~372


        // New: Filter appointments by status
        JLabel filterLabel = new JLabel("Filter by Status:");
        filterLabel.setForeground(TEXT_COLOR);
        appointmentStatusFilterCombo = createStyledComboBox(new String[]{"All", "Confirmed", "Pending", "Rescheduled", "Completed", "Cancelled"});
        appointmentStatusFilterCombo.addActionListener(e -> refreshAppointments()); // Refresh on filter change

        JButton viewPatientDetailsBtn = new JButton("View Patient Details"); // New button
        viewPatientDetailsBtn.setPreferredSize(new Dimension(180, 40));
        styleButton(viewPatientDetailsBtn, new Color(0, 100, 180)); // Blue color

        JLabel selectLabel = new JLabel("Select Appointment:");
        selectLabel.setForeground(TEXT_COLOR); // Black text
        topPanel.add(selectLabel);
        topPanel.add(appointmentCombo);
        topPanel.add(Box.createHorizontalStrut(15));
        topPanel.add(refreshBtn);
        topPanel.add(filterLabel); // Add filter label
        topPanel.add(appointmentStatusFilterCombo); // Add filter combo
        topPanel.add(viewPatientDetailsBtn); // Add view patient details button

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 15, 15)); // Increased rows for status update
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(MEDIUM_BLUE, 2),
                        "Enter Treatment Details"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        formPanel.setBackground(WHITE);

        diagnosisArea = createStyledTextArea();
        treatmentCombo = createStyledComboBox(TREATMENT_TYPES);
        medicationCombo = createStyledComboBox(MEDICATIONS);
        notesArea = createStyledTextArea();
        JButton submitBtn = new JButton("Submit Treatment");
        styleButton(submitBtn, new Color(0, 150, 0));

        // New: Appointment Status Update
        JComboBox<String> statusUpdateCombo = createStyledComboBox(new String[]{"confirmed", "completed", "cancelled", "rescheduled"});
        JButton updateStatusBtn = new JButton("Update Status");
        styleButton(updateStatusBtn, new Color(255, 140, 0)); // Orange color

        // Add form components with black text labels
        formPanel.add(createFormLabel("Diagnosis:"));
        formPanel.add(new JScrollPane(diagnosisArea));
        formPanel.add(createFormLabel("Treatment Type:"));
        formPanel.add(treatmentCombo);
        formPanel.add(createFormLabel("Medication:"));
        formPanel.add(medicationCombo);
        formPanel.add(createFormLabel("Additional Notes:"));
        formPanel.add(new JScrollPane(notesArea));
        formPanel.add(new JLabel()); // Empty cell for layout
        formPanel.add(submitBtn);
        formPanel.add(createFormLabel("Update Status:")); // New row for status update
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(statusUpdateCombo);
        statusPanel.add(updateStatusBtn);
        formPanel.add(statusPanel);


        refreshBtn.addActionListener(e -> refreshAppointments());
        submitBtn.addActionListener(e -> submitTreatment());
        updateStatusBtn.addActionListener(e -> updateAppointmentStatus((String) statusUpdateCombo.getSelectedItem()));
        viewPatientDetailsBtn.addActionListener(e -> viewSelectedPatientDetails()); // Action for new button


        refreshAppointments();

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 20, 20, 20));
        panel.setBackground(LIGHT_BLUE); // Lighter background

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchPanel.setBackground(LIGHT_BLUE);

        JTextField searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(TEXT_COLOR); // Black text

        JButton searchBtn = new JButton("Search Patient");
        JButton viewAllBtn = new JButton("View All Records");
        JButton exportRecordsBtn = new JButton("Export Records"); // New button
        styleButton(searchBtn, MEDIUM_BLUE);
        styleButton(viewAllBtn, DARK_BLUE);
        styleButton(exportRecordsBtn, new Color(60, 140, 60)); // Green color

        JLabel searchLabel = createFormLabel("Patient Name:");
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(Box.createHorizontalStrut(15));
        searchPanel.add(viewAllBtn);
        searchPanel.add(exportRecordsBtn); // Add export button

        recordsTable = new JTable();
        recordsTable.setAutoCreateRowSorter(true);
        recordsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recordsTable.setForeground(TEXT_COLOR); // Black text
        recordsTable.setRowHeight(25);
        recordsTable.setSelectionBackground(MEDIUM_BLUE);
        recordsTable.setSelectionForeground(WHITE);
        recordsTable.setGridColor(new Color(200, 200, 200));

        JScrollPane tableScrollPane = new JScrollPane(recordsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(MEDIUM_BLUE, 1));

        searchBtn.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                loadPatientRecords(searchTerm);
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a patient name to search",
                        "Input Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        viewAllBtn.addActionListener(e -> {
            searchField.setText("");
            loadPatientRecords("");
        });

        exportRecordsBtn.addActionListener(e -> exportPatientRecords()); // Action for new button

        loadPatientRecords("");

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(DARK_BLUE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, new Color(204, 0, 0));

        logoutBtn.addActionListener(e -> {
            this.dispose();
            SwingUtilities.invokeLater(LoginGUI::new);
        });
        panel.add(logoutBtn);
        return panel;
    }

    // Helper methods for styling components
    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_COLOR); // Black text
        return label;
    }

    private JTextArea createStyledTextArea() {
        JTextArea textArea = new JTextArea(3, 30);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setForeground(TEXT_COLOR); // Black text
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return textArea;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setForeground(TEXT_COLOR); // Black text
        comboBox.setBackground(WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return comboBox;
    }

    private void styleButton(JButton button, Color bgColor) {

        /* ---------- 1. use a plain UI so our colours show ---------- */
        button.setUI(new BasicButtonUI());        // no Windows gradient
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);

        /* ---------- 2. font & border ---------- */
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));

        /* ---------- 3. live colour handling ---------- */
        // initial colours
        button.setBackground(bgColor);
        button.setForeground(getContrastColor(bgColor));

        // update when the model (hover / press / disable) changes
        button.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ButtonModel m = (ButtonModel) e.getSource();

                // Lighten the background a bit while hovering, darken when pressed
                Color base = bgColor;
                if (m.isPressed()) base = base.darker();
                else if (m.isRollover()) base = base.brighter();

                button.setBackground(base);
                button.setForeground(getContrastColor(base));
            }
        });

        /* ---------- 4. quality‑of‑life ---------- */
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


    private Color getContrastColor(Color c) {
        double y = 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue(); // perceived luminance
        return y > 160 ? Color.BLACK : Color.WHITE;
    }

    // Original functional methods remain exactly the same
    private void refreshAppointments() {
        appointmentMap.clear();
        appointmentIdToPatientIdMap.clear(); // Clear patient ID map
        appointmentCombo.removeAllItems();

        String selectedStatusFilter = (String) appointmentStatusFilterCombo.getSelectedItem();
        String statusQueryParam = "";
        if (!"All".equals(selectedStatusFilter)) {
            // The DB query already filters out 'completed' and 'cancelled' for doctor appointments
            // So, we only need to filter if the user explicitly selects 'Confirmed', 'Pending', or 'Rescheduled'
            // The current DB query for getDoctorAppointments already handles this implicitly by NOT IN ('completed', 'cancelled')
            // If we want to strictly filter by selected status, we would need to modify the DB query or filter here.
            // For now, let's assume the existing DB query is sufficient and the filter just narrows down the displayed list.
            // If the DB query is changed to return all statuses, then this filter logic would be more critical.
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/appointments?doctorId=" + currentDoctorId))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String status = obj.getString("status");

                    // Apply filter based on selected status
                    if ("All".equals(selectedStatusFilter) || status.equalsIgnoreCase(selectedStatusFilter)) {
                        String info = String.format("ID: %d - %s - %s %s - Status: %s",
                                obj.getInt("id"),
                                obj.getString("patientName"),
                                obj.getString("date"),
                                obj.getString("time"),
                                status);

                        appointmentCombo.addItem(info);
                        appointmentMap.put(info, obj.getInt("id"));
                        // This line will now work correctly because patientId is added in DBConnection.java
                        appointmentIdToPatientIdMap.put(obj.getInt("id"), obj.getInt("patientId")); // Store patient ID
                    }
                }

                if (appointmentMap.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "No upcoming appointments available for treatment entry with the selected filter.",
                            "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private void loadPatientRecords(String searchTerm) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Build the request URI with the search term
            URI uri;
            if (searchTerm.isEmpty()) {
                uri = URI.create("http://localhost:8000/api/all-records");
            } else {
                uri = URI.create("http://localhost:8000/api/all-records?search=" +
                        URLEncoder.encode(searchTerm, "UTF-8"));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                DefaultTableModel model = new DefaultTableModel(
                        new Object[]{"Patient", "Date", "Time", "Diagnosis", "Treatment", "Medication", "Notes", "Doctor"}, 0);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    // Only add rows that match the search term (case insensitive)
                    if (searchTerm.isEmpty() ||
                            obj.getString("patientName").toLowerCase().contains(searchTerm.toLowerCase())) {
                        model.addRow(new Object[]{
                                obj.getString("patientName"),
                                obj.getString("date"),
                                obj.getString("time"),
                                obj.getString("diagnosis"),
                                obj.getString("treatmentType"),
                                obj.getString("medication"),
                                obj.optString("notes", ""),
                                obj.getString("doctorName")
                        });
                    }
                }

                if (model.getRowCount() == 0 && !searchTerm.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No records found for: " + searchTerm,
                            "No Results", JOptionPane.INFORMATION_MESSAGE);
                }

                recordsTable.setModel(model);
            } else {
                throw new IOException("Server returned status: " + response.statusCode());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading records: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void submitTreatment() {
        String selected = (String) appointmentCombo.getSelectedItem();
        if (selected == null || selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an appointment",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int appointmentId = appointmentMap.get(selected);
        String diagnosis = diagnosisArea.getText().trim();
        String treatmentType = (String) treatmentCombo.getSelectedItem();
        String medication = (String) medicationCombo.getSelectedItem();
        String notes = notesArea.getText().trim();

        if (diagnosis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Diagnosis is required",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("appointmentId", appointmentId);
            json.put("diagnosis", diagnosis);
            json.put("treatmentType", treatmentType);
            json.put("medication", medication);
            json.put("notes", notes);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/treatments"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                JOptionPane.showMessageDialog(this, "Treatment submitted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                diagnosisArea.setText("");
                notesArea.setText("");
                refreshAppointments(); // Refresh to show updated status
            } else {
                JOptionPane.showMessageDialog(this, "Error submitting treatment: " + response.body(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error submitting treatment: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateAppointmentStatus(String newStatus) {
        String selected = (String) appointmentCombo.getSelectedItem();
        if (selected == null || selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to update its status.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int appointmentId = appointmentMap.get(selected);

        try {
            JSONObject json = new JSONObject();
            json.put("status", newStatus);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/appointments/" + appointmentId + "/status"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(this, "Appointment status updated to " + newStatus + "!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAppointments(); // Refresh to show updated status
            } else {
                JOptionPane.showMessageDialog(this, "Error updating status: " + response.body(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // New Feature: View Patient Details
    private void viewSelectedPatientDetails() {
        String selected = (String) appointmentCombo.getSelectedItem();
        if (selected == null || selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to view patient details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int appointmentId = appointmentMap.get(selected);
        Integer patientId = appointmentIdToPatientIdMap.get(appointmentId);

        if (patientId == null) {
            JOptionPane.showMessageDialog(this, "Patient ID not found for the selected appointment.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/patients?id=" + patientId))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject patientData = new JSONObject(response.body());
                if (patientData.length() == 0) {
                    JOptionPane.showMessageDialog(this, "Patient details not found.",
                            "Information", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JDialog patientDetailsDialog = new JDialog(this, "Patient Details", true);
                patientDetailsDialog.setSize(400, 250);
                patientDetailsDialog.setLocationRelativeTo(this);
                patientDetailsDialog.setLayout(new GridLayout(5, 2, 10, 10));
                patientDetailsDialog.setBackground(LIGHT_BLUE);
                
                JDialog patientDetailsDialog1 = new JDialog(this, "Patient Details", true);
                patientDetailsDialog1.setSize(400, 250);
                patientDetailsDialog1.setLocationRelativeTo(this);

                JPanel contentPanel = new JPanel(new GridLayout(5, 2, 10, 10));
                contentPanel.setBackground(LIGHT_BLUE);
                contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                contentPanel.add(new JLabel("Name:"));
                contentPanel.add(new JLabel(patientData.optString("name", "N/A")));
                contentPanel.add(new JLabel("Email:"));
                contentPanel.add(new JLabel(patientData.optString("email", "N/A")));
                contentPanel.add(new JLabel("Date of Birth:"));
                contentPanel.add(new JLabel(patientData.optString("date_of_birth", "N/A")));
                contentPanel.add(new JLabel("Phone:"));
                contentPanel.add(new JLabel(patientData.optString("phone", "N/A")));

                JButton closeButton = new JButton("Close");
                styleButton(closeButton, new Color(100, 30, 30));
                closeButton.addActionListener(e -> patientDetailsDialog.dispose()); // Changed to patientDetailsDialog

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.setOpaque(false);
                buttonPanel.add(closeButton);

                contentPanel.add(buttonPanel);

                patientDetailsDialog.setContentPane(contentPanel); // Changed to patientDetailsDialog
                patientDetailsDialog.setVisible(true);

                // The following block seems to be a duplicate or an attempt to create another dialog.
                // It's likely redundant and might cause issues. I'm commenting it out.
                /*
                patientDetailsDialog1.add(new JLabel("Name:"));
                patientDetailsDialog1.add(new JLabel(patientData.optString("name", "N/A")));
                patientDetailsDialog1.add(new JLabel("Email:"));
                patientDetailsDialog1.add(new JLabel(patientData.optString("email", "N/A")));
                patientDetailsDialog1.add(new JLabel("Date of Birth:"));
                patientDetailsDialog1.add(new JLabel(patientData.optString("date_of_birth", "N/A")));
                patientDetailsDialog1.add(new JLabel("Phone:"));
                patientDetailsDialog1.add(new JLabel(patientData.optString("phone", "N/A")));

                JButton closeButton1 = new JButton("Close");
                styleButton(closeButton1, new Color(100, 30, 30));
                closeButton1.addActionListener(e -> patientDetailsDialog1.dispose());
                JPanel buttonPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanel1.setOpaque(false);
                buttonPanel1.add(closeButton1);
                patientDetailsDialog1.add(buttonPanel1);

                patientDetailsDialog1.setVisible(true);
                */

            } else {
                JOptionPane.showMessageDialog(this, "Error fetching patient details: " + response.body(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error viewing patient details: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // New Feature: Export Patient Records
    private void exportPatientRecords() {
        DefaultTableModel model = (DefaultTableModel) recordsTable.getModel();
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No records to export.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("DOCTOR PORTAL - PATIENT RECORDS\n");
            sb.append("Doctor ID: ").append(currentDoctorId).append("\n");
            sb.append("Export Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");

            // Append column headers
            for (int i = 0; i < model.getColumnCount(); i++) {
                sb.append(String.format("%-20s", model.getColumnName(i)));
            }
            sb.append("\n");
            sb.append("-".repeat(20 * model.getColumnCount())).append("\n");

            // Append data rows
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    sb.append(String.format("%-20s", model.getValueAt(i, j)));
                }
                sb.append("\n");
            }

            String filename = "doctor_" + currentDoctorId + "_patient_records_" +
                              new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";

            Files.write(Paths.get(filename), sb.toString().getBytes());

            JOptionPane.showMessageDialog(this, "Patient records exported to: " + filename,
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting records: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
