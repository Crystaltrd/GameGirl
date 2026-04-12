package Model;

public class PulseChannels extends Channel {
    private HardwareRegisters[] channelRegisters;
    //a initialiser dans write();
    private int periodValue;
    private int initialVolume;
    private int lengthEnabled;
    private short enveloppeDir;
    private int initLengthTimer;
    private short envelopPeriod;
    private short waveDuty;
    private boolean isDacEnable;
    private double sampleRate;

    private int currfrequency;
    private int lengthCounter;
    private boolean Enable;
    private short dutyStep;
    private int currVolume;
    private short envelopeCounter;



    private int lowPeriod;
    private int[][] dutyCycles = {
            {0, 0, 0, 0, 0, 0, 0, 1},
            {0, 0, 0, 0, 0, 0, 1, 1},
            {1, 0, 0, 0, 0, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 0}
    };



    public PulseChannels(HardwareRegisters... reg){
        this.channelRegisters = reg.clone();
    }
    public boolean isEnabled(){ return this.Enable;    }
    public boolean isDacEnabled(){return (channelRegisters[1].addr & 0xF8) != 0;}

    public void trigger(){
        this.Enable = true;
        this.lengthCounter = (this.lengthCounter == 0 ) ? 64 : this.lengthCounter;
        this.currfrequency = (2048-periodValue)*4;
        this.dutyStep = 0;
        this.envelopeCounter = this.envelopPeriod;
        this.currVolume = initialVolume;
    }

    public void tick(int cycles){
        if(Enable) {
            currfrequency -= cycles;
            if(currfrequency <= 0){
                step();
                currfrequency += (2048-periodValue)*4;

            }
        }
    }
    public void step(){

    }
    public void write(char addr, byte val){
        if(addr == (char) HardwareRegisters.NR10.addr){

        }
        else if((addr == (char) HardwareRegisters.NR11.addr) || addr == (char) HardwareRegisters.NR21.addr){
            this.waveDuty = (short) ((val >> 6) & 0x03);
            this.initLengthTimer = val & 0x3F;
            this.lengthCounter = 64 - (val & 0x3F);
        }
        else if((addr == (char) HardwareRegisters.NR12.addr) || addr == (char) HardwareRegisters.NR22.addr){
            this.initialVolume = (val >> 4) & 0x0F;
            this.currVolume = this.initialVolume;
            this.enveloppeDir = (short) ((val >> 5 ) & 0x01);
            this.envelopPeriod = (short) ((val & 0x07)*64); // 6 7
            if(this.initialVolume == 0 && this.enveloppeDir == 0){ isDacEnable = false;}
        }
        else if((addr == (char) HardwareRegisters.NR13.addr) || addr == (char) HardwareRegisters.NR23.addr){
            this.periodValue = ((val & 0xFF) << 3) | (lowPeriod & 0xFF);
            this.sampleRate = (double) (1_048_576) /(2048-this.periodValue);
        }
        else if((addr == (char) HardwareRegisters.NR14.addr) || addr == (char) HardwareRegisters.NR24.addr){
            this.lowPeriod = (val & 0x07);
            if((this.lengthEnabled = (val >> 6) & 0x01) == 1){this.lengthCounter--;}
            if(((val >> 7 ) & 0x01) == 1){trigger();}
        }

        System.err.println("Wrong component");
        System.exit(-1);



    }

//    public boolean isEnabled(){
//        return (HardwareRegisters.NR52.addr & dacBitIndex) != 0;
//    }
//    public boolean isTriggered(){
//        return (HardwareRegisters.NR24.addr & 0x80) != 0;
//    }


}
