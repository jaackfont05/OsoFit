import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class UserProgramDropPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final UserProgramsPage parentPage;

    private JTable table;
    private DefaultTableModel model;

    private JButton backBtn;
    private JButton refreshBtn;
    private JButton dropBtn;

    public UserProgramDropPage(user currentUser,
                               MySQLDatabaseConnector db,
                               UserProgramsPage parentPage) {
        this.currentUser = currentUser;
        this.db = db;
        this.parentPage = parentPage;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — Drop My Classes & Plans");
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ===== NORTH: menu bar + title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Drop My Classes & Plans", SwingConstants.CENTER);
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

        model = new DefaultTableModel(
                new Object[]{
                        "Program ID",
                        "Type",
                        "Name",
                        "Trainer",
                        "Fitness",
                        "Class Start",
                        "Class End",
                        "Enrolled At"
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

        // ===== SOUTH: buttons =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        refreshBtn = stdOutlinedButton("Refresh", defaultSettings.BORDER_COLOR);
        dropBtn = stdGreenButton("Drop Selected");

        bottom.add(backBtn);
        bottom.add(refreshBtn);
        bottom.add(dropBtn);

        add(bottom, BorderLayout.SOUTH);

        // Button actions
        backBtn.addActionListener(e -> dispose());
        refreshBtn.addActionListener(e -> reloadMyPrograms());
        dropBtn.addActionListener(e -> dropSelectedProgram());

        // initial load
        reloadMyPrograms();
    }

    // ===== DATA LOAD =====
    private void reloadMyPrograms() {
        model.setRowCount(0);

        if (currentUser == null || currentUser.getEmail() == null) {
            return;
        }

        String sql =
                "SELECT ep.program_id, ep.program_type, ep.name, " +
                        "       u.userN AS trainer_name, " +
                        "       ep.fitness_level, ep.class_start_date, ep.class_end_date, " +
                        "       up.enrolled_at " +
                        "FROM user_program up " +
                        "JOIN exercise_program ep ON up.program_id = ep.program_id " +
                        "JOIN users u ON ep.trainer_email = u.email " +
                        "WHERE up.user_email = ? AND ep.is_active = 1 " +
                        "ORDER BY up.enrolled_at DESC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int programId = rs.getInt("program_id");
                    String type   = rs.getString("program_type");
                    String name   = rs.getString("name");
                    String trainerName = rs.getString("trainer_name");
                    String fitness     = rs.getString("fitness_level");

                    Date start = rs.getDate("class_start_date");
                    Date end   = rs.getDate("class_end_date");
                    Timestamp enrolledAt = rs.getTimestamp("enrolled_at");

                    String startStr = (start != null ? start.toString() : "");
                    String endStr   = (end   != null ? end.toString()   : "");
                    String enrolledStr = (enrolledAt != null
                            ? enrolledAt.toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            : "");

                    model.addRow(new Object[]{
                            programId,
                            type,
                            name,
                            (trainerName != null ? trainerName : ""),
                            (fitness != null ? fitness : ""),
                            startStr,
                            endStr,
                            enrolledStr
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading your programs:\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== DROP LOGIC =====
    private void dropSelectedProgram() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a class or plan to drop.");
            return;
        }

        if (currentUser == null || currentUser.getEmail() == null) {
            JOptionPane.showMessageDialog(this, "No logged-in user.");
            return;
        }

        int programId = Integer.parseInt(model.getValueAt(row, 0).toString());
        String name   = model.getValueAt(row, 2).toString();

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to drop \"" + name + "\"?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = MySQLDatabaseConnector.getConnection()) {

            // Delete enrollment record
            String delSql = "DELETE FROM user_program WHERE user_email=? AND program_id=?";
            try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                ps.setString(1, currentUser.getEmail());
                ps.setInt(2, programId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    JOptionPane.showMessageDialog(this,
                            "You are not enrolled in this program (nothing to drop).");
                    return;
                }
            }

            // OPTIONAL: also remove any attendance rows for this user in this program's sessions
            // (skip if you don't want this behavior)
            String delAttSql =
                    "DELETE ca FROM class_attendance ca " +
                            "JOIN class_session cs ON ca.session_id = cs.session_id " +
                            "WHERE ca.user_email = ? AND cs.program_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delAttSql)) {
                ps.setString(1, currentUser.getEmail());
                ps.setInt(2, programId);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Program dropped successfully.");

            // refresh both this table and the unified list on UserProgramsPage
            reloadMyPrograms();
            if (parentPage != null) {
                parentPage.reloadMyPrograms();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error dropping program:\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== STYLE HELPERS =====
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
