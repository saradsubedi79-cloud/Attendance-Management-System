import gui.Dashboard;
import manager.AttendanceManager;

import javax.swing.*;

/**
 * Application Entry Point for the Attendance Management System.
 */
public class Main {
    public static void main(String[] args) {
        // Enable high-DPI scaling and subpixel font antialiasing on modern displays
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Set modern System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Initialize core business logic manager
        AttendanceManager manager = new AttendanceManager();

        // Launch Dashboard on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                Dashboard dashboard = new Dashboard(manager);
                dashboard.setVisible(true);
                System.out.println("=================================================");
                System.out.println("Attendance Management System launched successfully!");
                System.out.println("Enrolled Students: " + manager.getTotalStudentsCount());
                System.out.println("Overall Attendance: " + String.format("%.1f%%", manager.getOverallAttendancePercentage()));
                System.out.println("Data persisted in: ./data/");
                System.out.println("=================================================");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch Attendance Management System:\n" + e.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
//  Compile
//     javac -encoding UTF-8 -d bin src/model/*.java src/exception/*.java src/manager/*.java src/gui/*.java src/Main.java

//  Run
//     java -cp bin Main