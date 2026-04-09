package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LCDStat extends BitFieldRegister {

    private boolean lycIntSelect = false;
    private boolean mode2int = false;
    private boolean mode1int = false;
    private boolean mode0int = false;
    private boolean LYCEquLY = false;
    private byte ppuMode = 0;

    public byte getByte() {
        return (byte) (((ppuMode & 0x2)) |
                ((LYCEquLY ? 1 : 0) << 2) |
                ((mode0int ? 1 : 0) << 3) |
                ((mode1int ? 1 : 0) << 4) |
                ((mode2int ? 1 : 0) << 5) |
                ((lycIntSelect ? 1 : 0) << 6));
    }

    public void setByte(byte data) {
        ppuMode = (byte) (data & 0x2);
        LYCEquLY = (data & (1 << 2)) != 0;
        mode0int = (data & (1 << 3)) != 0;
        mode1int = (data & (1 << 4)) != 0;
        mode2int = (data & (1 << 5)) != 0;
        lycIntSelect = (data & (1 << 6)) != 0;
    }
}
