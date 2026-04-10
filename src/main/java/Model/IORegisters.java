package Model;

import java.net.ProtocolFamily;

public class IORegisters extends GBMemory {
    public byte[] data = new byte[0xFF7F - 0xFF00 + 1];
    public InterruptFlag IFReg = new InterruptFlag((byte) 0xE1);
    public DMARegister dmaRegister;
    public LCDControl lcdControl = new LCDControl();
    private final EmulationContext emulator;
    public Timer timer;
    public byte LY = (byte) 0x90;
    public byte LYC = (byte) 0x90;
    public LCDStat lcdStat = new LCDStat();
    public byte backgroundViewportY;
    public byte backgroundViewportX;
    public byte windowPositionY;
    public byte windowPositionX;
    public Palette bgPalette = new Palette();
    public Palette obgPalette0 = new Palette();
    public Palette obgPalette1 = new Palette();

    IORegisters(EmulationContext emulator) {
        this.emulator = emulator;
        this.timer = new Timer(emulator);
        this.dmaRegister = new DMARegister(emulator);
        lcdControl.setByte((byte) 0x91);
        bgPalette.setByte((byte) 0xFC);
        obgPalette0.setByte((byte) 0xFF);
        obgPalette1.setByte((byte) 0xFF);
    }

    public byte read(char addr) {
        if (addr >= HardwareRegisters.DIV.addr && addr <= HardwareRegisters.TAC.addr) {
            return timer.read(addr);
        } else if (addr == HardwareRegisters.IF.addr)
            return IFReg.getByte();
        else if (addr == HardwareRegisters.DMA.addr)
            return dmaRegister.getAddr();
        else if (addr == HardwareRegisters.LCDC.addr)
            return lcdControl.getByte();
        else if (addr == HardwareRegisters.LY.addr)
                return LY;
        else if (addr == HardwareRegisters.LYC.addr)
            return LYC;
        else if (addr == HardwareRegisters.SCY.addr)
            return backgroundViewportY;
        else if (addr == HardwareRegisters.SCX.addr)
            return backgroundViewportX;
        else if (addr == HardwareRegisters.WY.addr)
            return windowPositionY;
        else if (addr == HardwareRegisters.WX.addr)
            return windowPositionX;
        else if (addr == HardwareRegisters.STAT.addr) {
            return lcdStat.getByte();
        } else if (addr == HardwareRegisters.BGP.addr)
            return bgPalette.getByte();
        else if (addr == HardwareRegisters.OBP0.addr)
            return obgPalette0.getByte();
        else if (addr == HardwareRegisters.OBP1.addr)
            return obgPalette1.getByte();
        return data[addr];
    }

    public void write(char addr, byte val) {
        if (addr >= HardwareRegisters.DIV.addr && addr <= HardwareRegisters.TAC.addr) {
            timer.write(addr, val);
        } else if (addr == HardwareRegisters.IF.addr)
            IFReg.setByte(val);
        else if (addr == HardwareRegisters.DMA.addr) {
            dmaRegister.start(val);
        } else if (addr == HardwareRegisters.LCDC.addr) {
            lcdControl.setByte(val);

        }
        else if (addr == HardwareRegisters.LY.addr)
            return;
        else if (addr == HardwareRegisters.LYC.addr)
            LYC = val;
        else if (addr == HardwareRegisters.STAT.addr)
            lcdStat.setByte(val);
        else if (addr == HardwareRegisters.SCX.addr)
            backgroundViewportX = val;
        else if (addr == HardwareRegisters.SCY.addr)
            backgroundViewportY = val;
        else if (addr == HardwareRegisters.WX.addr)
            windowPositionX = val;
        else if (addr == HardwareRegisters.WY.addr)
            windowPositionY = val;
        else if (addr == HardwareRegisters.BGP.addr)
            bgPalette.setByte(val);
        else if (addr == HardwareRegisters.OBP0.addr)
            obgPalette0.setByte(val);
        else if (addr == HardwareRegisters.OBP1.addr)
            obgPalette1.setByte(val);
        else
            data[addr] = val;
    }
}
