import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RunSessionPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final String programName;
    private final int sessionId;
    private final InScheduleSessionsPage parentInSchedule;

    private JTextField dateField;
    private JTextField startField;
    private JTextField endField;
    private JTextField statusField;
    private JTextArea notesArea;

    private JTable table;
    private DefaultTableModel model;

    private JButton backBtn;
    private JButton saveBtn;
    private JButton finishBtn;

    // NEW: remember current status so we know if the session is still SCHEDULED
    private String sessionStatus;

    public RunSessionPage(user currentUser,
                          MySQLDatabaseConnector db,
                          int programId,
                          String programName,
                          int sessionId,
                          InScheduleSessionsPage parentInSchedule) {
        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.programName = programName;
        this.sessionId = sessionId;
        this.parentInSchedule = parentInSchedule;

        defaultSettings.setDefault(this);
        setTitle("Run Session — " + programName + " (Session " + sessionId + ")");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Run Session for Class: " + programName, SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER =====
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 12, 12, 12));
        add(center, BorderLayout.CENTER);

        // ---- top: basic info ----
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(4, 0, 8, 0));

        dateField = makeReadOnlyField();
        startField = makeReadOnlyField();
        endField = makeReadOnlyField();
        statusField = makeReadOnlyField();
        notesArea = new JTextArea(3, 40);
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(defaultSettings.BACKGROUND_COLOR);
        notesArea.setForeground(defaultSettings.TEXT_COLOR);
        notesArea.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 1, true));

        infoPanel.add(rowPanel("Session Date:", dateField));
        infoPanel.add(rowPanel("Start Time:", startField));
        infoPanel.add(rowPanel("End Time:", endField));
        infoPanel.add(rowPanel("Status:", statusField));

        JPanel notesRow = new JPanel(new BorderLayout());
        notesRow.setOpaque(false);
        JLabel notesLabel = new JLabel("Notes:");
        notesLabel.setForeground(defaultSettings.TEXT_COLOR);
        notesRow.add(notesLabel, BorderLayout.NORTH);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        notesScroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        notesRow.add(notesScroll, BorderLayout.CENTER);

        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(notesRow);

        center.add(infoPanel, BorderLayout.NORTH);

        // ---- middle: attendance table ----
        model = new DefaultTableModel(
                new Object[]{"User Email", "Attended?", "No-show?"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2; // only checkboxes
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1 || columnIndex == 2) return Boolean.class;
                return String.class;
            }
        };

        table = new JTable(model);
        table.setBackground(defaultSettings.BACKGROUND_COLOR);
        table.setForeground(defaultSettings.TEXT_COLOR);
        table.setFillsViewportHeight(true);
        table.setGridColor(defaultSettings.BORDER_COLOR);
        table.setSelectionBackground(Color.DARK_GRAY);
        table.setSelectionForeground(defaultSettings.TEXT_COLOR);
        table.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        table.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        tableScroll.setBackground(defaultSettings.BACKGROUND_COLOR);

        center.add(tableScroll, BorderLayout.CENTER);

        // ===== BOTTOM: buttons =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);
        backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        saveBtn = stdOutlinedButton("Save Attendance", defaultSettings.BORDER_COLOR);
        finishBtn = stdGreenButton("Finish Session");

        bottom.add(backBtn);
        bottom.add(saveBtn);
        bottom.add(finishBtn);

        center.add(bottom, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> saveAttendanceChanges());
        finishBtn.addActionListener(e -> {
            if (!saveAttendanceChanges()) return;
            markSessionCompleted();
        });

        // === IMPORTANT ORDER ===
        // 1. Load current status/info
        loadSessionInfo();
        // 2. If session still SCHEDULED, add new attendees from user_program
        syncAttendanceWithNewEnrollmentsIfNeeded();
        // 3. Possibly move session to IN_PROGRESS
        ensureInProgress();
        // 4. Now show everyone from class_attendance
        loadParticipants();
    }

    private JTextField makeReadOnlyField() {
        JTextField f = new JTextField(20);
        f.setEditable(false);
        f.setBackground(defaultSettings.BACKGROUND_COLOR);
        f.setForeground(defaultSettings.TEXT_COLOR);
        f.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 1, true));
        return f;
    }

    private JPanel rowPanel(String labelText, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(defaultSettings.TEXT_COLOR);
        row.add(lbl);
        row.add(field);
        return row;
    }

    private void loadSessionInfo() {
        String sql = "SELECT session_date, start_time, end_time, status, notes " +
                "FROM class_session WHERE session_id=? AND program_id=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setInt(2, programId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d       = rs.getDate("session_date");
                    Time st      = rs.getTime("start_time");
                    Time et      = rs.getTime("end_time");
                    String status = rs.getString("status");
                    String notes  = rs.getString("notes");

                    sessionStatus = status; // NEW: remember
                    dateField.setText(d != null ? d.toString() : "");
                    startField.setText(st != null ? st.toString() : "");
                    endField.setText(et != null ? et.toString() : "");
                    statusField.setText(status != null ? status : "");
                    notesArea.setText(notes != null ? notes : "");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading session info:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Before starting the session, make sure every enrolled user
     * has a row in class_attendance (status REGISTERED).
     * This catches students who registered after the session was created
     * but before the trainer clicked "Run Session".
     */
    private void syncAttendanceWithNewEnrollmentsIfNeeded() {
        if (sessionStatus == null ||
                !sessionStatus.equalsIgnoreCase("SCHEDULED")) {
            // only sync when still scheduled
            return;
        }

        String sql =
                "INSERT INTO class_attendance (session_id, user_email, status) " +
                        "SELECT ?, up.user_email, 'REGISTERED' " +
                        "FROM user_program up " +
                        "WHERE up.program_id = ? " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 FROM class_attendance ca " +
                        "    WHERE ca.session_id = ? AND ca.user_email = up.user_email " +
                        ")";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setInt(2, programId);
            ps.setInt(3, sessionId);
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error syncing new registrations for this session:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** If session is still SCHEDULED, set it to IN_PROGRESS once we open this page. */
    private void ensureInProgress() {
        String sql =
                "UPDATE class_session " +
                        "SET status='IN_PROGRESS' " +
                        "WHERE session_id=? AND program_id=? AND status='SCHEDULED'";
        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setInt(2, programId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        // refresh label/status from DB
        loadSessionInfo();
        if (parentInSchedule != null) parentInSchedule.reloadSessions();
        ManageClassSessionsPage parentManage = parentInSchedule != null
                ? parentInSchedule.getParentManagePage()
                : null;
        if (parentManage != null) parentManage.reloadSessions();
    }

    private void loadParticipants() {
        model.setRowCount(0);

        String sql =
                "SELECT user_email, status " +
                        "FROM class_attendance " +
                        "WHERE session_id=? " +
                        "ORDER BY user_email";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email  = rs.getString("user_email");
                    String status = rs.getString("status");

                    boolean attended = "ATTENDED".equalsIgnoreCase(status);
                    boolean noShow   = "NO_SHOW".equalsIgnoreCase(status);

                    model.addRow(new Object[]{
                            email,
                            attended,
                            noShow
                    });
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading participants:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Save checkbox changes back into class_attendance.
     * Returns true if everything was OK, false if validation failed.
     */
    private boolean saveAttendanceChanges() {
        int rowCount = model.getRowCount();
        // UPDATED: no check_in_time column in your table definition, so just update status
        String sql =
                "UPDATE class_attendance " +
                        "SET status=? " +
                        "WHERE session_id=? AND user_email=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < rowCount; i++) {
                String email   = model.getValueAt(i, 0).toString();
                Boolean attended = (Boolean) model.getValueAt(i, 1);
                Boolean noShow   = (Boolean) model.getValueAt(i, 2);

                if (attended == null) attended = false;
                if (noShow == null) noShow = false;

                if (attended && noShow) {
                    JOptionPane.showMessageDialog(this,
                            "For " + email + ", please choose either Attended or No-show, not both.",
                            "Validation error",
                            JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                String newStatus;
                if (attended) newStatus = "ATTENDED";
                else if (noShow) newStatus = "NO_SHOW";
                else newStatus = "REGISTERED";

                ps.setString(1, newStatus);
                ps.setInt(2, sessionId);
                ps.setString(3, email);
                ps.addBatch();
            }

            ps.executeBatch();
            JOptionPane.showMessageDialog(this, "Attendance saved.");
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving attendance:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void markSessionCompleted() {
        String sql = "UPDATE class_session SET status='COMPLETED' " +
                "WHERE session_id=? AND program_id=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setInt(2, programId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Session marked as COMPLETED.");

            if (parentInSchedule != null) {
                parentInSchedule.reloadSessions();
                ManageClassSessionsPage parentManage = parentInSchedule.getParentManagePage();
                if (parentManage != null) {
                    parentManage.reloadSessions();
                }
            }

            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error marking session completed:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // style helpers
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
