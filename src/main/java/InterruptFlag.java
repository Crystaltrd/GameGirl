import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterruptFlag extends BitFieldRegister {
    private boolean JoyPadEnable = false;
    private boolean SerialEnable = false;
    private boolean TimerEnable = false;
    private boolean LCDEnable = false;
    private boolean VBlankEnable = false;

    public InterruptFlag(byte data) {
        setByte(data);
    }

    public byte getByte() {
        byte data = 0;

        return (byte) (((JoyPadEnable ? 1 : 0) << 4) |
                ((SerialEnable ? 1 : 0) << 3) |
                ((TimerEnable ? 1 : 0) << 2) |
                ((LCDEnable ? 1 : 0) << 1) |
                (VBlankEnable ? 1 : 0));
    }

    public void setByte(byte data) {
        JoyPadEnable = (InterruptSource.IT_JOYPAD.getValue() & data) != 0;
        SerialEnable = (InterruptSource.IT_SERIAL.getValue() & data) != 0;
        TimerEnable = (InterruptSource.IT_TIMER.getValue() & data) != 0;
        LCDEnable = (InterruptSource.IT_LCD_STAT.getValue() & data) != 0;
        VBlankEnable = (InterruptSource.IT_VBLANK.getValue() & data) != 0;
    }
}
