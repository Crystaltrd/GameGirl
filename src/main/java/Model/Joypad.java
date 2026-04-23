package Model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BitField;

@Setter
@Getter
public class Joypad {
    public static BitField DPADMask = new BitField(0x20);
    public static BitField ButtonMask = new BitField(0x10);
    public static BitField ARightButtonMask = new BitField(0x1);
    public static BitField BLeftButtonMask = new BitField(0x2);
    public static BitField SelectUpButtonMask = new BitField(0x4);
    public static BitField StartDownButtonMask = new BitField(0x8);
    private Emulator context;
    private int buttonsNibble = 0x0F;
    private int dpadNibble = 0x0F;
    private int highNibble = 0xC0;

    Joypad(Emulator context) {
        this.context = context;
    }

    public int getByte() {
        int joyp = highNibble;
        if (DPADMask.isSet(highNibble))
            joyp |= dpadNibble;
        if (ButtonMask.isSet(highNibble))
            joyp |= buttonsNibble;
        return joyp;
    }

    public void setByte(int value) {
        highNibble = 0xC0 | (value & 0x30);
    }

    public void setButton(JOYP_BTNS button, boolean pressed) {
        switch (button) {
            case JP_A -> buttonsNibble = ARightButtonMask.setBoolean(buttonsNibble, pressed);
            case JP_B -> buttonsNibble = BLeftButtonMask.setBoolean(buttonsNibble, pressed);
            case JP_SEL -> buttonsNibble = SelectUpButtonMask.setBoolean(buttonsNibble, pressed);
            case JP_START -> buttonsNibble = StartDownButtonMask.setBoolean(buttonsNibble, pressed);
            case JP_RIGHT -> dpadNibble = ARightButtonMask.setBoolean(dpadNibble, pressed);
            case JP_LEFT -> dpadNibble = BLeftButtonMask.setBoolean(dpadNibble, pressed);
            case JP_UP -> dpadNibble = SelectUpButtonMask.setBoolean(dpadNibble, pressed);
            case JP_DOWN -> dpadNibble = StartDownButtonMask.setBoolean(dpadNibble, pressed);
            case JP_NONE -> {
                return;
            }
        }
        if (button == JOYP_BTNS.JP_A || button == JOYP_BTNS.JP_B || button == JOYP_BTNS.JP_SEL || button == JOYP_BTNS.JP_START) {
            if (ButtonMask.isSet(highNibble))
                context.getCpu().setJoypadInt(true);
        } else if (DPADMask.isSet(highNibble))
            context.getCpu().setJoypadInt(true);
    }

}
