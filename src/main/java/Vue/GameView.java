package Vue;

import Controller.LookAndFeelController;
import Model.Emulator;
import Model.PPU;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class GameView extends JPanel {

    private Emulator context = null;
    private final Font font;
    private final BufferedImage frameImage = new BufferedImage(PPU.XRES, PPU.YRES, BufferedImage.TYPE_INT_RGB);
    private final int[] frameImageData = ((DataBufferInt) frameImage.getRaster().getDataBuffer()).getData();

    GameView(Emulator context) {
        font = new Font("Zpix", Font.BOLD, 32);
        this.context = context;

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (context == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        int[] framebuffer = context.getPpu().framebuffer;
        System.arraycopy(framebuffer, 0, frameImageData, 0, frameImageData.length);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.drawImage(frameImage, 0, 0, getWidth(), getHeight(), null);
        g2d.setFont(font);
        g2d.setColor(Color.RED);
        g2d.drawString(String.valueOf(context.getFPS()), 10, 30);
        g2d.dispose();
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

}
