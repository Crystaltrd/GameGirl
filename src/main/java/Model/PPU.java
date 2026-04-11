package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PPU extends BusMemory {
    private Emulator context;
    private byte[] data = new byte[0xA000 - 0x8000];

    PPU(Emulator context) {
        this.context = context;
    }

    @Override
    public int read(int address) {
        return data[address - 0x8000] & 0xFF;
    }

    @Override
    public void write(int address, int value) {
        data[address - 0x8000] = (byte) (value & 0xFF);
    }
}
