import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ManagePlanStepsPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final String programName;

    private JTable stepsTable;
    private DefaultTableModel stepsModel;

    // form fields
    private JTextField stepOrderField;
    private JTextField titleField;
    private JTextArea instructionsArea;
    private JTextField targetMinutesField;
    private JTextField targetCaloriesField;

    // buttons
    private JButton backBtn;
    private JButton newStepBtn;
    private JButton saveStepBtn;
    private JButton deleteStepBtn;
    private JButton moveUpBtn;
    private JButton moveDownBtn;

    // currently edited step (null = new)
    private Integer currentStepId = null;

    public ManagePlanStepsPage(user currentUser,
                               MySQLDatabaseConnector db,
                               int programId,
                               String programName) {
        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.programName = programName;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — Manage Plan Steps");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel(
                "Manage Steps for Plan: " + programName + " (ID " + programId + ")",
                SwingConstants.CENTER
        );
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER (left table + right form) =====
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 12, 12, 12));
        add(center, BorderLayout.CENTER);

        // ---------- LEFT: steps table ----------
        stepsModel = new DefaultTableModel(
                new Object[]{"Step ID", "Order", "Title", "Target Minutes", "Target Calories"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        stepsTable = new JTable(stepsModel);
        stepsTable.setBackground(defaultSettings.BACKGROUND_COLOR);
        stepsTable.setForeground(defaultSettings.TEXT_COLOR);
        stepsTable.setFillsViewportHeight(true);
        stepsTable.setGridColor(defaultSettings.BORDER_COLOR);
        stepsTable.setSelectionBackground(Color.DARK_GRAY);
        stepsTable.setSelectionForeground(defaultSettings.TEXT_COLOR);

        JScrollPane tableScroll = new JScrollPane(stepsTable);
        tableScroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        tableScroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        stepsTable.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        stepsTable.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        // make sure scrollbars appear when needed
        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        tableScroll.setPreferredSize(new Dimension(420, 1)); // left side width
        center.add(tableScroll, BorderLayout.WEST);

        // HIDE Step ID column from the *view* (still in the model)
        stepsTable.removeColumn(stepsTable.getColumnModel().getColumn(0));

        stepsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int rowView = stepsTable.getSelectedRow();
                if (rowView >= 0) {
                    int rowModel = stepsTable.convertRowIndexToModel(rowView);
                    loadRowIntoForm(rowModel);
                }
            }
        });

        // ---------- RIGHT: form ----------
        JPanel formPanel = new JPanel();
        formPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JLabel orderLbl = stdLabel("Step Order (1,2,3...):");
        stepOrderField = stdTextField();

        JLabel titleLbl = stdLabel("Step Title:");
        titleField = stdTextField();

        JLabel instrLbl = stdLabel("Instructions:");
        instructionsArea = new JTextArea(5, 24);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setForeground(defaultSettings.TEXT_COLOR);
        instructionsArea.setBackground(defaultSettings.BACKGROUND_COLOR);
        instructionsArea.setCaretColor(defaultSettings.TEXT_COLOR);
        instructionsArea.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        JScrollPane instrScroll = new JScrollPane(instructionsArea);
        instrScroll.setPreferredSize(new Dimension(260, 120));
        instrScroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        instrScroll.setBackground(defaultSettings.BACKGROUND_COLOR);

        JLabel minsLbl = stdLabel("Target Duration (minutes, optional):");
        targetMinutesField = stdTextField();

        JLabel calLbl = stdLabel("Target Calories (optional):");
        targetCaloriesField = stdTextField();

        // left-align everything in the right panel
        alignLeft(orderLbl);
        alignLeft(stepOrderField);
        alignLeft(titleLbl);
        alignLeft(titleField);
        alignLeft(instrLbl);
        alignLeft(instrScroll);
        alignLeft(minsLbl);
        alignLeft(targetMinutesField);
        alignLeft(calLbl);
        alignLeft(targetCaloriesField);

        formPanel.add(orderLbl);
        formPanel.add(stepOrderField);
        formPanel.add(Box.createVerticalStrut(6));

        formPanel.add(titleLbl);
        formPanel.add(titleField);
        formPanel.add(Box.createVerticalStrut(6));

        formPanel.add(instrLbl);
        formPanel.add(instrScroll);
        formPanel.add(Box.createVerticalStrut(6));

        formPanel.add(minsLbl);
        formPanel.add(targetMinutesField);
        formPanel.add(Box.createVerticalStrut(6));

        formPanel.add(calLbl);
        formPanel.add(targetCaloriesField);
        formPanel.add(Box.createVerticalStrut(10));

        // buttons inside right side
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        formButtons.setOpaque(false);
        newStepBtn = stdGreenButton("New Step");
        saveStepBtn = stdOutlinedButton("Save / Update Step", defaultSettings.BORDER_COLOR);
        deleteStepBtn = stdOutlinedButton("Delete Selected", new Color(200, 40, 40));
        moveUpBtn = stdOutlinedButton("Move Up", defaultSettings.BORDER_COLOR);
        moveDownBtn = stdOutlinedButton("Move Down", defaultSettings.BORDER_COLOR);

        formButtons.add(newStepBtn);
        formButtons.add(saveStepBtn);
        formButtons.add(deleteStepBtn);
        formButtons.add(moveUpBtn);
        formButtons.add(moveDownBtn);

        alignLeft(formButtons);
        formPanel.add(formButtons);

        center.add(formPanel, BorderLayout.CENTER);

        // ===== SOUTH: back button =====
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomRow.setOpaque(false);
        backBtn = stdOutlinedButton("Back to Plans", defaultSettings.BORDER_COLOR);
        bottomRow.add(backBtn);
        add(bottomRow, BorderLayout.SOUTH);

        wireActions();
        loadStepsFromDb();
    }

    // ---------- actions ----------
    private void wireActions() {
        backBtn.addActionListener(e -> {
            dispose();
            new TrainerPlansPage(currentUser, db).setVisible(true);
        });

        newStepBtn.addActionListener(e -> clearForm());

        saveStepBtn.addActionListener(e -> saveOrUpdateStep());

        deleteStepBtn.addActionListener(e -> deleteSelectedStep());

        moveUpBtn.addActionListener(e -> moveStep(-1));
        moveDownBtn.addActionListener(e -> moveStep(+1));
    }

    // ---------- helpers for ints ----------
    private Integer parseOptionalInt(JTextField field, String label) {
        String txt = field.getText().trim();
        if (txt.isEmpty()) return null;
        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, label + " must be an integer.");
            return null;
        }
    }

    private Integer parseRequiredOrder() {
        String txt = stepOrderField.getText().trim();
        if (txt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Step order is required.");
            return null;
        }
        try {
            int v = Integer.parseInt(txt);
            if (v <= 0) {
                JOptionPane.showMessageDialog(this, "Step order must be >= 1.");
                return null;
            }
            return v;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Step order must be an integer.");
            return null;
        }
    }

    // ---------- load / form sync ----------
    private void loadStepsFromDb() {
        stepsModel.setRowCount(0);
        String sql = "SELECT step_id, step_order, title, " +
                "target_duration_minutes, target_calories " +
                "FROM program_step WHERE program_id=? " +
                "ORDER BY step_order, step_id";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sid = rs.getInt("step_id");
                    int order = rs.getInt("step_order");
                    String title = rs.getString("title");
                    Integer mins = (Integer) rs.getObject("target_duration_minutes");
                    Integer cals = (Integer) rs.getObject("target_calories");

                    stepsModel.addRow(new Object[]{
                            sid,
                            order,
                            (title != null ? title : ""),
                            (mins != null ? mins : ""),
                            (cals != null ? cals : "")
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

    private void loadRowIntoForm(int rowModel) {
        Object idObj = stepsModel.getValueAt(rowModel, 0);
        if (idObj == null) return;

        int stepId = Integer.parseInt(idObj.toString());
        currentStepId = stepId;

        String sql = "SELECT step_order, title, instructions, " +
                "target_duration_minutes, target_calories " +
                "FROM program_step WHERE step_id=? AND program_id=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stepId);
            ps.setInt(2, programId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int order = rs.getInt("step_order");
                    String title = rs.getString("title");
                    String instr = rs.getString("instructions");
                    Integer mins = (Integer) rs.getObject("target_duration_minutes");
                    Integer cals = (Integer) rs.getObject("target_calories");

                    stepOrderField.setText(String.valueOf(order));
                    titleField.setText(title != null ? title : "");
                    instructionsArea.setText(instr != null ? instr : "");
                    targetMinutesField.setText(mins != null ? mins.toString() : "");
                    targetCaloriesField.setText(cals != null ? cals.toString() : "");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading step:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        currentStepId = null;
        stepOrderField.setText("");
        titleField.setText("");
        instructionsArea.setText("");
        targetMinutesField.setText("");
        targetCaloriesField.setText("");

        // convenience: set default order = last + 1
        int rowCount = stepsModel.getRowCount();
        if (rowCount > 0) {
            Object lastOrderObj = stepsModel.getValueAt(rowCount - 1, 1);
            try {
                int lastOrder = Integer.parseInt(lastOrderObj.toString());
                stepOrderField.setText(String.valueOf(lastOrder + 1));
            } catch (NumberFormatException ignored) {}
        }
    }

    // ---------- save / update ----------
    private void saveOrUpdateStep() {
        Integer order = parseRequiredOrder();
        if (order == null) return;

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Step title is required.");
            return;
        }

        String instructions = instructionsArea.getText().trim();
        Integer mins = parseOptionalInt(targetMinutesField, "Target duration");
        if (mins == null && !targetMinutesField.getText().trim().isEmpty()) return;
        Integer cals = parseOptionalInt(targetCaloriesField, "Target calories");
        if (cals == null && !targetCaloriesField.getText().trim().isEmpty()) return;

        try (Connection conn = MySQLDatabaseConnector.getConnection()) {
            if (currentStepId == null) {
                // INSERT
                String sql = "INSERT INTO program_step " +
                        "(program_id, step_order, title, instructions, " +
                        " target_duration_minutes, target_calories) " +
                        "VALUES (?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, programId);
                    ps.setInt(2, order);
                    ps.setString(3, title);
                    ps.setString(4, instructions);
                    if (mins == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, mins);
                    if (cals == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, cals);
                    ps.executeUpdate();
                }
            } else {
                // UPDATE
                String sql = "UPDATE program_step SET " +
                        "step_order=?, title=?, instructions=?, " +
                        "target_duration_minutes=?, target_calories=? " +
                        "WHERE step_id=? AND program_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, order);
                    ps.setString(2, title);
                    ps.setString(3, instructions);
                    if (mins == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, mins);
                    if (cals == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, cals);
                    ps.setInt(6, currentStepId);
                    ps.setInt(7, programId);
                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Step saved.");
            loadStepsFromDb();
            clearForm();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving step:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- delete ----------
    private void deleteSelectedStep() {
        int rowView = stepsTable.getSelectedRow();
        if (rowView < 0) {
            JOptionPane.showMessageDialog(this, "Please select a step first.");
            return;
        }
        int rowModel = stepsTable.convertRowIndexToModel(rowView);
        Object idObj = stepsModel.getValueAt(rowModel, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "Invalid step ID.");
            return;
        }
        int stepId = Integer.parseInt(idObj.toString());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete step ID " + stepId + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM program_step WHERE step_id=? AND program_id=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stepId);
            ps.setInt(2, programId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Step deleted.");
            loadStepsFromDb();
            clearForm();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting step:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- move up / down (reorder) ----------
    private void moveStep(int delta) {
        int rowView = stepsTable.getSelectedRow();
        if (rowView < 0) {
            JOptionPane.showMessageDialog(this, "Please select a step to move.");
            return;
        }

        int rowModel = stepsTable.convertRowIndexToModel(rowView);
        int targetRowModel = rowModel + delta;
        if (targetRowModel < 0 || targetRowModel >= stepsModel.getRowCount()) {
            return; // can't move beyond bounds
        }

        int stepId = Integer.parseInt(stepsModel.getValueAt(rowModel, 0).toString());
        int stepOrder = Integer.parseInt(stepsModel.getValueAt(rowModel, 1).toString());

        int otherId = Integer.parseInt(stepsModel.getValueAt(targetRowModel, 0).toString());
        int otherOrder = Integer.parseInt(stepsModel.getValueAt(targetRowModel, 1).toString());

        String sql1 = "UPDATE program_step SET step_order=? WHERE step_id=? AND program_id=?";
        String sql2 = "UPDATE program_step SET step_order=? WHERE step_id=? AND program_id=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sql1);
                 PreparedStatement ps2 = conn.prepareStatement(sql2)) {

                // swap
                ps1.setInt(1, otherOrder);
                ps1.setInt(2, stepId);
                ps1.setInt(3, programId);
                ps1.executeUpdate();

                ps2.setInt(1, stepOrder);
                ps2.setInt(2, otherId);
                ps2.setInt(3, programId);
                ps2.executeUpdate();

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            loadStepsFromDb();

            // reselect moved row (convert back to view index)
            int newRowModel = targetRowModel;
            if (newRowModel >= 0 && newRowModel < stepsModel.getRowCount()) {
                int newRowView = stepsTable.convertRowIndexToView(newRowModel);
                stepsTable.setRowSelectionInterval(newRowView, newRowView);
                loadRowIntoForm(newRowModel);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error reordering steps:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- style helpers ----
    private JLabel stdLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(defaultSettings.TEXT_COLOR);
        l.setFont(defaultSettings.LABEL_FONT);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    private JTextField stdTextField() {
        JTextField f = new JTextField();
        Dimension d = new Dimension(260, 24);
        f.setPreferredSize(d);
        f.setMaximumSize(d);
        f.setForeground(defaultSettings.TEXT_COLOR);
        f.setBackground(defaultSettings.BACKGROUND_COLOR);
        f.setCaretColor(defaultSettings.TEXT_COLOR);
        f.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private void alignLeft(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (c instanceof JLabel) {
            ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
        }
    }

    private JButton stdGreenButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    private JButton stdOutlinedButton(String text, Color borderColor) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(borderColor, 2, true));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }
}
