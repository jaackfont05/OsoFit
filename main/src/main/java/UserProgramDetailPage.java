import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProgramDetailPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int programId;
    private final UserProgramSearchPage parent;

    public UserProgramDetailPage(user currentUser,
                                 MySQLDatabaseConnector db,
                                 int programId,
                                 UserProgramSearchPage parent) {
        this.currentUser = currentUser;
        this.db = db;
        this.programId = programId;
        this.parent = parent;

        // Apply shared look, but then override close behavior and size
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Program Details");
        setLayout(new BorderLayout(10, 10));
        setSize(750, 600);           // smaller window
        setLocationRelativeTo(null); // center on screen
        // <<< IMPORTANT: only close THIS window, not the whole app >>>
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ===== NORTH: title + red line (no menu bar here) =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel title = new JLabel("Program Details", SwingConstants.CENTER);
        title.setForeground(defaultSettings.TEXT_COLOR);
        title.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(title, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: details in a nice two-column layout =====
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(10, 20, 10, 20));
        add(center, BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setOpaque(false); // let background show through

        // Scroll pane needs explicit dark styling
        JScrollPane detailsScroll = new JScrollPane(detailsPanel);
        detailsScroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        detailsScroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        detailsScroll.setBorder(null);
        center.add(detailsScroll, BorderLayout.CENTER);

        // Fill the panel with DB data
        loadDetailsInto(detailsPanel, title);

        // ===== SOUTH: back button =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        JButton backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        backBtn.addActionListener(e -> dispose());
        bottom.add(backBtn);

        center.add(bottom, BorderLayout.SOUTH);
    }

    private void loadDetailsInto(JPanel detailsPanel, JLabel headerTitle) {
        String sql =
                "SELECT p.program_id, p.program_type, p.name, p.description, " +
                        "       p.fitness_level, p.required_equipment, " +
                        "       p.avg_session_minutes, p.suggested_freq_per_week, " +
                        "       p.class_start_date, p.class_end_date, " +
                        "       p.class_days_of_week, p.class_session_minutes, " +
                        "       p.class_num_weeks, p.max_participants, " +
                        "       p.registration_end_date, " +
                        "       u.userN AS trainer_name, u.email AS trainer_email " +
                        "FROM exercise_program p " +
                        "JOIN users u ON p.trainer_email = u.email " +
                        "WHERE p.program_id = ?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, programId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "Program not found (ID " + programId + ").",
                            "Not Found",
                            JOptionPane.WARNING_MESSAGE);
                    dispose();
                    return;
                }

                String type = rs.getString("program_type");
                String name = rs.getString("name");
                String desc = rs.getString("description");
                String fitness = rs.getString("fitness_level");
                String reqEquip = rs.getString("required_equipment");

                Integer avgMinutes = (Integer) rs.getObject("avg_session_minutes");
                Integer freqPerWeek = (Integer) rs.getObject("suggested_freq_per_week");

                Date start = rs.getDate("class_start_date");
                Date end = rs.getDate("class_end_date");
                String daysOfWeek = rs.getString("class_days_of_week");
                Integer sessionMinutes = (Integer) rs.getObject("class_session_minutes");
                Integer numWeeks = (Integer) rs.getObject("class_num_weeks");
                Integer maxPart = (Integer) rs.getObject("max_participants");
                Date regEnd = rs.getDate("registration_end_date");

                String trainerName = rs.getString("trainer_name");
                String trainerEmail = rs.getString("trainer_email");

                // Update header title with program name
                headerTitle.setText("Program Details — " + name);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.NORTHWEST;
                gbc.insets = new Insets(4, 4, 4, 4);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 0;
                gbc.weighty = 0;

                // rows
                addRow(detailsPanel, gbc, "Program ID:", String.valueOf(programId));
                addRow(detailsPanel, gbc, "Type:", safe(type));
                addRow(detailsPanel, gbc, "Name:", safe(name));
                addRow(detailsPanel, gbc, "Trainer:", safe(trainerName) +
                        (trainerEmail != null ? " (" + trainerEmail + ")" : ""));
                addRow(detailsPanel, gbc, "Fitness level:", safe(fitness));
                addRow(detailsPanel, gbc, "Required equipment:", safe(reqEquip));

                if ("SELF_PACED".equalsIgnoreCase(type)) {
                    addRow(detailsPanel, gbc, "Avg session length (min):",
                            avgMinutes == null ? "—" : avgMinutes.toString());
                    addRow(detailsPanel, gbc, "Suggested freq / week:",
                            freqPerWeek == null ? "—" : freqPerWeek.toString());
                } else if ("CLASS".equalsIgnoreCase(type)) {
                    addRow(detailsPanel, gbc, "Class start date:",
                            start == null ? "—" : start.toString());
                    addRow(detailsPanel, gbc, "Class end date:",
                            end == null ? "—" : end.toString());
                    addRow(detailsPanel, gbc, "Days of week:", safe(daysOfWeek));
                    addRow(detailsPanel, gbc, "Session length (min):",
                            sessionMinutes == null ? "—" : sessionMinutes.toString());
                    addRow(detailsPanel, gbc, "Number of weeks:",
                            numWeeks == null ? "—" : numWeeks.toString());
                    addRow(detailsPanel, gbc, "Max participants:",
                            maxPart == null ? "—" : maxPart.toString());
                    addRow(detailsPanel, gbc, "Registration deadline:",
                            regEnd == null ? "—" : regEnd.toString());
                }

                // description as a multi-line area at the end
                addMultiline(detailsPanel, gbc, "Description:", safe(desc));

                // filler so layout stretches nicely
                gbc.gridx = 0;
                gbc.gridwidth = 2;
                gbc.weighty = 1.0;
                gbc.gridy++;
                detailsPanel.add(Box.createVerticalGlue(), gbc);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading program details:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ====== layout helpers ======

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(defaultSettings.TEXT_COLOR);

        JLabel val = new JLabel(value);
        val.setForeground(defaultSettings.TEXT_COLOR);

        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(val, gbc);
    }

    private void addMultiline(JPanel panel, GridBagConstraints gbc, String label, String text) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(defaultSettings.TEXT_COLOR);

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(defaultSettings.TEXT_COLOR);
        area.setBackground(defaultSettings.BACKGROUND_COLOR);
        area.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 1, true));

        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        JScrollPane sp = new JScrollPane(area);
        sp.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        sp.setBackground(defaultSettings.BACKGROUND_COLOR);
        sp.setBorder(null);

        panel.add(sp, gbc);
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
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
