package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Model class representing an Attendance record.
 */
public class Attendance implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final String STATUS_PRESENT = "Present";
    public static final String STATUS_ABSENT = "Absent";
    public static final String STATUS_LATE = "Late";
    public static final String STATUS_EXCUSED = "Excused";

    private String studentId;
    private LocalDate date;
    private String status;
    private String remarks;

    public Attendance() {
        this.date = LocalDate.now();
        this.status = STATUS_PRESENT;
        this.remarks = "";
    }

    public Attendance(String studentId, LocalDate date, String status, String remarks) {
        this.studentId = studentId != null ? studentId.trim() : "";
        this.date = date != null ? date : LocalDate.now();
        this.status = normalizeStatus(status);
        this.remarks = remarks != null ? remarks.trim() : "";
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId != null ? studentId.trim() : "";
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date != null ? date : LocalDate.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalizeStatus(status);
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks != null ? remarks.trim() : "";
    }

    public static String normalizeStatus(String status) {
        if (status == null) return STATUS_PRESENT;
        String s = status.trim();
        if (s.equalsIgnoreCase(STATUS_PRESENT)) return STATUS_PRESENT;
        if (s.equalsIgnoreCase(STATUS_ABSENT)) return STATUS_ABSENT;
        if (s.equalsIgnoreCase(STATUS_LATE)) return STATUS_LATE;
        if (s.equalsIgnoreCase(STATUS_EXCUSED)) return STATUS_EXCUSED;
        return STATUS_PRESENT;
    }

    /**
     * Converts the Attendance object into a CSV string line.
     */
    public String toCsv() {
        return escapeCsv(studentId) + "," +
               escapeCsv(date.format(DATE_FORMATTER)) + "," +
               escapeCsv(status) + "," +
               escapeCsv(remarks);
    }

    /**
     * Parses an Attendance record from a CSV line.
     */
    public static Attendance fromCsv(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length < 3) {
            return null;
        }
        try {
            String studentId = unescapeCsv(parts[0]);
            LocalDate date = LocalDate.parse(unescapeCsv(parts[1]), DATE_FORMATTER);
            String status = unescapeCsv(parts[2]);
            String remarks = parts.length > 3 ? unescapeCsv(parts[3]) : "";
            return new Attendance(studentId, date, status, remarks);
        } catch (Exception e) {
            return null;
        }
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
        Attendance that = (Attendance) o;
        return Objects.equals(studentId.toLowerCase(), that.studentId.toLowerCase()) &&
               Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId != null ? studentId.toLowerCase() : "", date);
    }

    @Override
    public String toString() {
        return studentId + " [" + date.format(DATE_FORMATTER) + "] : " + status +
               (remarks.isEmpty() ? "" : " (" + remarks + ")");
    }
}
