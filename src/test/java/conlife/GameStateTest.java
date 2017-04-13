package conlife;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameStateTest {

    private GameState game;

    @Before
    public void setup() {
        game = GameState.createNewGame(100);
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
}