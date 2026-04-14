package Model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Setter
@Getter
public class PPUStateMachine {
    public static int targetFrameTime = 1000 / 60;
    public static long prevFrameTime;
    public static long startTimer;
    public static long frameCount;
    private final Emulator context;
    private final Deque<Integer> bgPixelFifo = new ArrayDeque<>();
    private final Deque<Integer> bgColorIdFifo = new ArrayDeque<>();
    private final List<ScanlineSprite> lineSprites = new ArrayList<>(10);
    private FETCH_STATE currFetchState = FETCH_STATE.FS_TILE;
    private boolean wyConditionTriggered;
    private boolean windowStartedThisLine;
    private boolean fetchingWindow;
    private int windowLineCounter;
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
        bgColorIdFifo.clear();
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
        int mapY = fetchingWindow ? windowLineCounter : ((lcd.getLY() + lcd.getSCY()) & 0xFF);
        int mapX = fetchingWindow ? fetchX : ((fetchX + lcd.getSCX()) & 0xFF);
        int tileY = (mapY & 0x7) * 2;
        switch (currFetchState) {
            case FS_TILE -> {
                int tileMapBase = fetchingWindow ? lcd.getWindowTileMap() : lcd.getBGTileMapArea();
                int tileMapAddr = tileMapBase
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
        maybeStartWindow();
        if (bgPixelFifo.isEmpty() || bgColorIdFifo.isEmpty()) {
            return;
        }
        LCD lcd = context.getIoRegisters().getLcd();
        int bgPixelData = bgPixelFifo.removeFirst();
        int bgColorId = bgColorIdFifo.removeFirst();
        if (discardPixels > 0) {
            discardPixels--;
            return;
        }
        if (pushedX < PPU.XRES) {
            int pixelData = bgPixelData;
            if (lcd.getObjEnable()) {
                int objPixelData = resolveObjPixelColor(pushedX, lcd.getLY(), bgColorId);
                if (objPixelData >= 0) {
                    pixelData = objPixelData;
                }
            }
            context.getPpu().framebuffer[pushedX + (lcd.getLY() * PPU.XRES)] = pixelData;
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
        if (!bgPixelFifo.isEmpty() || !bgColorIdFifo.isEmpty()) {
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
            bgColorIdFifo.addLast(colorId);
            bgPixelFifo.addLast(color);
        }
        return true;
    }

    public void processOAM() {
        if (context.getPpu().getLineTicks() == 1) {
            evaluateWindowForLine();
            evaluateSpritesForLine();
        }
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
            bgColorIdFifo.clear();
            context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_HBLANK);
            if (context.getIoRegisters().getLcd().getHBlankInt()) context.getCpu().setLCDStatInt(true);
        }
    }

    private void evaluateSpritesForLine() {
        lineSprites.clear();
        LCD lcd = context.getIoRegisters().getLcd();
        if (!lcd.getObjEnable()) {
            return;
        }
        int ly = lcd.getLY();
        int objHeight = lcd.getObjSize();
        OAMEntry[] oam = context.getPpu().getOam();
        for (int i = 0; i < oam.length && lineSprites.size() < 10; i++) {
            OAMEntry entry = oam[i];
            int spriteTop = entry.getYPos() - 16;
            if (ly < spriteTop || ly >= (spriteTop + objHeight)) {
                continue;
            }
            lineSprites.add(new ScanlineSprite(entry, i));
        }
        lineSprites.sort((lhs, rhs) -> {
            int xCmp = Integer.compare(lhs.entry.getXPos(), rhs.entry.getXPos());
            if (xCmp != 0) {
                return xCmp;
            }
            return Integer.compare(lhs.oamIndex, rhs.oamIndex);
        });
    }

    private void evaluateWindowForLine() {
        LCD lcd = context.getIoRegisters().getLcd();
        int ly = lcd.getLY();
        if (ly == lcd.getWY()) {
            wyConditionTriggered = true;
        }
        windowStartedThisLine = false;
        fetchingWindow = false;
    }

    private void maybeStartWindow() {
        if (windowStartedThisLine || pushedX >= PPU.XRES || !wyConditionTriggered) {
            return;
        }
        LCD lcd = context.getIoRegisters().getLcd();
        if (!lcd.getWindowEnabled() || !lcd.getBGWindowDisplay()) {
            return;
        }
        int wx = lcd.getWX();
        if (wx > 166) {
            return;
        }
        if ((pushedX + 7) < wx) {
            return;
        }
        bgPixelFifo.clear();
        bgColorIdFifo.clear();
        currFetchState = FETCH_STATE.FS_TILE;
        fetchX = 0;
        tileIndex = 0;
        tileDataLo = 0;
        tileDataHi = 0;
        discardPixels = 0;
        fetchingWindow = true;
        windowStartedThisLine = true;
    }

    private int resolveObjPixelColor(int x, int ly, int bgColorId) {
        LCD lcd = context.getIoRegisters().getLcd();
        int objHeight = lcd.getObjSize();
        for (ScanlineSprite sprite : lineSprites) {
            OAMEntry entry = sprite.entry;
            int spriteLeft = entry.getXPos() - 8;
            if (x < spriteLeft || x >= (spriteLeft + 8)) {
                continue;
            }
            int spriteX = x - spriteLeft;
            int spriteY = ly - (entry.getYPos() - 16);
            if (spriteY < 0 || spriteY >= objHeight) {
                continue;
            }
            if (entry.isXFlipped()) {
                spriteX = 7 - spriteX;
            }
            if (entry.isYFlipped()) {
                spriteY = objHeight - 1 - spriteY;
            }
            int tileIndex = entry.getTileIndex();
            if (objHeight == 16) {
                tileIndex &= 0xFE;
                if (spriteY >= 8) {
                    tileIndex++;
                    spriteY -= 8;
                }
            }
            int tileAddr = 0x8000 + (tileIndex * 16) + (spriteY * 2);
            int tileDataLo = context.read(tileAddr);
            int tileDataHi = context.read(tileAddr + 1);
            int bit = 7 - spriteX;
            int lo = (tileDataLo >> bit) & 0x1;
            int hi = (tileDataHi >> bit) & 0x1;
            int colorId = (hi << 1) | lo;
            if (colorId == 0) {
                continue;
            }
            if (entry.isDrawnOver() && bgColorId != 0) {
                continue;
            }
            int[] objPalette = entry.getDmgPalette() == 0 ? lcd.getSp1Colors() : lcd.getSp2Colors();
            return objPalette[colorId];
        }
        return -1;
    }

    public void processVBLANK() {
        if (context.getPpu().getLineTicks() >= PPU.TICKS_PER_LINE) {
            context.getIoRegisters().incLY();
            if (context.getIoRegisters().getLcd().getLY() >= PPU.LINES_PER_FRAME) {
                context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_OAM);
                context.getIoRegisters().getLcd().setLY(0);
                context.getIoRegisters().updateLYCompare();
                wyConditionTriggered = false;
                windowLineCounter = 0;
            }
            context.getPpu().setLineTicks(0);
        }
    }

    public void processHBLANK() {

        if (context.getPpu().getLineTicks() >= PPU.TICKS_PER_LINE) {
            if (windowStartedThisLine) {
                windowLineCounter = (windowLineCounter + 1) & 0xFF;
            }
            context.getIoRegisters().incLY();
            if (context.getIoRegisters().getLcd().getLY() >= PPU.YRES) {
                context.getIoRegisters().getLcd().setPPUMode(RENDER_MODE.MODE_VBLANK);
                context.getCpu().setVBlankInt(true);
                if (context.getIoRegisters().getLcd().getVBlankInt()) context.getCpu().setLCDStatInt(true);
                context.getPpu().setCurrFrame(context.getPpu().getCurrFrame() + 1);
                context.getPpu().requestFrameRepaint();
                wyConditionTriggered = false;
                windowLineCounter = 0;
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

    private static class ScanlineSprite {
        private final OAMEntry entry;
        private final int oamIndex;

        private ScanlineSprite(OAMEntry entry, int oamIndex) {
            this.entry = entry;
            this.oamIndex = oamIndex;
        }
    }
}
