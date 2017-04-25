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
    private static int boardSize = 1000;
    private static int totalSteps = 500;
    private static String inputFile = "./samples/LINEPUF.LIF";
    private static int threadCount = 4;
    private final static double NANOSECONDS_TO_MILLISECONDS = 1.0 / 1000000.0;
    
    public static void main(String[] args) throws FileNotFoundException, ParseException, Rules.RulesException, IOException {
        if (args.length > 0) {
            inputFile = args[0];
            if (args.length > 1) {
                boardSize = Integer.parseInt(args[1]);
                if (args.length > 2) {
                    totalSteps = Integer.parseInt(args[2]);
                    if (args.length > 3) {
                        threadCount = Integer.parseInt(args[3]);
                    }
                }
            }
        }
        reader = Lif1_5Reader.fromFile(new Dimension(boardSize, boardSize), new File(inputFile));
        String[] init = reader.createBoardString('.', '*').split("\n");
        gameState = GameState.createNewGame(init, '*', threadCount);
        
        long start = System.nanoTime();
        for (int step = 0; step < totalSteps; step++) {
            gameState.processGameStep();
        }
        
        long end = System.nanoTime();
        long time = end - start;
        double totalTime = time * NANOSECONDS_TO_MILLISECONDS;
        
        String report = String.format("%s\t%d\t%d\t%d\t%f\n", inputFile, boardSize, totalSteps, threadCount, totalTime);
        try {
            Files.write(Paths.get("timings.txt"), report.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        System.exit(0);
    }
}
