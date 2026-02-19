/*
 * ============================================================
 * ClassTrack – Student Attendance Management System
 * File: Student.java
 * Purpose: Model/POJO class representing a Student entity.
 * ============================================================
 */

package classtrack;

public class Student {

    // ----- Fields (mapped to 'students' table columns) -----
    private String rollNo; // Primary Key – supports alphanumeric (e.g., 24B11CS165)
    private String name; // Student's full name
    private String className; // Class (e.g., "CSE-A", "ECE-B")
    private String department; // Department (e.g., "Computer Science")

    // ----- Default Constructor -----
    public Student() {
    }

    // ----- Parameterized Constructor -----
    public Student(String rollNo, String name, String className, String department) {
        this.rollNo = rollNo;
        this.name = name;
        this.className = className;
        this.department = department;
    }

    // ----- Getters and Setters -----

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // ----- toString Method (for easy printing) -----
    @Override
    public String toString() {
        return "| Roll No: " + rollNo
                + " | Name: " + name
                + " | Class: " + className
                + " | Department: " + department + " |";
    }
}
