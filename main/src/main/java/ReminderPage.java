/**
 *  Mason Baxter
 *  Creates and displays the page for user's to add, view, and delete their daily reminders
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReminderPage extends JFrame {
    private user u;
    private MySQLDatabaseConnector db;
    private JComboBox<Workout> workoutCB;

    public ReminderPage(user u, MySQLDatabaseConnector db) {
        this.u = u;
        this.db = db;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Reminders Page");
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

        JLabel title = new JLabel("Reminders", SwingConstants.CENTER);
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

        //The main panel that will include two sub panels, one to create reminders and one to view/delete
        JPanel mainPanel = new JPanel(new GridLayout());
        mainPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        Border redBorder = BorderFactory.createLineBorder(Color.red, 5);
        Border lightBorder = BorderFactory.createLineBorder(new Color(255, 138, 138), 5);
        int createSpacing = 30;

        JPanel createPanel = new JPanel();
        createPanel.setLayout(new BoxLayout(createPanel,  BoxLayout.Y_AXIS));
        createPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        createPanel.setBorder(redBorder);

        JLabel createLabel = new JLabel("Create a New Reminder:", SwingConstants.CENTER);
        createLabel.setForeground(defaultSettings.TEXT_COLOR);
        createLabel.setFont(defaultSettings.TITLE_FONT.deriveFont(24f));
        createLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        createPanel.add(createLabel);

        createPanel.add(Box.createVerticalStrut(15));

        //The form to create a new reminder
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        titlePanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setPreferredSize(new Dimension(100, 50));
        titleLabel.setForeground(defaultSettings.TEXT_COLOR);
        titleLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);

        JTextField titleTextField = new JTextField("Replace this with the title of your reminder.");
        titleTextField.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleTextField.setForeground(defaultSettings.TEXT_COLOR);
        titleTextField.setBorder(lightBorder);
        titlePanel.add(titleTextField);
        createPanel.add(titlePanel);

        createPanel.add(Box.createVerticalStrut(createSpacing));

        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.X_AXIS));
        descriptionPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        descriptionPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel descriptionLabel = new JLabel("Description: ");
        descriptionLabel.setPreferredSize(new Dimension(100, 100));
        descriptionLabel.setForeground(defaultSettings.TEXT_COLOR);
        descriptionLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionPanel.add(descriptionLabel);

        JTextArea descriptionTextField = new JTextArea("The description is optional. \n Please delete this text and leave the field blank if you do not want a description.");
        descriptionTextField.setLineWrap(true);
        descriptionTextField.setBackground(defaultSettings.BACKGROUND_COLOR);
        descriptionTextField.setForeground(defaultSettings.TEXT_COLOR);
        descriptionTextField.setBorder(lightBorder);
        descriptionPanel.add(descriptionTextField);
        createPanel.add(descriptionPanel);

        createPanel.add(Box.createVerticalStrut(createSpacing));

        JPanel frequencyPanel = new JPanel();
        frequencyPanel.setLayout(new BoxLayout(frequencyPanel, BoxLayout.X_AXIS));
        frequencyPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        frequencyPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel frequencyLabel = new JLabel("Number per Day:");
        frequencyLabel.setPreferredSize(new Dimension(100, 50));
        frequencyLabel.setForeground(defaultSettings.TEXT_COLOR);
        frequencyLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        frequencyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        frequencyPanel.add(frequencyLabel);

        JTextField frequencyTextField = new JTextField("Replace this text with a number greater than 0");
        frequencyTextField.setBackground(defaultSettings.BACKGROUND_COLOR);
        frequencyTextField.setForeground(defaultSettings.TEXT_COLOR);
        frequencyTextField.setBorder(lightBorder);
        frequencyPanel.add(frequencyTextField);
        createPanel.add(frequencyPanel);

        createPanel.add(Box.createVerticalStrut(createSpacing));

        JButton addButton = new JButton("Add Reminder");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setBackground(defaultSettings.BACKGROUND_COLOR);
        addButton.setForeground(defaultSettings.TEXT_COLOR);
        addButton.setFont(defaultSettings.BUTTON_FONT);
        addButton.addActionListener(new ActionListener() {
            //to do
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String title = titleTextField.getText();
                    String description = descriptionTextField.getText();
                    int frequency = Integer.parseInt(frequencyTextField.getText());
                    db.createReminder(new Reminder(u.getEmail(), title, description, frequency), u);
                    new ReminderPage(u,db).setVisible(true);
                    dispose();
                }catch(SQLException ex){
                    System.out.println("Error adding new reminder.");
                }
            }
        });

        createPanel.add(addButton);

        createPanel.add(Box.createVerticalStrut(createSpacing));

        mainPanel.add(createPanel);

        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BoxLayout(viewPanel,  BoxLayout.Y_AXIS));
        viewPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        viewPanel.setBorder(redBorder);

        JLabel viewLabel = new JLabel("Your Reminders: ", SwingConstants.CENTER);
        viewLabel.setForeground(defaultSettings.TEXT_COLOR);
        viewLabel.setFont(defaultSettings.TITLE_FONT.deriveFont(24f));
        viewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewPanel.add(viewLabel);

        viewPanel.add(Box.createVerticalStrut(createSpacing));

        //Display all the reminders with ability to remove them
        try {
            ArrayList<Reminder> reminders = db.getReminders(u);
            for(Reminder r : reminders) {
                JPanel reminderPanel = new JPanel();
                reminderPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
                reminderPanel.setLayout(new BoxLayout(reminderPanel, BoxLayout.X_AXIS));
                reminderPanel.setBorder(new EmptyBorder(5,10,5,10));

                JTextArea reminderTextArea = new JTextArea(r.toString());
                reminderTextArea.setLineWrap(true);
                reminderTextArea.setPreferredSize(new Dimension(150, 30));
                reminderTextArea.setBackground(defaultSettings.BACKGROUND_COLOR);
                reminderTextArea.setForeground(defaultSettings.TEXT_COLOR);
                reminderTextArea.setBorder(lightBorder);
                reminderPanel.add(reminderTextArea);

                reminderPanel.add(Box.createHorizontalStrut(10));

                JButton deleteReminderButton = new JButton("Delete this Reminder");
                deleteReminderButton.setBackground(defaultSettings.BACKGROUND_COLOR);
                deleteReminderButton.setForeground(defaultSettings.TEXT_COLOR);
                deleteReminderButton.setFont(defaultSettings.BUTTON_FONT);
                deleteReminderButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ConfirmationDialog cd = makeConfirmationDialog(r,u);
                    }
                });
                reminderPanel.add(deleteReminderButton);

                viewPanel.add(reminderPanel);

                viewPanel.add(Box.createVerticalStrut(createSpacing));
            }
        } catch (SQLException e){
            System.out.println("An error occured when trying to get reminders.");
        }



        mainPanel.add(viewPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    public ConfirmationDialog makeConfirmationDialog(Reminder r, user u){
        return new ConfirmationDialog(this,r,u);
    }


    public class ConfirmationDialog extends JDialog {
        private ReminderPage owner;
        private Reminder r;
        private user u;

        public ConfirmationDialog(ReminderPage owner, Reminder r, user u) {
            super();
            this.owner = owner;
            this.r = r;
            this.u = u;
            createGUI();
        }

        public void createGUI() {
            this.setPreferredSize(new Dimension(500, 200));
            this.setTitle("Confirmation Dialog");

            JPanel confirmationPanel = new JPanel();
            confirmationPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
            confirmationPanel.setLayout(new BoxLayout(confirmationPanel, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel("Are you sure you want to delete this reminder?", SwingConstants.CENTER);
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

            JTextArea reminderText = new JTextArea(r.toString());
            reminderText.setLineWrap(true);
            reminderText.setPreferredSize(new Dimension(300, 100));
            reminderText.setBackground(defaultSettings.BACKGROUND_COLOR);
            reminderText.setForeground(defaultSettings.TEXT_COLOR);

            confirmationPanel.add(reminderText);

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
                        db.deleteReminder(r, u);
                        new ReminderPage(u, db).setVisible(true);
                        owner.dispose();
                        dispose();
                    }catch(SQLException ex){
                        System.out.println("Error deleting reminder.");
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
