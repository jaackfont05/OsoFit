import javax.swing.*;
import java.io.*;
import javax.swing.SwingUtilities;


public class main {
    public static void main(String[] args) {
        // Always start Swing apps on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MySQLDatabaseConnector db = new MySQLDatabaseConnector();
            db.connect();
            loginPage login = new loginPage(db);
            login.setVisible(true);
        });
    }
}
