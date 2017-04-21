package conlife;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SampleGameFilledSquareNoWrapTest {

    private static final String initialCondition
            = "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "....######....\n"
            + "....######....\n"
            + "....######....\n"
            + "....######....\n"
            + "....######....\n"
            + "....######....\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............";

    private static final String step1
            = "..............\n"
            + "..............\n"
            + "..............\n"
            + ".....####.....\n"
            + "....#....#....\n"
            + "...#......#...\n"
            + "...#......#...\n"
            + "...#......#...\n"
            + "...#......#...\n"
            + "....#....#....\n"
            + ".....####.....\n"
            + "..............\n"
            + "..............\n"
            + "..............";

    private static final String step2
            = "..............\n"
            + "..............\n"
            + "......##......\n"
            + ".....####.....\n"
            + "....######....\n"
            + "...##....##...\n"
            + "..###....###..\n"
            + "..###....###..\n"
            + "...##....##...\n"
            + "....######....\n"
            + ".....####.....\n"
            + "......##......\n"
            + "..............\n"
            + "..............";

    private static final String step3
            = "..............\n"
            + "..............\n"
            + ".....#..#.....\n"
            + "....#....#....\n"
            + "...#......#...\n"
            + "..#...##...#..\n"
            + ".....#..#.....\n"
            + ".....#..#.....\n"
            + "..#...##...#..\n"
            + "...#......#...\n"
            + "....#....#....\n"
            + ".....#..#.....\n"
            + "..............\n"
            + "..............";

    private static final String step4
            = "..............\n"
            + "..............\n"
            + "..............\n"
            + "....#....#....\n"
            + "...#......#...\n"
            + "......##......\n"
            + ".....#..#.....\n"
            + ".....#..#.....\n"
            + "......##......\n"
            + "...#......#...\n"
            + "....#....#....\n"
            + "..............\n"
            + "..............\n"
            + "..............";

    private static final String step5
            = "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "......##......\n"
            + ".....#..#.....\n"
            + ".....#..#.....\n"
            + "......##......\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............";

    private static final String step6
            = "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "......##......\n"
            + ".....#..#.....\n"
            + ".....#..#.....\n"
            + "......##......\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............\n"
            + "..............";

    private GameState game;

    @Before
    public void setup() {
        game = GameState.createNewGame(initialCondition.split("\n"), '#');
    }

    @Test
    public void testSampleGame() {
        Cell cell = game.getCell(5, 2);
        assertEquals("Step: " + game.getCurrentStep(), initialCondition, game.createBoardString('.', '#'));

        assertEquals(game.getBoardWidth() * game.getBoardHeight(), game.currentCellQueue.size());
        game.processGameStep();
        assertEquals(88, game.currentCellQueue.size());
        assertEquals("Step: " + game.getCurrentStep(), step1, game.createBoardString('.', '#'));

        Cell neighbor = game.getCell(5, 3);
        //assertTrue(game.currentCellQueue.contains(neighbor));
        assertEquals(Rules.Rule.SURVIVE, game.getRules().getRule(neighbor.isAlive(), neighbor.getLivingNeighborCount()));
        game._determineCellsNextState();
        assertTrue(neighbor.isAliveNextStep());
        game._updateCellStates();
        assertTrue(game.nextStepCellQueue.contains(cell));
        game._copyNextCellQueueToCurrent();
        game._incrementGameStep();
        assertEquals("Step: " + game.getCurrentStep(), step2, game.createBoardString('.', '#'));


        assertEquals(3, cell.getLivingNeighborCount());
        assertFalse(cell.isAlive());
        //assertTrue(game.currentCellQueue.contains(cell));
        game._determineCellsNextState();
        assertTrue(cell.isAliveNextStep());
        game._updateCellStates();
        game._copyNextCellQueueToCurrent();
        game._incrementGameStep();
        assertEquals("Step: " + game.getCurrentStep(), step3, game.createBoardString('.', '#'));

        game.processGameStep();
        assertEquals("Step: " + game.getCurrentStep(), step4, game.createBoardString('.', '#'));

        game.processGameStep();
        assertEquals("Step: " + game.getCurrentStep(), step5, game.createBoardString('.', '#'));

        game.processGameStep();
        assertEquals("Step: " + game.getCurrentStep(), step6, game.createBoardString('.', '#'));
    }
}
