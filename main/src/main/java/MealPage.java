import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MealPage extends JFrame {
    private static Meal meal;
    private MySQLDatabaseConnector db;
    private user u;
    private static JTextField nameTf;
    private static JTextField calsTf;

    public MealPage(user u, MySQLDatabaseConnector db) {
        this.db = db;
        this.u = u;

        // Apply shared defaults
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Meal Page");
        setLayout(new BorderLayout(10, 10));

        //NORTH part bas a sub BorderLayout for menu bar (N), title (C), red line (S)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        // (N) menu bar
        menuBar bar = new menuBar(this, u, db);
        topPanel.add(bar, BorderLayout.NORTH);

        // (C) title
        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Meal", SwingConstants.CENTER);
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

        //name
        fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel nameLbl = stdLabel("Meal Name:");
        form.add(nameLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        nameTf = stdTextField(360);
        nameTf.setColumns(20);
        Dimension tfSize = nameTf.getPreferredSize();
        nameTf.setMaximumSize(tfSize);   // prevent vertical stretching
        form.add(nameTf, fg);

        //calories
        fg.gridy++; fg.gridx = 0; fg.anchor = GridBagConstraints.EAST;
        JLabel weightLbl = stdLabel("Calories:");
        form.add(weightLbl, fg);

        fg.gridx = 1; fg.anchor = GridBagConstraints.WEST;
        calsTf = stdTextField(360);
        calsTf.setColumns(20);
        Dimension pfSize = calsTf.getPreferredSize();
        calsTf.setMaximumSize(pfSize);    // prevent vertical stretching
        form.add(calsTf, fg);

        fg.gridy++; fg.gridx = 0; fg.gridwidth = 2; fg.anchor = GridBagConstraints.CENTER;
        JButton set = button();
        form.add(set, fg);

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

    private static JButton button(){
        JButton b = new JButton("Log Meal");
        b.setOpaque(false);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setFont(defaultSettings.LABEL_FONT);
        b.addActionListener(e -> {
            try{
                String name = nameTf.getText();
                int cals = Integer.parseInt(calsTf.getText());
                meal = new Meal(name, cals);
                JOptionPane.showMessageDialog(null, "Meal logged");
                nameTf.setText("");
                calsTf.setText("");
            }catch(Exception ex){
                JOptionPane.showMessageDialog(null, "Meal did not set");
            }
        });
        return b;
    }
}
