package Model;

import Vue.GameView;
import Vue.MainView;
import Vue.RegistersView;
import Vue.TileView;
import Vue.CatridgeView;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.io.InputStream;

@Setter
@Getter
public class Emulator {
    private Process process = null;
    private TileView tileMapRenderer = null;
    private GameView GameRenderer = null;
    private RegistersView RegisterRenderer = null;
    private JTextArea debugWindow = null;
    private MainView mainView = null;
    private CatridgeView catridgeView = null;
    private boolean paused = false;
    private boolean running = false;
    private boolean emergency = false;
    private long ticks = 0;
    private long FPS = 0;

    private Catridge catridge;
    private CPU cpu;
    private PPU ppu;
    private Timer timer;
    private IORegisters ioRegisters;
    private byte[] WRAM = new byte[0x2000];
    private byte[] ZeroPage = new byte[0x80];

    public StringBuilder dbg_msg = new StringBuilder();
    private byte readReady = 0;

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


    public int read16(int address) {
        int lo = read(address);
        int hi = read(address + 1);
        return lo | (hi << 8);
    }

    public void write16(int address, int value) {
        write(address + 1, (value >> 8) & 0xFF);
        write(address, value & 0xFF);
    }


    public Emulator() {
        cpu = new CPU(this);
        ppu = new PPU(this);
        timer = new Timer(this);
        ioRegisters = new IORegisters(this);
    }

    public Emulator(Process pr) {
        cpu = new CPU(this);
        ppu = new PPU(this);
        timer = new Timer(this);
        ioRegisters = new IORegisters(this);
        process = pr;
    }

    private byte currchar = 0;

    public void tick(int cycles) {
        for (int i = 0; i < cycles; i++) {
            for (int j = 0; j < 4; j++) {
                ticks++;
                timer.tick();
                ppu.tick();
            }
            ioRegisters.getDma().tick();
        }
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
        else if (Commons.isBetween(address, 0xFE00, 0xFE9F)) {
            if (ioRegisters.getDma().isActive())
                return 0xFF;
            return ppu.read_oam(address);
        } else if (Commons.isBetween(address, 0xFEA0, 0xFEFF))
            return 0;
        else if (Commons.isBetween(address, 0xFF00, 0xFF7F)) {
            return ioRegisters.read(address - 0xFF00);
        } else if (Commons.isBetween(address, 0xFF80, 0xFFFE))
            return ZeroPage[address - 0xFF80] & 0xFF;
        else if (address == 0xFFFF)
            return cpu.getIE();
        System.err.printf("Reading from %04X failed\n", address);
        System.exit(-1);
        return 0;
    }

    public void dbg_update() {
        if (read((char) 0xFF02) != 0) {
            char c = (char) read((char) 0xFF01);
            dbg_msg.append(c);
            if (dbg_msg.length() > 255)
                dbg_msg.delete(0, dbg_msg.length());
            write((char) 0xFF02, (byte) 0);
        }
    }

    public void write(int address, int value) {
        if (Commons.isBetween(0x0000, 0x7FFF, address))
            catridge.write(address, value);
        else if (Commons.isBetween(address, 0x8000, 0x9FFF))
            ppu.write(address, value);
        else if (Commons.isBetween(address, 0xA000, 0xBFFF))
            return;
        else if (Commons.isBetween(address, 0xC000, 0xDFFF)) {
            WRAM[address - 0xC000] = (byte) value;
        } else if (Commons.isBetween(address, 0xE000, 0xFDFF))
            WRAM[address - 0xE000] = (byte) value;
        else if (Commons.isBetween(address, 0xFE00, 0xFE9F)) {
            if (ioRegisters.getDma().isActive())
                return;
            ppu.write_oam(address, value);
        } else if (Commons.isBetween(address, 0xFEA0, 0xFEFF))
            return;
        else if (Commons.isBetween(address, 0xFF00, 0xFF7F)) {
            ioRegisters.write(address - 0xFF00, value);
        } else if (Commons.isBetween(address, 0xFF80, 0xFFFE))
            ZeroPage[address - 0xFF80] = (byte) value;
        else if (address == 0xFFFF)
            cpu.setIE(value);

    }

    public int run(InputStream rom) {
        running = true;
        try {
            if (rom == null)
                return 1;
            catridge = new Catridge(rom);
            if (mainView != null)
                mainView.setTitle(catridge.getTitle() + " - Gamegirl Emu");
            if (catridgeView != null) {
                catridgeView.getTitle().setText("Title: " + catridge.getTitle());
                catridgeView.getLicensee().setText("Licensee: " + catridge.getLicensee());
                catridgeView.getType().setText("Catridge Type: " + catridge.getType());
                catridgeView.getSgb().setText("SGB Flag: " + (catridge.getSGBFlag() ? "Set" : "Not Set"));
                catridgeView.getRomSz().setText("ROM Size: " + catridge.getROMSize() + " KiB");
                catridgeView.getRamSz().setText("RAM Size: " + catridge.getRAMSize() + " KiB");
                catridgeView.getRegion().setText("Region: " + catridge.getRegion());
                catridgeView.getVer().setText("ROM Ver: " + catridge.getROMVer());
            }
            while (running) {

                if (process != null && !process.isAlive())
                    return 0;
                if (emergency)
                    return 1;
                if (paused) {
                    Thread.sleep(100);
                    continue;
                }
                if (!cpu.step()) {
                    System.out.println("CPU stopped");
                    return 0;
                }
                //if(RegisterRenderer != null)
                // RegisterRenderer.update();
            }
            return 0;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 1;
        }
    }
}
