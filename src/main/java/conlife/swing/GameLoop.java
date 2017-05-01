package conlife.swing;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles running the game in a loop for the GUI version.
 *
 * @author Jeremy Wood
 */
class GameLoop implements Runnable {

    static final int MAX_TIME_PER_GAME_LOOP = 2000;
    static final int MIN_TIME_PER_GAME_LOOP = 200;
    static final int DEFAULT_TIME_PER_GAME_LOOP = 1000;

    private final ConlifeGUI main;
    final AtomicBoolean running = new AtomicBoolean(false);

    final AtomicInteger timePerLoopMs = new AtomicInteger(1000); // default is 1 sec

    GameLoop(ConlifeGUI main) {
        this.main = main;
    }

    @Override
    public void run() {
        long previousTime = System.currentTimeMillis();
        while (running.get()) {
            SwingUtilities.invokeLater(main::step);

            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - previousTime;
            int timePerLoop;
            while (deltaTime < (timePerLoop = timePerLoopMs.get())) {
                try {
                    Thread.sleep(timePerLoop - deltaTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                currentTime = System.currentTimeMillis();
                deltaTime = currentTime - previousTime;
            }
            previousTime = currentTime;
        }
    }
}
