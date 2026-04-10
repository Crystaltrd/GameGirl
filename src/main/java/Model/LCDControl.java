package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LCDControl extends BitFieldRegister {
    private windowTileMapEnum windowTileMap = windowTileMapEnum.AREA98;
    private windowTileDataAreaEnum bgTileDataArea = windowTileDataAreaEnum.AREA88;
    private objSizeEnum objSize = objSizeEnum.OBJ_8X8;
    private boolean lcdEnabled = false;

    @Override
    public byte getByte() {
        return (byte) (((bgEnable ? 1 : 0)) |
                ((objEnable ? 1 : 0) << 1) |
                ((objSize == objSizeEnum.OBJ_8X16 ? 1 : 0) << 2) |
                ((bgTileArea ? 1 : 0) << 3) |
                ((bgTileDataArea == windowTileDataAreaEnum.AREA80 ? 1 : 0) << 4) |
                ((windowEnable ? 1 : 0) << 5) |
                ((windowTileMap == windowTileMapEnum.AREA9C ? 1 : 0) << 6) |
                ((lcdEnabled ? 1 : 0) << 7));
    }
    private boolean windowEnable = false;

    @Override
    public void setByte(byte data) {
        bgEnable = (data & (1)) != 0;
        objEnable = (data & (1 << 1)) != 0;
        objSize = (data & (1 << 2)) == 0 ? objSizeEnum.OBJ_8X8 : objSizeEnum.OBJ_8X16;
        bgTileArea = (data & (1 << 3)) != 0;
        bgTileDataArea = (data & (1 << 6)) == 0 ? windowTileDataAreaEnum.AREA88 : windowTileDataAreaEnum.AREA80;
        windowEnable = (data & (1 << 5)) != 0;
        windowTileMap = (data & (1 << 6)) == 0 ? windowTileMapEnum.AREA98 : windowTileMapEnum.AREA9C;
        lcdEnabled = (data & (1 << 7)) != 0;
    }
    private boolean bgTileArea = false;
    enum windowTileMapEnum {
        AREA98,
        AREA9C
    }
    private boolean objEnable = false;
    private boolean bgEnable = false;

    enum windowTileDataAreaEnum {
        AREA88,
        AREA80
    }

    enum objSizeEnum {
        OBJ_8X8,
        OBJ_8X16,
    }
}
