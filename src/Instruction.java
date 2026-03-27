import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Setter
@Getter
@ToString
public class Instruction {


    private Opcodes mnemonic = Opcodes.NONE;
    private int bytes;
    private int[] cycles;
    private InstructionOperands[] operands;
    private boolean immediate;
    private InstructionFlags flags;

}
