package conlife;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameStateTest {

    private static final String sampleCondition1
            = "00#0000\n"
            + "0#00000\n"
            + "00000#0\n"
            + "00#0000";
    private static final String sampleCondition2 = "00\n" + "00";


    private GameState game;

    @Before
    public void setup() {
        game = GameState.createNewGame();
    }

    @Test
    public void testBoardSetup() {
        for (int x = 0; x < game.getBoardWidth(); x++) {
            for (int y = 0; y < game.getBoardHeight(); y++) {
                Cell cell = game.getCell(x, y);
                assertNotNull(cell);
                for (Direction d : Direction.values()) {
                    Cell neighbor = cell.getNeighbor(d);
                    assertNotNull(neighbor);
                    assertSame(cell, neighbor.getNeighbor(d.getOpposite()));
                }
            }
        }
    }

    @Test
    public void testNewGameFromInitialConditionString() {
        GameState game = GameState.createNewGame(sampleCondition1.split("\n"), '#');
        assertTrue(game.getCell(2, 0).isAlive());
        assertTrue(game.getCell(1, 1).isAlive());
        assertTrue(game.getCell(5, 2).isAlive());
        assertTrue(game.getCell(2, 3).isAlive());
        assertEquals(sampleCondition1, game.createBoardString('0','#'));

        game = GameState.createNewGame(sampleCondition2.split("\n"), '#');
        assertFalse(game.getCell(0,0).isAlive());
        assertFalse(game.getCell(1,0).isAlive());
        assertFalse(game.getCell(0,1).isAlive());
        assertFalse(game.getCell(1,1).isAlive());
        assertEquals(sampleCondition2, game.createBoardString('0','#'));
    }
}