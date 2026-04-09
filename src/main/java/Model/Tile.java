package Model;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class Tile {
    byte[] data = new byte[16];

    public byte getByte(int n) {
        return data[n];
    }

    public void setByte(int n, byte data) {
        this.data[n] = data;
    }

    public byte getPixel(int x, int y) {
        byte b1 = data[2 * y];
        byte b2 = data[2 * y + 1];
        return (byte) ((byte) (((b2 >> x) & 1) << 1) | (byte) (((b1 >> x) & 1)));
    }
}
