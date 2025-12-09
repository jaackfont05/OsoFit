import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminPage extends JFrame {
    private static user admin;
    private static MySQLDatabaseConnector db;

    private JButton resetPassBtn;
    private JButton addUser;

    private JComboBox<String> userCombo;

    public AdminPage(user u, MySQLDatabaseConnector db) {
        this.admin = u;
        this.db = db;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Admin Page");
        setLayout(new BorderLayout(10, 10));

        // (C) title
        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Administrate", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        // (S) red underline
        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        //NORTH part bas a sub BorderLayout for menu bar (N), title (C), red line (S)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        userCombo = new JComboBox<>();

        // Populate userCombo with emails from the database
        try (Connection connection = db.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT email, role FROM users")) {

            while (rs.next()) {
                String email = rs.getString("email");
                String role = rs.getString("role");
                userCombo.addItem(email + " (" + role + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error");
        }

        userCombo.setBackground(defaultSettings.BACKGROUND_COLOR);
        userCombo.setPreferredSize(new Dimension(200, 30));
        userCombo.setOpaque(false);
        userCombo.setForeground(defaultSettings.TEXT_COLOR);

        JPanel centerP = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerP.setBackground(defaultSettings.BACKGROUND_COLOR);
        centerP.add(userCombo, BorderLayout.CENTER);

        resetPassBtn = resetB();
        centerP.add(resetPassBtn, BorderLayout.SOUTH);
        add(centerP, BorderLayout.CENTER);

        addUser = addUser();
        add(addUser, BorderLayout.SOUTH);
    }

    private JButton resetB(){
       JButton res = new JButton("Reset");
       res.setBackground(defaultSettings.BACKGROUND_COLOR);
       res.setForeground(defaultSettings.TEXT_COLOR);
       res.setFont(defaultSettings.TITLE_FONT);

       res.addActionListener(e -> {
           String selected = userCombo.getSelectedItem().toString();
           int parenIndex = selected.lastIndexOf(" (");
           String use = selected.substring(0, parenIndex);

           JPanel panel = new JPanel(new GridLayout(2, 1));
           JLabel label = new JLabel("Enter new password for " + use + ":");
           JPasswordField passwordField = new JPasswordField(20);
           panel.add(label);
           panel.add(passwordField);

           int option = JOptionPane.showConfirmDialog(null, panel, "Reset Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
           if (option == JOptionPane.OK_OPTION) {
               String newPass = new String(passwordField.getPassword());
               if (!newPass.isEmpty()) {
                   try {
                       if (db.updatePass(use, newPass)) {
                           JOptionPane.showMessageDialog(null, "Password updated");
                       } else {
                           JOptionPane.showMessageDialog(null, "Failed");
                       }
                   } catch (SQLException ex) {
                       JOptionPane.showMessageDialog(null, "Database error");
                   }
               } else {
                   JOptionPane.showMessageDialog(null, "Password cannot be empty.");
               }
           }
       });

       return res;
    }

    private JButton addUser(){
        JButton res  = new JButton("Add Account");
        res.setBackground(defaultSettings.BACKGROUND_COLOR);
        res.setForeground(defaultSettings.TEXT_COLOR);
        res.setFont(defaultSettings.TITLE_FONT);

        res.addActionListener(e -> {
            this.dispose();
            new createAccountPage(db).setVisible(true);
        });

        return res;
    }


}
