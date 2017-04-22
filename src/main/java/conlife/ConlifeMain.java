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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    private DrawPanel gamePanel;
    private JTextField stepField;
    private final CellComponent[][] board = new CellComponent[(int) GameState.DEFAULT_BOARD_SIZE.getHeight()][(int) GameState.DEFAULT_BOARD_SIZE.getWidth()];
    private int boardLeftInset = 0, boardTopInset = 0;
    private int boardWidth = 0, boardHeight = 0;
    private MouseState drawing = MouseState.NOT_DRAWING;
    private GameState gameState = GameState.createNewGame();
    private Set<CellComponent> cellsThatChangedState = new HashSet<>();

    private ExecutorService gameThread = Executors.newSingleThreadExecutor();
    private ExecutorService workerThread = Executors.newSingleThreadExecutor();

    private ConlifeMain() {
        setTitle("Conway's Game of Life (with Concurrency!)");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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

        JPanel settingPanel = new JPanel(new MigLayout("fill", "[shrink][grow][shrink][grow]", "[]"));
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
                final String text = rulesField.getText();
                gameThread.submit(() -> {
                    try {
                        Rules rules = Rules.parseRules(text);
                        GameState.updateDefaultRules(rules);
                    } catch (ParseException | Rules.RulesException ignore) {
                        SwingUtilities.invokeLater(() -> {rulesField.setText(GameState.DEFAULT_RULES_STRING);});
                    }
                });
            }
        });
        settingPanel.add(rulesLabel, "");
        settingPanel.add(rulesField, "growx");

        JLabel stepLabel = new JLabel("Current Step");
        stepField = new JTextField("0");
        stepField.setEnabled(false);
        settingPanel.add(stepLabel, "");
        settingPanel.add(stepField, "growx");

        gamePanel = new DrawPanel();
        gamePanel.setBorder(BorderFactory.createEtchedBorder());
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                final CellComponent cell = getCell(e.getPoint());
                if (cell == null) {
                    return;
                }
                workerThread.submit(() -> {
                    if (cell.isAlive()) {
                        drawing = MouseState.DRAWING_OFF;
                        if (cell.setAlive(false)) {
                            cellsThatChangedState.add(cell);
                            SwingUtilities.invokeLater(cell::repaint);
                        }
                    } else {
                        drawing = MouseState.DRAWING_ON;
                        if (cell.setAlive(true)) {
                            cellsThatChangedState.add(cell);
                            SwingUtilities.invokeLater(cell::repaint);
                        }
                    }
                });
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                workerThread.submit(() -> { drawing = MouseState.NOT_DRAWING; });

            }

            @Override
            public void mouseExited(MouseEvent e) {
                workerThread.submit(() -> { drawing = MouseState.NOT_DRAWING; });
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                final CellComponent cell = getCell(e.getPoint());
                if (cell == null) {
                    return;
                }
                workerThread.submit(() -> {
                    if (drawing == MouseState.NOT_DRAWING) {
                        return;
                    }
                    if (drawing == MouseState.DRAWING_ON) {
                        if (cell.setAlive(true)) {
                            cellsThatChangedState.add(cell);
                            SwingUtilities.invokeLater(cell::repaint);
                        }
                    } else {
                        if (cell.setAlive(false)) {
                            cellsThatChangedState.add(cell);
                            SwingUtilities.invokeLater(cell::repaint);
                        }
                    }
                });

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

        for (int i = 0; i < (int) GameState.DEFAULT_BOARD_SIZE.getWidth(); i++) {
            for (int j = 0; j < (int) GameState.DEFAULT_BOARD_SIZE.getHeight(); j++) {
                board[j][i] = new CellComponent(i, j);
                gamePanel.add(board[j][i]);
                Dimension size = board[j][i].getPreferredSize();
                board[j][i].setBounds(i * CellComponent.CELL_SIZE + insets.left, j * CellComponent.CELL_SIZE + insets.top, size.width, size.height);
            }
        }

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
        stepButton.addActionListener(e -> workerThread.submit(this::step));
        buttonsPanel.add(stepButton, "center, growx");

        panel.add(settingPanel, "growx, wrap");
        panel.add(gamePanel, "growx, growy, wrap");
        panel.add(buttonsPanel, "growx");

        // This magic number represent the additional space used by the UI surrounding the game board and exist due to
        // the developers lack of knowledge of how to programatically determine that space.
        this.setPreferredSize(new Dimension(CellComponent.CELL_SIZE * (int) GameState.DEFAULT_BOARD_SIZE.getWidth() + 35,
                CellComponent.CELL_SIZE * (int) GameState.DEFAULT_BOARD_SIZE.getHeight() + 148));
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
        final CellUpdate[] cellUpdatesFromUI = new CellUpdate[cellsThatChangedState.size()];
        int i = 0;
        for (CellComponent cellComponent : cellsThatChangedState) {
            cellUpdatesFromUI[i] = new CellUpdate(cellComponent.getCellX(), cellComponent.getCellY(), cellComponent.isAlive());
            i++;
        }

        final Future<CellUpdate[]> futureCellUpdates = gameThread.submit(() -> {
            for (CellUpdate cellUpdate : cellUpdatesFromUI) {
                Cell cell = gameState.getCell(cellUpdate.getX(), cellUpdate.getY());
                cell.setCurrentlyAlive(cellUpdate.isAlive());
            }
            gameState.processGameStep();
            Collection<Cell> cellUpdates = gameState.getCellsThatChangedState();
            CellUpdate[] cellUpdatesFromGame = new CellUpdate[cellUpdates.size()];
            int j = 0;
            for (Cell cell : cellUpdates) {
                cellUpdatesFromGame[j] = new CellUpdate(cell.getX(), cell.getY(), cell.isAlive());
                j++;
            }
            return cellUpdatesFromGame;
        });


        try {
            CellUpdate[] cellUpdatesFromGame = futureCellUpdates.get();
            for (CellUpdate cell : cellUpdatesFromGame) {
                CellComponent cellComponent = board[cell.getY()][cell.getX()];
                cellComponent.setAlive(cell.isAlive());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        SwingUtilities.invokeLater(() -> {
            gamePanel.repaint();
            stepField.setText(Integer.toString(gameState.getCurrentStep()));
        });
    }
}
