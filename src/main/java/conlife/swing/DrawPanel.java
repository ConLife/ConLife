package conlife.swing;

import javax.swing.*;
import java.awt.*;

class DrawPanel extends JPanel {

    DrawPanel() {
        super(null);
    }

    @Override
    protected void printComponent(Graphics g) {
        super.printComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public  Dimension getPreferredSize(){
        return new Dimension(500, 500);
    }
}
