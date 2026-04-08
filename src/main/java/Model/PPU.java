package Model;

import java.util.Scanner;

public class PPU extends GBMemory {
    public byte[] TileRAM = new byte[0x97FF - 0x8000 + 1];
    public byte[] BackgroundMap = new byte[0x9FFF - 0x9800 + 1];
    public byte[] ObjectAttributeMemory = new byte[0xFE9F - 0xFE00 + 1];
    private EmulationContext ctx;

    public PPU(EmulationContext ctx) {
        this.ctx = ctx;
    }

    public byte read(char address) {
        if (address >= 0x8000 && address <= 0x97FF)
            return TileRAM[address - 0x8000];
        else if (address >= 0x9800 && address <= 0x9FFF)
            return BackgroundMap[address - 0x9800];
        else if (address >= 0xFE00 && address <= 0xFE9F) {
            if (ctx.ioRegisters.dmaRegister.isActive())
                return (byte) 0xFF;
            return ObjectAttributeMemory[address - 0xFE00];
        } else
            return 0;
    }

    public void write(char address, byte value) {
        if (address >= 0x8000 && address <= 0x97FF)
            TileRAM[address - 0x8000] = value;
        else if (address >= 0x9800 && address <= 0x9FFF)
            BackgroundMap[address - 0x9800] = value;
        else if (address >= 0xFE00 && address <= 0xFE9F) {
            ObjectAttributeMemory[address - 0xFE00] = value;
        }
    }

}
