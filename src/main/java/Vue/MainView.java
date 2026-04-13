package Vue;

import Model.Emulator;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;

public class MainView extends JFrame {
    Emulator emulator = new Emulator();
    TileView tileMapViewer;
    GameView gameViewer;

    public MainView(boolean tile) {
        JFileChooser chooser = new JFileChooser();
        setLayout(new BorderLayout());
        tileMapViewer = new TileView(emulator);
        getContentPane().add(tileMapViewer, BorderLayout.EAST);
        emulator.setTileMapRenderer(tileMapViewer);
        gameViewer = new GameView(emulator);
        getContentPane().add(gameViewer, BorderLayout.WEST);
        emulator.setGameRenderer(gameViewer);
        setSize(gameViewer.getWidth() + tileMapViewer.getWidth() + 16,
                Math.max(gameViewer.getHeight(), tileMapViewer.getHeight()) + 16);
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
