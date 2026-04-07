import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            EmulationContext emulator;
            if (args.length > 0) {
                emulator = new EmulationContext(Main.class.getResourceAsStream(args[0]), false, true, false);
            } else {
                emulator = new EmulationContext(Main.class.getResourceAsStream("/ROMs/inter.gb"), false, true, false);
            }
            Scanner scanner = new Scanner(System.in);
            while (emulator.emuStep()) {
                emulator.dbg_update();
                emulator.dbg_print();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}