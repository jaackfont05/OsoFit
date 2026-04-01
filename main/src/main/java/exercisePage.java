import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;

public class exercisePage extends JFrame {
    private static user currentUser;
    private static MySQLDatabaseConnector db;//need to implement
    private static JTextField nameTf;
    private static JTextField weightTf;
    private static JTextField setsTf;
    private static JTextField repsTf;
    private static JTextField equipTf;



    public exercisePage(user currentUser, MySQLDatabaseConnector db) {
        this.currentUser = currentUser;
        this.db = db;

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

        // (C) title
        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Create Exercise", SwingConstants.CENTER);
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

        //CENTER: stacked column layout for "Login" title + name/field + buttons
        JPanel center = new JPanel(new GridBagLayout()); // centers the column panel
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        add(center, BorderLayout.CENTER);

        // A narrow column that won’t stretch wider than needed
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setMaximumSize(new Dimension(520, Integer.MAX_VALUE)); // cap width

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        center.add(column, gbc);

        // set layout rule
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints fg = new GridBagConstraints();
        fg.insets = new Insets(0, 12, 8, 12);
        fg.gridy = 0;

        // name row
        fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel nameLbl = stdLabel("Enter exercise name:");
        form.add(nameLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        nameTf = stdTextField(360);
        nameTf.setColumns(20);
        Dimension tfSize = nameTf.getPreferredSize();
        nameTf.setMaximumSize(tfSize);   // prevent vertical stretching
        form.add(nameTf, fg);

        //Equipment row
        fg.gridy++; fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel equipLbl = stdLabel("Enter necessary equipment:");
        form.add(equipLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        equipTf = stdTextField(360);
        equipTf.setColumns(20);
        Dimension etfSize = equipTf.getPreferredSize();
        equipTf.setMaximumSize(etfSize);   // prevent vertical stretching
        form.add(equipTf, fg);

        // weight row
        fg.gridy++; fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel weightLbl = stdLabel("Enter weight:");
        form.add(weightLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        weightTf = stdTextField(360);
        weightTf.setColumns(20);
        Dimension pfSize = weightTf.getPreferredSize();
        weightTf.setMaximumSize(pfSize);    // prevent vertical stretching
        form.add(weightTf, fg);

        //sets row
        fg.gridy++; fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel setsLbl = stdLabel("Enter sets:");
        form.add(setsLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        setsTf = stdTextField(360);
        setsTf.setColumns(20);
        Dimension setsSize = setsTf.getPreferredSize();
        setsTf.setMaximumSize(setsSize);    // prevent vertical stretching
        form.add(setsTf, fg);

        //reps row
        fg.gridy++; fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel repsLbl = stdLabel("Enter reps:");
        form.add(repsLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        repsTf = stdTextField(360);
        repsTf.setColumns(20);
        Dimension repsSize = repsTf.getPreferredSize();
        repsTf.setMaximumSize(repsSize);    // prevent vertical stretching
        form.add(repsTf, fg);

        //Add Exercise Button
        fg.gridy++; fg.gridx = 0; fg.gridwidth = 2; fg.anchor = GridBagConstraints.CENTER;
        JButton addE = addE();
        form.add(addE, fg);

        column.add(form);
        column.add(Box.createVerticalStrut(16));
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

    private static JButton addE(){
        JButton b = new JButton("Add Exercise");
        b.setOpaque(false);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setFont(defaultSettings.LABEL_FONT);
        b.addActionListener(e -> {
            try {
                String name = nameTf.getText();
                String equipment = equipTf.getText();
                int weight = Integer.parseInt(weightTf.getText());
                int sets = Integer.parseInt(setsTf.getText());
                int reps = Integer.parseInt(repsTf.getText());
                Exercise exercise = new Exercise(weight, equipment, sets, reps, name);
                db.createExercise(exercise, currentUser);
                JOptionPane.showMessageDialog(null, "Exercise added successfully");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter valid integers for weight, sets, and reps.");
            }catch(SQLException exc){
                JOptionPane.showMessageDialog(null, "Something went wrong while trying to add exercise.");
            }
        });

        return b;
    }
}
