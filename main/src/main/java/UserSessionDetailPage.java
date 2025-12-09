import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

public class UserSessionDetailPage extends JFrame {

    private final user currentUser;
    private final MySQLDatabaseConnector db;
    private final int sessionId;
    private final JFrame parent;

    public UserSessionDetailPage(user currentUser,
                                 MySQLDatabaseConnector db,
                                 int sessionId,
                                 JFrame parent) {
        this.currentUser = currentUser;
        this.db = db;
        this.sessionId = sessionId;
        this.parent = parent;

        // Apply default look, but override close behavior & size
        defaultSettings.setDefault(this);
        setTitle("OsoFit — Session Details");
        setLayout(new BorderLayout(10, 10));
        setSize(780, 580);
        setLocationRelativeTo(null);

        // only close THIS window, not the whole app
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ===== NORTH: title + red underline =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(defaultSettings.BACKGROUND_COLOR);

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setBackground(defaultSettings.BACKGROUND_COLOR);
        titleWrap.setBorder(new EmptyBorder(8, 12, 0, 12));

        JLabel titleLbl = new JLabel("Session Details", SwingConstants.CENTER);
        titleLbl.setForeground(defaultSettings.TEXT_COLOR);
        titleLbl.setFont(defaultSettings.TITLE_FONT);
        titleWrap.add(titleLbl, BorderLayout.CENTER);

        JPanel redLine = new JPanel();
        redLine.setBackground(new Color(220, 0, 0));
        redLine.setPreferredSize(new Dimension(1, 6));
        titleWrap.add(redLine, BorderLayout.SOUTH);

        topPanel.add(titleWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: main details panel =====
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(defaultSettings.BACKGROUND_COLOR);
        center.setBorder(new EmptyBorder(10, 20, 10, 20));
        add(center, BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(detailsPanel);
        scroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        scroll.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 1, true));
        center.add(scroll, BorderLayout.CENTER);

        // Load data and populate the panel
        loadDetailsInto(detailsPanel, titleLbl);

        // ===== SOUTH: Back button =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        JButton backBtn = stdOutlinedButton("Back", defaultSettings.BORDER_COLOR);
        backBtn.addActionListener(e -> dispose());

        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadDetailsInto(JPanel detailsPanel, JLabel headerTitle) {
        String sql =
                "SELECT s.session_id, s.session_date, s.start_time, s.end_time, " +
                        "       s.status AS session_status, s.notes, " +
                        "       p.program_id, p.name AS program_name, p.program_type, " +
                        "       p.fitness_level, p.required_equipment, " +
                        "       u.userN AS trainer_name, u.email AS trainer_email " +
                        "FROM class_session s " +
                        "JOIN exercise_program p ON s.program_id = p.program_id " +
                        "JOIN users u ON p.trainer_email = u.email " +
                        "WHERE s.session_id = ?";

        try (Connection conn = MySQLDatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // only one parameter now
            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "Session not found (ID " + sessionId + ").",
                            "Not Found",
                            JOptionPane.WARNING_MESSAGE);
                    dispose();
                    return;
                }

                int programId = rs.getInt("program_id");
                String programName = rs.getString("program_name");
                String programType = rs.getString("program_type");
                String fitness = rs.getString("fitness_level");
                String reqEquip = rs.getString("required_equipment");

                String trainerName = rs.getString("trainer_name");
                String trainerEmail = rs.getString("trainer_email");

                Date sessionDate = rs.getDate("session_date");
                Time startTime = rs.getTime("start_time");
                Time endTime   = rs.getTime("end_time");
                String sessionStatus = rs.getString("session_status");
                String notes = rs.getString("notes");

                // Set header with program name
                headerTitle.setText("Session Details — " + safe(programName));

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.NORTHWEST;
                gbc.insets = new Insets(4, 4, 4, 4);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 0;
                gbc.weighty = 0;

                // Program info
                addRow(detailsPanel, gbc, "Program ID:", String.valueOf(programId));
                addRow(detailsPanel, gbc, "Program name:", safe(programName));
                addRow(detailsPanel, gbc, "Program type:", safe(programType));
                addRow(detailsPanel, gbc, "Trainer:",
                        safe(trainerName) + (trainerEmail != null ? " (" + trainerEmail + ")" : ""));
                addRow(detailsPanel, gbc, "Fitness level:", safe(fitness));
                addRow(detailsPanel, gbc, "Required equipment:", safe(reqEquip));

                // Session info
                addRow(detailsPanel, gbc, "Session date:",
                        sessionDate == null ? "—" : sessionDate.toString());
                addRow(detailsPanel, gbc, "Start time:",
                        startTime == null ? "—" : startTime.toString());
                addRow(detailsPanel, gbc, "End time:",
                        endTime == null ? "—" : endTime.toString());
                addRow(detailsPanel, gbc, "Session status:", safe(sessionStatus));

                // NO attendance row anymore

                // Notes as multi-line text area
                addMultiline(detailsPanel, gbc, "Notes:", safe(notes));

                // filler so layout stretches
                gbc.gridx = 0;
                gbc.gridwidth = 2;
                gbc.weighty = 1.0;
                gbc.gridy++;
                detailsPanel.add(Box.createVerticalGlue(), gbc);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading session details:\n" + ex.getMessage(),
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

        JScrollPane areaScroll = new JScrollPane(area);
        areaScroll.getViewport().setBackground(defaultSettings.BACKGROUND_COLOR);
        areaScroll.setBackground(defaultSettings.BACKGROUND_COLOR);
        areaScroll.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 1, true));

        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(areaScroll, gbc);
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
