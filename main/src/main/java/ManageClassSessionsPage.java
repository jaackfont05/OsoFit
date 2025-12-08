import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class ManageClassSessionsPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final String programName;

    private JTable sessionsTable;
    private DefaultTableModel sessionsModel;

    private JButton addSessionBtn;
    private JButton editSessionBtn;
    private JButton deleteSessionBtn;
    private JButton backBtn;
    private JButton refreshBtn;
    private JButton inScheduleBtn;
    private JButton rankingBtn;

    public ManageClassSessionsPage(user currentUser,
                                   MySQLDatabaseConnector db,
                                   int programId,
                                   String programName) {
        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.programName = programName;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — Manage Class Sessions");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel(
                "Manage Sessions for Class: " + programName + " (ID " + programId + ")",
                SwingConstants.CENTER
        );
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: table + bottom buttons =====
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 12, 12, 12));
        add(center, BorderLayout.CENTER);

        // Table: one row per session in this class
        sessionsModel = new DefaultTableModel(
                new Object[]{
                        "Session ID",
                        "Date",
                        "Start Time",
                        "End Time",
                        "Status"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        sessionsTable = new JTable(sessionsModel);
        sessionsTable.setBackground(defaultSettings.BACKGROUND_COLOR);
        sessionsTable.setForeground(defaultSettings.TEXT_COLOR);
        sessionsTable.setFillsViewportHeight(true);
        sessionsTable.setGridColor(defaultSettings.BORDER_COLOR);
        sessionsTable.setSelectionBackground(Color.DARK_GRAY);
        sessionsTable.setSelectionForeground(defaultSettings.TEXT_COLOR);
        sessionsTable.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        sessionsTable.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        JScrollPane scroll = new JScrollPane(sessionsTable);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);

        center.add(scroll, BorderLayout.CENTER);

        // Bottom buttons (two rows: navigation + CRUD)
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        // row 1: navigation / stats
        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        navRow.setOpaque(false);
        inScheduleBtn = stdOutlinedButton("View In-Schedule / Start", defaultSettings.BORDER_COLOR);
        rankingBtn = stdOutlinedButton("View Attendance Ranking", defaultSettings.BORDER_COLOR);
        navRow.add(inScheduleBtn);
        navRow.add(rankingBtn);
        bottom.add(navRow, BorderLayout.WEST);

        // row 2: CRUD + refresh
        JPanel crudRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        crudRow.setOpaque(false);

        backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        refreshBtn = stdOutlinedButton("Refresh", defaultSettings.BORDER_COLOR);
        addSessionBtn = stdGreenButton("Add Session");
        editSessionBtn = stdOutlinedButton("Edit Selected", defaultSettings.BORDER_COLOR);
        deleteSessionBtn = stdOutlinedButton("Delete Selected", new Color(200, 40, 40));

        crudRow.add(backBtn);
        crudRow.add(refreshBtn);
        crudRow.add(addSessionBtn);
        crudRow.add(editSessionBtn);
        crudRow.add(deleteSessionBtn);

        bottom.add(crudRow, BorderLayout.EAST);

        center.add(bottom, BorderLayout.SOUTH);

        wireActions();
        reloadSessions();   // initial load
    }

    private void wireActions() {
        backBtn.addActionListener(e -> {
            dispose();
            new TrainerClassesPage(currentUser, db).setVisible(true);
        });

        refreshBtn.addActionListener(e -> reloadSessions());

        addSessionBtn.addActionListener(e -> {
            new ClassSessionFormPage(
                    currentUser,
                    db,
                    programId,
                    programName,
                    null,
                    this
            ).setVisible(true);
        });

        editSessionBtn.addActionListener(e -> editSelectedSession());
        deleteSessionBtn.addActionListener(e -> deleteSelectedSession());

        inScheduleBtn.addActionListener((ActionEvent e) -> {
            new InScheduleSessionsPage(
                    currentUser,
                    db,
                    programId,
                    programName,
                    this     // pass parent for auto refresh
            ).setVisible(true);
        });

        rankingBtn.addActionListener((ActionEvent e) -> {
            new ClassAttendanceRankingPage(
                    currentUser,
                    db,
                    programId,
                    programName
            ).setVisible(true);
        });
    }

    private void editSelectedSession() {
        int row = sessionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a session first.");
            return;
        }
        Object idObj = sessionsModel.getValueAt(row, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "Invalid session ID.");
            return;
        }
        int sessionId = Integer.parseInt(idObj.toString());

        new ClassSessionFormPage(
                currentUser,
                db,
                programId,
                programName,
                sessionId,
                this
        ).setVisible(true);
    }

    private void deleteSelectedSession() {
        int row = sessionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a session first.");
            return;
        }
        Object idObj = sessionsModel.getValueAt(row, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "Invalid session ID.");
            return;
        }
        int sessionId = Integer.parseInt(idObj.toString());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete session ID " + sessionId + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM class_session WHERE session_id=? AND program_id=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setInt(2, programId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Session deleted.");
            reloadSessions();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting session:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Public so child pages can ask the table to refresh. */
    public void reloadSessions() {
        sessionsModel.setRowCount(0);

        String sql = "SELECT session_id, session_date, start_time, end_time, status " +
                "FROM class_session " +
                "WHERE program_id=? " +
                "ORDER BY session_date, start_time";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("session_id");
                    Date d = rs.getDate("session_date");
                    Time st = rs.getTime("start_time");
                    Time et = rs.getTime("end_time");
                    String status = rs.getString("status");

                    sessionsModel.addRow(new Object[]{
                            id,
                            (d != null ? d.toString() : ""),
                            (st != null ? st.toString() : ""),
                            (et != null ? et.toString() : ""),
                            (status != null ? status : "")
                    });
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading sessions:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- small style helpers ----
    private JButton stdGreenButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        return b;
    }

    private JButton stdOutlinedButton(String text, Color borderColor) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(borderColor, 2, true));
        return b;
    }
}
