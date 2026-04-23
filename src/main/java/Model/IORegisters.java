package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IORegisters extends BusMemory {
    private Emulator context;
    private Joypad joyp;
    private int[] serial_data = new int[2];
    private int[] data = new int[0xFF];
    private DMA dma;
    private LCD lcd;

    IORegisters(Emulator context) {
        dma = new DMA(context);
        lcd = new LCD(context);
        joyp = new Joypad(context);
        this.context = context;
    }

    public void updateLYCompare() {
        if (lcd.getLY() == lcd.getLYC()) {
            lcd.setLYCEqLY(true);
            if (lcd.getLYCInt()) {
                context.getCpu().setLCDStatInt(true);
            }
        } else {
            lcd.setLYCEqLY(false);
        }
    }

    public void incLY() {
        lcd.setLY(lcd.getLY() + 1);
        updateLYCompare();
    }

    @Override
    public int read(int address) {
        if (Commons.isBetween(address, 0x10, 0x26) || Commons.isBetween(address, 0x30, 0x3F)) {
            return context.getApu().read(address);
        }
        return switch (HardwareRegisters.findByValue(address)) {
            case P1JOYP -> joyp.getByte();
            case SB -> serial_data[0];
            case SC -> serial_data[1];
            case DIV, TAC, TIMA, TMA -> context.getTimer().read(address);
            case IF -> context.getCpu().getIF() & 0x1F;
            case WY -> lcd.getWY();
            case WX -> lcd.getWX();
            case SCY -> lcd.getSCY();
            case SCX -> lcd.getSCX();
            case LY -> context.getProcess() == null ? lcd.getLY() : 0x90;
            case LYC -> lcd.getLYC();
            case DMA -> dma.getVal();
            case LCDC -> lcd.getLCDC();
            case STAT -> lcd.getSTAT();
            case OBP0 -> lcd.getObjPalette()[0];
            case OBP1 -> lcd.getObjPalette()[1];
            case BGP -> lcd.getBGPalette();
            case null, default -> data[address];
        };
    }

    @Override
    public void write(int address, int value) {
        if (Commons.isBetween(address, 0x10, 0x26) || Commons.isBetween(address, 0x30, 0x3F)) {
            context.getApu().write(address, value);
            return;
        }
        switch (HardwareRegisters.findByValue(address)) {
            case P1JOYP -> joyp.setByte(value);
            case SB -> serial_data[0] = value;
            case SC -> {
                serial_data[1] = value;
                if (context.getDebugWindow() != null) {
                    context.dbg_update();
                    context.getDebugWindow().setText(String.valueOf(context.dbg_msg));
                }
            }
            case DIV, TAC, TIMA, TMA -> context.getTimer().write(address, value);
            case IF -> context.getCpu().setIF(value & 0x1F);
            case SCY -> lcd.setSCY(value & 0xFF);
            case SCX -> lcd.setSCX(value & 0xFF);
            case LCDC -> {
                boolean wasEnabled = lcd.getLCDPPUEnabled();
                lcd.setLCDC(value & 0xFF);
                boolean isEnabled = lcd.getLCDPPUEnabled();
                if (wasEnabled && !isEnabled) {
                    context.getPpu().disableLCD();
                } else if (!wasEnabled && isEnabled) {
                    context.getPpu().enableLCD();
                }
            }
            case STAT -> lcd.writeSTAT(value & 0xFF);
            case LY -> {
                lcd.setLY(0);
                updateLYCompare();
            }
            case LYC -> {
                lcd.setLYC(value & 0xFF);
                updateLYCompare();
            }
            case DMA -> dma.start(value & 0xFFFF);
            case BGP -> lcd.updatePalette(value & 0xFFFF, 0);
            case OBP0 -> lcd.updatePalette(value & 0xFFFF, 1);
            case OBP1 -> lcd.updatePalette(value & 0xFFFF, 2);
            case WY -> lcd.setWY(value & 0xFF);
            case WX -> lcd.setWX(value & 0xFF);
            case null, default -> data[address] = value;

        }
        ;
    }
}
