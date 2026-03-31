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
    private char RegSP = 0xFFFE;
    private char RegPC = 0x0100;

    private boolean IME = false;
    private boolean halted = false;
    private boolean stepping = false;
    private Instruction currInstruction;
    private byte[] currParams;


    public void setFlagReg(byte data) {
        FlagReg.setByte(data);
    }
    public void setFlagReg(ALUResult result){
        if (result.Carry == FlagOperation.SET)
            FlagReg.setCarryFlag(true);
        if (result.HalfCarry == FlagOperation.SET)
            FlagReg.setHalfCarryFlag(true);
        if (result.Zero == FlagOperation.SET)
            FlagReg.setZeroFlag(true);        
        if (result.Dec == FlagOperation.SET)
            FlagReg.setSubstractFlag(true);
        
        if (result.Carry == FlagOperation.RESET)
            FlagReg.setCarryFlag(false);
        if (result.HalfCarry == FlagOperation.RESET)
            FlagReg.setHalfCarryFlag(false);
        if (result.Zero == FlagOperation.RESET)
            FlagReg.setZeroFlag(false);
        if (result.Dec == FlagOperation.RESET)
            FlagReg.setSubstractFlag(false);

    }
    public static char get18bit(byte[] data) {
        char ch = (char) ((data[1] << 8));
        ch |= (char) ((char) data[0] & 0x00FF);
        return ch;
    }

    public Object getRegFromOperandType(OperandType op) {
        switch (op) {
            case DOUBLE_REGISTER_AF -> {
                return get18bit(new byte[]{FlagReg.getByte(), RegA});
            }
            case DOUBLE_REGISTER_BC -> {
                return get18bit(new byte[]{RegC, RegB});
            }
            case DOUBLE_REGISTER_DE -> {
                return get18bit(new byte[]{RegE, RegD});
            }
            case DOUBLE_REGISTER_HL -> {
                return get18bit(new byte[]{RegL, RegH});
            }
            case DOUBLE_REGISTER_SP -> {
                return RegSP;
            }
            case REGISTER_A -> {
                return RegA;
            }
            case REGISTER_B -> {
                return RegB;
            }
            case REGISTER_C -> {
                return RegC;
            }
            case REGISTER_D -> {
                return RegD;
            }
            case REGISTER_E -> {
                return RegE;
            }
            case REGISTER_H -> {
                return RegH;
            }
            case REGISTER_L -> {
                return RegL;
            }
            case FLAG_CARRY -> {
                return FlagReg.isCarryFlag();
            }
            case FLAG_NOTCARRY -> {
                return !FlagReg.isCarryFlag();
            }
            case FLAG_ZERO -> {
              return FlagReg.isZeroFlag();  
            }
            case FLAG_NOTZERO -> {
                return !FlagReg.isZeroFlag();
            }
        }
        return 0;
    }

    public void setRegFromOperandType(OperandType op, Object val) {
        switch (op) {
            case DOUBLE_REGISTER_AF -> {
                RegA = getHigh((char) val);
                FlagReg.setByte(getLow((char) val));
            }
            case DOUBLE_REGISTER_BC -> {
                RegB = getHigh((char) val);
                RegC = getLow((char) val);
            }
            case DOUBLE_REGISTER_DE -> {
                RegD = getHigh((char) val);
                RegE = getLow((char) val);

            }
            case DOUBLE_REGISTER_HL -> {
                RegH = getHigh((char) val);
                RegL = getLow((char) val);

            }
            case DOUBLE_REGISTER_SP -> RegSP = (char) val;
            case REGISTER_A -> RegA = (byte) val;
            case REGISTER_B -> RegB = (byte) val;
            case REGISTER_C -> RegC = (byte) val;
            case REGISTER_D -> RegD = (byte) val;
            case REGISTER_E -> RegE = (byte) val;
            case REGISTER_H -> RegH = (byte) val;
            case REGISTER_L -> RegL = (byte) val;
        }
    }

    public void setHFlag(boolean flag) {
        FlagReg.setHalfCarryFlag(flag);
    }

    public void setCFlag(boolean flag) {
        FlagReg.setCarryFlag(flag);
    }
    public void setZFlag(boolean flag) {
        FlagReg.setZeroFlag(flag);
    }
    public void setSFlag(boolean flag) {
        FlagReg.setSubstractFlag(flag);
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
                ",F=" + FlagReg.toString() + Integer.toBinaryString(FlagReg.getByte() & 0xFF) +
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
