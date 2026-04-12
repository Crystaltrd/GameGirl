package Model;

import com.sun.jdi.event.BreakpointEvent;
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


    private int AF = 0x01B0;
    private int BC = 0x0013;
    private int DE = 0x00D8;
    private int HL = 0x014D;
    private int SP = 0xFFFE;
    private int PC = 0x0100;
    private int IE = 0x0000;

    private int fetchData;
    private int memDest;
    private int currOpcode;
    private boolean destIsMem;

    private boolean halted;
    private boolean IME;
    private boolean enablingIME;
    private boolean stepping;
    private Instruction currInst;

    CPU(Emulator context) {
        this.context = context;

    }

    public void fetchInstr() {
        currOpcode = context.read(PC++);
        currInst = InstructionSet.getInstr(currOpcode);
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
            case AM_IMP -> {
                return;
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

    public static boolean is16bitReg(REG_TYPE regType) {
        return switch (regType) {
            case RT_A, RT_B, RT_C, RT_D, RT_E, RT_F, RT_H, RT_L, RT_NONE -> false;
            case RT_AF, RT_SP, RT_BC, RT_DE, RT_HL, RT_PC -> true;
        };
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

    public boolean step() {
        if (!halted) {
            System.out.printf("A:%02X F:%02X B:%02X C:%02X D:%02X E:%02X H:%02X L:%02X SP:%04X PC:%04X PCMEM:%02X,%02X,%02X,%02X\n",
                    getA(), getF(), getB(), getC(), getD(), getE(), getH(), getL(), getSP(), getPC(), context.read(PC), context.read(PC + 1), context.read(PC + 2), context.read(PC + 3));
            fetchInstr();
            fetchParams();

            execute();
        } else {
            context.tick(1);
        }
        if (IME) {
            // Handle Interrupts
            enablingIME = false;
        }
        if (enablingIME) {
            IME = true;
        }
        return true;
    }

    public void writeReg(REG_TYPE regType, int val) {
        val &= 0xFFFF;
        switch (regType) {
            case RT_A -> setA(val);
            case RT_AF -> setAF(val);
            case RT_B -> setB(val);
            case RT_BC -> setBC(val);
            case RT_C -> setC(val);
            case RT_D -> setD(val);
            case RT_DE -> setDE(val);
            case RT_E -> setE(val);
            case RT_F -> setF(val);
            case RT_H -> setH(val);
            case RT_HL -> setHL(val);
            case RT_L -> setL(val);
            case RT_PC -> setPC(val);
            case RT_SP -> setSP(val);
        }
        ;
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

    public void gotoAddr(int addr, boolean pushPC) {
        if (checkCond(currInst.getCondType())) {
            if (pushPC) {
                context.tick(2);
                context.push16(getPC());
            }
            setPC(addr);
            context.tick(1);
        }
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

    public void setFlags(int Z, int N, int H, int C) {
        if (Z != -1)
            setZeroFlag(Z == 1);
        if (N != -1)
            setSubtractFlag(N == 1);
        if (H != -1)
            setHalfCarryFlag(H == 1);
        if (C != -1)
            setCarryFlag(C == 1);
    }
}
