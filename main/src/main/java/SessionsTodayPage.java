import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.time.LocalDate;

public class SessionsTodayPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;

    private JTable table;
    private DefaultTableModel model;

    private JButton backBtn;
    private JButton refreshBtn;
    private JButton startBtn;

    public SessionsTodayPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — Sessions Today");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Sessions Today", SwingConstants.CENTER);
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

        // table model:
        // all sessions today for all classes owned by this trainer
        model = new DefaultTableModel(
                new Object[]{
                        "Session ID",
                        "Class ID",
                        "Class Name",
                        "Date",
                        "Start",
                        "End",
                        "Status"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.add(scroll, BorderLayout.CENTER);

        // double-click → start session
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    startSelected();
                }
            }
        });

        // ===== BOTTOM buttons =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        refreshBtn = stdOutlinedButton("Refresh", defaultSettings.BORDER_COLOR);
        startBtn = stdGreenButton("Start Session");

        bottom.add(backBtn);
        bottom.add(refreshBtn);
        bottom.add(startBtn);

        center.add(bottom, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> dispose());
        refreshBtn.addActionListener(e -> reloadSessions());
        startBtn.addActionListener(e -> startSelected());

        reloadSessions();
    }

    private Integer getSelectedSessionId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a session.");
            return null;
        }
        Object idObj = model.getValueAt(row, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "Invalid session ID.");
            return null;
        }
        return Integer.parseInt(idObj.toString());
    }

    private Integer getSelectedProgramId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object idObj = model.getValueAt(row, 1);
        if (idObj == null) return null;
        return Integer.parseInt(idObj.toString());
    }

    private String getSelectedProgramName() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object nameObj = model.getValueAt(row, 2);
        return (nameObj == null) ? "" : nameObj.toString();
    }

    private String getSelectedStatus() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object statusObj = model.getValueAt(row, 6);
        return (statusObj == null) ? "" : statusObj.toString();
    }

    private void startSelected() {
        Integer sessionId = getSelectedSessionId();
        if (sessionId == null) return;

        String status = getSelectedStatus();
        if ("COMPLETED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(
                    this,
                    "This session is already COMPLETED.\nYou cannot start it again.",
                    "Session finished",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        Integer programId = getSelectedProgramId();
        String programName = getSelectedProgramName();
        if (programId == null || programName == null) {
            JOptionPane.showMessageDialog(this,
                    "Missing class information for this session.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Open RunSessionPage. We pass parentInSchedule = null
        // and add a WindowListener to auto-refresh this page
        RunSessionPage runPage = new RunSessionPage(
                currentUser,
                db,
                programId,
                programName,
                sessionId,
                null      // no InScheduleSessionsPage parent
        );
        runPage.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                reloadSessions();
            }
        });
        runPage.setVisible(true);
    }

    /** Load all of today's sessions for this trainer. */
    private void reloadSessions() {
        model.setRowCount(0);

        String sql =
                "SELECT s.session_id, s.program_id, p.name AS class_name, " +
                        "       s.session_date, s.start_time, s.end_time, s.status " +
                        "FROM class_session s " +
                        "JOIN exercise_program p ON s.program_id = p.program_id " +
                        "WHERE p.trainer_email = ? " +
                        "  AND s.session_date = ? " +
                        "ORDER BY s.session_date, s.start_time";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            // Use local “today” from Java instead of CURDATE()
            java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
            ps.setDate(2, today);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sessionId = rs.getInt("session_id");
                    int programId = rs.getInt("program_id");
                    String className = rs.getString("class_name");
                    Date d = rs.getDate("session_date");
                    Time st = rs.getTime("start_time");
                    Time et = rs.getTime("end_time");
                    String status = rs.getString("status");

                    model.addRow(new Object[]{
                            sessionId,
                            programId,
                            (className != null ? className : ""),
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
                    "Error loading today's sessions:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- style helpers ----
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
