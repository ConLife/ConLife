package conlife;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class Cell {

    private final int x, y;
    private AtomicBoolean alive = new AtomicBoolean(false);
    private AtomicBoolean nextStepLife = new AtomicBoolean(false);

    private AtomicBoolean currentStepStateCalculated = new AtomicBoolean(false);
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

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void populateNeighbor(Direction direction, Cell neighborCell) {
        neighbors[direction.ordinal()] = neighborCell;
    }

    /**
     * Looks at the current state of this cell and the current state of the neighbor cells and determines the next state
     * of this cell. Afterwards, it flags this cell that it has calculated its state for this step.
     */
    void determineNextState() {
        // neighbors[Direction.EAST.ordinal()]
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

    /**
     * For testing only
     */
    void _setCurrentlyAlive(boolean alive) {
        this.alive.set(alive);
    }
}
