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
                    case 0:
                        IO.println("TODO 0");
                        return false;
                    case 1:
                        IO.println("TODO 1");
                        return false;
                    case 2:
                        if (operands[0].getName().isReg16() && operands[1].getName() == OperandType.IMMEDIATE_DOUBLEWORD) {
                            switch (operands[0].getName()) {
                                case DOUBLE_REGISTER_AF -> {
                                    ctx.cpu.setFlagReg(params[0]);
                                    ctx.cpu.setRegA(params[1]);
                                }
                                case DOUBLE_REGISTER_BC -> {
                                    ctx.cpu.setRegC(params[0]);
                                    ctx.cpu.setRegB(params[1]);
                                }
                                case DOUBLE_REGISTER_DE -> {
                                    ctx.cpu.setRegE(params[0]);
                                    ctx.cpu.setRegD(params[1]);
                                }
                                case DOUBLE_REGISTER_HL -> {
                                    ctx.cpu.setRegL(params[0]);
                                    ctx.cpu.setRegH(params[1]);

                                }
                                case DOUBLE_REGISTER_SP -> {
                                    ctx.cpu.setRegSP(CPU.get18bit(params));
                                }
                            }
                        } else {
                            IO.println("TODO");
                            return false;
                        }
                        break;
                    default:
                        IO.println("TODO DEF");
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
    static {
        for (final Opcodes val : EnumSet.allOf(Opcodes.class)) {
            val.callBack = NONE_CB;
        }
        NOP.callBack = NOP_CB;
        JP.callBack = JP_CB;
        DI.callBack = DI_CB;
        LD.callBack = LD_CB;
        CALL.callBack = CALL_CB;
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
