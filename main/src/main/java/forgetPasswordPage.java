import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class forgetPasswordPage extends JFrame {

    private static final String DATA_PATH = "main/src/main/resources/data/userInformation.csv";

    private JTextField emailTf;
    private JTextField cityTf;
    private JTextField animalTf;

    public forgetPasswordPage() {
        // Apply default style
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Forget Password");
        setLayout(new BorderLayout());

        // Root layout
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(defaultSettings.BACKGROUND_COLOR);
        add(root);

        //NORTH: title
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(defaultSettings.BACKGROUND_COLOR);
        north.setBorder(new EmptyBorder(18, 18, 0, 18));

        JLabel title = new JLabel("Forget Password?", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        north.add(title, BorderLayout.CENTER);

        JPanel red = new JPanel();
        red.setPreferredSize(new Dimension(1, 6));
        red.setBackground(new Color(220, 0, 0));
        north.add(red, BorderLayout.SOUTH);

        root.add(north, BorderLayout.NORTH);

        //CENTER: Form section : 3 input fields verification
        JPanel center = new JPanel(new GridBagLayout());
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
        column.add(makeButtonsPanel());
        column.add(Box.createVerticalStrut(12));
    }

    private JPanel makeFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 40, 10, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 12, 8, 12);
        gc.gridy = 0;

        addRow(form, gc, "Email:", emailTf = textField());
        addRow(form, gc, "City you were born:", cityTf = textField());
        addRow(form, gc, "Favorite animal:", animalTf = textField());

        return form;
    }

    private JPanel makeButtonsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 16, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(480, 40));

        JButton verify = greenButton("Verify & Reset Password");
        JButton cancel = outlinedButton("Cancel", new Color(200, 40, 40));

        verify.addActionListener(this::onVerify);
        cancel.addActionListener(e -> dispose());

        p.add(verify);
        p.add(cancel);
        return p;
    }

    private void onVerify(ActionEvent e) {
        String email = trim(emailTf);
        String city = trim(cityTf);
        String animal = trim(animalTf);

        if (email.isEmpty() || city.isEmpty() || animal.isEmpty()) {
            alert("Please fill out all fields.");
            return;
        }

        try {
            if (verifyUser(email, city, animal)) {
                // Verified successfully → show reset window
                openResetDialog(email);
            } else {
                alert("Verification failed. Please check your answers.");
            }
        } catch (IOException ex) {
            alert("Error verifying account:\n" + ex.getMessage());
        }
    }

    private boolean verifyUser(String email, String city, String animal) throws IOException {
        Path p = Paths.get(DATA_PATH);
        if (!Files.exists(p)) return false;

        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            boolean hasHeader = false;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (!hasHeader && line.toLowerCase().startsWith("username,email,")) {
                    hasHeader = true;
                    continue;
                }
                String[] cols = line.split(",", -1);
                if (cols.length >= 6) {
                    String emailCol = cols[1].trim();
                    String cityCol = cols[3].trim();
                    String animalCol = cols[4].trim();
                    if (emailCol.equalsIgnoreCase(email)
                            && cityCol.equalsIgnoreCase(city)
                            && animalCol.equalsIgnoreCase(animal)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void openResetDialog(String email) {
        JDialog dialog = new JDialog(this, "Reset Password", true);
        dialog.setSize(500, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(defaultSettings.BACKGROUND_COLOR);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 40, 20, 40));
        form.setOpaque(false);

        JLabel newLbl = label("New Password:");
        JLabel reLbl = label("Re-type Password:");
        JPasswordField newPf = passwordField();
        JPasswordField rePf = passwordField();

        form.add(newLbl); form.add(newPf);
        form.add(reLbl); form.add(rePf);
        dialog.add(form, BorderLayout.CENTER);

        JButton confirm = greenButton("Confirm");
        confirm.addActionListener(ev -> {
            String p1 = new String(newPf.getPassword());
            String p2 = new String(rePf.getPassword());
            if (p1.isEmpty() || p2.isEmpty()) {
                alert("Please enter both fields.");
                return;
            }
            if (!p1.equals(p2)) {
                alert("Passwords do not match.");
                return;
            }
            try {
                updatePassword(email, p1);
                JOptionPane.showMessageDialog(dialog, "Password reset successfully!");
                dialog.dispose();
                dispose();
                new loginPage().setVisible(true);
            } catch (IOException ex) {
                alert("Failed to reset password:\n" + ex.getMessage());
            }
        });

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(confirm);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void updatePassword(String email, String newPass) throws IOException {
        Path p = Paths.get(DATA_PATH);
        Path temp = Paths.get(DATA_PATH + ".tmp");

        List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
        List<String> updated = new ArrayList<>();

        for (String line : lines) {
            if (line.toLowerCase().startsWith("username,email,")) {
                updated.add(line);
                continue;
            }
            String[] cols = line.split(",", -1);
            if (cols.length >= 6 && cols[1].trim().equalsIgnoreCase(email)) {
                cols[2] = newPass; // Update password column
                updated.add(String.join(",", cols));
            } else {
                updated.add(line);
            }
        }

        Files.write(temp, updated, StandardCharsets.UTF_8);
        Files.move(temp, p, StandardCopyOption.REPLACE_EXISTING);
    }

    // --- UI helpers ---
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
        //fix field boxes size
        Dimension d = new Dimension(280, 28);
        f.setPreferredSize(d);
        f.setMaximumSize(d);
        f.setMinimumSize(new Dimension(180, 24));  // optional
        return f;
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

