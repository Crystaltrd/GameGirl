package Model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FlagOperation {
    UNCHANGED("-"),
    SET_IF_ZERO("Z"),
    SET_IF_BCD_CARRY("H"),
    SET_IF_CARRY("C"),
    SET_IF_DEC("N"),
    SET_A7("A7"),
    RESET("0"),
    SET("1") ;

    public final String label;
    FlagOperation(String label) {
        this.label = label;
    }
    @JsonValue
    public String getLabel() {
        return label;
    }

}
