package Model;

import lombok.Getter;

@Getter
public class Timer extends GBMemory {
    private final EmulationContext ctx;
    private long div = 0xAC00;
    private char tima;
    private byte tma;
    private byte tac = (byte) 0xF8;

    Timer(EmulationContext ctx) {
        this.ctx = ctx;
    }

    public byte read(char addr) {
        if (addr == HardwareRegisters.DIV.addr)
            return (byte) (div >> 8);
        else if (addr == HardwareRegisters.TIMA.addr)
            return (byte) (tima & 0xFF);
        else if (addr == HardwareRegisters.TMA.addr)
            return tma;
        else if (addr == HardwareRegisters.TAC.addr)
            return tac;
        return 0;
    }

    public void write(char addr, byte val) {
        if (addr == HardwareRegisters.DIV.addr)
            div = 0;
        else if (addr == HardwareRegisters.TIMA.addr)
            tima = (char) val;
        else if (addr == HardwareRegisters.TMA.addr)
            tma = val;
        else if (addr == HardwareRegisters.TAC.addr)
            tac = val;
    }

    public void tick() {
        long prev_div = div;

        div++;

        boolean timer_update = switch (tac & (0b11)) {
            case 0b00 -> ((prev_div & (1 << 9)) != 0) && ((div & (1 << 9)) == 0);
            case 0b01 -> ((prev_div & (1 << 3)) != 0) && ((div & (1 << 3)) == 0);
            case 0b10 -> ((prev_div & (1 << 5)) != 0) && ((div & (1 << 5)) == 0);
            case 0b11 -> ((prev_div & (1 << 7)) != 0) && ((div & (1 << 7)) == 0);
            default -> false;
        };

        if (timer_update && (tac & (1 << 2)) != 0) {
            tima++;

            if (tima > 0xFF) {
                tima = (char) (tma & 0xFF);
                ctx.requestInterrupt(InterruptSource.IT_TIMER);
            }
        }
    }
}
