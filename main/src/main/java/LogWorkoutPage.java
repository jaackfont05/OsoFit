import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LogWorkoutPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;

    // Local model for items in the combo box
    private static class WorkoutOption {
        final int workoutId;
        final int exerciseId;
        final int duration;
        final int calories;
        final String exerciseName;

        WorkoutOption(int workoutId, int exerciseId, int duration, int calories, String exerciseName) {
            this.workoutId = workoutId;
            this.exerciseId = exerciseId;
            this.duration = duration;
            this.calories = calories;
            this.exerciseName = exerciseName;
        }

        @Override
        public String toString() {
            return exerciseName + " — " + duration + " min, " + calories + " cal";
        }
    }

    private final List<WorkoutOption> workoutList = new ArrayList<>();
    private final JComboBox<WorkoutOption> workoutCB;

    public LogWorkoutPage(user u, MySQLDatabaseConnector db) {
        this.currentUser = u;
        this.db = db;

        // Window defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Log Workout Page");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        // (N) menu bar
        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        // (C) title
        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Log A Workout", SwingConstants.CENTER);
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

        // ===== CENTER: combo + button =====
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel selectLabel = new JLabel("Select a workout:", SwingConstants.CENTER);
        selectLabel.setForeground(defaultSettings.TEXT_COLOR);
        selectLabel.setFont(new Font(defaultSettings.TITLE_FONT.getFontName(), Font.PLAIN, 18));
        centerPanel.add(selectLabel, gbc);

        workoutCB = new JComboBox<>();
        workoutCB.setFont(new Font("Serif", Font.PLAIN, 16));
        workoutCB.setForeground(defaultSettings.TEXT_COLOR);
        workoutCB.setBackground(Color.WHITE);
        workoutCB.setPreferredSize(new Dimension(400, 40));
        workoutCB.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        centerPanel.add(workoutCB, gbc);

        JButton logBtn = new JButton("Log Selected Workout");
        logBtn.setForeground(defaultSettings.TEXT_COLOR);
        logBtn.setBackground(defaultSettings.BACKGROUND_COLOR);
        logBtn.setFont(defaultSettings.LABEL_FONT);
        logBtn.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        centerPanel.add(logBtn, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Button action
        logBtn.addActionListener(e -> logSelectedWorkout());

        // Load workouts for this user
        loadWorkoutsForUser();
    }


    private static JLabel stdLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(defaultSettings.TEXT_COLOR);
        l.setFont(defaultSettings.LABEL_FONT);
        return l;
    }


    private void loadWorkoutsForUser() {
        workoutList.clear();
        workoutCB.removeAllItems();


        String sql =
                "SELECT w.workoutID, w.exerciseID, w.duration, w.calories, e.name " +
                        "FROM Workout w " +
                        "JOIN Exercises e ON w.exerciseID = e.exerciseID " +
                        "WHERE w.email = ? " +
                        "ORDER BY w.workoutID DESC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int workoutId = rs.getInt("workoutID");
                    int exerciseId = rs.getInt("exerciseID");
                    int duration = rs.getInt("duration");
                    int calories = rs.getInt("calories");
                    String name = rs.getString("name");

                    WorkoutOption opt =
                            new WorkoutOption(workoutId, exerciseId, duration, calories, name);
                    workoutList.add(opt);
                    workoutCB.addItem(opt);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading workouts:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Get next workoutID for this user (same pattern as CreateWorkoutPage)
    private int getNextWorkoutIdForUser(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(workoutID), 0) + 1 AS nextId FROM Workout WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentUser.getEmail());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("nextId");
                }
            }
        }
        return 1;
    }


    private void logSelectedWorkout() {
        WorkoutOption selected = (WorkoutOption) workoutCB.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a workout first.",
                    "No Workout Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Today as DATE, like in CreateWorkoutPage
        LocalDate today = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(today);

        String insertSql =
                "INSERT INTO Workout (email, workoutID, exerciseID, finish, time_current, duration, calories) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            int newWorkoutId = getNextWorkoutIdForUser(conn);

            ps.setString(1, currentUser.getEmail());
            ps.setInt(2, newWorkoutId);
            ps.setInt(3, selected.exerciseId);
            ps.setDate(4, sqlDate);                 // finish (DATE)
            ps.setDate(5, sqlDate);                 // time_current (DATE)
            ps.setInt(6, selected.duration);
            ps.setInt(7, selected.calories);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Workout logged successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Workout could not be logged.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error logging workout:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
