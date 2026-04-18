package Model;

public enum HardwareRegisters {
    P1JOYP(0x00),//Joypad		
    SB(0x01),//Serial transfer data		
    SC(0x02),//Serial transfer control		
    DIV(0x04),//Divider register		
    TIMA(0x05),//Model.Timer counter		
    TMA(0x06),//Model.Timer modulo		
    TAC(0x07),//Model.Timer control		
    IF(0x0F),//Interrupt flag		
    NR10(0x10),//Sound channel 1 sweep		
    NR11(0x11),//Sound channel 1 length timer & duty cycle		
    NR12(0x12),//Sound channel 1 volume & envelope		
    NR13(0x13),//Sound channel 1 period low	
    NR14(0x14),//Sound channel 1 period high & control		
    NR21(0x16),//Sound channel 2 length timer & duty cycle		
    NR22(0x17),//Sound channel 2 volume & envelope		
    NR23(0x18),//Sound channel 2 period low	
    NR24(0x19),//Sound channel 2 period high & control		
    NR30(0x1A),//Sound channel 3 DAC enable		
    NR31(0x1B),//Sound channel 3 length timer	
    NR32(0x1C),//Sound channel 3 output level		
    NR33(0x1D),//Sound channel 3 period low	
    NR34(0x1E),//Sound channel 3 period high & control		
    NR41(0x20),//Sound channel 4 length timer	
    NR42(0x21),//Sound channel 4 volume & envelope		
    NR43(0x22),//Sound channel 4 frequency & randomness		
    NR44(0x23),//Sound channel 4 control		
    NR50(0x24),//Master volume & VIN panning		
    NR51(0x25),//Sound panning		
    NR52(0x26),//Sound onoff	
    //0x30-0x3F:  Wave RAM Storage for one of the sound channels’ waveform		
    LCDC(0x40),//LCD control		
    STAT(0x41),//LCD status		
    SCY(0x42),//Viewport Y position		
    SCX(0x43),//Viewport X position		
    LY(0x44),//LCD Y coordinate		
    LYC(0x45),//LY compare		
    DMA(0x46),//OAM DMA source address & start		
    BGP(0x47),//BG palette data		
    OBP0(0x48),//OBJ palette 0 data	
    OBP1(0x49),//OBJ palette 1 data	
    WY(0x4A),//Window Y position		
    WX(0x4B),//Window X position plus 7		
    BANK(0x50),//Boot ROM mapping control	
    ;
    public final int addr;

    HardwareRegisters(int addr) {
        this.addr = addr;
    }

    public static HardwareRegisters findByValue(int addr) {
        for (HardwareRegisters reg : values()) {
            if (reg.addr == addr)
                return reg;
        }
        return null;
    }
}

