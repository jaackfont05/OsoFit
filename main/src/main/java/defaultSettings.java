/* this is the class mainly functionality is to add standard settings for
Jframe UI to make each interface consistency
*/

import javax.swing.*;
import java.awt.*;

public class defaultSettings {
    public static final Color BACKGROUND_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = new Color(0, 255, 0); // green
    public static final Color BORDER_COLOR = new Color(0, 128, 0); // darker green
    //Below Font is optional to use, but recommend each main page for a specific functionality to use this Font
    public static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 40);
    public static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 18);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 16);

    //Note: you should call this function before you create your customized UI, otherwise may have bugs.
    public static void setDefault(JFrame frame) {
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        frame.setSize(1200, 800);
        //frame.setExtendedState(JFrame.MAXIMIZED_BOTH); //maybe we can discuss whether to use full screen or not later
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null); // where potential bug come from
    }

}
