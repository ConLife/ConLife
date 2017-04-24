package conlife.console;

import conlife.GameState;
import conlife.Rules;
import conlife.utils.Lif1_5Reader;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import static java.lang.System.nanoTime;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConlifeCLI {
    private static GameState gameState;
    private static Lif1_5Reader reader;
    private final static int boardSize = 1000;
    private final static int maxStep = 500;
    private final static String sampleFile = "./samples/LINEPUF.LIF";
    private static int threadCount = 4;
    private final static double NANOSECONDS_TO_MILLISECONDS = 1.0 / 1000000.0;

    public static void main(String[] args) throws FileNotFoundException, ParseException, Rules.RulesException, IOException {
        reader = Lif1_5Reader.fromFile(new Dimension (boardSize, boardSize), new File(sampleFile));
        String[] init = reader.createBoardString('.', '*').split("\n");
        gameState = GameState.createNewGame(init, '*', threadCount);

        long start = System.nanoTime();
        for (int step = 0; step < maxStep; step++) {
            gameState.processGameStep();
        }
        long end = System.nanoTime();
        long time = end - start;
        double totalTime = time * NANOSECONDS_TO_MILLISECONDS;
        String report = sampleFile + "\t" + boardSize + "\t" + maxStep + "\t" + threadCount + "\t" + totalTime + "\n";
        try {
            Files.write(Paths.get("timings.txt"), report.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) { 
        }
    }
}
