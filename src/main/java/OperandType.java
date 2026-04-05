import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OperandType {
    RST_VEC_00("$00" ),
    RST_VEC_08("$08" ),
    RST_VEC_10("$10" ),
    RST_VEC_18("$18" ),
    RST_VEC_20("$20" ),
    RST_VEC_28("$28" ),
    RST_VEC_30("$30" ),
    RST_VEC_38("$38" ),
    BIT_0("0" ),
    BIT_1("1" ),
    BIT_2("2" ),
    BIT_3("3" ),
    BIT_4("4" ),
    BIT_5("5" ),
    BIT_6("6" ),
    BIT_7("7" ),
    DOUBLE_REGISTER_AF("AF" ),
    DOUBLE_REGISTER_BC("BC" ),
    DOUBLE_REGISTER_DE("DE" ),
    DOUBLE_REGISTER_HL("HL" ),
    DOUBLE_REGISTER_SP("SP" ),
    REGISTER_A("A" ),
    REGISTER_B("B" ),
    REGISTER_C("C" ),
    REGISTER_D("D" ),
    REGISTER_E("E" ),
    REGISTER_H("H" ),
    REGISTER_L("L" ),
    SIGNED_IMMEDIATE("e8" ),
    ADDRESS_DOUBLEWORD("a16" ),
    ADDRESS_BYTE("a8" ),
    IMMEDIATE_DOUBLEWORD("n16" ),
    IMMEDIATE_WORD("n8" ),
    FLAG_NOTCARRY("NC" ),
    FLAG_NOTZERO("NZ" ),
    FLAG_CARRY("CC" ),
    FLAG_ZERO("Z" );
    private final String label;

    OperandType(String label) {
        this.label = label;
    }

    public static boolean isr8(InstructionOperands operand) {
        OperandType op = operand.getName();
        return op == OperandType.REGISTER_A ||
                op == OperandType.REGISTER_B ||
                op == OperandType.REGISTER_C ||
                op == OperandType.REGISTER_D ||
                op == OperandType.REGISTER_E ||
                op == OperandType.REGISTER_H ||
                op == OperandType.REGISTER_L;
    }

    public static boolean isr16(InstructionOperands operand) {
        OperandType op = operand.getName();
        return op == OperandType.DOUBLE_REGISTER_AF ||
                op == OperandType.DOUBLE_REGISTER_BC ||
                op == OperandType.DOUBLE_REGISTER_DE ||
                op == OperandType.DOUBLE_REGISTER_HL ||
                op == OperandType.DOUBLE_REGISTER_SP;
    }

    public static boolean isCC(InstructionOperands operand) {
        OperandType op = operand.getName();
        return op == FLAG_CARRY || op == FLAG_NOTCARRY || op == FLAG_ZERO || op == FLAG_NOTZERO;
    }

    public static boolean isParamInBytecode(InstructionOperands operand) {
        OperandType op = operand.getName();
        return op == SIGNED_IMMEDIATE || op == ADDRESS_BYTE || op == ADDRESS_DOUBLEWORD || op == IMMEDIATE_WORD || op == IMMEDIATE_DOUBLEWORD;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

}
