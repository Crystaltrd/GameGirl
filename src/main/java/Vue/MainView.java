package Vue;

import Controller.LookAndFeelController;
import Model.Emulator;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainView extends JFrame {
    Emulator emulator = new Emulator();
    TileView renderer;

    public MainView() {
        JFileChooser chooser = new JFileChooser();
        renderer = new TileView(emulator);
        getContentPane().add(renderer);
        emulator.setRenderer(renderer);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(16 * (8 + 1) * LookAndFeelController.scale,
                32 * (8 + 2) * LookAndFeelController.scale);
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
