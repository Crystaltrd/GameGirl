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
    private PPU ppu;
    private OAM oam;
    private byte[] WRAM = new byte[0xE000 - 0xC000];
    private byte[] ZeroPage = new byte[0xFFFF - 0xFF80];

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
        else if (Commons.isBetween(address, 0x8000, 0x9FFF))
            return ppu.read(address);
        else if (Commons.isBetween(address, 0xA000, 0xBFFF))
            return 0; //TODO
        else if (Commons.isBetween(address, 0xC000, 0xDFFF))
            return WRAM[address - 0xC000] & 0xFF;
        else if (Commons.isBetween(address, 0xE000, 0xFDFF))
            return WRAM[address - 0xE000] & 0xFF;
        else if (Commons.isBetween(address, 0xFE00, 0xFE9F))
            return oam.read(address);
        else if (Commons.isBetween(address, 0xFEA0, 0xFEFF))
            return 0;
        else if (Commons.isBetween(address, 0xFF00, 0xFF7F))
            return 0; //TODO IO
        else if (Commons.isBetween(address, 0xFF80, 0xFFFE))
            return ZeroPage[address - 0xFF80] & 0xFF;
        else if (address == 0xFFFF)
            return cpu.getIE();
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
        else if (Commons.isBetween(address, 0x8000, 0x9FFF))
            ppu.write(address, value);
        else if (Commons.isBetween(address, 0xA000, 0xBFFF))
            return;
        else if (Commons.isBetween(address, 0xC000, 0xDFFF))
            WRAM[address - 0xC000] = (byte) value;
        else if (Commons.isBetween(address, 0xE000, 0xFDFF))
            WRAM[address - 0xE000] = (byte) value;
        else if (Commons.isBetween(address, 0xFE00, 0xFE9F))
            oam.write(address, value);
        else if (Commons.isBetween(address, 0xFEA0, 0xFEFF))
            return;
        else if (Commons.isBetween(address, 0xFF00, 0xFF7F))
            return; //TODO IO
        else if (Commons.isBetween(address, 0xFF80, 0xFFFE))
            ZeroPage[address - 0xFF80] = (byte) value;
        else if (address == 0xFFFF)
            cpu.setIE(value);

    }

    public void tick(int cycles) {
    }

    public int run(InputStream rom) {
        try {
            if (rom == null)
                return 1;
            catridge = new Catridge(rom);
            cpu = new CPU(this);
            ppu = new PPU(this);
            oam = new OAM(this);
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
