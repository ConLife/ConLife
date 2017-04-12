package conlife;

import java.text.ParseException;

public class GameState {

    private static final int BOARD_WIDTH = 100, BOARD_HEIGHT = 100;

    public static final String DEFAULT_RULES_STRING = "B03/S23";

    // synchronization of the rules is probably not a concern...
    private static final Object rulesLock = new Object();
    static Rules rules;

    static {
        try {
            rules = Rules.parseRules(DEFAULT_RULES_STRING);
        } catch (ParseException e) {
            throw new RuntimeException("Initial rules are busted");
        }

    }


    static void updateRules(Rules rules) {
        synchronized (rulesLock) {
            GameState.rules = rules;
        }
    }

    private int currentStep = 0;
    private final int maxStep;

    private Cell[][] board;

    static GameState createNewGame(int maxStep) {
        return new GameState(maxStep);
    }

    private GameState(int maxStep) {
        this.maxStep = maxStep;
        board = new Cell[BOARD_WIDTH][BOARD_HEIGHT];

        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                board[x][y] = new Cell(x, y);
            }
        }

        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                for (Direction d : Direction.values()) {
                    // setup neighbors
                }
            }
        }
    }


}
