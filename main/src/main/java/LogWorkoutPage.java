import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogWorkoutPage extends JFrame {

    private user currentUser;
    private MySQLDatabaseConnector db;
    private List<Workout> workoutList = new ArrayList<>();
    private JComboBox<Workout> workoutCB;

    public LogWorkoutPage(user u, MySQLDatabaseConnector db) {
        this.currentUser = u;
        this.db = db;

        // Apply shared defaults
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

    // ===== load workouts from db for this user and populate the box =====
    private void loadWorkoutsForUser() {
        workoutList.clear();
        workoutCB.removeAllItems();

        String sql = "SELECT workoutId, email, type, durationMin, calories, dateTime " +
                "FROM workouts WHERE email = ? ORDER BY dateTime DESC";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int workoutId = rs.getInt("workoutId");
                    String email = rs.getString("email");
                    String type = rs.getString("type");
                    int durationMin = rs.getInt("durationMin");
                    int calories = rs.getInt("calories");
                    Timestamp ts = rs.getTimestamp("dateTime");
                    LocalDateTime dt = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

                    Workout w = new Workout(
                            workoutId,
                            email,
                            type,
                            durationMin,
                            calories,
                            dt,
                            new ArrayList<>()   // no exercises loaded here
                    );

                    workoutList.add(w);
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

    // ===== called when user clicks "Log Selected Workout" =====
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

        // Create a NEW workout entry based on the selected one (copy type/duration/calories)
        LocalDateTime now = LocalDateTime.now();

        String insertSql = "INSERT INTO workouts (email, type, durationMin, calories, dateTime) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setString(1, currentUser.getEmail());
            ps.setString(2, selected.getType());
            ps.setInt(3, selected.getDurationMin());
            ps.setInt(4, selected.getCalories());
            ps.setTimestamp(5, Timestamp.valueOf(now));

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Workout logged successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                // optional: refresh combo so newest appears at top
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
