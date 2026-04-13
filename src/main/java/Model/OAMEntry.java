package Model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BitField;

@Setter
@Getter
public class OAMEntry {
    public static BitField PriorityMask = new BitField(0x80);
    public static BitField YFlipMask = new BitField(0x40);
    public static BitField XFlipMask = new BitField(0x20);
    public static BitField DmgPaletteMask = new BitField(0x10);
    public static BitField CGBBank = new BitField(0x8);
    public static BitField CGBPalette = new BitField(0x7);
    private int XPos;
    private int YPos;
    private int TileIndex;
    private int Attributes;


    public boolean isYFlipped() {
        return YFlipMask.isSet(Attributes);
    }

    public boolean isXFlipped() {
        return XFlipMask.isSet(Attributes);
    }

    public int getDmgPalette() {
        return XFlipMask.getValue(Attributes);
    }

    public boolean isDrawnOver() {
        return PriorityMask.isSet(Attributes);
    }

    public int getByte(int n) {
        assert Commons.isBetween(n, 0, 3);
        return switch (n) {
            case 0 -> XPos;
            case 1 -> YPos;
            case 2 -> TileIndex;
            case 3 -> Attributes;
            default -> 0;
        };
    }

    public void setByte(int n, int value) {
        switch (n) {
            case 0 -> XPos = value & 0xFF;
            case 1 -> YPos = value & 0xFF;
            case 2 -> TileIndex = value & 0xFF;
            case 3 -> Attributes = value & 0xFF;
        }
        ;
    }

}
