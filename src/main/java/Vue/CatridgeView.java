package Vue;

import Model.Emulator;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

@Setter
@Getter
public class CatridgeView extends JPanel {
    JLabel title = new JLabel("Title: ");
    JLabel licensee = new JLabel("Licensee: ");
    JLabel type = new JLabel("Catridge Type: ");
    JLabel sgb = new JLabel("SGB Flag: ");
    JLabel romSz = new JLabel("ROM Size: ");
    JLabel ramSz = new JLabel("RAM Size: ");
    JLabel region = new JLabel("Region: ");
    JLabel ver = new JLabel("ROM Version: ");
    private Emulator context;

    CatridgeView(Emulator context) {
        this.context = context;
        add(title);
        add(licensee);
        add(type);
        add(sgb);
        add(romSz);
        add(ramSz);
        add(region);
        add(ver);
        setLayout(new GridLayout(20, 0));
    }
}
