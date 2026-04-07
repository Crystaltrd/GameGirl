package Model;

import lombok.Getter;
import lombok.Setter;

import java.util.HexFormat;

@Setter
@Getter
public class CPU {
    private byte RegA = 1;
    private FlagRegister FlagReg = new FlagRegister((byte) 0xB0);
    private byte RegB = (byte) 0x00;
    private byte RegC = 0x13;
    private byte RegD = 0x00;
    private byte RegE = (byte) 0xD8;
    private byte RegH = (byte) 0x01;
    private byte RegL = 0x4D;
    private char RegSP = 0xFFFE;
    private char RegPC = 0x0100;

    private boolean IME = false;
    private boolean QueuedIME = false;
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
    public static char get16bit(byte[] data) {
        char ch = (char) ((data[1] << 8));
        ch |= (char) ((char) data[0] & 0x00FF);
        return ch;
    }
    public static char get16bit(byte high, byte low) {
        char ch = (char) ((high << 8));
        ch |= (char) ((char) low & 0x00FF);
        return ch;
    }
    public char getRegAF(){
        return get16bit(new byte[]{FlagReg.getByte(), RegA});
    }
    public char getRegBC(){
        return get16bit(new byte[]{RegC, RegB});
    }

    public char getRegDE(){
        return get16bit(new byte[]{RegE, RegD});
    }
    public char getRegHL(){
        return get16bit(new byte[]{RegL, RegH});
    }
    public void setRegHL(char val){
        RegH = getHigh(val);
        RegL = getLow(val);
    }
    public void setRegAF(char val){
        RegA = getHigh(val);
        FlagReg.setByte(getLow(val));
    }
    public void setRegBC(char val){
        RegB = getHigh(val);
        RegC = getLow(val);
    }

    public void setRegDE(char val){
        RegD = getHigh(val);
        RegE = getLow(val);
    }

 
    public Object getRegFromOperandType(OperandType op) {
        switch (op) {
            case DOUBLE_REGISTER_AF -> {
                return getRegAF();
            }
            case DOUBLE_REGISTER_BC -> {
                return getRegBC();
            }
            case DOUBLE_REGISTER_DE -> {
                return getRegDE();
            }
            case DOUBLE_REGISTER_HL -> {
                return getRegHL();
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
        return null;
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

    public static byte getLow(byte word) {
        return (byte) (word & 0x0F);
    }

    public static byte getHigh(byte word) {
        return (byte) (((word & 0xF0) >> 4) & 0x0F);
    }
    public String toString() {
        HexFormat hex = HexFormat.of();
        return "Model.CPU{" +
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
