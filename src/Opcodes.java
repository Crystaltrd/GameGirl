import com.fasterxml.jackson.annotation.JsonValue;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Function;

public enum Opcodes {
    NONE("NONE"),
    //  ==============================

    CP("CP"),
    EI("EI"),
    DI("DI"),
    DEC("DEC"),
    INC("INC"),
    JP("JP"),
    JR("JR"),
    LD("LD"),
    LDH("LDH"),
    NOP("NOP"),
    OR("OR"),
    PREFIX("PREFIX"),
    XOR("XOR"),
    AND("AND"),
    CPL("CPL"),
    CCF("CCF"),
    SCF("SCF"),
    ADD("ADD"),
    POP("POP"),
    PUSH("PUSH"),
    RET("RET"),
    RETI("RETI"),
    CALL("CALL"),
    //  ==============================
    RES("RES"),
    RL("RL"),
    RLC("RLC"),
    RRC("RRC"),
    RR("RR"),
    SLA("SLA"),
    SRA("SRA"),
    SWAP("SWAP"),
    SRL("SRL"),
    BIT("BIT"),
    //  ==============================
    RLA("RLA"),
    HALT("HALT"),
    STOP("STOP"),
    ADC("ADC"),
    DAA("DAA"),
    RLCA("RLCA"),
    RRA("RRA"),
    RRCA("RRCA"),
    RST("RST"),
    SBC("SBC"),
    SET("SET"),
    SUB("SUB"),
    // =================================
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
    // =================================
    ;
    public final String label;
    static final Function<Emu, Boolean> NONE_CB = (
            ctx -> {
                System.out.println("NOT IMPLEMENTED YET");
                return false;
            }
    );
    static final Function<Emu, Boolean> NOP_CB = (
            ctx -> {
//                System.out.println("EXECUTING NOP");
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> DI_CB = (
            ctx -> {
//                System.out.println("EXECUTING DI");
                ctx.cpu.setIME(false);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> EI_CB = (
            ctx -> {
//                System.out.println("EXECUTING EI");
                ctx.cpu.setQueuedIME(true);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> CPL_CB = (
            ctx -> {
//                System.out.println("EXECUTING CPL");
                ctx.cpu.setRegA((byte) ~ctx.cpu.getRegA());
                ctx.cpu.setSFlag(true);
                ctx.cpu.setHFlag(true);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );

    static final Function<Emu, Boolean> PUSH_CB = (
            ctx -> {
//                System.out.println("EXECUTING PUSH");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                char word = (char) ctx.cpu.getRegFromOperandType(operands[0].getName());
                ctx.push(word);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> POP_CB = (
            ctx -> {
//                System.out.println("EXECUTING POP");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                char word = ctx.pop();
                ctx.cpu.setRegFromOperandType(operands[0].getName(), word);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> CCF_CB = (
            ctx -> {
//                System.out.println("EXECUTING CCF");
                ctx.cpu.setCFlag(!ctx.cpu.getFlagReg().isCarryFlag());
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> SCF_CB = (
            ctx -> {
//                System.out.println("EXECUTING SCF");
                ctx.cpu.setCFlag(true);
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> JP_CB = (
            ctx -> {
//                System.out.println("EXECUTING JP");
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
                        else
                            ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                    }
                }
                return true;
            }
    );
    static final Function<Emu, Boolean> CALL_CB = (
            ctx -> {
//                System.out.println("EXECUTING CALL");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                char nextPC = (char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes());
                ctx.push(nextPC);
                if (Objects.requireNonNull(operands[0].getName()) == OperandType.ADDRESS_DOUBLEWORD) {
                    ctx.cpu.setRegPC(CPU.get18bit(params));
                } else {
                    if ((boolean) ctx.cpu.getRegFromOperandType(operands[0].getName()))
                        ctx.cpu.setRegPC(CPU.get18bit(params));
                    else
                        ctx.cpu.setRegPC(nextPC);
                }
                return true;
            }
    );
    static final Function<Emu, Boolean> RST_CB = (
            ctx -> {
//                System.out.println("EXECUTING RST");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                char nextPC = (char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes());
                ctx.push(nextPC);
                char addr = (char) Integer.parseInt(operands[0].getName().getLabel().replace("$", ""), 16);
                ctx.cpu.setRegPC(addr);
                return true;
            }
    );
    static final Function<Emu, Boolean> RET_CB = (
            ctx -> {
//                System.out.println("EXECUTING RET");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                char nextPC = (char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes());
                if (params.length == 0) {
                    ctx.cpu.setRegPC(ctx.pop());
                } else {
                    if ((boolean) ctx.cpu.getRegFromOperandType(operands[0].getName()))
                        ctx.cpu.setRegPC(ctx.pop());
                    else
                        ctx.cpu.setRegPC(nextPC);
                }
                return true;
            }
    );
    static final Function<Emu, Boolean> RETI_CB = (
            ctx -> {
//                System.out.println("EXECUTING RETI");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                ctx.cpu.setRegPC(ctx.pop());
                ctx.cpu.setQueuedIME(true);
                return true;
            }
    );
    static final Function<Emu, Boolean> JR_CB = (
            ctx -> {
//                System.out.println("EXECUTING JR");
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
//                System.out.println("EXECUTING LD");
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
                            } else if (operands[1].getName() == OperandType.ADDRESS_DOUBLEWORD && !operands[1].isImmediate()) {
                                char addr = CPU.get18bit(params);
                                byte load = ctx.bus_read(addr);
                                ctx.cpu.setRegFromOperandType(operands[0].getName(), load);
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
                        ctx.cpu.setCFlag(result.Carry == FlagOperation.SET);
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
    static final Function<Emu, Boolean> LDH_CB = (
            ctx -> {
//        System.out.println("EXECUTING LDH");
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
            }
    );
    static final Function<Emu, Boolean> XOR_CB = (
            ctx -> {
//                System.out.println("EXECUTING XOR");
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

    static final Function<Emu, Boolean> ADD_CB = (
            ctx -> {
//                System.out.println("EXECUTING ADD");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                byte[] params = ctx.cpu.getCurrParams();
                int load;
                int op1;
                int result;
                boolean z = false, n = false, h = false, c = false;
                if (OperandType.isr8(operands[0])) {
                    op1 = ctx.cpu.getRegA() & 0xFF;
                } else {
                    op1 = ((char) ctx.cpu.getRegFromOperandType(operands[0].getName()) & 0xFFFF);
                }
                if (OperandType.isr16(operands[1])) {
                    if (!operands[1].isImmediate()) {
                        char addr = (char) ctx.cpu.getRegFromOperandType(operands[1].getName());
                        load = (char) (ctx.bus_read(addr) & 0xFF);
                    } else {
                        load = (char) ctx.cpu.getRegFromOperandType(operands[1].getName()) & 0xFFFF;
                    }
                } else if (OperandType.isr8(operands[1])) {
                    load = (byte) ctx.cpu.getRegFromOperandType(operands[1].getName()) & 0xFF;
                } else {
                    load = params[0] & 0xFF;
                }
                result = op1 + load;
                if (OperandType.isr8(operands[0])) {
                    z = (result & 0xFF) == 0;
                    h = (load & 0xF) + (op1 & 0xF) > 0xF;
                    c = result > 0xFF;
                    ctx.cpu.setCFlag(c);
                    ctx.cpu.setHFlag(h);
                    ctx.cpu.setZFlag(z);
                    ctx.cpu.setSFlag(n);
                    ctx.cpu.setRegA((byte) (result & 0xFF));
                } else {
                    if (operands[0].getName() == OperandType.DOUBLE_REGISTER_SP) {
                        h = (load & 0xF) + (op1 & 0xF) > 0xF;
                        c = result > 0xFF;
                        ctx.cpu.setCFlag(c);
                        ctx.cpu.setHFlag(h);
                        ctx.cpu.setZFlag(false);
                        ctx.cpu.setSFlag(false);
                        ctx.cpu.setRegSP((char) (result & 0xFFFF));
                    } else {
                        h = (op1 & 0xFF) + (load & 0xFF) > 0xFF;
                        c = result > 0xFFFF;
                        ctx.cpu.setCFlag(c);
                        ctx.cpu.setHFlag(h);
                        ctx.cpu.setSFlag(false);
                        ctx.cpu.setRegHL((char) (result & 0xFFFF));
                    }
                }
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );
    static final Function<Emu, Boolean> CP_CB = (
            ctx -> {
//                System.out.println("EXECUTING CP");
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
                ctx.cpu.setCFlag((char) (load & 0xFF) > (char) (ctx.cpu.getRegA() & 0xFF));
                ctx.cpu.setRegPC((char) (ctx.cpu.getRegPC() + ctx.cpu.getCurrInstruction().getBytes()));
                return true;
            }
    );

    static final Function<Emu, Boolean> DEC_CB = (
            ctx -> {
//                System.out.println("EXECUTING DEC");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
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

    static final Function<Emu, Boolean> INC_CB = (
            ctx -> {
//                System.out.println("EXECUTING INC");
                Instruction instruction = ctx.cpu.getCurrInstruction();
                InstructionOperands[] operands = instruction.getOperands();
                if (OperandType.isr8(operands[0])) {
                    ALUResult result = ALU.INC((byte) ctx.cpu.getRegFromOperandType(operands[0].getName()));
                    ctx.cpu.setRegFromOperandType(operands[0].getName(), result.result);
                    ctx.cpu.setFlagReg(result);
                } else if (OperandType.isr16(operands[0])) {
                    if (operands[0].isImmediate()) {
                        char new_val = (char) ((char) ctx.cpu.getRegFromOperandType(operands[0].getName()) + 1);
                        ctx.cpu.setRegFromOperandType(operands[0].getName(), new_val);
                    } else {
                        char addr = (char) ctx.cpu.getRegFromOperandType(operands[0].getName());
                        byte load = ctx.bus_read(addr);
                        ALUResult result = ALU.INC(load);
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
//                System.out.println("EXECUTING OR");
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
//                System.out.println("EXECUTING AND");
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
        INC.callBack = INC_CB;
        ADD.callBack = ADD_CB;
        SCF.callBack = SCF_CB;
        CCF.callBack = CCF_CB;
        POP.callBack = POP_CB;
        PUSH.callBack = PUSH_CB;
        CPL.callBack = CPL_CB;
        EI.callBack = EI_CB;
        CALL.callBack = CALL_CB;
        RET.callBack = RET_CB;
        RETI.callBack = RETI_CB;
        RST.callBack = RST_CB;
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
