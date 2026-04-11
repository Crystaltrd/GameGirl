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

    private int frequency;
    private int lengthCounter;
    private boolean Enable;
    private short dutyStep;
    private int currVolume;
    private short envelopeTimer;


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
        this.frequency = (2048-periodValue)*4;
        this.dutyStep = 0;
        this.currVolume = initialVolume;
        this.envelopeTimer = this.envelopPeriod;
    }

    public void tick(int cycles){
        if(Enable) {
            frequency -= cycles;
            if(frequency <= 0){
                step();
                frequency += (2048-periodValue)*4;

            }
        }
    }
    public void step(){

    }
    public void write(char addr, int val){

    }

//    public boolean isEnabled(){
//        return (HardwareRegisters.NR52.addr & dacBitIndex) != 0;
//    }
//    public boolean isTriggered(){
//        return (HardwareRegisters.NR24.addr & 0x80) != 0;
//    }


}
