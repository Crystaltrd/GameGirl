package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PPUStateMachine {
    public static int DOTS_PER_LINE = 456;
    public static int YRES = 144;
    public static int XRES = 160;
    public static int VBLANK_LINES = 10;
    private EmulationContext ctx;
    private PPUStates currState = PPUStates.STATE_OAM;
    private int lineTicks = 0;
    PPUStateMachine(EmulationContext ctx) {
        this.ctx = ctx;
    }

    public void ppuModeOAM() {
        if (lineTicks >= 80) {
            currState = PPUStates.STATE_XFER;
        }
        if (lineTicks == 1) {
        }
    }

    public void ppuModeXFER() {
        if (lineTicks >= 172 + 80) {
            currState = PPUStates.STATE_HBLANK;
        }
    }

    public void ppuModeHBlank() {
        if (lineTicks >= DOTS_PER_LINE + (172 + 80)) {

            ctx.ioRegisters.LY++;
            currState = PPUStates.STATE_OAM;
            if (((int) ctx.ioRegisters.LY & 0xFF) >= YRES) {
                currState = PPUStates.STATE_VBLANK;
            }
            lineTicks = 0;
        }
    }

    enum PPUStates {
        STATE_OAM,
        STATE_XFER,
        STATE_HBLANK,
        STATE_VBLANK;
    }
}
