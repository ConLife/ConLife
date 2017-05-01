package conlife.swing;

class CellUpdate {
    private final int x, y;
    private final boolean alive;

    public CellUpdate(int x, int y, boolean alive) {
        this.x = x;
        this.y = y;
        this.alive = alive;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isAlive() {
        return alive;
    }
}
