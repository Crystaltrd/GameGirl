import Model.EmulationContext;
import Vue.EmulatorView;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            EmulationContext emulator;
            if (args.length > 0) {
                emulator = new EmulationContext(Main.class.getResourceAsStream(args[0]), false, true, false);
            } else {
                emulator = new EmulationContext(Main.class.getResourceAsStream("/ROMs/test.gb"), false, false, false);
            }
            EmulatorView emulatorView = new EmulatorView(emulator);
            while (emulator.emuStep()) {
                emulatorView.update();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}