import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
public class InstructionOperands {
    private OperandType name;
    private boolean immediate;
    private boolean increment = false;
    private boolean decrement = false;
    private int bytes = 0;

    public String toString() {
        return String.format("%c%s%c%c", immediate ? '\u0000' : '[', name, increment ? '+' : (decrement ? '-' : '\u0000'), immediate ? '\u0000' : ']').replace("\u0000", "");
    }

    public String toStringWithData(byte[] data) {
        String opstring = "";
        if (OperandType.isParamInBytecode(this)) opstring = switch (bytes) {
            case 2 -> opstring.concat(String.format("0x%02X%02X", data[1], data[0]));
            case 1 -> {
                HardwareRegisters reg;
                if ((reg = HardwareRegisters.findByValue(data[0])) == null)
                    yield opstring.concat(String.format("0x%02X(%d)", data[0], name == OperandType.SIGNED_IMMEDIATE ? data[0] : data[0] & 0xFF));
                else
                    yield opstring.concat(String.format("0x%02X(%d|r%s)", data[0], name == OperandType.SIGNED_IMMEDIATE ? data[0] : data[0] & 0xFF, reg.name()));
            }
            default -> opstring;
        };
        else {
            opstring = String.format("%s", name.getLabel());
        }
        opstring = String.format("%c%s%c%c", immediate ? '\u0000' : '[', opstring, increment ? '+' : (decrement ? '-' : '\u0000'), immediate ? '\u0000' : ']');
        return opstring.replace("\u0000", "");
    }

}
