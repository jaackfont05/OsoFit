import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class UserPlanStepsPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final String programName;
    private final UserProgramsPage parentPage;

    private JTable stepsTable;
    private DefaultTableModel stepsModel;

    public UserPlanStepsPage(user currentUser,
                             MySQLDatabaseConnector db,
                             int programId,
                             String programName,
                             UserProgramsPage parentPage) {
        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.programName = programName;
        this.parentPage = parentPage;

        defaultSettings.setDefault(this);       // full-size
        setTitle("OsoFit — Plan Steps");
        setLayout(new BorderLayout(10, 10));
        // Do not override close operation → behaves like main pages

        // ===== NORTH: title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Plan Steps — " + programName, SwingConstants.CENTER);
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

        stepsModel = new DefaultTableModel(
                new Object[]{
                        "Step #",
                        "Title",
                        "Target mins",
                        "Target calories"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stepsTable = new JTable(stepsModel);
        stepsTable.setBackground(defaultSettings.BACKGROUND_COLOR);
        stepsTable.setForeground(defaultSettings.TEXT_COLOR);
        stepsTable.setFillsViewportHeight(true);
        stepsTable.setGridColor(defaultSettings.BORDER_COLOR);
        stepsTable.setSelectionBackground(Color.DARK_GRAY);
        stepsTable.setSelectionForeground(defaultSettings.TEXT_COLOR);
        stepsTable.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        stepsTable.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        JScrollPane scroll = new JScrollPane(stepsTable);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.add(scroll, BorderLayout.CENTER);

        // double-click → open step details
        stepsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && stepsTable.getSelectedRow() >= 0) {
                    openSelectedStep();
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
        refreshBtn.addActionListener(e -> reloadSteps());
        viewBtn.addActionListener(e -> openSelectedStep());

        reloadSteps();
    }

    private void reloadSteps() {
        stepsModel.setRowCount(0);

        String sql =
                "SELECT step_order, title, target_duration_minutes, target_calories " +
                        "FROM program_step " +
                        "WHERE program_id = ? " +
                        "ORDER BY step_order ASC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int order = rs.getInt("step_order");
                    String title = rs.getString("title");
                    Integer mins = (Integer) rs.getObject("target_duration_minutes");
                    Integer cals = (Integer) rs.getObject("target_calories");

                    stepsModel.addRow(new Object[]{
                            order,
                            title == null ? "" : title,
                            mins == null ? "" : mins.toString(),
                            cals == null ? "" : cals.toString()
                    });
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading steps:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedStep() {
        int row = stepsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a step first.");
            return;
        }
        int modelRow = stepsTable.convertRowIndexToModel(row);
        int stepOrder = Integer.parseInt(
                stepsModel.getValueAt(modelRow, 0).toString());

        new UserPlanStepDetailPage(currentUser, db, programId, stepOrder, this)
                .setVisible(true);
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
