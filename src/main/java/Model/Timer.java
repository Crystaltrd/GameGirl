package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Timer extends BusMemory {
    private Emulator context;
    private int divReg = 0xAC00;
    private int timaReg;
    private int tmaReg;
    private int tacReg = 0xF8;

    Timer(Emulator context) {
        this.context = context;
    }

    public void tick() {
        int prev_div = divReg;
        divReg++;

        boolean timer_update = switch (tacReg & 0b11) {
            case 0 -> ((prev_div & (1 << 9)) != 0) && ((divReg & (1 << 9)) == 0);
            case 1 -> ((prev_div & (1 << 3)) != 0) && ((divReg & (1 << 3)) == 0);
            case 2 -> ((prev_div & (1 << 5)) != 0) && ((divReg & (1 << 5)) == 0);
            case 3 -> ((prev_div & (1 << 7)) != 0) && ((divReg & (1 << 7)) == 0);
            default -> false;
        };
        if (timer_update && (tacReg & (1 << 2)) != 0) {
            timaReg++;
            if (timaReg == 0xFF) {
                timaReg = tmaReg & 0xFF;
                context.getCpu().setTimerInt(true);
            }
        }
    }

    @Override
    public int read(int address) {
        return switch (HardwareRegisters.findByValue(address)) {
            case DIV -> (divReg >> 8) & 0xFF;
            case TIMA -> timaReg;
            case TMA -> tmaReg;
            case TAC -> tacReg;
            case null, default -> 0;
        };
    }

    @Override
    public void write(int address, int value) {
        switch (HardwareRegisters.findByValue(address)) {
            case DIV -> divReg = 0;
            case TIMA -> timaReg = value;
            case TMA -> tmaReg = value;
            case TAC -> tacReg = value;
            case null, default -> {
                return;
            }
        }
        ;
    }
}
