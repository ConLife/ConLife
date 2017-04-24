package conlife.utils;

import conlife.GameState;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PgmWriter {

    // Sample of how to use PgmWriter
    public static void main(String[] args) throws Exception {
        Lif1_5Reader reader = Lif1_5Reader.fromFile(new Dimension(1000, 1000), new File("./samples/LINEPUF.LIF"));
        GameState game = GameState.createNewGame(reader.createBoardString('.', '*').split("\n"), '*');
        PgmWriter writer = createPgmWriter(new File("./testOutput"), game);
        writer.createOutputForCurrentGameStep();
        game.processGameStep();
        writer.createOutputForCurrentGameStep();
        //...
    }

    /**
     * Creates a PgmWriter that can be used to create PGM file output in a given directory.
     *
     * @param outputDirectory the directory to store all the pgm files.
     * @param gameState the game state to create pgm output for.
     * @return a new PgmWriter.
     */
    public static PgmWriter createPgmWriter(File outputDirectory, GameState gameState) throws IOException {
        return new PgmWriter(outputDirectory, gameState);
    }

    private final File outputDirectory;
    private final GameState gameState;

    private PgmWriter(File outputDirectory, GameState gameState) throws IOException {
        this.outputDirectory = outputDirectory;
        this.gameState = gameState;

        if (!outputDirectory.exists() && !outputDirectory.mkdir()) {
            throw new IOException("The output directory cannot be found or created");
        }
    }

    int currentOutputNum = 0;

    public void createOutputForCurrentGameStep() {
        writeOutputToFile(createOutputString());
    }

    private String createOutputString() {
        StringBuilder b = new StringBuilder();
        b.append("P2\n");
        b.append(gameState.getBoardWidth()).append(" ").append(gameState.getBoardHeight()).append("\n");
        b.append("1\n");
        for (int y = 0; y < gameState.getBoardHeight(); y++) {
            for (int x = 0; x < gameState.getBoardWidth(); x++) {
                if (x != 0) {
                    b.append(" ");
                }
                b.append(gameState.getCell(x, y).isAlive() ? 1 : 0);
            }
            b.append("\n");
        }
        return b.toString();
    }

    private void writeOutputToFile(String outputString) {
        String fileName = "out" + getStepString() + ".pgm";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputDirectory, fileName)))) {
            writer.write(outputString);
        } catch (IOException e) {
            System.out.println("Unable to create " + fileName + " for game step " + gameState.getCurrentStep());
            e.printStackTrace();
        }
    }

    private String getStepString() {
        StringBuilder b = new StringBuilder();
        for (int i = 1000; i >= 10; i /= 10) {
            if (currentOutputNum / i < 1) {
                b.append("0");
            } else {
                break;
            }
        }
        b.append(currentOutputNum);
        return b.toString();
    }
}
