package Model;

public class PulseChannels  {
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

    private int periodValue;
    private int currfrequency;

    //sweep
    private int sweepPace;
    private int sweePaceCounter;
    private short sweepDirection;
    private int individualStep;
    private int shadowPeriod;
    private boolean sweepEnabled;





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
    public void trigger(){
        this.Enable = true;
        this.lengthCounter = (this.lengthCounter == 0 ) ? 64 : this.lengthCounter;
        this.currfrequency = (2048-periodValue)*4;
        this.envelopeEnabled = (this.envelopSweepPace != 0);
        this.envelopeCounter = (this.envelopSweepPace == 0) ? 8 : this.envelopSweepPace;
        this.currVolume = initialVolume;
        this.shadowPeriod = this.periodValue;
        this.sweePaceCounter = (this.sweepPace == 0) ? 8 : this.sweepPace;
        this.sweepEnabled = (this.sweepPace != 0) || (this.individualStep != 0);
        if (this.individualStep != 0) {
            int newPeriod = calculateNewPeriod();
            if (newPeriod > 2047) this.Enable = false;
        }

    }
    private int calculateNewPeriod() {
        int temp = this.shadowPeriod >> this.individualStep;
        return (this.sweepDirection == 0) ? this.shadowPeriod + temp : this.shadowPeriod - temp;
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
                this.envelopeCounter = (this.envelopSweepPace == 0) ? 8 : this.envelopSweepPace;
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

            this.sweePaceCounter--;
            if (this.sweePaceCounter <= 0) {
                this.sweePaceCounter = (this.sweepPace == 0) ? 8 : this.sweepPace;
                if (this.sweepEnabled && this.sweepPace != 0) {
                    int newPeriod = calculateNewPeriod();
                    if (newPeriod > 2047) {
                        this.Enable = false;
                    } else if (this.individualStep != 0) {
                        this.shadowPeriod = newPeriod;
                        this.periodValue = newPeriod;
                        this.currfrequency = (2048 - this.periodValue) * 4;
                        int newPeriod2 = calculateNewPeriod();
                        if (newPeriod2 > 2047) this.Enable = false;
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

    public void write(int addr, int val){

        HardwareRegisters a = HardwareRegisters.findByValue(addr);
        switch (a) {
            case NR10 -> {
                this.sweepPace = (val >> 4) & 0x07;
                this.sweePaceCounter = this.sweepPace;
                this.sweepDirection = (short) ((val >> 3) & 0x01);
                this.individualStep = val & 0x07;
            }
            case NR11, NR21 -> {
                this.waveDuty = (short) ((val >> 6) & 0x03);
                this.initLengthTimer = val & 0x3F;
                this.lengthCounter = 64 - (val & 0x3F);
            }
            case NR12, NR22 -> {
                this.initialVolume = (val >> 4) & 0x0F;

                this.enveloppeDir = (short) ((val >> 3) & 0x01);
                this.envelopSweepPace = (short) (val & 0x07);
                this.envelopeEnabled = (this.envelopSweepPace != 0);

                this.isDacEnable = (val & 0xF8) != 0;
                if (!isDacEnable) this.Enable = false;
            }
            case NR13, NR23 -> {
                this.periodValue = (this.periodValue & 0x0700) | (val & 0xFF);
            }
            case NR14, NR24 -> {
                this.periodValue = (this.periodValue & 0x00FF) | ((val & 0x07) << 8);
                this.lengthEnabled = (val >> 6) & 0x01;
                if (((val >> 7) & 0x01) == 1) {
                    trigger();
                }
            }
            case null, default ->  System.err.println("Wrong component");

        }






    }

}
