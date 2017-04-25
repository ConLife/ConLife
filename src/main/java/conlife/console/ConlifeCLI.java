package conlife.console;

import conlife.GameState;
import conlife.Rules;
import conlife.utils.Lif1_5Reader;
import conlife.utils.PgmWriter;
import static conlife.utils.PgmWriter.createPgmWriter;
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
    private static String inFile = "./samples/LINEPUF.LIF";
    private static int threadCount = 4;
    private final static double NANOSECONDS_TO_MILLISECONDS = 1.0 / 1000000.0;
    private static boolean outputs = false;
    private static PgmWriter writer;

    private static void parseArgs(String[] args) {
        int argsi = 0;
        while (args.length > argsi) {
            if (args[argsi].charAt(0) == '-') {
                switch (args[argsi].charAt(1)) {
                    case 'f'://input file name
                        inFile = args[argsi].substring(2);
                        break;
                    case 'b'://board size
                        boardSize = Integer.parseInt(args[argsi].substring(2));
                        break;
                    case 's'://total steps
                        totalSteps = Integer.parseInt(args[argsi].substring(2));
                        break;
                    case 't'://threads
                        threadCount = Integer.parseInt(args[argsi].substring(2));
                        break;
                    case 'o'://ouputs wanted
                        outputs = true;
                        break;
                }
                argsi++;
            }
        }
    }

    private static void runGame() {
        if (outputs) {
            writer.createOutputForCurrentGameStep();
        }
        for (int step = 0; step < totalSteps; step++) {
            gameState.processGameStep();
            if (outputs) {
                writer.createOutputForCurrentGameStep();
            }
        }
    }

    private static void report(double totalTime) {
        String report = String.format("%s\t%d\t%d\t%d\t%f\n", inFile, boardSize, totalSteps, threadCount, totalTime);
        try {
            Files.write(Paths.get("timings.txt"), report.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private static void init() throws FileNotFoundException, ParseException, Rules.RulesException, IOException {
        reader = Lif1_5Reader.fromFile(new Dimension(boardSize, boardSize), new File(inFile));
        gameState = GameState.createNewGame(reader.createBoardString('.', '*').split("\n"), '*', threadCount);
        if (outputs) {
            writer = createPgmWriter(new File("./testOutput"), gameState);
        }
    }

    private static double calcTime(long start){
        long end = System.nanoTime();
        long time = end - start;
        return (time * NANOSECONDS_TO_MILLISECONDS);
    }
    
    public static void main(String[] args) throws FileNotFoundException, ParseException, Rules.RulesException, IOException {
        parseArgs(args);
        init();
        long start = System.nanoTime();
        runGame();
        double totalTime = calcTime(start);
        report(totalTime);
        System.exit(0);
    }
}
