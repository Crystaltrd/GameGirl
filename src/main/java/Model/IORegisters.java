package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IORegisters extends BusMemory {
    private Emulator context;
    private int[] serial_data = new int[2];
    private int[] data = new int[0xFF];
    private int joyp = 0xCF;
    private DMA dma;
    private LCD lcd;

    IORegisters(Emulator context) {
        dma = new DMA(context);
        lcd = new LCD(context);
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
        return switch (HardwareRegisters.findByValue(address)) {
            case null -> data[address];
            case P1JOYP -> joyp;
            case SB -> serial_data[0];
            case SC -> serial_data[1];
            case DIV, TAC, TIMA, TMA -> context.getTimer().read(address);
            case IF -> context.getCpu().getIF() & 0x1F;
            case NR10 -> 0;
            case NR11 -> 0;
            case NR12 -> 0;
            case NR13 -> 0;
            case NR14 -> 0;
            case NR21 -> 0;
            case NR22 -> 0;
            case NR23 -> 0;
            case NR24 -> 0;
            case NR30 -> 0;
            case NR31 -> 0;
            case NR32 -> 0;
            case NR33 -> 0;
            case NR34 -> 0;
            case NR41 -> 0;
            case NR42 -> 0;
            case NR43 -> 0;
            case NR44 -> 0;
            case NR50 -> 0;
            case NR51 -> 0;
            case NR52 -> 0;
            case WY -> lcd.getWY();
            case WX -> lcd.getWX();
            case SCY -> lcd.getSCY();
            case SCX -> lcd.getSCX();
            case LY -> context.getProcess() == null ? 0x90 : lcd.getLY();
            case LYC -> lcd.getLYC();
            case DMA -> dma.getVal();
            case LCDC -> lcd.getLCDC();
            case STAT -> lcd.getSTAT();
            case OBP0 -> lcd.getObjPalette()[0];
            case OBP1 -> lcd.getObjPalette()[1];
            case BGP -> lcd.getBGPalette();
            case BANK -> 0;
        };
    }

    @Override
    public void write(int address, int value) {
        switch (HardwareRegisters.findByValue(address)) {
            case null -> data[address] = value;
            case P1JOYP -> joyp = 0xC0 | (value & 0x30) | 0x0F;
            case SB -> serial_data[0] = value;
            case SC -> serial_data[1] = value;
            case DIV, TAC, TIMA, TMA -> context.getTimer().write(address, value);
            case IF -> context.getCpu().setIF(value & 0x1F);
            case NR10 -> {
                return;
            }
            case NR11 -> {
                return;
            }
            case NR12 -> {
                return;
            }
            case NR13 -> {
                return;
            }
            case NR14 -> {
                return;
            }
            case NR21 -> {
                return;
            }
            case NR22 -> {
                return;
            }
            case NR23 -> {
                return;
            }
            case NR24 -> {
                return;
            }
            case NR30 -> {
                return;
            }
            case NR31 -> {
                return;
            }
            case NR32 -> {
                return;
            }
            case NR33 -> {
                return;
            }
            case NR34 -> {
                return;
            }
            case NR41 -> {
                return;
            }
            case NR42 -> {
                return;
            }
            case NR43 -> {
                return;
            }
            case NR44 -> {
                return;
            }
            case NR50 -> {
                return;
            }
            case NR51 -> {
                return;
            }
            case NR52 -> {
                return;
            }
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
            case BANK -> {
                return;
            }
        }
        ;
    }
}
