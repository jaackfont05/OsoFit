import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ClassAttendanceRankingPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final String programName;

    private JTable table;
    private DefaultTableModel model;
    private JButton backBtn;
    private JButton refreshBtn;

    public ClassAttendanceRankingPage(user currentUser,
                                      MySQLDatabaseConnector db,
                                      int programId,
                                      String programName) {
        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.programName = programName;

        defaultSettings.setDefault(this);
        setTitle("Attendance Ranking — " + programName);
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Attendance Ranking for Class: " + programName, SwingConstants.CENTER);
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
                        "User Email",
                        "Attended Sessions",
                        "Completed Sessions",
                        "Attendance Rate"
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

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);
        backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        refreshBtn = stdOutlinedButton("Refresh", defaultSettings.BORDER_COLOR);
        bottom.add(backBtn);
        bottom.add(refreshBtn);
        center.add(bottom, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> dispose());
        refreshBtn.addActionListener(e -> loadRanking());

        loadRanking();
    }

    private void loadRanking() {
        model.setRowCount(0);

        int completedSessions = 0;
        String countSql =
                "SELECT COUNT(*) FROM class_session " +
                        "WHERE program_id=? AND status='COMPLETED'";
        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement cps = conn.prepareStatement(countSql)) {
            cps.setInt(1, programId);
            try (ResultSet rs = cps.executeQuery()) {
                if (rs.next()) {
                    completedSessions = rs.getInt(1);
                }
            }

            String sql =
                    "SELECT a.user_email, " +
                            "SUM(CASE WHEN a.status='ATTENDED' THEN 1 ELSE 0 END) AS attended_count " +
                            "FROM class_attendance a " +
                            "JOIN class_session s ON a.session_id = s.session_id " +
                            "WHERE s.program_id=? AND s.status='COMPLETED' " +
                            "GROUP BY a.user_email " +
                            "ORDER BY attended_count DESC, a.user_email ASC";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, programId);

                try (ResultSet rs2 = ps.executeQuery()) {
                    while (rs2.next()) {
                        String email = rs2.getString("user_email");
                        int attendedCount = rs2.getInt("attended_count");

                        String rateStr;
                        if (completedSessions <= 0) {
                            rateStr = "N/A";
                        } else {
                            double rate = (double) attendedCount / completedSessions;
                            int pct = (int) Math.round(rate * 100.0);
                            rateStr = attendedCount + "/" + completedSessions + " (" + pct + "%)";
                        }

                        model.addRow(new Object[]{
                                email,
                                attendedCount,
                                completedSessions,
                                rateStr
                        });
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading ranking:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
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
