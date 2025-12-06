import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class CreateWorkoutPage extends JFrame {

    private final MySQLDatabaseConnector db;
    private final user u;

    // Combo-box item: we only need exerciseID + name
    private static class ExerciseOption {
        final int id;
        final String name;

        ExerciseOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            // What shows in the combo box
            return name + " (ID: " + id + ")";
        }
    }

    private final JComboBox<ExerciseOption> exerciseCB;
    private final JTextField durationField;
    private final JTextField caloriesField;

    public CreateWorkoutPage(user u, MySQLDatabaseConnector db) {
        this.u = u;
        this.db = db;

        // ===== window defaults =====
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Create Workout Page");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        // (N) menu bar
        menuBar bar = new menuBar(this, u, db);
        topPanel.add(bar, BorderLayout.NORTH);

        // (C) title
        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Create A Workout", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        // (S) red underline
        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: form =====
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        add(center, BorderLayout.CENTER);

        // narrow column, like other pages
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        center.add(column, gbc);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints fg = new GridBagConstraints();
        fg.insets = new Insets(0, 12, 8, 12);
        fg.gridy = 0;

        // === Row 1: exercise combo ===
        fg.gridx = 0;
        fg.anchor = GridBagConstraints.EAST;
        JLabel exerciseLbl = stdLabel("Select exercise:");
        form.add(exerciseLbl, fg);

        fg.gridx = 1;
        fg.anchor = GridBagConstraints.WEST;
        exerciseCB = new JComboBox<>();
        exerciseCB.setPreferredSize(new Dimension(360, 34));
        exerciseCB.setForeground(defaultSettings.TEXT_COLOR);
        exerciseCB.setBackground(Color.WHITE);
        exerciseCB.setFont(defaultSettings.LABEL_FONT);
        exerciseCB.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        form.add(exerciseCB, fg);

        // === Row 2: duration ===
        fg.gridy++;
        fg.gridx = 0;
        fg.anchor = GridBagConstraints.EAST;
        JLabel durationLbl = stdLabel("Duration (minutes):");
        form.add(durationLbl, fg);

        fg.gridx = 1;
        fg.anchor = GridBagConstraints.WEST;
        durationField = stdTextField(360);
        form.add(durationField, fg);

        // === Row 3: calories ===
        fg.gridy++;
        fg.gridx = 0;
        fg.anchor = GridBagConstraints.EAST;
        JLabel caloriesLbl = stdLabel("Calories burned:");
        form.add(caloriesLbl, fg);

        fg.gridx = 1;
        fg.anchor = GridBagConstraints.WEST;
        caloriesField = stdTextField(360);
        form.add(caloriesField, fg);

        // === Row 4: button ===
        fg.gridy++;
        fg.gridx = 0;
        fg.gridwidth = 2;
        fg.anchor = GridBagConstraints.CENTER;
        JButton createBtn = new JButton("Create Workout");
        createBtn.setOpaque(false);
        createBtn.setBackground(defaultSettings.BACKGROUND_COLOR);
        createBtn.setForeground(defaultSettings.TEXT_COLOR);
        createBtn.setFont(defaultSettings.LABEL_FONT);
        createBtn.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        form.add(createBtn, fg);

        column.add(form);
        column.add(Box.createVerticalStrut(16));

        // hook up button
        createBtn.addActionListener(e -> saveWorkout());

        // load exercises for this user
        loadExercisesForUser();
    }

    // ===== UI helpers =====
    private static JLabel stdLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(defaultSettings.TEXT_COLOR);
        l.setFont(defaultSettings.LABEL_FONT);
        return l;
    }

    private static JTextField stdTextField(int width) {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(width, 34));
        f.setForeground(defaultSettings.TEXT_COLOR);
        f.setBackground(defaultSettings.BACKGROUND_COLOR);
        f.setCaretColor(defaultSettings.TEXT_COLOR);
        f.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        return f;
    }

    // ===== Load user's exercises into combo box =====
    private void loadExercisesForUser() {
        exerciseCB.removeAllItems();

        String sql = "SELECT exerciseID, name FROM Exercises WHERE email = ?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("exerciseID");
                    String name = rs.getString("name");

                    ExerciseOption opt = new ExerciseOption(id, name);
                    exerciseCB.addItem(opt);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading exercises from database:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ===== Helper to get next workoutID for this user =====
    private int getNextWorkoutIdForUser(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(workoutID), 0) + 1 AS nextId FROM Workout WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("nextId");
                }
            }
        }
        return 1;
    }

    // ===== Save workout row into Workout table =====
    private void saveWorkout() {
        ExerciseOption selected = (ExerciseOption) exerciseCB.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an exercise.",
                    "Missing Exercise",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String durationText = durationField.getText().trim();
        String caloriesText = caloriesField.getText().trim();

        if (durationText.isEmpty() || caloriesText.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both duration and calories.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int duration;
        int calories;
        try {
            duration = Integer.parseInt(durationText);
            calories = Integer.parseInt(caloriesText);
            if (duration <= 0 || calories < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Duration must be > 0 and calories must be a non-negative number.",
                    "Invalid Number",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // === IMPORTANT PART: write a DATE, not a time string ===
        LocalDate today = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(today);  // "YYYY-MM-DD"

        String insertSql =
                "INSERT INTO Workout (email, workoutID, exerciseID, finish, time_current, duration, calories) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            int newWorkoutId = getNextWorkoutIdForUser(conn);

            ps.setString(1, u.getEmail());
            ps.setInt(2, newWorkoutId);     // workoutID
            ps.setInt(3, selected.id);      // exerciseID
            ps.setDate(4, sqlDate);         // finish (DATE)
            ps.setDate(5, sqlDate);         // time_current (DATE)
            ps.setInt(6, duration);         // duration
            ps.setInt(7, calories);         // calories

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Workout created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                durationField.setText("");
                caloriesField.setText("");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Workout could not be created.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "SQL error while writing workout:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

