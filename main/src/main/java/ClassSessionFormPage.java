import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ClassSessionFormPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final String programName;
    private final Integer sessionId; // null = add, non-null = edit
    private final ManageClassSessionsPage parent;

    private JTextField dateField;
    private JTextField startField;
    private JTextField endField;
    private JComboBox<String> statusCombo;
    private JTextArea notesArea;

    private JButton backBtn;
    private JButton clearBtn;
    private JButton saveBtn;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public ClassSessionFormPage(user currentUser,
                                MySQLDatabaseConnector db,
                                int programId,
                                String programName,
                                Integer sessionId,
                                ManageClassSessionsPage parent) {

        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.programName = programName;
        this.sessionId = sessionId;
        this.parent = parent;

        defaultSettings.setDefault(this);
        String modeTitle = (sessionId == null) ? "Add New Session for Class: " : "Edit Session for Class: ";
        setTitle(modeTitle + programName);
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu + title =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel(
                (sessionId == null ? "Add New Session for Class: " : "Edit Session for Class: ")
                        + programName,
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

        // ===== CENTER: LEFT-ALIGNED FORM =====
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 40, 12, 40));
        add(center, BorderLayout.CENTER);

        JPanel form = new JPanel();
        form.setBackground(defaultSettings.BACKGROUND_COLOR);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        center.add(form, BorderLayout.CENTER);

        // Helper: add label + component, left aligned
        form.add(makeLabel("Session Date (YYYY-MM-DD):"));
        dateField = stdTextField();
        form.add(dateField);
        form.add(Box.createVerticalStrut(8));

        form.add(makeLabel("Start Time (HH:MM):"));
        startField = stdTextField();
        form.add(startField);
        form.add(Box.createVerticalStrut(8));

        form.add(makeLabel("End Time (HH:MM):"));
        endField = stdTextField();
        form.add(endField);
        form.add(Box.createVerticalStrut(8));

        form.add(makeLabel("Status:"));
        statusCombo = new JComboBox<>(new String[]{"SCHEDULED", "IN_PROGRESS", "COMPLETED"});
        styleCombo(statusCombo);
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusCombo);
        form.add(Box.createVerticalStrut(8));

        form.add(makeLabel("Notes:"));
        notesArea = new JTextArea(6, 40);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setForeground(defaultSettings.TEXT_COLOR);
        notesArea.setBackground(defaultSettings.BACKGROUND_COLOR);
        notesArea.setCaretColor(defaultSettings.TEXT_COLOR);
        notesArea.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesScroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        notesScroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        form.add(notesScroll);

        // ===== BOTTOM BUTTONS =====
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomRow.setOpaque(false);

        // Back to Sessions (explicit navigation)
        backBtn = stdOutlinedButton("Back to Sessions", defaultSettings.BORDER_COLOR);
        clearBtn = stdOutlinedButton("Clear", defaultSettings.BORDER_COLOR);
        saveBtn = stdGreenButton("Save");

        bottomRow.add(backBtn);
        bottomRow.add(clearBtn);
        bottomRow.add(saveBtn);

        center.add(bottomRow, BorderLayout.SOUTH);

        wireActions();

        if (sessionId != null) {
            loadExistingSession();
        } else {
            statusCombo.setSelectedItem("SCHEDULED");
        }
    }

    private void wireActions() {
        backBtn.addActionListener(e -> {
            // Just close this window; ManageClassSessionsPage is still there
            dispose();
            if (parent != null) {
                parent.setVisible(true);
                parent.reloadSessions();
            }
        });

        clearBtn.addActionListener(e -> {
            dateField.setText("");
            startField.setText("");
            endField.setText("");
            statusCombo.setSelectedItem("SCHEDULED");
            notesArea.setText("");
        });

        saveBtn.addActionListener(e -> onSave());
    }

    private void loadExistingSession() {
        String sql = "SELECT session_date, start_time, end_time, status, notes " +
                "FROM class_session " +
                "WHERE session_id=? AND program_id=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setInt(2, programId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate("session_date");
                    Time st = rs.getTime("start_time");
                    Time et = rs.getTime("end_time");
                    String status = rs.getString("status");
                    String notes = rs.getString("notes");

                    dateField.setText(d != null ? d.toString() : "");
                    startField.setText(st != null ? st.toString().substring(0, 5) : ""); // HH:MM
                    endField.setText(et != null ? et.toString().substring(0, 5) : "");
                    if (status != null) {
                        statusCombo.setSelectedItem(status);
                    }
                    notesArea.setText(notes != null ? notes : "");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading session:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        String dateTxt = dateField.getText().trim();
        String startTxt = startField.getText().trim();
        String endTxt = endField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();
        String notes = notesArea.getText().trim();

        if (dateTxt.isEmpty() || startTxt.isEmpty() || endTxt.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Session date, start time, and end time are required.");
            return;
        }

        java.sql.Date sqlDate;
        java.sql.Time sqlStart;
        java.sql.Time sqlEnd;

        try {
            LocalDate d = LocalDate.parse(dateTxt); // expects YYYY-MM-DD
            sqlDate = java.sql.Date.valueOf(d);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Date must be in format YYYY-MM-DD.");
            return;
        }

        try {
            LocalTime st = LocalTime.parse(startTxt, TIME_FMT);
            sqlStart = java.sql.Time.valueOf(st);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Start time must be in format HH:MM (24-hour).");
            return;
        }

        try {
            LocalTime et = LocalTime.parse(endTxt, TIME_FMT);
            sqlEnd = java.sql.Time.valueOf(et);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "End time must be in format HH:MM (24-hour).");
            return;
        }

        if (!sqlEnd.after(sqlStart)) {
            JOptionPane.showMessageDialog(this,
                    "End time must be after start time.");
            return;
        }

        try (Connection conn = MySQLDatabaseConnector.getConnection()) {
            if (sessionId == null) {
                // INSERT
                String sql = "INSERT INTO class_session " +
                        "(program_id, session_date, start_time, end_time, status, notes) " +
                        "VALUES (?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, programId);
                    ps.setDate(2, sqlDate);
                    ps.setTime(3, sqlStart);
                    ps.setTime(4, sqlEnd);
                    ps.setString(5, status);
                    ps.setString(6, notes);
                    ps.executeUpdate();
                }
            } else {
                // UPDATE
                String sql = "UPDATE class_session SET " +
                        "session_date=?, start_time=?, end_time=?, status=?, notes=? " +
                        "WHERE session_id=? AND program_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDate(1, sqlDate);
                    ps.setTime(2, sqlStart);
                    ps.setTime(3, sqlEnd);
                    ps.setString(4, status);
                    ps.setString(5, notes);
                    ps.setInt(6, sessionId);
                    ps.setInt(7, programId);
                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Session saved.");
            if (parent != null) {
                parent.reloadSessions();
            }
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving session:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- style helpers ----
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(defaultSettings.TEXT_COLOR);
        l.setFont(defaultSettings.LABEL_FONT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField stdTextField() {
        JTextField f = new JTextField();
        Dimension d = new Dimension(250, 26);
        f.setPreferredSize(d);
        f.setMaximumSize(d);
        f.setMinimumSize(d);
        f.setForeground(defaultSettings.TEXT_COLOR);
        f.setBackground(defaultSettings.BACKGROUND_COLOR);
        f.setCaretColor(defaultSettings.TEXT_COLOR);
        f.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setForeground(defaultSettings.TEXT_COLOR);
        combo.setBackground(defaultSettings.BACKGROUND_COLOR);
        combo.setFont(defaultSettings.LABEL_FONT);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel l = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                l.setForeground(defaultSettings.TEXT_COLOR);
                l.setBackground(isSelected ? Color.DARK_GRAY : defaultSettings.BACKGROUND_COLOR);
                return l;
            }
        });
    }

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
