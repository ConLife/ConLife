package conlife;

import java.util.concurrent.atomic.AtomicBoolean;

public class Cell {

    private final int x, y;
    private final GameState gameState;
    private AtomicBoolean alive = new AtomicBoolean(false);
    // This should be reset to false at the end of each step
    private AtomicBoolean nextStepLife = new AtomicBoolean(false);

    // Should be true after #determineNextState() is called each step and false at the end of each step
    private AtomicBoolean currentStepStateCalculated = new AtomicBoolean(false);
    // When this cells state is determined it should be added to the next step queue unless another cell has caused it
    // to be added already which is why we have this boolean.
    private AtomicBoolean addedToNextStepQueue = new AtomicBoolean(false);
    private AtomicBoolean addedToUpdateQueue = new AtomicBoolean(false);

    Cell[] neighbors = new Cell[8];

    /*
    * Current step cell processing:
    *
    * determine the state of this cell:
    *   check neighbors' state (if null, they are dead)
    *     if a neighbor is dead
    *       something needs to create a new dead neighbor cell
    *       add cell to next step queue
    *   increment neighbor count for living neighbors
    *   check rules to determine this cell's state based on neighbor count
    *
    *
    *
    * */

    public Cell(GameState gameState, int x, int y) {
        this.gameState = gameState;
        this.x = x;
        this.y = y;
    }

    void populateNeighbor(Direction direction, Cell neighborCell) {
        neighbors[direction.ordinal()] = neighborCell;
    }

    /**
     * Looks at the current state of this cell and the current state of the neighbor cells and determines the next state
     * of this cell. Afterwards, it flags this cell that it has calculated its state for this step. This should also add
     * the cell to the next step queue if it has not already been added.
     */
    Rules.Rule determineNextState() {
        return _determineNextState(isAlive(), getLivingNeighborCount());
    }

    /**
     * Should only be called by this class or by tests.
     *
     * @param alive whether this cell is currently alive or not. The purpose of this is to extract the value from the
     *              AtomicBoolean - It shouldn't be changing during this but it may be faster to do it this way.
     * @param livingNeighbors similar to above.
     */
    Rules.Rule _determineNextState(boolean alive, int livingNeighbors) {
        Rules.Rule rule = gameState.getRules().getRule(alive, livingNeighbors);
        switch (rule) {
            case DEATH: // Rule 1 & 3
                // The cell is going to die and only should be added to next step queue if it has living neighbors
                prepareForNextState(false, livingNeighbors != 0);
                break;
            case SURVIVE: // Rule 2
                // The cell is going to stay alive and will have to be checked again next step
                prepareForNextState(true, true);
                break;
            case BIRTH: // Rule 4
                // The cell is going to be born and will have to be checked again next step
                prepareForNextState(true, true);
                break;
            case DEAD_NO_BIRTH:
                // The cell is dead and will stay dead but if it has any neighbors it will need to be checked next step
                if (livingNeighbors != 0){
                    prepareForNextState(false, true);
                }
                break;
        }
        // neighbors[Direction.EAST.ordinal()]
        //after tested
        currentStepStateCalculated.set(true);
        return rule;
    }

    /**
     * Configures the cell for the next step
     *
     * @param aliveNextStep whether the cell is alive next step
     * @param addToNextStepQueue whether this method should add the cell to the next step queue (if not added already)
     */
    private void prepareForNextState(boolean aliveNextStep, boolean addToNextStepQueue) {
        if ((addToNextStepQueue || isAlive() || aliveNextStep)) {
            addToUpdateQueueIfNotAdded();
        }
        nextStepLife.set(aliveNextStep);
        if (addToNextStepQueue) {
            addToNextStepQueueIfNotAdded();
        }
        if (aliveNextStep) {
            for (Direction d : Direction.values()) {
                getNeighbor(d).addToNextStepQueueIfNotAdded();
            }
        }
    }

    private void addToNextStepQueueIfNotAdded() {
        if (addedToNextStepQueue.compareAndSet(false, true)) {
            gameState.addCellToNextStepQueue(this);
            addToUpdateQueueIfNotAdded();
        }
    }

    private void addToUpdateQueueIfNotAdded() {
        if (addedToUpdateQueue.compareAndSet(false, true)) {
            gameState.addCellToUpdateQueue(this);
        }
    }

    /**
     * Sets the cell's current life state to the next life state and resets all other flags to the starting state.
     */
    boolean updateToNextState() {
        boolean result;
        boolean aliveNextStep = isAliveNextStep();
        result = aliveNextStep != alive.get();
        alive.set(aliveNextStep);
        nextStepLife.set(false);
        currentStepStateCalculated.set(false);
        addedToNextStepQueue.set(false);
        addedToUpdateQueue.set(false);
        return result;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Cell getNeighbor(Direction d) {
        return neighbors[d.ordinal()];
    }

    public boolean isAlive() {
        return alive.get();
    }

    public boolean isAliveNextStep() {
        return nextStepLife.get();
    }

    public boolean isStateCalculatedThisStep() {
        return currentStepStateCalculated.get();
    }

    public AtomicBoolean isAddedToNextStepQueue() {
        return addedToNextStepQueue;
    }

    public AtomicBoolean isAddedToUpdateQueue() {
        return addedToUpdateQueue;
    }

    public int getLivingNeighborCount() {
        int living = 0;
        for (Direction d : Direction.values()) {
            if (getNeighbor(d).isAlive()) {
                living++;
            }
        }
        return living;
    }

    /**
     * Sets whether the cell is currently alive. If this is different than before, the cell is added to the game cell
     * queue.
     *
     * @param alive whether the cell should be alive currently.
     */
    public void setCurrentlyAlive(boolean alive) {
        boolean previous = this.alive.getAndSet(alive);
        if (previous != alive) {
            if (!gameState.isCellCurrentlyQueued(this)) {
                gameState.addCellToCurrentQueue(this);
            }
            for (Direction d : Direction.values()) {
                Cell neighbor = getNeighbor(d);
                if (!gameState.isCellCurrentlyQueued(neighbor)) {
                    gameState.addCellToCurrentQueue(neighbor);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Cell{" +
                "x=" + x +
                ", y=" + y +
                ", alive=" + alive +
                ", nextStepLife=" + nextStepLife +
                ", currentStepStateCalculated=" + currentStepStateCalculated +
                ", addedToNextStepQueue=" + addedToNextStepQueue +
                ", addedToUpdateQueue=" + addedToUpdateQueue +
                '}';
    }
}
