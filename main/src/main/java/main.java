import javax.swing.*;
import java.io.*;
import javax.swing.SwingUtilities;

public class main {
    public static void main(String[] args) {
        // Always start Swing apps on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            loginPage login = new loginPage();
            login.setVisible(true);
        });
    }
}

/*public class main {
    static JFrame frame;

    public static void main(String[] args) {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setLayout(null);
        frame.setVisible(true);

        Ebutton e = new Ebutton();
        frame.add(e);

    }
}*/