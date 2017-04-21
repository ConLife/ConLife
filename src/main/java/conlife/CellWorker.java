package conlife;

import java.util.concurrent.Callable;

public class CellWorker implements Callable<Rules.Rule> {

    private final Cell cell;

    CellWorker(Cell cell) {
        this.cell = cell;
    }

    @Override
    public Rules.Rule call() throws Exception {
        return cell.determineNextState();
    }

    @Override
    public String toString() {
        return "CellWorker{" +
                "cell=" + cell +
                '}';
    }
}
