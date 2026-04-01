import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class InScheduleSessionsPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final String programName;
    private final ManageClassSessionsPage parentManagePage;

    private JTable table;
    private DefaultTableModel model;

    private JButton backBtn;
    private JButton startBtn;
    private JButton refreshBtn;

    // convenience ctor if opened from somewhere else (no parent manage page)
    public InScheduleSessionsPage(user currentUser,
                                  MySQLDatabaseConnector db,
                                  int programId,
                                  String programName) {
        this(currentUser, db, programId, programName, null);
    }

    public InScheduleSessionsPage(user currentUser,
                                  MySQLDatabaseConnector db,
                                  int programId,
                                  String programName,
                                  ManageClassSessionsPage parentManagePage) {
        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.programName = programName;
        this.parentManagePage = parentManagePage;

        defaultSettings.setDefault(this);
        setTitle("In-Schedule Sessions — " + programName);
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("In-Schedule Sessions for " + programName, SwingConstants.CENTER);
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

        model = new DefaultTableModel(
                new Object[]{
                        "Session ID", "Date", "Start", "End", "Status"
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

        // double-click = Start session
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    startSelected();
                }
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);
        backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        refreshBtn = stdOutlinedButton("Refresh", defaultSettings.BORDER_COLOR);
        startBtn = stdGreenButton("Start Session");

        bottom.add(backBtn);
        bottom.add(refreshBtn);
        bottom.add(startBtn);

        center.add(bottom, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> {
            // just close; parent ManageClassSessionsPage stays open
            if (parentManagePage != null) {
                parentManagePage.reloadSessions();
            }
            dispose();
        });
        refreshBtn.addActionListener(e -> reloadSessions());
        startBtn.addActionListener(e -> startSelected());

        reloadSessions();
    }

    private Integer getSelectedId() {
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

    private void startSelected() {
        Integer id = getSelectedId();
        if (id == null) return;

        // open the run-session page; it will set IN_PROGRESS and later COMPLETED
        new RunSessionPage(
                currentUser,
                db,
                programId,
                programName,
                id,
                this
        ).setVisible(true);
    }

    /** Public so RunSessionPage can refresh after it finishes a session. */
    public void reloadSessions() {
        model.setRowCount(0);

        String sql =
                "SELECT session_id, session_date, start_time, end_time, status " +
                        "FROM class_session " +
                        "WHERE program_id=? AND status IN ('SCHEDULED','IN_PROGRESS') " +
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

                    model.addRow(new Object[]{
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
                    "Error loading in-schedule sessions:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Give RunSessionPage access to parent manage page (may be null). */
    public ManageClassSessionsPage getParentManagePage() {
        return parentManagePage;
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
