import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrainerPlansPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;

    private JTable plansTable;
    private DefaultTableModel plansModel;
    private JButton backBtn;
    private JButton manageSelectedBtn;

    public TrainerPlansPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — My Self-paced Plans");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("All My Self-paced Plans", SwingConstants.CENTER);
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

        // top: back button
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topButtons.setOpaque(false);
        backBtn = stdOutlinedButton("Back to Trainer Programs", defaultSettings.BORDER_COLOR);
        topButtons.add(backBtn);
        center.add(topButtons, BorderLayout.NORTH);

        // table with plan-specific columns
        plansModel = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "Name",
                        "Fitness",
                        "Avg Minutes",
                        "Sessions/Week",
                        "Equipment"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        plansTable = new JTable(plansModel);
        plansTable.setBackground(defaultSettings.BACKGROUND_COLOR);
        plansTable.setForeground(defaultSettings.TEXT_COLOR);
        plansTable.setFillsViewportHeight(true);
        plansTable.setGridColor(defaultSettings.BORDER_COLOR);
        plansTable.setSelectionBackground(Color.DARK_GRAY);
        plansTable.setSelectionForeground(defaultSettings.TEXT_COLOR);

        plansTable.getTableHeader().setReorderingAllowed(true);
        plansTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(plansTable);
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

        plansTable.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        plansTable.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        center.add(scroll, BorderLayout.CENTER);

        // double-click → manage plan steps
        plansTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && plansTable.getSelectedRow() >= 0) {
                    manageSelectedPlan();
                }
            }
        });

        // bottom: manage selected
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomRow.setOpaque(false);
        manageSelectedBtn = stdOutlinedButton("Manage Selected Plan", new Color(200, 40, 40));
        bottomRow.add(manageSelectedBtn);
        center.add(bottomRow, BorderLayout.SOUTH);

        // actions
        backBtn.addActionListener(e -> {
            dispose();
            new TrainerProgramsPage(currentUser, db).setVisible(true);
        });

        manageSelectedBtn.addActionListener(e -> manageSelectedPlan());

        // load data
        loadPlansFromDb();
    }

    private void manageSelectedPlan() {
        int row = plansTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a plan first.");
            return;
        }

        Object idObj = plansModel.getValueAt(row, 0);
        Object nameObj = plansModel.getValueAt(row, 1);
        int programId = Integer.parseInt(idObj.toString());
        String programName = (nameObj != null) ? nameObj.toString() : ("Plan " + programId);

        // Open ManagePlanStepsPage
        dispose();
        new ManagePlanStepsPage(currentUser, db, programId, programName).setVisible(true);
    }

    private void loadPlansFromDb() {
        plansModel.setRowCount(0);

        String sql =
                "SELECT program_id, name, fitness_level, " +
                        "avg_session_minutes, suggested_freq_per_week, required_equipment " +
                        "FROM exercise_program " +
                        "WHERE trainer_email = ? AND is_active = 1 " +
                        "  AND program_type = 'SELF_PACED' " +
                        "ORDER BY created_at DESC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("program_id");
                    String name = rs.getString("name");
                    String fitness = rs.getString("fitness_level");
                    Integer avgMin = (Integer) rs.getObject("avg_session_minutes");
                    Integer freq = (Integer) rs.getObject("suggested_freq_per_week");
                    String equip = rs.getString("required_equipment");

                    plansModel.addRow(new Object[]{
                            id,
                            name,
                            fitness,
                            (avgMin != null ? avgMin : ""),
                            (freq != null ? freq : ""),
                            (equip != null ? equip : "")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading plans:\n" + e.getMessage(),
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
