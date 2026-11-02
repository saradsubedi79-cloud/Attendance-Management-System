package gui;

import manager.AttendanceManager;
import manager.AttendanceManager.StudentAttendanceSummary;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Panel for generating attendance reports, statistics, analytics, and CSV exports.
 */
public class ReportPanel extends JPanel {
    private final AttendanceManager manager;
    private final Dashboard parentDashboard;

    // Filter controls
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JComboBox<String> cmbDepartment;
    private JCheckBox chkDefaultersOnly;
    private JTextField txtSearch;

    // KPI Cards
    private JLabel lblTotalAnalyzed;
    private JLabel lblAverageRate;
    private JLabel lblDefaultersCount;
    private JLabel lblEligibleCount;

    // Table
    private JTable reportTable;
    private DefaultTableModel tableModel;

    // UI Colors
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);
    private static final Color COLOR_WARNING = new Color(245, 158, 11);
    private static final Color COLOR_BG_LIGHT = new Color(241, 245, 249);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(203, 213, 225);
    private static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_MUTED = new Color(71, 85, 105);

    public ReportPanel(AttendanceManager manager, Dashboard parentDashboard) {
        this.manager = manager;
        this.parentDashboard = parentDashboard;
        initUI();
        refreshReport();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top Filter Bar
        JPanel filterCard = createFilterBar();
        add(filterCard, BorderLayout.NORTH);

        // Center Content (KPIs + Table)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setBackground(COLOR_BG_LIGHT);

        // KPI Ribbon
        JPanel kpiRibbon = createKpiRibbon();
        centerPanel.add(kpiRibbon, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = createTablePanel();
        centerPanel.add(tableCard, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createFilterBar() {
        JPanel card = new JPanel(new BorderLayout(12, 10));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel leftFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        leftFlow.setBackground(COLOR_CARD_BG);

        // Start Date
        JLabel lblFrom = new JLabel("From:");
        lblFrom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFrom.setForeground(COLOR_TEXT_MAIN);

        LocalDate initialStart = LocalDate.now().minusDays(30);
        Date startDate = Date.from(initialStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        startDateSpinner.setValue(startDate);
        startDateSpinner.setPreferredSize(new Dimension(115, 34));

        // End Date
        JLabel lblTo = new JLabel("To:");
        lblTo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTo.setForeground(COLOR_TEXT_MAIN);

        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        endDateSpinner.setValue(new Date());
        endDateSpinner.setPreferredSize(new Dimension(115, 34));

        // Department
        JLabel lblDept = new JLabel("Dept:");
        lblDept.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDept.setForeground(COLOR_TEXT_MAIN);

        cmbDepartment = new JComboBox<>();
        cmbDepartment.setPreferredSize(new Dimension(160, 34));
        cmbDepartment.setBackground(Color.WHITE);
        cmbDepartment.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshDepartmentFilter();

        // Checkbox Defaulters Only
        chkDefaultersOnly = new JCheckBox("Defaulters Only (< 75%)");
        chkDefaultersOnly.setBackground(COLOR_CARD_BG);
        chkDefaultersOnly.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkDefaultersOnly.setForeground(COLOR_DANGER);

        // Search text
        txtSearch = new JTextField(12);
        txtSearch.setPreferredSize(new Dimension(120, 34));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(2, 6, 2, 6)));

        JButton btnFilter = Dashboard.createPrimaryBtn("Generate Report");
        btnFilter.setPreferredSize(new Dimension(135, 34));
        btnFilter.addActionListener(e -> refreshReport());

        leftFlow.add(lblFrom);
        leftFlow.add(startDateSpinner);
        leftFlow.add(lblTo);
        leftFlow.add(endDateSpinner);
        leftFlow.add(lblDept);
        leftFlow.add(cmbDepartment);
        leftFlow.add(chkDefaultersOnly);
        leftFlow.add(txtSearch);
        leftFlow.add(btnFilter);

        // Right side: Export button with prominent Green styling
        JPanel rightFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        rightFlow.setBackground(COLOR_CARD_BG);

        JButton btnExport = Dashboard.createSuccessBtn("Export to CSV");
        btnExport.setPreferredSize(new Dimension(130, 34));
        btnExport.addActionListener(e -> handleExportCsv());
        rightFlow.add(btnExport);

        card.add(leftFlow, BorderLayout.CENTER);
        card.add(rightFlow, BorderLayout.EAST);

        return card;
    }

    private JPanel createKpiRibbon() {
        JPanel ribbon = new JPanel(new GridLayout(1, 4, 14, 0));
        ribbon.setBackground(COLOR_BG_LIGHT);

        lblTotalAnalyzed = new JLabel("0", SwingConstants.CENTER);
        lblAverageRate = new JLabel("0.0%", SwingConstants.CENTER);
        lblDefaultersCount = new JLabel("0", SwingConstants.CENTER);
        lblEligibleCount = new JLabel("0", SwingConstants.CENTER);

        ribbon.add(createKpiCard("Students Analyzed", lblTotalAnalyzed, COLOR_PRIMARY));
        ribbon.add(createKpiCard("Average Attendance", lblAverageRate, COLOR_SUCCESS));
        ribbon.add(createKpiCard("Defaulters (< 75%)", lblDefaultersCount, COLOR_DANGER));
        ribbon.add(createKpiCard("Eligible (>= 75%)", lblEligibleCount, new Color(13, 148, 136)));

        return ribbon;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        String[] columns = {
                "Student ID", "Full Name", "Department", "Semester",
                "Total Classes", "Present", "Absent", "Late", "Excused",
                "Attendance %", "Academic Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(32);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.setGridColor(new Color(241, 245, 249));

        JTableHeader header = reportTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_MAIN);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Center renderers for counts and percentages
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        reportTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

        reportTable.getColumnModel().getColumn(9).setCellRenderer(new PercentageCellRenderer());
        reportTable.getColumnModel().getColumn(10).setCellRenderer(new StatusBadgeCellRenderer());

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    public void refreshDepartmentFilter() {
        String current = (String) cmbDepartment.getSelectedItem();
        cmbDepartment.removeAllItems();
        cmbDepartment.addItem("All Departments");
        for (String d : manager.getAllDepartments()) {
            cmbDepartment.addItem(d);
        }
        if (current != null) {
            cmbDepartment.setSelectedItem(current);
        }
    }

    public void refreshReport() {
        LocalDate start = ((Date) startDateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = ((Date) endDateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String dept = (String) cmbDepartment.getSelectedItem();
        boolean defaultersOnly = chkDefaultersOnly.isSelected();
        String query = txtSearch.getText().trim().toLowerCase();

        List<StudentAttendanceSummary> summaries = manager.getAttendanceSummaries(start, end, dept);
        tableModel.setRowCount(0);

        int totalCount = 0;
        int defaultersCount = 0;
        int eligibleCount = 0;
        double totalPctSum = 0.0;

        for (StudentAttendanceSummary s : summaries) {
            if (defaultersOnly && !s.isDefaulter()) {
                continue;
            }

            if (!query.isEmpty()) {
                String id = s.getStudent().getStudentId().toLowerCase();
                String name = s.getStudent().getName().toLowerCase();
                if (!id.contains(query) && !name.contains(query)) {
                    continue;
                }
            }

            totalCount++;
            totalPctSum += s.getPercentage();
            if (s.isDefaulter()) {
                defaultersCount++;
            } else {
                eligibleCount++;
            }

            tableModel.addRow(new Object[]{
                    s.getStudent().getStudentId(),
                    s.getStudent().getName(),
                    s.getStudent().getDepartment(),
                    s.getStudent().getSemester(),
                    s.getTotalClasses(),
                    s.getPresentCount(),
                    s.getAbsentCount(),
                    s.getLateCount(),
                    s.getExcusedCount(),
                    String.format("%.1f%%", s.getPercentage()),
                    s.getEligibilityStatus()
            });
        }

        double avgRate = totalCount > 0 ? (totalPctSum / totalCount) : 0.0;
        lblTotalAnalyzed.setText(String.valueOf(totalCount));
        lblAverageRate.setText(String.format("%.1f%%", avgRate));
        lblDefaultersCount.setText(String.valueOf(defaultersCount));
        lblEligibleCount.setText(String.valueOf(eligibleCount));
    }

    private void handleExportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Attendance Report as CSV");
        fileChooser.setSelectedFile(new File("Attendance_Report_" + LocalDate.now() + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            LocalDate start = ((Date) startDateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = ((Date) endDateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String dept = (String) cmbDepartment.getSelectedItem();

            boolean ok = manager.exportReportToCsv(fileToSave, start, end, dept);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Report successfully exported to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to export report to file.",
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Cell Renderers
    private static class PercentageCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD));

            if (!isSelected && value != null) {
                try {
                    String str = value.toString().replace("%", "").trim();
                    double pct = Double.parseDouble(str);
                    if (pct < 75.0) {
                        setForeground(COLOR_DANGER);
                    } else if (pct >= 85.0) {
                        setForeground(COLOR_SUCCESS);
                    } else {
                        setForeground(COLOR_WARNING);
                    }
                } catch (Exception ignored) {
                }
            }
            return c;
        }
    }

    private static class StatusBadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD));

            if (!isSelected && value != null) {
                String status = value.toString();
                if (status.contains("Eligible")) {
                    setForeground(COLOR_SUCCESS);
                } else if (status.contains("Warning")) {
                    setForeground(COLOR_WARNING);
                } else {
                    setForeground(COLOR_DANGER);
                }
            }
            return c;
        }
    }
}
