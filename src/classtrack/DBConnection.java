/*
 * ============================================================
 * ClassTrack - Student Attendance Management System
 * File: DBConnection.java
 * Purpose: Handles MySQL database connectivity using JDBC.
 * ============================================================
 *
 * ===================== SETUP INSTRUCTIONS =====================
 *
 * You need the MySQL Connector/J JAR file to run this project.
 * Download it from: https://dev.mysql.com/downloads/connector/j/
 * Choose Platform Independent, download the ZIP, extract it,
 * you need the file: mysql-connector-j-8.x.x.jar
 *
 * --- VS CODE SETUP ---
 * 1. Create a lib folder in your project root.
 * 2. Copy mysql-connector-j-8.x.x.jar into the lib folder.
 * 3. Press Ctrl+Shift+P then Java: Configure Classpath
 * 4. Under Referenced Libraries, click Add and select the JAR.
 *    OR add this to .vscode/settings.json:
 *    "java.project.referencedLibraries": ["lib/STAR/STAR.jar"]
 *    (replace STAR with asterisk *)
 *
 * --- IntelliJ IDEA SETUP ---
 * 1. Go to File then Project Structure then Libraries.
 * 2. Click + then Java then select the JAR file then OK.
 *
 * --- Eclipse SETUP ---
 * 1. Right-click project then Build Path then Add External Archives.
 * 2. Select the mysql-connector-j-8.x.x.jar file then Apply.
 *
 * ============================================================
 */

package classtrack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ----- Database Configuration -----
    // Change these values if your MySQL setup is different.
    private static final String URL = "jdbc:mysql://localhost:3306/classtrack_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "root"; // Your MySQL username
    private static final String PASSWORD = "12345"; // Your MySQL password (empty by default for XAMPP/WAMP)

    /**
     * Returns a Connection object to the classtrack_db database.
     * Call this method whenever you need to interact with the database.
     *
     * Usage example:
     * Connection conn = DBConnection.getConnection();
     *
     * @return Connection object, or null if connection fails
     */
    public static Connection getConnection() {
        Connection connection = null;

        try {
            // Step 1: Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: Establish the connection
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        } catch (ClassNotFoundException e) {
            System.out.println("==========================================================");
            System.out.println("ERROR: MySQL JDBC Driver not found!");
            System.out.println("----------------------------------------------------------");
            System.out.println("Please make sure you have added the MySQL Connector/J JAR");
            System.out.println("file to your project classpath.");
            System.out.println("Download from: https://dev.mysql.com/downloads/connector/j/");
            System.out.println("==========================================================");

        } catch (SQLException e) {
            String errorMsg = e.getMessage().toLowerCase();

            if (errorMsg.contains("access denied")) {
                System.out.println("==========================================================");
                System.out.println("ERROR: Access Denied!");
                System.out.println("----------------------------------------------------------");
                System.out.println("The username or password in DBConnection.java is incorrect.");
                System.out.println("Current username: " + USERNAME);
                System.out.println("Please update the USERNAME and PASSWORD fields.");
                System.out.println("==========================================================");

            } else if (errorMsg.contains("unknown database")) {
                System.out.println("==========================================================");
                System.out.println("ERROR: Database classtrack_db not found!");
                System.out.println("----------------------------------------------------------");
                System.out.println("Please run the setup.sql script first to create the database.");
                System.out.println("Command: mysql -u root -p < setup.sql");
                System.out.println("==========================================================");

            } else {
                System.out.println("==========================================================");
                System.out.println("ERROR: Database connection failed!");
                System.out.println("----------------------------------------------------------");
                System.out.println("Details: " + e.getMessage());
                System.out.println("Make sure MySQL server is running on localhost:3306");
                System.out.println("==========================================================");
            }
        }

        return connection;
    }
}
