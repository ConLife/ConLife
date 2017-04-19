package conlife;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SampleGameFilledSquareTest {

    private static final String initialCondition
            = "........\n"
            + ".######.\n"
            + ".######.\n"
            + ".######.\n"
            + ".######.\n"
            + ".######.\n"
            + ".######.\n"
            + ".######.\n"
            + "........";

    private static final String step1
            = "..####..\n"
            + ".#....#.\n"
            + "#......#\n"
            + "#......#\n"
            + "#......#\n"
            + "#......#\n"
            + "#......#\n"
            + ".#....#.\n"
            + "..####..";

    private static final String step2
            = ".#....#.\n"
            + "########\n"
            + ".#....#.\n"
            + ".#....#.\n"
            + ".#....#.\n"
            + ".#....#.\n"
            + ".#....#.\n"
            + "########\n"
            + ".#....#.";

    private static final String step3
            = "...##...\n"
            + "...##...\n"
            + "...##...\n"
            + "###..###\n"
            + "###..###\n"
            + "###..###\n"
            + "...##...\n"
            + "...##...\n"
            + "...##...";

    private GameState game;

    @Before
    public void setup() {
        game = GameState.createNewGame(initialCondition.split("\n"), '#');
    }

    @Test
    public void testSampleGame() {
        Cell cell = game.getCell(0,1);
        assertEquals(initialCondition, game.createBoardString('.', '#'));

        game._determineCellsNextState();
        assertTrue(cell.isAddedToNextStepQueue().get());
        assertTrue(cell.isAddedToUpdateQueue());
        game._updateCellStates();
        game._copyNextCellQueueToCurrent();
        assertEquals(step1, game.createBoardString('.', '#'));

        //game.processGameStep();
        assertTrue(game.currentCellQueue.contains(cell));
        assertFalse(cell.isAddedToNextStepQueue().get());
        game._determineCellsNextState();
        assertTrue(cell.isAddedToNextStepQueue().get());
        assertTrue(game.nextStepCellQueue.contains(cell));
        game._updateCellStates();
        game._copyNextCellQueueToCurrent();
        assertEquals(step2, game.createBoardString('.', '#'));
        assertTrue(game.currentCellQueue.contains(cell));

        game._determineCellsNextState();
        game._updateCellStates();
        game._copyNextCellQueueToCurrent();
        assertEquals(step3, game.createBoardString('.', '#'));
    }
}
