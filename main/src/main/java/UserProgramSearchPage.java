import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;

/**
 * Search & register page for normal users.
 * Allows searching programs (class / self-paced) and registering.
 */
public class UserProgramSearchPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final UserProgramsPage parentPage;   // to refresh unified list after register

    private JTextField searchField;
    private JCheckBox allTypesChk;
    private JCheckBox classChk;
    private JCheckBox planChk;

    private JTable resultsTable;
    private DefaultTableModel resultsModel;

    private JButton backBtn;
    private JButton refreshBtn;
    private JButton registerBtn;

    public UserProgramSearchPage(user currentUser,
                                 MySQLDatabaseConnector db,
                                 UserProgramsPage parentPage) {
        this.currentUser = currentUser;
        this.db = db;
        this.parentPage = parentPage;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — Search Classes & Plans");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Search Classes & Plans", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: search controls + table =====
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 12, 12, 12));
        add(center, BorderLayout.CENTER);

        // --- search controls (top of center) ---
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // row 1: search field
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(defaultSettings.TEXT_COLOR);
        searchField = new JTextField(30);

        // style input: black bg, green text, green caret, green border
        searchField.setBackground(defaultSettings.BACKGROUND_COLOR);
        searchField.setForeground(defaultSettings.TEXT_COLOR);
        searchField.setCaretColor(defaultSettings.TEXT_COLOR);
        searchField.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));

        JButton searchBtn = stdOutlinedButton("Search", defaultSettings.BORDER_COLOR);
        searchBtn.addActionListener(e -> reloadResults());

        searchRow.add(searchLbl);
        searchRow.add(searchField);
        searchRow.add(searchBtn);

        // row 2: type filters (All / Classes / Plans)
        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        typeRow.setOpaque(false);

        allTypesChk = new JCheckBox("All", true);
        classChk    = new JCheckBox("Classes");
        planChk     = new JCheckBox("Plans");

        styleCheckBox(allTypesChk);
        styleCheckBox(classChk);
        styleCheckBox(planChk);

        allTypesChk.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                classChk.setSelected(false);
                planChk.setSelected(false);
            }
            reloadResults();
        });

        classChk.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                allTypesChk.setSelected(false);
            } else if (!planChk.isSelected()) {
                allTypesChk.setSelected(true);
            }
            reloadResults();
        });

        planChk.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                allTypesChk.setSelected(false);
            } else if (!classChk.isSelected()) {
                allTypesChk.setSelected(true);
            }
            reloadResults();
        });

        JLabel typeLbl = new JLabel("Type:");
        typeLbl.setForeground(defaultSettings.TEXT_COLOR);
        typeRow.add(typeLbl);
        typeRow.add(allTypesChk);
        typeRow.add(classChk);
        typeRow.add(planChk);

        searchPanel.add(searchRow);
        searchPanel.add(typeRow);

        center.add(searchPanel, BorderLayout.NORTH);

        // --- results table ---
        resultsModel = new DefaultTableModel(
                new Object[]{
                        "Program ID",
                        "Type",
                        "Name",
                        "Trainer",
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

        resultsTable = new JTable(resultsModel);
        resultsTable.setBackground(defaultSettings.BACKGROUND_COLOR);
        resultsTable.setForeground(defaultSettings.TEXT_COLOR);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setGridColor(defaultSettings.BORDER_COLOR);
        resultsTable.setSelectionBackground(Color.DARK_GRAY);
        resultsTable.setSelectionForeground(defaultSettings.TEXT_COLOR);
        resultsTable.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        resultsTable.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        // double-click row → open details window
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && resultsTable.getSelectedRow() >= 0) {
                    openDetailsForSelected();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.add(scroll, BorderLayout.CENTER);

        // ==== SOUTH: buttons ====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        refreshBtn = stdOutlinedButton("Refresh", defaultSettings.BORDER_COLOR);
        registerBtn = stdGreenButton("Register Selected");

        bottom.add(backBtn);
        bottom.add(refreshBtn);
        bottom.add(registerBtn);

        add(bottom, BorderLayout.SOUTH);

        // button actions
        backBtn.addActionListener(e -> dispose());
        refreshBtn.addActionListener(e -> reloadResults());
        registerBtn.addActionListener(e -> registerSelectedProgram());

        // initial load
        reloadResults();
    }

    // ====== DATA LOADING ======

    private void reloadResults() {
        resultsModel.setRowCount(0);

        String searchText = searchField.getText() != null
                ? searchField.getText().trim()
                : "";

        boolean filterAll   = allTypesChk.isSelected() || (!classChk.isSelected() && !planChk.isSelected());
        boolean filterClass = classChk.isSelected();
        boolean filterPlan  = planChk.isSelected();

        StringBuilder sql = new StringBuilder(
                "SELECT ep.program_id, ep.program_type, ep.name, " +
                        "COALESCE(u.userN, ep.trainer_email) AS trainer_name, " +
                        "ep.fitness_level, ep.class_start_date, ep.class_end_date, " +
                        "ep.registration_end_date, ep.max_participants, " +
                        "COUNT(up2.user_program_id) AS current_enrolled " +
                        "FROM exercise_program ep " +
                        "LEFT JOIN user_program up2 ON up2.program_id = ep.program_id " +
                        "LEFT JOIN users u ON u.email = ep.trainer_email " +
                        "WHERE ep.is_active = 1 "
        );

        // type filter
        if (!filterAll) {
            sql.append(" AND ep.program_type IN (");
            boolean first = true;
            if (filterClass) {
                sql.append("'CLASS'");
                first = false;
            }
            if (filterPlan) {
                if (!first) sql.append(",");
                sql.append("'SELF_PACED'");
            }
            sql.append(") ");
        }

        // search filter
        boolean hasSearch = !searchText.isEmpty();
        if (hasSearch) {
            sql.append(" AND (");
            sql.append("ep.name LIKE ? ");
            sql.append("OR ep.trainer_email LIKE ? ");
            sql.append("OR COALESCE(u.userN,'') LIKE ? ");
            sql.append("OR CAST(ep.program_id AS CHAR) LIKE ? ");
            sql.append(") ");
        }

        sql.append(
                "GROUP BY ep.program_id, ep.program_type, ep.name, trainer_name, " +
                        "ep.fitness_level, ep.class_start_date, ep.class_end_date, " +
                        "ep.registration_end_date, ep.max_participants " +
                        "ORDER BY ep.class_start_date IS NULL, ep.class_start_date, ep.name"
        );

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (hasSearch) {
                String like = "%" + searchText + "%";
                ps.setString(idx++, like); // ep.name
                ps.setString(idx++, like); // trainer_email
                ps.setString(idx++, like); // trainer_name (userN)
                ps.setString(idx++, like); // program_id
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int programId      = rs.getInt("program_id");
                    String type        = rs.getString("program_type");
                    String name        = rs.getString("name");
                    String trainerName = rs.getString("trainer_name");
                    String fitness     = rs.getString("fitness_level");

                    Date start = rs.getDate("class_start_date");
                    Date end   = rs.getDate("class_end_date");
                    Date reg   = rs.getDate("registration_end_date");

                    Integer maxPart = (Integer) rs.getObject("max_participants");
                    int currentEnrolled = rs.getInt("current_enrolled");

                    String startStr = (start != null ? start.toString() : "");
                    String endStr   = (end   != null ? end.toString()   : "");
                    String regStr   = (reg   != null ? reg.toString()   : "");

                    String currentMaxStr;
                    if (maxPart == null || maxPart <= 0) {
                        currentMaxStr = currentEnrolled + " / —";
                    } else {
                        currentMaxStr = currentEnrolled + " / " + maxPart;
                    }

                    resultsModel.addRow(new Object[]{
                            programId,
                            type,
                            name,
                            (trainerName != null ? trainerName : ""),
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
                    "Error searching programs:\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ====== REGISTRATION LOGIC ======

    private void registerSelectedProgram() {
        int row = resultsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a program first.");
            return;
        }

        if (currentUser == null || currentUser.getEmail() == null) {
            JOptionPane.showMessageDialog(this, "No logged-in user.");
            return;
        }

        int programId = Integer.parseInt(resultsModel.getValueAt(row, 0).toString());

        try (Connection conn = MySQLDatabaseConnector.getConnection()) {

            // 1) already enrolled?
            String checkSql = "SELECT COUNT(*) FROM user_program WHERE user_email=? AND program_id=?";
            try (PreparedStatement cps = conn.prepareStatement(checkSql)) {
                cps.setString(1, currentUser.getEmail());
                cps.setInt(2, programId);
                try (ResultSet rs = cps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                                "You are already enrolled in this program.");
                        return;
                    }
                }
            }

            // 2) get program info
            String progSql =
                    "SELECT program_type, registration_end_date, max_participants " +
                            "FROM exercise_program " +
                            "WHERE program_id=? AND is_active=1";
            String programType;
            Date regDeadline = null;
            Integer maxPart = null;

            try (PreparedStatement pps = conn.prepareStatement(progSql)) {
                pps.setInt(1, programId);
                try (ResultSet rs = pps.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "Program not found or inactive.");
                        return;
                    }
                    programType = rs.getString("program_type");
                    regDeadline = rs.getDate("registration_end_date");
                    maxPart     = (Integer) rs.getObject("max_participants");
                }
            }

            // 3) if CLASS: check registration deadline & capacity
            if ("CLASS".equalsIgnoreCase(programType)) {
                LocalDate today = LocalDate.now();
                if (regDeadline != null && regDeadline.toLocalDate().isBefore(today)) {
                    JOptionPane.showMessageDialog(this,
                            "Registration for this class has closed.");
                    return;
                }

                if (maxPart != null && maxPart > 0) {
                    String countSql = "SELECT COUNT(*) FROM user_program WHERE program_id=?";
                    int enrolled;
                    try (PreparedStatement cps2 = conn.prepareStatement(countSql)) {
                        cps2.setInt(1, programId);
                        try (ResultSet rs = cps2.executeQuery()) {
                            enrolled = rs.next() ? rs.getInt(1) : 0;
                        }
                    }
                    if (enrolled >= maxPart) {
                        JOptionPane.showMessageDialog(this,
                                "This class is full. You cannot register.");
                        return;
                    }
                }
            }

            // 4) insert enrollment
            String insertSql = "INSERT INTO user_program (user_email, program_id) VALUES (?, ?)";
            try (PreparedStatement ips = conn.prepareStatement(insertSql)) {
                ips.setString(1, currentUser.getEmail());
                ips.setInt(2, programId);
                ips.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Successfully registered for this program!");

            // 5) refresh parent + this table so counts update
            if (parentPage != null) {
                parentPage.reloadMyPrograms();
            }
            reloadResults();

        } catch (SQLIntegrityConstraintViolationException dup) {
            JOptionPane.showMessageDialog(this,
                    "You are already enrolled in this program.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error registering:\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ====== DOUBLE-CLICK DETAILS ======

    private void openDetailsForSelected() {
        int row = resultsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a class or plan first.");
            return;
        }

        int programId = Integer.parseInt(resultsModel.getValueAt(row, 0).toString());
        // This page should already exist from earlier steps
        new UserProgramDetailPage(currentUser, db, programId, this).setVisible(true);
    }

    // ====== STYLE HELPERS ======

    private void styleCheckBox(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setForeground(defaultSettings.TEXT_COLOR);
    }

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
