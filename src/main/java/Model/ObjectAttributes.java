package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ObjectAttributes {
    byte ypos = 0;
    byte xpos = 0;
    byte tileIndex = 0;
    ObjectAttributesFlags flags = new ObjectAttributesFlags();

    public byte getByte(int n) {
        return switch (n) {
            case 0 -> ypos;
            case 1 -> xpos;
            case 2 -> tileIndex;
            case 3 -> flags.getByte();
            default -> 0x00;
        };
    }

    public void setByte(int n, byte val) {
        switch (n) {
            case 0 -> ypos = val;
            case 1 -> xpos = val;
            case 2 -> tileIndex = val;
            case 3 -> flags.setByte(val);
        }
        ;
    }
}
