package conlife.swing;

class CellUpdate {
    private final int x, y;
    private final boolean alive;

    CellUpdate(int x, int y, boolean alive) {
        this.x = x;
        this.y = y;
        this.alive = alive;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    boolean isAlive() {
        return alive;
    }
}
