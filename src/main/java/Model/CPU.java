package Model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.BitField;
import Model.REG_TYPE.*;

@Setter
@Getter
public class CPU {

    private Emulator context;
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
    private int PC = 0x100;

    private int fetchData;
    private int memDest;
    private int currOpcode;
    private boolean destIsMem;

    private boolean halted;
    private boolean stepping;
    private Instruction currInst;

    CPU(Emulator context) {
        this.context = context;

    }

    public void fetchInstr() {
        currOpcode = context.read(PC++);
        currInst = InstructionSet.getInstr(currOpcode);
        System.out.printf("Curr Instruction: %02X %s\n", currOpcode, currInst.getInType());
    }

    public void fetchParams() {
        memDest = 0;
        destIsMem = false;
        switch (currInst.getAddrMode()) {
            case AM_D16_R, AM_A16_R -> {
                int lo = context.read(PC);
                context.tick(1);
                int hi = context.read(PC + 1);
                context.tick(1);
                memDest = lo | (hi << 8);
                destIsMem = true;
                PC += 2;
                fetchData = readReg(currInst.getReg2());
            }
            case AM_A8_R -> {
                memDest = context.read(PC) | 0xFF00;
                destIsMem = true;
                context.tick(1);
                PC++;
            }
            case AM_D8, AM_HL_SPR, AM_R_A8, AM_R_D8 -> {
                fetchData = context.read(PC);
                context.tick(1);
                PC++;
            }
            case AM_MR -> {
                memDest = readReg(currInst.getReg1());
                destIsMem = true;
                fetchData = context.read(readReg(currInst.getReg1()));
                context.tick(1);
            }
            case AM_MR_D8 -> {
                fetchData = context.read(PC);
                context.tick(1);
                PC++;
                memDest = readReg(currInst.getReg1());
                destIsMem = true;
            }
            case AM_R -> fetchData = readReg(currInst.getReg1());
            case AM_R_A16 -> {
                int lo = context.read(PC);
                context.tick(1);
                int hi = context.read(PC + 1);
                context.tick(1);
                int addr = lo | (hi << 8);
                PC += 2;
                fetchData = context.read(addr);
                context.tick(1);
            }
            case AM_D16, AM_R_D16 -> {
                int lo = context.read(PC);
                context.tick(1);
                int hi = context.read(PC + 1);
                context.tick(1);
                fetchData = lo | (hi << 8);
                PC += 2;
            }
            case AM_HLD_R -> {
                fetchData = readReg(currInst.getReg2());
                memDest = readReg(currInst.getReg1());
                destIsMem = true;
                context.tick(1);
                HL--;
            }
            case AM_HLI_R -> {
                fetchData = readReg(currInst.getReg2());
                memDest = readReg(currInst.getReg1());
                destIsMem = true;
                context.tick(1);
                HL++;
            }
            case AM_R_HLD -> {
                fetchData = context.read(readReg(currInst.getReg2()));
                context.tick(1);
                HL--;
            }
            case AM_R_HLI -> {
                fetchData = context.read(readReg(currInst.getReg2()));
                context.tick(1);
                HL++;
            }
            case AM_MR_R -> {
                fetchData = readReg(currInst.getReg2());
                memDest = readReg(currInst.getReg1());
                destIsMem = true;

                if (currInst.getReg1() == REG_TYPE.RT_C) {
                    memDest |= 0xFF00;
                }
            }
            case AM_R_MR -> {
                int addr = readReg(currInst.getReg2());
                if (currInst.getReg2() == REG_TYPE.RT_C) {
                    addr |= 0xFF00;
                }
                fetchData = context.read(addr);
                context.tick(1);
            }
            case AM_R_R -> fetchData = readReg(currInst.getReg2());
        }
    }

    public void execute() {
        currInst.getInType().callBack.accept(context);
    }

    public boolean step() {
        if (!halted) {
            fetchInstr();
            fetchParams();
            execute();
        }
        return true;
    }

    public int readReg(REG_TYPE regType) {
        return switch (regType) {
            case RT_A -> getA();
            case RT_AF -> getAF();
            case RT_B -> getB();
            case RT_BC -> getBC();
            case RT_C -> getC();
            case RT_D -> getD();
            case RT_DE -> getDE();
            case RT_E -> getE();
            case RT_F -> getF();
            case RT_H -> getH();
            case RT_HL -> getHL();
            case RT_L -> getL();
            case RT_PC -> getPC();
            case RT_SP -> getSP();
            default -> 0;
        };
    }

    public boolean checkCond(COND_TYPE condition) {
        return switch (condition) {
            case CT_C -> getCarryFlag();
            case CT_NC -> !getCarryFlag();
            case CT_NONE -> true;
            case CT_NZ -> !getZeroFlag();
            case CT_Z -> getZeroFlag();
        };
    }

    public boolean checkCond() {
        COND_TYPE condition = currInst.getCondType();
        return switch (condition) {
            case CT_C -> getCarryFlag();
            case CT_NC -> !getCarryFlag();
            case CT_NONE -> true;
            case CT_NZ -> !getZeroFlag();
            case CT_Z -> getZeroFlag();
        };
    }

    public int getA() {
        return HighByte.getValue(AF);
    }

    public int getF() {
        return lowByte.getValue(AF);
    }

    public int getB() {
        return HighByte.getValue(BC);
    }

    public int getC() {
        return lowByte.getValue(BC);
    }

    public int getD() {
        return HighByte.getValue(DE);
    }

    public int getE() {
        return lowByte.getValue(DE);
    }

    public int getH() {
        return HighByte.getValue(HL);
    }

    public int getL() {
        return lowByte.getValue(HL);
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
