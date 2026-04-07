package Model;

public enum HardwareRegisters {
    P1JOYP((byte) 0x00),//Joypad		
    SB((byte) 0x01),//Serial transfer data		
    SC((byte) 0x02),//Serial transfer control		
    DIV((byte) 0x04),//Divider register		
    TIMA((byte) 0x05),//Model.Timer counter		
    TMA((byte) 0x06),//Model.Timer modulo		
    TAC((byte) 0x07),//Model.Timer control		
    IF((byte) 0x0F),//Interrupt flag		
    NR10((byte) 0x10),//Sound channel 1 sweep		
    NR11((byte) 0x11),//Sound channel 1 length timer & duty cycle		
    NR12((byte) 0x12),//Sound channel 1 volume & envelope		
    NR13((byte) 0x13),//Sound channel 1 period low	
    NR14((byte) 0x14),//Sound channel 1 period high & control		
    NR21((byte) 0x16),//Sound channel 2 length timer & duty cycle		
    NR22((byte) 0x17),//Sound channel 2 volume & envelope		
    NR23((byte) 0x18),//Sound channel 2 period low	
    NR24((byte) 0x19),//Sound channel 2 period high & control		
    NR30((byte) 0x1A),//Sound channel 3 DAC enable		
    NR31((byte) 0x1B),//Sound channel 3 length timer	
    NR32((byte) 0x1C),//Sound channel 3 output level		
    NR33((byte) 0x1D),//Sound channel 3 period low	
    NR34((byte) 0x1E),//Sound channel 3 period high & control		
    NR41((byte) 0x20),//Sound channel 4 length timer	
    NR42((byte) 0x21),//Sound channel 4 volume & envelope		
    NR43((byte) 0x22),//Sound channel 4 frequency & randomness		
    NR44((byte) 0x23),//Sound channel 4 control		
    NR50((byte) 0x24),//Master volume & VIN panning		
    NR51((byte) 0x25),//Sound panning		
    NR52((byte) 0x26),//Sound onoff		
    //0x30-0x3F:  Wave RAM Storage for one of the sound channels’ waveform		
    LCDC((byte) 0x40),//LCD control		
    STAT((byte) 0x41),//LCD status		
    SCY((byte) 0x42),//Viewport Y position		
    SCX((byte) 0x43),//Viewport X position		
    LY((byte) 0x44),//LCD Y coordinate		
    LYC((byte) 0x45),//LY compare		
    DMA((byte) 0x46),//OAM DMA source address & start		
    BGP((byte) 0x47),//BG palette data		
    OBP0((byte) 0x48),//OBJ palette 0 data	
    OBP1((byte) 0x49),//OBJ palette 1 data	
    WY((byte) 0x4A),//Window Y position		
    WX((byte) 0x4B),//Window X position plus 7		
    BANK((byte) 0x50),//Boot ROM mapping control	
    ;
    public final byte addr;
    public static HardwareRegisters findByValue(byte addr){
        for(HardwareRegisters reg: values()){
            if(reg.addr == addr)
                return reg;
        }
        return null;
    }
    HardwareRegisters(byte addr) {
        this.addr = addr;
    }
}
