package Model;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

public enum IN_TYPE {
    IN_ADC,
    IN_ADD,
    IN_AND,
    IN_CALL,
    IN_CCF,
    IN_CP,
    IN_CPL,
    IN_DAA,
    IN_DEC,
    IN_DI,
    IN_EI,
    IN_HALT,
    IN_INC,
    IN_JP,
    IN_JR,
    IN_LD,
    IN_LDH,
    IN_NONE,
    IN_NOP,
    IN_OR,
    IN_POP,
    IN_PREFIX,
    IN_PUSH,
    IN_RES,
    IN_RET,
    IN_RETI,
    IN_RLA,
    IN_RLCA,
    IN_RRA,
    IN_RRCA,
    IN_RST,
    IN_SBC,
    IN_SCF,
    IN_STOP, // WONT IMPLEMENT
    IN_SUB,
    IN_XOR;
    static final Consumer<Emulator> NOP_CB = (ctx -> {
    });
    static final Consumer<Emulator> JR_CB = (ctx -> {
        byte rel = (byte) (ctx.getCpu().getFetchData() & 0xFF);
        int addr = ctx.getCpu().getPC() + rel;
        ctx.getCpu().gotoAddr(addr, false);
    });
    static final Consumer<Emulator> XOR_CB = (ctx -> {
        ctx.getCpu().setA(ctx.getCpu().getA() ^ ctx.getCpu().getFetchData() & 0xFF);
        ctx.getCpu().setFlags(ctx.getCpu().getA() == 0 ? 1 : 0, 0, 0, 0);
    });

    static final Consumer<Emulator> AND_CB = (ctx -> {
        ctx.getCpu().setA(ctx.getCpu().getA() & ctx.getCpu().getFetchData() & 0xFF);
        ctx.getCpu().setFlags(ctx.getCpu().getA() == 0 ? 1 : 0, 0, 1, 0);
    });
    static final Consumer<Emulator> OR_CB = (ctx -> {
        ctx.getCpu().setA(ctx.getCpu().getA() | ctx.getCpu().getFetchData() & 0xFF);
        ctx.getCpu().setFlags(ctx.getCpu().getA() == 0 ? 1 : 0, 0, 0, 0);
    });
    static final Consumer<Emulator> DI_CB = (ctx -> {
        ctx.getCpu().setIME(false);
    });

    static final Consumer<Emulator> CP_CB = (ctx -> {
        int n = ctx.getCpu().getA() - ctx.getCpu().getFetchData();
        ctx.getCpu().setFlags(n == 0 ? 1 : 0, 1, (ctx.getCpu().getA() & 0x0F) - (ctx.getCpu().getFetchData() & 0x0F) < 0 ? 1 : 0, n < 0 ? 1 : 0);
    });
    static final Consumer<Emulator> LD_CB = (ctx -> {
        if (ctx.getCpu().getCurrOpcode() == 0xF8 && ctx.getCpu().getFetchData() == 0xFF)
            ctx.tick(1);
        if (ctx.getCpu().isDestIsMem()) {
            if (CPU.is16bitReg(ctx.getCpu().getCurrInst().getReg2())) {
                ctx.tick(1);
                ctx.write16(ctx.getCpu().getMemDest(), ctx.getCpu().getFetchData());
            } else {
                ctx.write(ctx.getCpu().getMemDest(), ctx.getCpu().getFetchData());
            }
            ctx.tick(1);
            return;
        }
        if (ctx.getCpu().getCurrInst().getAddrMode() == ADDR_MODE.AM_HL_SPR) {
            boolean hflag = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg2()) & 0xF) +
                    (ctx.getCpu().getFetchData() & 0xF) > 0xF;
            boolean cflag = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg2()) & 0xFF) +
                    (ctx.getCpu().getFetchData() & 0xFF) > 0xFF;
            ctx.getCpu().setFlags(0, 0, hflag ? 1 : 0, cflag ? 1 : 0);
            ctx.getCpu().writeReg(ctx.getCpu().getCurrInst().getReg1(),
                    ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg2()) + (byte) ctx.getCpu().getFetchData());
            return;
        }
        ctx.getCpu().writeReg(ctx.getCpu().getCurrInst().getReg1(), ctx.getCpu().getFetchData());
    });
    static final Consumer<Emulator> LDH_CB = (ctx -> {
        if (ctx.getCpu().getCurrInst().getReg1() == REG_TYPE.RT_A)
            ctx.getCpu().setA(ctx.read(0xFF00 | ctx.getCpu().getFetchData()));
        else
            ctx.write(ctx.getCpu().getMemDest(), ctx.getCpu().getA());
        ctx.tick(1);
    });
    static final Consumer<Emulator> NONE_CB = (ctx -> {
        System.err.printf("%02X %s Not Implemented Yet%n", ctx.getCpu().getCurrOpcode(), ctx.getCpu().getCurrInst().getInType());
        ctx.setEmergency(true);
    });

    static final Consumer<Emulator> CALL_CB = (ctx -> {
        ctx.getCpu().gotoAddr(ctx.getCpu().getFetchData(), true);
    });

    static final Consumer<Emulator> RST_CB = (ctx -> {
        ctx.getCpu().gotoAddr(ctx.getCpu().getCurrInst().getParam(), true);
    });
    static final Consumer<Emulator> RET_CB = (ctx -> {
        if (ctx.getCpu().getCurrInst().getCondType() != COND_TYPE.CT_NONE)
            ctx.tick(1);

        if (ctx.getCpu().checkCond()) {
            int lo = ctx.pop();
            ctx.tick(1);
            int hi = ctx.pop();
            ctx.tick(1);
            int n = (hi << 8) | lo;
            ctx.getCpu().setPC(n);
            ctx.tick(1);
        }
    });

    static final Consumer<Emulator> POP_CB = (ctx -> {
        int lo = ctx.pop();
        ctx.tick(1);
        int hi = ctx.pop();
        ctx.tick(1);
        int n = (hi << 8) | lo;
        ctx.getCpu().writeReg(ctx.getCpu().getCurrInst().getReg1(), n);
        if (ctx.getCpu().getCurrInst().getReg1() == REG_TYPE.RT_AF)
            ctx.getCpu().writeReg(ctx.getCpu().getCurrInst().getReg1(), n & 0xFFF0);
    });
    static final Consumer<Emulator> PUSH_CB = (ctx -> {
        int hi = ((ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) >> 8) & 0xFF);
        ctx.tick(1);
        ctx.push(hi);
        int lo = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) & 0xFF);
        ctx.tick(1);
        ctx.push(lo);
        ctx.tick(1);
    });
    static final Consumer<Emulator> JP_CB = (ctx -> {
        ctx.getCpu().gotoAddr(ctx.getCpu().getFetchData(), false);
    });
    static final Consumer<Emulator> DEC_CB = (ctx -> {
        int val = ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) - 1;
        if (CPU.is16bitReg(ctx.getCpu().getCurrInst().getReg1()))
            ctx.tick(1);
        if (ctx.getCpu().getCurrInst().getReg1() == REG_TYPE.RT_HL && ctx.getCpu().getCurrInst().getAddrMode() == ADDR_MODE.AM_MR) {
            val = ctx.read(ctx.getCpu().getHL()) - 1;
            ctx.write(ctx.getCpu().getHL(), val);
        } else {
            ctx.getCpu().writeReg(ctx.getCpu().getCurrInst().getReg1(), val);
            val = ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1());
        }
        if ((ctx.getCpu().getCurrOpcode() & 0x0B) == 0x0B) {
            return;
        }
        ctx.getCpu().setFlags(val == 0 ? 1 : 0, 1, (val & 0x0F) == 0x0F ? 1 : 0, -1);
    });

    static final Consumer<Emulator> INC_CB = (ctx -> {
        int val = ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) + 1;
        if (CPU.is16bitReg(ctx.getCpu().getCurrInst().getReg1()))
            ctx.tick(1);
        if (ctx.getCpu().getCurrInst().getReg1() == REG_TYPE.RT_HL && ctx.getCpu().getCurrInst().getAddrMode() == ADDR_MODE.AM_MR) {
            val = ctx.read(ctx.getCpu().getHL()) + 1;
            ctx.write(ctx.getCpu().getHL(), val);
        } else {
            ctx.getCpu().writeReg(ctx.getCpu().getCurrInst().getReg1(), val);
            val = ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1());
        }
        if ((ctx.getCpu().getCurrOpcode() & 0x03) == 0x03) {
            return;
        }
        ctx.getCpu().setFlags(val == 0 ? 1 : 0, 0, (val & 0x0F) == 0 ? 1 : 0, -1);
    });

    static final Consumer<Emulator> SUB_CB = (ctx -> {
        char val = (char) (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) - ctx.getCpu().getFetchData());
        int z = val == 0 ? 1 : 0;
        int h = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) & 0xF) - (ctx.getCpu().getFetchData() & 0xF) < 0 ? 1 : 0;
        int c = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) & 0xFF) - (ctx.getCpu().getFetchData() & 0xFF) < 0 ? 1 : 0;
        ctx.getCpu().writeReg(ctx.getCpu().getCurrInst().getReg1(), val);
        ctx.getCpu().setFlags(z, 1, h, c);

    });
    static final Consumer<Emulator> ADC_CB = (ctx -> {
        char u = (char) ctx.getCpu().getFetchData();
        char a = (char) ctx.getCpu().getA();
        char c = (char) (ctx.getCpu().getCarryFlag() ? 1 : 0);
        ctx.getCpu().setA((a + u + c) & 0xFF);
        ctx.getCpu().setFlags(ctx.getCpu().getA() == 0 ? 1 : 0, 0, (((a & 0xF) + (u & 0xF) + c) > 0xF) ? 1 : 0,
                (a + u + c > 0xFF) ? 1 : 0);
    });
    static final Consumer<Emulator> ADD_CB = (ctx -> {
        int val = ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) + ctx.getCpu().getFetchData();
        boolean is_16bit = CPU.is16bitReg(ctx.getCpu().getCurrInst().getReg1());
        if (is_16bit)
            ctx.tick(1);
        if (ctx.getCpu().getCurrInst().getReg1() == REG_TYPE.RT_SP)
            val = ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) + (byte) ctx.getCpu().getFetchData();
        int z = (val & 0xFF) == 0 ? 1 : 0;
        int h = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) & 0xF) + (ctx.getCpu().getFetchData() & 0xF) > 0xF ? 1 : 0;
        int c = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) & 0xFF) + (ctx.getCpu().getFetchData() & 0xFF) > 0xFF ? 1 : 0;
        if (is_16bit) {
            z = -1;
            h = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) & 0xFFF) + (ctx.getCpu().getFetchData() & 0xFFF) > 0xFFF ? 1 : 0;
            long n = (long) ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) + (long) ctx.getCpu().getFetchData();
            c = n >= 0x10000 ? 1 : 0;
        }
        if (ctx.getCpu().getCurrInst().getReg1() == REG_TYPE.RT_SP) {
            z = 0;
            h = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) & 0xF) + (ctx.getCpu().getFetchData() & 0xF) > 0xF ? 1 : 0;
            c = (ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg1()) & 0xFF) + (ctx.getCpu().getFetchData() & 0xFF) > 0xFF ? 1 : 0;
        }
        ctx.getCpu().writeReg(ctx.getCpu().getCurrInst().getReg1(), val & 0xFFFF);
        ctx.getCpu().setFlags(z, 0, h, c);
    });

    static final Consumer<Emulator> RRA_CB = (ctx -> {
        int carry = ctx.getCpu().getCarryFlag() ? 1 : 0;
        int newCarry = ctx.getCpu().getA() & 1;
        ctx.getCpu().setA(ctx.getCpu().getA() >> 1);
        ctx.getCpu().setA(ctx.getCpu().getA() | (carry << 7));
        ctx.getCpu().setFlags(0, 0, 0, newCarry);
    });
    static final Consumer<Emulator> PREFIX_CB = (ctx -> {
        int op = ctx.getCpu().getFetchData();
        REG_TYPE regType = REG_TYPE.decodeReg(op & 0b111);
        int bit = (op >> 3) & 0b111;
        int bit_op = (op >> 6) & 0b111;
        int reg_val = ctx.getCpu().readReg(regType);
        if (regType == REG_TYPE.RT_HL) {
            reg_val = ctx.read(ctx.getCpu().getHL());
            ctx.tick(2);
        }
        switch (bit_op) {
            case 1:
                ctx.getCpu().setFlags((reg_val & (1 << bit)) == 0 ? 1 : 0, 0, 1, -1);
                return;
            case 2:
                reg_val &= ~(1 << bit);
                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), reg_val);
                else
                    ctx.getCpu().writeReg(regType, reg_val);
                return;
            case 3:
                reg_val |= (1 << bit);
                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), reg_val);
                else
                    ctx.getCpu().writeReg(regType, reg_val);
                return;
        }
        boolean C = ctx.getCpu().getCarryFlag();
        switch (bit) {
            case 0 -> {
                boolean setC = false;
                byte result = (byte) ((reg_val << 1) & 0xFF);
                if ((reg_val & (1 << 7)) != 0) {
                    result |= 1;
                    setC = true;
                }
                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), result);
                else
                    ctx.getCpu().writeReg(regType, result);
                ctx.getCpu().setFlags(result == 0 ? 1 : 0, 0, 0, setC ? 1 : 0);
            }
            case 1 -> {
                byte old = (byte) reg_val;
                reg_val >>= 1;
                reg_val &= 0x80;
                reg_val |= (old << 7);

                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), reg_val);
                else
                    ctx.getCpu().writeReg(regType, reg_val);
                ctx.getCpu().setFlags((reg_val & 0xFF) == 0 ? 1 : 0, 0, 0, (old & 1) != 0 ? 1 : 0);
            }
            case 2 -> {
                byte old = (byte) reg_val;
                reg_val <<= 1;
                reg_val |= C ? 1 : 0;

                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), reg_val);
                else
                    ctx.getCpu().writeReg(regType, reg_val);
                ctx.getCpu().setFlags((reg_val & 0xFF) == 0 ? 1 : 0, 0, 0, (old & 0x80) != 0 ? 1 : 0);
            }
            case 3 -> {
                byte old = (byte) reg_val;
                reg_val >>= 1;
                reg_val |= C ? (1 << 7) : 0;

                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), reg_val);
                else
                    ctx.getCpu().writeReg(regType, reg_val);
                ctx.getCpu().setFlags((reg_val & 0xFF) == 0 ? 1 : 0, 0, 0, (old & 1) != 0 ? 1 : 0);
            }
            case 4 -> {

                byte old = (byte) reg_val;
                reg_val <<= 1;

                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), reg_val);
                else
                    ctx.getCpu().writeReg(regType, reg_val);
                ctx.getCpu().setFlags((reg_val & 0xFF) == 0 ? 1 : 0, 0, 0, (old & 0x80) != 0 ? 1 : 0);
            }
            case 5 -> {

                byte u = (byte) ((byte) reg_val >> 1);
                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), u);
                else
                    ctx.getCpu().writeReg(regType, u);
                ctx.getCpu().setFlags(u == 0 ? 1 : 0, 0, 0, (reg_val & 1) != 0 ? 1 : 0);
            }
            case 6 -> {
                reg_val = ((reg_val & 0xF0) >> 4) | ((reg_val & 0xF) << 4);
                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), reg_val);
                else
                    ctx.getCpu().writeReg(regType, reg_val);
                ctx.getCpu().setFlags((reg_val & 0xFF) == 0 ? 1 : 0, 0, 0, 0);
            }
            case 7 -> {
                byte u = (byte) (reg_val >> 1);
                if (regType == REG_TYPE.RT_HL)
                    ctx.write(ctx.getCpu().getHL(), u);
                else
                    ctx.getCpu().writeReg(regType, u);

                ctx.getCpu().setFlags(u == 0 ? 1 : 0, 0, 0, (reg_val & 1) != 0 ? 1 : 0);
            }
        }
    });

    static {
        for (final var val : EnumSet.allOf(IN_TYPE.class)) {
            val.callBack = NONE_CB;
        }
        IN_AND.callBack = AND_CB;
        IN_ADD.callBack = ADD_CB;
        IN_ADC.callBack = ADC_CB;
        IN_CALL.callBack = CALL_CB;
        IN_CP.callBack = CP_CB;
        IN_DEC.callBack = DEC_CB;
        IN_DI.callBack = DI_CB;
        IN_INC.callBack = INC_CB;
        IN_JP.callBack = JP_CB;
        IN_JR.callBack = JR_CB;
        IN_LD.callBack = LD_CB;
        IN_LDH.callBack = LDH_CB;
        IN_NOP.callBack = NOP_CB;
        IN_OR.callBack = OR_CB;
        IN_POP.callBack = POP_CB;
        IN_PUSH.callBack = PUSH_CB;
        IN_RET.callBack = RET_CB;
        IN_RRA.callBack = RRA_CB;
        IN_RST.callBack = RST_CB;
        IN_SUB.callBack = SUB_CB;
        IN_XOR.callBack = XOR_CB;
        IN_PREFIX.callBack = PREFIX_CB;
    }

    public Consumer<Emulator> callBack;
}
