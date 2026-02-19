/*
 * ============================================================
 * ClassTrack – Database Migration
 * Automatically upgrades the database schema on app startup.
 * Adds role column, default accounts, and attendance_log table.
 * ============================================================
 */

package classtrack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration {

    /**
     * Runs all migrations. Safe to call every startup —
     * each migration checks if it's already been applied.
     */
    public static void migrate() {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                UIHelper.printError("Cannot connect to database for migration.");
                return;
            }

            addRoleColumn(conn);
            addRollNoColumnToUsers(conn);
            insertDefaultAccounts(conn);
            createAttendanceLogTable(conn);
            fixAttendanceDuplicates(conn);
            addSubjectColumn(conn);
            createMissingStudentLogins(conn);

            UIHelper.printSuccess("Database is up to date.");

        } catch (SQLException e) {
            UIHelper.printError("Migration error: " + e.getMessage());
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Adds 'role' column to users table if it doesn't exist.
     */
    private static void addRoleColumn(Connection conn) throws SQLException {
        if (columnExists(conn, "users", "role"))
            return;

        Statement stmt = conn.createStatement();
        stmt.executeUpdate("ALTER TABLE users ADD COLUMN role ENUM('HOD','FACULTY','STUDENT') NOT NULL DEFAULT 'HOD'");
        stmt.close();
        UIHelper.printInfo("  Added 'role' column to users table.");
    }

    /**
     * Adds 'roll_no' column to users table if it doesn't exist.
     */
    private static void addRollNoColumnToUsers(Connection conn) throws SQLException {
        if (columnExists(conn, "users", "roll_no"))
            return;

        Statement stmt = conn.createStatement();
        stmt.executeUpdate("ALTER TABLE users ADD COLUMN roll_no VARCHAR(20) NULL");
        stmt.close();
        UIHelper.printInfo("  Added 'roll_no' column to users table.");
    }

    /**
     * Consolidates duplicate attendance rows and adds UNIQUE index on roll_no.
     */
    private static void fixAttendanceDuplicates(Connection conn) throws SQLException {
        // Check if UNIQUE index already exists on roll_no
        if (indexExists(conn, "attendance", "roll_no"))
            return;

        Statement stmt = conn.createStatement();

        // Step 1: Create a temp table with consolidated data
        stmt.executeUpdate(
                "CREATE TEMPORARY TABLE att_temp AS " +
                        "SELECT roll_no, SUM(total_periods) AS total_periods, SUM(present_periods) AS present_periods "
                        +
                        "FROM attendance GROUP BY roll_no");

        // Step 2: Delete all rows from attendance
        stmt.executeUpdate("DELETE FROM attendance");

        // Step 3: Re-insert consolidated data
        stmt.executeUpdate(
                "INSERT INTO attendance (roll_no, total_periods, present_periods) " +
                        "SELECT roll_no, total_periods, present_periods FROM att_temp");

        // Step 4: Drop temp table
        stmt.executeUpdate("DROP TEMPORARY TABLE att_temp");

        // Step 5: Add UNIQUE constraint so this never happens again
        stmt.executeUpdate("ALTER TABLE attendance ADD UNIQUE INDEX uq_attendance_rollno (roll_no)");

        stmt.close();
        UIHelper.printInfo("  Fixed duplicate attendance rows and added UNIQUE constraint.");
    }

    /**
     * Checks if a UNIQUE index exists on a column.
     */
    private static boolean indexExists(Connection conn, String table, String column) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.statistics " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ? AND non_unique = 0 " +
                        "AND index_name != 'PRIMARY'");
        pstmt.setString(1, table);
        pstmt.setString(2, column);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        boolean exists = rs.getInt(1) > 0;
        rs.close();
        pstmt.close();
        return exists;
    }

    /**
     * Creates attendance_log table if it doesn't exist.
     */
    private static void createAttendanceLogTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS attendance_log (" +
                        "  log_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "  roll_no VARCHAR(20) NOT NULL," +
                        "  date DATE NOT NULL," +
                        "  period INT NOT NULL," +
                        "  status ENUM('P','A') NOT NULL," +
                        "  FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE," +
                        "  UNIQUE(roll_no, date, period)" +
                        ")");
        stmt.close();
    }

    /**
     * Adds 'subject' column to attendance_log if it doesn't exist.
     */
    private static void addSubjectColumn(Connection conn) throws SQLException {
        if (columnExists(conn, "attendance_log", "subject"))
            return;

        Statement stmt = conn.createStatement();
        stmt.executeUpdate("ALTER TABLE attendance_log ADD COLUMN subject VARCHAR(50) DEFAULT 'General'");
        stmt.close();
        UIHelper.printInfo("  Added 'subject' column to attendance_log table.");
    }

    /**
     * Inserts default HOD and Faculty accounts if they don't exist.
     * Updates existing 'admin' account to HOD role.
     */
    private static void insertDefaultAccounts(Connection conn) throws SQLException {
        // Update any existing 'admin' user to HOD role
        PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE users SET role = 'HOD' WHERE username = 'admin'");
        updateStmt.executeUpdate();
        updateStmt.close();

        // Insert HOD account if not exists
        insertUserIfNotExists(conn, "hod", "hod123", "HOD", null);

        // Insert Faculty accounts if not exists
        insertUserIfNotExists(conn, "faculty1", "faculty123", "FACULTY", null);
        insertUserIfNotExists(conn, "faculty2", "faculty123", "FACULTY", null);
    }

    /**
     * Creates login accounts for any students that don't have one yet.
     */
    private static void createMissingStudentLogins(Connection conn) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, roll_no) "
                + "SELECT s.roll_no, 'student123', 'STUDENT', s.roll_no "
                + "FROM students s "
                + "WHERE s.roll_no NOT IN (SELECT username FROM users WHERE role = 'STUDENT')";
        Statement stmt = conn.createStatement();
        int count = stmt.executeUpdate(sql);
        stmt.close();
        if (count > 0) {
            UIHelper.printInfo("  Created " + count + " missing student login account(s).");
        }
    }

    /**
     * Inserts a user if the username doesn't already exist.
     */
    private static void insertUserIfNotExists(Connection conn, String username, String password, String role,
            String rollNo) throws SQLException {
        PreparedStatement checkStmt = conn.prepareStatement("SELECT user_id FROM users WHERE username = ?");
        checkStmt.setString(1, username);
        ResultSet rs = checkStmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        checkStmt.close();

        if (!exists) {
            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role, roll_no) VALUES (?, ?, ?, ?)");
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, role);
            insertStmt.setString(4, rollNo);
            insertStmt.executeUpdate();
            insertStmt.close();
            UIHelper.printInfo("  Created account: " + username + " (" + role + ")");
        }
    }

    /**
     * Checks if a column exists in a table.
     */
    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?");
        pstmt.setString(1, table);
        pstmt.setString(2, column);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        boolean exists = rs.getInt(1) > 0;
        rs.close();
        pstmt.close();
        return exists;
    }
}
