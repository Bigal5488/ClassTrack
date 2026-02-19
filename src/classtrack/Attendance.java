/*
 * ============================================================
 * ClassTrack – Student Attendance Management System
 * File: Attendance.java
 * Purpose: Model/POJO class representing an Attendance record.
 * ============================================================
 */

package classtrack;

public class Attendance {

    // ----- Fields (mapped to 'attendance' table columns) -----
    private int attendanceId; // Primary Key
    private String rollNo; // Foreign Key – supports alphanumeric
    private int totalPeriods; // Total periods conducted
    private int presentPeriods; // Total periods student was present

    // ----- Default Constructor -----
    public Attendance() {
    }

    // ----- Parameterized Constructor -----
    public Attendance(int attendanceId, String rollNo, int totalPeriods, int presentPeriods) {
        this.attendanceId = attendanceId;
        this.rollNo = rollNo;
        this.totalPeriods = totalPeriods;
        this.presentPeriods = presentPeriods;
    }

    // ----- Getters and Setters -----

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public int getTotalPeriods() {
        return totalPeriods;
    }

    public void setTotalPeriods(int totalPeriods) {
        this.totalPeriods = totalPeriods;
    }

    public int getPresentPeriods() {
        return presentPeriods;
    }

    public void setPresentPeriods(int presentPeriods) {
        this.presentPeriods = presentPeriods;
    }

    /**
     * Dynamically calculates the attendance percentage.
     */
    public double getPercentage() {
        if (totalPeriods == 0) {
            return 0.0;
        }
        return ((double) presentPeriods / totalPeriods) * 100;
    }

    // ----- toString Method -----
    @Override
    public String toString() {
        return "| Roll No: " + rollNo
                + " | Total Periods: " + totalPeriods
                + " | Present: " + presentPeriods
                + " | Attendance: " + String.format("%.2f", getPercentage()) + "% |";
    }
}
