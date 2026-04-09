package Model;

import java.util.Scanner;

public class PPU extends GBMemory {
    public Tile[] tiles = new Tile[384];
    public byte[] BackgroundMap = new byte[0x9FFF - 0x9800 + 1];
    public ObjectAttributes[] objectAttributes = new ObjectAttributes[40];
    private EmulationContext ctx;

    public PPU(EmulationContext ctx) {
        this.ctx = ctx;
        for (int i = 0; i < objectAttributes.length; i++) {
            objectAttributes[i] = new ObjectAttributes();
        }

        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new Tile();
        }
    }

    public byte read(char address) {
        if (address >= 0x8000 && address <= 0x97FF)
            return tiles[(address - 0x8000) / 16].getByte((address - 0x8000) % 16);
        else if (address >= 0x9800 && address <= 0x9FFF)
            return BackgroundMap[address - 0x9800];
        else if (address >= 0xFE00 && address <= 0xFE9F) {
            if (ctx.ioRegisters.dmaRegister.isActive())
                return (byte) 0xFF;
            return objectAttributes[(address - 0xFE00) / 4].getByte((address - 0xFE00) % 4);
        } else
            return 0;
    }

    public void write(char address, byte value) {
        if (address >= 0x8000 && address <= 0x97FF)
            tiles[(address - 0x8000) / 16].setByte((address - 0x8000) % 16, value);
        else if (address >= 0x9800 && address <= 0x9FFF)
            BackgroundMap[address - 0x9800] = value;
        else if (address >= 0xFE00 && address <= 0xFE9F) {
            objectAttributes[(address - 0xFE00) / 4].setByte((address - 0xFE00) % 4, value);
        }
    }

    public byte getPixel(int tile, int x, int y) {
        return tiles[tile].getPixel(7 - x, y);
    }
}
