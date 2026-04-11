package Model;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

public enum IN_TYPE {
    IN_ADC,
    IN_ADD,
    IN_AND,
    IN_BIT,
    IN_CALL,
    IN_CCF,
    IN_CP,
    IN_CPL,
    IN_DAA,
    IN_DEC,
    IN_DI,
    IN_EI,
    IN_HALT,
    IN_ILLEGAL_D3,
    IN_ILLEGAL_DB,
    IN_ILLEGAL_DD,
    IN_ILLEGAL_E3,
    IN_ILLEGAL_E4,
    IN_ILLEGAL_EB,
    IN_ILLEGAL_EC,
    IN_ILLEGAL_ED,
    IN_ILLEGAL_F4,
    IN_ILLEGAL_FC,
    IN_ILLEGAL_FD,
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
    IN_RL,
    IN_RLA,
    IN_RLC,
    IN_RLCA,
    IN_RR,
    IN_RRA,
    IN_RRC,
    IN_RRCA,
    IN_RST,
    IN_SBC,
    IN_SCF,
    IN_SET,
    IN_SLA,
    IN_SRA,
    IN_SRL,
    IN_STOP, // WONT IMPLEMENT
    IN_SUB,
    IN_SWAP,
    IN_XOR;
    static final Consumer<Emulator> NOP_CB = (ctx -> {
    });
    static final Consumer<Emulator> NONE_CB = (ctx -> {
        System.err.printf("%02X %s Not Implemented Yet%n", ctx.getCpu().getCurrOpcode(), ctx.getCpu().getCurrInst().getInType());
        System.exit(-5);
    });

    static final Consumer<Emulator> JP_CB = (ctx -> {
        if (ctx.getCpu().checkCond()) {
            ctx.getCpu().setPC(ctx.getCpu().getFetchData());
            ctx.tick(1);
        }
    });

    static {
        for (final var val : EnumSet.allOf(IN_TYPE.class)) {
            val.callBack = NONE_CB;
        }
        IN_JP.callBack = JP_CB;
        IN_NOP.callBack = NOP_CB;
    }

    public Consumer<Emulator> callBack;
}
