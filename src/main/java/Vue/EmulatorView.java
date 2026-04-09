package Vue;

import Controller.DebugController;
import Controller.RegistersController;
import Model.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

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
    GamePanel gameBackground;
    JPanel registersPanel;
    private HashMap<String, JTextField> componentMap;
    Color[] palette = {
            new Color(0xaaaaaa),
            new Color(0x9bbc0f),
            new Color(0x306230),
            new Color(0x0f380f)
    };

    Color[] paletteAlt = {
            new Color(0x081820),
            new Color(0x346856),
            new Color(0x88c070),
            new Color(0xe0f8d0)
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
        debugScreen.setMinimumSize(new Dimension(400, 60));
        debugScreen.setMaximumSize(new Dimension(400, 90));
        debugScreen.setEditable(false);
        tileMap = new TileMapPanel(this);
        gameBackground = new GamePanel(this);
        gameCanvas = new JPanel(new FlowLayout());
        gameCanvas.setSize(new Dimension(3, 3));
        gameCanvas.add(gameBackground);
        getContentPane().add(tileMap, BorderLayout.EAST);
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(gameCanvas, BorderLayout.CENTER);
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
        updateTiles();
        updatePanel();
        debugController.updateDebug();
    }

    public void updatePanel() {
        componentMap.forEach((k, v) -> v.setText(registersController.getRegValue(k)));
    }

    public void makeRegisterPanel() {
        componentMap = new HashMap<>();
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
            textField.setName(reg);
            textField.setEditable(false);
            componentMap.put(reg, textField);
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

    public void updateTiles() {
        tileMap.repaint();
        gameBackground.repaint();
    }
}

class TileMapPanel extends JPanel {
    public EmulatorView parent;
    public int select = 0x180;

    public TileMapPanel(EmulatorView parent) {
        setBorder(BorderFactory.createLineBorder(Color.black));
        this.parent = parent;
    }

    public Dimension getPreferredSize() {
        return new Dimension(
                16 * 8 * parent.scale + (16 * parent.scale),
                24 * 8 * parent.scale + (24 * parent.scale));
    }

    public void drawTile(Graphics g, char addr, int tileNum, int x, int y) {
        int rc_x = x, rc_y = y;
        for (int pixy = 0; pixy < 8; pixy++) {
            for (int pixx = 0; pixx < 8; pixx++) {
                g.setColor(parent.palette[parent.ctx.ppu.getPixel(tileNum, pixx, pixy)]);
                g.fillRect(rc_x, rc_y, parent.scale, parent.scale);
                rc_x += parent.scale;
            }
            rc_y += parent.scale;
            rc_x = x;
        }
    }

    public void paintComponent(Graphics g) {
        int tilenum = 0;
        int xDraw = 0;
        int yDraw = 0;
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 16 * 8 * parent.scale + (16 * parent.scale), 24 * 8 * parent.scale + (24 * parent.scale));
        for (int y = 0; y < 24; y++) {
            for (int x = 0; x < 16; x++) {
                drawTile(g, (char) 0x8000, tilenum, xDraw + (x * parent.scale), yDraw + (y * parent.scale));
                xDraw += (8 * parent.scale);
                tilenum++;
            }
            yDraw += (8 * parent.scale);
            xDraw = 0;
        }
    }
}

class GamePanel extends JPanel {
    public EmulatorView parent;

    public GamePanel(EmulatorView parent) {
        setBorder(BorderFactory.createLineBorder(Color.black));
        this.parent = parent;
    }

    public Dimension getPreferredSize() {

        return new Dimension(32 * 8 * parent.scale, 32 * 8 * parent.scale);
    }


    public void drawTile(Graphics g, char addr, int tileNum, int x, int y) {
        int rc_x = x, rc_y = y;
        for (int pixy = 0; pixy < 8; pixy++) {
            for (int pixx = 0; pixx < 8; pixx++) {
                g.setColor(parent.paletteAlt[parent.ctx.ppu.getPixel(tileNum, pixx, pixy)]);
                g.fillRect(rc_x, rc_y, parent.scale, parent.scale);
                rc_x += parent.scale;
            }
            rc_y += parent.scale;
            rc_x = x;
        }
    }
    public void paintComponent(Graphics g) {
        int tilenum = 0;
        int xDraw = 0;
        int yDraw = 0;
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 32 * 8 * parent.scale, 32 * 8 * parent.scale);

        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                drawTile(g, (char) 0x8000, parent.ctx.bus_read((char) (0x9800 + tilenum)) & 0xFF, xDraw, yDraw);
                g.setColor(parent.paletteAlt[3]);
                g.drawRect(0, 0, 160 * parent.scale, 144 * parent.scale);
                xDraw += (8 * parent.scale);
                tilenum++;
            }
            yDraw += (8 * parent.scale);
            xDraw = 0;
        }
    }
}
