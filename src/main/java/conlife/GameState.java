package conlife;

import java.awt.Dimension;
import java.text.ParseException;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls all aspects of the mechanics of the game. Most of the operations in this class are not thread safe. However,
 * game steps are performed in a thread safe manner and can be parallelized effectively. A ga
 */
public class GameState {

    public static final Dimension DEFAULT_BOARD_SIZE = new Dimension(100, 100);

    public static final String DEFAULT_RULES_STRING = "B03/S23";

    public static final int DEFAULT_THREAD_COUNT = 4;

    // synchronization of the rules is probably not a concern...
    private static final Object rulesLock = new Object();
    private static Rules defaultRules;

    static {
        try {
            defaultRules = Rules.parseRules(DEFAULT_RULES_STRING);
        } catch (ParseException | Rules.RulesException e) {
            throw new RuntimeException("Initial rules are busted", e);
        }

    }

    static void updateDefaultRules(Rules rules) {
        synchronized (rulesLock) {
            GameState.defaultRules = rules;
        }
    }

    static Rules getDefaultRules() {
        return defaultRules;
    }

    private AtomicInteger currentStep = new AtomicInteger(0);
    private int maxStep = -1;

    private ExecutorService threadPool;

    private Rules rules;
    private final int boardWidth, boardHeight;
    private Cell[][] board;

    final Queue<Cell.CellStateDeterminer> currentCellQueue = new ConcurrentLinkedQueue<>();
    final Queue<Callable<Void>> cellUpdateQueue = new ConcurrentLinkedQueue<>();
    final Queue<Callable<Void>> nextStepCellQueue = new ConcurrentLinkedQueue<>();

    final Queue<Cell> cellsThatChangedState = new ConcurrentLinkedQueue<>();

    static GameState createNewGame() {
        return createNewGame(DEFAULT_BOARD_SIZE);
    }

    static GameState createNewGame(String[] initialCondition, char livingCellChar) {
        return createNewGame(initialCondition, livingCellChar, DEFAULT_THREAD_COUNT);
    }

    static GameState createNewGame(String[] initialCondition, char livingCellChar, int threadCount) {
        return createNewGame(getDefaultRules(), initialCondition, livingCellChar, threadCount);
    }

    static GameState createNewGame(Rules rules, String[] initialCondition, char livingCellChar) {
        return createNewGame(rules, initialCondition, livingCellChar, DEFAULT_THREAD_COUNT);
    }

    static GameState createNewGame(Rules rules, String[] initialCondition, char livingCellChar, int threadCount) {
        int height = initialCondition.length;
        int width = -1;
        for (String line : initialCondition) {
            if (width == -1) {
                width = line.length();
            } else if (width != line.length()) {
                throw new IllegalArgumentException("Every line must be equal length");
            }
        }
        GameState game = createNewGame(rules, new Dimension(width, height), threadCount);
        game.setInitialGameState(initialCondition, livingCellChar);
        return game;
    }

    static GameState createNewGame(Dimension boardSize) {
        return createNewGame(boardSize, DEFAULT_THREAD_COUNT);
    }

    static GameState createNewGame(Dimension boardSize, int threadCount) {
        return createNewGame(getDefaultRules(), boardSize, threadCount);
    }

    static GameState createNewGame(Rules rules, Dimension boardSize) {
        return createNewGame(rules, boardSize, DEFAULT_THREAD_COUNT);
    }

    static GameState createNewGame(Rules rules, Dimension boardSize, int threadCount) {
        return new GameState(rules, (int) boardSize.getWidth(), (int) boardSize.getHeight(), threadCount);
    }

    private GameState(Rules rules, int boardWidth, int boardHeight, int threadCount) {
        threadPool = Executors.newFixedThreadPool(threadCount);
        this.rules = rules;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        board = new Cell[boardHeight][boardWidth];

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                final Cell cell = new Cell(this, x, y);
                board[y][x] = cell;

                // TEMPORARY TODO REMOVE
                currentCellQueue.add(new Cell.CellStateDeterminer(cell));
            }
        }

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                for (Direction d : Direction.values()) {
                    board[y][x].populateNeighbor(d, board[d.getNeighborY(y, boardHeight)][d.getNeighborX(x, boardWidth)]);
                }
            }
        }
    }

    private void setInitialGameState(String[] initialCondition, char livingCellChar) throws IllegalArgumentException {
        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                if (initialCondition[y].charAt(x) == livingCellChar) {
                    getCell(x,y).setCurrentlyAlive(true);
                }
            }
        }
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public Rules getRules() {
        return rules;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public Cell getCell(int x, int y) {
        if (x < 0 || x >= boardWidth) {
            throw new IllegalArgumentException(String.format("X position %d not within board x dimensions [0,%d)", x, boardWidth));
        }
        if (y < 0 || y >= boardHeight) {
            throw new IllegalArgumentException(String.format("Y position %d not within board y dimensions [0,%d)", y, boardHeight));
        }
        return board[y][x];
    }

    void addCellToNextStepQueue(Cell cell) {
        nextStepCellQueue.add(() -> {
            currentCellQueue.add(new Cell.CellStateDeterminer(cell));
            return null;
        });
    }

    void addCellToUpdateQueue(Cell cell) {
        cellUpdateQueue.add(() -> {
            if (cell.updateToNextState()) {
                cellsThatChangedState.add(cell);
            }
            return null;
        });
    }

    /**
     * For testing only
     */
    boolean _nextStepQueueContainsCell(Cell cell) {
        return nextStepCellQueue.contains(cell);
    }

    // This is probably where the majority of the threading tricks are gonna come in... We may need to discuss this
    // further in class. However, we could probably initialy make it work in serial. I don't think it will be too
    // difficult to change over to parallel.
    public void processGameStep() {
        cellsThatChangedState.clear();
        _determineCellsNextState();
        _updateCellStates();
        _copyNextCellQueueToCurrent();
        _incrementGameStep();
    }

    void _determineCellsNextState() {
        try {
            threadPool.invokeAll(currentCellQueue);
            currentCellQueue.clear();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void _updateCellStates() {
        try {
            threadPool.invokeAll(cellUpdateQueue);
            cellUpdateQueue.clear();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void _copyNextCellQueueToCurrent() {
        try {
            threadPool.invokeAll(nextStepCellQueue);
            nextStepCellQueue.clear();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void _incrementGameStep() {
        currentStep.incrementAndGet();
    }

    /**
     * Gets the current game step. This method is thread safe.
     *
     * @return the current game step.
     */
    public int getCurrentStep() {
        return currentStep.get();
    }

    public void setMaxStep(int maxStep) {
        if (maxStep < 1) {
            throw new IllegalArgumentException("Max step must be greater than 0");
        }
        this.maxStep = maxStep;
    }

    public Collection<Cell> getCellsThatChangedState() {
        return cellsThatChangedState;
    }

    public int getMaxStep() {
        return maxStep;
    }

    public boolean isGameOver() {
        return false;
    }

    String createBoardString(char deadCell, char liveCell) {
        StringBuilder builder = new StringBuilder();
        for (int y = 0; y < boardHeight; y++) {
            if (y != 0) {
                builder.append("\n");
            }
            for (int x = 0; x < boardWidth; x++) {
                builder.append((getCell(x, y).isAlive() ? liveCell : deadCell));
            }
        }
        return builder.toString();
    }

    /**
     * Changes the number of threads used to process the game steps. This method is DEFINITELY not thread safe and
     * should only ever be called when a step is not being processed.
     *
     * @param threadPoolSize The new number of threads to use to process game steps.
     */
    public void setThreadPoolSize(int threadPoolSize) {
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }
}
