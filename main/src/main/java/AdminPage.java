import javax.swing.*;
import java.awt.*;

public class AdminPage extends JFrame {
    private user admin;
    private MySQLDatabaseConnector db;

    private JButton resetPassBtn;
    private JButton viewUsersBtn;
    private JButton viewTrainersBtn;


    public AdminPage(user u, MySQLDatabaseConnector db) {
        this.admin = u;
        this.db = db;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Admin Page");
        setLayout(new BorderLayout(10, 10));
    }

    private JButton resetB(){
        JButton res = new JButton("Reset Password");

        res.addActionListener(e -> {

        });

        return res;
    }
}
