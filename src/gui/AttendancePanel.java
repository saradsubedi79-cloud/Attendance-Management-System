package gui;

import manager.AttendanceManager;
import model.Attendance;
import model.Student;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Panel for marking and updating daily student attendance records.
 */
public class AttendancePanel extends JPanel {
    private final AttendanceManager manager;
    private final Dashboard parentDashboard;

    // Date & Filters
    private JSpinner dateSpinner;
    private JComboBox<String> cmbDepartmentFilter;
    private JButton btnToday;
    private JButton btnYesterday;

    // Bulk buttons
    private JButton btnAllPresent;
    private JButton btnAllAbsent;

    // Table
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    // Bottom Action & Summary
    private JButton btnSaveAttendance;
    private JLabel lblDaySummary;

    // UI Colors
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);
    private static final Color COLOR_WARNING = new Color(245, 158, 11);
    private static final Color COLOR_BG_LIGHT = new Color(241, 245, 249);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(203, 213, 225);
    private static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);

    public AttendancePanel(AttendanceManager manager, Dashboard parentDashboard) {
        this.manager = manager;
        this.parentDashboard = parentDashboard;
        initUI();
        loadAttendanceForSelectedDate();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top Filter & Control Card
        JPanel topCard = createTopControlPanel();
        add(topCard, BorderLayout.NORTH);

        // Center Table Card
        JPanel centerCard = createTablePanel();
        add(centerCard, BorderLayout.CENTER);

        // Bottom Action Card
        JPanel bottomCard = createBottomPanel();
        add(bottomCard, BorderLayout.SOUTH);
    }

    private JPanel createTopControlPanel() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Left: Date selection & Department
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setBackground(COLOR_CARD_BG);

        JLabel lblDate = new JLabel("Attendance Date:");
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDate.setForeground(COLOR_TEXT_MAIN);

        // Date Spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        dateSpinner.setPreferredSize(new Dimension(120, 34));
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateSpinner.addChangeListener(e -> loadAttendanceForSelectedDate());

        btnToday = Dashboard.createSecondaryBtn("Today");
        btnToday.setPreferredSize(new Dimension(75, 34));
        btnToday.addActionListener(e -> {
            dateSpinner.setValue(new Date());
            loadAttendanceForSelectedDate();
        });

        btnYesterday = Dashboard.createSecondaryBtn("Yesterday");
        btnYesterday.setPreferredSize(new Dimension(90, 34));
        btnYesterday.addActionListener(e -> {
            LocalDate y = LocalDate.now().minusDays(1);
            dateSpinner.setValue(Date.from(y.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            loadAttendanceForSelectedDate();
        });

        JLabel lblDept = new JLabel("Filter Dept:");
        lblDept.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDept.setForeground(COLOR_TEXT_MAIN);

        cmbDepartmentFilter = new JComboBox<>();
        cmbDepartmentFilter.setPreferredSize(new Dimension(170, 34));
        cmbDepartmentFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbDepartmentFilter.setBackground(Color.WHITE);
        refreshDepartmentFilter();
        cmbDepartmentFilter.addActionListener(e -> loadAttendanceForSelectedDate());

        leftControls.add(lblDate);
        leftControls.add(dateSpinner);
        leftControls.add(btnToday);
        leftControls.add(btnYesterday);
        leftControls.add(Box.createHorizontalStrut(10));
        leftControls.add(lblDept);
        leftControls.add(cmbDepartmentFilter);

        // Right: Batch Action buttons with high visibility
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setBackground(COLOR_CARD_BG);

        btnAllPresent = Dashboard.createSuccessBtn("Mark All Present");
        btnAllPresent.setPreferredSize(new Dimension(140, 34));
        btnAllPresent.addActionListener(e -> setAllStatuses(Attendance.STATUS_PRESENT));

        btnAllAbsent = Dashboard.createDangerBtn("Mark All Absent");
        btnAllAbsent.setPreferredSize(new Dimension(140, 34));
        btnAllAbsent.addActionListener(e -> setAllStatuses(Attendance.STATUS_ABSENT));

        rightControls.add(btnAllPresent);
        rightControls.add(btnAllAbsent);

        card.add(leftControls, BorderLayout.CENTER);
        card.add(rightControls, BorderLayout.EAST);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        String[] columns = {"Student ID", "Full Name", "Department", "Status", "Remarks / Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4;
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(34);
        attendanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceTable.setGridColor(new Color(241, 245, 249));

        // Header Styling
        JTableHeader header = attendanceTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_MAIN);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Column widths
        TableColumn colId = attendanceTable.getColumnModel().getColumn(0);
        colId.setPreferredWidth(110);
        colId.setMaxWidth(130);

        TableColumn colName = attendanceTable.getColumnModel().getColumn(1);
        colName.setPreferredWidth(180);

        TableColumn colDept = attendanceTable.getColumnModel().getColumn(2);
        colDept.setPreferredWidth(160);

        TableColumn colStatus = attendanceTable.getColumnModel().getColumn(3);
        colStatus.setPreferredWidth(140);
        colStatus.setMaxWidth(160);

        // Status Combo Cell Editor
        String[] statuses = {
                Attendance.STATUS_PRESENT,
                Attendance.STATUS_ABSENT,
                Attendance.STATUS_LATE,
                Attendance.STATUS_EXCUSED
        };
        JComboBox<String> statusEditorCombo = new JComboBox<>(statuses);
        statusEditorCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        colStatus.setCellEditor(new DefaultCellEditor(statusEditorCombo));
        colStatus.setCellRenderer(new StatusCellRenderer());

        // Center Student ID
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        colId.setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBottomPanel() {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        lblDaySummary = new JLabel("Summary: Total 0 | Present: 0 | Absent: 0 | Late: 0");
        lblDaySummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDaySummary.setForeground(COLOR_TEXT_MAIN);

        btnSaveAttendance = Dashboard.createPrimaryBtn("Save Attendance Records");
        btnSaveAttendance.setPreferredSize(new Dimension(210, 38));
        btnSaveAttendance.addActionListener(e -> handleSaveAttendance());

        card.add(lblDaySummary, BorderLayout.WEST);
        card.add(btnSaveAttendance, BorderLayout.EAST);

        return card;
    }

    public LocalDate getSelectedDate() {
        Date d = (Date) dateSpinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void refreshDepartmentFilter() {
        String currentSelection = (String) cmbDepartmentFilter.getSelectedItem();
        cmbDepartmentFilter.removeAllItems();
        cmbDepartmentFilter.addItem("All Departments");
        for (String dept : manager.getAllDepartments()) {
            cmbDepartmentFilter.addItem(dept);
        }
        if (currentSelection != null) {
            cmbDepartmentFilter.setSelectedItem(currentSelection);
        }
    }

    public void loadAttendanceForSelectedDate() {
        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        LocalDate selectedDate = getSelectedDate();
        String selectedDept = (String) cmbDepartmentFilter.getSelectedItem();
        boolean filterDept = selectedDept != null && !selectedDept.equals("All Departments");

        List<Student> students = manager.getAllStudents();
        tableModel.setRowCount(0);

        int presentCount = 0;
        int absentCount = 0;
        int lateCount = 0;
        int excusedCount = 0;

        for (Student s : students) {
            if (filterDept && !s.getDepartment().equalsIgnoreCase(selectedDept)) {
                continue;
            }

            Attendance existing = manager.getAttendance(s.getStudentId(), selectedDate);
            String status = existing != null ? existing.getStatus() : Attendance.STATUS_PRESENT;
            String remarks = existing != null ? existing.getRemarks() : "";

            tableModel.addRow(new Object[]{
                    s.getStudentId(),
                    s.getName(),
                    s.getDepartment(),
                    status,
                    remarks
            });

            switch (status) {
                case Attendance.STATUS_PRESENT:
                    presentCount++;
                    break;
                case Attendance.STATUS_ABSENT:
                    absentCount++;
                    break;
                case Attendance.STATUS_LATE:
                    lateCount++;
                    break;
                case Attendance.STATUS_EXCUSED:
                    excusedCount++;
                    break;
            }
        }

        int total = tableModel.getRowCount();
        double rate = total > 0 ? ((double) (presentCount + lateCount) / total * 100.0) : 0.0;
        lblDaySummary.setText(String.format("Date: %s  |  Total: %d  |  Present: %d  |  Absent: %d  |  Late: %d  |  Rate: %.1f%%",
                selectedDate, total, presentCount, absentCount, lateCount, rate));
    }

    private void setAllStatuses(String status) {
        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(status, i, 3);
        }
    }

    private void handleSaveAttendance() {
        if (attendanceTable.isEditing()) {
            attendanceTable.getCellEditor().stopCellEditing();
        }

        LocalDate date = getSelectedDate();
        int rows = tableModel.getRowCount();

        for (int i = 0; i < rows; i++) {
            String studentId = tableModel.getValueAt(i, 0).toString();
            String status = tableModel.getValueAt(i, 3).toString();
            Object remarksObj = tableModel.getValueAt(i, 4);
            String remarks = remarksObj != null ? remarksObj.toString() : "";

            manager.recordAttendance(studentId, date, status, remarks);
        }

        JOptionPane.showMessageDialog(this,
                "Attendance successfully saved for " + date + " (" + rows + " records updated).",
                "Attendance Saved", JOptionPane.INFORMATION_MESSAGE);

        loadAttendanceForSelectedDate();

        if (parentDashboard != null) {
            parentDashboard.notifyDataChanged();
        }
    }

    // Custom cell renderer for status tag
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD));

            if (!isSelected && value != null) {
                String status = value.toString();
                switch (status) {
                    case Attendance.STATUS_PRESENT:
                        setForeground(COLOR_SUCCESS);
                        break;
                    case Attendance.STATUS_ABSENT:
                        setForeground(COLOR_DANGER);
                        break;
                    case Attendance.STATUS_LATE:
                        setForeground(COLOR_WARNING);
                        break;
                    case Attendance.STATUS_EXCUSED:
                        setForeground(new Color(59, 130, 246));
                        break;
                    default:
                        setForeground(COLOR_TEXT_MAIN);
                }
            }
            return c;
        }
    }
}
