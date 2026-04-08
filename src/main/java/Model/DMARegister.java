package Model;

import lombok.Getter;
import lombok.Setter;

import java.util.Scanner;

@Getter
@Setter
public class DMARegister {
    private boolean active = false;
    private byte addr;
    private byte val;
    private byte start_delay;
    private EmulationContext ctx;

    public DMARegister(EmulationContext ctx) {
        this.ctx = ctx;
    }

    public void start(byte start) {
        active = true;
        addr = 0;
        start_delay = 2;
        val = start;
    }

    public void tick() {
        if (!active)
            return;
        if (start_delay != 0) {
            start_delay--;
            return;
        }
        ctx.ppu.write((char) (0xFE00 + addr), ctx.bus_read((char) ((char) (val * 0x100) + addr)));
        addr++;
        active = ((char) addr & 0xFF) < ((char) 0xA0 & 0xFF);

    }
}
