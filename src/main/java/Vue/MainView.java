package Vue;

import Model.Emulator;
import Model.JOYP_BTNS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;


public class MainView extends JFrame implements KeyListener {
    Emulator emulator = new Emulator();
    TileView tileMapViewer;
    GameView gameViewer;
    RegistersView registersView;
    JTextArea debugScreen;
    JPanel gameCanvas;
    JPanel tileCanvas;
    CatridgeView catridgeView;

    public MainView() {
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
                System.exit(emulator.run(new FileInputStream(chooser.getSelectedFile())));
            } else {
                System.exit(0);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }


    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        JOYP_BTNS button = JOYP_BTNS.JP_NONE;
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_W -> button = JOYP_BTNS.JP_UP;
            case KeyEvent.VK_S -> button = JOYP_BTNS.JP_DOWN;
            case KeyEvent.VK_A -> button = JOYP_BTNS.JP_LEFT;
            case KeyEvent.VK_D -> button = JOYP_BTNS.JP_RIGHT;
            case KeyEvent.VK_Q -> button = JOYP_BTNS.JP_A;
            case KeyEvent.VK_E -> button = JOYP_BTNS.JP_B;
            case KeyEvent.VK_ESCAPE -> button = JOYP_BTNS.JP_SEL;
            case KeyEvent.VK_ENTER -> button = JOYP_BTNS.JP_START;
        }
        emulator.getIoRegisters().getJoyp().setButton(button, false);
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

        JOYP_BTNS button = JOYP_BTNS.JP_NONE;
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_W -> button = JOYP_BTNS.JP_UP;
            case KeyEvent.VK_S -> button = JOYP_BTNS.JP_DOWN;
            case KeyEvent.VK_A -> button = JOYP_BTNS.JP_LEFT;
            case KeyEvent.VK_D -> button = JOYP_BTNS.JP_RIGHT;
            case KeyEvent.VK_Q -> button = JOYP_BTNS.JP_A;
            case KeyEvent.VK_E -> button = JOYP_BTNS.JP_B;
            case KeyEvent.VK_ESCAPE -> button = JOYP_BTNS.JP_SEL;
            case KeyEvent.VK_ENTER -> button = JOYP_BTNS.JP_START;
        }
        emulator.getIoRegisters().getJoyp().setButton(button, true);
    }
}
