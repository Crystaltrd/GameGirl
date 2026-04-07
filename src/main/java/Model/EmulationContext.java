package Model;

import java.io.InputStream;
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
public class EmulationContext {
    public CPU cpu;
    public InstructionSet instructionSet;
    public Cartridge cartridge;
    public PPU ppu = new PPU();

    public IORegisters ioRegisters = new IORegisters(this);
    public byte[] WorkRAM = new byte[0xDFFF - 0xC000 + 1];
    public byte[] HRAM = new byte[0xFFFE - 0xFF80 + 1];
    public InterruptFlag IEReg = new InterruptFlag((byte) 0);
    public long ticks = 0;
    public boolean gameboyDoctor;
    public boolean silent;

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
        else if (addr < 0xFF80) {
            return ioRegisters.read((char) (addr - 0xFF00));
        } else if (addr < 0xFFFF)
            return HRAM[addr - 0xFF80];
        else {
            return IEReg.getByte();
        }
        return 0;
    }

    public char pop() {
        byte lowbyte = bus_read(cpu.getRegSP());
        cpu.setRegSP((char) (cpu.getRegSP() + 1));
        byte highbyte = bus_read(cpu.getRegSP());
        cpu.setRegSP((char) (cpu.getRegSP() + 1));
        return CPU.get16bit(highbyte, lowbyte);
    }

    public void push(char word) {
        cpu.setRegSP((char) (cpu.getRegSP() - 1));
        bus_write(cpu.getRegSP(), CPU.getHigh(word));
        cpu.setRegSP((char) (cpu.getRegSP() - 1));
        bus_write(cpu.getRegSP(), CPU.getLow(word));
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
        else if (addr < 0xFF80) {
            ioRegisters.write((char) (addr - 0xFF00), val);
        } else if (addr < 0xFFFF)
            HRAM[addr - 0xFF80] = val;
        else {
            IEReg.setByte(val);
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

        return instruction.getMnemonic().callBack.apply(this);
    }

    public boolean GBDoctorskipInstr = false;

    public StringBuilder dbg_msg = new StringBuilder();

    public boolean step() {
        if (!cpu.isHalted()) {
            cpu.setCurrInstruction(fetchInstr(false));
            if (cpu.getCurrInstruction().getMnemonic() == Opcodes.PREFIX) {
                cpu.setRegPC((char) (cpu.getRegPC() + 1));
                cpu.setCurrInstruction(fetchInstr(true));
            }
            tick(1);
            cpu.setCurrParams(fetchParams(cpu.getCurrInstruction()));
            if (!silent)
                if (!gameboyDoctor) {
                    System.out.printf("%04X - 0x%02X: %-30sA:%02X F:%s BC:%04X DE:%04X HL:%04X SP:%04X\n", (short) cpu.getRegPC(),
                            bus_read(cpu.getRegPC()),
                            cpu.getCurrInstruction().toStringWithOperands(cpu.getCurrParams()),
                            cpu.getRegA(),
                            cpu.getFlagReg(),
                            (short) cpu.getRegBC(),
                            (short) cpu.getRegDE(),
                            (short) cpu.getRegHL(),
                            (short) cpu.getRegSP());
                }
            boolean ret = execute();

            if (cpu.isQueuedIME()) {
                cpu.setIME(true);
            }
            return ret;
        }
        return false;
    }

    public void dbg_update() {
        if (bus_read((char) 0xFF02) != 0) {
            char c = (char) bus_read((char) 0xFF01);
            dbg_msg.append(c);
            bus_write((char) 0xFF02, (byte) 0);
        }
    }

    public void dbg_print() {
        if (!dbg_msg.isEmpty()) {
            System.out.println("DBG: " + dbg_msg);
        }
    }

    public void requestInterupt(InterruptSource src) {
        if (src == InterruptSource.IT_VBLANK) {
            ioRegisters.IFReg.setVBlankEnable(true);
        } else if (src == InterruptSource.IT_LCD_STAT) {
            ioRegisters.IFReg.setLCDEnable(true);
        } else if (src == InterruptSource.IT_TIMER) {
            ioRegisters.IFReg.setTimerEnable(true);
        } else if (src == InterruptSource.IT_SERIAL) {
            ioRegisters.IFReg.setSerialEnable(true);
        } else if (src == InterruptSource.IT_JOYPAD) {
            ioRegisters.IFReg.setJoyPadEnable(true);
        }
    }

    public void tick(int cycles) {
        for (int i = 0; i < cycles; i++) {
            for (int j = 0; j < 4; j++) {
                ticks++;
                ioRegisters.timer.tick();
            }
        }
    }

    public boolean emuStep() {
        if (!silent)
            if (gameboyDoctor && !GBDoctorskipInstr) {
                System.out.printf("A:%02X F:%02X B:%02X C:%02X D:%02X E:%02X H:%02X L:%02X SP:%04X PC:%04X PCMEM:%02X,%02X,%02X,%02X%n",
                        cpu.getRegA(), cpu.getFlagReg().getByte(), cpu.getRegB(), cpu.getRegC(), cpu.getRegD(), cpu.getRegE(), cpu.getRegH(), cpu.getRegL(), (short) cpu.getRegSP(), (short) cpu.getRegPC(), bus_read(cpu.getRegPC()), bus_read((char) (cpu.getRegPC() + 1)), bus_read((char) (cpu.getRegPC() + 2)), bus_read((char) (cpu.getRegPC() + 3)));
            }

        if (cpu.isIME()) {
            if ((IEReg.getByte() & ioRegisters.IFReg.getByte()) != 0) {
                if (ioRegisters.IFReg.isVBlankEnable() && IEReg.isVBlankEnable()) {
                    ioRegisters.IFReg.setVBlankEnable(false);
                    cpu.setIME(false);
                    push(cpu.getRegPC());
                    cpu.setRegPC((char) 0x40);
                    tick(5);
                } else if (ioRegisters.IFReg.isLCDEnable() && IEReg.isLCDEnable()) {
                    ioRegisters.IFReg.setLCDEnable(false);
                    cpu.setIME(false);
                    push(cpu.getRegPC());
                    cpu.setRegPC((char) 0x48);
                    tick(5);
                } else if (ioRegisters.IFReg.isTimerEnable() && IEReg.isTimerEnable()) {
                    ioRegisters.IFReg.setTimerEnable(false);
                    cpu.setIME(false);
                    push(cpu.getRegPC());
                    cpu.setRegPC((char) 0x50);
                    tick(5);
                } else if (ioRegisters.IFReg.isSerialEnable() && IEReg.isSerialEnable()) {
                    cpu.setIME(false);
                    ioRegisters.IFReg.setSerialEnable(false);
                    push(cpu.getRegPC());
                    cpu.setRegPC((char) 0x58);
                    tick(5);
                } else if (ioRegisters.IFReg.isJoyPadEnable() && IEReg.isJoyPadEnable()) {
                    ioRegisters.IFReg.setJoyPadEnable(false);
                    cpu.setIME(false);
                    push(cpu.getRegPC());
                    cpu.setRegPC((char) 0x60);
                    tick(5);
                }
            }
            cpu.setQueuedIME(false);
        }
        if (cpu.isHalted()) {
            tick(1);
            GBDoctorskipInstr = true;
            if (ioRegisters.IFReg.getByte() != 0) {
                cpu.setHalted(false);
                GBDoctorskipInstr = false;
                return step();
            } else
                return true;
        } else
            return step();
    }

    public EmulationContext(InputStream ROMFile, boolean gameboyDoctor, boolean interactive, boolean silent) {
        try {
            cartridge = new Cartridge(ROMFile);
            var instructionStream = getClass().getResourceAsStream("/json/Opcodes.json");
            instructionSet = InstructionSet.fromFile(instructionStream);
            cpu = new CPU();
            this.gameboyDoctor = gameboyDoctor;
            this.silent = silent;
            cpu.setRegPC((char) 0x0100);
            bus_write((char) 0xFF44, (byte) 0x90);
            if (!silent) {
                if (!gameboyDoctor) {
                    System.out.println(cartridge.getHeader().humanReadable());
                    if (interactive) {
                        Scanner scanner = new Scanner(System.in);
                        scanner.nextLine();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}