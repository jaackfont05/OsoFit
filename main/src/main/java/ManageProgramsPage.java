import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ManageProgramsPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;

    // form fields
    private JComboBox<String> typeCombo;          // SELF_PACED / CLASS
    private JTextField nameField;
    private JTextArea descArea;
    private JComboBox<String> fitnessCombo;
    private JTextField equipmentField;

    // self-paced fields
    private JTextField avgSessionMinutesField;
    private JTextField freqPerWeekField;

    // class fields
    private JTextField classStartDateField;       // YYYY-MM-DD
    private JTextField classEndDateField;         // YYYY-MM-DD
    private JTextField classDaysOfWeekField;      // MON,WED
    private JTextField classSessionMinutesField;  // e.g. 60
    private JTextField classNumWeeksField;        // e.g. 6
    private JTextField maxParticipantsField;      // e.g. 20
    private JTextField registrationEndDateField;  // YYYY-MM-DD

    // panels for switching self-paced vs class
    private JPanel selfPanel;
    private JPanel classPanel;

    // table
    private JTable programsTable;
    private DefaultTableModel programsModel;

    // currently edited program id (null = new)
    private Integer selectedProgramId = null;

    public ManageProgramsPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

        // same defaults as mainPage
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Manage Programs");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Manage My Classes & Plans", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: left form (scroll) + right table =====
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(8, 12, 12, 12));
        add(center, BorderLayout.CENTER);

        // LEFT: form with vertical + horizontal scroll
        JPanel formPanel = buildFormPanel();
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        formScroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        formScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // dark scrollbars
        JScrollBar vFormBar = formScroll.getVerticalScrollBar();
        vFormBar.setBackground(defaultSettings.BACKGROUND_COLOR);
        vFormBar.setForeground(defaultSettings.TEXT_COLOR);
        JScrollBar hFormBar = formScroll.getHorizontalScrollBar();
        hFormBar.setBackground(defaultSettings.BACKGROUND_COLOR);
        hFormBar.setForeground(defaultSettings.TEXT_COLOR);

        // keep left side relatively narrow
        formScroll.setPreferredSize(new Dimension(400, 1));
        center.add(formScroll, BorderLayout.WEST);

        // RIGHT: table
        JPanel tablePanel = buildTablePanel();
        center.add(tablePanel, BorderLayout.CENTER);

        // default to SELF_PACED view
        typeCombo.setSelectedItem("SELF_PACED");
        updateProgramTypePanels();

        // initial load
        loadProgramsFromDb();
    }

    // ========= LEFT FORM =========
    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setBackground(defaultSettings.BACKGROUND_COLOR);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(360, 900)); // height mainly controlled by scroll

        // fixed sizes
        Dimension shortFieldSize = new Dimension(220, 26);
        Dimension descSize = new Dimension(260, 70);

        // Program Type
        JLabel typeLbl = stdLabel("Program Type:");
        typeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeCombo = new JComboBox<>(new String[]{"SELF_PACED", "CLASS"});
        styleCombo(typeCombo);
        typeCombo.addActionListener(e -> updateProgramTypePanels());
        typeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Name
        JLabel nameLbl = stdLabel("Name:");
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField = stdTextField();
        nameField.setPreferredSize(shortFieldSize);
        nameField.setMaximumSize(shortFieldSize);
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Description
        JLabel descLbl = stdLabel("Description:");
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        descArea = new JTextArea(3, 22);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setForeground(defaultSettings.TEXT_COLOR);
        descArea.setBackground(defaultSettings.BACKGROUND_COLOR);
        descArea.setCaretColor(defaultSettings.TEXT_COLOR);
        descArea.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(descSize);
        descScroll.setMaximumSize(descSize);
        descScroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        descScroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fitness level
        JLabel fitnessLbl = stdLabel("Fitness Level:");
        fitnessLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        fitnessCombo = new JComboBox<>(new String[]{"BEGINNER", "INTERMEDIATE", "ADVANCED"});
        styleCombo(fitnessCombo);
        fitnessCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Equipment
        JLabel equipLbl = stdLabel("Required Equipment:");
        equipLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        equipmentField = stdTextField();
        equipmentField.setPreferredSize(shortFieldSize);
        equipmentField.setMaximumSize(shortFieldSize);
        equipmentField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ===== self-paced sub-panel =====
        selfPanel = new JPanel();
        selfPanel.setOpaque(false);
        selfPanel.setLayout(new BoxLayout(selfPanel, BoxLayout.Y_AXIS));
        selfPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel selfHeader = stdSectionLabel("Self-paced Plan Details");
        selfHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel avgMinutesLbl = stdLabel("Avg Session Minutes (int):");
        avgMinutesLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        avgSessionMinutesField = stdTextField();
        avgSessionMinutesField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel freqLbl = stdLabel("Suggested Sessions/Week (int):");
        freqLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        freqPerWeekField = stdTextField();
        freqPerWeekField.setAlignmentX(Component.LEFT_ALIGNMENT);

        selfPanel.add(selfHeader);
        selfPanel.add(avgMinutesLbl);
        selfPanel.add(avgSessionMinutesField);
        selfPanel.add(freqLbl);
        selfPanel.add(freqPerWeekField);

        // ===== class sub-panel =====
        classPanel = new JPanel();
        classPanel.setOpaque(false);
        classPanel.setLayout(new BoxLayout(classPanel, BoxLayout.Y_AXIS));
        classPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel classHeader = stdSectionLabel("Class Details");
        classHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel startLbl = stdLabel("Start Date (YYYY-MM-DD):");
        startLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        classStartDateField = stdTextField();
        classStartDateField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel endLbl = stdLabel("End Date (YYYY-MM-DD):");
        endLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        classEndDateField = stdTextField();
        classEndDateField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel daysLbl = stdLabel("Days of Week (e.g. MON,WED):");
        daysLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        classDaysOfWeekField = stdTextField();
        classDaysOfWeekField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel classMinutesLbl = stdLabel("Session Minutes (int):");
        classMinutesLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        classSessionMinutesField = stdTextField();
        classSessionMinutesField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel weeksLbl = stdLabel("Number of Weeks (int):");
        weeksLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        classNumWeeksField = stdTextField();
        classNumWeeksField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel maxPartLbl = stdLabel("Max Participants (int):");
        maxPartLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        maxParticipantsField = stdTextField();
        maxParticipantsField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel regEndLbl = stdLabel("<html>Registration End Date<br>(YYYY-MM-DD):</html>");
        regEndLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        registrationEndDateField = stdTextField();
        registrationEndDateField.setAlignmentX(Component.LEFT_ALIGNMENT);

        classPanel.add(classHeader);
        classPanel.add(startLbl);
        classPanel.add(classStartDateField);
        classPanel.add(endLbl);
        classPanel.add(classEndDateField);
        classPanel.add(daysLbl);
        classPanel.add(classDaysOfWeekField);
        classPanel.add(classMinutesLbl);
        classPanel.add(classSessionMinutesField);
        classPanel.add(weeksLbl);
        classPanel.add(classNumWeeksField);
        classPanel.add(maxPartLbl);
        classPanel.add(maxParticipantsField);
        classPanel.add(regEndLbl);
        classPanel.add(registrationEndDateField);

        // Buttons
        JButton saveBtn = stdGreenButton("Save Program");
        JButton clearBtn = stdOutlinedButton("Clear Form", defaultSettings.BORDER_COLOR);

        saveBtn.addActionListener(e -> saveProgram());
        clearBtn.addActionListener(e -> clearForm());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(saveBtn);
        btnRow.add(clearBtn);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Layout with BoxLayout (top → bottom)
        form.add(typeLbl);
        form.add(typeCombo);
        form.add(Box.createVerticalStrut(4));

        form.add(nameLbl);
        form.add(nameField);
        form.add(Box.createVerticalStrut(4));

        form.add(descLbl);
        form.add(descScroll);
        form.add(Box.createVerticalStrut(4));

        form.add(fitnessLbl);
        form.add(fitnessCombo);
        form.add(Box.createVerticalStrut(4));

        form.add(equipLbl);
        form.add(equipmentField);
        form.add(Box.createVerticalStrut(8));

        form.add(selfPanel);
        form.add(Box.createVerticalStrut(8));
        form.add(classPanel);
        form.add(Box.createVerticalStrut(10));
        form.add(btnRow);

        return form;
    }

    private void updateProgramTypePanels() {
        String type = (String) typeCombo.getSelectedItem();
        boolean isClass = "CLASS".equalsIgnoreCase(type);

        if (selfPanel != null && classPanel != null) {
            selfPanel.setVisible(!isClass);
            classPanel.setVisible(isClass);
            selfPanel.revalidate();
            classPanel.revalidate();
            selfPanel.repaint();
            classPanel.repaint();
        }
    }

    // ========= RIGHT TABLE =========
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(defaultSettings.BACKGROUND_COLOR);

        programsModel = new DefaultTableModel(
                new Object[]{
                        "ID", "Type", "Name", "Fitness",
                        "Start Date", "End Date", "Max Participants"
                }, 0
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

        // allow column reordering + auto-resize all columns
        programsTable.getTableHeader().setReorderingAllowed(true);
        programsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(programsTable);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // dark scrollbars for table
        JScrollBar vTableBar = scroll.getVerticalScrollBar();
        vTableBar.setBackground(defaultSettings.BACKGROUND_COLOR);
        vTableBar.setForeground(defaultSettings.TEXT_COLOR);
        JScrollBar hTableBar = scroll.getHorizontalScrollBar();
        hTableBar.setBackground(defaultSettings.BACKGROUND_COLOR);
        hTableBar.setForeground(defaultSettings.TEXT_COLOR);

        programsTable.getTableHeader().setBackground(defaultSettings.BACKGROUND_COLOR);
        programsTable.getTableHeader().setForeground(defaultSettings.TEXT_COLOR);

        panel.add(scroll, BorderLayout.CENTER);

        // double-click → load into form for editing
        programsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (programsTable.getSelectedRow() >= 0 && e.getClickCount() == 2) {
                    loadProgramIntoForm(programsTable.getSelectedRow());
                }
            }
        });

        // bottom buttons: Refresh + Delete Selected
        JButton refreshBtn = stdOutlinedButton("Refresh", defaultSettings.BORDER_COLOR);
        JButton deleteBtn = stdOutlinedButton("Delete Selected", new Color(200, 40, 40));

        refreshBtn.addActionListener(e -> loadProgramsFromDb());
        deleteBtn.addActionListener(e -> deleteSelectedProgram());

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        bottomRow.setOpaque(false);
        bottomRow.add(refreshBtn);
        bottomRow.add(deleteBtn);

        panel.add(bottomRow, BorderLayout.SOUTH);

        return panel;
    }

    // ========= STYLE HELPERS =========
    private JLabel stdLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(defaultSettings.TEXT_COLOR);
        l.setFont(defaultSettings.LABEL_FONT);
        return l;
    }

    private JLabel stdSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(defaultSettings.TEXT_COLOR);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setBorder(new EmptyBorder(8, 0, 4, 0));
        return l;
    }

    private JTextField stdTextField() {
        JTextField f = new JTextField();
        Dimension d = new Dimension(220, 26);
        f.setPreferredSize(d);
        f.setMaximumSize(d);
        f.setForeground(defaultSettings.TEXT_COLOR);
        f.setBackground(defaultSettings.BACKGROUND_COLOR);
        f.setCaretColor(defaultSettings.TEXT_COLOR);
        f.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        return f;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setForeground(defaultSettings.TEXT_COLOR);
        combo.setBackground(defaultSettings.BACKGROUND_COLOR);
        combo.setFont(defaultSettings.LABEL_FONT);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel l = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                l.setForeground(defaultSettings.TEXT_COLOR);
                l.setBackground(isSelected ? Color.DARK_GRAY : defaultSettings.BACKGROUND_COLOR);
                return l;
            }
        });
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

    // ========= SMALL PARSE HELPERS =========
    private Integer parseOptionalInt(JTextField field, String label, boolean required) {
        String txt = field.getText().trim();
        if (txt.isEmpty()) {
            if (required) {
                JOptionPane.showMessageDialog(this, label + " is required.");
                return null;
            }
            return null;
        }
        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, label + " must be an integer.");
            return null;
        }
    }

    private java.sql.Date parseOptionalDate(JTextField field, String label, boolean required) {
        String txt = field.getText().trim();
        if (txt.isEmpty()) {
            if (required) {
                JOptionPane.showMessageDialog(this, label + " is required.");
                return null;
            }
            return null;
        }
        try {
            LocalDate d = LocalDate.parse(txt); // expects YYYY-MM-DD
            return java.sql.Date.valueOf(d);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, label + " must be YYYY-MM-DD.");
            return null;
        }
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.INTEGER);
        } else {
            ps.setInt(idx, value);
        }
    }

    private void setNullableDate(PreparedStatement ps, int idx, java.sql.Date value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.DATE);
        } else {
            ps.setDate(idx, value);
        }
    }

    // ========= DB / LOGIC =========
    private void loadProgramsFromDb() {
        programsModel.setRowCount(0);
        selectedProgramId = null;

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

    private void saveProgram() {
        String type = (String) typeCombo.getSelectedItem();
        String name = nameField.getText().trim();
        String description = descArea.getText().trim();
        String fitness = (String) fitnessCombo.getSelectedItem();
        String equipment = equipmentField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a name.");
            return;
        }

        boolean isClass = "CLASS".equalsIgnoreCase(type);

        // self-paced fields
        Integer avgMinutes = null;
        Integer freqPerWeek = null;

        // class fields
        java.sql.Date startDate = null;
        java.sql.Date endDate = null;
        String daysOfWeek = null;
        Integer sessionMinutes = null;
        Integer numWeeks = null;
        Integer maxParticipants = null;
        java.sql.Date regEndDate = null;

        if (!isClass) {
            // SELF_PACED
            avgMinutes = parseOptionalInt(avgSessionMinutesField,
                    "Avg Session Minutes", false);
            if (avgMinutes == null && !avgSessionMinutesField.getText().trim().isEmpty())
                return;

            freqPerWeek = parseOptionalInt(freqPerWeekField,
                    "Suggested Sessions/Week", false);
            if (freqPerWeek == null && !freqPerWeekField.getText().trim().isEmpty())
                return;
        } else {
            // CLASS
            startDate = parseOptionalDate(classStartDateField,
                    "Start Date", true);
            if (startDate == null) return;

            endDate = parseOptionalDate(classEndDateField,
                    "End Date", true);
            if (endDate == null) return;

            daysOfWeek = classDaysOfWeekField.getText().trim();
            if (daysOfWeek.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Days of Week is required for a class.");
                return;
            }

            sessionMinutes = parseOptionalInt(classSessionMinutesField,
                    "Session Minutes", true);
            if (sessionMinutes == null) return;

            numWeeks = parseOptionalInt(classNumWeeksField,
                    "Number of Weeks", true);
            if (numWeeks == null) return;

            maxParticipants = parseOptionalInt(maxParticipantsField,
                    "Max Participants", true);
            if (maxParticipants == null) return;

            regEndDate = parseOptionalDate(registrationEndDateField,
                    "Registration End Date", true);
            if (regEndDate == null) return;
        }

        try (Connection conn = MySQLDatabaseConnector.getConnection()) {
            if (selectedProgramId == null) {
                // INSERT
                String sql = "INSERT INTO exercise_program (" +
                        "trainer_email, program_type, name, description, fitness_level, required_equipment, " +
                        "avg_session_minutes, suggested_freq_per_week, " +
                        "class_start_date, class_end_date, class_days_of_week, class_session_minutes, " +
                        "class_num_weeks, max_participants, registration_end_date, is_active" +
                        ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int i = 1;
                    ps.setString(i++, currentUser.getEmail());
                    ps.setString(i++, type);
                    ps.setString(i++, name);
                    ps.setString(i++, description);
                    ps.setString(i++, fitness);
                    ps.setString(i++, equipment);

                    setNullableInt(ps, i++, avgMinutes);
                    setNullableInt(ps, i++, freqPerWeek);

                    setNullableDate(ps, i++, startDate);
                    setNullableDate(ps, i++, endDate);
                    ps.setString(i++, daysOfWeek);
                    setNullableInt(ps, i++, sessionMinutes);
                    setNullableInt(ps, i++, numWeeks);
                    setNullableInt(ps, i++, maxParticipants);
                    setNullableDate(ps, i++, regEndDate);

                    ps.executeUpdate();
                }
            } else {
                // UPDATE
                String sql = "UPDATE exercise_program SET " +
                        "program_type=?, name=?, description=?, fitness_level=?, required_equipment=?, " +
                        "avg_session_minutes=?, suggested_freq_per_week=?, " +
                        "class_start_date=?, class_end_date=?, class_days_of_week=?, class_session_minutes=?, " +
                        "class_num_weeks=?, max_participants=?, registration_end_date=? " +
                        "WHERE program_id=? AND trainer_email=?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int i = 1;
                    ps.setString(i++, type);
                    ps.setString(i++, name);
                    ps.setString(i++, description);
                    ps.setString(i++, fitness);
                    ps.setString(i++, equipment);

                    setNullableInt(ps, i++, avgMinutes);
                    setNullableInt(ps, i++, freqPerWeek);

                    setNullableDate(ps, i++, startDate);
                    setNullableDate(ps, i++, endDate);
                    ps.setString(i++, daysOfWeek);
                    setNullableInt(ps, i++, sessionMinutes);
                    setNullableInt(ps, i++, numWeeks);
                    setNullableInt(ps, i++, maxParticipants);
                    setNullableDate(ps, i++, regEndDate);

                    ps.setInt(i++, selectedProgramId);
                    ps.setString(i, currentUser.getEmail());

                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Program saved.");
            loadProgramsFromDb();
            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving program:\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProgram() {
        int row = programsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a program to delete.");
            return;
        }
        Object idObj = programsModel.getValueAt(row, 0);
        int programId = Integer.parseInt(idObj.toString());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete program ID " + programId + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM exercise_program WHERE program_id=? AND trainer_email=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);
            ps.setString(2, currentUser.getEmail());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Program deleted.");
            loadProgramsFromDb();
            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting program:\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        selectedProgramId = null;
        typeCombo.setSelectedItem("SELF_PACED");
        updateProgramTypePanels();

        nameField.setText("");
        descArea.setText("");
        fitnessCombo.setSelectedItem("BEGINNER");
        equipmentField.setText("");
        avgSessionMinutesField.setText("");
        freqPerWeekField.setText("");
        classStartDateField.setText("");
        classEndDateField.setText("");
        classDaysOfWeekField.setText("");
        classSessionMinutesField.setText("");
        classNumWeeksField.setText("");
        maxParticipantsField.setText("");
        registrationEndDateField.setText("");
    }

    private void loadProgramIntoForm(int tableRow) {
        Object idObj = programsModel.getValueAt(tableRow, 0);
        if (idObj == null) return;

        int programId = Integer.parseInt(idObj.toString());
        selectedProgramId = programId;

        String sql = "SELECT * FROM exercise_program WHERE program_id=? AND trainer_email=?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);
            ps.setString(2, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("program_type");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    String fitness = rs.getString("fitness_level");
                    String equipment = rs.getString("required_equipment");

                    Integer avgMinutes = (Integer) rs.getObject("avg_session_minutes");
                    Integer freqPerWeek = (Integer) rs.getObject("suggested_freq_per_week");

                    Date start = rs.getDate("class_start_date");
                    Date end = rs.getDate("class_end_date");
                    String days = rs.getString("class_days_of_week");
                    Integer sessionMinutes = (Integer) rs.getObject("class_session_minutes");
                    Integer numWeeks = (Integer) rs.getObject("class_num_weeks");
                    Integer maxPart = (Integer) rs.getObject("max_participants");
                    Date regEnd = rs.getDate("registration_end_date");

                    typeCombo.setSelectedItem(type);
                    updateProgramTypePanels();

                    nameField.setText(name != null ? name : "");
                    descArea.setText(description != null ? description : "");
                    fitnessCombo.setSelectedItem(fitness != null ? fitness : "BEGINNER");
                    equipmentField.setText(equipment != null ? equipment : "");

                    avgSessionMinutesField.setText(avgMinutes != null ? avgMinutes.toString() : "");
                    freqPerWeekField.setText(freqPerWeek != null ? freqPerWeek.toString() : "");

                    classStartDateField.setText(start != null ? start.toString() : "");
                    classEndDateField.setText(end != null ? end.toString() : "");
                    classDaysOfWeekField.setText(days != null ? days : "");
                    classSessionMinutesField.setText(sessionMinutes != null ? sessionMinutes.toString() : "");
                    classNumWeeksField.setText(numWeeks != null ? numWeeks.toString() : "");
                    maxParticipantsField.setText(maxPart != null ? maxPart.toString() : "");
                    registrationEndDateField.setText(regEnd != null ? regEnd.toString() : "");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading program details:\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
