package gui;

import exception.DuplicateStudentException;
import manager.AttendanceManager;
import model.Student;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Panel for managing student records: Add, Update, Delete, Search, and View.
 */
public class StudentPanel extends JPanel {
    private final AttendanceManager manager;
    private final Dashboard parentDashboard;

    // Form inputs
    private JTextField txtStudentId;
    private JTextField txtName;
    private JTextField txtEmail;
    private JComboBox<String> cmbDepartment;
    private JComboBox<String> cmbSemester;

    // Buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // Table & Search
    private JTextField txtSearch;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalStudents;

    // UI Theme Constants
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);
    private static final Color COLOR_BG_LIGHT = new Color(241, 245, 249);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(203, 213, 225);
    private static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_MUTED = new Color(71, 85, 105);

    public StudentPanel(AttendanceManager manager, Dashboard parentDashboard) {
        this.manager = manager;
        this.parentDashboard = parentDashboard;
        initUI();
        refreshStudentTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setBackground(COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Left Form Panel
        JPanel leftPanel = createFormPanel();

        // Right Table Panel
        JPanel rightPanel = createTablePanel();

        // Split arrangement
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(360);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setBackground(COLOR_BG_LIGHT);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // Header
        JLabel lblHeading = new JLabel("Student Information");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeading.setForeground(COLOR_TEXT_MAIN);

        JLabel lblSubheading = new JLabel("Enroll or edit student profile details");
        lblSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubheading.setForeground(COLOR_TEXT_MUTED);

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        headerPanel.setBackground(COLOR_CARD_BG);
        headerPanel.add(lblHeading);
        headerPanel.add(lblSubheading);
        card.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(COLOR_CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        int row = 0;

        // Student ID
        gbc.gridy = row++;
        formGrid.add(createFieldLabel("Student ID *"), gbc);
        txtStudentId = createStyledTextField("e.g. STU-1025");
        gbc.gridy = row++;
        formGrid.add(txtStudentId, gbc);

        // Name
        gbc.gridy = row++;
        formGrid.add(createFieldLabel("Full Name *"), gbc);
        txtName = createStyledTextField("e.g. John Doe");
        gbc.gridy = row++;
        formGrid.add(txtName, gbc);

        // Department
        gbc.gridy = row++;
        formGrid.add(createFieldLabel("Department *"), gbc);
        String[] depts = {
            "Computer Science",
            "Information Technology",
            "Software Engineering",
            "AI & Data Science",
            "Electrical Engineering",
            "Mechanical Engineering",
            "Civil Engineering"
        };
        cmbDepartment = new JComboBox<>(depts);
        cmbDepartment.setEditable(true);
        styleComboBox(cmbDepartment);
        gbc.gridy = row++;
        formGrid.add(cmbDepartment, gbc);

        // Semester
        gbc.gridy = row++;
        formGrid.add(createFieldLabel("Semester *"), gbc);
        String[] semesters = {"Sem 1", "Sem 2", "Sem 3", "Sem 4", "Sem 5", "Sem 6", "Sem 7", "Sem 8"};
        cmbSemester = new JComboBox<>(semesters);
        styleComboBox(cmbSemester);
        gbc.gridy = row++;
        formGrid.add(cmbSemester, gbc);

        // Email
        gbc.gridy = row++;
        formGrid.add(createFieldLabel("Email Address *"), gbc);
        txtEmail = createStyledTextField("e.g. john.doe@college.edu");
        gbc.gridy = row++;
        formGrid.add(txtEmail, gbc);

        // Spacer to push buttons nicely
        gbc.gridy = row++;
        gbc.weighty = 1.0;
        formGrid.add(Box.createVerticalGlue(), gbc);

        card.add(formGrid, BorderLayout.CENTER);

        // Action Buttons with High Visibility & Contrast
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBackground(COLOR_CARD_BG);

        btnAdd = Dashboard.createSuccessBtn("Add Student");
        btnUpdate = Dashboard.createPrimaryBtn("Update");
        btnDelete = Dashboard.createDangerBtn("Delete");
        btnClear = Dashboard.createSecondaryBtn("Clear Form");

        btnAdd.addActionListener(e -> handleAddStudent());
        btnUpdate.addActionListener(e -> handleUpdateStudent());
        btnDelete.addActionListener(e -> handleDeleteStudent());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Top Toolbar (Search & Stats)
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(COLOR_CARD_BG);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setBackground(COLOR_CARD_BG);

        JLabel lblSearch = new JLabel("Filter:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(COLOR_TEXT_MAIN);

        txtSearch = new JTextField(18);
        txtSearch.setPreferredSize(new Dimension(200, 34));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(4, 8, 4, 8)));

        JButton btnSearch = Dashboard.createPrimaryBtn("Search");
        btnSearch.setPreferredSize(new Dimension(85, 34));
        btnSearch.addActionListener(e -> handleSearch());

        JButton btnResetSearch = Dashboard.createSecondaryBtn("Reset");
        btnResetSearch.setPreferredSize(new Dimension(85, 34));
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            refreshStudentTable();
        });

        // Instant search on enter
        txtSearch.addActionListener(e -> handleSearch());

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnResetSearch);

        lblTotalStudents = new JLabel("Students: 0");
        lblTotalStudents.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalStudents.setForeground(COLOR_PRIMARY);

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(lblTotalStudents, BorderLayout.EAST);

        card.add(topBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"Student ID", "Full Name", "Department", "Semester", "Email", "Attendance %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(32);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setGridColor(new Color(241, 245, 249));
        studentTable.setShowVerticalLines(false);

        // Header Styling
        JTableHeader header = studentTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_MAIN);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Center alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        studentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        studentTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        studentTable.getColumnModel().getColumn(5).setCellRenderer(new AttendancePercentageRenderer());

        // Row Selection listener
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromTable(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void handleAddStudent() {
        String id = txtStudentId.getText().trim();
        String name = txtName.getText().trim();
        String dept = cmbDepartment.getSelectedItem() != null ? cmbDepartment.getSelectedItem().toString().trim() : "";
        String sem = cmbSemester.getSelectedItem() != null ? cmbSemester.getSelectedItem().toString().trim() : "";
        String email = txtEmail.getText().trim();

        if (id.isEmpty() || name.isEmpty() || dept.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (ID, Name, Department, Email).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Student student = new Student(id, name, email, dept, sem);
            manager.addStudent(student);
            JOptionPane.showMessageDialog(this,
                    "Student '" + name + "' (" + id + ") added successfully!",
                    "Student Registered", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshStudentTable();
            if (parentDashboard != null) {
                parentDashboard.notifyDataChanged();
            }
        } catch (DuplicateStudentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage() + "\nPlease provide a unique Student ID.",
                    "Duplicate Student Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding student: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateStudent() {
        String id = txtStudentId.getText().trim();
        String name = txtName.getText().trim();
        String dept = cmbDepartment.getSelectedItem() != null ? cmbDepartment.getSelectedItem().toString().trim() : "";
        String sem = cmbSemester.getSelectedItem() != null ? cmbSemester.getSelectedItem().toString().trim() : "";
        String email = txtEmail.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select or enter the Student ID to update.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student student = new Student(id, name, email, dept, sem);
        boolean success = manager.updateStudent(student);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Student record for ID '" + id + "' updated successfully!",
                    "Record Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshStudentTable();
            if (parentDashboard != null) {
                parentDashboard.notifyDataChanged();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Student with ID '" + id + "' was not found.",
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteStudent() {
        String id = txtStudentId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student from the table to delete.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student '" + id + "'?\nThis will also remove their attendance records.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = manager.deleteStudent(id);
            if (deleted) {
                JOptionPane.showMessageDialog(this,
                        "Student '" + id + "' was deleted successfully.",
                        "Deleted", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshStudentTable();
                if (parentDashboard != null) {
                    parentDashboard.notifyDataChanged();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete student: record not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        List<Student> results = manager.searchStudents(query);
        populateTable(results);
    }

    public void refreshStudentTable() {
        populateTable(manager.getAllStudents());
    }

    private void populateTable(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student s : students) {
            double rate = manager.getStudentAttendancePercentage(s.getStudentId());
            tableModel.addRow(new Object[]{
                    s.getStudentId(),
                    s.getName(),
                    s.getDepartment(),
                    s.getSemester(),
                    s.getEmail(),
                    String.format("%.1f%%", rate)
            });
        }
        lblTotalStudents.setText("Students: " + students.size());
    }

    private void populateFormFromTable(int row) {
        txtStudentId.setText(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        cmbDepartment.setSelectedItem(tableModel.getValueAt(row, 2).toString());
        cmbSemester.setSelectedItem(tableModel.getValueAt(row, 3).toString());
        txtEmail.setText(tableModel.getValueAt(row, 4).toString());
        txtStudentId.setEditable(false);
    }

    private void clearForm() {
        txtStudentId.setText("");
        txtName.setText("");
        txtEmail.setText("");
        if (cmbDepartment.getItemCount() > 0) cmbDepartment.setSelectedIndex(0);
        if (cmbSemester.getItemCount() > 0) cmbSemester.setSelectedIndex(0);
        txtStudentId.setEditable(true);
        studentTable.clearSelection();
    }

    // Helper UI Builders
    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_MAIN);
        return lbl;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 34));
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
    }

    // Custom cell renderer for percentage column
    private static class AttendancePercentageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected && value != null) {
                try {
                    String str = value.toString().replace("%", "").trim();
                    double pct = Double.parseDouble(str);
                    if (pct < 75.0) {
                        c.setForeground(COLOR_DANGER);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (pct >= 85.0) {
                        c.setForeground(COLOR_SUCCESS);
                    } else {
                        c.setForeground(COLOR_TEXT_MAIN);
                    }
                } catch (Exception ignored) {
                }
            }
            return c;
        }
    }
}
