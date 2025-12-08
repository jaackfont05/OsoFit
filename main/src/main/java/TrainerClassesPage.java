import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrainerClassesPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;

    private JTable classesTable;
    private DefaultTableModel classesModel;
    private JButton backBtn;
    private JButton manageSelectedBtn;

    public TrainerClassesPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — My Classes");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title + line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("All My Classes", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: table + buttons =====
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 12, 12, 12));
        add(center, BorderLayout.CENTER);

        // top: Back button
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topButtons.setOpaque(false);
        backBtn = stdOutlinedButton("Back to Trainer Programs", defaultSettings.BORDER_COLOR);
        topButtons.add(backBtn);
        center.add(topButtons, BorderLayout.NORTH);

        // table with CLASS-specific columns
        classesModel = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "Name",
                        "Fitness",
                        "Start Date",
                        "End Date",
                        "Days",
                        "Session Min",
                        "Weeks",
                        "Max",
                        "Reg Deadline"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        classesTable = new JTable(classesModel);
        classesTable.setBackground(defaultSettings.BACKGROUND_COLOR);
        classesTable.setForeground(defaultSettings.TEXT_COLOR);
        classesTable.setFillsViewportHeight(true);
        classesTable.setGridColor(defaultSettings.BORDER_COLOR);
        classesTable.setSelectionBackground(Color.DARK_GRAY);
        classesTable.setSelectionForeground(defaultSettings.TEXT_COLOR);

        classesTable.getTableHeader().setReorderingAllowed(true);
        classesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(classesTable);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollBar vTableBar = scroll.getVerticalScrollBar();
        vTableBar.setBackground(defaultSettings.BACKGROUND_COLOR);
        vTableBar.setForeground(defaultSettings.TEXT_COLOR);
        JScrollBar hTableBar = scroll.getHorizontalScrollBar();
        hTableBar.setBackground(defaultSettings.BACKGROUND_COLOR);
        hTableBar.setForeground(defaultSettings.TEXT_COLOR);

        classesTable.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        classesTable.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        center.add(scroll, BorderLayout.CENTER);

        // double-click → manage selected
        classesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && classesTable.getSelectedRow() >= 0) {
                    manageSelectedClass();
                }
            }
        });

        // bottom: Manage Selected (sessions)
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomRow.setOpaque(false);
        manageSelectedBtn = stdOutlinedButton("Manage Selected Class", new Color(200, 40, 40));
        bottomRow.add(manageSelectedBtn);
        center.add(bottomRow, BorderLayout.SOUTH);

        // wire actions
        backBtn.addActionListener(e -> {
            dispose();
            new TrainerProgramsPage(currentUser, db).setVisible(true);
        });

        manageSelectedBtn.addActionListener(e -> manageSelectedClass());

        // load data
        loadClassesFromDb();
    }

    private void manageSelectedClass() {
        int row = classesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a class first.");
            return;
        }

        Object idObj = classesModel.getValueAt(row, 0);
        Object nameObj = classesModel.getValueAt(row, 1);
        int programId = Integer.parseInt(idObj.toString());
        String programName = nameObj != null ? nameObj.toString() : ("Class " + programId);

        dispose();
        new ManageClassSessionsPage(currentUser, db, programId, programName).setVisible(true);
    }

    private void loadClassesFromDb() {
        classesModel.setRowCount(0);

        String sql =
                "SELECT program_id, name, fitness_level, " +
                        "class_start_date, class_end_date, class_days_of_week, " +
                        "class_session_minutes, class_num_weeks, " +
                        "max_participants, registration_end_date " +
                        "FROM exercise_program " +
                        "WHERE trainer_email = ? AND is_active = 1 " +
                        "  AND program_type = 'CLASS' " +
                        "ORDER BY created_at DESC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("program_id");
                    String name = rs.getString("name");
                    String fitness = rs.getString("fitness_level");

                    Date start = rs.getDate("class_start_date");
                    Date end = rs.getDate("class_end_date");
                    String days = rs.getString("class_days_of_week");
                    Integer sessionMin = (Integer) rs.getObject("class_session_minutes");
                    Integer weeks = (Integer) rs.getObject("class_num_weeks");
                    Integer maxPart = (Integer) rs.getObject("max_participants");
                    Date regDeadline = rs.getDate("registration_end_date");

                    classesModel.addRow(new Object[]{
                            id,
                            name,
                            fitness,
                            (start != null ? start.toString() : ""),
                            (end != null ? end.toString() : ""),
                            (days != null ? days : ""),
                            (sessionMin != null ? sessionMin : ""),
                            (weeks != null ? weeks : ""),
                            (maxPart != null ? maxPart : ""),
                            (regDeadline != null ? regDeadline.toString() : "")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading classes:\n" + e.getMessage(),
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
