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
                System.out.println("NOT IMPLEMENTED YET");
                return false;
            }
    );
    static final Function<Emu, Boolean> NOP_CB = (
            ctx -> {
                System.out.println("EXECUTING NOP");
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> DI_CB = (
            ctx -> {
                System.out.println("EXECUTING DI");
                ctx.cpu.setIME(false);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> JP_CB = (
            ctx -> {
                System.out.println("EXECUTING JP");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                switch (operands[0].getName()) {
                    case DOUBLE_REGISTER_HL ->
                            ctx.cpu.setRegPC((char) ctx.cpu.getRegFromOperandType(OperandType.DOUBLE_REGISTER_HL));
                    case ADDRESS_DOUBLEWORD -> ctx.cpu.setRegPC(CPU.get18bit(params));
                    default -> {
                        if ((boolean) ctx.cpu.getRegFromOperandType(operands[0].getName()))
                            ctx.cpu.setRegPC(CPU.get18bit(params));
                    }
                }
                return true;
            }
    );
    static final Function<Emu, Boolean> JR_CB = (
            ctx -> {
                System.out.println("EXECUTING JR");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                char nextPC = (char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes());
                switch (operands[0].getName()) {
                    case SIGNED_IMMEDIATE: {
                        ALUResult result = ALU.addByteToReg(nextPC, params[0], true);
                        ctx.cpu.setRegPC((char) result.result);
                    }
                    break;
                    case FLAG_NOTCARRY:
                    case FLAG_NOTZERO:
                    case FLAG_CARRY:
                    case FLAG_ZERO:
                        if ((boolean) ctx.cpu.getRegFromOperandType(operands[0].getName())) {
                            ALUResult result = ALU.addByteToReg(nextPC, params[0], true);
                            ctx.cpu.setRegPC((char) result.result);
                        } else {
                            ctx.cpu.setRegPC(nextPC);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
    );

    static final Function<Emu, Boolean> LD_CB = (
            ctx -> {
                System.out.println("EXECUTING LD");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                switch (operands.length) {
                    case 2:
                        if (OperandType.isr8(operands[0])) {
                            if (OperandType.isr8(operands[1])) {
                                byte load = (byte) ctx.cpu.getRegFromOperandType(operands[1].getName());
                                ctx.cpu.setRegFromOperandType(operands[0].getName(), load);
                            } else if (operands[1].getName() == OperandType.IMMEDIATE_WORD) {
                                ctx.cpu.setRegFromOperandType(operands[0].getName(), params[0]);
                            } else if (OperandType.isr16(operands[1]) && !operands[1].isImmediate()) {
                                char addr = (char) ctx.cpu.getRegFromOperandType(operands[1].getName());
                                byte load = ctx.bus_read(addr);
                                ctx.cpu.setRegFromOperandType(operands[0].getName(), load);
                                if (operands[1].isDecrement())
                                    ctx.cpu.setRegFromOperandType(operands[1].getName(), (char) (addr - 1));
                                else if (operands[1].isIncrement())
                                    ctx.cpu.setRegFromOperandType(operands[1].getName(), (char) (addr + 1));
                            } else {
                                System.out.println("UNSUPPORTED");
                                return false;
                            }
                        } else if (OperandType.isr16(operands[0])) {
                            if (operands[0].isImmediate()) {
                                if (OperandType.isr16(operands[1])) {
                                    char load = (char) ctx.cpu.getRegFromOperandType(operands[1].getName());
                                    ctx.cpu.setRegFromOperandType(operands[0].getName(), load);
                                } else
                                    ctx.cpu.setRegFromOperandType(operands[0].getName(), CPU.get18bit(params));
                            } else {
                                char addr = (char) ctx.cpu.getRegFromOperandType(operands[0].getName());
                                if (OperandType.isr8(operands[1])) {
                                    ctx.bus_write(addr, (byte) ctx.cpu.getRegFromOperandType(operands[1].getName()));
                                } else if (operands[1].getName() == OperandType.IMMEDIATE_WORD) {
                                    ctx.bus_write(addr, params[0]);
                                } else {
                                    System.out.println("UNSUPPORTED");
                                    return false;
                                }
                                if (operands[0].isDecrement())
                                    ctx.cpu.setRegFromOperandType(operands[0].getName(), (char) (addr - 1));
                                else if (operands[0].isIncrement())
                                    ctx.cpu.setRegFromOperandType(operands[0].getName(), (char) (addr + 1));
                            }


                        } else if (operands[0].getName() == OperandType.ADDRESS_DOUBLEWORD && !operands[0].isImmediate()) {
                            if (OperandType.isr8(operands[1])) {
                                byte load = (byte) ctx.cpu.getRegFromOperandType(operands[1].getName());
                                ctx.bus_write(CPU.get18bit(params), load);
                            } else {
                                System.out.println("UNSUPPORTED");
                                return false;
                            }
                        } else {
                            System.out.println("UNSUPPORTED");
                            return false;
                        }
                        break;
                    case 3:
                        ALUResult result = ALU.addByteToReg(ctx.cpu.getRegSP(), params[0], true);
                        ctx.cpu.setFlagReg(result);
                        ctx.cpu.setRegFromOperandType(OperandType.DOUBLE_REGISTER_HL, result.result);
                        break;
                    default:
                        System.out.println("UNSUPPORTED");
                        return false;
                }
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> LDH_CB = ctx -> {
        System.out.println("EXECUTING LDH");
        Instruction instruction = ctx.cpu.getCurrInstruction();
        InstructionOperands[] operands = instruction.getOperands();
        byte[] params = ctx.cpu.getCurrParams();
        char load;
        if (operands[0].getName() == OperandType.REGISTER_A) {
            if (operands[1].getName() == OperandType.ADDRESS_BYTE)
                load = (char) (0xFF00 | params[0]);
            else
                load = (char) (0xFF00 | ctx.cpu.getRegC());
            byte val = ctx.bus_read(load);
            ctx.cpu.setRegA(val);
        } else {
            if (operands[0].getName() == OperandType.ADDRESS_BYTE)
                load = (char) (0xFF00 | params[0]);
            else
                load = (char) (0xFF00 | ctx.cpu.getRegC());
            ctx.bus_write(load, ctx.cpu.getRegA());
        }
        ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));

        return true;
    };
    static final Function<Emu, Boolean> XOR_CB = (
            ctx -> {
                System.out.println("EXECUTING XOR");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                byte load;
                if (OperandType.isr8(operands[1])) {
                    load = (byte) ctx.cpu.getRegFromOperandType(operands[1].getName());
                } else if (operands[1].getName() == OperandType.IMMEDIATE_WORD) {
                    load = params[0];
                } else {
                    char addr = (char) ctx.cpu.getRegFromOperandType(operands[1].getName());
                    load = ctx.bus_read(addr);
                }
                ALUResult result = ALU.XOR(ctx.cpu.getRegA(), load);
                ctx.cpu.setRegA((byte) result.result);
                ctx.cpu.setFlagReg(result);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );

    static final Function<Emu, Boolean> CP_CB = (
            ctx -> {
                System.out.println("EXECUTING CP");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                byte load;
                if (OperandType.isr8(operands[1])) {
                    load = (byte) ctx.cpu.getRegFromOperandType(operands[1].getName());
                } else if (operands[1].getName() == OperandType.IMMEDIATE_WORD) {
                    load = params[0];
                } else {
                    char addr = (char) ctx.cpu.getRegFromOperandType(operands[1].getName());
                    load = ctx.bus_read(addr);
                }
                byte result = (byte) (ctx.cpu.getRegA() - load);
                ctx.cpu.setSFlag(true);
                ctx.cpu.setZFlag(result == 0);
                ctx.cpu.setHFlag((ctx.cpu.getRegA() & 0xF) + (result & 0xF) > 0xF);
                ctx.cpu.setCFlag((char)(load & 0xFF) > (char)(ctx.cpu.getRegA() & 0xFF));
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );

    static final Function<Emu, Boolean> DEC_CB = (
            ctx -> {
                System.out.println("EXECUTING DEC");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                if (OperandType.isr8(operands[0])) {
                    ALUResult result = ALU.DEC((byte) ctx.cpu.getRegFromOperandType(operands[0].getName()));
                    ctx.cpu.setRegFromOperandType(operands[0].getName(), result.result);
                    ctx.cpu.setFlagReg(result);
                } else if (OperandType.isr16(operands[0])) {
                    if (operands[0].isImmediate()) {
                        ctx.cpu.setRegFromOperandType(operands[0].getName(), ctx.cpu.getRegFromOperandType(operands[0].getName()));
                    } else {
                        char addr = (char) ctx.cpu.getRegFromOperandType(operands[0].getName());
                        byte load = ctx.bus_read(addr);
                        ALUResult result = ALU.DEC(load);
                        ctx.bus_write(addr, (byte) result.result);
                        ctx.cpu.setFlagReg(result);
                    }
                }
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> OR_CB = (
            ctx -> {
                System.out.println("EXECUTING OR");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                byte load;
                if (OperandType.isr8(operands[1])) {
                    load = (byte) ctx.cpu.getRegFromOperandType(operands[1].getName());
                } else if (operands[1].getName() == OperandType.IMMEDIATE_WORD) {
                    load = params[0];
                } else {
                    char addr = (char) ctx.cpu.getRegFromOperandType(operands[1].getName());
                    load = ctx.bus_read(addr);
                }
                ALUResult result = ALU.OR(ctx.cpu.getRegA(), load);
                ctx.cpu.setRegA((byte) result.result);
                ctx.cpu.setFlagReg(result);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );

    static final Function<Emu, Boolean> AND_CB = (
            ctx -> {
                System.out.println("EXECUTING AND");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                byte load;
                if (OperandType.isr8(operands[1])) {
                    load = (byte) ctx.cpu.getRegFromOperandType(operands[1].getName());
                } else if (operands[1].getName() == OperandType.IMMEDIATE_WORD) {
                    load = params[0];
                } else {
                    char addr = (char) ctx.cpu.getRegFromOperandType(operands[1].getName());
                    load = ctx.bus_read(addr);
                }
                ALUResult result = ALU.AND(ctx.cpu.getRegA(), load);
                ctx.cpu.setRegA((byte) result.result);
                ctx.cpu.setFlagReg(result);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );

    static {
        for (final Opcodes val : EnumSet.allOf(Opcodes.class)) {
            val.callBack = NONE_CB;
        }
        NOP.callBack = NOP_CB;
        DI.callBack = DI_CB;
        LD.callBack = LD_CB;
        LDH.callBack = LDH_CB;
        JP.callBack = JP_CB;
        JR.callBack = JR_CB;
        XOR.callBack = XOR_CB;
        AND.callBack = AND_CB;
        OR.callBack = OR_CB;
        CP.callBack = CP_CB;
        DEC.callBack = DEC_CB;
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
