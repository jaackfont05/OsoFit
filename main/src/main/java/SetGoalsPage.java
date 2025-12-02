import javax.swing.*;
import java.awt.*;

public class SetGoalsPage extends JFrame {
    public MySQLDatabaseConnector db;
    public user currentUser;

    public SetGoalsPage(MySQLDatabaseConnector db, user u) {
        this.db = db;
        currentUser = u;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Create Exercise Page");
        setLayout(new BorderLayout(10, 10));

        //NORTH part bas a sub BorderLayout for menu bar (N), title (C), red line (S)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        // (N) menu bar
        menuBar bar = new menuBar(this, currentUser, db);
        topPanel.add(bar, BorderLayout.NORTH);
    }
}
