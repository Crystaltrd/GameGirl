import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OperandType {
    RST_VEC_00("$00"),
    RST_VEC_08("$08"),
    RST_VEC_10("$10"),
    RST_VEC_18("$18"),
    RST_VEC_20("$20"),
    RST_VEC_28("$28"),
    RST_VEC_30("$30"),
    RST_VEC_38("$38"),
    BIT_0("0"),
    BIT_1("1"),
    BIT_2("2"),
    BIT_3("3"),
    BIT_4("4"),
    BIT_5("5"),
    BIT_6("6"),
    BIT_7("7"),
    DOUBLE_REGISTER_AF("AF",false,true),
    DOUBLE_REGISTER_BC("BC",false,true),
    DOUBLE_REGISTER_DE("DE",false,true),
    DOUBLE_REGISTER_HL("HL",false,true),
    DOUBLE_REGISTER_SP("SP",false,true),
    REGISTER_A("A",true,false),
    REGISTER_B("B",true,false),
    REGISTER_C("C",true,false),
    REGISTER_D("D",true,false),
    REGISTER_E("E",true,false),
    REGISTER_H("H",true,false),
    REGISTER_L("L",true,false),
    SIGNED_IMMEDIATE("e8"),
    ADDRESS_DOUBLEWORD("a16"),
    ADDRESS_BYTE("a8"),
    IMMEDIATE_DOUBLEWORD("n16"),
    IMMEDIATE_WORD("n8"),
    AMBIGUOUS_CARRYFLAG_OR_REGISTERC("C"),
    FLAG_NOTCARRY("NC"),
    FLAG_NOTZERO("NZ"),
    FLAG_ZERO("Z");
    private final String label;
    private boolean reg8 = false;
    private boolean reg16 = false;
    OperandType(String label) {
        this.label = label;
    }
    OperandType(String label, boolean reg8, boolean reg16) {
        this.label = label;
        this.reg8 = reg8;
        this.reg16 = reg16;
    }    @JsonValue
    public String getLabel() {
        return label;
    }

}
