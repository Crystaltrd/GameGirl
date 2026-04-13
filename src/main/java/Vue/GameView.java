package Vue;

import Controller.LookAndFeelController;
import Model.Emulator;
import Model.PPU;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GameView extends JPanel {

    private Emulator context = null;

    GameView(Emulator context) {
        this.context = context;
    }

    public void paintComponent(Graphics g) {
        if (context != null) {
            super.paintComponent(g);
            Image img = renderGame();
            g.drawImage(img, 0, 0, this);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(160 * LookAndFeelController.scale * 2, 144 * LookAndFeelController.scale * 2);
    }

    public Dimension getSize() {
        return new Dimension(160 * LookAndFeelController.scale * 2, 144 * LookAndFeelController.scale * 2);
    }

    public int getHeight() {
        return 144 * LookAndFeelController.scale * 2;
    }

    public int getWidth() {
        return 160 * LookAndFeelController.scale * 2;
    }

    private Image renderGame() {
        BufferedImage bufferedImage = new BufferedImage(160 * LookAndFeelController.scale * 2, 144 * LookAndFeelController.scale * 2, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        for (int lineNum = 0; lineNum < PPU.YRES; lineNum++) {
            for (int x = 0; x < PPU.XRES; x++) {
                g.setColor(new Color(context.getPpu().framebuffer[x + (lineNum * PPU.XRES)]));
                g.fillRect(x * LookAndFeelController.scale * 2, lineNum * LookAndFeelController.scale * 2,
                        LookAndFeelController.scale * 2, LookAndFeelController.scale * 2);
            }
        }
        return bufferedImage;
    }
}
