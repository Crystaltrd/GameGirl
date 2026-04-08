package Vue;

import Controller.DebugController;
import Controller.RegistersController;
import Model.*;

import javax.swing.*;
import java.awt.*;
import java.security.cert.X509Certificate;

public class EmulatorView extends JFrame {
    public final EmulationContext ctx;
    public final RegistersController registersController = new RegistersController(this);
    public final DebugController debugController = new DebugController(this);
    private final String[] registers = {"A", "F", "BC", "DE", "HL", "SP", "PC",
            "IME", "IE", "HALT", "INSTR", "DIV", "TIMA", "TMA", "TAC", "LY", "STAT"};
    public JTextArea debugScreen;
    JToolBar toolBar;
    JPanel gameCanvas;
    TileMapPanel tileMap;
    JPanel registersPanel;
    Color[] palette = {
            new Color(0xaaaaaa),
            new Color(0x9bbc0f),
            new Color(0x306230),
            new Color(0x0f380f)
    };
    int scale = 2;

    public EmulatorView(EmulationContext ctx) {
        this.ctx = ctx;
        setTitle("Gamegirl Emu - " + ctx.cartridge.getTitle());
        setPreferredSize(new Dimension(1360, 720));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        makeToolbar();
        makeRegisterPanel();
        debugScreen = new JTextArea();
        debugScreen.setMinimumSize(new Dimension(450, 60));
        debugScreen.setMaximumSize(new Dimension(450, 90));
        debugScreen.setEditable(false);
        tileMap = new TileMapPanel(this);
        getContentPane().add(tileMap, BorderLayout.EAST);
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(registersPanel, BorderLayout.WEST);
        getContentPane().add(debugScreen, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    public void makeToolbar() {
        JButton fileButton = new JButton("File");
        JButton editButton = new JButton("Edit");
        JButton helpButton = new JButton("Help");
        toolBar = new JToolBar(SwingConstants.HORIZONTAL);
        toolBar.add(fileButton);
        toolBar.add(editButton);
        toolBar.add(helpButton);
    }

    public void update() {
        debugController.updateDebug();
    }

    public void makeRegisterPanel() {
        registersPanel = new JPanel(new GridLayout(registers.length + 1, 0));
        registersPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        for (String reg : registers) {
            JPanel panel = new JPanel(new FlowLayout());
            var label = new JLabel(reg + ":");
            label.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(label);
            var textField = new JTextField();
            textField.setPreferredSize(new Dimension(90, 20));
            textField.setText(registersController.getRegValue(reg));
            textField.setEditable(false);
            panel.add(textField);
            registersPanel.add(panel);
        }
        var updateButton = new JButton("Update Values");
        updateButton.addActionListener(registersController);
        registersPanel.add(updateButton);
        this.getContentPane().add(registersPanel, BorderLayout.WEST);
        pack();
        setVisible(true);
    }
}

class TileMapPanel extends JPanel {
    public EmulatorView parent;

    public TileMapPanel(EmulatorView parent) {
        setBorder(BorderFactory.createLineBorder(Color.black));
        this.parent = parent;
    }

    public Dimension getPreferredSize() {
        return new Dimension(27 * 8 * parent.scale, 16 * 8 * parent.scale);
    }

    public void drawTile(Graphics g, char addr, int tileNum, int x, int y) {
        int rc_x = 0, rc_y = 0, rc_w = 0, rc_h = 0;
        for (int tileY = 0; tileY < 16; tileY += 2) {
            byte b1 = parent.ctx.bus_read((char) (addr + (tileNum * 16) + tileY));
            byte b2 = parent.ctx.bus_read((char) (addr + (tileNum * 16) + tileY + 1));
            for (int j = 7; j >= 0; j--) {
                byte pixel = 0;
                pixel = (byte) ((byte) (((b2 >> j) & 1) << 1) | (byte) (((b1 >> j) & 1)));
                rc_x = x + ((7 - j) * parent.scale);
                rc_y = y + ((tileY / 2) * parent.scale);
                rc_w = parent.scale;
                rc_h = parent.scale;
                g.setColor(parent.palette[pixel]);
                g.fillRect(rc_x, rc_y, rc_w, rc_h);
            }
        }
    }

    public void paintComponent(Graphics g) {
        int tilenum = 0;
        int xDraw = 0;
        int yDraw = 0;
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 27 * 8 * parent.scale, 18 * 8 * parent.scale);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 24; x++) {
                drawTile(g, (char) 0x8000, tilenum, xDraw + (x * parent.scale), yDraw + (y * parent.scale));
                xDraw += (8 * parent.scale);
                tilenum++;
            }
            yDraw += (8 * parent.scale);
            xDraw = 0;
        }
    }
}
