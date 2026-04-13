package Model;

import Controller.LookAndFeelController;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BitField;

@Setter
@Getter
public class LCD {
    public static BitField LCDPPUEnabledMask = new BitField(0x80);
    public static BitField WindowTileMapMask = new BitField(0x40);
    public static BitField WindowEnabledMask = new BitField(0x20);
    public static BitField BGWindowTileAreaMask = new BitField(0x10);
    public static BitField BGTileMapAreaMask = new BitField(0x8);
    public static BitField ObjSizeMask = new BitField(0x4);
    public static BitField ObjEnableMask = new BitField(0x2);
    public static BitField BGWindowDisplayMask = new BitField(0x1);

    public static BitField LYCIntMask = new BitField(0x40);
    public static BitField OAMIntMask = new BitField(0x20);
    public static BitField VBLANKIntMask = new BitField(0x10);
    public static BitField HBLANKIntMask = new BitField(0x8);
    public static BitField LYCEqLYMask = new BitField(0x4);
    public static BitField PPUModeMask = new BitField(0x3);

    public static BitField ColorID3Mask = new BitField(0xC0);
    public static BitField ColorID2Mask = new BitField(0x30);
    public static BitField ColorID1Mask = new BitField(0xC);
    public static BitField ColorID0Mask = new BitField(0x3);


    private Emulator context;
    private int LCDC = 0x91;
    private int LY = 0x90;
    private int LYC;
    private int STAT;
    private int SCY;
    private int SCX;
    private int WY;
    private int WX;
    private int BGPalette = 0xFC;
    private int[] ObjPalette = {0xFF, 0xFF};

    private int[] bgColors;
    private int[] sp1Colors;
    private int[] sp2Colors;

    LCD(Emulator context) {
        this.context = context;
        bgColors = LookAndFeelController.defaultPalette.clone();
        sp1Colors = LookAndFeelController.defaultPalette.clone();
        sp2Colors = LookAndFeelController.defaultPalette.clone();
        setPPUMode(RENDER_MODE.MODE_OAM);
    }

    public void updatePalette(int val, int pal) {
        int col0 = ColorID0Mask.getValue(pal);
        int col1 = ColorID1Mask.getValue(pal);
        int col2 = ColorID2Mask.getValue(pal);
        int col3 = ColorID3Mask.getValue(pal);
        switch (pal) {
            case 0:
                bgColors[0] = LookAndFeelController.defaultPalette[col0];
                bgColors[1] = LookAndFeelController.defaultPalette[col1];
                bgColors[2] = LookAndFeelController.defaultPalette[col2];
                bgColors[3] = LookAndFeelController.defaultPalette[col3];
                BGPalette = val;
                break;
            case 1:
                sp1Colors[0] = LookAndFeelController.defaultPalette[0];
                sp1Colors[1] = LookAndFeelController.defaultPalette[col1];
                sp1Colors[2] = LookAndFeelController.defaultPalette[col2];
                sp1Colors[3] = LookAndFeelController.defaultPalette[col3];
                ObjPalette[0] = val & ~(0x3);
                break;
            case 2:
                sp2Colors[0] = LookAndFeelController.defaultPalette[0];
                sp2Colors[1] = LookAndFeelController.defaultPalette[col1];
                sp2Colors[2] = LookAndFeelController.defaultPalette[col2];
                sp2Colors[3] = LookAndFeelController.defaultPalette[col3];
                ObjPalette[1] = val & ~(0x3);
                break;
        }
    }

    public boolean getLCDPPUEnabled() {
        return LCDPPUEnabledMask.isSet(LCDC);
    }

    public void setLCDPPUEnabled(boolean flag) {
        LCDC = LCDPPUEnabledMask.setBoolean(LCDC, flag);
    }

    public int getWindowTileMap() {
        return WindowTileMapMask.isSet(LCDC) ? 0x9C00 : 0x9800;
    }

    public void setWindowTileMap(int addr) {
        LCDC = WindowTileMapMask.setBoolean(LCDC, addr == 0x9C00);
    }

    public boolean getWindowEnabled() {
        return WindowEnabledMask.isSet(LCDC);
    }

    public void setWindowEnabled(boolean flag) {
        LCDC = WindowEnabledMask.setBoolean(LCDC, flag);
    }

    public int getBGWindowTileArea() {
        return BGWindowTileAreaMask.isSet(LCDC) ? 0x8000 : 0x8800;
    }

    public void setBGWindowTileArea(int addr) {
        LCDC = BGWindowTileAreaMask.setBoolean(LCDC, addr == 0x8000);
    }

    public int getBGTileMapArea() {
        return BGTileMapAreaMask.isSet(LCDC) ? 0x9C00 : 0x9800;
    }

    public void setBGTileMapArea(int addr) {
        LCDC = BGTileMapAreaMask.setBoolean(LCDC, addr == 0x9C00);
    }

    public int getObjSize() {
        return ObjSizeMask.isSet(LCDC) ? 16 : 8;
    }

    public void setObjSize(int size) {
        LCDC = ObjSizeMask.setBoolean(LCDC, size == 16);
    }

    public boolean getObjEnable() {
        return ObjEnableMask.isSet(LCDC);
    }

    public void setObjEnable(boolean flag) {
        LCDC = ObjEnableMask.setBoolean(LCDC, flag);
    }

    public boolean getBGWindowDisplay() {
        return BGWindowDisplayMask.isSet(LCDC);
    }

    public void setBGWindowDisplay(boolean flag) {
        LCDC = BGWindowDisplayMask.setBoolean(LCDC, flag);
    }

    public boolean getLYCInt() {
        return LYCIntMask.isSet(STAT);
    }

    public void setLYCInt(boolean flag) {
        STAT = LYCIntMask.setBoolean(STAT, flag);
    }

    public boolean getOAMInt() {
        return OAMIntMask.isSet(STAT);
    }

    public void setOAMInt(boolean flag) {
        STAT = OAMIntMask.setBoolean(STAT, flag);
    }

    public boolean getVBlankInt() {
        return VBLANKIntMask.isSet(STAT);
    }

    public void setVBlankInt(boolean flag) {
        STAT = VBLANKIntMask.setBoolean(STAT, flag);
    }

    public boolean getHBlankInt() {
        return HBLANKIntMask.isSet(STAT);
    }

    public void setHBlankInt(boolean flag) {
        STAT = HBLANKIntMask.setBoolean(STAT, flag);
    }

    public boolean getLYCEqLY() {
        return LYCEqLYMask.isSet(STAT);
    }

    public void setLYCEqLY(boolean flag) {
        STAT = LYCEqLYMask.setBoolean(STAT, flag);
    }

    public RENDER_MODE getPPUMode() {
        return RENDER_MODE.values()[PPUModeMask.getValue(STAT)];
    }

    public void setPPUMode(RENDER_MODE mode) {
        STAT = PPUModeMask.setValue(STAT, mode.ordinal());
    }
}
