package Vue;

import Model.Emulator;
import Model.JOYP_BTNS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;


public class MainView extends JFrame implements KeyListener {
    Emulator emulator;
    private boolean restarting = false;
    TileView tileMapViewer;
    GameView gameViewer;
    RegistersView registersView;
    JTextArea debugScreen;
    JPanel gameCanvas;
    JPanel tileCanvas;
    CatridgeView catridgeView;

    public MainView() {
        int returnValue = 0;
        do {
            restarting = false;
            emulator = new Emulator();
            JFileChooser chooser = new JFileChooser();
            setLayout(new BorderLayout());
            emulator.setMainView(this);
            tileMapViewer = new TileView(emulator);
            catridgeView = new CatridgeView(emulator);
            emulator.setCatridgeView(catridgeView);
            tileCanvas = new JPanel(new BorderLayout());
            tileCanvas.add(tileMapViewer, BorderLayout.NORTH);
            tileCanvas.add(catridgeView, BorderLayout.SOUTH);
            tileCanvas.setBorder(BorderFactory.createLineBorder(Color.black, 2));
            getContentPane().add(tileCanvas, BorderLayout.EAST);
            emulator.setTileMapRenderer(tileMapViewer);
            gameViewer = new GameView(emulator);
            gameCanvas = new JPanel(new FlowLayout());
            gameCanvas.setSize(new Dimension(300, 300));
            gameCanvas.add(gameViewer);
            gameViewer.setBorder(BorderFactory.createLineBorder(Color.black, 2));
            getContentPane().add(gameCanvas, BorderLayout.CENTER);
            emulator.setGameRenderer(gameViewer);

            registersView = new RegistersView(emulator);
            getContentPane().add(registersView, BorderLayout.WEST);
            emulator.setRegisterRenderer(registersView);
            registersView.setBorder(BorderFactory.createLineBorder(Color.black, 2));


            debugScreen = new JTextArea();
            debugScreen.setMinimumSize(new Dimension(400, 60));
            debugScreen.setMaximumSize(new Dimension(400, 90));
            debugScreen.setEditable(false);
            emulator.setDebugWindow(debugScreen);

            getContentPane().add(debugScreen, BorderLayout.SOUTH);
            setSize(1500, 900);
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

            addKeyListener(this);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
            try {
                int returnVal = chooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    System.out.println("You chose to open this file: " +
                            chooser.getSelectedFile());
                    returnValue = emulator.run(new FileInputStream(chooser.getSelectedFile()));
                    getContentPane().removeAll();
                } else {
                    returnValue = 0;
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                returnValue = -1;
            }
        } while (restarting);
        System.exit(returnValue);

    }

    public void restartROM() {
        emulator.setRunning(false);
        restarting = true;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        switch (keyEvent.getKeyChar()) {
            case 'r' -> restartROM();
            default -> System.out.println(keyEvent.getKeyChar());
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        JOYP_BTNS button = decodeKey(keyEvent);
        emulator.getIoRegisters().getJoyp().setButton(button, false);
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        JOYP_BTNS button = decodeKey(keyEvent);
        emulator.getIoRegisters().getJoyp().setButton(button, true);
    }

    private JOYP_BTNS decodeKey(KeyEvent keyEvent) {
        return switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_W -> JOYP_BTNS.JP_UP;
            case KeyEvent.VK_S -> JOYP_BTNS.JP_DOWN;
            case KeyEvent.VK_A -> JOYP_BTNS.JP_LEFT;
            case KeyEvent.VK_D -> JOYP_BTNS.JP_RIGHT;
            case KeyEvent.VK_K -> JOYP_BTNS.JP_A;
            case KeyEvent.VK_L -> JOYP_BTNS.JP_B;
            case KeyEvent.VK_ESCAPE -> JOYP_BTNS.JP_SEL;
            case KeyEvent.VK_ENTER -> JOYP_BTNS.JP_START;
            default -> JOYP_BTNS.JP_NONE;
        };
    }
}
