
public class Main {
    public static void main(String[] args) {
        try {
            Emu emulator;
            if (args.length > 0) {
                emulator = new Emu(Main.class.getResourceAsStream(args[0]), false, true, false);
            } else {
                emulator = new Emu(Main.class.getResourceAsStream("/ROMs/11-op a,(hl).gb"), true, false, false);
            }

            while (emulator.emuStep()) {
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}