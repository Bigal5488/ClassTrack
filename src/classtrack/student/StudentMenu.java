/*
 * ============================================================
 * ClassTrack – Student Menu (View Only)
 * Students can only view their OWN attendance and profile.
 * CANNOT: Add, modify, or delete anything.
 * ============================================================
 */

package classtrack.student;

import classtrack.*;
import java.util.List;
import java.util.Scanner;

public class StudentMenu {

    private Scanner scanner;
    private String rollNo; // The logged-in student's roll number
    private StudentDAO studentDAO = new StudentDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();

    public StudentMenu(Scanner scanner, String rollNo) {
        this.scanner = scanner;
        this.rollNo = rollNo;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("  Enter your choice: ");

            switch (choice) {
                case 1:
                    viewMyProfile();
                    break;
                case 2:
                    viewMyAttendance();
                    break;
                case 3:
                    System.out.println("\n  Logging out of Student portal...");
                    running = false;
                    break;
                default:
                    UIHelper.printError("Invalid choice! Please enter 1-3.");
            }
        }
    }

    // =====================================================
    // STUDENT MENU DISPLAY
    // =====================================================
    private void printMenu() {
        int w = 50;
        String border = "\u2550".repeat(w);
        System.out.println("\n" + UIHelper.BRIGHT_CYAN + "\u2554" + border + "\u2557" + UIHelper.RESET);
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.BLUE
                + "%-" + w + "s" + UIHelper.BRIGHT_CYAN + "\u2551%n",
                "     STUDENT PORTAL (" + rollNo + ")");
        System.out.println(UIHelper.BRIGHT_CYAN + "\u2560" + border + "\u2563" + UIHelper.RESET);
        printItem(w, "1.  View My Profile", UIHelper.CYAN);
        printItem(w, "2.  View My Attendance", UIHelper.YELLOW);
        printItem(w, "3.  Logout", UIHelper.MAGENTA);
        System.out.println(UIHelper.BRIGHT_CYAN + "\u255a" + border + "\u255d" + UIHelper.RESET);
    }

    private void printItem(int w, String text, String color) {
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + color + "  %-" + (w - 2) + "s"
                + UIHelper.BRIGHT_CYAN + "\u2551%n", text);
    }

    // =====================================================
    // VIEW MY PROFILE
    // =====================================================
    private void viewMyProfile() {
        List<Student> results = studentDAO.searchStudent(rollNo);
        if (results.isEmpty()) {
            UIHelper.printError("Profile not found for Roll No: " + rollNo);
            return;
        }

        Student s = results.get(0);
        int w = 50;
        String border = "\u2550".repeat(w);
        System.out.println("\n" + UIHelper.BRIGHT_CYAN + "\u2554" + border + "\u2557");
        System.out.printf("\u2551%-" + w + "s\u2551%n", "              MY PROFILE");
        System.out.println("\u2560" + border + "\u2563");
        System.out.printf("\u2551%-" + w + "s\u2551%n", "  Roll No    : " + s.getRollNo());
        System.out.printf("\u2551%-" + w + "s\u2551%n", "  Name       : " + s.getName());
        System.out.printf("\u2551%-" + w + "s\u2551%n", "  Class      : " + s.getClassName());
        System.out.printf("\u2551%-" + w + "s\u2551%n", "  Department : " + s.getDepartment());
        System.out.println("\u255a" + border + "\u255d" + UIHelper.RESET);
    }

    // =====================================================
    // VIEW MY ATTENDANCE – Sub-menu
    // =====================================================
    private void viewMyAttendance() {
        int w = 50;
        String border = "\u2550".repeat(w);
        System.out.println("\n" + UIHelper.BRIGHT_CYAN + "\u2554" + border + "\u2557" + UIHelper.RESET);
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.BRIGHT_YELLOW
                + "%-" + w + "s" + UIHelper.BRIGHT_CYAN + "\u2551%n",
                "         MY ATTENDANCE");
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
        String input = scanner.nextLine().trim();

        switch (input) {
            case "1":
                attendanceDAO.viewStudentTodayAttendance(rollNo);
                break;
            case "2":
                attendanceDAO.viewAttendance(rollNo);
                break;
            case "3":
                System.out.print("  Enter Date (YYYY-MM-DD): ");
                String dateStr = scanner.nextLine().trim();
                attendanceDAO.viewStudentAttendanceByDate(rollNo, dateStr);
                break;
            default:
                UIHelper.printError("Invalid choice.");
        }
    }

    // =====================================================
    // HELPER
    // =====================================================
    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }
}
