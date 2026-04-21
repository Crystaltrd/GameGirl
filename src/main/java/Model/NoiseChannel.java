package Model;

public class NoiseChannel  {
    private final HardwareRegisters[] channelRegisters;

    private boolean isDacEnable;
    private boolean Enable;

    private int lengthEnabled;
    private int lengthCounter;

    private int initialVolume;
    private int currVolume;
    private short enveloppeDir;
    private short envelopSweepPace;
    private short envelopeCounter;
    private boolean envelopeEnabled;

    private int lfsr = 0x7FFF;
    private int clockShift;
    private int clockDivider;
    private short lfsrWidth;
    private int currfrequency;
    private int currSample;

    public NoiseChannel(HardwareRegisters... reg) {
        this.channelRegisters = reg.clone();
    }

    public boolean isEnabled() {
        return this.Enable;
    }

    public void trigger() {
        this.Enable = true;
        this.lengthCounter = (this.lengthCounter == 0) ? 64 : this.lengthCounter;
        this.currfrequency = getFrequencyTimer();
        this.lfsr = 0x7FFF;
        this.envelopeEnabled = (this.envelopSweepPace != 0);
        this.envelopeCounter = (this.envelopSweepPace == 0) ? 8 : this.envelopSweepPace;
        this.currVolume = this.initialVolume;
    }

    private int getFrequencyTimer() {
        int divisor = (this.clockDivider == 0) ? 8 : (this.clockDivider * 16);
        return divisor << this.clockShift;
    }

    public void tick(int cycles) {
        if (Enable && isDacEnable) {
            this.currfrequency -= cycles;
            if (this.currfrequency <= 0) {
                step();
                this.currfrequency += getFrequencyTimer();
            }
        }
    }

    public void step() {
        int xorResult = (this.lfsr & 0x01) ^ ((this.lfsr >> 1) & 0x01);
        this.lfsr >>= 1;
        this.lfsr |= (xorResult << 14);

        if (this.lfsrWidth == 1) {
            this.lfsr &= ~0x40;
            this.lfsr |= (xorResult << 6);
        }

        this.currSample = ((this.lfsr & 0x01) == 0) ? this.currVolume : 0;
    }

    public int getAmplitude() {
        return (Enable && isDacEnable) ? this.currSample : 0;
    }

    public void lengthClock() {
        if (this.lengthEnabled == 1 && this.lengthCounter > 0) {
            this.lengthCounter--;
            if (this.lengthCounter == 0) this.Enable = false;
        }
    }

    public void envelopeClock() {
        if (this.envelopeEnabled) {
            this.envelopeCounter--;
            if (this.envelopeCounter <= 0) {
                this.envelopeCounter = (this.envelopSweepPace == 0) ? 8 : this.envelopSweepPace;                if (enveloppeDir == 0 && currVolume > 0) currVolume--;
                else if (enveloppeDir == 1 && currVolume < 15) currVolume++;
            }
        }
    }

    public void write(int addr, int val) {
        HardwareRegisters reg = HardwareRegisters.findByValue(addr);

        switch (reg) {
            case NR41 -> {
                this.lengthCounter = 64 - (val & 0x3F);
            }
            case NR42 -> {
                this.initialVolume = (val >> 4) & 0x0F;
                this.enveloppeDir = (short) ((val >> 3) & 0x01);
                this.envelopSweepPace = (short) (val & 0x07);
                this.isDacEnable = (val & 0xF8) != 0;
                if (!isDacEnable) {
                    this.Enable = false;
                }
            }
            case NR43 -> {
                this.clockShift = (val >> 4) & 0x0F;
                this.lfsrWidth = (short) ((val >> 3) & 0x01);
                this.clockDivider = val & 0x07;
            }
            case NR44 -> {
                this.lengthEnabled = (val >> 6) & 0x01;
                if (((val >> 7) & 0x01) == 1) {
                    trigger();
                }
            }
            case null, default -> {
            }
        }
    }
}