-- ============================================================
-- ClassTrack – Student Attendance Management System
-- Database Setup Script (with Role-Based Access Control)
-- ============================================================
-- HOW TO RUN THIS SCRIPT:
--   Option 1: Open MySQL Workbench → File → Open SQL Script → Run
--   Option 2: MySQL Command Line →  source C:/path/to/setup.sql
-- ============================================================

-- Step 1: Create the database (Fresh Start)
DROP DATABASE IF EXISTS classtrack_db;
CREATE DATABASE classtrack_db;
USE classtrack_db;

-- Step 2: Create the 'users' table (with role support)
CREATE TABLE IF NOT EXISTS users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    role       ENUM('HOD', 'FACULTY', 'STUDENT') NOT NULL,
    roll_no    VARCHAR(20)  NULL
);

-- Step 3: Create the 'students' table
CREATE TABLE IF NOT EXISTS students (
    roll_no    VARCHAR(20)  PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    class_name VARCHAR(50)  NOT NULL,
    department VARCHAR(100) NOT NULL
);

-- Step 4: Create the 'attendance' table
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no       VARCHAR(20) NOT NULL,
    total_periods INT NOT NULL DEFAULT 0,
    present_periods INT NOT NULL DEFAULT 0,
    FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE
);

-- Step 4b: Create 'attendance_log' for period-wise tracking
CREATE TABLE IF NOT EXISTS attendance_log (
    log_id      INT AUTO_INCREMENT PRIMARY KEY,
    roll_no     VARCHAR(20) NOT NULL,
    date        DATE NOT NULL,
    period      INT NOT NULL,
    status      ENUM('P', 'A') NOT NULL,
    FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE,
    UNIQUE(roll_no, date, period)
);

-- Step 5: Insert default credentials for each role
-- HOD (Head of Department) – Full Access
INSERT INTO users (username, password, role) VALUES ('hod', 'hod123', 'HOD');

-- Faculty – Can add students & mark attendance
INSERT INTO users (username, password, role) VALUES ('faculty1', 'faculty123', 'FACULTY');
INSERT INTO users (username, password, role) VALUES ('faculty2', 'faculty123', 'FACULTY');

-- Student accounts (linked to roll_no, created when student is added)
-- Example: INSERT INTO users (username, password, role, roll_no) VALUES ('24B11CS165', 'student123', 'STUDENT', '24B11CS165');

-- ============================================================
-- Setup complete!
-- Default Logins:
--   HOD:     Username: hod       | Password: hod123
--   Faculty: Username: faculty1  | Password: faculty123
--   Student: Username: <roll_no> | Password: student123
-- ============================================================
