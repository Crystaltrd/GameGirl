package Model;

public class WaveChannel{
    private final HardwareRegisters[] channelRegisters;
    private final byte[] waveRam;

    private int periodValue;
    private int lengthEnabled;
    private int initLengthTimer;
    private short outputLevel;
    private boolean isDacEnable;
    private double sampleRate;

    private int currfrequency;
    private int lengthCounter;
    private boolean Enable;
    private short waveRamPosition;
    private int currSample;

    public WaveChannel(byte[] waveRam, HardwareRegisters... reg) {
        this.channelRegisters = reg.clone();
        this.waveRam = waveRam;
    }

    public boolean isEnabled() {
        return this.Enable;
    }

    public int getCurrSample() {
        return this.currSample;
    }

    public void trigger() {
        if (!this.isDacEnable) {
            this.Enable = false;
            return;
        }
        this.Enable = true;
        this.lengthCounter = (this.lengthCounter == 0) ? 256 : this.lengthCounter;
        this.currfrequency = (2048 - this.periodValue) * 2;
        this.waveRamPosition = 0;
        this.currSample = 0;
    }

    public void tick(int cycles) {
        if (Enable) {
            this.currfrequency -= cycles;
            while (this.currfrequency <= 0) {
                step();
                this.currfrequency += (2048 - this.periodValue) * 2;
            }
        }
    }

    public void step() {
        this.waveRamPosition = (short) ((this.waveRamPosition + 1) & 0x1F);
        int byteIndex = this.waveRamPosition >> 1;
        int sample;
        if ((this.waveRamPosition & 0x01) == 0) {
            sample = (this.waveRam[byteIndex] >> 4) & 0x0F;
        } else {
            sample = this.waveRam[byteIndex] & 0x0F;
        }

        if (this.outputLevel == 0) {
            this.currSample = 0;
        } else if (this.outputLevel == 1) {
            this.currSample = sample;
        } else if (this.outputLevel == 2) {
            this.currSample = sample >> 1;
        } else {
            this.currSample = sample >> 2;
        }
    }
    public void lengthClock(){
        if (this.lengthEnabled == 1) {
            this.lengthCounter--;
            if (this.lengthCounter <= 0) {
                this.Enable = false;
            }
        }
    }

    public int getAmplitude(){
        if (!this.Enable || !this.isDacEnable) {
            return 0;
        }
        return this.currSample;
    }

    public void write(char addr, byte val) {
        if (addr == (char) HardwareRegisters.NR30.addr) {
            this.isDacEnable = ((val >> 7) & 0x01) == 1;
            if (!this.isDacEnable) {
                this.Enable = false;
            }
        } else if (addr == (char) HardwareRegisters.NR31.addr) {
            this.initLengthTimer = val & 0xFF;
            this.lengthCounter = 256 - this.initLengthTimer;
        } else if (addr == (char) HardwareRegisters.NR32.addr) {
            this.outputLevel = (short) ((val >> 5) & 0x03);
        } else if (addr == (char) HardwareRegisters.NR33.addr) {
            this.periodValue = (this.periodValue & 0x0700) | (val & 0xFF);
            this.sampleRate = (double) 2_097_152 / (2048 - this.periodValue);
        } else if (addr == (char) HardwareRegisters.NR34.addr) {
            this.periodValue = (this.periodValue & 0x00FF) | ((val & 0x07) << 8);
            this.lengthEnabled = ((val >> 6) & 0x01);


            if (((val >> 7) & 0x01) == 1) {
                trigger();
            }
        } else {
            System.err.println("Wrong component");

        }
    }
}
