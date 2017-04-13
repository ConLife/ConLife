package conlife;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class Cell {

    private final int x, y;
    private AtomicBoolean alive = new AtomicBoolean(false);
    private AtomicBoolean nextStepLife = new AtomicBoolean(false);

    private AtomicBoolean currentStepCalculated = new AtomicBoolean(false);
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

    void determineNextState() {
        // neighbors[Direction.EAST.ordinal()]
    }

    public Cell getNeighbor(Direction d) {
        return neighbors[d.ordinal()];
    }
}
