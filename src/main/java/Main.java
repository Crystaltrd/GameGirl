import Model.Emulator;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;


public class Main {
    public static void main(String[] args) {
        Emulator emulator = new Emulator();
        InputStream romFile = Main.class.getResourceAsStream("/ROMs/tetris.gb");
        System.exit(emulator.run(romFile));
    }
}
