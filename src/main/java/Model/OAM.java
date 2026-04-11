package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OAM extends BusMemory {
    private Emulator context;
    private byte[] data = new byte[0xFEA0 - 0xFE00];

    OAM(Emulator context) {
        this.context = context;
    }

    @Override
    public int read(int address) {

        return data[address - 0xFE00] & 0xFF;
    }

    @Override
    public void write(int address, int value) {
        data[address - 0xFE00] = (byte) (value & 0xFF);
    }
}
