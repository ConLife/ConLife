package conlife;

import conlife.GameState.Phase;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;

import static conlife.GameState.Phase.*;

/**
 * Responsible for processing all of cells that have been assigned to it.
 *
 * @author Jeremy Wood, Nathan Coggins
 */
class GameThread extends Thread {

    volatile GameState.Phase phase = WAIT;

    private final CyclicBarrier barrier;

    private Queue<Cell> workQueue;
    private Queue<Cell> cellsThatChanged;
    private final Queue<Cell> cellUpdateQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Cell> nextStepQueue = new ConcurrentLinkedQueue<>();

    GameThread(ThreadGroup threadGroup, String name, CyclicBarrier barrier) {
        super(threadGroup, name);
        workQueue = new ConcurrentLinkedQueue<>();
        cellsThatChanged = new ConcurrentLinkedQueue<>();
        this.barrier = barrier;
    }

    /**
     * Adds a cell to the work queue to be processed by this thread. This is NOT thread safe and should only be called
     * between phases (e.g. when phase == WAIT).
     *
     * @param cell The cell to add to this threads work queue.
     * @throws IllegalStateException If attempting to add cells when thread is not in the WAIT phase.
     */
    void addCellToWorkQueue(Cell cell) throws IllegalStateException {
        if (phase != WAIT) {
            throw new IllegalArgumentException("Cells can only be added to a thread's work queue during a waiting"
                    + " phase.");
        }
        workQueue.add(cell);
    }

    Collection<Cell> getCellsThatChanged() {
        return cellsThatChanged;
    }

    void clearCellsThatChanged() {
        cellsThatChanged.clear();
    }

    @Override
    public void run() {
        while (phase != Phase.EXIT) {
            switch (phase) {
                case WAIT:
                    innocuousWait();
                    break;
                case DETERMINE_NEXT_STATE:
                    determineNextState();
                    barrier();
                    break;
                case UPDATE:
                    update();
                    barrier();
                    break;
                case COPY:
                    //copyCells();
                    barrier();
                    break;
                default:
                    break;
            }
        }
    }

    private void barrier() {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    private void busyWait() {
        while (phase == WAIT) {
            // This is going to max out cpu core usage but it is apparently the method
            // that will receive updates the fastest.
            // Turns out this method also makes some computers run like garbage.
        }
    }

    private void innocuousWait() {
        synchronized (barrier) {
            while (phase == WAIT) {
                try {
                    barrier.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void determineNextState() {
        if (phase != DETERMINE_NEXT_STATE) {
            throw new IllegalArgumentException("Phase must be set to DETERMINE_NEXT_STATE");
        }
        Cell cell;
        while ((cell = workQueue.poll()) != null) {
            cell.determineNextState();
        }
        phase = WAIT;
    }

    private void update() {
        if (phase != UPDATE) {
            throw new IllegalArgumentException("Phase must be set to UPDATE");
        }
        Cell cell;
        while ((cell = cellUpdateQueue.poll()) != null) {
            if (cell.updateToNextState()) {
                cellsThatChanged.add(cell);
            }
        }
        phase = WAIT;
    }

    boolean isCellCurrentQueued(Cell cell) {
        return workQueue.contains(cell);
    }

    boolean isCellInNextStepQueue(Cell cell) {
        return nextStepQueue.contains(cell);
    }

    void addCellToNextStepQueue(Cell cell) {
        nextStepQueue.add(cell);
    }

    void addCellToUpdateQueue(Cell cell) {
        cellUpdateQueue.add(cell);
    }

    int getWorkQueueSize() {
        return workQueue.size();
    }

    void copyNextCellQueueToCurrent() {
        workQueue.addAll(nextStepQueue);
        nextStepQueue.clear();
    }
}
