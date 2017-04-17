package conlife;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class Cell {

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
    void determineNextState() {
        int livingNeighbors = getLivingNeighborCount();
        if(isAlive() && livingNeighbors < 2){//rule 1
            nextStepLife.set(false);
        } else if (isAlive() && (livingNeighbors == 2 || livingNeighbors == 3)) {//rule 2
            nextStepLife.set(true);
            gameState.addCellToNextStepQueue(this);
        } else if (isAlive() && livingNeighbors > 3){// rule 3
            nextStepLife.set(false);
        } else if (!isAlive() && livingNeighbors == 3){// rule 4
            nextStepLife.set(true);
            gameState.addCellToNextStepQueue(this);   
        }
        // neighbors[Direction.EAST.ordinal()]
        //after tested
        currentStepStateCalculated.set(true);
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

    public boolean isAddedToNextStepQueue() {
        return addedToNextStepQueue.get();
    }

    public int getLivingNeighborCount() {
        int living = 0;
        for (int neighbor = 0; neighbor < 8; neighbor++) {
            if (neighbors[neighbor].isAlive()) {
                living++;
            }
        }
        return living;
    }

    void setCurrentlyAlive(boolean alive) throws IllegalStateException {
        if (gameState.getCurrentStep() > 0) {
            throw new IllegalStateException("Cannot set cell life after game has started");
        }
        this.alive.set(alive);
    }
}
