import lombok.Getter;
import lombok.Setter;
import java.util.HexFormat;

@Setter
@Getter
public class CPU {
    private byte RegA = 1;
    private FlagRegister FlagReg = new FlagRegister((byte) 0);
    private byte RegB;
    private byte RegC;
    private byte RegD;
    private byte RegE;
    private byte RegH;
    private byte RegL;
    private char RegSP;
    private char RegPC = 0x100;

    private boolean IME = false;
    private boolean halted = false;
    private boolean stepping = false;
    private Instruction currInstruction;
    private byte[] currParams;


    public void setFlagReg(byte data) {
        FlagReg.setByte(data);
    }

    public static char get18bit(byte[] data) {
        char ch = (char) ((data[1] << 8));
        ch |= (char) ((char) data[0] & 0x00FF);
        return ch;
    }

    public static byte getLow(char word) {
        return (byte) (word & 0x00FF);
    }

    public static byte getHigh(char word) {
        return (byte) ((word & 0xFF00) >> 8);
    }

    public String toString() {
        HexFormat hex = HexFormat.of();
        return "CPU{" +
                "A=" + hex.toHexDigits(RegA) +
                ",F=" + hex.toHexDigits(FlagReg.getByte()) +
                ",B=" + hex.toHexDigits(RegB) +
                ", C=" + hex.toHexDigits(RegC) +
                ", D=" + hex.toHexDigits(RegD) +
                ", E=" + hex.toHexDigits(RegE) +
                ", H=" + hex.toHexDigits(RegH) +
                ", L=" + hex.toHexDigits(RegL) +
                ", SP=" + hex.toHexDigits(RegSP) +
                ", PC=" + hex.toHexDigits(RegPC) +
                ", IME=" + IME +
                ", halted=" + halted +
                ", stepping=" + stepping +
                '}';
    }
}
