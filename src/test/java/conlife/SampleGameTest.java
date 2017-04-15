package conlife;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SampleGameTest {

    private static final String initialCondition
            = "0000000\n"
            + "0###000\n"
            + "0000000\n"
            + "0000000\n"
            + "0000000\n"
            + "000##00\n"
            + "000##00\n"
            + "0000000\n"
            + "0000000";

    private static final String step1
            = "00#0000\n"
            + "00#0000\n"
            + "00#0000\n"
            + "0000000\n"
            + "0000000\n"
            + "000##00\n"
            + "000##00\n"
            + "0000000\n"
            + "0000000";

    private static final String step2
            = "0000000\n"
            + "0###000\n"
            + "0000000\n"
            + "0000000\n"
            + "0000000\n"
            + "000##00\n"
            + "000##00\n"
            + "0000000\n"
            + "0000000";

    private GameState game;

    @Before
    public void setup() {
        game = GameState.createNewGame(initialCondition.split("\n"), '#');
    }

    @Test
    public void testSampleGame() {
        assertEquals(initialCondition, game.createBoardString('0', '#'));
        game.processGameStep();
        assertEquals(step1, game.createBoardString('0', '#'));
        game.processGameStep();
        assertEquals(step2, game.createBoardString('0', '#'));
    }
}
