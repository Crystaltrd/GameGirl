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
        ctx.getCpu().setFlags(ctx.getCpu().getA() == 0 ? 0 : 1, 0, 0, 0);
    });

    static final Consumer<Emulator> AND_CB = (ctx -> {
        ctx.getCpu().setA(ctx.getCpu().getA() & ctx.getCpu().getFetchData() & 0xFF);
        ctx.getCpu().setFlags(ctx.getCpu().getA() == 0 ? 0 : 1, 0, 1, 0);
    });
    static final Consumer<Emulator> OR_CB = (ctx -> {
        ctx.getCpu().setA(ctx.getCpu().getA() | ctx.getCpu().getFetchData() & 0xFF);
        ctx.getCpu().setFlags(ctx.getCpu().getA() == 0 ? 0 : 1, 0, 0, 0);
    });
    static final Consumer<Emulator> DI_CB = (ctx -> {
        ctx.getCpu().setIME(false);
    });

    static final Consumer<Emulator> CP_CB = (ctx -> {
        int n = ctx.getCpu().getA() - ctx.getCpu().getFetchData();
        ctx.getCpu().setFlags(n == 0 ? 1 : 0, 1, (ctx.getCpu().getA() & 0x0F) - (ctx.getCpu().getFetchData() & 0x0F) < 0 ? 1 : 0, n < 0 ? 1 : 0);
    });
    static final Consumer<Emulator> LD_CB = (ctx -> {
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
                    ctx.getCpu().readReg(ctx.getCpu().getCurrInst().getReg2()) + ctx.getCpu().getFetchData());
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
        System.exit(-5);
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
        ctx.getCpu().setFlags(val == 0 ? 0 : 1, 1, (val & 0x0F) == 0x0F ? 1 : 0, -1);
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
        ctx.getCpu().setFlags(val == 0 ? 0 : 1, 0, (val & 0x0F) == 0 ? 1 : 0, -1);
    });

    static {
        for (final var val : EnumSet.allOf(IN_TYPE.class)) {
            val.callBack = NONE_CB;
        }
        IN_JP.callBack = JP_CB;
        IN_NOP.callBack = NOP_CB;
        IN_DI.callBack = DI_CB;
        IN_XOR.callBack = XOR_CB;
        IN_OR.callBack = OR_CB;
        IN_AND.callBack = AND_CB;
        IN_LD.callBack = LD_CB;
        IN_LDH.callBack = LDH_CB;
        IN_DEC.callBack = DEC_CB;
        IN_INC.callBack = INC_CB;
        IN_JR.callBack = JR_CB;
        IN_CP.callBack = CP_CB;
    }

    public Consumer<Emulator> callBack;
}
