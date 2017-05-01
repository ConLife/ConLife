package conlife.console;

import conlife.GameState;
import conlife.Rules;
import conlife.utils.Lif1_5Reader;
import conlife.utils.PgmWriter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;

import static conlife.utils.PgmWriter.createPgmWriter;

/**
 * A program to run timing tests and create pgm image files of the game board.
 *
 * @author Nathan Coggins
 */
public class ConlifeCLI {

    private static final int WARMUP_LOOPS = 10000;
    private final static double NANOSECONDS_TO_MILLISECONDS = 1.0 / 1000000.0;
    private static GameState gameState;
    private static Lif1_5Reader reader;
    private static int boardSize = 1000;
    private static int totalSteps = 500;
    private static String inFile = "./samples/LINEPUF.LIF";
    private static int threadCount = 4;
    private static boolean outputs = false;
    private static PgmWriter writer;

    private static void parseArgs(String[] args) {
        int argsi = 0;
        while (args.length > argsi) {
            if (args[argsi].charAt(0) == '-') {
                char flag = args[argsi].charAt(1);
                argsi++; // We want to know what arg comes after the flag
                try {
                    switch (flag) {
                        case 'f'://input file name
                            inFile = args[argsi];
                            break;
                        case 'b'://board size
                            boardSize = Integer.parseInt(args[argsi]);
                            break;
                        case 's'://total steps
                            totalSteps = Integer.parseInt(args[argsi]);
                            break;
                        case 't'://threads
                            threadCount = Integer.parseInt(args[argsi]);
                            break;
                        case 'o'://ouputs wanted
                            argsi--; // This flag isn't interested in the next arg
                            outputs = true;
                            break;
                    }
                } catch (NumberFormatException e) {
                    System.out.printf("Was expecting number for flag -%c but received \"%s\" instead. Using default...\n",
                            flag, args[argsi]);
                }
            }
            argsi++;
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

    private static void init() throws ParseException, Rules.RulesException, IOException {
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

    private static void warmup() throws ParseException, IOException, Rules.RulesException {
        int blocks = (int) Math.ceil(WARMUP_LOOPS / (double) totalSteps);
        System.out.printf("Warming up the JVM with %d game runs", blocks);
        for (int i = 0; i < blocks; i++) {
            init();
            runGame();
            System.out.print(".");
        }
    }

    public static void main(String[] args) throws ParseException, Rules.RulesException, IOException {
        parseArgs(args);
        if (!outputs) {
            warmup();
        }
        System.out.println("Performing timing");
        try {
            init();
        } catch (ParseException e) {
            System.out.println("Unable to read initial conditions.");
            System.exit(1);
        } catch (Rules.RulesException e) {
            System.out.println("Error with the current rules.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Unable to open initial conditions file.");
            System.exit(1);
        }
        long start = System.nanoTime();
        runGame();
        double totalTime = calcTime(start);
        report(totalTime);
        System.exit(0);
    }
}
