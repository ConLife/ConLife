package conlife;

import conlife.Rules.Rule;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class CellTest {

    GameState game;

    @Before
    public void setup() throws Exception {
        GameState.updateDefaultRules(Rules.parseRules("B3/S23"));
        game = setupGame();
    }

    private GameState setupGame() throws Exception {
        return GameState.createNewGame();
    }

    private Cell getSpyCell(GameState game, int liveNeighborCount) {
        Cell cell = game.getCell(5, 5);
        cell = spy(cell);
        when(cell.getLivingNeighborCount()).thenReturn(liveNeighborCount);
        return cell;
    }

    @Test
    public void testDefaultCellState() {
        Cell cell = game.getCell(5, 5);
        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        assertFalse(cell.isStateCalculatedThisStep());
        assertFalse(cell.isAddedToNextStepQueue().get());
    }

    @Test
    public void testGetNeighborCount() {
        Cell cell = game.getCell(5, 5);
        cell = spy(cell);
        assertEquals(0, cell.getLivingNeighborCount());
        mockNeighborCellLiving(cell, Direction.NORTH);
        assertEquals(1, cell.getLivingNeighborCount());
        mockNeighborCellLiving(cell, Direction.NORTH_EAST);
        assertEquals(2, cell.getLivingNeighborCount());
        mockNeighborCellLiving(cell, Direction.EAST);
        assertEquals(3, cell.getLivingNeighborCount());
        mockNeighborCellLiving(cell, Direction.SOUTH_EAST);
        assertEquals(4, cell.getLivingNeighborCount());
        mockNeighborCellLiving(cell, Direction.SOUTH);
        assertEquals(5, cell.getLivingNeighborCount());
        mockNeighborCellLiving(cell, Direction.SOUTH_WEST);
        assertEquals(6, cell.getLivingNeighborCount());
        mockNeighborCellLiving(cell, Direction.WEST);
        assertEquals(7, cell.getLivingNeighborCount());
        mockNeighborCellLiving(cell, Direction.NORTH_WEST);
        assertEquals(8, cell.getLivingNeighborCount());
    }

    // Creates the illusion of a neighbor cell being alive in the given direction for the local cell
    private Cell mockNeighborCellLiving(Cell localCell, Direction neighborDirection) {
        Cell neighborCell = localCell.getNeighbor(neighborDirection);
        final Cell spyNeighborCell = spy(neighborCell);
        when(spyNeighborCell.isAlive()).thenReturn(true);
        doReturn(spyNeighborCell).when(localCell).getNeighbor(eq(neighborDirection));
        return neighborCell;
    }

    @Test
    public void testNextStateEmptyBoard() {
        Cell cell = game.getCell(5, 5);
        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        cell.determineNextState();
        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        assertFalse(game._nextStepQueueContainsCell(cell));
    }

    @Test
    public void testBirth() throws Exception {
        GameState game = setupGame(); // Due to the way running these tests can work, this is a safety precaution...
        Cell cell = getSpyCell(game, 3);

        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        cell.determineNextState();
        assertTrue(cell.isStateCalculatedThisStep());
        assertFalse(cell.isAlive());
        assertTrue(cell.isAliveNextStep());
        assertTrue(cell.isAddedToNextStepQueue().get());

        for (int i = 0; i < 9; i++) {
            if (i == 3) {
                continue;
            }
            game = setupGame();
            cell = getSpyCell(game, i);

            assertFalse(cell.isAlive());
            assertFalse(cell.isAliveNextStep());
            cell.determineNextState();
            assertTrue(cell.isStateCalculatedThisStep());
            assertFalse(cell.isAlive());
            assertFalse(cell.isAliveNextStep());
            if (i == 0) {
                // If there are no neighbors, and this cell is dead, it won't be checked next step
                assertFalse(cell.isAddedToNextStepQueue().get());
            } else {
                // But if there are any number of neighbors alive, it will be checked next step
                assertTrue(cell.isAddedToNextStepQueue().get());
            }
        }

        game = setupGame();
        cell = getSpyCell(game, 3);
        when(cell.isAlive()).thenReturn(true);
        assertTrue(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        cell.determineNextState();
        assertTrue(cell.isStateCalculatedThisStep());
        assertTrue(cell.isAlive());
        assertTrue(cell.isAliveNextStep());
    }

    @Test
    public void testSurvive() throws Exception {
        GameState game = setupGame(); // Due to the way running these tests can work, this is a safety precaution...
        Cell cell = game.getCell(5, 5);
        cell = spy(cell);
        when(cell.isAlive()).thenReturn(true);

        when(cell.getLivingNeighborCount()).thenReturn(3);

        assertTrue(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        cell.determineNextState();
        assertTrue(cell.isStateCalculatedThisStep());
        assertTrue(cell.isAlive());
        assertTrue(cell.isAliveNextStep());

        game = setupGame();
        cell = game.getCell(5, 5);
        cell = spy(cell);
        when(cell.isAlive()).thenReturn(true);
        when(cell.getLivingNeighborCount()).thenReturn(2);

        assertTrue(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        cell.determineNextState();
        assertTrue(cell.isStateCalculatedThisStep());
        assertTrue(cell.isAlive());
        assertTrue(cell.isAliveNextStep());

        for (int i = 0; i < 9; i++) {
            if (i == 3 || i == 2) {
                continue;
            }
            game = setupGame();
            cell = game.getCell(5, 5);
            cell = spy(cell);
            when(cell.isAlive()).thenReturn(true);

            when(cell.getLivingNeighborCount()).thenReturn(i);

            assertTrue(cell.isAlive());
            assertFalse(cell.isAliveNextStep());
            cell.determineNextState();
            assertTrue(cell.isAlive());
            assertFalse(cell.isAliveNextStep());
        }
    }

    @Test
    public void testAddToUpdateQueue() {
        Cell cell = game.getCell(5, 5);
        cell = spy(cell);

        when(cell.isAlive()).thenReturn(false);
        for (int i = 0; i < 9; i++) {
            Rule rule = game.getRules().getRule(false, i);
            if (rule == Rule.BIRTH) {
                cell._determineNextState(false, i);
                assertTrue("Neighbors: " + i, cell.isAddedToUpdateQueue().get()); // Needs to be updated
                cell.updateToNextState(); // Needs to be updated/reset
            } else {
                cell._determineNextState(false, i);
                if (i == 0) {
                    // Only dead cells with no neighbors don't need an update...
                    assertFalse("Neighbors: " + i, cell.isAddedToUpdateQueue().get());
                } else {
                    assertTrue("Neighbors: " + i, cell.isAddedToUpdateQueue().get());
                    cell.updateToNextState(); // Needs to be updated/reset
                }
            }
        }

        when(cell.isAlive()).thenReturn(true);
        // In every case of the cell being alive, the next state needs to be shifted to current state
        for (int i = 0; i < 9; i++) {
            cell._determineNextState(true, i);
            assertTrue("Neighbors: " + i, cell.isAddedToUpdateQueue().get());
            cell.updateToNextState(); // Needs to be updated/reset
        }
    }
}