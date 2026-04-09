package Model;

public class ObjectAttributesFlags extends BitFieldRegister {
    boolean priority = false;
    boolean yflip = false;
    boolean xflip = false;
    boolean palette = false;

    public byte getByte() {
        return (byte) (((priority ? 1 : 0) << 7) |
                ((yflip ? 1 : 0) << 6) |
                ((xflip ? 1 : 0) << 5) |
                ((palette ? 1 : 0) << 4));
    }

    public void setByte(byte data) {
        priority = (data & (1 << 7)) != 0;
        yflip = (data & (1 << 6)) != 0;
        xflip = (data & (1 << 5)) != 0;
        palette = (data & (1 << 4)) != 0;
    }
}
