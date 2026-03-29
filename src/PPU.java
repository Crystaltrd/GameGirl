public class PPU {
    public byte[] TileRAM = new byte[0x97FF - 0x8000 + 1];
    public byte[] BackgroundMap = new byte[0x9FFF - 0x9800 + 1];
    public byte[] ObjectAttributeMemory = new byte[0xFE9F - 0xFE00 + 1];

    public byte read(char address) {
        if (address >= 0x8000 && address <= 0x97FF)
            return TileRAM[0x8000 - address];
        else if (address >= 0x9800 && address <= 0x9FFF)
            return BackgroundMap[0x9800 - address];
        else if (address >= 0xFE00 && address <= 0xFE9F)
            return ObjectAttributeMemory[0xFE9F - address];
        else
            return 0;
    }

    public void write(char address, byte value) {
        if (address >= 0x8000 && address <= 0x97FF)
            TileRAM[0x8000 - address] = value;
        else if (address >= 0x9800 && address <= 0x9FFF)
            BackgroundMap[0x9800 - address] = value;
        else if (address >= 0xFE00 && address <= 0xFE9F)
            ObjectAttributeMemory[0xFE9F - address] = value;
    }

}
