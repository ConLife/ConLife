package conlife;

/**
 * Cardinal directions used to help find cell neighbors.
 *
 * @author Jeremy Wood, Nathan Coggins
 */
enum Direction {

    // DO NOT CHANGE THE ORDER OF THESE
    NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;

    public int getNeighborX(int localX, int boardWidth) {
        int x;
        switch (this) {
            case NORTH:
            case SOUTH:
                return localX;
            case NORTH_EAST:
            case EAST:
            case SOUTH_EAST:
                x = localX + 1;
                if (x >= boardWidth) {
                    x = 0;
                }
                return x;
            case NORTH_WEST:
            case WEST:
            case SOUTH_WEST:
                x = localX - 1;
                if (x < 0) {
                    x = boardWidth - 1;
                }
                return x;
            default:
                return localX;
        }
    }

    public int getNeighborY(int localY, int boardHeight) {
        int y;
        switch (this) {
            case EAST:
            case WEST:
                return localY;
            case SOUTH_WEST:
            case SOUTH:
            case SOUTH_EAST:
                y = localY + 1;
                if (y >= boardHeight) {
                    y = 0;
                }
                return y;
            case NORTH_WEST:
            case NORTH:
            case NORTH_EAST:
                y = localY - 1;
                if (y < 0) {
                    y = boardHeight - 1;
                }
                return y;
            default:
                return localY;
        }
    }

    public Direction getOpposite() {
        int ordinal = this.ordinal() + Direction.values().length / 2;
        if (ordinal >= Direction.values().length) {
            ordinal = ordinal - Direction.values().length;
        }
        return Direction.values()[ordinal];
    }
}
