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

public class TrainerProgramsPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;

    private JTable programsTable;
    private DefaultTableModel programsModel;

    private JButton manageAllBtn;
    private JButton viewClassesBtn;
    private JButton viewPlansBtn;
    private JButton manageSelectedBtn;

    public TrainerProgramsPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — Trainer Programs");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Trainer Programs", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: top buttons + table + bottom button =====
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 12, 12, 12));
        add(center, BorderLayout.CENTER);

        // --- top row buttons ---
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topButtons.setOpaque(false);

        manageAllBtn   = stdGreenButton("Manage My Classes & Plans");
        viewClassesBtn = stdOutlinedButton("View All My Classes", defaultSettings.BORDER_COLOR);
        viewPlansBtn   = stdOutlinedButton("View All My Plans", defaultSettings.BORDER_COLOR);

        topButtons.add(manageAllBtn);
        topButtons.add(viewClassesBtn);
        topButtons.add(viewPlansBtn);

        center.add(topButtons, BorderLayout.NORTH);

        // --- table: all programs for this trainer ---
        programsModel = new DefaultTableModel(
                new Object[]{"ID", "Type", "Name", "Fitness",
                        "Start Date", "End Date", "Max Participants"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        programsTable = new JTable(programsModel);
        programsTable.setBackground(defaultSettings.BACKGROUND_COLOR);
        programsTable.setForeground(defaultSettings.TEXT_COLOR);
        programsTable.setFillsViewportHeight(true);
        programsTable.setGridColor(defaultSettings.BORDER_COLOR);
        programsTable.setSelectionBackground(Color.DARK_GRAY);
        programsTable.setSelectionForeground(defaultSettings.TEXT_COLOR);

        programsTable.getTableHeader().setReorderingAllowed(true);
        programsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(programsTable);
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

        programsTable.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        programsTable.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        center.add(scroll, BorderLayout.CENTER);

        // double-click row = manage selected program
        programsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && programsTable.getSelectedRow() >= 0) {
                    manageSelectedProgram();
                }
            }
        });

        // --- bottom: Manage Selected button ---
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomRow.setOpaque(false);

        manageSelectedBtn = stdOutlinedButton("Manage Selected", new Color(200, 40, 40));
        bottomRow.add(manageSelectedBtn);

        center.add(bottomRow, BorderLayout.SOUTH);

        wireActions();
        loadProgramsFromDb();
    }

    // ================== BUTTON ACTIONS ==================

    private void wireActions() {
        manageAllBtn.addActionListener(e -> {
            dispose();
            new ManageProgramsPage(currentUser, db).setVisible(true);
        });

        viewClassesBtn.addActionListener(e -> {
            dispose();
            new TrainerClassesPage(currentUser, db).setVisible(true);
        });

        viewPlansBtn.addActionListener(e -> {
            dispose();
            new TrainerPlansPage(currentUser, db).setVisible(true);
        });

        manageSelectedBtn.addActionListener(e -> manageSelectedProgram());
    }

    private void manageSelectedProgram() {
        int row = programsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a program first.");
            return;
        }

        Object idObj   = programsModel.getValueAt(row, 0);
        Object typeObj = programsModel.getValueAt(row, 1);
        Object nameObj = programsModel.getValueAt(row, 2);

        int programId = Integer.parseInt(idObj.toString());
        String type   = (typeObj != null) ? typeObj.toString() : "";
        String name   = (nameObj != null) ? nameObj.toString() : ("Program " + programId);

        if ("CLASS".equalsIgnoreCase(type)) {
            // go to ManageClassSessionsPage for this class
            dispose();
            new ManageClassSessionsPage(currentUser, db, programId, name).setVisible(true);
        } else if ("SELF_PACED".equalsIgnoreCase(type)) {
            // go to ManagePlanStepsPage for this plan
            dispose();
            new ManagePlanStepsPage(currentUser, db, programId, name).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Unknown program type: " + type);
        }
    }

    // ================== DB LOAD (ALL PROGRAMS) ==================

    private void loadProgramsFromDb() {
        programsModel.setRowCount(0);

        String sql =
                "SELECT program_id, program_type, name, fitness_level, " +
                        "class_start_date, class_end_date, max_participants " +
                        "FROM exercise_program " +
                        "WHERE trainer_email = ? AND is_active = 1 " +
                        "ORDER BY created_at DESC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("program_id");
                    String type = rs.getString("program_type");
                    String name = rs.getString("name");
                    String fitness = rs.getString("fitness_level");

                    Date start = rs.getDate("class_start_date");
                    Date end = rs.getDate("class_end_date");
                    Integer maxPart = (Integer) rs.getObject("max_participants");

                    programsModel.addRow(new Object[]{
                            id,
                            type,
                            name,
                            fitness,
                            (start != null ? start.toString() : ""),
                            (end != null ? end.toString() : ""),
                            (maxPart != null ? maxPart : "")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading programs:\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== STYLE HELPERS ==================

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
