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
        address &= 0xFFFF;
        if (Commons.isBetween(address, 0x0000, 0x7FFF))
            return catridge.read(address);
        System.exit(-1);
        return 0;
    }

    public void write(int address, int value) {
        if (Commons.isBetween(0x0000, 0x7FFF, address))
            catridge.write(address, value);
    }

    public void tick(int cycles) {
    }

    public int run(InputStream rom) {
        try {
            if (rom == null)
                return 1;
            catridge = new Catridge(rom);
            cpu = new CPU(this);
            while (running) {
                if (paused) {
                    Thread.sleep(100);
                    continue;
                }
                if (!cpu.step()) {
                    System.out.println("CPU stopped");
                    return 0;
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
