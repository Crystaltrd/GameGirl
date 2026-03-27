import java.io.File;
import java.io.IOException;

public class Emu {
    private CPU cpu;
    private InstructionSet instructionSet;
    private Cartridge cartridge;
    
    Emu(String ROMFile,String OpCodesFile) throws IOException {
        cartridge = new Cartridge(new File(ROMFile));
        instructionSet = InstructionSet.fromFile(new File(OpCodesFile));
        cpu = new CPU();
        IO.println(cartridge.header.humanReadable());
        for (int i = 0; i <cartridge.data.length ; i++) {
            if(i % 16 == 0)
                IO.print(String.format("\n%1$04X: ",i));
            IO.print(String.format("%1$02X ", cartridge.data[i]));
        }
    }
}
