package Model;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

@Setter
@Getter
public class Emulator {
    private boolean paused = false;
    private boolean running = true;
    private long ticks = 0;

    private Catridge catridge;
    private CPU cpu;

    public int read(int address) {
        if (Commons.isBetween(0x0000, 0x7FFF, address))
            return catridge.read(address);
        System.exit(-5);
        return 0;
    }

    public void write(int address, int value) {
        if (Commons.isBetween(0x0000, 0x7FFF, address))
            catridge.write(address, value);
    }

    public int run(InputStream rom) {
        try {
            if (rom == null)
                return 1;
            catridge = new Catridge(rom);
            cpu = new CPU();
            while (running) {
                if (paused) {
                    Thread.sleep(100);
                    continue;
                }

                ticks++;
            }
            return 0;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 1;
        }
    }
}
