package conlife;

import javax.swing.*;
import java.awt.*;

public class CellComponent extends JComponent {

    @Override
    public void paintComponent(Graphics g){
        this.setOpaque(false);
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.red);
        g2.fillOval(0, 0, 5, 5);
    }
    @Override
    public  Dimension getPreferredSize(){
        return new Dimension(5, 5);
    }
}
