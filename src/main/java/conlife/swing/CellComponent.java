package conlife.swing;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The visual representation of a cell on the game board.
 *
 * @author Jeremy Wood
 */
class CellComponent extends JComponent {

    static final int CELL_SIZE = 8;

    private static final Color ALIVE_COLOR = Color.red;
    private static final Color DEAD_COLOR = Color.white;

    private final int cellX, cellY;
    private AtomicBoolean alive = new AtomicBoolean(false);

    CellComponent(int x, int y) {
        this.cellX = x;
        this.cellY = y;
    }

    @Override
    public void paintComponent(Graphics g){
        this.setOpaque(false);
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(alive.get() ? ALIVE_COLOR : DEAD_COLOR);
        g2.fillOval(0, 0, CELL_SIZE, CELL_SIZE);
    }
    @Override
    public  Dimension getPreferredSize(){
        return new Dimension(CELL_SIZE, CELL_SIZE);
    }

    /**
     * Sets the alive state of this cell and return whether the new state is different than the previous state.
     *
     * @param alive the new state
     * @return true if the new state is different from the previous state.
     */
    boolean setAlive(boolean alive) {
        return alive != this.alive.getAndSet(alive);
    }

    boolean isAlive() {
        return this.alive.get();
    }

    int getCellX() {
        return cellX;
    }

    int getCellY() {
        return cellY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CellComponent that = (CellComponent) o;

        return cellX == that.cellX && cellY == that.cellY;
    }

    @Override
    public int hashCode() {
        int result = cellX;
        result = 31 * result + cellY;
        return result;
    }
}
