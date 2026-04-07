import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Setter
@Getter
@ToString
public class InstructionSet {
    private Map<String, Instruction> unprefixed;
    private Map<String, Instruction> cbprefixed;

    public static InstructionSet fromFile(InputStream opcodesFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(opcodesFile, InstructionSet.class);
    }

    public Instruction getUnprefixedInstruction(String opcode) {
        return unprefixed.getOrDefault(opcode, new Instruction());
    }

    public Instruction getCBPrefixedInstruction(String opcode) {
        return cbprefixed.getOrDefault(opcode, new Instruction());
    }
}
