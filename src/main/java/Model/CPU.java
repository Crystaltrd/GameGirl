package Model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BitField;

@Setter
@Getter
public class CPU {
    public static BitField lowByte = new BitField(0xFF);
    public static BitField HighByte = new BitField(0xFF00);
    public static BitField ZFlagMask = new BitField(0x80);
    public static BitField NFlagMask = new BitField(0x40);
    public static BitField HFlagMask = new BitField(0x20);
    public static BitField CFlagMask = new BitField(0x10);


    private int AF;
    private int BC;
    private int DE;
    private int HL;
    private int SP;
    private int PC;

    private int fetchData;
    private int memDest;
    private int currOpcode;

    private boolean halted;
    private boolean stepping;
    private Instruction currInst;

    public boolean step() {
        return true;
    }


    public void getA() {
        AF = HighByte.getValue(AF);
    }

    public void getF() {
        AF = lowByte.getValue(AF);
    }

    public void getB() {
        BC = HighByte.getValue(BC);
    }

    public void getC() {
        BC = lowByte.getValue(BC);
    }

    public void getD() {
        DE = HighByte.getValue(DE);
    }

    public void getE() {
        DE = lowByte.getValue(DE);
    }

    public void getH() {
        HL = HighByte.getValue(HL);
    }

    public void getL() {
        HL = lowByte.getValue(HL);
    }

    public void setA(int val) {
        AF = HighByte.setValue(AF, val & 0xFF);
    }

    public void setF(int val) {
        AF = lowByte.setValue(AF, val & 0xFF);
    }

    public void setB(int val) {
        BC = HighByte.setValue(BC, val & 0xFF);
    }

    public void setC(int val) {
        BC = lowByte.setValue(BC, val & 0xFF);
    }

    public void setD(int val) {
        DE = HighByte.setValue(DE, val & 0xFF);
    }

    public void setE(int val) {
        DE = lowByte.setValue(DE, val & 0xFF);
    }

    public void setH(int val) {
        HL = HighByte.setValue(HL, val & 0xFF);
    }

    public void setL(int val) {
        HL = lowByte.setValue(HL, val & 0xFF);
    }

    public boolean getZeroFlag() {
        return ZFlagMask.isSet(AF);
    }

    public void setZeroFlag(boolean val) {
        AF = ZFlagMask.setBoolean(AF, val);
    }

    public boolean getSubtractFlag() {
        return NFlagMask.isSet(AF);
    }

    public void setSubtractFlag(boolean val) {
        AF = NFlagMask.setBoolean(AF, val);
    }

    public boolean getHalfCarryFlag() {
        return HFlagMask.isSet(AF);
    }

    public void setHalfCarryFlag(boolean val) {
        AF = HFlagMask.setBoolean(AF, val);
    }

    public boolean getCarryFlag() {
        return CFlagMask.isSet(AF);
    }

    public void setCarryFlag(boolean val) {
        AF = CFlagMask.setBoolean(AF, val);
    }
}
