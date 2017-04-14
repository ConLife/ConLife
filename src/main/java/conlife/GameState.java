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
    private static Rules rules;

    static {
        try {
            rules = Rules.parseRules(DEFAULT_RULES_STRING);
        } catch (ParseException | Rules.RulesException e) {
            throw new RuntimeException("Initial rules are busted", e);
        }

    }

    static void updateRules(Rules rules) {
        synchronized (rulesLock) {
            GameState.rules = rules;
        }
    }

    static Rules getRules() {
        return rules;
    }

    private int currentStep = 0;
    private final int maxStep;

    private final int boardWidth, boardHeight;
    private Cell[][] board;

    private final Queue<Cell> currentCellQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Cell> nextStepCellQueue = new ConcurrentLinkedQueue<>();

    static GameState createNewGame(int maxStep) {
        return new GameState(maxStep, (int) DEFAULT_BOARD_SIZE.getWidth(), (int) DEFAULT_BOARD_SIZE.getHeight());
    }

    private GameState(int maxStep, int boardWidth, int boardHeight) {
        this.maxStep = maxStep;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        board = new Cell[boardWidth][boardHeight];

        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                board[x][y] = new Cell(this, x, y);
            }
        }

        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                for (Direction d : Direction.values()) {
                    board[x][y].populateNeighbor(d, board[d.getNeighborX(x, boardWidth)][d.getNeighborY(y, boardHeight)]);
                }
            }
        }
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public Cell getCell(int x, int y) {
        if (x < 0 || x >= boardWidth) {
            throw new RuntimeException(String.format("X position (%d) not within board dimensions (0-%d)", x, boardWidth));
        }
        if (y < 0 || y >= boardWidth) {
            throw new RuntimeException(String.format("Y position (%d) not within board dimensions (0-%d)", y, boardHeight));
        }
        return board[x][y];
    }

    void addCellToNextStepQueue(Cell cell) {
        if (cell.isAddedToNextStepQueue()) {
            throw new IllegalStateException("Cell has already been added to the queue");
        }
        nextStepCellQueue.add(cell);
    }

    /**
     * For testing only
     */
    boolean _nextStepQueueContainsCell(Cell cell) {
        return nextStepCellQueue.contains(cell);
    }
}
