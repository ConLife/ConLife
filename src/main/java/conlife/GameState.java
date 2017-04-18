package conlife;

import java.awt.Dimension;
import java.text.ParseException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameState {

    private static final Dimension DEFAULT_BOARD_SIZE = new Dimension(100, 100);

    public static final String DEFAULT_RULES_STRING = "B03/S23";

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

    private int currentStep = 0;
    private int maxStep = -1;

    private final Rules rules;
    private final int boardWidth, boardHeight;
    private Cell[][] board;

    private final Queue<Cell> currentCellQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Cell> cellUpdateQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Cell> nextStepCellQueue = new ConcurrentLinkedQueue<>();

    static GameState createNewGame() {
        return createNewGame(DEFAULT_BOARD_SIZE);
    }

    static GameState createNewGame(String[] initialCondition, char livingCellChar) {
        return createNewGame(getDefaultRules(), initialCondition, livingCellChar);
    }

    /**
     * Probably going to only be for testing...
     */
    static GameState createNewGame(Rules rules, String[] initialCondition, char livingCellChar) {
        int height = initialCondition.length;
        int width = -1;
        for (String line : initialCondition) {
            if (width == -1) {
                width = line.length();
            } else if (width != line.length()) {
                throw new IllegalArgumentException("Every line must be equal length");
            }
        }
        GameState game = createNewGame(rules, new Dimension(width, height));
        game.setInitialGameState(initialCondition, livingCellChar);
        return game;
    }

    static GameState createNewGame(Dimension boardSize) {
        return createNewGame(getDefaultRules(), boardSize);
    }

    static GameState createNewGame(Rules rules, Dimension boardSize) {
        return new GameState(rules, (int) boardSize.getWidth(), (int) boardSize.getHeight());
    }

    private GameState(Rules rules, int boardWidth, int boardHeight) {
        this.rules = rules;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        board = new Cell[boardHeight][boardWidth];

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                board[y][x] = new Cell(this, x, y);

                // TEMPORARY TODO REMOVE
                currentCellQueue.add(board[y][x]);
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
        if (cell.isAddedToNextStepQueue()) {
            throw new IllegalStateException("Cell has already been added to the next state queue");
        }
        nextStepCellQueue.add(cell);
    }

    void addCellToUpdateQueue(Cell cell) {
        if (cell.isAddedToUpdateQueue()) {
            throw new IllegalStateException("Cell has already been added to the update queue");
        }
        cellUpdateQueue.add(cell);
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
        Cell cell;
        while ((cell = currentCellQueue.poll()) != null) {
            cell.determineNextState();
        }

        while ((cell = cellUpdateQueue.poll()) != null) {
            cell.updateToNextState();
        }

        while ((cell = nextStepCellQueue.poll()) != null) {
            currentCellQueue.add(cell);
        }

        currentStep++;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setMaxStep(int maxStep) {
        if (maxStep < 1) {
            throw new IllegalArgumentException("Max step must be greater than 0");
        }
        this.maxStep = maxStep;
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
}
