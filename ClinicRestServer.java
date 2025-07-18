import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class ClinicRestServer {
    private static final int DEFAULT_PORT = 8000;
    private static final int MAX_PORT_ATTEMPTS = 10;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        HttpServer server = createServer(DEFAULT_PORT, MAX_PORT_ATTEMPTS);
        if (server == null) {
            System.err.println("Failed to start server: No available ports");
            return;
        }

        // Configure server contexts
        server.createContext("/api/health", wrapWithCors(new HealthHandler()));
        server.createContext("/api/treatments", wrapWithCors(new TreatmentsHandler()));
        server.createContext("/api/doctors", wrapWithCors(new DoctorsHandler()));
        server.createContext("/api/patients", wrapWithCors(new PatientsHandler()));
        server.createContext("/api/records", wrapWithCors(new RecordsHandler()));
        server.createContext("/api/all-records", wrapWithCors(new AllRecordsHandler()));
        // This context handles /api/appointments (GET, POST) and /api/appointments/{id} (DELETE)
        server.createContext("/api/appointments", wrapWithCors(new AppointmentsHandler()));
        // This context should only handle rescheduling requests
        server.createContext("/api/appointments/reschedule", wrapWithCors(new RescheduleAppointmentHandler()));
        // This context handles requests like /api/appointments/{id}/status
        server.createContext("/api/appointments/status", wrapWithCors(new AppointmentStatusHandler()));
        server.setExecutor(threadPool);
        server.start();

        System.out.println("Server started on port " + server.getAddress().getPort());
        System.out.println("Available endpoints:");
        System.out.println("- GET /api/health");
        System.out.println("- GET /api/doctors");
        System.out.println("- GET /api/patients");
        System.out.println("- GET /api/appointments?patientId={id}");
        System.out.println("- GET /api/appointments?doctorId={id}");
        System.out.println("- GET /api/appointments?doctorId={id}&date={date}&time={time}"); // For conflict check
        System.out.println("- POST /api/appointments");
        System.out.println("- PUT /api/appointments/{id}/status"); // New
        System.out.println("- PUT /api/appointments/{id}/reschedule"); // New
        System.out.println("- DELETE /api/appointments/{id}"); // Added for clarity
        System.out.println("- POST /api/treatments");
        System.out.println("- GET /api/records?doctorId={id}");
        System.out.println("- GET /api/all-records");
        System.out.println("- GET /api/all-records?search={searchTerm}");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop(1);
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }));
    }

    private static HttpServer createServer(int startPort, int maxAttempts) throws IOException {
        for (int port = startPort; port < startPort + maxAttempts; port++) {
            try {
                return HttpServer.create(new InetSocketAddress(port), 0);
            } catch (BindException e) {
                System.out.println("Port " + port + " is in use, trying next port...");
            }
        }
        return null;
    }

    private static HttpHandler wrapWithCors(HttpHandler handler) {
        return exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            handler.handle(exchange);
        };
    }

    static class AllRecordsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;

            try {
                if ("GET".equals(method)) {
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    if (params.containsKey("search")) {
                        String searchTerm = params.get("search");
                        response = DBConnection.searchPatientRecords(searchTerm);
                    } else {
                        response = DBConnection.getAllPatientRecords();
                    }
                } else {
                    throw new UnsupportedOperationException("Method not allowed");
                }
            } catch (Exception e) {
                response = createErrorResponse(e);
                statusCode = determineStatusCode(e);
            }

            sendResponse(exchange, response, statusCode);
        }
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JSONObject response = null;
            try {
                response = new JSONObject()
                        .put("status", "ok")
                        .put("timestamp", System.currentTimeMillis());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendResponse(exchange, response.toString(), 200);
        }
    }

    static class RecordsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;

            try {
                if ("GET".equals(method)) {
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    if (params.containsKey("doctorId")) {
                        int doctorId = Integer.parseInt(params.get("doctorId"));
                        response = DBConnection.getPatientRecordsForDoctor(doctorId);
                    } else if (params.containsKey("patientId")) { // New: Get records for a specific patient
                        int patientId = Integer.parseInt(params.get("patientId"));
                        response = DBConnection.getPatientRecords(patientId);
                    } else {
                        throw new IllegalArgumentException("Missing doctorId or patientId parameter");
                    }
                } else {
                    throw new UnsupportedOperationException("Method not allowed");
                }
            } catch (Exception e) {
                response = createErrorResponse(e);
                statusCode = determineStatusCode(e);
            }

            sendResponse(exchange, response, statusCode);
        }
    }

    static class AppointmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;

            try {
                if ("GET".equals(method)) {
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

                    if (params.containsKey("patientId")) {
                        int patientId = Integer.parseInt(params.get("patientId"));
                        response = DBConnection.getPatientAppointments(patientId);
                    } else if (params.containsKey("doctorId")) {
                        int doctorId = Integer.parseInt(params.get("doctorId"));
                        String date = params.get("date");
                        String time = params.get("time");
                        if (date != null && time != null) {
                            response = DBConnection.checkAppointmentConflict(doctorId, date, time);
                        } else {
                            response = DBConnection.getDoctorAppointments(doctorId);
                        }
                    } else {
                        throw new IllegalArgumentException("Missing patientId or doctorId parameter");
                    }
                } else if ("POST".equals(method)) {
                    JSONObject json = parseRequestBody(exchange);
                    validateAppointmentFields(json);

                    DBConnection.insertAppointment(
                            json.getInt("patientId"),
                            json.getInt("doctorId"),
                            json.getString("date"),
                            json.getString("time")
                    );
                    response = new JSONObject().put("status", "success").toString();
                    statusCode = 201;
                } else if ("DELETE".equals(method)) {
                    // Extract appointment ID from path
                    String path = exchange.getRequestURI().getPath();
                    String[] pathSegments = path.split("/");
                    
                    // Expected format: /api/appointments/{id}
                    if (pathSegments.length != 4) {
                        throw new IllegalArgumentException("Invalid URL format for deletion. Expected: /api/appointments/{id}");
                    }
                    
                    int appointmentId = Integer.parseInt(pathSegments[3]);
                    
                    // Ensure the appointment exists before attempting to cancel
                    if (!DBConnection.appointmentExists(appointmentId)) {
                        throw new IllegalArgumentException("Appointment with ID " + appointmentId + " does not exist");
                    }

                    // Cancel the appointment
                    if (DBConnection.cancelAppointment(appointmentId)) {
                        response = new JSONObject()
                                .put("status", "success")
                                .put("message", "Appointment cancelled successfully")
                                .toString();
                    } else {
                        throw new SQLException("Failed to cancel appointment in database");
                    }
                } else {
                    throw new UnsupportedOperationException("Method not allowed");
                }
            } catch (Exception e) {
                response = createErrorResponse(e);
                statusCode = determineStatusCode(e);
            }

            sendResponse(exchange, response, statusCode);
        }

        private void validateAppointmentFields(JSONObject json) {
            if (!json.has("patientId") || !json.has("doctorId") ||
                    !json.has("date") || !json.has("time")) {
                throw new IllegalArgumentException("Missing required fields");
            }
        }
    }


    // New Handler for Rescheduling Appointments
    static class RescheduleAppointmentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;

            try {
                String path = exchange.getRequestURI().getPath();

                // FIX: Ensure this handler only processes paths ending with "/reschedule"
                // This prevents it from incorrectly handling DELETE requests for /api/appointments/{id}
                if (!path.endsWith("/reschedule")) {
                    // If the path doesn't end with /reschedule, this handler should not process it.
                    // This allows the AppointmentsHandler to handle plain /api/appointments/{id} paths.
                    // We send a 404 Not Found or 400 Bad Request, or simply let the request fall through
                    // if there were other contexts that could match. For HttpServer, returning
                    // without sending a response might lead to a hanging connection, so sending an error is safer.
                    sendResponse(exchange, createErrorResponse(new IllegalArgumentException("Invalid path for reschedule operation.")), 400);
                    return;
                }

                if ("PUT".equals(method)) {
                    // Extract appointment ID from path (e.g., /api/appointments/{id}/reschedule)
                    String[] pathSegments = path.split("/");
                    // This logic assumes the path is /api/appointments/{id}/reschedule
                    // So, pathSegments[3] should be the ID.
                    if (pathSegments.length < 4) { // Expected: /api/appointments/{id}/reschedule
                        throw new IllegalArgumentException("Invalid URL for reschedule request.");
                    }
                    int appointmentId = Integer.parseInt(pathSegments[3]); // Assuming ID is the 4th segment

                    JSONObject json = parseRequestBody(exchange);
                    String newDate = json.getString("date");
                    String newTime = json.getString("time");

                    if (DBConnection.rescheduleAppointment(appointmentId, newDate, newTime)) {
                        response = new JSONObject().put("status", "success").put("message", "Appointment rescheduled successfully.").toString();
                        statusCode = 200;
                    } else {
                        throw new SQLException("Failed to reschedule appointment in database.");
                    }
                } else {
                    throw new UnsupportedOperationException("Method not allowed");
                }
            } catch (Exception e) {
                response = createErrorResponse(e);
                statusCode = determineStatusCode(e);
            }

            sendResponse(exchange, response, statusCode);
        }
    }


    // New Handler for Updating Appointment Status
    static class AppointmentStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;

            try {
                if ("PUT".equals(method)) {
                    // Extract appointment ID from path (e.g., /api/appointments/{id}/status)
                    String path = exchange.getRequestURI().getPath();
                    String[] pathSegments = path.split("/");
                    if (pathSegments.length < 4) { // Expected: /api/appointments/{id}/status
                        throw new IllegalArgumentException("Invalid URL for status update request.");
                    }
                    int appointmentId = Integer.parseInt(pathSegments[3]); // Assuming ID is the 4th segment

                    JSONObject json = parseRequestBody(exchange);
                    String newStatus = json.getString("status");

                    if (DBConnection.updateAppointmentStatus(appointmentId, newStatus)) {
                        response = new JSONObject().put("status", "success").put("message", "Appointment status updated successfully.").toString();
                        statusCode = 200;
                    } else {
                        throw new SQLException("Failed to update appointment status in database.");
                    }
                } else {
                    throw new UnsupportedOperationException("Method not allowed");
                }
            } catch (Exception e) {
                response = createErrorResponse(e);
                statusCode = determineStatusCode(e);
            }

            sendResponse(exchange, response, statusCode);
        }
    }


    static class TreatmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "";
            int statusCode = 200;

            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    JSONObject json = parseRequestBody(exchange);
                    validateTreatmentFields(json);

                    DBConnection.insertTreatment(
                            json.getInt("appointmentId"),
                            json.getString("diagnosis"),
                            json.getString("treatmentType"),
                            json.getString("medication"),
                            json.optString("notes", "")
                    );
                    response = new JSONObject().put("status", "success").toString();
                    statusCode = 201;
                } else if ("GET".equals(exchange.getRequestMethod())) { // New: Get treatments by patientId
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    if (params.containsKey("patientId")) {
                        int patientId = Integer.parseInt(params.get("patientId"));
                        response = DBConnection.getPatientRecords(patientId); // Reusing getPatientRecords for this
                    } else {
                        throw new IllegalArgumentException("Missing patientId parameter for GET treatments.");
                    }
                } else {
                    throw new UnsupportedOperationException("Method not allowed");
                }
            } catch (Exception e) {
                response = createErrorResponse(e);
                statusCode = determineStatusCode(e);
            }

            sendResponse(exchange, response, statusCode);
        }

        private void validateTreatmentFields(JSONObject json) {
            if (!json.has("appointmentId") || !json.has("diagnosis") ||
                    !json.has("treatmentType") || !json.has("medication")) {
                throw new IllegalArgumentException("Missing required fields");
            }
        }
    }

    static class DoctorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "";
            int statusCode = 200;

            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    response = DBConnection.getAllDoctors();
                } else {
                    throw new UnsupportedOperationException("Method not allowed");
                }
            } catch (Exception e) {
                response = createErrorResponse(e);
                statusCode = determineStatusCode(e);
            }

            sendResponse(exchange, response, statusCode);
        }
    }

    static class PatientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "";
            int statusCode = 200;

            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                    if (params.containsKey("id")) { // New: Get patient by ID
                        int patientId = Integer.parseInt(params.get("id"));
                        response = DBConnection.getPatientById(patientId);
                    } else {
                        response = DBConnection.getAllPatients();
                    }
                } else if ("PUT".equals(exchange.getRequestMethod())) { // New: Update patient profile
                    String path = exchange.getRequestURI().getPath();
                    String[] pathSegments = path.split("/");
                    if (pathSegments.length < 4) { // Expected: /api/patients/{id}/profile
                        throw new IllegalArgumentException("Invalid URL for patient profile update.");
                    }
                    int patientId = Integer.parseInt(pathSegments[3]); // Assuming ID is the 4th segment

                    JSONObject json = parseRequestBody(exchange);
                    String name = json.optString("name");
                    String email = json.optString("email");
                    String phone = json.optString("phone");
                    String dob = json.optString("date_of_birth");

                    DBConnection.updatePatientProfile(patientId, name, email, phone, dob);
                    response = new JSONObject().put("status", "success").put("message", "Patient profile updated successfully.").toString();
                    statusCode = 200;
                } else {
                    throw new UnsupportedOperationException("Method not allowed");
                }
            } catch (Exception e) {
                response = createErrorResponse(e);
                statusCode = determineStatusCode(e);
            }

            sendResponse(exchange, response, statusCode);
        }
    }

    // Utility methods
    private static JSONObject parseRequestBody(HttpExchange exchange) throws IOException, JSONException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return new JSONObject(requestBody);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            }
        }
        return result;
    }

    private static String createErrorResponse(Exception e) {
        try {
            return new JSONObject()
                    .put("error", e.getMessage() != null ? e.getMessage() : "Unknown error")
                    .put("type", e.getClass().getSimpleName())
                    .toString();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private static int determineStatusCode(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return 400;
        } else if (e instanceof UnsupportedOperationException) {
            return 405;
        } else if (e instanceof SQLException) {
            return 503;
        }
        return 500;
    }

    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
