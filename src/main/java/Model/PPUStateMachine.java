package Model;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;

@Setter
@Getter
public class PPUStateMachine {
    public static int targetFrameTime = 1000 / 60;
    public static long prevFrameTime;
    public static long startTimer;
    public static long frameCount;
    private final Emulator context;
    private ArrayDeque<Integer> pixelQueue = new ArrayDeque<>();
    private int lineX;
    private int pushedX;
    private int fetchX;
    private int[] BGWFetchData = new int[3];

    PPUStateMachine(Emulator context) {
        this.context = context;
    }

    public void processOAM() {
        if (context.getPpu().getLineTicks() >= 80)
            context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_XFER);
    }

    public void processXFER() {
        if (context.getPpu().getLineTicks() >= 80 + 172)
            context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_HBLANK);
    }

    public void processVBLANK() {
        if (context.getPpu().getLineTicks() >= PPU.TICKS_PER_LINE) {
            context.getIoRegisters().incLY();
            if (context.getIoRegisters().getLcd().getLY() >= PPU.LINES_PER_FRAME) {
                context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_OAM);
                context.getIoRegisters().getLcd().setLY(0);
            }
            context.getPpu().setLineTicks(0);
        }
    }

    public void processHBLANK() {

        if (context.getPpu().getLineTicks() >= PPU.TICKS_PER_LINE) {
            context.getIoRegisters().incLY();
            if (context.getIoRegisters().getLcd().getLY() >= PPU.YRES) {
                context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_VBLANK);
                context.getCpu().setVBlankInt(true);
                if (context.getIoRegisters().getLcd().getVBlankInt())
                    context.getCpu().setLCDStatInt(true);
                context.getPpu().setCurrFrame(context.getPpu().getCurrFrame() + 1);
                long end = Commons.getTicks();
                long frameTime = end - prevFrameTime;
                if (frameTime < targetFrameTime) {
                    try {
                        Thread.sleep(targetFrameTime - frameTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (end - startTimer >= 1000) {
                    long fps = frameCount;
                    startTimer = end;
                    frameCount = 0;
                    if (context.getProcess() == null)
                        System.out.printf("FPS: %d%n", fps);
                }
                frameCount++;
                prevFrameTime = Commons.getTicks();
            } else
                context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_OAM);
            context.getPpu().setLineTicks(0);
        }
    }

    enum FETCH_STATE {
        FS_TILE,
        FS_DATA0,
        FS_DATA1,
        FS_IDLE,
        FS_PUSH
    }
}
