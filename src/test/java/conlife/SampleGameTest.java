package conlife;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SampleGameTest {

    private static final String initialCondition
            = ".......\n"
            + ".###...\n"
            + ".......\n"
            + ".......\n"
            + ".......\n"
            + "...##..\n"
            + "...##..\n"
            + ".......\n"
            + ".......";

    private static final String step1
            = "..#....\n"
            + "..#....\n"
            + "..#....\n"
            + ".......\n"
            + ".......\n"
            + "...##..\n"
            + "...##..\n"
            + ".......\n"
            + ".......";

    private static final String step2
            = ".......\n"
            + ".###...\n"
            + ".......\n"
            + ".......\n"
            + ".......\n"
            + "...##..\n"
            + "...##..\n"
            + ".......\n"
            + ".......";

    private GameState game;

    @Before
    public void setup() {
        game = GameState.createNewGame(initialCondition.split("\n"), '#');
    }

    @Test
    public void testSampleGame() {
        assertEquals(initialCondition, game.createBoardString('.', '#'));
        game.processGameStep();
        assertEquals(step1, game.createBoardString('.', '#'));
        game.processGameStep();
        assertEquals(step2, game.createBoardString('.', '#'));
    }
}
