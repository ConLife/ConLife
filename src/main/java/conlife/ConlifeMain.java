package conlife;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.util.regex.Pattern;

public class ConlifeMain extends JFrame {

    private static final Pattern rulesPattern = Pattern.compile("B\\d+\\/S\\d+");

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
        try {
            getContentPane().add(initComponents());
        } catch (ParseException e) {
            System.err.println("Could not create the GUI");
            System.exit(1);
        }
        setPreferredSize(new Dimension(900, 700));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel initComponents() throws ParseException {
        JPanel panel = new JPanel(new MigLayout("fill", "[grow]", "[shrink][grow][shrink]"));

        JPanel settingPanel = new JPanel(new MigLayout("fill", "[shrink][grow]", "[]"));
        settingPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel rulesLabel = new JLabel("Rules");
        MaskFormatter formatter = new MaskFormatter("B##/S##");
        final JFormattedTextField rulesField = new JFormattedTextField(formatter);
        rulesField.setText(GameState.DEFAULT_RULES_STRING);
        rulesField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { }

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    Rules rules = Rules.parseRules(rulesField.getText());
                    GameState.updateRules(rules);
                } catch (ParseException | Rules.RulesException ignore) {
                    SwingUtilities.invokeLater(() -> {rulesField.setText(GameState.DEFAULT_RULES_STRING);});
                }
            }
        });
        settingPanel.add(rulesLabel, "");
        settingPanel.add(rulesField, "growx");

        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities(glprofile);
        GLJPanel gamePanel = new GLJPanel(glcapabilities);

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

        JPanel buttonsPanel = new JPanel(new MigLayout("fill", "[grow][grow][grow][grow]", ""));
        buttonsPanel.setBorder(BorderFactory.createEtchedBorder());

        JButton startButton = new JButton("Start");
        buttonsPanel.add(startButton, "center, growx");
        JButton pauseButton = new JButton("Pause");
        pauseButton.setEnabled(false);
        buttonsPanel.add(pauseButton, "center, growx");
        JButton stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        buttonsPanel.add(stopButton, "center, growx");
        JButton stepButton = new JButton("Step");
        stepButton.setEnabled(false);
        buttonsPanel.add(stepButton, "center, growx");

        panel.add(settingPanel, "growx, wrap");
        panel.add(gamePanel, "growx, growy, wrap");
        panel.add(buttonsPanel, "growx");

        return panel;
    }


}
