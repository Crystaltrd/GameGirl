import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Setter
@Getter
@ToString
public class InstructionSet {
    private Map<String,Instruction> unprefixed;
    private Map<String,Instruction> cbprefixed;
    public static InstructionSet fromFile(File opcodesFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(opcodesFile, InstructionSet.class);
    }

}
