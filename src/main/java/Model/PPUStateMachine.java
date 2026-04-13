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
    private FETCH_STATE currFetchState = FETCH_STATE.FS_TILE;
    private int lineX;
    private int pushedX;
    private int fetchX;
    private int[] BGWFetchData = new int[3];
    private int[] FetchEntryData = new int[6];
    private int mapY;
    private int mapX;
    private int tileY;
    private int fifoX;

    PPUStateMachine(Emulator context) {
        this.context = context;
    }


    public void fetch() {
        switch (currFetchState) {
            case FS_TILE -> {
                if (context.getIoRegisters().getLcd().getBGWindowDisplay()) {
                    BGWFetchData[0] = context.read(context.getIoRegisters().getLcd().getBGTileMapArea() +
                            (mapX / 8) + ((mapY / 8) * 32));

                    if (context.getIoRegisters().getLcd().getBGWindowTileArea() == 0x8800) {
                        BGWFetchData[0] += 128;
                    }
                }
                currFetchState = FETCH_STATE.FS_DATA0;
                fetchX += 8;
            }
            case FS_DATA0 -> {
                BGWFetchData[1] = context.read(context.getIoRegisters().getLcd().getBGWindowTileArea() +
                        ((BGWFetchData[0] & 0xFF) * 16) + tileY);
                currFetchState = FETCH_STATE.FS_DATA1;
            }
            case FS_DATA1 -> {

                BGWFetchData[2] = context.read(context.getIoRegisters().getLcd().getBGWindowTileArea() +
                        ((BGWFetchData[0] & 0xFF) * 16) + tileY + 1);
                currFetchState = FETCH_STATE.FS_IDLE;
            }
            case FS_IDLE -> {
                currFetchState = FETCH_STATE.FS_PUSH;
            }
            case FS_PUSH -> {
                if (add())
                    currFetchState = FETCH_STATE.FS_TILE;

            }
        }
    }

    public void pushPixel() {
        if (pixelQueue.size() > 8) {
            int pixelData = pixelQueue.pop();
            if (lineX >= (context.getIoRegisters().getLcd().getSCX() % 8)) {
                context.getPpu().framebuffer[pushedX + (context.getIoRegisters().getLcd().getLY() * PPU.XRES)] = pixelData;
                pushedX++;
            }
            lineX++;
        }
    }

    public void process() {
        mapY = context.getIoRegisters().getLcd().getLY() + context.getIoRegisters().getLcd().getSCY();
        mapX = fetchX + context.getIoRegisters().getLcd().getSCX();
        tileY = (mapY % 8) * 2;
        if (context.getPpu().getLineTicks() % 2 == 0) {
            fetch();
        }
        pushPixel();
    }


    public boolean add() {
        if (pixelQueue.size() > 8)
            return false;
        int x = fetchX - (8 - (context.getIoRegisters().getLcd().getSCX() % 8));
        for (int i = 0; i < 8; i++) {
            int lo = ((BGWFetchData[2] >> i) & 1) << 1;
            int hi = ((BGWFetchData[1] >> i) & 1);
            int color = context.getIoRegisters().getLcd().getBgColors()[hi | lo];
            if (x >= 0) {
                pixelQueue.push(color);
                fifoX++;
            }
        }
        return true;
    }

    public void processOAM() {
        if (context.getPpu().getLineTicks() >= 80) {
            context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_XFER);
            currFetchState = FETCH_STATE.FS_TILE;
            lineX = 0;
            fetchX = 0;
            pushedX = 0;
            fifoX = 0;
        }
    }

    public void processXFER() {
        process();
        if (pushedX >= PPU.XRES) {
            pixelQueue.clear();
            context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_HBLANK);
            if (context.getIoRegisters().getLcd().getHBlankInt()) context.getCpu().setLCDStatInt(true);
        }
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
                if (context.getIoRegisters().getLcd().getVBlankInt()) context.getCpu().setLCDStatInt(true);
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
                    if (context.getProcess() == null) System.out.printf("FPS: %d%n", fps);
                }
                frameCount++;
                prevFrameTime = Commons.getTicks();
            } else context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_OAM);
            context.getPpu().setLineTicks(0);
        }
    }

    enum FETCH_STATE {
        FS_TILE, FS_DATA0, FS_DATA1, FS_IDLE, FS_PUSH
    }
}
