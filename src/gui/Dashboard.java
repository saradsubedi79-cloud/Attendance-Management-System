package gui;

import manager.AttendanceManager;
import manager.AttendanceManager.StudentAttendanceSummary;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main Application Dashboard Frame for the Attendance Management System.
 */
public class Dashboard extends JFrame {
    private final AttendanceManager manager;

    // Tabs
    private JTabbedPane tabbedPane;
    private StudentPanel studentPanel;
    private AttendancePanel attendancePanel;
    private ReportPanel reportPanel;

    // Overview Components
    private JLabel lblTotalStudentsVal;
    private JLabel lblTodayPresentVal;
    private JLabel lblTodayAbsentVal;
    private JLabel lblAvgRateVal;
    private JProgressBar progressTodayRate;
    private JLabel lblProgressText;
    private DefaultTableModel defaultersTableModel;

    // UI Colors
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);         // Vibrant Royal Blue
    public static final Color COLOR_PRIMARY_DARK = new Color(15, 23, 42);     // Deep Slate Navy
    public static final Color COLOR_PRIMARY_BORDER = new Color(29, 78, 216);
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);        // Emerald Green
    public static final Color COLOR_SUCCESS_BORDER = new Color(5, 150, 105);
    public static final Color COLOR_DANGER = new Color(239, 68, 68);          // Rose Red
    public static final Color COLOR_DANGER_BORDER = new Color(220, 38, 38);
    public static final Color COLOR_WARNING = new Color(245, 158, 11);        // Amber
    public static final Color COLOR_SECONDARY_BG = new Color(226, 232, 240);  // High contrast slate
    public static final Color COLOR_SECONDARY_BORDER = new Color(148, 163, 184); // Clear border
    public static final Color COLOR_BG_LIGHT = new Color(241, 245, 249);     // App Background
    public static final Color COLOR_CARD_BG = Color.WHITE;                    // Card Background
    public static final Color COLOR_BORDER = new Color(203, 213, 225);        // Input borders
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);        // Primary text
    public static final Color COLOR_TEXT_MUTED = new Color(71, 85, 105);      // Muted text

    public Dashboard(AttendanceManager manager) {
        this.manager = manager;
        initFrame();
        notifyDataChanged();
    }

    private void initFrame() {
        setTitle("Attendance Management System");
        setSize(1140, 750);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(COLOR_BG_LIGHT);

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(COLOR_TEXT_MAIN);

        // Child Panels
        JPanel overviewPanel = createOverviewPanel();
        studentPanel = new StudentPanel(manager, this);
        attendancePanel = new AttendancePanel(manager, this);
        reportPanel = new ReportPanel(manager, this);

        tabbedPane.addTab("  Dashboard Overview  ", overviewPanel);
        tabbedPane.addTab("  Student Management  ", studentPanel);
        tabbedPane.addTab("  Mark Attendance  ", attendancePanel);
        tabbedPane.addTab("  Reports & Analytics  ", reportPanel);

        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            if (selected == 0) {
                refreshOverviewMetrics();
            } else if (selected == 1) {
                studentPanel.refreshStudentTable();
            } else if (selected == 2) {
                attendancePanel.refreshDepartmentFilter();
                attendancePanel.loadAttendanceForSelectedDate();
            } else if (selected == 3) {
                reportPanel.refreshDepartmentFilter();
                reportPanel.refreshReport();
            }
        });

        mainContainer.add(tabbedPane, BorderLayout.CENTER);

        // Status Bar Footer
        JPanel statusBar = createStatusBar();
        mainContainer.add(statusBar, BorderLayout.SOUTH);

        setContentPane(mainContainer);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_PRIMARY_DARK);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Left Branding
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        brandPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("ATTENDANCE MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Smart Academic Attendance Tracking & Performance Analytics");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(203, 213, 225));

        brandPanel.add(lblTitle);
        brandPanel.add(lblSubtitle);

        // Right Info (Date and Status)
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        infoPanel.setOpaque(false);

        LocalDate now = LocalDate.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        JLabel lblDate = new JLabel(dateStr, SwingConstants.RIGHT);
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDate.setForeground(Color.WHITE);

        JLabel lblStatus = new JLabel("System Status: Online  ●", SwingConstants.RIGHT);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(52, 211, 153));

        infoPanel.add(lblDate);
        infoPanel.add(lblStatus);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(infoPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(COLOR_BG_LIGHT);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        // 1. Top KPI Summary Cards (4 Cards)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiGrid.setBackground(COLOR_BG_LIGHT);

        lblTotalStudentsVal = new JLabel("0");
        lblTodayPresentVal = new JLabel("0");
        lblTodayAbsentVal = new JLabel("0");
        lblAvgRateVal = new JLabel("0.0%");

        kpiGrid.add(createCard("Total Enrolled Students", lblTotalStudentsVal, COLOR_PRIMARY, "Active student profiles"));
        kpiGrid.add(createCard("Today's Present", lblTodayPresentVal, COLOR_SUCCESS, "Marked present or late today"));
        kpiGrid.add(createCard("Today's Absent", lblTodayAbsentVal, COLOR_DANGER, "Absent in today's classes"));
        kpiGrid.add(createCard("Average Attendance Rate", lblAvgRateVal, new Color(13, 148, 136), "Historical overall average"));

        panel.add(kpiGrid, BorderLayout.NORTH);

        // 2. Center Two Cards: Left (Quick Actions & Today's Gauge), Right (Defaulters Alert List)
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setBackground(COLOR_BG_LIGHT);

        // Left Card: Daily Attendance Snapshot & Quick Actions
        JPanel leftCard = createTodaySnapshotCard();
        centerGrid.add(leftCard);

        // Right Card: Low Attendance / Defaulter Watchlist (< 75%)
        JPanel rightCard = createDefaulterWatchlistCard();
        centerGrid.add(rightCard);

        panel.add(centerGrid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCard(String title, JLabel valLabel, Color accent, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLabel.setForeground(accent);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(100, 116, 139));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createTodaySnapshotCard() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // Title
        JLabel title = new JLabel("Today's Attendance Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(COLOR_TEXT_MAIN);
        card.add(title, BorderLayout.NORTH);

        // Center Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_CARD_BG);

        lblProgressText = new JLabel("Daily Turnout Rate: 0.0%");
        lblProgressText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblProgressText.setForeground(COLOR_TEXT_MAIN);
        lblProgressText.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressTodayRate = new JProgressBar(0, 100);
        progressTodayRate.setValue(0);
        progressTodayRate.setStringPainted(true);
        progressTodayRate.setPreferredSize(new Dimension(300, 26));
        progressTodayRate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressTodayRate.setForeground(COLOR_SUCCESS);
        progressTodayRate.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoText = new JLabel("<html>Marking daily attendance ensures accurate eligibility records and triggers automatic warnings for students falling below the mandatory 75% attendance threshold.</html>");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoText.setForeground(COLOR_TEXT_MUTED);
        infoText.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(Box.createVerticalStrut(8));
        content.add(lblProgressText);
        content.add(Box.createVerticalStrut(8));
        content.add(progressTodayRate);
        content.add(Box.createVerticalStrut(16));
        content.add(infoText);
        content.add(Box.createVerticalGlue());

        card.add(content, BorderLayout.CENTER);

        // Quick Action Buttons - using high-visibility StyledButtons
        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        actionPanel.setBackground(COLOR_CARD_BG);

        StyledButton btnGoMark = createPrimaryBtn("Take Attendance");
        btnGoMark.addActionListener(e -> tabbedPane.setSelectedIndex(2));

        StyledButton btnGoStudents = new StyledButton("Add Student", new Color(15, 118, 110), Color.WHITE, new Color(17, 94, 89), 6);
        btnGoStudents.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        StyledButton btnGoReports = new StyledButton("View Reports", new Color(51, 65, 85), Color.WHITE, new Color(30, 41, 59), 6);
        btnGoReports.addActionListener(e -> tabbedPane.setSelectedIndex(3));

        actionPanel.add(btnGoMark);
        actionPanel.add(btnGoStudents);
        actionPanel.add(btnGoReports);

        card.add(actionPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createDefaulterWatchlistCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_CARD_BG);

        JLabel title = new JLabel("Attendance Defaulters Watchlist (< 75%)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(COLOR_DANGER);

        JLabel lblBadge = new JLabel("Action Required");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(COLOR_DANGER);

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(lblBadge, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Student Name", "Department", "Attendance %", "Status"};
        defaultersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable defaultersTable = new JTable(defaultersTableModel);
        defaultersTable.setRowHeight(30);
        defaultersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        defaultersTable.setGridColor(new Color(241, 245, 249));

        JTableHeader th = defaultersTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(254, 242, 242));
        th.setForeground(COLOR_DANGER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        defaultersTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        defaultersTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer();
        statusRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        statusRenderer.setForeground(COLOR_DANGER);
        defaultersTable.getColumnModel().getColumn(4).setCellRenderer(statusRenderer);

        JScrollPane scrollPane = new JScrollPane(defaultersTable);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(Color.WHITE);
        status.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, false),
                new EmptyBorder(6, 16, 6, 16)
        ));

        JLabel lblLeft = new JLabel("Attendance Management System v2.0  |  Java SE & Swing  |  Data Storage: data/students.csv & data/attendance.csv");
        lblLeft.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLeft.setForeground(COLOR_TEXT_MUTED);

        JLabel lblRight = new JLabel("Ready", SwingConstants.RIGHT);
        lblRight.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRight.setForeground(COLOR_TEXT_MUTED);

        status.add(lblLeft, BorderLayout.WEST);
        status.add(lblRight, BorderLayout.EAST);

        return status;
    }

    public void notifyDataChanged() {
        refreshOverviewMetrics();
    }

    private void refreshOverviewMetrics() {
        LocalDate today = LocalDate.now();
        int totalStudents = manager.getTotalStudentsCount();
        long todayPresent = manager.getTodayPresentCount(today);
        long todayAbsent = manager.getTodayAbsentCount(today);
        double overallRate = manager.getOverallAttendancePercentage();

        lblTotalStudentsVal.setText(String.valueOf(totalStudents));
        lblTodayPresentVal.setText(String.valueOf(todayPresent));
        lblTodayAbsentVal.setText(String.valueOf(todayAbsent));
        lblAvgRateVal.setText(String.format("%.1f%%", overallRate));

        long todayTotal = todayPresent + todayAbsent;
        double todayRate = todayTotal > 0 ? ((double) todayPresent / todayTotal * 100.0) : 0.0;
        progressTodayRate.setValue((int) Math.round(todayRate));
        progressTodayRate.setString(String.format("%.1f%%", todayRate));
        lblProgressText.setText(String.format("Today's Turnout Rate: %.1f%% (%d of %d active)", todayRate, todayPresent, totalStudents));

        // Refresh Defaulters Watchlist
        defaultersTableModel.setRowCount(0);
        List<StudentAttendanceSummary> defaulters = manager.getDefaulters(75.0, null, null);
        for (StudentAttendanceSummary def : defaulters) {
            defaultersTableModel.addRow(new Object[]{
                    def.getStudent().getStudentId(),
                    def.getStudent().getName(),
                    def.getStudent().getDepartment(),
                    String.format("%.1f%%", def.getPercentage()),
                    def.getEligibilityStatus()
            });
        }
    }

    // =========================================================================
    // Reusable Custom High-Visibility Button Helpers
    // =========================================================================

    public static StyledButton createPrimaryBtn(String text) {
        return new StyledButton(text, COLOR_PRIMARY, Color.WHITE, COLOR_PRIMARY_BORDER, 6);
    }

    public static StyledButton createSuccessBtn(String text) {
        return new StyledButton(text, COLOR_SUCCESS, Color.WHITE, COLOR_SUCCESS_BORDER, 6);
    }

    public static StyledButton createDangerBtn(String text) {
        return new StyledButton(text, COLOR_DANGER, Color.WHITE, COLOR_DANGER_BORDER, 6);
    }

    public static StyledButton createSecondaryBtn(String text) {
        return new StyledButton(text, COLOR_SECONDARY_BG, COLOR_TEXT_MAIN, COLOR_SECONDARY_BORDER, 6);
    }
}

/**
 * Custom Painted High-Visibility Button Component.
 * Bypasses native Look-and-Feel button overrides, guaranteeing rich colors,
 * high contrast, crisp borders, and smooth hover/press animations.
 */
class StyledButton extends JButton {
    private final Color normalBg;
    private final Color hoverBg;
    private final Color pressedBg;
    private final Color borderColor;
    private final int cornerRadius;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public StyledButton(String text, Color bg, Color fg, Color borderColor, int cornerRadius) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = computeHoverColor(bg);
        this.pressedBg = bg.darker();
        this.borderColor = borderColor;
        this.cornerRadius = cornerRadius;

        setForeground(fg);
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(8, 14, 8, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    private static Color computeHoverColor(Color c) {
        // Light secondary background check
        if (c.getRed() > 200 && c.getGreen() > 200 && c.getBlue() > 200) {
            return new Color(
                Math.max(0, c.getRed() - 22),
                Math.max(0, c.getGreen() - 22),
                Math.max(0, c.getBlue() - 22)
            );
        }
        // Colored buttons: slightly brighter/richer
        return new Color(
            Math.min(255, (int) (c.getRed() * 1.12)),
            Math.min(255, (int) (c.getGreen() * 1.12)),
            Math.min(255, (int) (c.getBlue() * 1.12))
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color currentBg = isPressed ? pressedBg : (isHovered ? hoverBg : normalBg);
        g2.setColor(currentBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
