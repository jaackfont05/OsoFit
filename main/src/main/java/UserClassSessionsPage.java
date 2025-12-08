import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class UserClassSessionsPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final String programName;
    private final UserProgramsPage parentPage;

    private JTable sessionsTable;
    private DefaultTableModel sessionsModel;

    public UserClassSessionsPage(user currentUser,
                                 MySQLDatabaseConnector db,
                                 int programId,
                                 String programName,
                                 UserProgramsPage parentPage) {
        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.programName = programName;
        this.parentPage = parentPage;

        defaultSettings.setDefault(this);          // gives full-size window
        setTitle("OsoFit — Class Sessions");
        setLayout(new BorderLayout(10, 10));
        // DO NOT override close operation → behaves like other main pages

        // ===== NORTH: title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Class Sessions — " + programName, SwingConstants.CENTER);
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

        sessionsModel = new DefaultTableModel(
                new Object[]{
                        "Session ID",   // hidden
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

        // hide Session ID column visually
        sessionsTable.getColumnModel().getColumn(0).setMinWidth(0);
        sessionsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        sessionsTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // double-click → open session detail
        sessionsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && sessionsTable.getSelectedRow() >= 0) {
                    openSelectedSession();
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
        refreshBtn.addActionListener(e -> reloadSessions());
        viewBtn.addActionListener(e -> openSelectedSession());

        reloadSessions();
    }

    private void reloadSessions() {
        sessionsModel.setRowCount(0);

        String sql =
                "SELECT session_id, session_date, start_time, end_time, status " +
                        "FROM class_session " +
                        "WHERE program_id = ? " +
                        "ORDER BY session_date ASC, start_time ASC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sessionId = rs.getInt("session_id");
                    Date date = rs.getDate("session_date");
                    Time start = rs.getTime("start_time");
                    Time end   = rs.getTime("end_time");
                    String status = rs.getString("status");

                    sessionsModel.addRow(new Object[]{
                            sessionId,
                            date == null ? "" : date.toString(),
                            start == null ? "" : start.toString(),
                            end   == null ? "" : end.toString(),
                            status == null ? "" : status
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

    private void openSelectedSession() {
        int row = sessionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a session first.");
            return;
        }
        int modelRow = sessionsTable.convertRowIndexToModel(row);
        int sessionId = Integer.parseInt(
                sessionsModel.getValueAt(modelRow, 0).toString());

        // reuse existing session detail page
        new UserSessionDetailPage(currentUser, db, sessionId, this).setVisible(true);
    }

    // ===== small helpers =====
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
