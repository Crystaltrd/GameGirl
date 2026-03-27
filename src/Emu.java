import java.io.File;
import java.io.IOException;

public class Emu {
    private CPU cpu;
    private InstructionSet instructionSet;
    private Cartridge cartridge;

    Emu(String ROMFile, String OpCodesFile) throws IOException {
        cartridge = new Cartridge(new File(ROMFile));
        instructionSet = InstructionSet.fromFile(new File(OpCodesFile));
        cpu = new CPU();
        IO.println(cartridge.header.humanReadable());
        int i = 0x0100;
        while (i < 0x8000){
            Instruction instruction = instructionSet.getUnprefixedInstruction(String.format("0x%1$02X", cartridge.data[i]));
            if(instruction.getMnemonic() == Opcodes.PREFIX) {
                IO.println("NOT IMPLEMENTED");
                break;
            }
            IO.println("Executing: "+ instruction.getMnemonic() + String.format(" 0x%1$02X PC: 0x%2$02X", cartridge.data[i],i));
            i+=instruction.getBytes();
        }

    }
}
