package Vue;

import Model.Emulator;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;

public class MainView extends JFrame {
    Emulator emulator = new Emulator();
    TileView tileMapViewer;
    GameView gameViewer;
    RegistersView registersView;
    JTextArea debugScreen;

    public MainView() {
        JFileChooser chooser = new JFileChooser();
        setLayout(new BorderLayout());

        tileMapViewer = new TileView(emulator);
        tileMapViewer.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        getContentPane().add(tileMapViewer, BorderLayout.EAST);
        emulator.setTileMapRenderer(tileMapViewer);

        gameViewer = new GameView(emulator);
        JPanel gameCanvas = new JPanel(new FlowLayout());
        gameCanvas.setSize(new Dimension(3, 3));
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
}
