import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class mainPage extends JFrame {

    private final user currentUser;
    private MySQLDatabaseConnector db;

    public mainPage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Main Page");
        setLayout(new BorderLayout(10, 10));

        //NORTH part bas a sub BorderLayout for menu bar (N), title (C), red line (S)
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

        //root CENTER: big welcome
        String displayName = (currentUser != null && currentUser.getUserN() != null && !currentUser.getUserN().isBlank())
                ? currentUser.getUserN()
                : "User";

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(defaultSettings.BACKGROUND_COLOR);

        JLabel welcome = new JLabel("Welcome, " + displayName + "!", SwingConstants.CENTER);
        welcome.setForeground(defaultSettings.TEXT_COLOR);
        welcome.setFont(new Font("Serif", Font.BOLD, 36));
        center.add(welcome, BorderLayout.NORTH);

        add(center, BorderLayout.CENTER);
    }

}
