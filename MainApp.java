import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        try {
            // 1. Initialize files and database
            JSONHandler.initializeFiles();
            DBConnection.initializeDatabase();

            // 2. Start REST server with error handling
            new Thread(() -> {
                try {
                    ClinicRestServer.main(args);
                    System.out.println("REST server started successfully");
                } catch (Exception e) {
                    System.err.println("Failed to start REST server:");
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "REST Server Error: " + e.getMessage(),
                            "Server Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }).start();

            // 3. Add small delay to ensure server is up
            Thread.sleep(1000); // Give server a moment to start

            // 4. Start GUI
            SwingUtilities.invokeLater(() -> new LoginGUI());

        } catch (Exception e) {
            System.err.println("Application failed to initialize:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Initialization Error: " + e.getMessage(),
                    "Fatal Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
