/*
 * ============================================================
 * ClassTrack – Student Attendance Management System
 * File: StudentDAO.java
 * Purpose: Data Access Object for Student CRUD operations.
 *          All database queries related to students are here.
 * ============================================================
 */

package classtrack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // =====================================================
    // 1. ADD STUDENT
    // =====================================================
    public void addStudent(Student student) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            // Check if roll number already exists
            String checkSQL = "SELECT roll_no FROM students WHERE roll_no = ?";
            checkStmt = conn.prepareStatement(checkSQL);
            checkStmt.setString(1, student.getRollNo());
            rs = checkStmt.executeQuery();

            if (rs.next()) {
                UIHelper.printError("ERROR: Roll No " + student.getRollNo() + " already exists!");
                UIHelper.printInfo("Please use a different roll number.");
                return;
            }

            // Insert the new student
            String insertSQL = "INSERT INTO students (roll_no, name, class_name, department) VALUES (?, ?, ?, ?)";
            insertStmt = conn.prepareStatement(insertSQL);
            insertStmt.setString(1, student.getRollNo());
            insertStmt.setString(2, student.getName());
            insertStmt.setString(3, student.getClassName());
            insertStmt.setString(4, student.getDepartment());

            int rowsInserted = insertStmt.executeUpdate();

            if (rowsInserted > 0) {
                UIHelper.printSuccess("Student added successfully!");
                System.out.println(UIHelper.CYAN + "  " + student + UIHelper.RESET);
            }

        } catch (SQLException e) {
            UIHelper.printError("ERROR while adding student: " + e.getMessage());

        } finally {
            closeResources(conn, checkStmt, rs);
            closeStatement(insertStmt);
        }
    }

    // =====================================================
    // 2. UPDATE STUDENT
    // =====================================================
    public void updateStudent(Student student) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            String sql = "UPDATE students SET name = ?, class_name = ?, department = ? WHERE roll_no = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getClassName());
            pstmt.setString(3, student.getDepartment());
            pstmt.setString(4, student.getRollNo());

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                UIHelper.printSuccess("Student updated successfully!");
                System.out.println(UIHelper.CYAN + "  " + student + UIHelper.RESET);
            } else {
                UIHelper.printError("No student found with Roll No: " + student.getRollNo());
            }

        } catch (SQLException e) {
            UIHelper.printError("ERROR while updating student: " + e.getMessage());

        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    // =====================================================
    // 3. DELETE STUDENT
    // =====================================================
    public void deleteStudent(String rollNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            String sql = "DELETE FROM students WHERE roll_no = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, rollNo);

            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                UIHelper.printSuccess("Student with Roll No " + rollNo + " deleted successfully!");
                UIHelper.printInfo("(Attendance records also removed automatically)");
            } else {
                UIHelper.printError("No student found with Roll No: " + rollNo);
            }

        } catch (SQLException e) {
            UIHelper.printError("ERROR while deleting student: " + e.getMessage());

        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    // =====================================================
    // 4. SEARCH STUDENT
    // =====================================================
    public List<Student> searchStudent(String keyword) {
        List<Student> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return results;

            // Search by roll number (exact or partial) or name (partial LIKE match)
            String sql = "SELECT * FROM students WHERE roll_no LIKE ? OR name LIKE ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Student student = new Student();
                student.setRollNo(rs.getString("roll_no"));
                student.setName(rs.getString("name"));
                student.setClassName(rs.getString("class_name"));
                student.setDepartment(rs.getString("department"));
                results.add(student);
            }

        } catch (SQLException e) {
            UIHelper.printError("ERROR while searching: " + e.getMessage());

        } finally {
            closeResources(conn, pstmt, rs);
        }

        return results;
    }

    // =====================================================
    // 5. CHECK IF STUDENT EXISTS
    // =====================================================
    public boolean studentExists(String rollNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return false;

            String sql = "SELECT roll_no FROM students WHERE roll_no = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            UIHelper.printError("ERROR: " + e.getMessage());
            return false;

        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // =====================================================
    // 6. GET STUDENTS BY CLASS (For Batch Attendance)
    // =====================================================
    public List<String> getStudentsByClass(String className) {
        List<String> rollNumbers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return rollNumbers;

            String sql = "SELECT roll_no FROM students WHERE class_name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, className);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                rollNumbers.add(rs.getString("roll_no"));
            }

        } catch (SQLException e) {
            UIHelper.printError("ERROR while fetching class students: " + e.getMessage());

        } finally {
            closeResources(conn, pstmt, rs);
        }

        return rollNumbers;
    }

    // =====================================================
    // 7. CREATE STUDENT LOGIN ACCOUNT
    // =====================================================
    public void createStudentLogin(String rollNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null)
                return;

            String sql = "INSERT INTO users (username, password, role, roll_no) VALUES (?, 'student123', 'STUDENT', ?) "
                    + "ON DUPLICATE KEY UPDATE username = username";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, rollNo);
            pstmt.setString(2, rollNo);
            pstmt.executeUpdate();

            UIHelper.printInfo("Student login created: Username = " + rollNo + " | Password = student123");

        } catch (SQLException e) {
            UIHelper.printWarning("Could not create student login: " + e.getMessage());

        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    // =====================================================
    // HELPER METHODS – Resource Cleanup
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
