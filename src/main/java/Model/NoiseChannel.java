package Model;

public class NoiseChannel extends Channel {
    private final HardwareRegisters[] channelRegisters;
    private static final int[] DIVISOR_TABLE = {8, 16, 32, 48, 64, 80, 96, 112};

    private int initialVolume;
    private int lengthEnabled;
    private short enveloppeDir;
    private int initLengthTimer;
    private short envelopPeriod;
    private boolean isDacEnable;

    private short clockShift;
    private short lfsrWidth;
    private short clockDivider;
    private double sampleRate;

    private int currfrequency;
    private int lengthCounter;
    private boolean Enable;
    private int currVolume;
    private short envelopeCounter;
    private int lfsr = 0x7FFF;
    private int currSample;

    public NoiseChannel(HardwareRegisters... reg) {
        this.channelRegisters = reg.clone();
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
        this.lengthCounter = (this.lengthCounter == 0) ? 64 : this.lengthCounter;
        this.envelopeCounter = this.envelopPeriod;
        this.currVolume = this.initialVolume;
        this.lfsr = 0x7FFF;
        this.currfrequency = getFrequencyTimerPeriod();
        this.currSample = 0;
    }

    public void tick(int cycles) {
        if (Enable) {
            this.currfrequency -= cycles;
            while (this.currfrequency <= 0) {
                step();
                this.currfrequency += getFrequencyTimerPeriod();
            }
        }
    }

    public void step() {
        int shiftedOutBit = this.lfsr & 0x01;
        int xorBit = (this.lfsr & 0x01) ^ ((this.lfsr >> 1) & 0x01);

        this.lfsr >>= 1;
        this.lfsr |= (xorBit << 14);

        if (this.lfsrWidth == 1) {
            this.lfsr = (this.lfsr & ~(1 << 6)) | (xorBit << 6);
        }

        if (shiftedOutBit == 0) {
            this.currSample = 0;
        } else {
            this.currSample = this.currVolume;
        }
    }

    private int getFrequencyTimerPeriod() {
        if (this.clockShift >= 14) {
            return Integer.MAX_VALUE;
        }
        return DIVISOR_TABLE[this.clockDivider & 0x07] << this.clockShift;
    }

    public void write(char addr, byte val) {
        if (addr == (char) HardwareRegisters.NR41.addr) {
            this.initLengthTimer = val & 0x3F;
            this.lengthCounter = 64 - this.initLengthTimer;
        } else if (addr == (char) HardwareRegisters.NR42.addr) {
            this.initialVolume = (val >> 4) & 0x0F;
            this.currVolume = this.initialVolume;
            this.enveloppeDir = (short) ((val >> 3) & 0x01);
            this.envelopPeriod = (short) (val & 0x07);
            this.isDacEnable = (val & 0xF8) != 0;
            if (!this.isDacEnable) {
                this.Enable = false;
            }
        } else if (addr == (char) HardwareRegisters.NR43.addr) {
            this.clockShift = (short) ((val >> 4) & 0x0F);
            this.lfsrWidth = (short) ((val >> 3) & 0x01);
            this.clockDivider = (short) (val & 0x07);
            this.sampleRate = 4_194_304.0 / getFrequencyTimerPeriod();
        } else if (addr == (char) HardwareRegisters.NR44.addr) {
            if ((this.lengthEnabled = (val >> 6) & 0x01) == 1) {
                this.lengthCounter--;
            }
            if (((val >> 7) & 0x01) == 1) {
                trigger();
            }
        } else {
            System.err.println("Wrong component");

        }
    }
}
