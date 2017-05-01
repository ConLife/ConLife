package conlife;

import java.awt.Dimension;
import java.text.ParseException;
import java.util.Collection;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls all aspects of the mechanics of the game. Most of the operations in this class are not thread safe. However,
 * game steps are performed in a thread safe manner and can be parallelized effectively.
 *
 * @author Jeremy Wood, Nathan Coggins
 */
public class GameState {

    public static final Dimension DEFAULT_BOARD_SIZE = new Dimension(100, 100);

    public static final String DEFAULT_RULES_STRING = "B3/S23";

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

    private GameThread[] threadPool;
    private final CyclicBarrier barrier;
    //private AtomicLong waitingThread = new AtomicLong(0);
    private final Random random = new Random();

    private Rules rules;
    private final int boardWidth, boardHeight;
    private Cell[][] board;

    final Queue<Cell> cellsThatChangedState = new ConcurrentLinkedQueue<>();

    public static GameState createNewGame() {
        return createNewGame(DEFAULT_BOARD_SIZE);
    }

    public static GameState createNewGame(String[] initialCondition, char livingCellChar) {
        return createNewGame(initialCondition, livingCellChar, DEFAULT_THREAD_COUNT);
    }

    public static GameState createNewGame(String[] initialCondition, char livingCellChar, int threadCount) {
        return createNewGame(getDefaultRules(), initialCondition, livingCellChar, threadCount);
    }

    public static GameState createNewGame(Rules rules, String[] initialCondition, char livingCellChar) {
        return createNewGame(rules, initialCondition, livingCellChar, DEFAULT_THREAD_COUNT);
    }

    public static GameState createNewGame(Rules rules, String[] initialCondition, char livingCellChar, int threadCount) {
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

    public static GameState createNewGame(Dimension boardSize) {
        return createNewGame(boardSize, DEFAULT_THREAD_COUNT);
    }

    public static GameState createNewGame(Dimension boardSize, int threadCount) {
        return createNewGame(getDefaultRules(), boardSize, threadCount);
    }

    public static GameState createNewGame(Rules rules, Dimension boardSize) {
        return createNewGame(rules, boardSize, DEFAULT_THREAD_COUNT);
    }

    public static GameState createNewGame(Rules rules, Dimension boardSize, int threadCount) {
        return new GameState(rules, (int) boardSize.getWidth(), (int) boardSize.getHeight(), threadCount);
    }

    private GameState(Rules rules, int boardWidth, int boardHeight, int threadCount) {
        this.rules = rules;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        board = new Cell[boardHeight][boardWidth];

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                final Cell cell = new Cell(this, x, y);
                board[y][x] = cell;
            }
        }

        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                for (Direction d : Direction.values()) {
                    board[y][x].populateNeighbor(d, board[d.getNeighborY(y, boardHeight)][d.getNeighborX(x, boardWidth)]);
                }
            }
        }

        ThreadGroup threadGroup = new ThreadGroup("game-logic-thread-group");
        threadGroup.setDaemon(true);
        barrier = new CyclicBarrier(threadCount + 1);
        threadPool = new GameThread[threadCount];
        // Determine max number of cells any thread should ever be dealing with
        int perThreadWorkQueueSize = (int) Math.ceil((boardHeight * boardWidth) / (double) threadCount);
        // Spin up the game threads
        for (int i = 0; i < threadCount; i++) {
            threadPool[i] = new GameThread(threadGroup, "game-logic-thread-" + i, perThreadWorkQueueSize, barrier);
            threadPool[i].start();
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
        threadPool[random.nextInt(threadPool.length)].addCellToNextStepQueue(cell);
    }

    void addCellToUpdateQueue(Cell cell) {
        threadPool[random.nextInt(threadPool.length)].addCellToUpdateQueue(cell);
    }

    /**
     * For testing only
     */
    boolean _nextStepQueueContainsCell(Cell cell) {
        for (GameThread thread : threadPool) {
            if (thread.isCellInNextStepQueue(cell)) {
                return true;
            }
        }
        return false;
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

    private void distributeWorkload(Queue<Cell> totalWorkload, Phase nextPhase) {
        int[] cellCounts = getThreadWorkloadSizes(totalWorkload.size());
        for (int i = 0; i < threadPool.length; i++) {
            int cellCount = cellCounts[i];
            for (int j = 0; j < cellCount; j++) {
                threadPool[i].addCellToWorkQueue(totalWorkload.poll());
            }
            // This will start the thread's processing of cells
            threadPool[i].phase = nextPhase;
        }

        if (!totalWorkload.isEmpty()) {
            // This should never happen...
            throw new IllegalStateException("The cell queue was not properly emptied");
        }
    }

    private void setThreadPoolPhase(Phase phase) {
        for (GameThread t : threadPool) {
            t.phase = phase;
        }
    }

    void _determineCellsNextState() {
        setThreadPoolPhase(Phase.DETERMINE_NEXT_STATE);
        //distributeWorkload(currentCellQueue, Phase.DETERMINE_NEXT_STATE);
        wakeupWorkers();
        waitForWorkersToFinish();
    }

    void _updateCellStates() {
        setThreadPoolPhase(Phase.UPDATE);
        //distributeWorkload(cellUpdateQueue, Phase.UPDATE);
        wakeupWorkers();
        waitForWorkersToFinish();
        for (int i = 0; i < threadPool.length; i++) {
            cellsThatChangedState.addAll(threadPool[i].getCellsThatChanged());
            threadPool[i].clearCellsThatChanged();
        }
    }

    void _copyNextCellQueueToCurrent() {
        // This is probably not worth parallelizing in the way of the other two steps since it would just be two adds
        // instead of one.
        //nextStepCellQueue.parallelStream().forEach(currentCellQueue::add);
        for (GameThread t : threadPool) {
            t.copyNextCellQueueToCurrent();
        }
    }

    void _incrementGameStep() {
        currentStep.incrementAndGet();
    }

    private void wakeupWorkers() {
        synchronized (barrier) {
            barrier.notifyAll();
        }
    }

    private void waitForWorkersToFinish() {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the current game step. This method is thread safe.
     *
     * @return the current game step.
     */
    public int getCurrentStep() {
        return currentStep.get();
    }

    public Collection<Cell> getCellsThatChangedState() {
        return cellsThatChangedState;
    }

    boolean isCellCurrentlyQueued(Cell cell) {
        for (GameThread thread : threadPool) {
            if (thread.isCellCurrentQueued(cell)) {
                return true;
            }
        }
        return false;
    }

    void addCellToCurrentQueue(Cell cell) {
        threadPool[random.nextInt(threadPool.length)].addCellToWorkQueue(cell);
    }

    int getCurrentCellQueueSize() {
        int size = 0;
        for (GameThread t : threadPool) {
            size += t.getWorkQueueSize();
        }
        return size;
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

    private int[] getThreadWorkloadSizes(int numCells) {
        int threadCount = threadPool.length;
        int[] result = new int[threadCount];
        int threadsWithExtra = numCells % threadCount;
        int normalWorkLoad = numCells / threadCount;
        int i;
        for (i = 0; i < threadCount; i++) {
            result[i] = normalWorkLoad + (i < threadsWithExtra ? 1 : 0);
        }
        return result;
    }

    /**
     * Used to direct game threads on what they should be doing.
     */
    enum Phase {
        DETERMINE_NEXT_STATE, UPDATE, COPY, WAIT, EXIT;
    }
}
