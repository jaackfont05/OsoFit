import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;

public class UserPlanStepDetailPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final int stepOrder;
    private final JFrame parent;

    public UserPlanStepDetailPage(user currentUser,
                                  MySQLDatabaseConnector db,
                                  int programId,
                                  int stepOrder,
                                  JFrame parent) {
        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.stepOrder = stepOrder;
        this.parent = parent;

        defaultSettings.setDefault(this);
        setTitle("OsoFit — Step Details");
        setLayout(new BorderLayout(10, 10));
        setSize(780, 580);                // same as UserSessionDetailPage
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(defaultSettings.BACKGROUND_COLOR);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel headerTitle = new JLabel("Step Details", SwingConstants.CENTER);
        headerTitle.setForeground(defaultSettings.TEXT_COLOR);
        headerTitle.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(headerTitle, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        top.add(titleWrap, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(10, 20, 10, 20));
        add(center, BorderLayout.CENTER);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(defaultSettings.TEXT_COLOR);
        area.setBackground(defaultSettings.BACKGROUND_COLOR);
        area.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 1, true));

        JScrollPane scroll = new JScrollPane(area);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 1, true));

        center.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);
        JButton backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        backBtn.addActionListener(e -> dispose());
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);

        loadStep(area, headerTitle);
    }

    private void loadStep(JTextArea area, JLabel headerTitle) {
        String sql =
                "SELECT step_order, title, instructions, " +
                        "       target_duration_minutes, target_calories " +
                        "FROM program_step " +
                        "WHERE program_id = ? AND step_order = ?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);
            ps.setInt(2, stepOrder);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    area.setText("Step not found.");
                    return;
                }

                int order = rs.getInt("step_order");
                String title = rs.getString("title");
                String instructions = rs.getString("instructions");
                Integer mins = (Integer) rs.getObject("target_duration_minutes");
                Integer cals = (Integer) rs.getObject("target_calories");

                headerTitle.setText("Step " + order + " — " + (title == null ? "" : title));

                StringBuilder sb = new StringBuilder();
                sb.append("Step #").append(order).append("\n");
                sb.append("Title: ").append(title == null ? "—" : title).append("\n\n");
                if (mins != null) {
                    sb.append("Target duration (min): ").append(mins).append("\n");
                }
                if (cals != null) {
                    sb.append("Target calories: ").append(cals).append("\n");
                }
                sb.append("\nInstructions:\n");
                sb.append(instructions == null || instructions.isBlank()
                        ? "—"
                        : instructions);

                area.setText(sb.toString());
                area.setCaretPosition(0);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            area.setText("Error loading step details:\n" + ex.getMessage());
        }
    }

    private JButton stdOutlinedButton(String text, Color borderColor) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(borderColor, 2, true));
        return b;
    }
}
