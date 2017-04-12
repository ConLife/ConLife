package conlife;

enum Direction {

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
                if (localX >= boardWidth) {
                    x = 0;
                }
                return x;
            case NORTH_WEST:
            case WEST:
            case SOUTH_WEST:
                x = localX - 1;
                if (localX < 0) {
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
                if (localY >= boardHeight) {
                    y = 0;
                }
                return y;
            case NORTH_WEST:
            case NORTH:
            case NORTH_EAST:
                y = localY - 1;
                if (localY < 0) {
                    y = boardHeight - 1;
                }
                return y;
            default:
                return localY;
        }
    }
}
