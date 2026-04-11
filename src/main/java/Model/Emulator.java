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

    public void push(int val) {
        cpu.setSP(cpu.getSP() - 1);
        write(cpu.getSP(), val);
    }

    public int pop() {
        int val = read(cpu.getSP());
        cpu.setSP(cpu.getSP() + 1);
        return val;
    }

    public void push16(int val) {
        push((val >> 8) & 0xFF);
        push(val & 0xFF);
    }

    public int pop16() {
        int lo = pop();
        int hi = pop();
        return (hi << 8) | lo;
    }

    public int read(int address) {
        address &= 0xFFFF;
        if (Commons.isBetween(address, 0x0000, 0x7FFF))
            return catridge.read(address);
        System.err.printf("Reading from %04X failed\n", address);
        System.exit(-1);
        return 0;
    }

    public int read16(int address) {
        int lo = read(address);
        int hi = read(address + 1);
        return lo | (hi << 8);
    }

    public void write16(int address, int value) {
        write(address + 1, (value >> 8) & 0xFF);
        write(address, value & 0xFF);
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
