import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public class JSONHandler {
    private static final String DOCTORS_JSON = "doctors.json";
    private static final String PATIENTS_JSON = "patients.json";
    private static final String APPOINTMENTS_JSON = "appointments.json";
    private static final String TREATMENTS_JSON = "treatments.json";
    private static final String RESCHEDULE_JSON = "reschedule_requests.json"; // Added for reschedule requests

    // Initialize JSON files if they don't exist
    public static void initializeFiles() {
        createFileIfNotExists(DOCTORS_JSON);
        createFileIfNotExists(PATIENTS_JSON);
        createFileIfNotExists(APPOINTMENTS_JSON);
        createFileIfNotExists(TREATMENTS_JSON); // Ensure treatments.json is created
        createFileIfNotExists(RESCHEDULE_JSON); // Ensure reschedule_requests.json is created
    }

    private static void createFileIfNotExists(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("[]"); // Empty JSON array
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Database to JSON synchronization
    public static void syncAllToJSON() {
        syncDoctors();
        syncPatients();
        syncAppointments();
        syncTreatments();
    }

    // Doctor operations
    public static void syncDoctors() {
        try (Connection conn = DBConnection.getConnection()) {
            JSONArray jsonArray = new JSONArray();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM doctors");

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id", rs.getInt("id"));
                obj.put("name", rs.getString("name"));
                obj.put("email", rs.getString("email"));
                obj.put("password", rs.getString("password"));
                obj.put("specialization", rs.getString("specialization"));
                jsonArray.put(obj);
            }
            writeJSONFile(DOCTORS_JSON, jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Patient operations
    public static void syncPatients() {
        try (Connection conn = DBConnection.getConnection()) {
            JSONArray jsonArray = new JSONArray();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM patients");

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id", rs.getInt("id"));
                obj.put("name", rs.getString("name"));
                obj.put("email", rs.getString("email"));
                obj.put("password", rs.getString("password"));
                obj.put("date_of_birth", rs.getString("date_of_birth"));
                obj.put("phone", rs.getString("phone"));
                jsonArray.put(obj);
            }
            writeJSONFile(PATIENTS_JSON, jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncTreatments() {
        try (Connection conn = DBConnection.getConnection()) {
            JSONArray jsonArray = new JSONArray();
            String sql = "SELECT t.*, a.patient_id, a.doctor_id, a.date, a.time " +
                    "FROM treatments t " +
                    "JOIN appointments a ON t.appointment_id = a.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("appointmentId", rs.getInt("appointment_id"));
                obj.put("patientId", rs.getInt("patient_id"));
                obj.put("doctorId", rs.getInt("doctor_id"));
                obj.put("date", rs.getString("date"));
                obj.put("time", rs.getString("time"));
                obj.put("diagnosis", rs.getString("diagnosis"));
                obj.put("treatmentType", rs.getString("treatment_type"));
                obj.put("medication", rs.getString("medication"));
                obj.put("notes", rs.getString("notes"));
                jsonArray.put(obj);
            }
            writeJSONFile(TREATMENTS_JSON, jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Appointment operations
    public static void syncAppointments() {
        try (Connection conn = DBConnection.getConnection()) {
            JSONArray jsonArray = new JSONArray();
            String sql = "SELECT a.id, a.patient_id, a.doctor_id, d.name as doctor_name, " +
                    "a.date, a.time, a.status " +
                    "FROM appointments a " +
                    "JOIN doctors d ON a.doctor_id = d.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id", rs.getInt("id")); // Added appointment ID
                obj.put("patientId", rs.getInt("patient_id"));
                obj.put("doctorId", rs.getInt("doctor_id"));
                obj.put("doctorName", rs.getString("doctor_name"));
                obj.put("date", rs.getString("date"));
                obj.put("time", rs.getString("time"));
                obj.put("status", rs.getString("status"));
                jsonArray.put(obj);
            }
            writeJSONFile(APPOINTMENTS_JSON, jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load data from JSON
    public static List<Map<String, Object>> loadDoctors() {
        return loadFromJSON(DOCTORS_JSON);
    }

    public static List<Map<String, Object>> loadPatients() {
        return loadFromJSON(PATIENTS_JSON);
    }

    public static List<Map<String, Object>> loadAppointments() {
        return loadFromJSON(APPOINTMENTS_JSON);
    }

    public static List<Map<String, Object>> loadTreatments() {
        return loadFromJSON(TREATMENTS_JSON);
    }

    private static List<Map<String, Object>> loadFromJSON(String filename) {
        List<Map<String, Object>> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            if (sb.length() > 0) {
                JSONArray jsonArray = new JSONArray(sb.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Map<String, Object> item = new HashMap<>();
                    Iterator<String> keys = obj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        item.put(key, obj.get(key));
                    }
                    data.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // Helper method to write JSON files
    private static void writeJSONFile(String filename, JSONArray jsonArray) throws JSONException {
        try (FileWriter file = new FileWriter(filename)) {
            file.write(jsonArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Reschedule Request operations (moved from DBConnection to JSONHandler for file-based storage)
    public static void submitRescheduleRequest(JSONObject request) throws JSONException {
        try {
            File file = new File(RESCHEDULE_JSON);
            JSONArray array = file.exists() ? new JSONArray(new String(Files.readAllBytes(file.toPath()))) : new JSONArray();
            array.put(request);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(array.toString(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray loadAllRescheduleRequests() throws JSONException {
        try {
            File file = new File(RESCHEDULE_JSON);
            if (!file.exists()) return new JSONArray();
            return new JSONArray(new String(Files.readAllBytes(file.toPath())));
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
}
