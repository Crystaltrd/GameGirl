import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length > 1) {
                Emu emulator = new Emu(args[0]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}