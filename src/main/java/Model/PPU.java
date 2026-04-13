package Model;

import Vue.GameView;
import Vue.TileView;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PPU extends BusMemory {
    public static final int LINES_PER_FRAME = 154;
    public static final int TICKS_PER_LINE = 456;
    public static final int YRES = 144;
    public static final int XRES = 160;

    private Emulator context;
    private byte[] VRAM = new byte[0xA000 - 0x8000];
    private OAMEntry[] oam = new OAMEntry[40];
    private int currFrame;
    private int lineTicks;
    public int[] framebuffer = new int[YRES * XRES * 2];
    private PPUStateMachine stateMachine;
    
    PPU(Emulator context) {
        this.context = context;
        stateMachine = new PPUStateMachine(context);
        for (int i = 0; i < oam.length; i++) {
            oam[i] = new OAMEntry();
        }
    }

    public void tick() {
        lineTicks++;
        TileView tileDisplay = context.getTileMapRenderer();
        GameView gameDisplay = context.getGameRenderer();
        if (tileDisplay != null) {
            tileDisplay.repaint();
        }
        if (gameDisplay != null) {
            gameDisplay.repaint();
        }
        switch (context.getIoRegisters().getLcd().getPPUMode()) {
            case MODE_HBLANK -> {
                stateMachine.processHBLANK();
            }
            case MODE_VBLANK -> {
                stateMachine.processVBLANK();
            }
            case MODE_OAM -> {
                stateMachine.processOAM();
            }
            case MODE_XFER -> {
                stateMachine.processXFER();
            }
        }
    }

    @Override
    public int read(int address) {
        if (address >= 0x8000)
            address -= 0x8000;
        return VRAM[address] & 0xFF;
    }

    @Override
    public void write(int address, int value) {
        if (address >= 0x8000)
            address -= 0x8000;
        VRAM[address] = (byte) (value & 0xFF);

    }

    public int read_oam(int address) {
        if (address >= 0xFE00)
            address -= 0xFE00;
        return oam[(address) / 4].getByte((address) % 4) & 0xFF;
    }

    public void write_oam(int address, int value) {
        if (address >= 0xFE00)
            address -= 0xFE00;
        oam[(address) / 4].setByte((address) % 4, value);
    }
}
