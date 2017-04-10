package conlife;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class ConlifeMain extends JFrame {

    /**
     * Starts the application.
     *
     * @param args no required or optional args.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConlifeMain().setVisible(true));
    }

    private ConlifeMain() {
        setTitle("Conway's Game of Life (with Concurrency!)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(this);
        getContentPane().add(initComponents());
        setPreferredSize(new Dimension(900, 700));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel initComponents() {
        JPanel panel = new JPanel(new MigLayout("fill", "[grow]", "[shrink][grow][shrink]"));

        JPanel settingPanel = new JPanel(new MigLayout("fill", "[grow]", "[]"));
        settingPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        JLabel rulesLabel = new JLabel("Rules");
        JTextField rulesField = new JTextField();
        settingPanel.add(rulesLabel, "");
        settingPanel.add(rulesField, "growx");


        JPanel gamePanel = new JPanel();

        JPanel buttonsPanel = new JPanel(new MigLayout());

        panel.add(settingPanel, "growx, wrap");
        panel.add(gamePanel, "wrap");
        panel.add(buttonsPanel, "wrap");

        return panel;
    }
}
