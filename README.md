# ClassTrack - Student Attendance Management System

**ClassTrack** is a robust, console-based Java application designed to streamline student attendance management. It implements a secure Role-Based Access Control (RBAC) system, ensuring that Heads of Departments (HODs), Faculty, and Students have access to the features relevant to their roles.

## 🚀 Features

-   **Role-Based Access Control (RBAC)**: secure login and menu system for three distinct roles:
    -   **HOD (Head of Department)**: Full administrative control to manage students, faculty, and view comprehensive attendance records.
    -   **Faculty**: Ability to mark attendance for classes, view student lists, and generate attendance reports.
    -   **Student**: View personal attendance records, profile details, and login securely.
-   **Data Persistence**: Utilizes **MySQL** for reliable and scalable data storage.
-   **Interactive Console UI**: Features a clean, menu-driven command-line interface with color-coded feedback and formatted tables for better usability.
-   **Automated Database Setup**: Includes a SQL script for quick and easy database initialization.

## 🛠️ Technology Stack

-   **Language**: Java (JDK 8+)
-   **Database**: MySQL (8.0+)
-   **Connectivity**: JDBC (Java Database Connectivity)
-   **Driver**: MySQL Connector/J (Included in `lib/`)

## 📂 Project Structure

The project is organized efficiently for development and maintenance:

-   `src/`: Contains the Java source code, organized by packages (`classtrack`, `classtrack.hod`, etc.).
-   `bin/`: Destination for compiled Java bytecode (`.class` files).
-   `lib/`: Contains external libraries, specifically the MySQL JDBC driver (`mysql-connector-j-8.3.0.jar`).
-   `setup.sql`: SQL script to initialize the database, tables, and default users.

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

1.  **Java Development Kit (JDK)**: Version 8 or higher.
2.  **MySQL Server**: A running instance of MySQL.

## ⚙️ Installation & Setup

### 1. Clone the Repository
Download or clone this repository to your local machine.
```bash
git clone https://github.com/your-username/ClassTrack.git
cd ClassTrack
```

### 2. Database Setup
You need to create the database and tables before running the application.

1.  Open your MySQL client (Workbench, Command Line, etc.).
2.  Run the provided `setup.sql` script.
    -   **MySQL Command Line**:
        ```bash
        mysql -u root -p < setup.sql
        ```
    -   **MySQL Workbench**: Open `setup.sql` and click the "Execute" (Lightning bolt) button.

### 3. Configure Database Credentials
By default, the application connects with:
-   **Username**: `root`
-   **Password**: `12345`

If your MySQL credentials differ:
1.  Open `src/classtrack/DBConnection.java`.
2.  Update the `USERNAME` and `PASSWORD` constants to match your local MySQL server.
3.  Save the file.

## ▶️ Usage Guide

### 1. Compile the Application
Open a terminal in the project root directory and run the following command to compile all source files:

```bash
javac -d bin -cp ".;lib/mysql-connector-j-8.3.0.jar" src/classtrack/*.java src/classtrack/hod/*.java src/classtrack/faculty/*.java src/classtrack/student/*.java
```
*(Note: On Linux/Mac, replace `;` with `:` in the classpath)*

### 2. Run the Application
Start the application with:
```bash
java -cp "bin;lib/mysql-connector-j-8.3.0.jar" classtrack.MainMenu
```

## 🔐 Login Credentials

The `setup.sql` script creates the following default accounts for testing:

| Role | Username | Password |
| :--- | :--- | :--- |
| **HOD** | `hod` | `hod123` |
| **Faculty** | `faculty1` | `faculty123` |
| **Student** | *(Requires Roll No)* | `student123` |

*Note: Student accounts are linked to a specific Roll Number. You may need to create a student profile via the HOD or Faculty menu first, or verify the `users` table for existing student web logins.*
