import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class InstructionOperands {
    private OperandType name;
    private boolean immediate;
    private boolean increment = false;
    private boolean decrement = false;
    private int bytes = 0;
}
