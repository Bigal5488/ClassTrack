/*
 * ============================================================
 * ClassTrack – Student Attendance Management System
 * File: MainMenu.java
 * Purpose: Entry point. Shows role selection, handles login,
 *          and routes to the appropriate role-specific menu.
 * ============================================================
 */

package classtrack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import classtrack.hod.HODMenu;
import classtrack.faculty.FacultyMenu;
import classtrack.student.StudentMenu;

public class MainMenu {

    private static Scanner scanner = new Scanner(System.in);

    // =====================================================
    // MAIN METHOD
    // =====================================================
    public static void main(String[] args) {
        UIHelper.printBanner();

        // Auto-migrate database schema (adds role column etc. if missing)
        DatabaseMigration.migrate();

        boolean running = true;
        while (running) {
            printRoleSelection();
            int roleChoice = readInt("  Enter your choice: ");

            switch (roleChoice) {
                case 1: // HOD Login
                    String hodUser = loginPrompt("HOD");
                    if (hodUser != null) {
                        String role = validateLogin(hodUser, getPasswordInput(), "HOD");
                        if (role != null) {
                            UIHelper.printSuccess("HOD Login successful! Welcome, " + hodUser + "!");
                            new HODMenu(scanner).run();
                        } else {
                            UIHelper.printError("Invalid HOD credentials.");
                        }
                    }
                    break;

                case 2: // Faculty Login
                    String facUser = loginPrompt("FACULTY");
                    if (facUser != null) {
                        String role = validateLogin(facUser, getPasswordInput(), "FACULTY");
                        if (role != null) {
                            UIHelper.printSuccess("Faculty Login successful! Welcome, " + facUser + "!");
                            new FacultyMenu(scanner).run();
                        } else {
                            UIHelper.printError("Invalid Faculty credentials.");
                        }
                    }
                    break;

                case 3: // Student Login
                    String stuUser = loginPrompt("STUDENT");
                    if (stuUser != null) {
                        String password = getPasswordInput();
                        String role = validateLogin(stuUser, password, "STUDENT");
                        if (role != null) {
                            // Get the linked roll_no
                            String rollNo = getLinkedRollNo(stuUser);
                            if (rollNo != null) {
                                UIHelper.printSuccess("Student Login successful! Welcome!");
                                new StudentMenu(scanner, rollNo).run();
                            } else {
                                UIHelper.printError("No student profile linked to this account.");
                            }
                        } else {
                            UIHelper.printError("Invalid Student credentials.");
                        }
                    }
                    break;

                case 4: // Exit
                    running = false;
                    System.out.println("\n" + UIHelper.BRIGHT_CYAN
                            + "╔══════════════════════════════════════════════════════╗");
                    System.out.println(UIHelper.BRIGHT_CYAN + "║" + UIHelper.BRIGHT_YELLOW
                            + "   Thank you for using ClassTrack!                " + UIHelper.BRIGHT_CYAN + "║");
                    System.out.println(UIHelper.BRIGHT_CYAN + "║" + UIHelper.BRIGHT_GREEN
                            + "   Goodbye!                                       " + UIHelper.BRIGHT_CYAN + "║");
                    System.out.println(UIHelper.BRIGHT_CYAN
                            + "╚══════════════════════════════════════════════════════╝" + UIHelper.RESET);
                    break;

                default:
                    UIHelper.printError("Invalid choice! Please enter 1-4.");
            }
        }

        scanner.close();
    }

    // =====================================================
    // ROLE SELECTION SCREEN
    // =====================================================
    private static void printRoleSelection() {
        int width = 50;
        String border = "\u2550".repeat(width);

        System.out.println("\n" + UIHelper.BRIGHT_CYAN + "\u2554" + border + "\u2557" + UIHelper.RESET);
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.BRIGHT_YELLOW + "%-" + width + "s"
                + UIHelper.BRIGHT_CYAN + "\u2551%n", "           SELECT YOUR ROLE");
        System.out.println(UIHelper.BRIGHT_CYAN + "\u2560" + border + "\u2563" + UIHelper.RESET);
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.BRIGHT_RED + "  %-" + (width - 2) + "s"
                + UIHelper.BRIGHT_CYAN + "\u2551%n", "1. HOD (Head of Department)");
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.BRIGHT_GREEN + "  %-" + (width - 2) + "s"
                + UIHelper.BRIGHT_CYAN + "\u2551%n", "2. Faculty / Teacher");
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.BLUE + "  %-" + (width - 2) + "s"
                + UIHelper.BRIGHT_CYAN + "\u2551%n", "3. Student");
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.MAGENTA + "  %-" + (width - 2) + "s"
                + UIHelper.BRIGHT_CYAN + "\u2551%n", "4. Exit");
        System.out.println(UIHelper.BRIGHT_CYAN + "\u255a" + border + "\u255d" + UIHelper.RESET);
    }

    // =====================================================
    // LOGIN HELPERS
    // =====================================================
    private static String loginPrompt(String roleName) {
        UIHelper.printSectionHeader(roleName + " LOGIN", UIHelper.BRIGHT_CYAN);
        System.out.print("  Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            UIHelper.printError("Username cannot be empty.");
            return null;
        }
        return username;
    }

    private static String getPasswordInput() {
        System.out.print("  Password: ");
        java.io.Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword();
            return pwd != null ? new String(pwd).trim() : "";
        }
        // Fallback for IDEs where Console is not available
        return scanner.nextLine().trim();
    }

    /**
     * Validates login credentials against the database.
     * Returns the role if valid, null otherwise.
     */
    private static String validateLogin(String username, String password, String expectedRole) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return null;

            String sql = "SELECT role FROM users WHERE username = ? AND password = ? AND role = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, expectedRole);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }
            return null;

        } catch (SQLException e) {
            UIHelper.printError("Login error: " + e.getMessage());
            return null;

        } finally {
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
    }

    /**
     * Gets the roll_no linked to a student user account.
     */
    private static String getLinkedRollNo(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return null;

            String sql = "SELECT roll_no FROM users WHERE username = ? AND role = 'STUDENT'";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("roll_no");
            }
            return null;

        } catch (SQLException e) {
            return null;
        } finally {
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
    }

    // =====================================================
    // HELPER METHOD – Read Integer
    // =====================================================
    public static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }
}
