package conlife.utils;

import conlife.Rules;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Supports reading initial game conditions in the format of Life 1.05. Can read from a String or from a File.
 */
public class Lif1_5Reader {

    private final static Pattern DELIMITER_PATTERN = Pattern.compile("[\\r\\n;]+");
    private final static Pattern VERSION_PATTERN = Pattern.compile("(?:#(Life 1.05))");
    private final static Pattern DESCRIPTION_PATTERN = Pattern.compile("(?:#D.*)");
    private final static Pattern RULES_PATTERN = Pattern.compile("#R ([012345678]+)\\/([012345678]+)");
    private final static Pattern DEFAULT_RULES_PATTERN = Pattern.compile("(?:#N)");
    private final static Pattern POSITION_PATTERN = Pattern.compile("#P (-?\\d+) (-?\\d+)");
    private final static Pattern BOARD_LINE_PATTERN = Pattern.compile("(\\*|\\.)+");

    public static Lif1_5Reader fromFile(Dimension boardSize, File file) throws FileNotFoundException, ParseException, Rules.RulesException {
        Scanner scanner = new Scanner(file);
        return new Lif1_5Reader(boardSize, scanner);
    }

    public static Lif1_5Reader fromString(Dimension boardSize, String string) throws ParseException, Rules.RulesException {
        Scanner scanner = new Scanner(string);
        return new Lif1_5Reader(boardSize, scanner);
    }

    private boolean[][] board;
    private Rules rules;
    private int boardWidth, boardHeight;

    private Lif1_5Reader(Dimension boardSize, Scanner scanner) throws ParseException, Rules.RulesException {
        boardWidth = (int) boardSize.getWidth();
        boardHeight = (int) boardSize.getHeight();
        int centerX = (int) Math.floor((double) boardWidth / 2D);
        int centerY = (int) Math.floor((double) boardHeight / 2D);
        board = new boolean[boardHeight][boardWidth];

        scanner.useDelimiter(DELIMITER_PATTERN);

        if (scanner.hasNext(VERSION_PATTERN)) {
            String version = scanner.next(VERSION_PATTERN);
            Matcher matcher = VERSION_PATTERN.matcher(version);
            matcher.matches();
            System.out.printf("Using Format: %s\n", matcher.group(1));
        } else {
            throw new ParseException("Unknown format", -1);
        }

        while (scanner.hasNext(DESCRIPTION_PATTERN)) {
            scanner.next();
        }

        String rulesString = "#R 23/3";
        if (scanner.hasNext(RULES_PATTERN)) {
            rulesString = scanner.next(RULES_PATTERN);
        }
        if (scanner.hasNext(DEFAULT_RULES_PATTERN)) {
            scanner.next(DEFAULT_RULES_PATTERN);
        }
        Matcher rulesMatcher = RULES_PATTERN.matcher(rulesString);
        rulesMatcher.matches();
        String surviveRules = rulesMatcher.group(1);
        String birthRules = rulesMatcher.group(2);
        rules = Rules.parseRules("B" + birthRules + "/S" + surviveRules);

        while (scanner.hasNext(POSITION_PATTERN)) {
            String positionPattern = scanner.next();
            Matcher positionMatcher = POSITION_PATTERN.matcher(positionPattern);
            positionMatcher.matches();
            int originX = centerX + Integer.parseInt(positionMatcher.group(1));
            int originY = centerY + Integer.parseInt(positionMatcher.group(2));
            int y = originY;
            while (scanner.hasNext(BOARD_LINE_PATTERN)) {
                String line = scanner.next();
                for (int j = 0, x = originX; j < line.length(); j++, x++) {
                    if (line.charAt(j) == '*') {
                        board[y][x] = true;
                    }
                }
                y++;
            }
        }
    }

    public boolean[][] getBoard() {
        return board;
    }

    public String createBoardString(char deadCell, char liveCell) {
        StringBuilder builder = new StringBuilder();
        for (int y = 0; y < boardHeight; y++) {
            if (y != 0) {
                builder.append("\n");
            }
            for (int x = 0; x < boardWidth; x++) {
                builder.append(board[y][x] ? liveCell : deadCell);
            }
        }
        return builder.toString();
    }

    public Rules getRules() {
        return rules;
    }
}
