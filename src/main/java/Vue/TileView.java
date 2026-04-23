package Vue;

import Controller.LookAndFeelController;
import Model.Emulator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TileView extends JPanel {
    public int[] bvec = new int[2];
    private Emulator context = null;

    TileView(Emulator context) {
        this.context = context;
        bvec[0] = 16 * (8 + 1) * LookAndFeelController.scale;
        bvec[1] = 24 * (8 + 1) * LookAndFeelController.scale;
        setIgnoreRepaint(true);
    }

    public void paintComponent(Graphics g) {
        if (context != null) {
            super.paintComponent(g);
            Image img = createTilemapImage();
            g.drawImage(img, 0, 0, this);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(bvec[0], bvec[1]);
    }

    public int getHeight() {
        return bvec[1];
    }

    public int getWidth() {
        return bvec[0];
    }

    public Dimension getSize() {
        return new Dimension(bvec[0], bvec[1]);
    }
    public void displayTile(Graphics g, int addr, int tileNum, int x, int y) {
        Rectangle rc = new Rectangle();
        for (int tileY = 0; tileY < 16; tileY += 2) {
            int b1 = context.read(addr + (tileNum * 16) + tileY);
            int b2 = context.read(addr + (tileNum * 16) + tileY + 1);
            for (int bit = 7; bit >= 0; bit--) {
                int lo = ((b2 >> bit) & 1) << 1;
                int hi = ((b1 >> bit) & 1);
                int pixel = hi | lo;
                rc.x = x + ((7 - bit) * LookAndFeelController.scale);
                rc.y = y + ((tileY / 2) * LookAndFeelController.scale);
                rc.width = rc.height = LookAndFeelController.scale;
                g.setColor(new Color(LookAndFeelController.defaultPalette[0][pixel]));
                g.fillRect(rc.x, rc.y, rc.width, rc.height);
            }
        }
    }


    private Image createTilemapImage() {
        int xDraw = 0;
        int yDraw = 0;
        int tilenum = 0;

        BufferedImage bufferedImage = new BufferedImage(bvec[0], bvec[1], BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.getGraphics();
        g.setColor(new Color(0xFF111111));
        g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        for (int y = 0; y < 24; y++) {
            for (int x = 0; x < 16; x++) {
                displayTile(g, 0x8000, tilenum, xDraw + (x * LookAndFeelController.scale), yDraw + (y * LookAndFeelController.scale));
                xDraw += (8 * LookAndFeelController.scale);
                tilenum++;
            }
            yDraw += (8 * LookAndFeelController.scale);
            xDraw = 0;
        }
        return bufferedImage;
    }
}
