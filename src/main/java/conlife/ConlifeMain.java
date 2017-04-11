package conlife;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
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
        settingPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        JLabel rulesLabel = new JLabel("Rules");
        JTextField rulesField = new JTextField();
        settingPanel.add(rulesLabel, "");
        settingPanel.add(rulesField, "growx");

        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        GLJPanel gamePanel = new GLJPanel( glcapabilities );

        gamePanel.addGLEventListener( new GLEventListener() {

            @Override
            public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height ) {
                OneTriangle.setup( glAutoDrawable.getGL().getGL2(), width, height );
            }

            @Override
            public void init( GLAutoDrawable glAutoDrawable ) {}

            @Override
            public void dispose( GLAutoDrawable glAutoDrawable ) {}

            @Override
            public void display( GLAutoDrawable glAutoDrawable ) {
                OneTriangle.render( glAutoDrawable.getGL().getGL2(), glAutoDrawable.getSurfaceWidth(), glAutoDrawable.getSurfaceHeight() );
            }
        });

        JPanel buttonsPanel = new JPanel(new MigLayout());
        buttonsPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        JButton startButton = new JButton("Start");
        buttonsPanel.add(startButton, "center");

        panel.add(settingPanel, "growx, wrap");
        panel.add(gamePanel, "growx, growy, wrap");
        panel.add(buttonsPanel, "growx");

        return panel;
    }
}
