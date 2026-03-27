import com.fasterxml.jackson.annotation.JsonValue;

import java.util.EnumSet;
import java.util.function.Function;

public enum Opcodes {
    NONE("NONE"),
    ADC("ADC"),
    ADD("ADD"),
    AND("AND"),
    BIT("BIT"),
    CALL("CALL"),
    CCF("CCF"),
    CP("CP"),
    CPL("CPL"),
    DAA("DAA"),
    DEC("DEC"),
    DI("DI"),
    EI("EI"),
    HALT("HALT"),
    ILLEGAL_D3("ILLEGAL_D3"),
    ILLEGAL_DB("ILLEGAL_DB"),
    ILLEGAL_DD("ILLEGAL_DD"),
    ILLEGAL_E3("ILLEGAL_E3"),
    ILLEGAL_E4("ILLEGAL_E4"),
    ILLEGAL_EB("ILLEGAL_EB"),
    ILLEGAL_EC("ILLEGAL_EC"),
    ILLEGAL_ED("ILLEGAL_ED"),
    ILLEGAL_F4("ILLEGAL_F4"),
    ILLEGAL_FC("ILLEGAL_FC"),
    ILLEGAL_FD("ILLEGAL_FD"),
    INC("INC"),
    JP("JP"),
    JR("JR"),
    LD("LD"),
    LDH("LDH"),
    NOP("NOP"),
    OR("OR"),
    POP("POP"),
    PREFIX("PREFIX"),
    PUSH("PUSH"),
    RES("RES"),
    RET("RET"),
    RETI("RETI"),
    RL("RL"),
    RLA("RLA"),
    RLC("RLC"),
    RLCA("RLCA"),
    RR("RR"),
    RRA("RRA"),
    RRC("RRC"),
    RRCA("RRCA"),
    RST("RST"),
    SBC("SBC"),
    SCF("SCF"),
    SET("SET"),
    SLA("SLA"),
    SRA("SRA"),
    SRL("SRL"),
    STOP("STOP"),
    SUB("SUB"),
    SWAP("SWAP"),
    XOR("XOR");

    public final String label;
    static final Function<Emu, Boolean> NONE_CB = (
            ctx -> {
                IO.println("NOT IMPLEMENTED YET");
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return false;
            }
    );
    static final Function<Emu, Boolean> NOP_CB = (
            ctx -> {
                IO.println("EXECUTING NOP");
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> DI_CB = (
            ctx -> {
                IO.println("EXECUTING DI");
                ctx.cpu.setIME(false);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> LD_CB = (
            ctx -> {
                IO.println("EXECUTING LD");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                switch (operands.length) {
                    case 2:
                        if (OperandType.isr8(operands[0])) {
                            if (OperandType.isr8(operands[1])) {
                                byte load = (byte) ctx.cpu.getRegFromOperandTypr(operands[1].getName());
                                ctx.cpu.setRegFromOperandTypr(operands[0].getName(), load);
                            } else if (operands[1].getName() == OperandType.IMMEDIATE_WORD) {
                                ctx.cpu.setRegFromOperandTypr(operands[0].getName(), params[0]);
                            } else if (OperandType.isr16(operands[1]) && !operands[1].isImmediate()) {
                                char addr = (char) ctx.cpu.getRegFromOperandTypr(operands[1].getName());
                                byte load = ctx.bus_read(addr);
                                ctx.cpu.setRegFromOperandTypr(operands[0].getName(), load);
                                if (operands[1].isDecrement())
                                    ctx.cpu.setRegFromOperandTypr(operands[1].getName(), (char) (addr - 1));
                                else if (operands[1].isIncrement())
                                    ctx.cpu.setRegFromOperandTypr(operands[1].getName(), (char) (addr + 1));
                            } else {
                                IO.println("UNSUPPORTED");
                                return false;
                            }
                        } else if (OperandType.isr16(operands[0])) {
                            if (operands[0].isImmediate()) {
                                if (OperandType.isr16(operands[1])) {
                                    char load = (char) ctx.cpu.getRegFromOperandTypr(operands[1].getName());
                                    ctx.cpu.setRegFromOperandTypr(operands[0].getName(), load);
                                } else
                                    ctx.cpu.setRegFromOperandTypr(operands[0].getName(), CPU.get18bit(params));
                            } else {
                                char addr = (char) ctx.cpu.getRegFromOperandTypr(operands[0].getName());
                                if (OperandType.isr8(operands[1])) {
                                    ctx.bus_write(addr, (byte) ctx.cpu.getRegFromOperandTypr(operands[1].getName()));
                                } else if (operands[1].getName() == OperandType.IMMEDIATE_WORD) {
                                    ctx.bus_write(addr, params[0]);
                                } else {
                                    IO.println("UNSUPPORTED");
                                    return false;
                                }
                            }
                        } else if (operands[0].getName() == OperandType.IMMEDIATE_DOUBLEWORD && operands[0].isImmediate()) {
                            if (OperandType.isr8(operands[1])) {
                                byte load = (byte) ctx.cpu.getRegFromOperandTypr(operands[1].getName());
                                ctx.bus_write(CPU.get18bit(params), load);
                            } else {
                                IO.println("UNSUPPORTED");
                                return false;
                            }
                        } else {
                            IO.println("UNSUPPORTED");
                            return false;
                        }
                        break;
                    case 3:
                    default:
                        IO.println("UNSUPPORTED");
                        return false;
                }
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );

    static final Function<Emu, Boolean> JP_CB = (
            ctx -> {
                IO.println("EXECUTING JP");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                switch (operands.length) {
                    case 0:
                        IO.println("TODO 0");
                        return false;
                    case 1:
                        if (operands[0].getName() == OperandType.ADDRESS_DOUBLEWORD) {
                            char newPC = (char) (params[1] << 8 | params[0]);
                            ctx.cpu.setRegPC(newPC);
                            return true;
                        }
                        return false;
                    case 2:
                        IO.println("TODO 2");
                        return false;
                    default:
                        IO.println("TODO DEF");
                        return false;
                }
                //ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                //return true;
            }
    );

    static final Function<Emu, Boolean> CALL_CB = (
            ctx -> {
                IO.println("EXECUTING CALL");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                ctx.cpu.setRegSP((char) (ctx.cpu.getRegSP() - 1));
                ctx.bus_write(ctx.cpu.getRegSP(), CPU.getHigh((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes())));
                ctx.cpu.setRegSP((char) (ctx.cpu.getRegSP() - 1));
                ctx.bus_write(ctx.cpu.getRegSP(), CPU.getLow((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes())));
                ctx.cpu.setRegPC(CPU.get18bit(params));
                return true;
            }
    );
    static final Function<Emu, Boolean> XOR_CB = (
            ctx -> {
                IO.println("EXECUTING XOR");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                if (OperandType.isr8(operands[1])) {
                    switch (operands[1].getName()) {
                        case REGISTER_A -> ctx.cpu.setRegA((byte) 0);
                        case REGISTER_B -> ctx.cpu.setRegA((byte) (ctx.cpu.getRegA() ^ ctx.cpu.getRegB()));
                        case REGISTER_C -> ctx.cpu.setRegA((byte) (ctx.cpu.getRegA() ^ ctx.cpu.getRegC()));
                        case REGISTER_D -> ctx.cpu.setRegA((byte) (ctx.cpu.getRegA() ^ ctx.cpu.getRegD()));
                        case REGISTER_E -> ctx.cpu.setRegA((byte) (ctx.cpu.getRegA() ^ ctx.cpu.getRegE()));
                        case REGISTER_H -> ctx.cpu.setRegA((byte) (ctx.cpu.getRegA() ^ ctx.cpu.getRegH()));
                        case REGISTER_L -> ctx.cpu.setRegA((byte) (ctx.cpu.getRegA() ^ ctx.cpu.getRegL()));
                    }
                }
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );

    static {
        for (final Opcodes val : EnumSet.allOf(Opcodes.class)) {
            val.callBack = NONE_CB;
        }
        NOP.callBack = NOP_CB;
        JP.callBack = JP_CB;
        DI.callBack = DI_CB;
        LD.callBack = LD_CB;
        CALL.callBack = CALL_CB;
        XOR.callBack = XOR_CB;
    }

    public Function<Emu, Boolean> callBack;

    Opcodes(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
