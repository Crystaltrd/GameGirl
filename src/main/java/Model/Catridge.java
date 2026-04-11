package Model;

import java.io.IOException;
import java.io.InputStream;

public class Catridge extends BusMemory {
    private byte[] data;

    public Catridge(InputStream romFile) throws IOException {
        data = romFile.readAllBytes();
    }

    public int read(int address) {
        return data[address] & 0xFF;
    }

    public void write(int address, int value) {
        data[address] = (byte) value;
    }
}
