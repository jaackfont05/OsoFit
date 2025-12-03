import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class LogWorkoutPage extends JFrame {
    private user u;
    private MySQLDatabaseConnector db;
    private List<Workout> workoutList = new ArrayList<>();
    private JComboBox<Workout> workoutCB;

    public LogWorkoutPage(user u, MySQLDatabaseConnector db) {
        this.u = u;
        this.db = db;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Log Workout Page");
        setLayout(new BorderLayout(10, 10));

        //NORTH part bas a sub BorderLayout for menu bar (N), title (C), red line (S)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        // (N) menu bar
        menuBar bar = new menuBar(this, u, db);
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


        JLabel selectLabel = new JLabel("Select a workout:", SwingConstants.CENTER);
        selectLabel.setForeground(defaultSettings.TEXT_COLOR);
        selectLabel.setFont(new Font(defaultSettings.TITLE_FONT.getFontName(), Font.PLAIN, 18));
        centerPanel.add(selectLabel, gbc);

        workoutCB = new JComboBox<>();
        workoutCB.setFont(new Font("Sarif", Font.PLAIN, 16));
        workoutCB.setForeground(defaultSettings.TEXT_COLOR);
        workoutCB.setBackground(Color.WHITE);

        workoutCB.setPreferredSize(new Dimension(400, 40));
        centerPanel.add(workoutCB, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }
}
