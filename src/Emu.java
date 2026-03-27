import java.io.File;
import java.io.IOException;
import java.util.HexFormat;

public class Emu {
    public CPU cpu;
    public InstructionSet instructionSet;
    public Cartridge cartridge;

    public byte bus_read(char addr) {
        if (addr < 0x8000)
            return cartridge.read(addr);
        return 0;
    }

    public void bus_write(char addr, byte val) {
        if (addr < 0x8000)
            cartridge.write(addr,val);
    }
    public byte[] fetchParams(Instruction instruction) {
        byte[] params = new byte[instruction.getBytes() - 1];
        for (char i = 0; i < params.length; i++) {
            params[i] = bus_read((char) (cpu.getRegPC() + i + 1));
        }
        return params;
    }

    public Instruction fetchInstr(boolean prefixed) {
        if (prefixed)
            return instructionSet.getCBPrefixedInstruction(String.format("0x%1$02X", bus_read(cpu.getRegPC())));
        else
            return instructionSet.getUnprefixedInstruction(String.format("0x%1$02X", bus_read(cpu.getRegPC())));
    }

    public boolean execute() {
        Instruction instruction = cpu.getCurrInstruction();
        byte[] params = cpu.getCurrParams();
        HexFormat commaFormat = HexFormat.ofDelimiter(" ").withPrefix("");
        if (instruction.getMnemonic() == Opcodes.PREFIX) {
            IO.print("PREFIXED: ");
            cpu.setRegPC((char) (cpu.getRegPC() + 1));
            instruction = fetchInstr(true);
            params = fetchParams(instruction);
        }
        IO.println(String.format("%#04X: %-10s (0x%02X %s)",
                (short) cpu.getRegPC(),
                instruction.getMnemonic(),
                bus_read(cpu.getRegPC()),
                commaFormat.formatHex(params))
        );
        IO.println(cpu);
        return instruction.getMnemonic().callBack.apply(this);
    }

    public boolean step() {
        if (!cpu.isHalted()) {
            cpu.setCurrInstruction(fetchInstr(false));
            cpu.setCurrParams(fetchParams(cpu.getCurrInstruction()));
            return execute();
        }
        return true;
    }

    Emu(String ROMFile, String OpCodesFile) throws IOException {
        bus_read((char) 0x9000);
        cartridge = new Cartridge(new File(ROMFile));
        instructionSet = InstructionSet.fromFile(new File(OpCodesFile));
        cpu = new CPU();
        IO.println(cartridge.header.humanReadable());
        cpu.setRegPC((char) 0x0100);
        while (step()) {

        }

    }
}
