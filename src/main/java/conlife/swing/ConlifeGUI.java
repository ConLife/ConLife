package conlife.swing;

import conlife.*;
import conlife.utils.Lif1_5Reader;
import net.miginfocom.swing.MigLayout;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class ConlifeGUI extends JFrame {

    private static enum MouseState {
        DRAWING_ON, DRAWING_OFF, NOT_DRAWING
    }

    private static final Pattern rulesPattern = Pattern.compile("B\\d+\\/S\\d+");

    /**
     * Starts the application.
     *
     * @param args no required or optional args.
     */
    public static void main(String[] args) throws Exception {
        // This is required to fix a Swing related bug in the JDK that causes an exception in our program on startup.
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        //This class simply extends a JDialog and contains an image and a jlabel (Please wait)

        final ConlifeGUI gui = showLoadingDialog().get();
        SwingUtilities.invokeLater(() -> {
            gui.setVisible(true);
        });
    }

    private static SwingWorker<ConlifeGUI, Void> showLoadingDialog() {
        SwingWorker<ConlifeGUI, Void> gameCreator = new SwingWorker<ConlifeGUI, Void>() {
            @Override
            protected ConlifeGUI doInBackground() throws Exception {
                GameState gameState = GameState.createNewGame();
                return new ConlifeGUI(gameState);
            }
        };

        final JDialog dialog = new JDialog((Frame) null, "Loading Game");

        gameCreator.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("state")) {
                if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                    dialog.dispose();
                }
            }
        });
        gameCreator.execute();

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(new JLabel("Please wait..."), BorderLayout.PAGE_START);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return gameCreator;
    }

    private JPanel gamePanel;
    private JTextField stepField;
    private JButton playButton;
    private JButton stepButton;
    private JTextField rulesField;
    private JMenuItem clearBoardItem;
    private JMenuItem randomBoardItem;
    private JMenuItem loadItem;
    private JFileChooser fileChooser = new JFileChooser(".");
    private final CellComponent[][] board = new CellComponent[(int) GameState.DEFAULT_BOARD_SIZE.getHeight()][(int) GameState.DEFAULT_BOARD_SIZE.getWidth()];
    private int boardLeftInset = 0, boardTopInset = 0;
    private int boardWidth = 0, boardHeight = 0;
    private MouseState drawing = MouseState.NOT_DRAWING;
    private GameState gameState;
    private Set<CellComponent> cellsThatChangedState = new HashSet<>();

    private GameLoop gameLoop = new GameLoop(this);
    private ExecutorService gameThread = Executors.newSingleThreadExecutor();
    private ExecutorService workerThread = Executors.newSingleThreadExecutor();

    private final Random random = new Random();

    private ConlifeGUI(GameState gameState) {
        this.gameState = gameState;
        setTitle("Conway's Game of Life (with Concurrency!)");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(this);
        try {
            getContentPane().add(initComponents());
        } catch (ParseException e) {
            System.err.println("Could not create the GUI");
            System.exit(1);
        }
        initMenuBar();
        pack();
        setLocationRelativeTo(null);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        loadItem = new JMenuItem("Load");
        loadItem.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                Lif1_5Reader reader;
                try {
                    reader = Lif1_5Reader.fromFile(new Dimension(gameState.getBoardWidth(), gameState.getBoardHeight()), file);
                } catch (Exception exp) {
                    JOptionPane.showMessageDialog(this, exp, "Error loading file", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean[][] loadedBoard = reader.getBoard();
                for (int i = 0; i < (int) GameState.DEFAULT_BOARD_SIZE.getWidth(); i++) {
                    for (int j = 0; j < (int) GameState.DEFAULT_BOARD_SIZE.getHeight(); j++) {
                        CellComponent cell = board[j][i];
                        if (cell.setAlive(loadedBoard[j][i])) {
                            cellsThatChangedState.add(cell);
                        }
                    }
                }
                gamePanel.repaint();
            }
        });
        fileMenu.add(loadItem);
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        clearBoardItem = new JMenuItem("Clear Board");
        clearBoardItem.addActionListener(e -> {
            for (int i = 0; i < (int) GameState.DEFAULT_BOARD_SIZE.getWidth(); i++) {
                for (int j = 0; j < (int) GameState.DEFAULT_BOARD_SIZE.getHeight(); j++) {
                    CellComponent cell = board[j][i];
                    if (cell.setAlive(false)) {
                        cellsThatChangedState.add(cell);
                    }
                }
            }
            gamePanel.repaint();
        });
        editMenu.add(clearBoardItem);
        randomBoardItem = new JMenuItem("Randomize");
        randomBoardItem.addActionListener(e -> {
            for (int i = 0; i < (int) GameState.DEFAULT_BOARD_SIZE.getWidth(); i++) {
                for (int j = 0; j < (int) GameState.DEFAULT_BOARD_SIZE.getHeight(); j++) {
                    CellComponent cell = board[j][i];
                    if (cell.setAlive(random.nextBoolean())) {
                        cellsThatChangedState.add(cell);
                    }
                }
            }
            gamePanel.repaint();
        });
        editMenu.add(randomBoardItem);
        menuBar.add(editMenu);

        this.setJMenuBar(menuBar);
    }

    private JPanel initComponents() throws ParseException {
        JPanel panel = new JPanel(new MigLayout("fill", "[grow]", "[shrink][grow][shrink]"));

        JPanel settingPanel = new JPanel(new MigLayout("fill", "[shrink][grow][shrink][grow]", "[]"));
        settingPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel rulesLabel = new JLabel("Rules");
        MaskFormatter formatter = new MaskFormatter("B##/S##");
        rulesField = new JTextField(GameState.DEFAULT_RULES_STRING);
        rulesField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { }

            @Override
            public void focusLost(FocusEvent e) {
                final String text = rulesField.getText();
                gameThread.submit(() -> {
                    try {
                        Rules rules = Rules.parseRules(text);
                        gameState.setRules(rules);
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
        stepField.setEditable(false);
        settingPanel.add(stepLabel, "");
        settingPanel.add(stepField, "growx");

        gamePanel = new JPanel(null);
        gamePanel.setBorder(BorderFactory.createEtchedBorder());
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameLoop.running.get()) {
                    return;
                }
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
                if (gameLoop.running.get()) {
                    return;
                }
                workerThread.submit(() -> { drawing = MouseState.NOT_DRAWING; });
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (gameLoop.running.get()) {
                    return;
                }
                workerThread.submit(() -> { drawing = MouseState.NOT_DRAWING; });
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (gameLoop.running.get()) {
                    return;
                }
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

        JPanel buttonsPanel = new JPanel(new MigLayout("fill", "[grow][][grow][grow]", ""));
        buttonsPanel.setBorder(BorderFactory.createEtchedBorder());

        playButton = new JButton("Play");
        playButton.addActionListener(e -> togglePlay());
        buttonsPanel.add(playButton, "center, growx");
        JLabel speedLabel = new JLabel("Speed");
        buttonsPanel.add(speedLabel);
        final JSlider speedSlider = new JSlider(SwingConstants.HORIZONTAL, 1000 / GameLoop.MAX_TIME_PER_GAME_LOOP,
                1000 / GameLoop.MIN_TIME_PER_GAME_LOOP, 1000 / GameLoop.DEFAULT_TIME_PER_GAME_LOOP);
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!speedSlider.getValueIsAdjusting()) {
                    double lps = speedSlider.getValue();
                    if (lps == 0) {
                        lps = .5D;
                    }
                    int newSpeed = (int) (1000 / lps);
                    gameLoop.timePerLoopMs.set(newSpeed);
                }
            }
        });
        buttonsPanel.add(speedSlider, "center, growx");
        stepButton = new JButton("Step");
        stepButton.addActionListener(e -> {
            if (gameLoop.running.get()) {
                return;
            }
            workerThread.submit(this::step);
        });
        buttonsPanel.add(stepButton, "center, growx");

        panel.add(settingPanel, "growx, wrap");
        panel.add(gamePanel, "growx, growy, wrap");
        panel.add(buttonsPanel, "growx");

        // This magic number represent the additional space used by the UI surrounding the game board and exist due to
        // the developers lack of knowledge of how to programatically determine that space.
        this.setPreferredSize(new Dimension(CellComponent.CELL_SIZE * (int) GameState.DEFAULT_BOARD_SIZE.getWidth() + 35,
                CellComponent.CELL_SIZE * (int) GameState.DEFAULT_BOARD_SIZE.getHeight() + 171)); //148
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

    private void togglePlay() {
        final boolean running = !gameLoop.running.getAndSet(!gameLoop.running.get());
        if (running) {
            playButton.setText("Pause");
            stepButton.setEnabled(false);
            rulesField.setEnabled(false);
            clearBoardItem.setEnabled(false);
            randomBoardItem.setEnabled(false);
            loadItem.setEnabled(false);
        } else {
            playButton.setText("Play");
            stepButton.setEnabled(true);
            rulesField.setEnabled(true);
            clearBoardItem.setEnabled(true);
            randomBoardItem.setEnabled(true);
            loadItem.setEnabled(true);
        }
        workerThread.submit(gameLoop);
    }

    void step() {
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
