import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

//Add menBar object for your own main page UI functionality
public class menuBar extends JPanel {
    private JFrame owner;       // which page has a menu bar
    private user currentUser;   // logged-in user

    //!!! add your menu button variable here
    private final JButton mainBtn;
    private final JButton addExerciseBtn;

    //After we know the owner, we can decide which page need dispose when we click
    // a button on the menu bar.
    public menuBar(JFrame owner, user currentUser) {
        this.owner = owner;
        this.currentUser = currentUser;

        setBackground(defaultSettings.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1, 48));
        setBorder(new MatteBorder(0, 0, 2, 0, new Color(220, 0, 0))); // thin red line under bar

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row.setOpaque(false);
        //!!!declare your menu button variable by using otherFeatureButton()
        mainBtn = mainPageBtn("Main Page");  //
        addExerciseBtn = otherFeatureBtn("Add Exercise",
                new Color(200, 40, 40)); // you can choose different color if you want :)

        //HelpMe: if someone can add a short sound when user is clicking a menu button, that would be perfect!
        mainBtn.addActionListener(e -> {
            // If we're already on main, just bring it to front
            if (owner instanceof mainPage) {
                owner.toFront(); // ensures it appears on top just in case
                owner.requestFocus(); // keep your current input field is the same if click the btn on menu
                return;
            }
            if (owner != null) owner.dispose();           // close current page
            new mainPage(currentUser).setVisible(true);   // open main page
        });

        addExerciseBtn.addActionListener(e -> {
            //add your customized actionListener, don't forget
            //check whether to dispose current page/ui if needed
        });
        //!!!add your actionListener for your own button here

        //!!!don't forget add your btn in row here
        row.add(mainBtn);
        row.add(addExerciseBtn);
        add(row, BorderLayout.CENTER);
    }

    // Btn helpers
    private JButton mainPageBtn(String text) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);                // text color
        b.setBackground(defaultSettings.BACKGROUND_COLOR);          // button fill (black)
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true)); // green outline
        return b;
    }

    private JButton otherFeatureBtn(String text, Color borderColor) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(borderColor, 2, true));          // red outline
        return b;
    }


}

