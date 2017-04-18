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
        Cell cell = game.getCell(0,1);
        assertEquals(initialCondition, game.createBoardString('.', '#'));

        game.processGameStep();
        assertEquals(step1, game.createBoardString('.', '#'));

        game.processGameStep();
        assertEquals(step2, game.createBoardString('.', '#'));

        game.processGameStep();
        assertEquals(step3, game.createBoardString('.', '#'));

        game.processGameStep();
        assertEquals(step4, game.createBoardString('.', '#'));

        game.processGameStep();
        assertEquals(step5, game.createBoardString('.', '#'));

        game.processGameStep();
        assertEquals(step6, game.createBoardString('.', '#'));
    }
}
