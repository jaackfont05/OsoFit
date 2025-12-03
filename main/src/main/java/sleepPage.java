import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class sleepPage extends JFrame {

    private final user currentUser;
    private MySQLDatabaseConnector db;

    public sleepPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
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

        // ===== CENTER: form column =====
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        add(center, BorderLayout.CENTER);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setMaximumSize(new Dimension(520, Integer.MAX_VALUE)); // cap width

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        center.add(column, gbc);

        // ---- form panel ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints fg = new GridBagConstraints();
        fg.insets = new Insets(0, 12, 8, 12);
        fg.gridy = 0;

        // date row
        fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel dateLbl = stdLabel("Enter date (MM/DD/YYYY):");
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
        saveBtn.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Sleep entry recorded (UI only – DB logic later).",
                    "Sleep Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
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
