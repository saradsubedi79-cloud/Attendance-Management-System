package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model class representing a Student in the Attendance Management System.
 */
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentId;
    private String name;
    private String email;
    private String department;
    private String semester;

    public Student() {
    }

    public Student(String studentId, String name, String email, String department, String semester) {
        this.studentId = studentId != null ? studentId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.department = department != null ? department.trim() : "";
        this.semester = semester != null ? semester.trim() : "";
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId != null ? studentId.trim() : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim() : "";
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester != null ? semester.trim() : "";
    }

    /**
     * Converts the Student object into a CSV string line.
     */
    public String toCsv() {
        return escapeCsv(studentId) + "," +
               escapeCsv(name) + "," +
               escapeCsv(email) + "," +
               escapeCsv(department) + "," +
               escapeCsv(semester);
    }

    /**
     * Parses a Student object from a CSV string line.
     */
    public static Student fromCsv(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length < 5) {
            return null;
        }
        return new Student(
            unescapeCsv(parts[0]),
            unescapeCsv(parts[1]),
            unescapeCsv(parts[2]),
            unescapeCsv(parts[3]),
            unescapeCsv(parts[4])
        );
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String unescapeCsv(String value) {
        if (value == null) return "";
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId.toLowerCase(), student.studentId.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId != null ? studentId.toLowerCase() : "");
    }

    @Override
    public String toString() {
        return studentId + " - " + name + " (" + department + ")";
    }
}
