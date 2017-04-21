package conlife;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import net.miginfocom.swing.MigLayout;

import java.util.Collection;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.Set;
import java.util.regex.Pattern;

public class ConlifeMain extends JFrame {

    private static enum MouseState {
        DRAWING_ON, DRAWING_OFF, NOT_DRAWING
    }

    private static final Pattern rulesPattern = Pattern.compile("B\\d+\\/S\\d+");

    /**
     * Starts the application.
     *
     * @param args no required or optional args.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConlifeMain().setVisible(true));
    }

    private final CellComponent[][] board = new CellComponent[100][100];
    private int boardLeftInset = 0, boardTopInset = 0;
    private int boardWidth = 0, boardHeight = 0;
    private MouseState drawing = MouseState.NOT_DRAWING;
    private GameState gameState = GameState.createNewGame();
    private Set<CellComponent> cellsThatChangedState = new HashSet<>();

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
        //setPreferredSize(new Dimension(900, 700));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel initComponents() throws ParseException {
        JPanel panel = new JPanel(new MigLayout("fill", "[grow]", "[shrink][grow][shrink]"));

        JPanel settingPanel = new JPanel(new MigLayout("fill", "[shrink][grow][shrink][grow]", "[][]"));
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
                    GameState.updateDefaultRules(rules);
                } catch (ParseException | Rules.RulesException ignore) {
                    SwingUtilities.invokeLater(() -> {rulesField.setText(GameState.DEFAULT_RULES_STRING);});
                }
            }
        });
        settingPanel.add(rulesLabel, "");
        settingPanel.add(rulesField, "growx");

        JLabel maxStepsLabel = new JLabel("Max Steps");
        final JTextField maxStepsField = new JTextField("-1");
        rulesField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { }

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    String text = maxStepsField.getText();
                    Integer.parseInt(text);
                } catch (NumberFormatException ignore) {
                    SwingUtilities.invokeLater(() -> {rulesField.setText("-1");});
                }
            }
        });
        settingPanel.add(maxStepsLabel, "");
        settingPanel.add(maxStepsField, "growx, wrap");

        JLabel fpsLabel = new JLabel("FPS");
        JTextField fpsField = new JTextField("0");
        fpsField.setEnabled(false);
        settingPanel.add(fpsLabel, "");
        settingPanel.add(fpsField, "growx");

        JLabel stepLabel = new JLabel("Current Step");
        JTextField stepField = new JTextField("0");
        stepField.setEnabled(false);
        settingPanel.add(stepLabel, "");
        settingPanel.add(stepField, "growx");

        DrawPanel gamePanel = new DrawPanel();
        gamePanel.setBorder(BorderFactory.createEtchedBorder());
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                CellComponent cell = getCell(e.getPoint());
                if (cell == null) {
                    return;
                }
                if (cell.isAlive()) {
                    drawing = MouseState.DRAWING_OFF;
                    if (cell.setAlive(false)) {
                        cellsThatChangedState.add(cell);
                    }
                } else {
                    drawing = MouseState.DRAWING_ON;
                    if (cell.setAlive(true)) {
                        cellsThatChangedState.add(cell);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drawing = MouseState.NOT_DRAWING;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                drawing = MouseState.NOT_DRAWING;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawing == MouseState.NOT_DRAWING) {
                    return;
                }
                CellComponent cell = getCell(e.getPoint());
                if (cell == null) {
                    return;
                }
                if (drawing == MouseState.DRAWING_ON) {
                    if (cell.setAlive(true)) {
                        cellsThatChangedState.add(cell);
                    }
                } else {
                    if (cell.setAlive(false)) {
                        cellsThatChangedState.add(cell);
                    }
                }
            }
        };
        gamePanel.addMouseMotionListener(mouseAdapter);
        gamePanel.addMouseListener(mouseAdapter);

        //CellComponent cellComponent = new CellComponent();
        //gamePanel.add(cellComponent);

        Insets insets = gamePanel.getInsets();
        boardLeftInset = insets.left;
        boardTopInset = insets.top;
        boardWidth = 100 * CellComponent.CELL_SIZE;
        boardHeight = 100 * CellComponent.CELL_SIZE;

        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                board[j][i] = new CellComponent(i, j);
                gamePanel.add(board[j][i]);
                Dimension size = board[j][i].getPreferredSize();
                board[j][i].setBounds(i * CellComponent.CELL_SIZE + insets.left, j * CellComponent.CELL_SIZE + insets.top, size.width, size.height);
            }
        }



//        Dimension size = cellComponent.getPreferredSize();
//        cellComponent.setBounds(25 + insets.left, 5 + insets.top,
//                size.width, size.height);

//        GLProfile glprofile = GLProfile.getDefault();
//        GLCapabilities glcapabilities = new GLCapabilities(glprofile);
//        GLJPanel gamePanel = new GLJPanel(glcapabilities);
//
//        gamePanel.addGLEventListener( new GLEventListener() {
//            @Override
//            public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height ) {
//                OneTriangle.setup( glAutoDrawable.getGL().getGL2(), width, height );
//            }
//
//            @Override
//            public void init( GLAutoDrawable glAutoDrawable ) {}
//
//            @Override
//            public void dispose( GLAutoDrawable glAutoDrawable ) {}
//
//            @Override
//            public void display( GLAutoDrawable glAutoDrawable ) {
//                OneTriangle.render( glAutoDrawable.getGL().getGL2(), glAutoDrawable.getSurfaceWidth(), glAutoDrawable.getSurfaceHeight() );
//            }
//        });

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
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step();
            }
        });
        buttonsPanel.add(stepButton, "center, growx");

        panel.add(settingPanel, "growx, wrap");
        panel.add(gamePanel, "growx, growy, wrap");
        panel.add(buttonsPanel, "growx");

        insets = gamePanel.getInsets();
        this.setPreferredSize(new Dimension(CellComponent.CELL_SIZE * 100 + 50, CellComponent.CELL_SIZE * 100 + 200));
        return panel;
    }

    CellComponent getCell(Point p) {
        if (p.x < boardLeftInset || p.y < boardTopInset || p.x >= boardLeftInset + boardWidth || p.y >= boardTopInset + boardHeight) {
            return null;
        }
        int x = (p.x - boardLeftInset) / CellComponent.CELL_SIZE;
        int y = (p.y - boardTopInset) / CellComponent.CELL_SIZE;
        return board[y][x];
    }

    private void step() {
        for (CellComponent cellComponent : cellsThatChangedState) {
            Cell cell = gameState.getCell(cellComponent.getCellX(), cellComponent.getCellY());
            cell.setCurrentlyAlive(cellComponent.isAlive());
        }
        gameState.processGameStep();
        Collection<Cell> cellUpdates = gameState.getCellsThatChangedState();
        for (Cell cell : cellUpdates) {
            CellComponent cellComponent = board[cell.getY()][cell.getX()];
            cellComponent.setAlive(cell.isAlive());
        }
    }
}
