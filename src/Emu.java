import java.io.File;
import java.io.IOException;
import java.util.HexFormat;

// 0x0000 -> 0x3FFF: ROM Bank 0
// 0x4000 -> 0x7FFF: ROM Bank 1-NN, switchable
// 0x8000 -> 0x9FFF: VRAM
// 0xA000 -> 0xBFFF: External RAM, switchable

// 0xC000 -> 0xDFFF: WRAM
// 0xE000 -> 0xFDFF: Echo RAM
// 0xFE00 -> 0xFE9F: OAM
// 0xFEA0 -> 0xFEFF: UNUSABLE
// 0xFF00 -> 0xFF7F: IO Registers
// 0xFF80 -> 0xFFFE: HRAM
// 0xFFFF  Interrupt Enable Register
public class Emu {
    public CPU cpu;
    public InstructionSet instructionSet;
    public Cartridge cartridge;
    public PPU ppu = new PPU();
    public byte[] WorkRAM = new byte[0xDFFF - 0xC000 + 1];

    public byte bus_read(char addr) {
        if (addr < 0x8000)
            return cartridge.read(addr);
        else if (addr < 0xA000)
            return ppu.read(addr);
        else if (addr < 0xC000)
            return WorkRAM[0xC000 - addr];
        else if (addr < 0xFE00)
            return WorkRAM[0xFE00 - addr];
        else if (addr < 0xFEA0)
            return ppu.read(addr);
        else if (addr < 0xFF00)
            return 0;
        else if (addr < 0xFF80)
            IO.println("TODO: IO REGISTERS");
        else if(addr < 0xFFFF)
            IO.println("TODO: HRAM");
        else{
            IO.println("TODO: IE REGISTER");
        }
        return 0;
    }

    public void bus_write(char addr, byte val) {
        if (addr < 0x8000)
             cartridge.write(addr,val);
        else if (addr < 0xA000)
             ppu.write(addr,val);
        else if (addr < 0xC000)
             WorkRAM[0xC000 - addr] = val;
        else if (addr < 0xFE00)
             WorkRAM[0xFE00 - addr] = val;
        else if (addr < 0xFEA0)
             ppu.write(addr,val);
        else if (addr < 0xFF00)
            IO.println("NUH UH");
        else if (addr < 0xFF80)
            IO.println("TODO: IO REGISTERS");
        else if(addr < 0xFFFF)
            IO.println("TODO: HRAM");
        else{
            IO.println("TODO: IE REGISTER");
        }
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
        IO.println(cpu);
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
        cartridge = new Cartridge(new File(ROMFile));
        instructionSet = InstructionSet.fromFile(new File(OpCodesFile));
        cpu = new CPU();
        IO.println(cartridge.header.humanReadable());
        cpu.setRegPC((char) 0x0100);
        while (step()) {
        }

    }
}
