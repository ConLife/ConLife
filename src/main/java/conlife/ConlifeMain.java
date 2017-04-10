package conlife;

import javax.swing.*;

public class ConlifeMain extends JFrame {

    /**
     * Starts the application.
     *
     * @param args no required or optional args.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConlifeMain().setVisible(true));
    }
}
