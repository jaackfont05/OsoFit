import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Main landing page after login.
 * Shows greeting + summary stats (calorie intake, weight, sleep).
 */
public class mainPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;

    // Labels that will show the stats
    private JLabel caloriesValueLbl;
    private JLabel weightValueLbl;
    private JLabel sleepValueLbl;
    private JLabel calorieGoalLbl;
    private JLabel weightGoalLbl;
    private JLabel sleepGoalLbl;

    private int calorieGoal = 0;
    private int weightGoal = 0;
    private int stepGoal = 0;

    public mainPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

        getGoals();
        // Apply shared defaults (size, bg, close operation, etc.)
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Main Page");
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

        JLabel title = new JLabel("Main Page", SwingConstants.CENTER);
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


        String displayName = (currentUser != null
                && currentUser.getUserN() != null
                && !currentUser.getUserN().isBlank())
                ? currentUser.getUserN()
                : "User";

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(defaultSettings.BACKGROUND_COLOR);


        JLabel welcome = new JLabel("Welcome, " + displayName + "!", SwingConstants.CENTER);
        welcome.setForeground(defaultSettings.TEXT_COLOR);
        welcome.setFont(new Font("Serif", Font.BOLD, 36));
        welcome.setBorder(new EmptyBorder(40, 0, 20, 0));
        center.add(welcome, BorderLayout.NORTH);

       //HIDE STATS FOR TRAINERS
        if (currentUser != null && "trainer".equalsIgnoreCase(currentUser.getRole())) {
            // Trainer: just show the welcome banner, no stats cards
            add(center, BorderLayout.CENTER);
            return; // stop here, do NOT build the 3 columns
        }

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 30, 0));

        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(10, 80, 40, 80));

        // calorie intake panel
        JPanel caloriesCard = createStatCard("Calorie intake today");
        caloriesValueLbl = createStatValueLabel("0");
        caloriesCard.add(caloriesValueLbl, BorderLayout.CENTER);
        statsRow.add(caloriesCard);

        JLabel cGoal = new JLabel("Calorie Goal: " + String.valueOf(calorieGoal), SwingConstants.CENTER);
        cGoal.setForeground(defaultSettings.TEXT_COLOR);
        cGoal.setFont(new Font("Serif", Font.BOLD, 12));
        cGoal.setBorder(new EmptyBorder(10, 80, 40, 80));
        caloriesCard.add(cGoal, BorderLayout.SOUTH);


        //weight panel
        JPanel weightCard = createStatCard("Current weight (lbs)");
        weightValueLbl = createStatValueLabel("--");
        weightCard.add(weightValueLbl, BorderLayout.CENTER);
        statsRow.add(weightCard);

        JLabel wGoal = new JLabel("Weight Goal: " + String.valueOf(weightGoal), SwingConstants.CENTER);
        wGoal.setForeground(defaultSettings.TEXT_COLOR);
        wGoal.setFont(new Font("Serif", Font.BOLD, 12));
        wGoal.setBorder(new EmptyBorder(40, 0, 20, 0));
        weightCard.add(wGoal, BorderLayout.SOUTH);

        //sleep panel
        JPanel sleepCard = createStatCard("Last night's sleep (hrs)");
        sleepValueLbl = createStatValueLabel("--");
        sleepCard.add(sleepValueLbl, BorderLayout.CENTER);
        statsRow.add(sleepCard);

        center.add(statsRow, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);


        loadLatestStatsForUser();


    }



    private JPanel createStatCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setForeground(defaultSettings.TEXT_COLOR);
        titleLbl.setFont(new Font("Serif", Font.PLAIN, 18));
        titleLbl.setBorder(new EmptyBorder(10, 10, 5, 10));

        card.add(titleLbl, BorderLayout.NORTH);
        return card;
    }

    private JLabel createStatValueLabel(String initialText) {
        JLabel lbl = new JLabel(initialText, SwingConstants.CENTER);
        lbl.setForeground(defaultSettings.TEXT_COLOR);
        lbl.setFont(new Font("Serif", Font.BOLD, 28));
        lbl.setBorder(new EmptyBorder(10, 10, 10, 10));
        return lbl;
    }

    private void loadLatestStatsForUser() {
        // Default values if nothing in DB
        int caloriesIn = 0;
        double weight = 0.0;
        int sleepHours = 0;
        boolean foundRow = false;

        if (currentUser == null || currentUser.getEmail() == null) {
            // No logged in user
            updateStatsLabels(caloriesIn, weight, sleepHours, false);
            return;
        }

        String mealsSql = "SELECT SUM(calories) AS total_calories, COUNT(*) AS meal_count " +
                "FROM Meals " +
                "WHERE email = ? AND mealDate = (SELECT MAX(mealDate) FROM Meals WHERE email = ?)";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement psMeals = conn.prepareStatement(mealsSql)) {

            psMeals.setString(1, currentUser.getEmail());
            psMeals.setString(2, currentUser.getEmail());

            try (ResultSet rsMeals = psMeals.executeQuery()) {
                if (rsMeals.next()) {
                    int mealCount = rsMeals.getInt("meal_count");
                    if (mealCount > 0) {
                        caloriesIn = rsMeals.getInt("total_calories");
                       foundRow = true;
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }


        String statsSql = "SELECT weight_pounds " +
                "FROM Stats " +
                "WHERE email = ? " +
                "ORDER BY date_time DESC " +
                "LIMIT 1";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement psStats = conn.prepareStatement(statsSql)) {

            psStats.setString(1, currentUser.getEmail());

            try (ResultSet rsStats = psStats.executeQuery()) {
                if (rsStats.next()) {
                    weight = rsStats.getDouble("weight_pounds");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String sleepSql = "SELECT hours FROM Sleep WHERE email = ? AND date = (SELECT MAX(date) FROM Sleep WHERE email = ?)";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement psStats = conn.prepareStatement(sleepSql)) {

            psStats.setString(1, currentUser.getEmail());
            psStats.setString(2, currentUser.getEmail());

            try (ResultSet rsStats = psStats.executeQuery()) {
                if (rsStats.next()) {
                    sleepHours = rsStats.getInt("hours");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        updateStatsLabels(caloriesIn, weight, sleepHours, foundRow);
    }

    // pushes values into labels
    private void updateStatsLabels(int caloriesIn, double weight, int sleepHours, boolean hasData) {
        if (!hasData) {
            if (caloriesValueLbl != null) caloriesValueLbl.setText("—");
            if (weightValueLbl != null)    weightValueLbl.setText("—");
            if (sleepValueLbl != null)     sleepValueLbl.setText("—");
            return;
        }

        if (caloriesValueLbl != null) {
            caloriesValueLbl.setText(String.valueOf(caloriesIn));
        }
        if (weightValueLbl != null) {
            // One decimal place for weight
            weightValueLbl.setText(String.format("%.1f", weight));
        }
        if (sleepValueLbl != null) {
            // One decimal place for sleep hours
            sleepValueLbl.setText(String.valueOf(sleepHours));
        }
    }

    private void getGoals(){

        String goalsSql = "SELECT goalWeight, steps, calories FROM Goals WHERE email = ?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement psMeals = conn.prepareStatement(goalsSql)) {

            psMeals.setString(1, currentUser.getEmail());

            try (ResultSet rsMeals = psMeals.executeQuery()) {
                if (rsMeals.next()) {
                   weightGoal = rsMeals.getInt("goalWeight");
                   stepGoal = rsMeals.getInt("steps");
                   calorieGoal = rsMeals.getInt("calories");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
