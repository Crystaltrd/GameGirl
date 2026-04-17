package Model;

public class PulseChannels extends Channel {
    private final HardwareRegisters[] channelRegisters;
    //global activation values?
    private boolean isDacEnable;
    private boolean Enable;

    //length related (life period of the channel kinda)
    private int lengthEnabled;
    private int initLengthTimer;
    private int lengthCounter;

    //volume only variables
    private int initialVolume;
    private int currVolume;

    //envelope related variables
    private short enveloppeDir;
    private short envelopSweepPace;
    private short envelopeCounter;
    private boolean envelopeEnabled;

    //i don't know yet variables
    private int periodValue;
    private int currfrequency;

    //sweep
    private int sweepPace;
    private int sweePaceCounter;
    private short sweepDirection;
    private int individualStep;


    //sample relaterd variables
    private double sampleRate;



    //duty cycles
    private short dutyStep;
    private short waveDuty;
    private final int[][] dutyCycles = {
            {0, 0, 0, 0, 0, 0, 0, 1},
            {0, 0, 0, 0, 0, 0, 1, 1},
            {1, 0, 0, 0, 0, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 0}
    };



    public PulseChannels(HardwareRegisters... reg){
        this.channelRegisters = reg.clone();
    }
    public boolean isEnabled(){ return this.Enable;    }
//    public boolean isDacEnabled(){return (channelRegisters[1].addr & 0xF8) != 0;}

    public void trigger(){
        this.Enable = true;
        this.lengthCounter = (this.lengthCounter == 0 ) ? 64 : this.lengthCounter;
        this.currfrequency = (2048-periodValue)*4;
        this.dutyStep = 0;
        this.envelopeEnabled = true;
        this.envelopeCounter = this.envelopSweepPace;
        this.currVolume = initialVolume;
    }

    public void tick(int cycles){
        if(Enable) {
            this.currfrequency -= cycles;
            if(this.currfrequency <= 0){
                step();
                this.currfrequency += (2048-this.periodValue)*4;
            }

        }
    }
    public void lengthClock() {
        if (this.lengthEnabled == 1) {
            this.lengthCounter--;
            if (this.lengthCounter <= 0) {
                this.Enable = false;
            }
        }
    }
    public void envelopeClock() {
        if (this.envelopeEnabled) {
            this.envelopeCounter--;
            if (this.envelopeCounter <= 0) {
                this.envelopeCounter = this.envelopSweepPace;
                if (enveloppeDir == 0)
                {
                    if(currVolume > 0){
                        currVolume--;
                    }
                }
                else {
                    if(currVolume < 15){
                        currVolume++;
                    }
                }
            }

        }
    }
    public void sweepClock(){
        if (this.sweepPace == 0) {
            return;
        }
        this.sweePaceCounter--;
        if (this.sweePaceCounter <= 0) {
            this.sweePaceCounter = this.sweepPace;

            int nextPeriod;

            if (this.sweepDirection == 0) {
                nextPeriod = this.periodValue + (this.periodValue >> this.individualStep);
            } else {
                nextPeriod = this.periodValue - (this.periodValue >> this.individualStep);
            }

            if (nextPeriod > 2047) {
                this.Enable = false;
            } else if (this.individualStep > 0) {
                this.periodValue = nextPeriod;

                this.currfrequency = (2048 - this.periodValue) * 4;


                if ((this.periodValue + (this.periodValue >> this.individualStep)) > 2047&& (this.sweepDirection == 0)) {
                    this.Enable = false;
                }
            }
        }



    }




    public void step(){
        dutyStep = (short) ((dutyStep +1) % 8);
    }

    public int getAmplitude(){
        if(!this.Enable || !this.isDacEnable)
        {
            return 0;
        }
        int dutyCycleState = this.dutyCycles[this.waveDuty][this.dutyStep];
        return (dutyCycleState == 1) ? this.currVolume : 0;

    }

    //next need to differentiate the two states when the channel is on or off on writing
    public void write(char addr, byte val){
        if(addr == (char) HardwareRegisters.NR10.addr){
            this.sweepPace = (val >> 4) & 0x07;
            this.sweePaceCounter = this.sweepPace;
            this.sweepDirection = (short) ((val >> 3) & 0x01);
            this.individualStep = val & 0x07;
            return;
        }
        else if((addr == (char) HardwareRegisters.NR11.addr) || addr == (char) HardwareRegisters.NR21.addr){
            this.waveDuty = (short) ((val >> 6) & 0x03);
            this.initLengthTimer = val & 0x3F;
            this.lengthCounter = 64 - (val & 0x3F);
            return;
        }
        else if((addr == (char) HardwareRegisters.NR12.addr) || addr == (char) HardwareRegisters.NR22.addr){
            this.initialVolume = (val >> 4) & 0x0F;
            this.currVolume = this.initialVolume; // not sure
            this.enveloppeDir = (short) ((val >> 3 ) & 0x01);
            if ((this.envelopSweepPace = (short) (val & 0x07) ) == 0) this.envelopeEnabled = false; else envelopeEnabled = true;
            this.isDacEnable = (val & 0xF8) != 0;
            if(!isDacEnable){this.Enable = false;}
            return;
        }
        else if((addr == (char) HardwareRegisters.NR13.addr) || addr == (char) HardwareRegisters.NR23.addr){
            this.periodValue = (this.periodValue & 0x0700) | (val & 0xFF);
            this.sampleRate = (double) (1_048_576) /(2048-this.periodValue);
            return;
        }
        else if((addr == (char) HardwareRegisters.NR14.addr) || addr == (char) HardwareRegisters.NR24.addr){
            this.periodValue = (this.periodValue & 0x00FF) | ((val & 0x07) << 8);
            this.lengthEnabled = (val >> 6) & 0x01;
            if(((val >> 7 ) & 0x01) == 1){trigger();}
            return;
        }

        System.err.println("Wrong component");




    }

}
