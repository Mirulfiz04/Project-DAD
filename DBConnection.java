import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.*;

public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/clinicdb";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    public static void initializeDatabase() {
        // Initialize JSON files first
        JSONHandler.initializeFiles();

        try (Connection conn = getConnection()) {
            createTables(conn);
            updateFromJson(conn);
            // Initial sync from DB to JSON
            JSONHandler.syncAllToJSON();
            System.out.println("Database initialized and updated successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed:");
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String[] createTables = {
                "CREATE TABLE IF NOT EXISTS doctors (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) UNIQUE NOT NULL, " +
                        "password VARCHAR(100) NOT NULL, " +
                        "specialization VARCHAR(100))",

                "CREATE TABLE IF NOT EXISTS patients (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) UNIQUE NOT NULL, " +
                        "password VARCHAR(100) NOT NULL, " +
                        "date_of_birth DATE, " +
                        "phone VARCHAR(20))",

                "CREATE TABLE IF NOT EXISTS appointments (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "patient_id INT NOT NULL, " +
                        "doctor_id INT NOT NULL, " +
                        "date DATE NOT NULL, " +
                        "time TIME NOT NULL, " +
                        "status ENUM('pending','confirmed','completed','cancelled','rescheduled') DEFAULT 'pending'," + // Added 'rescheduled'
                        "FOREIGN KEY (patient_id) REFERENCES patients(id)," +
                        "FOREIGN KEY (doctor_id) REFERENCES doctors(id))",

                "CREATE TABLE IF NOT EXISTS treatments (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "appointment_id INT NOT NULL, " +
                        "diagnosis TEXT NOT NULL, " +
                        "treatment_type VARCHAR(100) NOT NULL, " +
                        "medication VARCHAR(100) NOT NULL, " +
                        "notes TEXT, " +
                        "FOREIGN KEY (appointment_id) REFERENCES appointments(id))"
        };

        for (String sql : createTables) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
    }

    private static void updateFromJson(Connection conn) throws SQLException {
        updateDoctorsFromJson(conn);
        updatePatientsFromJson(conn);
        updateAppointmentsFromJson(conn);
        updateTreatmentsFromJson(conn);
    }

    private static void updateDoctorsFromJson(Connection conn) throws SQLException {
        List<Map<String, Object>> doctors = JSONHandler.loadDoctors();
        Set<String> existingEmails = getExistingEmails(conn, "doctors");

        String sql = "INSERT INTO doctors (name, email, password, specialization) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), specialization=VALUES(specialization)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map<String, Object> doctor : doctors) {
                String email = (String) doctor.get("email");
                if (!existingEmails.contains(email)) {
                    pstmt.setString(1, (String) doctor.get("name"));
                    pstmt.setString(2, email);
                    pstmt.setString(3, (String) doctor.get("password"));
                    pstmt.setString(4, (String) doctor.get("specialization"));
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }
    }

    private static void updateTreatmentsFromJson(Connection conn) throws SQLException {
        List<Map<String, Object>> treatments = JSONHandler.loadTreatments();
        Set<Integer> existingAppointmentIds = getExistingTreatmentAppointmentIds(conn);

        String sql = "INSERT INTO treatments (appointment_id, diagnosis, treatment_type, medication, notes) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "diagnosis=VALUES(diagnosis), treatment_type=VALUES(treatment_type), " +
                "medication=VALUES(medication), notes=VALUES(notes)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map<String, Object> treatment : treatments) {
                int appointmentId = (Integer) treatment.get("appointmentId");
                if (!existingAppointmentIds.contains(appointmentId)) {
                    pstmt.setInt(1, appointmentId);
                    pstmt.setString(2, (String) treatment.get("diagnosis"));
                    pstmt.setString(3, (String) treatment.get("treatmentType"));
                    pstmt.setString(4, (String) treatment.get("medication"));
                    pstmt.setString(5, (String) treatment.get("notes"));
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }
    }

    private static Set<Integer> getExistingTreatmentAppointmentIds(Connection conn) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT appointment_id FROM treatments";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getInt("appointment_id"));
            }
        }
        return ids;
    }

    private static void updatePatientsFromJson(Connection conn) throws SQLException {
        List<Map<String, Object>> patients = JSONHandler.loadPatients();
        Set<String> existingEmails = getExistingEmails(conn, "patients");

        String sql = "INSERT INTO patients (name, email, password, date_of_birth, phone) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), date_of_birth=VALUES(date_of_birth), phone=VALUES(phone)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map<String, Object> patient : patients) {
                String email = (String) patient.get("email");
                if (!existingEmails.contains(email)) {
                    pstmt.setString(1, (String) patient.get("name"));
                    pstmt.setString(2, email);
                    pstmt.setString(3, (String) patient.get("password"));
                    pstmt.setString(4, (String) patient.get("date_of_birth"));
                    pstmt.setString(5, (String) patient.get("phone"));
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }
    }

    private static void updateAppointmentsFromJson(Connection conn) throws SQLException {
        List<Map<String, Object>> appointments = JSONHandler.loadAppointments();
        Set<String> existingAppointments = getExistingAppointments(conn);

        String sql = "INSERT INTO appointments (patient_id, doctor_id, date, time, status) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE status=VALUES(status)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map<String, Object> app : appointments) {
                String appointmentKey = app.get("patientId") + "-" +
                        app.get("doctorId") + "-" +
                        app.get("date") + "-" +
                        app.get("time");

                if (!existingAppointments.contains(appointmentKey)) {
                    pstmt.setInt(1, (Integer) app.get("patientId"));
                    pstmt.setInt(2, (Integer) app.get("doctorId"));
                    pstmt.setString(3, (String) app.get("date"));
                    pstmt.setString(4, (String) app.get("time"));
                    pstmt.setString(5, (String) app.getOrDefault("status", "confirmed"));
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }
    }

    private static Set<String> getExistingEmails(Connection conn, String table) throws SQLException {
        Set<String> emails = new HashSet<>();
        String sql = "SELECT email FROM " + table;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                emails.add(rs.getString("email"));
            }
        }
        return emails;
    }

    private static Set<String> getExistingAppointments(Connection conn) throws SQLException {
        Set<String> appointments = new HashSet<>();
        String sql = "SELECT CONCAT(patient_id, '-', doctor_id, '-', date, '-', time) as appointment_key FROM appointments";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                appointments.add(rs.getString("appointment_key"));
            }
        }
        return appointments;
    }

    // New methods for REST API support
    public static String getDoctorAppointments(int doctorId) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        try (Connection conn = getConnection()) {
            // MODIFIED: Added a.patient_id to the SELECT statement
            String sql = "SELECT a.id, a.patient_id, p.name as patientName, a.date, a.time, a.status " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "WHERE a.doctor_id = ? AND a.date >= ? " +
                    "AND a.status NOT IN ('completed', 'cancelled') " +
                    "ORDER BY a.date, a.time";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorId);
            stmt.setDate(2, new java.sql.Date(System.currentTimeMillis()));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("id", rs.getInt("id"));
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("patientName", rs.getString("patientName"));
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
                // ADDED: Put patientId into the JSONObject
                try {
                    obj.put("patientId", rs.getInt("patient_id"));
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("date", rs.getDate("date").toString());
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("time", rs.getTime("time").toString());
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("status", rs.getString("status"));
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
                jsonArray.put(obj);
            }
        }
        return jsonArray.toString();
    }

    public static String getAllPatients() throws SQLException {
        JSONArray jsonArray = new JSONArray();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, email FROM patients");
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("id", rs.getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("name", rs.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("email", rs.getString("email"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                jsonArray.put(obj);
            }
        }
        return jsonArray.toString();
    }

    public static String getPatientById(int patientId) throws SQLException {
        JSONObject obj = null;
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, name, email, date_of_birth, phone FROM patients WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                obj = new JSONObject();
                try {
                    obj.put("id", rs.getInt("id"));
                    obj.put("name", rs.getString("name"));
                    obj.put("email", rs.getString("email"));
                    obj.put("date_of_birth", rs.getString("date_of_birth"));
                    obj.put("phone", rs.getString("phone"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj != null ? obj.toString() : new JSONObject().toString();
    }


    public static String getAllDoctors() throws SQLException {
        JSONArray jsonArray = new JSONArray();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM doctors");
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("id", rs.getInt("id"));
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("name", rs.getString("name"));
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
                jsonArray.put(obj);
            }
        }
        return jsonArray.toString();
    }

    public static String getPatientAppointments(int patientId) throws SQLException {
        String sql = """
                SELECT a.id, d.name AS doctorName, a.date, a.time, a.status
                FROM appointments a
                JOIN doctors d ON a.doctor_id = d.id
                WHERE a.patient_id = ?
                ORDER BY a.date DESC, a.time DESC
                """;

        JSONArray array = new JSONArray();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("id", rs.getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("doctorName", rs.getString("doctorName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("date", rs.getString("date"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("time", rs.getString("time"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("status", rs.getString("status"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                array.put(obj);
            }
        }
        return array.toString();
    }

    public static String checkAppointmentConflict(int doctorId, String date, String time) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        try (Connection conn = getConnection()) {
            String sql = "SELECT id FROM appointments WHERE doctor_id = ? AND date = ? AND time = ? AND status NOT IN ('cancelled', 'completed')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorId);
            stmt.setString(2, date);
            stmt.setString(3, time);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("id", rs.getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(obj);
            }
        }
        return jsonArray.toString();
    }


    public static void insertAppointment(int patientId, int doctorId, String date, String time) throws SQLException {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO appointments (patient_id, doctor_id, date, time, status) " +
                    "VALUES (?, ?, ?, ?, 'confirmed')";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, date);
            stmt.setString(4, time);
            stmt.executeUpdate();

            JSONHandler.syncAppointments();
        }
    }

    public static void insertTreatment(int appointmentId, String diagnosis,
                                       String treatmentType, String medication, String notes) throws SQLException {
        try (Connection conn = getConnection()) {
            // Check if treatment exists
            boolean exists = false;
            String checkSql = "SELECT id FROM treatments WHERE appointment_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, appointmentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            String sql = exists ?
                    "UPDATE treatments SET diagnosis=?, treatment_type=?, medication=?, notes=? WHERE appointment_id=?" :
                    "INSERT INTO treatments (appointment_id, diagnosis, treatment_type, medication, notes) VALUES (?,?,?,?,?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (exists) {
                    stmt.setString(1, diagnosis);
                    stmt.setString(2, treatmentType);
                    stmt.setString(3, medication);
                    stmt.setString(4, notes);
                    stmt.setInt(5, appointmentId);
                } else {
                    stmt.setInt(1, appointmentId);
                    stmt.setString(2, diagnosis);
                    stmt.setString(3, treatmentType);
                    stmt.setString(4, medication);
                    stmt.setString(5, notes);
                }
                stmt.executeUpdate();
            }

            // Update appointment status to completed
            String updateSql = "UPDATE appointments SET status='completed' WHERE id=?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, appointmentId);
                updateStmt.executeUpdate();
            }

            JSONHandler.syncAppointments();
            JSONHandler.syncTreatments(); // Sync treatments as well
        }
    }

    public static boolean rescheduleAppointment(int appointmentId, String newDate, String newTime) throws SQLException {
        String sql = "UPDATE appointments SET date = ?, time = ?, status = 'Rescheduled' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newDate);
            pstmt.setString(2, newTime);
            pstmt.setInt(3, appointmentId);
            int updatedRows = pstmt.executeUpdate();

            if (updatedRows > 0) {
                JSONHandler.syncAppointments();  // Update JSON after DB update
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error rescheduling appointment: " + e.getMessage());
            throw e; // Re-throw to be caught by the handler
        }
        return false;
    }

    public static boolean cancelAppointment(int appointmentId) throws SQLException {
        String sql = "UPDATE appointments SET status = 'cancelled' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            int updatedRows = pstmt.executeUpdate();

            if (updatedRows > 0) {
                JSONHandler.syncAppointments();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
            throw e;
        }
        return false;
    }

    public static boolean updateAppointmentStatus(int appointmentId, String newStatus) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, appointmentId);
            int updatedRows = pstmt.executeUpdate();

            if (updatedRows > 0) {
                JSONHandler.syncAppointments();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating appointment status: " + e.getMessage());
            throw e;
        }
        return false;
    }

    public static boolean updatePatientProfile(int patientId, String name, String email, String phone, String dob) throws SQLException {
        String sql = "UPDATE patients SET name = ?, email = ?, phone = ?, date_of_birth = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, dob);
            pstmt.setInt(5, patientId);
            int updatedRows = pstmt.executeUpdate();

            if (updatedRows > 0) {
                JSONHandler.syncPatients();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating patient profile: " + e.getMessage());
            throw e;
        }
        return false;
    }


    public static String searchPatientRecords(String searchTerm) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        try (Connection conn = getConnection()) {
            String sql = "SELECT p.name AS patientName, a.date, a.time, " +
                    "t.diagnosis, t.treatment_type, t.medication, t.notes, d.name AS doctorName " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN doctors d ON a.doctor_id = d.id " +
                    "JOIN treatments t ON t.appointment_id = a.id " +
                    "WHERE a.status = 'completed' AND p.name LIKE ? " +
                    "ORDER BY a.date DESC, a.time DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("patientName", rs.getString("patientName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("date", rs.getString("date"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("time", rs.getString("time"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("diagnosis", rs.getString("diagnosis"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("treatmentType", rs.getString("treatment_type"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("medication", rs.getString("medication"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("notes", rs.getString("notes"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("doctorName", rs.getString("doctorName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                jsonArray.put(obj);
            }
        }
        return jsonArray.toString();
    }

    public static String getPatientRecordsForDoctor(int doctorId) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        try (Connection conn = getConnection()) {
            String sql = "SELECT p.name AS patientName, a.date, a.time, " +
                    "t.diagnosis, t.treatment_type, t.medication, t.notes, d.name AS doctorName " + // Added notes
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN doctors d ON a.doctor_id = d.id " +
                    "JOIN treatments t ON t.appointment_id = a.id " +
                    "WHERE a.doctor_id = ? AND a.status = 'completed' " +
                    "ORDER BY a.date DESC, a.time DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("patientName", rs.getString("patientName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("date", rs.getString("date"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("time", rs.getString("time"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("diagnosis", rs.getString("diagnosis"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("treatmentType", rs.getString("treatment_type"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("medication", rs.getString("medication"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("notes", rs.getString("notes")); // Added notes
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("doctorName", rs.getString("doctorName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                jsonArray.put(obj);
            }
        }
        return jsonArray.toString();
    }

    public static String getPatientRecords(int patientId) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        try (Connection conn = getConnection()) {
            String sql = "SELECT p.name AS patientName, a.date, a.time, " +
                    "t.diagnosis, t.treatment_type, t.medication, t.notes, d.name AS doctorName " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN doctors d ON a.doctor_id = d.id " +
                    "JOIN treatments t ON t.appointment_id = a.id " +
                    "WHERE p.id = ? AND a.status = 'completed' " +
                    "ORDER BY a.date DESC, a.time DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("patientName", rs.getString("patientName"));
                    obj.put("date", rs.getString("date"));
                    obj.put("time", rs.getString("time"));
                    obj.put("diagnosis", rs.getString("diagnosis"));
                    obj.put("treatmentType", rs.getString("treatment_type"));
                    obj.put("medication", rs.getString("medication"));
                    obj.put("notes", rs.getString("notes"));
                    obj.put("doctorName", rs.getString("doctorName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(obj);
            }
        }
        return jsonArray.toString();
    }


    public static String getAllPatientRecords() throws SQLException {
        JSONArray jsonArray = new JSONArray();
        try (Connection conn = getConnection()) {
            String sql = "SELECT p.name AS patientName, a.date, a.time, " +
                    "t.diagnosis, t.treatment_type, t.medication, t.notes, d.name AS doctorName " + // Added notes
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN doctors d ON a.doctor_id = d.id " +
                    "JOIN treatments t ON t.appointment_id = a.id " +
                    "WHERE a.status = 'completed' " +  // Only show completed appointments
                    "ORDER BY a.date DESC, a.time DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("patientName", rs.getString("patientName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("date", rs.getString("date"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("time", rs.getString("time"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("diagnosis", rs.getString("diagnosis"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("treatmentType", rs.getString("treatment_type"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("medication", rs.getString("medication"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("notes", rs.getString("notes")); // Added notes
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    obj.put("doctorName", rs.getString("doctorName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                jsonArray.put(obj);
            }
        }
        return jsonArray.toString();
    }

	public static boolean appointmentExists(int appointmentId) throws SQLException {
    // Implement database check here
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(
             "SELECT 1 FROM appointments WHERE id = ?")) {
        stmt.setInt(1, appointmentId);
        return stmt.executeQuery().next();
    }
}
}
