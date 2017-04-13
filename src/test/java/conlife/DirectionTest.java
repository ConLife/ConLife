package conlife;

import org.junit.Test;

import static org.junit.Assert.*;

public class DirectionTest {

    @Test
    public void getNeighborX() throws Exception {
        assertEquals(0, Direction.NORTH.getNeighborX(0, 10));
        assertEquals(1, Direction.NORTH_EAST.getNeighborX(0, 10));
        assertEquals(1, Direction.EAST.getNeighborX(0, 10));
        assertEquals(1, Direction.SOUTH_EAST.getNeighborX(0, 10));
        assertEquals(0, Direction.SOUTH.getNeighborX(0, 10));
        assertEquals(9, Direction.SOUTH_WEST.getNeighborX(0, 10));
        assertEquals(9, Direction.WEST.getNeighborX(0, 10));
        assertEquals(9, Direction.NORTH_WEST.getNeighborX(0, 10));

        assertEquals(9, Direction.NORTH.getNeighborX(9, 10));
        assertEquals(0, Direction.NORTH_EAST.getNeighborX(9, 10));
        assertEquals(0, Direction.EAST.getNeighborX(9, 10));
        assertEquals(0, Direction.SOUTH_EAST.getNeighborX(9, 10));
        assertEquals(9, Direction.SOUTH.getNeighborX(9, 10));
        assertEquals(8, Direction.SOUTH_WEST.getNeighborX(9, 10));
        assertEquals(8, Direction.WEST.getNeighborX(9, 10));
        assertEquals(8, Direction.NORTH_WEST.getNeighborX(9, 10));

        assertEquals(5, Direction.NORTH.getNeighborX(5, 10));
        assertEquals(6, Direction.NORTH_EAST.getNeighborX(5, 10));
        assertEquals(6, Direction.EAST.getNeighborX(5, 10));
        assertEquals(6, Direction.SOUTH_EAST.getNeighborX(5, 10));
        assertEquals(5, Direction.SOUTH.getNeighborX(5, 10));
        assertEquals(4, Direction.SOUTH_WEST.getNeighborX(5, 10));
        assertEquals(4, Direction.WEST.getNeighborX(5, 10));
        assertEquals(4, Direction.NORTH_WEST.getNeighborX(5, 10));
    }

    @Test
    public void getNeighborY() throws Exception {
        assertEquals(9, Direction.NORTH.getNeighborY(0, 10));
        assertEquals(9, Direction.NORTH_EAST.getNeighborY(0, 10));
        assertEquals(0, Direction.EAST.getNeighborY(0, 10));
        assertEquals(1, Direction.SOUTH_EAST.getNeighborY(0, 10));
        assertEquals(1, Direction.SOUTH.getNeighborY(0, 10));
        assertEquals(1, Direction.SOUTH_WEST.getNeighborY(0, 10));
        assertEquals(0, Direction.WEST.getNeighborY(0, 10));
        assertEquals(9, Direction.NORTH_WEST.getNeighborY(0, 10));

        assertEquals(8, Direction.NORTH.getNeighborY(9, 10));
        assertEquals(8, Direction.NORTH_EAST.getNeighborY(9, 10));
        assertEquals(9, Direction.EAST.getNeighborY(9, 10));
        assertEquals(0, Direction.SOUTH_EAST.getNeighborY(9, 10));
        assertEquals(0, Direction.SOUTH.getNeighborY(9, 10));
        assertEquals(0, Direction.SOUTH_WEST.getNeighborY(9, 10));
        assertEquals(9, Direction.WEST.getNeighborY(9, 10));
        assertEquals(8, Direction.NORTH_WEST.getNeighborY(9, 10));

        assertEquals(4, Direction.NORTH.getNeighborY(5, 10));
        assertEquals(4, Direction.NORTH_EAST.getNeighborY(5, 10));
        assertEquals(5, Direction.EAST.getNeighborY(5, 10));
        assertEquals(6, Direction.SOUTH_EAST.getNeighborY(5, 10));
        assertEquals(6, Direction.SOUTH.getNeighborY(5, 10));
        assertEquals(6, Direction.SOUTH_WEST.getNeighborY(5, 10));
        assertEquals(5, Direction.WEST.getNeighborY(5, 10));
        assertEquals(4, Direction.NORTH_WEST.getNeighborY(5, 10));
    }

    @Test
    public void getOpposite() throws Exception {
        assertEquals(Direction.SOUTH, Direction.NORTH.getOpposite());
        assertEquals(Direction.SOUTH_WEST, Direction.NORTH_EAST.getOpposite());
        assertEquals(Direction.WEST, Direction.EAST.getOpposite());
        assertEquals(Direction.NORTH_WEST, Direction.SOUTH_EAST.getOpposite());
        assertEquals(Direction.NORTH, Direction.SOUTH.getOpposite());
        assertEquals(Direction.NORTH_EAST, Direction.SOUTH_WEST.getOpposite());
        assertEquals(Direction.EAST, Direction.WEST.getOpposite());
        assertEquals(Direction.SOUTH_EAST, Direction.NORTH_WEST.getOpposite());
    }

}