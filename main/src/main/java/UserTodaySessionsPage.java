import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class UserTodaySessionsPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final UserProgramsPage parentPage;

    private JTable sessionsTable;
    private DefaultTableModel sessionsModel;

    public UserTodaySessionsPage(user currentUser,
                                 MySQLDatabaseConnector db,
                                 UserProgramsPage parentPage) {
        this.currentUser = currentUser;
        this.db = db;
        this.parentPage = parentPage;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — My Sessions Today");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("My Sessions Today", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: table =====
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 12, 12, 12));
        add(center, BorderLayout.CENTER);

        // NOTE: first column is Session ID (hidden later)
        sessionsModel = new DefaultTableModel(
                new Object[]{
                        "Session ID",   // hidden
                        "Program ID",
                        "Class Name",
                        "Trainer",
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

        // hide Session ID column visually
        sessionsTable.getColumnModel().getColumn(0).setMinWidth(0);
        sessionsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        sessionsTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scroll = new JScrollPane(sessionsTable);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.add(scroll, BorderLayout.CENTER);

        // double-click row → open detail window
        sessionsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && sessionsTable.getSelectedRow() >= 0) {
                    openSelectedSessionDetail();
                }
            }
        });

        // ===== SOUTH: buttons =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        JButton backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        JButton refreshBtn = stdOutlinedButton("Refresh", defaultSettings.BORDER_COLOR);
        JButton viewBtn = stdGreenButton("View Selected");

        bottom.add(backBtn);
        bottom.add(refreshBtn);
        bottom.add(viewBtn);
        add(bottom, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> dispose());
        refreshBtn.addActionListener(e -> reloadTodaySessions());
        viewBtn.addActionListener(e -> openSelectedSessionDetail());

        // initial load
        reloadTodaySessions();
    }

    private void reloadTodaySessions() {
        sessionsModel.setRowCount(0);

        if (currentUser == null || currentUser.getEmail() == null) {
            return;
        }

        String sql =
                "SELECT cs.session_id, cs.program_id, ep.name AS class_name, " +
                        "       u.userN AS trainer_name, " +
                        "       cs.session_date, cs.start_time, cs.end_time, cs.status " +
                        "FROM class_session cs " +
                        "JOIN exercise_program ep ON cs.program_id = ep.program_id " +
                        "JOIN user_program up ON up.program_id = ep.program_id " +
                        "JOIN users u ON ep.trainer_email = u.email " +
                        "WHERE up.user_email = ? " +
                        "  AND cs.session_date = CURDATE() " +
                        "ORDER BY cs.start_time";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sessionId = rs.getInt("session_id");
                    int programId = rs.getInt("program_id");
                    String className = rs.getString("class_name");
                    String trainerName = rs.getString("trainer_name");
                    Date date = rs.getDate("session_date");
                    Time startTime = rs.getTime("start_time");
                    Time endTime = rs.getTime("end_time");
                    String status = rs.getString("status");

                    sessionsModel.addRow(new Object[]{
                            sessionId,                     // hidden
                            programId,
                            (className != null ? className : ""),
                            (trainerName != null ? trainerName : ""),
                            (date != null ? date.toString() : ""),
                            (startTime != null ? startTime.toString() : ""),
                            (endTime != null ? endTime.toString() : ""),
                            (status != null ? status : "")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading today's sessions:\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedSessionDetail() {
        int row = sessionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a session first.");
            return;
        }

        // get hidden Session ID from model
        int modelRow = sessionsTable.convertRowIndexToModel(row);
        int sessionId = Integer.parseInt(
                sessionsModel.getValueAt(modelRow, 0).toString()
        );

        new UserSessionDetailPage(currentUser, db, sessionId, this).setVisible(true);
    }

    // ===== style helpers =====

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
