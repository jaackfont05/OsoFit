import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

public class StatsPage extends JFrame {
    private user u;
    private MySQLDatabaseConnector db;

    public StatsPage(user u,  MySQLDatabaseConnector db) {
        this.u = u;
        this.db = db;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Stats Page");
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

        JLabel title = new JLabel("Current Stats", SwingConstants.CENTER);
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

        //User Stats: Current Weight & Steps
        JPanel mainPanel = new JPanel(new GridLayout());
        mainPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        Border lightBorder = BorderFactory.createLineBorder(new Color(255, 138, 138), 5);

        //Make ability to create a new entry
        JPanel createPanel = new JPanel();
        createPanel.setLayout(new BoxLayout(createPanel, BoxLayout.Y_AXIS));
        createPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        createPanel.setBorder(new LineBorder(Color.red,5));

        JLabel createTitle = new JLabel("Add New Statistic Entry");
        createTitle.setFont(defaultSettings.LABEL_FONT);
        createTitle.setBackground(defaultSettings.BACKGROUND_COLOR);
        createTitle.setForeground(defaultSettings.TEXT_COLOR);
        createTitle.setHorizontalAlignment(SwingConstants.CENTER);
        createPanel.add(createTitle);

        createPanel.add(Box.createVerticalStrut(10));

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel,BoxLayout.X_AXIS));
        datePanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        datePanel.setBorder(new EmptyBorder(5,10,5,10));

        JLabel dateLabel = new JLabel("Enter Date (YYYY-MM-DD): ");
        dateLabel.setPreferredSize(new Dimension(200,30));
        dateLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        dateLabel.setForeground(defaultSettings.TEXT_COLOR);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        datePanel.add(dateLabel);

        JTextField dateField = new JTextField();
        dateField.setBackground(defaultSettings.BACKGROUND_COLOR);
        dateField.setForeground(defaultSettings.TEXT_COLOR);
        dateField.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateField.setBorder(lightBorder);
        datePanel.add(dateField);

        createPanel.add(datePanel);

        createPanel.add(Box.createVerticalStrut(10));

        JPanel weightPanel = new JPanel();
        weightPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        weightPanel.setLayout(new BoxLayout(weightPanel,BoxLayout.X_AXIS));
        weightPanel.setBorder(new EmptyBorder(5,10,5,10));

        JLabel weightLabel = new JLabel("Weight (lbs): ");
        weightLabel.setPreferredSize(new Dimension(200,30));
        weightLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        weightLabel.setForeground(defaultSettings.TEXT_COLOR);
        weightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        weightPanel.add(weightLabel);

        JTextField weightField = new JTextField();
        weightField.setBackground(defaultSettings.BACKGROUND_COLOR);
        weightField.setForeground(defaultSettings.TEXT_COLOR);
        weightField.setAlignmentX(Component.CENTER_ALIGNMENT);
        weightField.setPreferredSize(new Dimension(100,30));
        weightField.setBorder(lightBorder);
        weightPanel.add(weightField);

        createPanel.add(weightPanel);

        createPanel.add(Box.createVerticalStrut(10));

        JPanel stepPanel = new JPanel();
        stepPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        stepPanel.setLayout(new BoxLayout(stepPanel,BoxLayout.X_AXIS));
        stepPanel.setBorder(new EmptyBorder(5,10,5,10));

        JLabel stepLabel = new JLabel("Daily Steps: ");
        stepLabel.setPreferredSize(new Dimension(200,30));
        stepLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        stepLabel.setForeground(defaultSettings.TEXT_COLOR);
        stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stepPanel.add(stepLabel);

        JTextField stepField = new JTextField();
        stepField.setBackground(defaultSettings.BACKGROUND_COLOR);
        stepField.setForeground(defaultSettings.TEXT_COLOR);
        stepField.setAlignmentX(Component.CENTER_ALIGNMENT);
        stepField.setPreferredSize(new Dimension(100,30));
        stepField.setBorder(lightBorder);
        stepPanel.add(stepField);

        createPanel.add(stepPanel);

        createPanel.add(Box.createVerticalStrut(10));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));

        JButton addButton = new JButton("Add New Statistic Entry");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setBackground(defaultSettings.BACKGROUND_COLOR);
        addButton.setForeground(defaultSettings.TEXT_COLOR);
        addButton.setFont(defaultSettings.BUTTON_FONT);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Date d = Date.valueOf(dateField.getText());
                    Long w = Long.valueOf(weightField.getText());
                    int s = Integer.valueOf(stepField.getText());
                    db.createStatistic(new Statistic(u.getEmail(), d, w, s), u);
                    new StatsPage(u,db).setVisible(true);
                    dispose();
                }catch(SQLException ex){
                    System.out.println("Error inserting new statistic.");
                }
            }
        });

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createHorizontalGlue());

        createPanel.add(buttonPanel);

        createPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createPanel);

        //Make ability to view previous entries

        JPanel viewPanel = new JPanel();
        viewPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        viewPanel.setLayout(new BoxLayout(viewPanel,BoxLayout.Y_AXIS));
        viewPanel.setBorder(new LineBorder(Color.red,5));

        JLabel viewLabel = new JLabel("Your Statistics History: ");
        viewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        viewLabel.setForeground(defaultSettings.TEXT_COLOR);
        viewLabel.setFont(defaultSettings.LABEL_FONT);

        viewPanel.add(viewLabel);

        viewPanel.add(Box.createVerticalStrut(10));

        JPanel statPanel = new JPanel();
        statPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        statPanel.setLayout(new BoxLayout(statPanel,BoxLayout.Y_AXIS));
        statPanel.setBorder(new EmptyBorder(5,10,5,10));

        ArrayList<Statistic> stats = db.getStatistics(u);

        for(Statistic s : stats){
            JTextField statField = new JTextField(s.toString());
            statField.setPreferredSize(new Dimension(statPanel.getWidth(),100));
            statField.setAlignmentX(Component.CENTER_ALIGNMENT);
            statField.setBackground(defaultSettings.BACKGROUND_COLOR);
            statField.setForeground(defaultSettings.TEXT_COLOR);
            statField.setBorder(lightBorder);

            statField.setMaximumSize(new Dimension(Integer.MAX_VALUE,200));

            statPanel.add(statField);
            statPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(statPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        viewPanel.add(scrollPane);

        mainPanel.add(viewPanel);

        add(mainPanel);
    }
}
