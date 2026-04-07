import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Emu emulator;
            if (args.length > 0) {
                emulator = new Emu(Main.class.getResourceAsStream(args[0]), false, true, false);
            } else {
                emulator = new Emu(Main.class.getResourceAsStream("/ROMs/tetris.gb"), false, false, false);
            }
            Scanner scanner = new Scanner(System.in);
            while (emulator.emuStep()) {
                System.out.println("Ticks: " + emulator.ticks);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}