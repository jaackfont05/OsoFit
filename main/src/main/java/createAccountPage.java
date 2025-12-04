

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class createAccountPage extends JFrame {

    // this path is just temporarily used since we will use database finally
    private MySQLDatabaseConnector db = new MySQLDatabaseConnector();
    // regex utility to check email format
    private static final Pattern EMAIL_RE =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    // UI fields
    private JTextField usernameTf;
    private JTextField emailTf;
    private JPasswordField passTf;
    private JPasswordField retypeTf;
    private JTextField cityTf;
    private JTextField animalTf;
    private JRadioButton userRb;
    private JRadioButton trainerRb;

    public createAccountPage(MySQLDatabaseConnector db) {
        this.db = db;
        // Apply defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Create Account");

        // Because frame layout is null, add a root panel that fills the frame.
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(defaultSettings.BACKGROUND_COLOR);
        root.setBounds(0, 0, getWidth(), getHeight());
        add(root);

        // NORTH: title
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(defaultSettings.BACKGROUND_COLOR);
        north.setBorder(new EmptyBorder(18, 18, 0, 18));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        north.add(title, BorderLayout.CENTER);

        JPanel red = new JPanel();
        red.setPreferredSize(new Dimension(1, 6));
        red.setBackground(new Color(220, 0, 0));
        north.add(red, BorderLayout.SOUTH);

        root.add(north, BorderLayout.NORTH);

        // CENTER: form column
        JPanel center = new JPanel(new GridBagLayout()); // centers the column
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        root.add(center, BorderLayout.CENTER);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        center.add(column, new GridBagConstraints());

        column.add(Box.createVerticalStrut(16));
        column.add(makeFormPanel());
        column.add(Box.createVerticalStrut(18));
        column.add(makeRolePanel());
        column.add(Box.createVerticalStrut(18));
        column.add(makeButtonsPanel());
        column.add(Box.createVerticalStrut(12));
    }

    //UI subpanels

    private JPanel makeFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 40, 10, 40));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 12, 8, 12);
        gc.gridy = 0;

        // Row helpers
        addRow(form, gc, "Username:", usernameTf = textField());
        addRow(form, gc, "Email:",    emailTf    = textField());
        addRow(form, gc, "Password:", passTf     = passwordField());
        addRow(form, gc, "Re-type Password:", retypeTf = passwordField());
        addRow(form, gc, "City you were born:",  cityTf   = textField());
        addRow(form, gc, "Favorite animal:",     animalTf = textField());

        return form;
    }

    private JPanel makeRolePanel() {
        JPanel role = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        role.setOpaque(false);

        JLabel roleLbl = label("Choose role:");
        userRb = new JRadioButton("User");
        trainerRb = new JRadioButton("Trainer");
        styleRadio(userRb);
        styleRadio(trainerRb);
        userRb.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(userRb);
        group.add(trainerRb);

        role.add(roleLbl);
        role.add(userRb);
        role.add(trainerRb);
        return role;
    }

    private JPanel makeButtonsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 16, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(480, 40));
        JButton signup = greenButton("Sign Up");
        JButton cancel = outlinedButton("Cancel", new Color(200, 40, 40));

        signup.addActionListener(this::onSignUp);
        cancel.addActionListener(e -> {
            // Close and optionally return to login
            dispose();
            new loginPage(db).setVisible(true);
        });

        // Make Enter key trigger Sign Up
        getRootPane().setDefaultButton(signup);

        p.add(signup);
        p.add(cancel);
        return p;
    }

    //Actions & validation

    private void onSignUp(ActionEvent e) {
        String username = trim(usernameTf);
        String email    = trim(emailTf);
        String pass1    = new String(passTf.getPassword());
        String pass2    = new String(retypeTf.getPassword());
        String city     = trim(cityTf);
        String animal   = trim(animalTf);
        String role;

        if (userRb.isSelected()) {
            role = "User";
        }else{
            role = "Trainer";
        }

        // 1) Required fields
        if (username.isEmpty() || email.isEmpty() || pass1.isEmpty() || pass2.isEmpty()
                || city.isEmpty() || animal.isEmpty()) {
            alert("Please fill out all fields.");
            return;
        }

        // 2) Email format
        if (!isValidEmail(email)) {
            alert("Please enter a valid email address.");
            return;
        }

        // 3) Password match
        if (!pass1.equals(pass2)) {
            alert("Passwords do not match.");
            return;
        }


        user newUser;
        if (role.equals("Trainer")) {
            newUser = new trainer(username, email, pass1, city, animal);
        } else {
            newUser = new user(username, email, pass1, city, animal, "User");
        }

        try{
            boolean f = db.insertUser(newUser);
            if(!f){
                alert("Username or email already in use.");
                return;
            }
        } catch (SQLException ex) {
            alert("Error while creating user");
        }


        JOptionPane.showMessageDialog(this,
                "Sign up successful! You can now log in.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        // Go back to login
        dispose();
        new loginPage(db).setVisible(true);
    }

    private boolean isValidEmail(String email) {
        return EMAIL_RE.matcher(email).matches();
    }

    private String sanitize(String s) { // keep no comma to make sure well structure to store data

        return s.replace(",", " ").trim();
    }

    // small UI utilities

    private void addRow(JPanel form, GridBagConstraints gc, String label, JComponent field) {
        gc.gridx = 0; gc.anchor = GridBagConstraints.EAST;
        form.add(label(label), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST;
        form.add(field, gc);
        gc.gridy++;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(defaultSettings.TEXT_COLOR);
        l.setFont(defaultSettings.LABEL_FONT);
        return l;
    }

    private JTextField textField() {
        JTextField f = new JTextField(22);
        f.setForeground(defaultSettings.TEXT_COLOR);
        f.setBackground(defaultSettings.BACKGROUND_COLOR);
        f.setCaretColor(defaultSettings.TEXT_COLOR);
        f.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        Dimension d = new Dimension(360, 34);
        f.setPreferredSize(d);
        f.setMaximumSize(d);
        return f;
    }

    private JPasswordField passwordField() {
        JPasswordField f = new JPasswordField(22);
        f.setForeground(defaultSettings.TEXT_COLOR);
        f.setBackground(defaultSettings.BACKGROUND_COLOR);
        f.setCaretColor(defaultSettings.TEXT_COLOR);
        f.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        Dimension d = new Dimension(360, 34);
        f.setPreferredSize(d);
        f.setMaximumSize(d);
        return f;
    }

    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setForeground(defaultSettings.TEXT_COLOR);
        rb.setFont(defaultSettings.LABEL_FONT);
    }

    private JButton greenButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        b.setFocusPainted(false);
        return b;
    }

    private JButton outlinedButton(String text, Color border) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setBorder(new LineBorder(border, 2, true));
        b.setFocusPainted(false);
        return b;
    }

    private String trim(JTextField f) { return f.getText().trim(); }

    private void alert(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }
}
