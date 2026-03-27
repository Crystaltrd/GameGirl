import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CPU {
    private byte RegA = 1;
    private FlagRegister FlagReg;
    private byte RegB;
    private byte RegC;
    private byte RegD;
    private byte RegE;
    private byte RegH;
    private byte RegL;
    private short RegSP;
    private short RegPC = 0x100;
    
    private boolean halted = false;
    private  boolean stepping = false;

    public void setFlagReg(byte data) {
        FlagReg.setByte(data);
    }
}
