import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class InstructionFlags {

    @JsonProperty("Z")
    private FlagOperation Z;
    @JsonProperty("N")
    private FlagOperation N;
    @JsonProperty("H")
    private FlagOperation H;
    @JsonProperty("C")
    private FlagOperation C;
}
