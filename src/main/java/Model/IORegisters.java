package Model;

public class IORegisters extends GBMemory {
    public byte[] data = new byte[0xFF7F - 0xFF00 + 1];
    public InterruptFlag IFReg = new InterruptFlag((byte) 0xE1);
    private final EmulationContext emulator;
    public Timer timer;

    IORegisters(EmulationContext emulator) {
        this.emulator = emulator;
        this.timer = new Timer(emulator);
    }

    public byte read(char addr) {
        if (addr >= HardwareRegisters.DIV.addr && addr <= HardwareRegisters.TAC.addr) {
            return timer.read(addr);
        } else if (addr == HardwareRegisters.IF.addr)
            return IFReg.getByte();
        return data[addr];
    }

    public void write(char addr, byte val) {
        if (addr >= HardwareRegisters.DIV.addr && addr <= HardwareRegisters.TAC.addr) {
            timer.write(addr, val);
        } else if (addr == HardwareRegisters.IF.addr)
            IFReg.setByte(val);
        else
            data[addr] = val;
    }
}
