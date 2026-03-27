import com.fasterxml.jackson.annotation.JsonValue;

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

    Opcodes(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
