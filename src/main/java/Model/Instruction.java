package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Instruction {
    
    private Opcodes mnemonic = Opcodes.NONE;
    private int bytes;
    private int[] cycles;
    private InstructionOperands[] operands;
    private boolean immediate;
    private InstructionFlags flags;
    public String toString() {
        String string = String.format("%s", mnemonic);
        for (InstructionOperands operands1 : operands) {
            if(OperandType.isParamInBytecode(operands1)){
                string = string.concat(String.format(" %s",operands1));
            }
        }
        return string;
    }

    public String toStringWithOperands(byte[] ops) {
        String string = String.format("%s", mnemonic);
        for (InstructionOperands operands1 : operands) {
                string = string.concat(String.format(" %s,",operands1.toStringWithData(ops)));
        }
        string = string.replaceAll(",$", "");
        return string;
    }
}
