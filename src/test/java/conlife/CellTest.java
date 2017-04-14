package conlife;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class CellTest {

    GameState game;

    @Before
    public void setup() throws Exception {
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
    public void testBirth() throws Exception {
        Cell cell = game.getCell(5, 5);
        cell = Mockito.spy(cell);

        Mockito.when(cell.getLivingNeighborCount()).thenReturn(3);

        assertFalse(cell.isAlive());
        assertFalse(cell.isAliveNextStep());

        cell.determineNextState();
        assertFalse(cell.isAlive());
        assertTrue(cell.isAliveNextStep());

        for (int i = 0; i < 9; i++) {
            if (i == 3) {
                continue;
            }
            setup();
            cell = game.getCell(5, 5);
            cell = Mockito.spy(cell);

            Mockito.when(cell.getLivingNeighborCount()).thenReturn(i);

            assertFalse(cell.isAlive());
            assertFalse(cell.isAliveNextStep());

            cell.determineNextState();
            assertFalse(cell.isAlive());
            assertFalse(cell.isAliveNextStep());
        }

        setup();
        cell = game.getCell(5, 5);
        cell = Mockito.spy(cell);
        cell._setCurrentlyAlive(true);
        assertTrue(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        Mockito.when(cell.getLivingNeighborCount()).thenReturn(3);
        assertTrue(cell.isAlive());
        assertTrue(cell.isAliveNextStep());
    }

    @Test
    public void testSurvive() throws Exception {
        Cell cell = game.getCell(5, 5);
        cell = Mockito.spy(cell);
        cell._setCurrentlyAlive(true);

        Mockito.when(cell.getLivingNeighborCount()).thenReturn(3);

        assertTrue(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        cell.determineNextState();
        assertTrue(cell.isAlive());
        assertTrue(cell.isAliveNextStep());

        setup();
        cell = game.getCell(5, 5);
        cell = Mockito.spy(cell);
        cell._setCurrentlyAlive(true);

        Mockito.when(cell.getLivingNeighborCount()).thenReturn(2);

        assertTrue(cell.isAlive());
        assertFalse(cell.isAliveNextStep());
        cell.determineNextState();
        assertTrue(cell.isAlive());
        assertTrue(cell.isAliveNextStep());

        for (int i = 0; i < 9; i++) {
            if (i == 3 || i == 2) {
                continue;
            }
            setup();
            cell = game.getCell(5, 5);
            cell = Mockito.spy(cell);
            cell._setCurrentlyAlive(true);

            Mockito.when(cell.getLivingNeighborCount()).thenReturn(i);

            assertTrue(cell.isAlive());
            assertFalse(cell.isAliveNextStep());
            cell.determineNextState();
            assertTrue(cell.isAlive());
            assertFalse(cell.isAliveNextStep());
        }
    }

}