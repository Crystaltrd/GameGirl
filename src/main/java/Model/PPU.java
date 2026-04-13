package Model;

import Vue.TileView;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

@Setter
@Getter
public class PPU extends BusMemory {
    private Emulator context;
    private byte[] VRAM = new byte[0xA000 - 0x8000];
    private OAMEntry[] oam = new OAMEntry[40];

    PPU(Emulator context) {
        this.context = context;
        for (int i = 0; i < oam.length; i++) {
            oam[i] = new OAMEntry();
        }
    }

    public void tick() {
        TileView display = context.getRenderer();
        if (display != null) {
            display.repaint();
        }
    }
    @Override
    public int read(int address) {
        if (address >= 0x8000)
            address -= 0x8000;
        return VRAM[address] & 0xFF;
    }

    @Override
    public void write(int address, int value) {
        if (address >= 0x8000)
            address -= 0x8000;
        VRAM[address] = (byte) (value & 0xFF);
    }

    public int read_oam(int address) {
        if (address >= 0xFE00)
            address -= 0xFE00;
        return oam[(address) / 4].getByte((address) % 4) & 0xFF;
    }

    public void write_oam(int address, int value) {
        if (address >= 0xFE00)
            address -= 0xFE00;
        oam[(address) / 4].setByte((address) % 4, value);
    }
}
