/*
 * ============================================================
 * ClassTrack – Student Attendance Management System
 * File: AttendanceDAO.java
 * Purpose: Data Access Object for Attendance operations.
 *          Handles marking, viewing, and defaulter reporting.
 * ============================================================
 */

package classtrack;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AttendanceDAO {

    // Table display width (characters between left and right borders)
    private static final int TABLE_WIDTH = 84;

    // =====================================================
    // 1. MARK ATTENDANCE (Single Student – logs + summary)
    // =====================================================
    public void markAttendance(String rollNo, Date date, int period, String subject, String status) {
        Connection conn = null;
        PreparedStatement logStmt = null;
        PreparedStatement summaryStmt = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            conn.setAutoCommit(false);

            // 1. Insert into attendance_log
            String logSQL = "INSERT INTO attendance_log (roll_no, date, period, status, subject) VALUES (?, ?, ?, ?, ?)";
            logStmt = conn.prepareStatement(logSQL);
            logStmt.setString(1, rollNo);
            logStmt.setDate(2, date);
            logStmt.setInt(3, period);
            logStmt.setString(4, status);
            logStmt.setString(5, subject);
            try {
                logStmt.executeUpdate();
            } catch (SQLException e) {
                UIHelper.printError("Attendance already marked for this student/period on this date.");
                conn.rollback();
                return;
            }

            // 2. Update summary table
            int presentIncrement = status.equals("P") ? 1 : 0;
            String summarySQL = "INSERT INTO attendance (roll_no, total_periods, present_periods) VALUES (?, 1, ?) "
                    + "ON DUPLICATE KEY UPDATE total_periods = total_periods + 1, present_periods = present_periods + ?";
            summaryStmt = conn.prepareStatement(summarySQL);
            summaryStmt.setString(1, rollNo);
            summaryStmt.setInt(2, presentIncrement);
            summaryStmt.setInt(3, presentIncrement);
            summaryStmt.executeUpdate();

            conn.commit();

            String statusText = status.equals("P") ? "PRESENT" : "ABSENT";
            UIHelper.printSuccess("Attendance marked for " + rollNo + " | " + subject
                    + " | Period " + period + " | " + statusText);

        } catch (SQLException e) {
            UIHelper.printError("ERROR while marking attendance: " + e.getMessage());
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
            }
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (SQLException ex) {
            }
            closeResources(conn, logStmt, null);
            closeStatement(summaryStmt);
        }
    }

    // =====================================================
    // 2. BATCH ATTENDANCE (Class-wise)
    // =====================================================
    public void markBatchAttendance(String className, Date date, int period, String subject,
            List<String> allStudents, List<String> absentees) {
        Connection conn = null;
        PreparedStatement logStmt = null;
        PreparedStatement summaryStmt = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            conn.setAutoCommit(false);

            String logSQL = "INSERT INTO attendance_log (roll_no, date, period, status, subject) VALUES (?, ?, ?, ?, ?)";
            logStmt = conn.prepareStatement(logSQL);

            String summarySQL = "INSERT INTO attendance (roll_no, total_periods, present_periods) VALUES (?, 1, ?) " +
                    "ON DUPLICATE KEY UPDATE total_periods = total_periods + 1, present_periods = present_periods + ?";
            summaryStmt = conn.prepareStatement(summarySQL);

            int successCount = 0;

            for (String rollNo : allStudents) {
                boolean isAbsent = absentees.contains(rollNo);
                String status = isAbsent ? "A" : "P";
                int presentIncrement = isAbsent ? 0 : 1;

                logStmt.setString(1, rollNo);
                logStmt.setDate(2, date);
                logStmt.setInt(3, period);
                logStmt.setString(4, status);
                logStmt.setString(5, subject);
                try {
                    logStmt.executeUpdate();
                } catch (SQLException e) {
                    UIHelper.printWarning("Skipping Roll " + rollNo + ": Attendance already marked for this period.");
                    continue;
                }

                summaryStmt.setString(1, rollNo);
                summaryStmt.setInt(2, presentIncrement);
                summaryStmt.setInt(3, presentIncrement);
                summaryStmt.executeUpdate();

                successCount++;
            }

            conn.commit();
            UIHelper.printSuccess("Attendance marked for " + successCount + " students in " + className
                    + " | Subject: " + subject + " | Period: " + period);

        } catch (SQLException e) {
            UIHelper.printError("Transaction failed: " + e.getMessage());
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
            }
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (SQLException ex) {
            }
            closeResources(conn, logStmt, null);
            closeStatement(summaryStmt);
        }
    }

    // =====================================================
    // 3. VIEW ATTENDANCE (Overall + Date-wise)
    // =====================================================
    public void viewAttendance(String rollNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement logStmt = null;
        ResultSet rs = null;
        ResultSet logRs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            // --- PART 1: Overall Semester Summary ---
            String sql = "SELECT s.roll_no, s.name, s.class_name, s.department, "
                    + "a.total_periods, a.present_periods "
                    + "FROM students s "
                    + "LEFT JOIN attendance a ON s.roll_no = a.roll_no "
                    + "WHERE s.roll_no = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int totalPeriods = rs.getInt("total_periods");
                int presentPeriods = rs.getInt("present_periods");
                double percentage = (totalPeriods > 0) ? ((double) presentPeriods / totalPeriods) * 100 : 0;

                int w = 55;
                String border = "\u2550".repeat(w);
                System.out.println("\n\u2554" + border + "\u2557");
                System.out.printf("\u2551%-" + w + "s\u2551%n", "           SEMESTER ATTENDANCE REPORT");
                System.out.println("\u2560" + border + "\u2563");
                System.out.printf("\u2551%-" + w + "s\u2551%n", "  Roll No     : " + rs.getString("roll_no"));
                System.out.printf("\u2551%-" + w + "s\u2551%n", "  Name        : " + rs.getString("name"));
                System.out.printf("\u2551%-" + w + "s\u2551%n", "  Class       : " + rs.getString("class_name"));
                System.out.printf("\u2551%-" + w + "s\u2551%n", "  Department  : " + rs.getString("department"));
                System.out.println("\u2560" + border + "\u2563");

                if (totalPeriods == 0) {
                    System.out.printf("\u2551%-" + w + "s\u2551%n", "  Attendance  : No attendance records found.");
                } else {
                    System.out.printf("\u2551%-" + w + "s\u2551%n", "  Total Periods : " + totalPeriods);
                    System.out.printf("\u2551%-" + w + "s\u2551%n", "  Present       : " + presentPeriods);
                    System.out.printf("\u2551%-" + w + "s\u2551%n",
                            "  Absent        : " + (totalPeriods - presentPeriods));
                    System.out.printf("\u2551%-" + w + "s\u2551%n",
                            "  Percentage    : " + String.format("%.2f", percentage) + "%");

                    if (percentage < 75.0) {
                        System.out.printf("\u2551%-" + w + "s\u2551%n", "  Status        : ! DEFAULTER (Below 75%)");
                    } else {
                        System.out.printf("\u2551%-" + w + "s\u2551%n", "  Status        : [OK] REGULAR");
                    }
                }
                System.out.println(UIHelper.BRIGHT_CYAN + "\u255a" + border + "\u255d" + UIHelper.RESET);

                // --- PART 2: Date-wise Breakdown ---
                String logSQL = "SELECT date, period, status, subject FROM attendance_log "
                        + "WHERE roll_no = ? ORDER BY date ASC, period ASC";
                logStmt = conn.prepareStatement(logSQL);
                logStmt.setString(1, rollNo);
                logRs = logStmt.executeQuery();

                // Check if any date-wise records exist
                boolean hasLogs = false;
                int dw = 55;
                String dborder = "\u2550".repeat(dw);

                System.out.println("\n" + UIHelper.YELLOW + "\u2554" + dborder + "\u2557" + UIHelper.RESET);
                System.out.printf(UIHelper.YELLOW + "\u2551%-" + dw + "s\u2551%n" + UIHelper.RESET,
                        "           DATE-WISE BREAKDOWN");
                System.out.println(UIHelper.YELLOW + "\u2560" + dborder + "\u2563" + UIHelper.RESET);
                System.out.printf(UIHelper.YELLOW + "\u2551" + UIHelper.RESET
                        + " %-12s %-8s %-14s %-15s"
                        + UIHelper.YELLOW + "%4s\u2551%n" + UIHelper.RESET,
                        "Date", "Period", "Subject", "Status", "");

                System.out.println(UIHelper.YELLOW + "\u2560" + dborder + "\u2563" + UIHelper.RESET);

                while (logRs.next()) {
                    hasLogs = true;
                    String date = logRs.getString("date");
                    int period = logRs.getInt("period");
                    String status = logRs.getString("status");
                    String subject = logRs.getString("subject");
                    if (subject == null)
                        subject = "General";

                    String statusText = status.equals("P") ? "Present" : "Absent";
                    String statusColor = status.equals("P") ? UIHelper.GREEN : UIHelper.RED;

                    String row = String.format(" %-12s %-8d %-14s ", date, period, subject);
                    System.out.printf(UIHelper.YELLOW + "\u2551" + UIHelper.RESET
                            + "%-35s" + statusColor + "%-15s" + UIHelper.RESET
                            + UIHelper.YELLOW + "%5s\u2551%n" + UIHelper.RESET,
                            row, statusText, "");
                }

                if (!hasLogs) {
                    System.out.printf(UIHelper.YELLOW + "\u2551%-" + dw + "s\u2551%n" + UIHelper.RESET,
                            "  No date-wise records found.");
                }

                System.out.println(UIHelper.YELLOW + "\u255a" + dborder + "\u255d" + UIHelper.RESET);

            } else {
                UIHelper.printError("No student found with Roll No: " + rollNo);
            }

        } catch (SQLException e) {
            UIHelper.printError("ERROR while viewing attendance: " + e.getMessage());

        } finally {
            try {
                if (logRs != null)
                    logRs.close();
            } catch (SQLException e) {
            }
            try {
                if (logStmt != null)
                    logStmt.close();
            } catch (SQLException e) {
            }
            closeResources(conn, pstmt, rs);
        }
    }

    // =====================================================
    // 4. VIEW ALL ATTENDANCE – Sub-menu (Section-based)
    // =====================================================
    public void viewAllAttendance(java.util.Scanner sc) {

        System.out.print("\n  Enter Section / Class (e.g., CSE-1): ");
        String section = sc.nextLine().trim();
        if (section.isEmpty()) {
            UIHelper.printError("Section cannot be empty!");
            return;
        }

        int w = 50;
        String border = "\u2550".repeat(w);
        System.out.println("\n" + UIHelper.BRIGHT_CYAN + "\u2554" + border + "\u2557" + UIHelper.RESET);
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.BRIGHT_YELLOW
                + "%-" + w + "s" + UIHelper.BRIGHT_CYAN + "\u2551%n",
                "   ATTENDANCE SUMMARY - " + section);
        System.out.println(UIHelper.BRIGHT_CYAN + "\u2560" + border + "\u2563" + UIHelper.RESET);
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.GREEN
                + "  %-" + (w - 2) + "s" + UIHelper.BRIGHT_CYAN + "\u2551%n",
                "1. Today's Attendance");
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.CYAN
                + "  %-" + (w - 2) + "s" + UIHelper.BRIGHT_CYAN + "\u2551%n",
                "2. Overall Semester Attendance");
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.YELLOW
                + "  %-" + (w - 2) + "s" + UIHelper.BRIGHT_CYAN + "\u2551%n",
                "3. Check Attendance by Date");
        System.out.println(UIHelper.BRIGHT_CYAN + "\u255a" + border + "\u255d" + UIHelper.RESET);

        System.out.print("  Enter your choice: ");
        String input = sc.nextLine().trim();

        switch (input) {
            case "1":
                viewTodayAttendance(section);
                break;
            case "2":
                viewOverallAttendance(section);
                break;
            case "3":
                System.out.print("  Enter Date (YYYY-MM-DD): ");
                String dateStr = sc.nextLine().trim();
                viewAttendanceByDate(section, dateStr);
                break;
            default:
                UIHelper.printError("Invalid choice.");
        }
    }

    // =====================================================
    // 4a. TODAY'S ATTENDANCE (filtered by section)
    // =====================================================
    private void viewTodayAttendance(String section) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

            String sql = "SELECT s.roll_no, s.name, s.class_name, "
                    + "SUM(CASE WHEN al.status = 'P' THEN 1 ELSE 0 END) AS present, "
                    + "SUM(CASE WHEN al.status = 'A' THEN 1 ELSE 0 END) AS absent "
                    + "FROM attendance_log al "
                    + "JOIN students s ON al.roll_no = s.roll_no "
                    + "WHERE al.date = ? AND s.class_name = ? "
                    + "GROUP BY s.roll_no, s.name, s.class_name "
                    + "ORDER BY s.roll_no ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, today);
            pstmt.setString(2, section);
            rs = pstmt.executeQuery();

            String border = "\u2550".repeat(TABLE_WIDTH);
            System.out.println("\n\u2554" + border + "\u2557");
            String title = "TODAY'S ATTENDANCE - " + section + " (" + today + ")";
            int leftPad = Math.max(0, (TABLE_WIDTH - title.length()) / 2);
            System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n",
                    " ".repeat(leftPad) + title);
            System.out.println("\u2560" + border + "\u2563");

            String header = String.format("  %-14s %-20s %-12s %-12s %-12s",
                    "Roll No", "Name", "Class", "Present", "Absent");
            System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n", header);
            System.out.println("\u2560" + border + "\u2563");

            boolean found = false;
            while (rs.next()) {
                found = true;
                int present = rs.getInt("present");
                int absent = rs.getInt("absent");

                String row = String.format("  %-14s %-20s %-12s ",
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getString("class_name"));
                System.out.printf("\u2551%-" + (TABLE_WIDTH - 24) + "s"
                        + UIHelper.GREEN + "%-12d" + UIHelper.RESET
                        + UIHelper.RED + "%-12d" + UIHelper.RESET + "\u2551%n",
                        row, present, absent);
            }

            if (!found) {
                System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n",
                        "  No attendance recorded for " + section + " today.");
            }
            System.out.println("\u255a" + border + "\u255d");

        } catch (SQLException e) {
            UIHelper.printError("ERROR: " + e.getMessage());
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // =====================================================
    // 4b. OVERALL SEMESTER ATTENDANCE (filtered by section)
    // =====================================================
    private void viewOverallAttendance(String section) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            String sql = "SELECT s.roll_no, s.name, s.class_name, s.department, "
                    + "a.total_periods, a.present_periods, "
                    + "(a.present_periods * 100.0 / a.total_periods) AS percentage "
                    + "FROM students s "
                    + "LEFT JOIN attendance a ON s.roll_no = a.roll_no "
                    + "WHERE s.class_name = ? "
                    + "ORDER BY s.roll_no ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, section);
            rs = pstmt.executeQuery();

            String border = "\u2550".repeat(TABLE_WIDTH);
            System.out.println("\n\u2554" + border + "\u2557");
            String title = "OVERALL ATTENDANCE - " + section;
            int leftPad = Math.max(0, (TABLE_WIDTH - title.length()) / 2);
            System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n",
                    " ".repeat(leftPad) + title);
            System.out.println("\u2560" + border + "\u2563");

            String header = String.format("  %-14s %-18s %-10s %-10s %-7s %-7s %-8s",
                    "Roll No", "Name", "Class", "Department", "Total", "Pres.", "Percent");
            System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n", header);
            System.out.println("\u2560" + border + "\u2563");

            boolean found = false;
            while (rs.next()) {
                found = true;
                int total = rs.getInt("total_periods");
                int present = rs.getInt("present_periods");
                double pct = (total > 0) ? rs.getDouble("percentage") : 0.0;

                String row = String.format("  %-14s %-18s %-10s %-10s %-7d %-7d %-7.2f%%",
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getString("class_name"),
                        rs.getString("department"),
                        total, present, pct);
                System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n", row);
            }

            if (!found) {
                System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n",
                        "  No students found in section: " + section);
            }
            System.out.println("\u255a" + border + "\u255d");

        } catch (SQLException e) {
            UIHelper.printError("ERROR: " + e.getMessage());
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // =====================================================
    // 4c. ATTENDANCE BY SPECIFIC DATE (filtered by section)
    // =====================================================
    private void viewAttendanceByDate(String section, String dateStr) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            java.sql.Date.valueOf(dateStr);

            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            String sql = "SELECT s.roll_no, s.name, s.class_name, "
                    + "SUM(CASE WHEN al.status = 'P' THEN 1 ELSE 0 END) AS present, "
                    + "SUM(CASE WHEN al.status = 'A' THEN 1 ELSE 0 END) AS absent "
                    + "FROM attendance_log al "
                    + "JOIN students s ON al.roll_no = s.roll_no "
                    + "WHERE al.date = ? AND s.class_name = ? "
                    + "GROUP BY s.roll_no, s.name, s.class_name "
                    + "ORDER BY s.roll_no ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, dateStr);
            pstmt.setString(2, section);
            rs = pstmt.executeQuery();

            String border = "\u2550".repeat(TABLE_WIDTH);
            System.out.println("\n\u2554" + border + "\u2557");
            String title = "ATTENDANCE - " + section + " (" + dateStr + ")";
            int leftPad = Math.max(0, (TABLE_WIDTH - title.length()) / 2);
            System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n",
                    " ".repeat(leftPad) + title);
            System.out.println("\u2560" + border + "\u2563");

            String header = String.format("  %-14s %-20s %-12s %-12s %-12s",
                    "Roll No", "Name", "Class", "Present", "Absent");
            System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n", header);
            System.out.println("\u2560" + border + "\u2563");

            boolean found = false;
            while (rs.next()) {
                found = true;
                int present = rs.getInt("present");
                int absent = rs.getInt("absent");

                String row = String.format("  %-14s %-20s %-12s ",
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getString("class_name"));
                System.out.printf("\u2551%-" + (TABLE_WIDTH - 24) + "s"
                        + UIHelper.GREEN + "%-12d" + UIHelper.RESET
                        + UIHelper.RED + "%-12d" + UIHelper.RESET + "\u2551%n",
                        row, present, absent);
            }

            if (!found) {
                System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n",
                        "  No attendance for " + section + " on " + dateStr);
            }
            System.out.println("\u255a" + border + "\u255d");

        } catch (IllegalArgumentException e) {
            UIHelper.printError("Invalid date format! Use YYYY-MM-DD.");
        } catch (SQLException e) {
            UIHelper.printError("ERROR: " + e.getMessage());
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // =====================================================
    // 4d. STUDENT: TODAY'S ATTENDANCE (single student)
    // =====================================================
    public void viewStudentTodayAttendance(String rollNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

            String sql = "SELECT period, subject, status FROM attendance_log "
                    + "WHERE roll_no = ? AND date = ? ORDER BY period ASC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            pstmt.setString(2, today);
            rs = pstmt.executeQuery();

            int w = 60;
            String border = "\u2550".repeat(w);
            System.out.println("\n" + UIHelper.YELLOW + "\u2554" + border + "\u2557" + UIHelper.RESET);
            String title = "MY ATTENDANCE - TODAY (" + today + ")";
            int leftPad = Math.max(0, (w - title.length()) / 2);
            System.out.printf(UIHelper.YELLOW + "\u2551%-" + w + "s\u2551%n" + UIHelper.RESET,
                    " ".repeat(leftPad) + title);
            System.out.println(UIHelper.YELLOW + "\u2560" + border + "\u2563" + UIHelper.RESET);
            System.out.printf(UIHelper.YELLOW + "\u2551" + UIHelper.RESET
                    + "  %-10s %-20s %-20s"
                    + UIHelper.YELLOW + "%8s\u2551%n" + UIHelper.RESET,
                    "Period", "Subject", "Status", "");
            System.out.println(UIHelper.YELLOW + "\u2560" + border + "\u2563" + UIHelper.RESET);

            boolean found = false;
            while (rs.next()) {
                found = true;
                int period = rs.getInt("period");
                String subject = rs.getString("subject");
                if (subject == null)
                    subject = "General";
                String status = rs.getString("status");
                String statusText = status.equals("P") ? "Present" : "Absent";
                String statusColor = status.equals("P") ? UIHelper.GREEN : UIHelper.RED;

                String row = String.format("  %-10d %-20s ", period, subject);
                int remaining = w - row.length();
                String statusPadded = String.format("%-" + remaining + "s", statusText);
                System.out.println(UIHelper.YELLOW + "\u2551" + UIHelper.RESET
                        + row + statusColor + statusPadded + UIHelper.RESET
                        + UIHelper.YELLOW + "\u2551" + UIHelper.RESET);
            }

            if (!found) {
                System.out.printf(UIHelper.YELLOW + "\u2551%-" + w + "s\u2551%n" + UIHelper.RESET,
                        "  No attendance recorded for today.");
            }
            System.out.println(UIHelper.YELLOW + "\u255a" + border + "\u255d" + UIHelper.RESET);

        } catch (SQLException e) {
            UIHelper.printError("ERROR: " + e.getMessage());
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // =====================================================
    // 4e. STUDENT: ATTENDANCE BY DATE (single student)
    // =====================================================
    public void viewStudentAttendanceByDate(String rollNo, String dateStr) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            java.sql.Date.valueOf(dateStr); // validate

            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            String sql = "SELECT period, subject, status FROM attendance_log "
                    + "WHERE roll_no = ? AND date = ? ORDER BY period ASC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            pstmt.setString(2, dateStr);
            rs = pstmt.executeQuery();

            int w = 60;
            String border = "\u2550".repeat(w);
            System.out.println("\n" + UIHelper.YELLOW + "\u2554" + border + "\u2557" + UIHelper.RESET);
            String title = "MY ATTENDANCE - " + dateStr;
            int leftPad = Math.max(0, (w - title.length()) / 2);
            System.out.printf(UIHelper.YELLOW + "\u2551%-" + w + "s\u2551%n" + UIHelper.RESET,
                    " ".repeat(leftPad) + title);
            System.out.println(UIHelper.YELLOW + "\u2560" + border + "\u2563" + UIHelper.RESET);
            System.out.printf(UIHelper.YELLOW + "\u2551" + UIHelper.RESET
                    + "  %-10s %-20s %-20s"
                    + UIHelper.YELLOW + "%8s\u2551%n" + UIHelper.RESET,
                    "Period", "Subject", "Status", "");
            System.out.println(UIHelper.YELLOW + "\u2560" + border + "\u2563" + UIHelper.RESET);

            boolean found = false;
            while (rs.next()) {
                found = true;
                int period = rs.getInt("period");
                String subject = rs.getString("subject");
                if (subject == null)
                    subject = "General";
                String status = rs.getString("status");
                String statusText = status.equals("P") ? "Present" : "Absent";
                String statusColor = status.equals("P") ? UIHelper.GREEN : UIHelper.RED;

                String row = String.format("  %-10d %-20s ", period, subject);
                int remaining = w - row.length();
                String statusPadded = String.format("%-" + remaining + "s", statusText);
                System.out.println(UIHelper.YELLOW + "\u2551" + UIHelper.RESET
                        + row + statusColor + statusPadded + UIHelper.RESET
                        + UIHelper.YELLOW + "\u2551" + UIHelper.RESET);
            }

            if (!found) {
                System.out.printf(UIHelper.YELLOW + "\u2551%-" + w + "s\u2551%n" + UIHelper.RESET,
                        "  No attendance recorded for " + dateStr);
            }
            System.out.println(UIHelper.YELLOW + "\u255a" + border + "\u255d" + UIHelper.RESET);

        } catch (IllegalArgumentException e) {
            UIHelper.printError("Invalid date format! Use YYYY-MM-DD.");
        } catch (SQLException e) {
            UIHelper.printError("ERROR: " + e.getMessage());
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // =====================================================
    // 5. SHOW DEFAULTERS (Below 75%)
    // =====================================================
    public void showDefaulters() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            String sql = "SELECT s.roll_no, s.name, s.class_name, s.department, "
                    + "a.total_periods, a.present_periods, "
                    + "(a.present_periods * 100.0 / a.total_periods) AS percentage "
                    + "FROM students s "
                    + "INNER JOIN attendance a ON s.roll_no = a.roll_no "
                    + "WHERE a.total_periods > 0 "
                    + "AND (a.present_periods * 100.0 / a.total_periods) < 75 "
                    + "ORDER BY percentage ASC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            String border = "\u2550".repeat(TABLE_WIDTH);

            // Title
            System.out.println("\n\u2554" + border + "\u2557");
            String title = "DEFAULTERS LIST (Attendance < 75%)";
            int leftPad = (TABLE_WIDTH - title.length()) / 2;
            System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n",
                    " ".repeat(leftPad) + title);
            System.out.println("\u2560" + border + "\u2563");

            // Header
            String header = String.format("  %-14s %-18s %-10s %-10s %-7s %-7s %-8s",
                    "Roll No", "Name", "Class", "Department", "Total", "Pres.", "Percent");
            System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n", header);
            System.out.println("\u2560" + border + "\u2563");

            // Data
            boolean found = false;
            while (rs.next()) {
                found = true;
                String row = String.format("  %-14s %-18s %-10s %-10s %-7d %-7d %-7.2f%%",
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getString("class_name"),
                        rs.getString("department"),
                        rs.getInt("total_periods"),
                        rs.getInt("present_periods"),
                        rs.getDouble("percentage"));
                System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n", row);
            }

            if (!found) {
                System.out.printf("\u2551%-" + TABLE_WIDTH + "s\u2551%n",
                        "  No defaulters found! All students are regular.");
            }

            System.out.println("\u255a" + border + "\u255d");

        } catch (SQLException e) {
            UIHelper.printError("ERROR while fetching defaulters: " + e.getMessage());

        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
        }
        try {
            if (pstmt != null)
                pstmt.close();
        } catch (SQLException e) {
        }
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
        }
    }

    private void closeStatement(PreparedStatement pstmt) {
        try {
            if (pstmt != null)
                pstmt.close();
        } catch (SQLException e) {
        }
    }
}
