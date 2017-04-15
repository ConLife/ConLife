package conlife;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class CellTest {

    GameState game;

    @Before
    public void setup() throws Exception {
        GameState.updateRules(Rules.parseRules("B03/S23"));
        game = setupGame();
    }

    private GameState setupGame() throws Exception {
        return GameState.createNewGame();
    }

    private Cell getSpyCell(GameState game, int liveNeighborCount) {
        Cell cell = game.getCell(5, 5);
        cell = Mockito.spy(cell);
        when(cell.getLivingNeighborCount()).thenReturn(liveNeighborCount);
        return cell;
    }

    @Test
    public void testDefaultCellState() {
        Cell cell = game.getCell(5, 5);
        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        assertFalse(cell.isStateCalculatedThisStep());
        assertFalse(cell.isAddedToNextStepQueue());
    }

    @Test
    public void testGetNeighborCount() {
        Cell cell = game.getCell(5, 5);
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

    private Cell mockNeighborCellLiving(Cell localCell, Direction neighborDirection) {
        Cell neighborCell = spy(localCell.getNeighbor(neighborDirection));
        when(neighborCell.isAlive()).thenReturn(true);
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
        assertTrue(game._nextStepQueueContainsCell(cell));

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
                assertFalse(game._nextStepQueueContainsCell(cell));
            } else {
                assertTrue(game._nextStepQueueContainsCell(cell));
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
        cell = Mockito.spy(cell);
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
        cell = Mockito.spy(cell);
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
            cell = Mockito.spy(cell);
            when(cell.isAlive()).thenReturn(true);

            when(cell.getLivingNeighborCount()).thenReturn(i);

            assertTrue(cell.isAlive());
            assertFalse(cell.isAliveNextStep());
            cell.determineNextState();
            assertTrue(cell.isAlive());
            assertFalse(cell.isAliveNextStep());
        }
    }

}