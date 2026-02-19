/*
 * ============================================================
 * ClassTrack – HOD Menu (Full Access)
 * Head of Department can: Add/Update/Delete students,
 * Mark/Modify attendance, View all, Show defaulters.
 * ============================================================
 */

package classtrack.hod;

import classtrack.*;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class HODMenu {

    private Scanner scanner;
    private StudentDAO studentDAO = new StudentDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();

    public HODMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("  Enter your choice: ");

            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    updateStudent();
                    break;
                case 3:
                    deleteStudent();
                    break;
                case 4:
                    searchStudent();
                    break;
                case 5:
                    markAttendance();
                    break;
                case 6:
                    markBatchAttendance();
                    break;
                case 7:
                    viewAttendance();
                    break;
                case 8:
                    attendanceDAO.viewAllAttendance(scanner);
                    break;
                case 9:
                    attendanceDAO.showDefaulters();
                    break;
                case 10:
                    System.out.println("\n  Logging out of HOD portal...");
                    running = false;
                    break;
                default:
                    UIHelper.printError("Invalid choice! Please enter 1-10.");
            }
        }
    }

    // =====================================================
    // HOD MENU DISPLAY
    // =====================================================
    private void printMenu() {
        int w = 50;
        String border = "\u2550".repeat(w);
        System.out.println("\n" + UIHelper.BRIGHT_CYAN + "\u2554" + border + "\u2557" + UIHelper.RESET);
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + UIHelper.BRIGHT_YELLOW
                + "%-" + w + "s" + UIHelper.BRIGHT_CYAN + "\u2551%n",
                "          HOD PORTAL (Full Access)");
        System.out.println(UIHelper.BRIGHT_CYAN + "\u2560" + border + "\u2563" + UIHelper.RESET);
        printItem(w, "1.  Add Student", UIHelper.GREEN);
        printItem(w, "2.  Update Student", UIHelper.YELLOW);
        printItem(w, "3.  Delete Student", UIHelper.RED);
        printItem(w, "4.  Search Student", UIHelper.BLUE);
        printItem(w, "5.  Mark Attendance (Single)", UIHelper.GREEN);
        printItem(w, "6.  Mark Class Attendance (Batch)", UIHelper.CYAN);
        printItem(w, "7.  View Attendance (Single)", UIHelper.YELLOW);
        printItem(w, "8.  View Attendance Summary (All)", UIHelper.CYAN);
        printItem(w, "9.  Show Defaulters (Below 75%)", UIHelper.RED);
        printItem(w, "10. Logout", UIHelper.MAGENTA);
        System.out.println(UIHelper.BRIGHT_CYAN + "\u255a" + border + "\u255d" + UIHelper.RESET);
    }

    private void printItem(int w, String text, String color) {
        System.out.printf(UIHelper.BRIGHT_CYAN + "\u2551" + color + "  %-" + (w - 2) + "s"
                + UIHelper.BRIGHT_CYAN + "\u2551%n", text);
    }

    // =====================================================
    // STUDENT CRUD
    // =====================================================
    private void addStudent() {
        UIHelper.printSectionHeader("Add New Student", UIHelper.BRIGHT_GREEN);
        System.out.print("  Enter Roll No (e.g., 24B11CS165): ");
        String rollNo = scanner.nextLine().trim();
        if (rollNo.isEmpty()) {
            UIHelper.printError("Roll No cannot be empty!");
            return;
        }

        System.out.print("  Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("  Enter Class (e.g., CSE-A): ");
        String className = scanner.nextLine().trim();
        System.out.print("  Enter Department: ");
        String department = scanner.nextLine().trim();

        if (name.isEmpty() || className.isEmpty() || department.isEmpty()) {
            UIHelper.printError("All fields are required!");
            return;
        }

        Student student = new Student(rollNo, name, className, department);
        studentDAO.addStudent(student);

        // Auto-create student login account
        studentDAO.createStudentLogin(rollNo);
    }

    private void updateStudent() {
        UIHelper.printSectionHeader("Update Student", UIHelper.BRIGHT_YELLOW);
        System.out.print("  Enter Roll No of student to update: ");
        String rollNo = scanner.nextLine().trim();
        if (!studentDAO.studentExists(rollNo)) {
            UIHelper.printError("No student found with Roll No: " + rollNo);
            return;
        }

        System.out.print("  Enter New Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("  Enter New Class: ");
        String className = scanner.nextLine().trim();
        System.out.print("  Enter New Department: ");
        String department = scanner.nextLine().trim();

        if (name.isEmpty() || className.isEmpty() || department.isEmpty()) {
            UIHelper.printError("All fields are required!");
            return;
        }

        Student student = new Student(rollNo, name, className, department);
        studentDAO.updateStudent(student);
    }

    private void deleteStudent() {
        UIHelper.printSectionHeader("Delete Student", UIHelper.BRIGHT_RED);
        System.out.print("  Enter Roll No to delete: ");
        String rollNo = scanner.nextLine().trim();
        System.out.print("  Are you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes") || confirm.equals("y")) {
            studentDAO.deleteStudent(rollNo);
        } else {
            System.out.println("  Deletion cancelled.");
        }
    }

    private void searchStudent() {
        UIHelper.printSectionHeader("Search Student", UIHelper.BRIGHT_BLUE);
        System.out.print("  Enter Roll No or Name: ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) {
            UIHelper.printError("Please enter a search term.");
            return;
        }

        List<Student> results = studentDAO.searchStudent(keyword);
        if (results.isEmpty()) {
            System.out.println("\n  No students found matching: \"" + keyword + "\"");
        } else {
            System.out.println("\n  Found " + results.size() + " student(s):");
            for (Student s : results) {
                System.out.println("  " + s);
            }
        }
    }

    // =====================================================
    // ATTENDANCE
    // =====================================================
    private void markAttendance() {
        UIHelper.printSectionHeader("Mark Attendance (Single)", UIHelper.BRIGHT_GREEN);
        System.out.print("  Enter Roll No: ");
        String rollNo = scanner.nextLine().trim();
        if (!studentDAO.studentExists(rollNo)) {
            UIHelper.printError("No student found with Roll No: " + rollNo);
            return;
        }

        System.out.print("  Enter Date (YYYY-MM-DD) or press Enter for today: ");
        String dateStr = scanner.nextLine().trim();
        Date date;
        if (dateStr.isEmpty()) {
            date = new Date(System.currentTimeMillis());
        } else {
            try {
                date = Date.valueOf(dateStr);
            } catch (IllegalArgumentException e) {
                UIHelper.printError("Invalid date! Use YYYY-MM-DD.");
                return;
            }
        }

        int period = readInt("  Enter Period Number: ");

        System.out.print("  Enter Subject Name: ");
        String subject = scanner.nextLine().trim();
        if (subject.isEmpty()) {
            UIHelper.printError("Subject name cannot be empty!");
            return;
        }

        System.out.print("  Status - Present(P) or Absent(A): ");
        String status = scanner.nextLine().trim().toUpperCase();
        if (!status.equals("P") && !status.equals("A")) {
            UIHelper.printError("Invalid status! Enter P or A.");
            return;
        }

        attendanceDAO.markAttendance(rollNo, date, period, subject, status);
    }

    private void markBatchAttendance() {
        UIHelper.printSectionHeader("Mark Class Attendance (Batch)", UIHelper.BRIGHT_CYAN);
        System.out.print("  Enter Class Name (e.g., CSE-1): ");
        String className = scanner.nextLine().trim();

        List<String> students = studentDAO.getStudentsByClass(className);
        if (students.isEmpty()) {
            UIHelper.printError("No students in class: " + className);
            return;
        }
        System.out.println(UIHelper.GREEN + "  Found " + students.size() + " students." + UIHelper.RESET);

        System.out.print("  Enter Date (YYYY-MM-DD): ");
        String dateStr = scanner.nextLine().trim();
        Date date;
        try {
            date = Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            UIHelper.printError("Invalid date! Use YYYY-MM-DD.");
            return;
        }

        int period = readInt("  Enter Period Number: ");

        System.out.print("  Enter Subject Name: ");
        String subject = scanner.nextLine().trim();
        if (subject.isEmpty()) {
            UIHelper.printError("Subject name cannot be empty!");
            return;
        }

        System.out.println("\n  " + UIHelper.YELLOW + "Enter ABSENT roll numbers (comma separated):" + UIHelper.RESET);
        System.out.print("  > ");
        String absentInput = scanner.nextLine().trim();

        java.util.ArrayList<String> absentees = new java.util.ArrayList<>();
        if (!absentInput.isEmpty()) {
            for (String p : absentInput.split(",")) {
                String t = p.trim();
                if (!t.isEmpty())
                    absentees.add(t);
            }
        }

        attendanceDAO.markBatchAttendance(className, date, period, subject, students, absentees);
    }

    private void viewAttendance() {
        UIHelper.printSectionHeader("View Attendance", UIHelper.BRIGHT_YELLOW);
        System.out.print("  Enter Roll No: ");
        String rollNo = scanner.nextLine().trim();
        attendanceDAO.viewAttendance(rollNo);
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
