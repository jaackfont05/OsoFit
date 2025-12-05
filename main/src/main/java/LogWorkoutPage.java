import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogWorkoutPage extends JFrame {

    private user currentUser;
    private MySQLDatabaseConnector db;

    private List<Workout> workoutList = new ArrayList<>();
    private Map<Integer, Integer> workoutIdToExerciseId = new HashMap<>();

    private JComboBox<Workout> workoutCB;

    public LogWorkoutPage(user u, MySQLDatabaseConnector db) {
        this.currentUser = u;
        this.db = db;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Log Workout Page");
        setLayout(new BorderLayout(10, 10));


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


        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel selectLabel = new JLabel("Select a workout to log again:", SwingConstants.CENTER);
        selectLabel.setForeground(defaultSettings.TEXT_COLOR);
        selectLabel.setFont(new Font(defaultSettings.TITLE_FONT.getFontName(), Font.PLAIN, 18));
        centerPanel.add(selectLabel, gbc);

        workoutCB = new JComboBox<>();
        workoutCB.setFont(new Font("Serif", Font.PLAIN, 16));
        workoutCB.setForeground(defaultSettings.TEXT_COLOR);
        workoutCB.setBackground(Color.WHITE);
        workoutCB.setPreferredSize(new Dimension(400, 40));
        centerPanel.add(workoutCB, gbc);

        JButton logBtn = new JButton("Log Selected Workout");
        logBtn.setFont(defaultSettings.LABEL_FONT);
        logBtn.setBackground(defaultSettings.BACKGROUND_COLOR);
        logBtn.setForeground(defaultSettings.TEXT_COLOR);
        centerPanel.add(logBtn, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // hook button
        logBtn.addActionListener(e -> logSelectedWorkout());

        // load workouts from DB
        loadWorkoutsForUser();
    }

    //oad workouts
    private void loadWorkoutsForUser() {
        workoutList.clear();
        workoutIdToExerciseId.clear();
        workoutCB.removeAllItems();

       //updating based on requirements from db
        String sql =
                "SELECT w.workoutID, w.email, w.exerciseID, w.time_current, w.duration, w.calories, " +
                        "       w.finish, e.name AS exerciseName " +
                        "FROM Workout w " +
                        "LEFT JOIN Exercises e ON w.exerciseID = e.exerciseID " +
                        "WHERE w.email = ? " +
                        "ORDER BY w.time_current DESC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int workoutId   = rs.getInt("workoutID");
                    String email    = rs.getString("email");
                    int exerciseId  = rs.getInt("exerciseID");
                    int duration    = rs.getInt("duration");
                    int calories    = rs.getInt("calories");
                    Timestamp ts    = rs.getTimestamp("time_current");
                    LocalDateTime dt = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

                    String exerciseName = rs.getString("exerciseName");
                    if (exerciseName == null || exerciseName.isBlank()) {
                        exerciseName = "Exercise " + exerciseId;
                    }


                    Workout w = new Workout(
                            workoutId,
                            email,
                            exerciseName,
                            duration,
                            calories,
                            dt,
                            new ArrayList<>()
                    );

                    workoutList.add(w);
                    workoutIdToExerciseId.put(workoutId, exerciseId);
                    workoutCB.addItem(w);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading workouts from database.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void logSelectedWorkout() {
        Workout selected = (Workout) workoutCB.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a workout first.",
                    "No Workout Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Look up the exerciseID for this workout from map
        int exerciseId = workoutIdToExerciseId.getOrDefault(selected.getWorkoutId(), 0);
        if (exerciseId == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Exercise ID not found for selected workout.",
                    "Data Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTs = Timestamp.valueOf(now);


        String insertSql =
                "INSERT INTO Workout (email, exerciseID, finish, time_current, duration, calories) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setString(1, currentUser.getEmail());
            ps.setInt(2, exerciseId);
            ps.setTimestamp(3, nowTs);                     // finish
            ps.setTimestamp(4, nowTs);                     // time_current
            ps.setInt(5, selected.getDurationMin());       // duration
            ps.setInt(6, selected.getCalories());          // calories

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Workout logged successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                // refresh combo so newest appears at top
                loadWorkoutsForUser();
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
                    "Error logging workout.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
