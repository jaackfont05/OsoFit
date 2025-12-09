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

    public mainPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

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

        //weight panel
        JPanel weightCard = createStatCard("Current weight (lbs)");
        weightValueLbl = createStatValueLabel("--");
        weightCard.add(weightValueLbl, BorderLayout.CENTER);
        statsRow.add(weightCard);

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
        double sleepHours = 0.0;
        boolean foundRow = false;

        if (currentUser == null || currentUser.getEmail() == null) {
            // No logged in user
            updateStatsLabels(caloriesIn, weight, sleepHours, false);
            return;
        }

        String sql = "SELECT calories_in, weight_pounds " +
                "FROM Stats " +
                "WHERE email = ? " +
                "ORDER BY date_time DESC " +
                "LIMIT 1";


        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUser.getEmail());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    caloriesIn = rs.getInt("calories_in");
                    weight = rs.getDouble("weight_pounds");
                    foundRow = true;
                }
            }

        } catch (SQLException ex) {

            ex.printStackTrace();
        }

        updateStatsLabels(caloriesIn, weight, sleepHours, foundRow);
    }

    // pushes values into labels
    private void updateStatsLabels(int caloriesIn, double weight, double sleepHours, boolean hasData) {
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
            sleepValueLbl.setText(String.format("%.1f", sleepHours));
        }
    }
}
