import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.util.ArrayList;

public class sleepPage extends JFrame {

    private final user u;
    private MySQLDatabaseConnector db;

    public sleepPage(user currentUser, MySQLDatabaseConnector db) {
        this.u = currentUser;
        this.db = db;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Record Sleep Page");
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

        JLabel title = new JLabel("Record Sleep", SwingConstants.CENTER);
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
        add(centerPanel, BorderLayout.CENTER);

        JLabel addLabel = new JLabel("Log New Sleep Record:");
        addLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        addLabel.setBackground(defaultSettings.BACKGROUND_COLOR);
        addLabel.setForeground(defaultSettings.TEXT_COLOR);
        addLabel.setFont(defaultSettings.LABEL_FONT);
        addPanel.add(addLabel);

        addPanel.add(Box.createVerticalStrut(10));

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setMaximumSize(new Dimension(520, Integer.MAX_VALUE)); // cap width

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        addPanel.add(column, gbc);

        // ---- form panel ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints fg = new GridBagConstraints();
        fg.insets = new Insets(0, 12, 8, 12);
        fg.gridy = 0;

        // date row
        fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel dateLbl = stdLabel("Enter date (YYYY-MM-DD):");
        form.add(dateLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        JTextField dateTf = stdTextField(360);
        dateTf.setColumns(20);
        Dimension dateSize = dateTf.getPreferredSize();
        dateTf.setMaximumSize(dateSize);
        form.add(dateTf, fg);

        // hours row
        fg.gridy++; fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel hoursLbl = stdLabel("Hours of sleep:");
        form.add(hoursLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        JTextField hoursTf = stdTextField(360);
        hoursTf.setColumns(20);
        Dimension hoursSize = hoursTf.getPreferredSize();
        hoursTf.setMaximumSize(hoursSize);
        form.add(hoursTf, fg);

        // quality row
        fg.gridy++; fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel qualityLbl = stdLabel("Sleep quality (1–10):");
        form.add(qualityLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        JTextField qualityTf = stdTextField(360);
        qualityTf.setColumns(20);
        Dimension qSize = qualityTf.getPreferredSize();
        qualityTf.setMaximumSize(qSize);
        form.add(qualityTf, fg);

        // add form to column
        column.add(form);
        column.add(Box.createVerticalStrut(16));

        // ---- Save button row ----
        JButton saveBtn = saveSleepButton();
        JPanel btnWrap = new JPanel();
        btnWrap.setOpaque(false);
        btnWrap.add(saveBtn);
        column.add(btnWrap);

        // simple placeholder logic for now
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               Date date = Date.valueOf(dateTf.getText());
               int  hours = Integer.parseInt(hoursTf.getText());
               int  quality = Integer.parseInt(qualityTf.getText());
               db.createSleep(new Sleep(u.getEmail(),hours,quality,date),u);
               new sleepPage(u,db).setVisible(true);
               dispose();
            }
        });

        centerPanel.add(addPanel);

        //Panel to view sleep records

        Border lightBorder = BorderFactory.createLineBorder(new Color(255, 138, 138), 5);

        JPanel rightSide = new JPanel();
        rightSide.setBackground(defaultSettings.BACKGROUND_COLOR);
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setBorder(new LineBorder(Color.red,5));

        JPanel viewPanel = new JPanel();
        viewPanel.setBackground(defaultSettings.BACKGROUND_COLOR);
        viewPanel.setPreferredSize(new Dimension(rightSide.getWidth(), (rightSide.getHeight() * 3 / 4)));
        viewPanel.setLayout(new BoxLayout(viewPanel,BoxLayout.Y_AXIS));

        JLabel viewLabel = new JLabel("Your Sleep History: ");
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

        ArrayList<Sleep> sleepRecords = db.getSleepRecords(u);

        for(Sleep s : sleepRecords){
            JTextField sleepField = new JTextField(s.toString());
            sleepField.setPreferredSize(new Dimension(sleepPanel.getWidth(),100));
            sleepField.setAlignmentX(Component.CENTER_ALIGNMENT);
            sleepField.setBackground(defaultSettings.BACKGROUND_COLOR);
            sleepField.setForeground(defaultSettings.TEXT_COLOR);
            sleepField.setBorder(lightBorder);

            sleepField.setMaximumSize(new Dimension(Integer.MAX_VALUE,200));

            sleepPanel.add(sleepField);
            sleepPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(sleepPanel);
        scrollPane.setBackground(defaultSettings.BACKGROUND_COLOR);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        viewPanel.add(scrollPane);

        rightSide.add(viewPanel);

        JButton sleepGoalsButton = new JButton("Create & View Sleep Goals");
        sleepGoalsButton.setBackground(defaultSettings.BACKGROUND_COLOR);
        sleepGoalsButton.setForeground(defaultSettings.TEXT_COLOR);
        sleepGoalsButton.setFont(defaultSettings.BUTTON_FONT);
        sleepGoalsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sleepGoalsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                //TO-DO: Create & Display Sleep Goals Page
            }
        });

        rightSide.add(sleepGoalsButton);
        centerPanel.add(rightSide);
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
}
