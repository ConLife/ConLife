package conlife.console;

import conlife.GameState;
import conlife.Rules;
import conlife.utils.Lif1_5Reader;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConlifeCLI {

    private static GameState gameState;
    private static Lif1_5Reader reader;
    private final static int BOARD_SIZE = 1000;
    private final static int TOTAL_STEPS = 500;
    private final static String SAMPLE_FILE = "./samples/LINEPUF.LIF";
    private static int threadCount = 4;
    private final static double NANOSECONDS_TO_MILLISECONDS = 1.0 / 1000000.0;

    public static void main(String[] args) throws FileNotFoundException, ParseException, Rules.RulesException, IOException {
        reader = Lif1_5Reader.fromFile(new Dimension(BOARD_SIZE, BOARD_SIZE), new File(SAMPLE_FILE));
        String[] init = reader.createBoardString('.', '*').split("\n");
        gameState = GameState.createNewGame(init, '*', threadCount);

        long start = System.nanoTime();
        for (int step = 0; step < TOTAL_STEPS; step++) {
            gameState.processGameStep();
        }
        long end = System.nanoTime();
        long time = end - start;
        double totalTime = time * NANOSECONDS_TO_MILLISECONDS;
        String report = String.format("%s\t%d\t%d\t%d\t%f\n", SAMPLE_FILE, BOARD_SIZE, TOTAL_STEPS, threadCount, totalTime);
        try {
            Files.write(Paths.get("timings.txt"), report.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
        }
    }
}
