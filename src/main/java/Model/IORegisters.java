package Model;

import java.net.ProtocolFamily;

public class IORegisters extends GBMemory {
    public byte[] data = new byte[0xFF7F - 0xFF00 + 1];
    public InterruptFlag IFReg = new InterruptFlag((byte) 0xE1);
    public DMARegister dmaRegister;
    private final EmulationContext emulator;
    public Timer timer;
    public byte LY = 0;
    IORegisters(EmulationContext emulator) {
        this.emulator = emulator;
        this.timer = new Timer(emulator);
        this.dmaRegister = new DMARegister(emulator);
    }

    public byte read(char addr) {
        if (addr >= HardwareRegisters.DIV.addr && addr <= HardwareRegisters.TAC.addr) {
            return timer.read(addr);
        } else if (addr == HardwareRegisters.IF.addr)
            return IFReg.getByte();
        else if (addr == HardwareRegisters.DMA.addr)
            return dmaRegister.getAddr();
        else if (addr == HardwareRegisters.LY.addr)
            return LY++;
        return data[addr];
    }

    public void write(char addr, byte val) {
        if (addr >= HardwareRegisters.DIV.addr && addr <= HardwareRegisters.TAC.addr) {
            timer.write(addr, val);
        } else if (addr == HardwareRegisters.IF.addr)
            IFReg.setByte(val);
        else if (addr == HardwareRegisters.DMA.addr) {
            System.out.printf("%02X", val);
            dmaRegister.start(val);
        } else if (addr == HardwareRegisters.LY.addr)
            LY = val;
        else
            data[addr] = val;
    }
}
