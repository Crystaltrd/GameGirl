package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IORegisters extends BusMemory {
    private Emulator context;
    private int[] serial_data = new int[2];
    private int[] data = new int[0xFF];

    IORegisters(Emulator context) {
        this.context = context;
    }

    @Override
    public int read(int address) {
        return switch (HardwareRegisters.findByValue(address)) {
            case null -> data[address];
            case P1JOYP -> 0;
            case SB -> serial_data[0];
            case SC -> serial_data[1];
            case DIV, TAC, TIMA, TMA -> context.getTimer().read(address);
            case IF -> context.getCpu().getIF() & 0x1F;
            case NR10 -> 0;
            case NR11 -> 0;
            case NR12 -> 0;
            case NR13 -> 0;
            case NR14 -> 0;
            case NR21 -> 0;
            case NR22 -> 0;
            case NR23 -> 0;
            case NR24 -> 0;
            case NR30 -> 0;
            case NR31 -> 0;
            case NR32 -> 0;
            case NR33 -> 0;
            case NR34 -> 0;
            case NR41 -> 0;
            case NR42 -> 0;
            case NR43 -> 0;
            case NR44 -> 0;
            case NR50 -> 0;
            case NR51 -> 0;
            case NR52 -> 0;
            case LCDC -> 0;
            case STAT -> 0;
            case SCY -> 0;
            case SCX -> 0;
            case LY -> 0x90;
            case LYC -> 0;
            case DMA -> 0;
            case BGP -> 0;
            case OBP0 -> 0;
            case OBP1 -> 0;
            case WY -> 0;
            case WX -> 0;
            case BANK -> 0;
        };
    }

    @Override
    public void write(int address, int value) {
        switch (HardwareRegisters.findByValue(address)) {
            case null -> data[address] = value;
            case P1JOYP -> {
                return;
            }
            case SB -> serial_data[0] = value;
            case SC -> serial_data[1] = value;
            case DIV, TAC, TIMA, TMA -> context.getTimer().write(address, value);
            case IF -> context.getCpu().setIF(value & 0x1F);

            case NR10 -> {
                return;
            }
            case NR11 -> {
                return;
            }
            case NR12 -> {
                return;
            }
            case NR13 -> {
                return;
            }
            case NR14 -> {
                return;
            }
            case NR21 -> {
                return;
            }
            case NR22 -> {
                return;
            }
            case NR23 -> {
                return;
            }
            case NR24 -> {
                return;
            }
            case NR30 -> {
                return;
            }
            case NR31 -> {
                return;
            }
            case NR32 -> {
                return;
            }
            case NR33 -> {
                return;
            }
            case NR34 -> {
                return;
            }
            case NR41 -> {
                return;
            }
            case NR42 -> {
                return;
            }
            case NR43 -> {
                return;
            }
            case NR44 -> {
                return;
            }
            case NR50 -> {
                return;
            }
            case NR51 -> {
                return;
            }
            case NR52 -> {
                return;
            }
            case LCDC -> {
                return;
            }
            case STAT -> {
                return;
            }
            case SCY -> {
                return;
            }
            case SCX -> {
                return;
            }
            case LY -> {
                return;
            }
            case LYC -> {
                return;
            }
            case DMA -> {
                return;
            }
            case BGP -> {
                return;
            }
            case OBP0 -> {
                return;
            }
            case OBP1 -> {
                return;
            }
            case WY -> {
                return;
            }
            case WX -> {
                return;
            }
            case BANK -> {
                return;
            }
        }
        ;
    }
}
