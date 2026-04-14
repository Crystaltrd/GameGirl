package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DMA {
    private Emulator context;
    private boolean active = false;
    private int addr;
    private int val;
    private int start_delay;

    DMA(Emulator context) {
        this.context = context;
    }

    public void start(int start) {
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
        context.getPpu().write_oam(addr, context.read(((val & 0xFF) * 0x100) + addr));
        addr++;
        active = (addr & 0xFF) < 0xA0;
    }
}
