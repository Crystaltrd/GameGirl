package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LCDControl extends BitFieldRegister {

    private boolean lcdEnabled = false;
    private boolean windowTileMap = false;
    private boolean windowEnable = false;
    private boolean bgTileUnsignedAddr = false;
    private boolean bgTileArea = false;
    private boolean objSize = false;
    private boolean objEnable = false;
    private boolean bgEnable = false;

    @Override
    public byte getByte() {
        return (byte) (((bgEnable ? 1 : 0)) |
                ((objEnable ? 1 : 0) << 1) |
                ((objSize ? 1 : 0) << 2) |
                ((bgTileArea ? 1 : 0) << 3) |
                ((bgTileUnsignedAddr ? 1 : 0) << 4) |
                ((windowEnable ? 1 : 0) << 5) |
                ((windowTileMap ? 1 : 0) << 6) |
                ((lcdEnabled ? 1 : 0) << 7));
    }

    @Override
    public void setByte(byte data) {
        bgEnable = (data & (1)) != 0;
        objEnable = (data & (1 << 1)) != 0;
        objSize = (data & (1 << 2)) != 0;
        bgTileArea = (data & (1 << 3)) != 0;
        bgTileUnsignedAddr = (data & (1 << 4)) != 0;
        windowEnable = (data & (1 << 5)) != 0;
        windowTileMap = (data & (1 << 6)) != 0;
        lcdEnabled = (data & (1 << 7)) != 0;
    }
}
