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

public class sleepGoalsPage extends JFrame {

    private final user u;
    private MySQLDatabaseConnector db;

    public sleepGoalsPage(user currentUser, MySQLDatabaseConnector db) {
        this.u = currentUser;
        this.db = db;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Sleep Goals Page");
        setLayout(new BorderLayout(10, 10));

        // ===== NORTH: menu bar + title + red line =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        // (N) menu bar – NOTE: pass db
        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);

        // (C) title
        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Manage Sleep Goals", SwingConstants.CENTER);
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

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        centerPanel.setLayout(new GridLayout());
        centerPanel.setBorder(new EmptyBorder(5,5,5,5));

        //Panel to make new sleep record
        JPanel addPanel = new JPanel();
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
        addPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        addPanel.setBorder(new LineBorder(Color.red,5));

        JLabel addLabel = new JLabel("Create New Sleep Goal:");
        addLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        addLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        addLabel.setForeground(defaultSettings.TEXT_COLOR);
        addLabel.setFont(defaultSettings.LABEL_FONT);
        addPanel.add(addLabel);

        addPanel.add(Box.createVerticalStrut(10));

        Border lightBorder = BorderFactory.createLineBorder(new Color(255, 138, 138), 5);

        JPanel totalHoursPanel = new JPanel();
        totalHoursPanel.setBorder(new EmptyBorder(5,10,5,10));
        totalHoursPanel.setLayout(new BoxLayout(totalHoursPanel, BoxLayout.X_AXIS));
        totalHoursPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        JLabel totalHoursLabel = new JLabel("Total Hours:");
        totalHoursLabel.setPreferredSize(new Dimension(175, 100));
        totalHoursLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        totalHoursLabel.setForeground(defaultSettings.TEXT_COLOR);
        totalHoursLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        totalHoursPanel.add(totalHoursLabel);

        JTextField totalHoursText = new JTextField();
        totalHoursText.setBackground(defaultSettings.BACKGROUND_COLOR);
        totalHoursText.setForeground(defaultSettings.TEXT_COLOR);
        totalHoursText.setBorder(lightBorder);
        totalHoursPanel.add(totalHoursText);

        addPanel.add(totalHoursPanel);

        addPanel.add(Box.createVerticalStrut(10));

        JPanel minQualityPanel = new JPanel();
        minQualityPanel.setBorder(new EmptyBorder(5,10,5,10));
        minQualityPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        minQualityPanel.setLayout(new BoxLayout(minQualityPanel, BoxLayout.X_AXIS));

        JLabel minQualityLabel = new JLabel("Minimum Quality (1-10):");
        minQualityLabel.setPreferredSize(new Dimension(175, 100));
        minQualityLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        minQualityLabel.setForeground(defaultSettings.TEXT_COLOR);
        minQualityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        minQualityPanel.add(minQualityLabel);

        JTextField minQualityText = new JTextField();
        minQualityText.setBackground(defaultSettings.BACKGROUND_COLOR);
        minQualityText.setForeground(defaultSettings.TEXT_COLOR);
        minQualityText.setBorder(lightBorder);
        minQualityPanel.add(minQualityText);

        addPanel.add(minQualityPanel);

        addPanel.add(Box.createVerticalStrut(10));

        JPanel startDatePanel = new JPanel();
        startDatePanel.setBorder(new EmptyBorder(5,10,5,10));
        startDatePanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        startDatePanel.setLayout(new BoxLayout(startDatePanel, BoxLayout.X_AXIS));

        JLabel  startDateLabel = new JLabel("Start Date (YYYY-MM-DD):");
        startDateLabel.setPreferredSize(new Dimension(175, 100));
        startDateLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        startDateLabel.setForeground(defaultSettings.TEXT_COLOR);
        startDateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        startDatePanel.add(startDateLabel);

        JTextField  startDateText = new JTextField();
        startDateText.setBackground(defaultSettings.BACKGROUND_COLOR);
        startDateText.setForeground(defaultSettings.TEXT_COLOR);
        startDateText.setBorder(lightBorder);
        startDatePanel.add(startDateText);

        addPanel.add(startDatePanel);

        addPanel.add(Box.createVerticalStrut(10));

        JPanel endDatePanel = new JPanel();
        endDatePanel.setBorder(new EmptyBorder(5,10,5,10));
        endDatePanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        endDatePanel.setLayout(new BoxLayout(endDatePanel, BoxLayout.X_AXIS));

        JLabel endDateLabel = new JLabel("End Date (YYYY-MM-DD):");
        endDateLabel.setPreferredSize(new Dimension(175, 100));
        endDateLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        endDateLabel.setForeground(defaultSettings.TEXT_COLOR);
        endDateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        endDatePanel.add(endDateLabel);

        JTextField  endDateText = new JTextField();
        endDateText.setBackground(defaultSettings.BACKGROUND_COLOR);
        endDateText.setForeground(defaultSettings.TEXT_COLOR);
        endDateText.setBorder(lightBorder);
        endDatePanel.add(endDateText);
        addPanel.add(endDatePanel);

        addPanel.add(Box.createVerticalStrut(10));

        JButton addButton = new JButton("Add Goal");
        addButton.setBackground(defaultSettings.BACKGROUND_COLOR);
        addButton.setForeground(defaultSettings.TEXT_COLOR);
        addButton.setFont(defaultSettings.BUTTON_FONT);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int totalHours = Integer.parseInt(totalHoursText.getText());
                int minQuality;
                if(minQualityText.getText().isEmpty()) {
                    minQuality = 1;
                }else {
                    minQuality = Integer.parseInt(minQualityText.getText());
                }
                Date startDate = Date.valueOf(startDateText.getText());
                Date endDate = Date.valueOf(endDateText.getText());
                sleepGoal addMe = new sleepGoal(u.getEmail(),totalHours,minQuality,startDate,endDate);
                addMe.setHourProgress(db.getHourProgress(addMe,u));
                db.createSleepGoal(addMe, u);
                new sleepGoalsPage(u,db).setVisible(true);
                dispose();
            }
        });

        addPanel.add(addButton);
        addPanel.add(Box.createVerticalStrut(10));

        centerPanel.add(addPanel);

        //Panel to view sleep records

        JPanel rightSide = new JPanel();
        rightSide.setBackground(defaultSettings.BACKGROUND_COLOR);
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setBorder(new LineBorder(Color.red,5));

        JPanel viewPanel = new JPanel();
        viewPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        viewPanel.setPreferredSize(new Dimension(rightSide.getWidth(), rightSide.getHeight()));
        viewPanel.setLayout(new BoxLayout(viewPanel,BoxLayout.Y_AXIS));

        JLabel viewLabel = new JLabel("Your Sleep Goals: ");
        viewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        viewLabel.setForeground(defaultSettings.TEXT_COLOR);
        viewLabel.setFont(defaultSettings.LABEL_FONT);

        viewPanel.add(viewLabel);

        viewPanel.add(Box.createVerticalStrut(10));

        JPanel sleepPanel = new JPanel();
        sleepPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        sleepPanel.setLayout(new BoxLayout(sleepPanel,BoxLayout.Y_AXIS));
        sleepPanel.setBorder(new EmptyBorder(5,10,5,10));

        ArrayList<sleepGoal> sleepGoals = db.getSleepGoals(u);
        for(int idx = 0; idx < sleepGoals.size(); idx++) {
            db.getHourProgress(sleepGoals.get(idx),u);
        }

        for(sleepGoal sg : sleepGoals){
            JPanel goalPanel = new JPanel();
            goalPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
            goalPanel.setLayout(new BoxLayout(goalPanel,BoxLayout.X_AXIS));

            JTextArea sleepField = new JTextArea(sg.toString());
            System.out.println("SG currentHours: " + sg.getCurrentHours());
            System.out.println("Database currentHours: " + db.getHourProgress(sg,u));
            sleepField.setLineWrap(true);
            sleepField.setEditable(false);
            sleepField.setPreferredSize(new Dimension(sleepPanel.getWidth() * (3 / 4),100));
            sleepField.setAlignmentX(Component.CENTER_ALIGNMENT);
            sleepField.setBackground(defaultSettings.BACKGROUND_COLOR);
            sleepField.setForeground(defaultSettings.TEXT_COLOR);
            sleepField.setBorder(lightBorder);

            sleepField.setMaximumSize(new Dimension(Integer.MAX_VALUE,200));

            goalPanel.add(sleepField);

            JButton deleteGoalButton = new JButton("Delete Goal");
            deleteGoalButton.setBackground(defaultSettings.BACKGROUND_COLOR);
            deleteGoalButton.setForeground(defaultSettings.TEXT_COLOR);
            deleteGoalButton.setFont(defaultSettings.BUTTON_FONT);
            deleteGoalButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfirmationDialog cd = makeConfirmationDialog(sg,u);
                }
            });

            goalPanel.add(deleteGoalButton);
            sleepPanel.add(goalPanel);

            sleepPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(sleepPanel);
        scrollPane.setBackground(defaultSettings.BACKGROUND_COLOR);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        viewPanel.add(scrollPane);

        rightSide.add(viewPanel);

        centerPanel.add(rightSide);

        add(centerPanel, BorderLayout.CENTER);
    }



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

    private static JButton saveSleepButton() {
        JButton b = new JButton("Save Sleep");
        b.setOpaque(false);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setFont(defaultSettings.LABEL_FONT);
        return b;
    }

    public ConfirmationDialog makeConfirmationDialog(sleepGoal sg, user u){
        return new ConfirmationDialog(this,sg,u);
    }


    public class ConfirmationDialog extends JDialog {
        private sleepGoalsPage owner;
        private sleepGoal sg;
        private user u;

        public ConfirmationDialog(sleepGoalsPage owner, sleepGoal sg, user u) {
            super();
            this.owner = owner;
            this.sg = sg;
            this.u = u;
            createGUI();
        }

        public void createGUI() {
            this.setPreferredSize(new Dimension(500, 200));
            this.setTitle("Confirmation Dialog");

            JPanel confirmationPanel = new JPanel();
            confirmationPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
            confirmationPanel.setLayout(new BoxLayout(confirmationPanel, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel("Are you sure you want to delete this sleep goal?", SwingConstants.CENTER);
            titleLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
            titleLabel.setForeground(defaultSettings.TEXT_COLOR);
            titleLabel.setFont(defaultSettings.TITLE_FONT.deriveFont(24f));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            confirmationPanel.add(titleLabel);

            JPanel redLine = new JPanel();
            redLine.setBackground(new Color(220, 0, 0));
            redLine.setPreferredSize(new Dimension(1, 6));
            confirmationPanel.add(redLine);

            confirmationPanel.add(Box.createVerticalStrut(10));

            JTextArea sleepGoalText = new JTextArea(sg.toString());
            sleepGoalText.setLineWrap(true);
            sleepGoalText.setPreferredSize(new Dimension(300, 100));
            sleepGoalText.setBackground(defaultSettings.BACKGROUND_COLOR);
            sleepGoalText.setForeground(defaultSettings.TEXT_COLOR);

            confirmationPanel.add(sleepGoalText);

            confirmationPanel.add(Box.createVerticalStrut(10));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

            JButton yesButton = new JButton("Yes");
            yesButton.setBackground(defaultSettings.BACKGROUND_COLOR);
            yesButton.setForeground(defaultSettings.TEXT_COLOR);
            yesButton.setFont(defaultSettings.BUTTON_FONT);
            yesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        db.deleteSleepGoal(sg, u);
                        new sleepGoalsPage(u, db).setVisible(true);
                        owner.dispose();
                        dispose();
                    }catch(SQLException ex){
                        System.out.println("Error deleting sleep goal.");
                    }
                }
            });
            buttonPanel.add(yesButton);

            buttonPanel.add(Box.createHorizontalStrut(10));

            JButton noButton = new JButton("No");
            noButton.setBackground(defaultSettings.BACKGROUND_COLOR);
            noButton.setForeground(defaultSettings.TEXT_COLOR);
            noButton.setFont(defaultSettings.BUTTON_FONT);
            noButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            buttonPanel.add(noButton);

            confirmationPanel.add(buttonPanel);
            this.add(confirmationPanel);
            this.pack();
            setVisible(true);
        }
    }
}