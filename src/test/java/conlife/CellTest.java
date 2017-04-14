package conlife;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CellTest {

    GameState game;

    @Before
    public void setUp() throws Exception {
        GameState.updateRules(Rules.parseRules("B03/S23"));
        game = GameState.createNewGame(-1);
    }

    @Test
    public void testNextStateEmptyBoard() {
        Cell cell = game.getCell(5, 5);
        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        cell.determineNextState();
        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
    }

    @Test
    public void testBirth() {
        Cell cell = game.getCell(5, 5);
        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        for (Direction d : Direction.values()) {
            assertFalse(cell.getNeighbor(d).isAlive());
        }
        cell.getNeighbor(Direction.NORTH)._setCurrentlyAlive(true);
        cell.getNeighbor(Direction.SOUTH)._setCurrentlyAlive(true);
        cell.getNeighbor(Direction.WEST)._setCurrentlyAlive(true);
        assertTrue(cell.getNeighbor(Direction.NORTH).isAlive());
        assertTrue(cell.getNeighbor(Direction.SOUTH).isAlive());
        assertTrue(cell.getNeighbor(Direction.WEST).isAlive());
        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        cell.determineNextState();
        assertFalse(cell.isAlive());
        assertTrue(cell.isAliveNextStep());
    }

}