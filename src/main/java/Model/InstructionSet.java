package Model;

import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@ToString
public class InstructionSet {
    private Map<String, Instruction> unprefixed;
    private Map<String, Instruction> cbprefixed;

    private ArrayList<Instruction> unprefixedFinal;
    private ArrayList<Instruction> cbprefixedFinal;

    public static InstructionSet fromFile(InputStream opcodesFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(opcodesFile, InstructionSet.class);
    }

    public void fillParsed() {
        unprefixedFinal = new ArrayList<>();
        cbprefixedFinal = new ArrayList<>();
        unprefixed.forEach((k, v) -> {
                    int index = Integer.parseInt(k.substring(2), 16);
                    unprefixedFinal.add(index, v);
                }
        );

        cbprefixed.forEach((k, v) -> {
                    int index = Integer.parseInt(k.substring(2), 16);
                    cbprefixedFinal.add(index, v);
                }
        );
    }

    public Instruction getUnprefixedInstruction(int opcode) {
        return unprefixedFinal.get(opcode);
    }

    public Instruction getCBPrefixedInstruction(int opcode) {
        return cbprefixedFinal.get(opcode);
    }
}
