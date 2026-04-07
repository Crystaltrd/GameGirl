import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
public class FlagRegister extends BitFieldRegister {
    private boolean CarryFlag;
    private boolean HalfCarryFlag;
    private boolean SubstractFlag;
    private boolean ZeroFlag;

    public void setByte(byte data) {
        CarryFlag = (data & (1 << 4)) != 0;
        HalfCarryFlag = (data & (1 << 5)) != 0;
        SubstractFlag = (data & (1 << 6)) != 0;
        ZeroFlag = (data & (1 << 7)) != 0;
    }

    public byte getByte() {
        return (byte) (((CarryFlag ? 1 : 0) << 4) |
                ((HalfCarryFlag ? 1 : 0) << 5) |
                ((SubstractFlag ? 1 : 0) << 6) |
                ((ZeroFlag ? 1 : 0) << 7));
    }
    FlagRegister(byte data){
        setByte(data);
    }
    public String toString(){
        return String.format("%c%c%c%c",ZeroFlag ? 'Z' : '-',
                SubstractFlag ? 'N' : '-',
                HalfCarryFlag ? 'H' : '-',
                CarryFlag ? 'C' : '-'
                );
    }
}
