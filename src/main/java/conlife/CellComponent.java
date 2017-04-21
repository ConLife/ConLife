package conlife;

import javax.swing.*;
import java.awt.*;

public class CellComponent extends JComponent {

    public static final int CELL_SIZE = 8;

    private static final Color ALIVE_COLOR = Color.red;
    private static final Color DEAD_COLOR = Color.white;

    private final int cellX, cellY;
    private boolean alive = false;

    public CellComponent(int x, int y) {
        this.cellX = x;
        this.cellY = y;
    }

    @Override
    public void paintComponent(Graphics g){
        this.setOpaque(false);
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(alive ? ALIVE_COLOR : DEAD_COLOR);
        g2.fillOval(0, 0, CELL_SIZE, CELL_SIZE);
    }
    @Override
    public  Dimension getPreferredSize(){
        return new Dimension(CELL_SIZE, CELL_SIZE);
    }

    public boolean setAlive(boolean alive) {
        boolean result = alive != this.alive;
        if (result) {
            SwingUtilities.invokeLater(this::repaint);
        }
        this.alive = alive;
        return result;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public int getCellX() {
        return cellX;
    }

    public int getCellY() {
        return cellY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CellComponent that = (CellComponent) o;

        if (cellX != that.cellY) return false;
        return cellY == that.cellY;
    }

    @Override
    public int hashCode() {
        int result = cellX;
        result = 31 * result + cellY;
        return result;
    }
}
