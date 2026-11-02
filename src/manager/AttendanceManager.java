package manager;

import exception.DuplicateStudentException;
import model.Attendance;
import model.Student;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business and data persistence manager for the Attendance Management System.
 */
public class AttendanceManager {
    private static final String DATA_DIR = "data";
    private static final String STUDENTS_FILE = DATA_DIR + File.separator + "students.csv";
    private static final String ATTENDANCE_FILE = DATA_DIR + File.separator + "attendance.csv";

    private final Map<String, Student> studentMap = new LinkedHashMap<>();
    private final List<Attendance> attendanceList = new ArrayList<>();

    public AttendanceManager() {
        ensureDataDirectoryExists();
        loadData();
    }

    private void ensureDataDirectoryExists() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // =========================================================================
    // Student Management
    // =========================================================================

    public synchronized void addStudent(Student student) throws DuplicateStudentException {
        if (student == null || student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        String key = student.getStudentId().trim().toUpperCase();
        if (studentMap.containsKey(key)) {
            throw new DuplicateStudentException(student.getStudentId());
        }
        studentMap.put(key, student);
        saveStudents();
    }

    public synchronized boolean updateStudent(Student student) {
        if (student == null || student.getStudentId() == null) {
            return false;
        }
        String key = student.getStudentId().trim().toUpperCase();
        if (!studentMap.containsKey(key)) {
            return false;
        }
        studentMap.put(key, student);
        saveStudents();
        return true;
    }

    public synchronized boolean deleteStudent(String studentId) {
        if (studentId == null) return false;
        String key = studentId.trim().toUpperCase();
        if (!studentMap.containsKey(key)) {
            return false;
        }
        studentMap.remove(key);
        // Also remove attendance records for this student
        attendanceList.removeIf(a -> a.getStudentId().equalsIgnoreCase(studentId));
        saveStudents();
        saveAttendance();
        return true;
    }

    public synchronized Student getStudent(String studentId) {
        if (studentId == null) return null;
        return studentMap.get(studentId.trim().toUpperCase());
    }

    public synchronized List<Student> getAllStudents() {
        return new ArrayList<>(studentMap.values());
    }

    public synchronized List<Student> searchStudents(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllStudents();
        }
        String q = query.trim().toLowerCase();
        return studentMap.values().stream()
                .filter(s -> s.getStudentId().toLowerCase().contains(q) ||
                             s.getName().toLowerCase().contains(q) ||
                             s.getDepartment().toLowerCase().contains(q) ||
                             s.getEmail().toLowerCase().contains(q) ||
                             s.getSemester().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public synchronized List<String> getAllDepartments() {
        Set<String> depts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Student s : studentMap.values()) {
            if (!s.getDepartment().isEmpty()) {
                depts.add(s.getDepartment());
            }
        }
        return new ArrayList<>(depts);
    }

    public synchronized int getTotalStudentsCount() {
        return studentMap.size();
    }

    // =========================================================================
    // Attendance Management
    // =========================================================================

    public synchronized void recordAttendance(String studentId, LocalDate date, String status, String remarks) {
        if (studentId == null || date == null) return;
        String normalizedStatus = Attendance.normalizeStatus(status);

        // Check if record already exists for this student on this date
        Optional<Attendance> existing = attendanceList.stream()
                .filter(a -> a.getStudentId().equalsIgnoreCase(studentId) && a.getDate().equals(date))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setStatus(normalizedStatus);
            existing.get().setRemarks(remarks != null ? remarks.trim() : "");
        } else {
            attendanceList.add(new Attendance(studentId, date, normalizedStatus, remarks));
        }
        saveAttendance();
    }

    public synchronized Attendance getAttendance(String studentId, LocalDate date) {
        if (studentId == null || date == null) return null;
        return attendanceList.stream()
                .filter(a -> a.getStudentId().equalsIgnoreCase(studentId) && a.getDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    public synchronized List<Attendance> getAttendanceByDate(LocalDate date) {
        if (date == null) return Collections.emptyList();
        return attendanceList.stream()
                .filter(a -> a.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public synchronized List<Attendance> getAttendanceByStudent(String studentId) {
        if (studentId == null) return Collections.emptyList();
        return attendanceList.stream()
                .filter(a -> a.getStudentId().equalsIgnoreCase(studentId))
                .sorted(Comparator.comparing(Attendance::getDate).reversed())
                .collect(Collectors.toList());
    }

    public synchronized List<Attendance> getAllAttendance() {
        return new ArrayList<>(attendanceList);
    }

    public synchronized void deleteAttendance(String studentId, LocalDate date) {
        if (studentId == null || date == null) return;
        boolean removed = attendanceList.removeIf(a -> a.getStudentId().equalsIgnoreCase(studentId) && a.getDate().equals(date));
        if (removed) {
            saveAttendance();
        }
    }

    // =========================================================================
    // Analytics & Metrics
    // =========================================================================

    public synchronized long getTodayPresentCount(LocalDate date) {
        return attendanceList.stream()
                .filter(a -> a.getDate().equals(date) &&
                             (a.getStatus().equals(Attendance.STATUS_PRESENT) || a.getStatus().equals(Attendance.STATUS_LATE)))
                .count();
    }

    public synchronized long getTodayAbsentCount(LocalDate date) {
        return attendanceList.stream()
                .filter(a -> a.getDate().equals(date) && a.getStatus().equals(Attendance.STATUS_ABSENT))
                .count();
    }

    public synchronized double getOverallAttendancePercentage() {
        if (attendanceList.isEmpty()) return 100.0;
        long attended = attendanceList.stream()
                .filter(a -> a.getStatus().equals(Attendance.STATUS_PRESENT) || a.getStatus().equals(Attendance.STATUS_LATE))
                .count();
        return (double) attended / attendanceList.size() * 100.0;
    }

    public synchronized double getStudentAttendancePercentage(String studentId) {
        List<Attendance> records = getAttendanceByStudent(studentId);
        if (records.isEmpty()) return 100.0;
        long attended = records.stream()
                .filter(a -> a.getStatus().equals(Attendance.STATUS_PRESENT) || a.getStatus().equals(Attendance.STATUS_LATE))
                .count();
        return (double) attended / records.size() * 100.0;
    }

    // =========================================================================
    // Reporting Data Transfer Object
    // =========================================================================

    public static class StudentAttendanceSummary {
        private final Student student;
        private final int totalClasses;
        private final int presentCount;
        private final int absentCount;
        private final int lateCount;
        private final int excusedCount;
        private final double percentage;

        public StudentAttendanceSummary(Student student, int totalClasses, int presentCount,
                                      int absentCount, int lateCount, int excusedCount) {
            this.student = student;
            this.totalClasses = totalClasses;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.lateCount = lateCount;
            this.excusedCount = excusedCount;
            int attended = presentCount + lateCount;
            this.percentage = totalClasses > 0 ? ((double) attended / totalClasses * 100.0) : 100.0;
        }

        public Student getStudent() { return student; }
        public int getTotalClasses() { return totalClasses; }
        public int getPresentCount() { return presentCount; }
        public int getAbsentCount() { return absentCount; }
        public int getLateCount() { return lateCount; }
        public int getExcusedCount() { return excusedCount; }
        public double getPercentage() { return percentage; }
        public boolean isDefaulter() { return percentage < 75.0; }
        public String getEligibilityStatus() {
            if (percentage >= 75.0) return "Eligible";
            if (percentage >= 65.0) return "Warning (<75%)";
            return "Defaulter / Barred";
        }
    }

    public synchronized List<StudentAttendanceSummary> getAttendanceSummaries(LocalDate startDate, LocalDate endDate, String departmentFilter) {
        List<StudentAttendanceSummary> summaries = new ArrayList<>();

        for (Student s : studentMap.values()) {
            if (departmentFilter != null && !departmentFilter.equalsIgnoreCase("All") && !departmentFilter.trim().isEmpty()) {
                if (!s.getDepartment().equalsIgnoreCase(departmentFilter.trim())) {
                    continue;
                }
            }

            List<Attendance> records = attendanceList.stream()
                    .filter(a -> a.getStudentId().equalsIgnoreCase(s.getStudentId()))
                    .filter(a -> (startDate == null || !a.getDate().isBefore(startDate)) &&
                                 (endDate == null || !a.getDate().isAfter(endDate)))
                    .collect(Collectors.toList());

            int total = records.size();
            int present = 0;
            int absent = 0;
            int late = 0;
            int excused = 0;

            for (Attendance a : records) {
                switch (a.getStatus()) {
                    case Attendance.STATUS_PRESENT:
                        present++;
                        break;
                    case Attendance.STATUS_ABSENT:
                        absent++;
                        break;
                    case Attendance.STATUS_LATE:
                        late++;
                        break;
                    case Attendance.STATUS_EXCUSED:
                        excused++;
                        break;
                }
            }

            summaries.add(new StudentAttendanceSummary(s, total, present, absent, late, excused));
        }

        return summaries;
    }

    public synchronized List<StudentAttendanceSummary> getDefaulters(double thresholdPercent, LocalDate startDate, LocalDate endDate) {
        return getAttendanceSummaries(startDate, endDate, "All").stream()
                .filter(sum -> sum.getTotalClasses() > 0 && sum.getPercentage() < thresholdPercent)
                .collect(Collectors.toList());
    }

    public synchronized boolean exportReportToCsv(File targetFile, LocalDate fromDate, LocalDate toDate, String department) {
        List<StudentAttendanceSummary> summaries = getAttendanceSummaries(fromDate, toDate, department);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            writer.write("Student ID,Name,Department,Semester,Total Classes,Present,Absent,Late,Excused,Percentage,Status");
            writer.newLine();

            for (StudentAttendanceSummary s : summaries) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,%d,%d,%.2f%%,\"%s\"",
                        s.getStudent().getStudentId(),
                        s.getStudent().getName(),
                        s.getStudent().getDepartment(),
                        s.getStudent().getSemester(),
                        s.getTotalClasses(),
                        s.getPresentCount(),
                        s.getAbsentCount(),
                        s.getLateCount(),
                        s.getExcusedCount(),
                        s.getPercentage(),
                        s.getEligibilityStatus()
                ));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // Persistence & Sample Data Seeding
    // =========================================================================

    public synchronized void loadData() {
        loadStudents();
        loadAttendance();

        if (studentMap.isEmpty()) {
            seedSampleData();
        }
    }

    private void loadStudents() {
        File file = new File(STUDENTS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Student s = Student.fromCsv(line);
                if (s != null && !s.getStudentId().isEmpty()) {
                    studentMap.put(s.getStudentId().toUpperCase(), s);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading students file: " + e.getMessage());
        }
    }

    public synchronized void saveStudents() {
        ensureDataDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(STUDENTS_FILE), StandardCharsets.UTF_8))) {
            for (Student s : studentMap.values()) {
                writer.write(s.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing students file: " + e.getMessage());
        }
    }

    private void loadAttendance() {
        File file = new File(ATTENDANCE_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Attendance a = Attendance.fromCsv(line);
                if (a != null && !a.getStudentId().isEmpty()) {
                    attendanceList.add(a);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading attendance file: " + e.getMessage());
        }
    }

    public synchronized void saveAttendance() {
        ensureDataDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ATTENDANCE_FILE), StandardCharsets.UTF_8))) {
            for (Attendance a : attendanceList) {
                writer.write(a.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing attendance file: " + e.getMessage());
        }
    }

    /**
     * Seeds initial realistic data so the application is ready to demo out-of-the-box.
     */
    private void seedSampleData() {
        List<Student> initialStudents = Arrays.asList(
            new Student("STU-1001", "Aarav Sharma", "aarav.sharma@college.edu", "Computer Science", "Sem 4"),
            new Student("STU-1002", "Priya Patel", "priya.patel@college.edu", "Computer Science", "Sem 4"),
            new Student("STU-1003", "Rohan Verma", "rohan.verma@college.edu", "Information Technology", "Sem 4"),
            new Student("STU-1004", "Ananya Iyer", "ananya.iyer@college.edu", "Information Technology", "Sem 4"),
            new Student("STU-1005", "Vikram Singh", "vikram.singh@college.edu", "Software Engineering", "Sem 6"),
            new Student("STU-1006", "Sneha Joshi", "sneha.joshi@college.edu", "Software Engineering", "Sem 6"),
            new Student("STU-1007", "Kabir Nair", "kabir.nair@college.edu", "AI & Data Science", "Sem 2"),
            new Student("STU-1008", "Diya Deshmukh", "diya.deshmukh@college.edu", "AI & Data Science", "Sem 2"),
            new Student("STU-1009", "Arjun Reddy", "arjun.reddy@college.edu", "Computer Science", "Sem 4"),
            new Student("STU-1010", "Meera Sen", "meera.sen@college.edu", "Computer Science", "Sem 4"),
            new Student("STU-1011", "Karan Malhotra", "karan.malhotra@college.edu", "Software Engineering", "Sem 6"),
            new Student("STU-1012", "Ishita Gupta", "ishita.gupta@college.edu", "Information Technology", "Sem 4")
        );

        for (Student s : initialStudents) {
            studentMap.put(s.getStudentId().toUpperCase(), s);
        }

        // Seed 10 days of attendance history up to today
        LocalDate today = LocalDate.now();
        Random random = new Random(42); // deterministic seed for consistent nice data

        for (int dayOffset = 9; dayOffset >= 0; dayOffset--) {
            LocalDate date = today.minusDays(dayOffset);
            // Skip weekends
            if (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7) {
                continue;
            }

            for (Student s : initialStudents) {
                String id = s.getStudentId();
                String status;
                String remarks = "";

                // Give different profiles to students (some high attendance, one or two defaulters for realistic reports)
                if (id.equals("STU-1009") || id.equals("STU-1011")) {
                    // Lower attendance students
                    int r = random.nextInt(100);
                    if (r < 55) {
                        status = Attendance.STATUS_ABSENT;
                        remarks = "Unexcused Absence";
                    } else if (r < 70) {
                        status = Attendance.STATUS_LATE;
                        remarks = "Late by 15 mins";
                    } else {
                        status = Attendance.STATUS_PRESENT;
                    }
                } else {
                    // Regular high attendance students
                    int r = random.nextInt(100);
                    if (r < 85) {
                        status = Attendance.STATUS_PRESENT;
                    } else if (r < 93) {
                        status = Attendance.STATUS_LATE;
                        remarks = "Bus delay";
                    } else if (r < 97) {
                        status = Attendance.STATUS_EXCUSED;
                        remarks = "Medical leave";
                    } else {
                        status = Attendance.STATUS_ABSENT;
                        remarks = "Personal leave";
                    }
                }

                attendanceList.add(new Attendance(id, date, status, remarks));
            }
        }

        saveStudents();
        saveAttendance();
    }
}
