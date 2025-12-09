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

/**
 * Hub page for normal users:
 * - Search & register for classes/plans
 * - Drop existing classes/plans
 * - View today's sessions
 * - See a unified list of all programs they are enrolled in
 */
public class UserProgramsPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;

    private JTable myProgramsTable;
    private DefaultTableModel myProgramsModel;

    private JButton searchRegisterBtn;
    private JButton dropProgramsBtn;
    private JButton todaySessionsBtn;
    private JButton viewSelectedBtn;

    public UserProgramsPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — My Classes & Plans");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("My Classes & Plans", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: top buttons + table + bottom row =====
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 12, 12, 12));
        add(center, BorderLayout.CENTER);

        // --- top row of buttons ---
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topButtons.setOpaque(false);

        searchRegisterBtn = stdGreenButton("Search & Register");
        dropProgramsBtn   = stdOutlinedButton("Drop My Classes/Plans", defaultSettings.BORDER_COLOR);
        todaySessionsBtn  = stdOutlinedButton("View My Today Sessions", defaultSettings.BORDER_COLOR);

        topButtons.add(searchRegisterBtn);
        topButtons.add(dropProgramsBtn);
        topButtons.add(todaySessionsBtn);

        center.add(topButtons, BorderLayout.NORTH);

        // --- unified table of all programs user is enrolled in ---
        myProgramsModel = new DefaultTableModel(
                new Object[]{
                        "Program ID",
                        "Type",
                        "Name",
                        "Trainer",      // now userN, not email
                        "Fitness",
                        "Start Date",
                        "End Date",
                        "Reg Deadline",
                        "Current / Max"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        myProgramsTable = new JTable(myProgramsModel);
        myProgramsTable.setBackground(defaultSettings.BACKGROUND_COLOR);
        myProgramsTable.setForeground(defaultSettings.TEXT_COLOR);
        myProgramsTable.setFillsViewportHeight(true);
        myProgramsTable.setGridColor(defaultSettings.BORDER_COLOR);
        myProgramsTable.setSelectionBackground(Color.DARK_GRAY);
        myProgramsTable.setSelectionForeground(defaultSettings.TEXT_COLOR);
        myProgramsTable.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        myProgramsTable.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        JScrollPane scroll = new JScrollPane(myProgramsTable);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.add(scroll, BorderLayout.CENTER);

        // double-click row → view selected program (sessions/steps)
        myProgramsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && myProgramsTable.getSelectedRow() >= 0) {
                    viewSelectedProgram();
                }
            }
        });

        // --- bottom row: view selected ---
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomRow.setOpaque(false);

        viewSelectedBtn = stdOutlinedButton("View Sessions / Steps", defaultSettings.BORDER_COLOR);
        bottomRow.add(viewSelectedBtn);

        center.add(bottomRow, BorderLayout.SOUTH);

        wireActions();
        reloadMyPrograms();
    }

    private void wireActions() {
        // Search & Register
        searchRegisterBtn.addActionListener(e ->
                new UserProgramSearchPage(currentUser, db, this).setVisible(true));

        // Drop programs
        dropProgramsBtn.addActionListener(e ->
                new UserProgramDropPage(currentUser, db, this).setVisible(true));

        // Today sessions
        todaySessionsBtn.addActionListener(e ->
                new UserTodaySessionsPage(currentUser, db, this).setVisible(true));

        // View selected program (class sessions or plan steps)
        viewSelectedBtn.addActionListener(e -> viewSelectedProgram());
    }

    private void viewSelectedProgram() {
        int row = myProgramsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a program first.");
            return;
        }

        int modelRow = myProgramsTable.convertRowIndexToModel(row);

        Object idObj   = myProgramsModel.getValueAt(modelRow, 0);
        Object typeObj = myProgramsModel.getValueAt(modelRow, 1);
        Object nameObj = myProgramsModel.getValueAt(modelRow, 2);

        int programId = Integer.parseInt(idObj.toString());
        String type   = typeObj.toString();
        String name   = nameObj != null ? nameObj.toString() : "";

        if ("CLASS".equalsIgnoreCase(type)) {
            new UserClassSessionsPage(currentUser, db, programId, name, this).setVisible(true);
        } else if ("SELF_PACED".equalsIgnoreCase(type)) {
            new UserPlanStepsPage(currentUser, db, programId, name, this).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Unknown program type: " + type);
        }
    }

    /**
     * Reloads the unified list of programs this user is enrolled in.
     * Call this again from other pages after registering/dropping.
     */
    public void reloadMyPrograms() {
        myProgramsModel.setRowCount(0);

        if (currentUser == null || currentUser.getEmail() == null) {
            return;
        }

        String sql =
                "SELECT " +
                        "  ep.program_id, " +
                        "  ep.program_type, " +
                        "  ep.name, " +
                        "  u.userN AS trainer_name, " +
                        "  ep.fitness_level, " +
                        "  ep.class_start_date, " +
                        "  ep.class_end_date, " +
                        "  ep.registration_end_date, " +
                        "  ep.max_participants, " +
                        "  COUNT(up2.user_program_id) AS current_enrolled " +
                        "FROM user_program up " +
                        "JOIN exercise_program ep ON up.program_id = ep.program_id " +
                        "JOIN users u ON ep.trainer_email = u.email " +
                        "LEFT JOIN user_program up2 ON up2.program_id = ep.program_id " +
                        "WHERE up.user_email = ? " +
                        "GROUP BY " +
                        "  ep.program_id, ep.program_type, ep.name, u.userN, " +
                        "  ep.fitness_level, ep.class_start_date, ep.class_end_date, " +
                        "  ep.registration_end_date, ep.max_participants " +
                        "ORDER BY ep.created_at DESC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int programId = rs.getInt("program_id");
                    String type   = rs.getString("program_type");
                    String name   = rs.getString("name");
                    String trainerName = rs.getString("trainer_name");
                    String fitness      = rs.getString("fitness_level");

                    Date startDate = rs.getDate("class_start_date");
                    Date endDate   = rs.getDate("class_end_date");
                    Date regEnd    = rs.getDate("registration_end_date");

                    Integer maxPart = (Integer) rs.getObject("max_participants");
                    int currentEnrolled = rs.getInt("current_enrolled");

                    String startStr = (startDate != null ? startDate.toString() : "");
                    String endStr   = (endDate   != null ? endDate.toString()   : "");
                    String regStr   = (regEnd    != null ? regEnd.toString()    : "");

                    String currentMaxStr;
                    if (maxPart == null || maxPart <= 0) {
                        // for self-paced plans or unlimited classes
                        currentMaxStr = currentEnrolled + " / —";
                    } else {
                        currentMaxStr = currentEnrolled + " / " + maxPart;
                    }

                    myProgramsModel.addRow(new Object[]{
                            programId,
                            type,
                            name,
                            trainerName,  // <--- name only
                            (fitness != null ? fitness : ""),
                            startStr,
                            endStr,
                            regStr,
                            currentMaxStr
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

    // ===== small style helpers =====

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
