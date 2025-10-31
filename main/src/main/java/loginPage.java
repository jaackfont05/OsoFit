
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class loginPage extends JFrame {
    // you may need to reference this path to make picture visible
    private static final String IMG_DIR = "main/src/main/resources/images/";
    private static final String LEFT_IMG_PATH  = IMG_DIR + "bear.png";
    private static final String RIGHT_IMG_PATH = IMG_DIR + "bu.png";
    private static final String USERS_CSV = "main/src/main/resources/data/userInformation.csv";


    // rescale picture so that can show them perfectly
    private BufferedImage leftSrc, rightSrc;
    private JLabel leftLabel, rightLabel;
    private JPanel west, east;

    public loginPage() {
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Login");

        // Root layout: keep everything centered and resizable
        setLayout(new BorderLayout(10, 10));

        // NORTH: title + red underline
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(defaultSettings.BACKGROUND_COLOR);
        north.setBorder(new EmptyBorder(16, 16, 0, 16));

        JLabel title = new JLabel("OsoFit", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        north.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        north.add(redLine, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);

        // WEST/EAST: side images
        leftLabel = new JLabel();
        leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
        west = wrap(leftLabel, 16, 0, 0, 0);
        west.setPreferredSize(new Dimension(240, 1));
        add(west, BorderLayout.WEST);

        rightLabel = new JLabel();
        rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        east = wrap(rightLabel, 0, 0, 16, 0);
        east.setPreferredSize(new Dimension(260, 1));
        add(east, BorderLayout.EAST);

        //CENTER: stacked column layout for "Login" title + name/field + buttons
        JPanel center = new JPanel(new GridBagLayout()); // centers the column panel
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        add(center, BorderLayout.CENTER);

        // A narrow column that won’t stretch wider than needed
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setMaximumSize(new Dimension(520, Integer.MAX_VALUE)); // cap width
        center.add(column, new GridBagConstraints()); // centered by parent GridBag

        // Header "Login"
        JLabel loginHeader = new JLabel("Login", SwingConstants.CENTER);
        loginHeader.setForeground(defaultSettings.TEXT_COLOR);
        loginHeader.setFont(new Font("SansSerif", Font.PLAIN, 28));
        loginHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(loginHeader);
        column.add(Box.createVerticalStrut(12));

       // set layout rule
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints fg = new GridBagConstraints();
        fg.insets = new Insets(8, 12, 8, 12);
        fg.gridy = 0;

       // Email row
        fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel emailLbl = stdLabel("Enter email:");
        form.add(emailLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        JTextField emailTf = stdTextField(360);
        emailTf.setColumns(20);
        Dimension tfSize = emailTf.getPreferredSize();
        emailTf.setMaximumSize(tfSize);   // prevent vertical stretching
        form.add(emailTf, fg);

       // Password row
        fg.gridy++; fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel passLbl = stdLabel("Enter password:");
        form.add(passLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        JPasswordField passTf = stdPasswordField(360);
        passTf.setColumns(20);
        Dimension pfSize = passTf.getPreferredSize();
        passTf.setMaximumSize(pfSize);    // prevent vertical stretching
        form.add(passTf, fg);

        column.add(form);
        column.add(Box.createVerticalStrut(16));

        // Buttons in one column
        JPanel buttons = new JPanel(new GridLayout(3, 1, 10, 10));
        buttons.setOpaque(false);
        buttons.setMaximumSize(new Dimension(360, 3 * 36 + 2 * 10));
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn   = stdGreenButton("Login");
        JButton forgotBtn  = stdOutlinedButton("Forgot password?", new Color(200, 40, 40));
        JButton createBtn  = stdOutlinedButton("Create Account", defaultSettings.BORDER_COLOR);
        //so that "Enter" button in keyboard will take us into user main page
        getRootPane().setDefaultButton(loginBtn);

        buttons.add(loginBtn);
        buttons.add(forgotBtn);
        buttons.add(createBtn);
        column.add(buttons);
        column.add(Box.createVerticalStrut(8)); // small bottom padding

        //Button actions
        loginBtn.addActionListener(e -> {
            String email = emailTf.getText().trim();
            String pass  = new String(passTf.getPassword());

            if (email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter email and password.",
                        "Missing info", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                user u = authenticate(email, pass);   // try to load the user from CSV
                if (u == null) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid email or password.",
                            "Login failed",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // success: go to main page with the logged-in user
                dispose();
                new mainPage(u).setVisible(true);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error reading user data:\n" + ex.getMessage(),
                        "Login error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });


        forgotBtn.addActionListener(e -> {
            dispose(); // may need to close login page or not, we can discuss later
            new forgetPasswordPage().setVisible(true);
        });


        createBtn.addActionListener(e -> {
            dispose(); // ditto
            new createAccountPage().setVisible(true);
        });

        // Load and scale side images helper, this part is kind tricky, recommend you to google how to insert
        // a picture to show it perfectly
        try {
            leftSrc = ImageIO.read(new File(LEFT_IMG_PATH));
            rightSrc = ImageIO.read(new File(RIGHT_IMG_PATH));
        } catch (Exception ex) {
            System.err.println("Could not load side images: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(this::rescaleSideImages);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { rescaleSideImages(); }
        });
    }

    // helper: dynamically rescale side images
    private void rescaleSideImages() {
        if (leftSrc == null || rightSrc == null) return;

        int frameW = getWidth() > 0 ? getWidth() : 900;
        int sideW = (int) (frameW * 0.22);
        sideW = Math.max(180, Math.min(340, sideW));

        west.setPreferredSize(new Dimension(sideW, 1));
        east.setPreferredSize(new Dimension(sideW, 1));
        west.revalidate();
        east.revalidate();

        int maxW = Math.max(1, sideW - 32);
        int maxH = Math.max(1, getHeight() - 180);

        leftLabel.setIcon(new ImageIcon(scaleToFit(leftSrc, maxW, maxH)));
        rightLabel.setIcon(new ImageIcon(scaleToFit(rightSrc, maxW, maxH)));
    }

    private static Image scaleToFit(BufferedImage src, int maxW, int maxH) {
        double rw = maxW / (double) src.getWidth();
        double rh = maxH / (double) src.getHeight();
        double r = Math.min(rw, rh);
        int w = (int) Math.round(src.getWidth() * r);
        int h = (int) Math.round(src.getHeight() * r);
        return src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    private static JPanel wrap(JComponent c, int l, int t, int r, int b) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(defaultSettings.BACKGROUND_COLOR);
        p.setBorder(new EmptyBorder(t, l, b, r));
        p.add(c, BorderLayout.CENTER);
        return p;
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

    private static JPasswordField stdPasswordField(int width) {
        JPasswordField f = new JPasswordField();
        f.setPreferredSize(new Dimension(width, 34));
        f.setForeground(defaultSettings.TEXT_COLOR);
        f.setBackground(defaultSettings.BACKGROUND_COLOR);
        f.setCaretColor(defaultSettings.TEXT_COLOR);
        f.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        return f;
    }

    private static JButton stdGreenButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true));
        return b;
    }

    private static JButton stdOutlinedButton(String text, Color borderColor) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(borderColor, 2, true));
        return b;
    }

    private user authenticate(String email, String password) throws IOException {
        Path p = Paths.get(USERS_CSV);
        if (!Files.exists(p)) return null;

        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Skip header if present
                if (header) {
                    header = false;
                    if (line.toLowerCase().startsWith("username,email,password,")) {
                        continue;
                    }
                }

                String[] cols = line.split(",", -1);
                if (cols.length < 6) continue;

                String usernameCol = cols[0].trim();
                String emailCol    = cols[1].trim();
                String passCol     = cols[2].trim();
                String cityCol     = cols[3].trim();
                String animalCol   = cols[4].trim();
                String roleCol     = cols[5].trim();

                if (emailCol.equalsIgnoreCase(email) && passCol.equals(password)) { // if verify successfully
                    return new user(usernameCol, emailCol, passCol, cityCol, animalCol, roleCol);
                    //superclass user used as a flag to identify its specific functionalities and its own database information.
                }
            }
        }
        return null;
    }
}
