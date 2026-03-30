import java.io.File;
import java.io.IOException;
import java.util.HexFormat;
import java.util.Scanner;

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
    public byte[] IORegisters = new byte[0xFF7F - 0xFF00 + 1]; //TODO: move to dedicated class later
    public byte[] HRAM = new byte[0xFFFE - 0xFF80 + 1];
    public byte IEReg = 0;

    public byte bus_read(char addr) {
        if (addr < 0x8000)
            return cartridge.read(addr);
        else if (addr < 0xA000)
            return ppu.read(addr);
        else if (addr < 0xC000)
            System.out.println("TODO: EXTERNAL RAM");
        else if (addr < 0xE000)
            return WorkRAM[addr - 0xC000];
        else if (addr < 0xFE00)
            return WorkRAM[addr - 0xE000];
        else if (addr < 0xFEA0)
            return ppu.read(addr);
        else if (addr < 0xFF00)
            return 0;
        else if (addr < 0xFF80)
            return IORegisters[addr - 0xFF00];
        else if (addr < 0xFFFF)
            return HRAM[addr & 0x00FF];
        else {
            return IEReg;
        }
        return 0;
    }

    public void bus_write(char addr, byte val) {
        if (addr < 0x8000)
            cartridge.write(addr, val);
        else if (addr < 0xA000)
            ppu.write(addr, val);
        else if (addr < 0xC000)
            System.out.println("EXTERNAL RAM: TODO");//TODO
        else if (addr < 0xE000)
            WorkRAM[addr - 0xC000] = val;
        else if (addr < 0xFE00)
            WorkRAM[addr - 0xE000] = val;
        else if (addr < 0xFEA0)
            ppu.write(addr, val);
        else if (addr < 0xFF00)
            System.out.println("Unwritable Memory");
        else if (addr < 0xFF80)
            IORegisters[addr - 0xFF00] = val;
        else if (addr < 0xFFFF)
            HRAM[addr - 0xFF80] = val;
        else {
            IEReg = val;
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
        System.out.println(cpu);
        if (instruction.getMnemonic() == Opcodes.PREFIX) {
            System.out.print("PREFIXED: ");
            cpu.setRegPC((char) (cpu.getRegPC() + 1));
            instruction = fetchInstr(true);
            params = fetchParams(instruction);
        }
        System.out.printf("%#04X: %-10s (0x%02X %s)%n",
                (short) cpu.getRegPC(),
                instruction.getMnemonic(),
                bus_read(cpu.getRegPC()),
                commaFormat.formatHex(params));
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
        System.out.println(cartridge.header.humanReadable());
        cpu.setRegPC((char) 0x0100);
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        while (step()) {
            
        }

    }
}
