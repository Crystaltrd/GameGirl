package Model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.Deque;

@Setter
@Getter
public class PPUStateMachine {
    public static int targetFrameTime = 1000 / 60;
    public static long prevFrameTime;
    public static long startTimer;
    public static long frameCount;
    private final Emulator context;
    private final Deque<Integer> bgPixelFifo = new ArrayDeque<>();
    private FETCH_STATE currFetchState = FETCH_STATE.FS_TILE;
    private int pushedX;
    private int fetchX;
    private int discardPixels;
    private int tileIndex;
    private int tileDataLo;
    private int tileDataHi;

    PPUStateMachine(Emulator context) {
        this.context = context;
    }

    public void resetFetcher() {
        bgPixelFifo.clear();
        currFetchState = FETCH_STATE.FS_TILE;
        pushedX = 0;
        fetchX = 0;
        discardPixels = 0;
        tileIndex = 0;
        tileDataLo = 0;
        tileDataHi = 0;
    }


    public void fetch() {
        LCD lcd = context.getIoRegisters().getLcd();
        int mapY = (lcd.getLY() + lcd.getSCY()) & 0xFF;
        int mapX = (fetchX + lcd.getSCX()) & 0xFF;
        int tileY = (mapY & 0x7) * 2;
        switch (currFetchState) {
            case FS_TILE -> {
                int tileMapAddr = lcd.getBGTileMapArea()
                        + ((mapX >> 3) & 0x1F)
                        + (((mapY >> 3) & 0x1F) * 32);
                tileIndex = context.read(tileMapAddr) & 0xFF;
                currFetchState = FETCH_STATE.FS_DATA0;
            }
            case FS_DATA0 -> {
                tileDataLo = context.read(resolveTileDataAddress(lcd, tileIndex, tileY));
                currFetchState = FETCH_STATE.FS_DATA1;
            }
            case FS_DATA1 -> {
                tileDataHi = context.read(resolveTileDataAddress(lcd, tileIndex, tileY) + 1);
                currFetchState = FETCH_STATE.FS_IDLE;
            }
            case FS_IDLE -> {
                currFetchState = FETCH_STATE.FS_PUSH;
            }
            case FS_PUSH -> {
                if (pushToFifo()) {
                    fetchX += 8;
                    currFetchState = FETCH_STATE.FS_TILE;
                }
            }
        }
    }

    public void pushPixel() {
        if (bgPixelFifo.isEmpty()) {
            return;
        }
        int pixelData = bgPixelFifo.removeFirst();
        if (discardPixels > 0) {
            discardPixels--;
            return;
        }
        if (pushedX < PPU.XRES) {
            context.getPpu().framebuffer[pushedX + (context.getIoRegisters().getLcd().getLY() * PPU.XRES)] = pixelData;
            pushedX++;
        }
    }

    public void process() {
        if (context.getPpu().getLineTicks() % 2 == 0) {
            fetch();
        }
        pushPixel();
    }


    private int resolveTileDataAddress(LCD lcd, int tileId, int tileY) {
        if (lcd.getBGWindowTileArea() == 0x8000) {
            return 0x8000 + (tileId * 16) + tileY;
        }
        int signedTileId = (byte) tileId;
        return 0x9000 + (signedTileId * 16) + tileY;
    }

    public boolean pushToFifo() {
        if (!bgPixelFifo.isEmpty()) {
            return false;
        }
        LCD lcd = context.getIoRegisters().getLcd();
        boolean bgEnabled = lcd.getBGWindowDisplay();
        for (int bit = 7; bit >= 0; bit--) {
            int colorId = 0;
            if (bgEnabled) {
                int lo = (tileDataLo >> bit) & 0x1;
                int hi = (tileDataHi >> bit) & 0x1;
                colorId = (hi << 1) | lo;
            }
            int color = lcd.getBgColors()[colorId];
            bgPixelFifo.addLast(color);
        }
        return true;
    }

    public void processOAM() {
        if (context.getPpu().getLineTicks() >= 80) {
            context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_XFER);
            resetFetcher();
            discardPixels = context.getIoRegisters().getLcd().getSCX() & 0x7;
        }
    }

    public void processXFER() {
        process();
        if (pushedX >= PPU.XRES) {
            bgPixelFifo.clear();
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
                context.getIoRegisters().updateLYCompare();
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
                    if (context.getProcess() == null) context.setFPS(fps);
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
