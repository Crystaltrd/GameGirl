package Model;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class APU extends BusMemory {
    private static final int MASTER_CLOCK = 4_194_304;
    private static final int SAMPLE_RATE = 48_000;
    private static final double HPF_CHARGE_FACTOR = 0.999958;
    private static final int[] DUTY_PATTERNS = {
            0b0000_0001,
            0b1000_0001,
            0b1000_0111,
            0b0111_1110
    };
    private static final int[] NOISE_DIVISORS = {8, 16, 32, 48, 64, 80, 96, 112};

    private final Emulator context;
    private final AudioOutput output;
    private final PulseChannel channel1 = new PulseChannel(true);
    private final PulseChannel channel2 = new PulseChannel(false);
    private final WaveChannel channel3 = new WaveChannel();
    private final NoiseChannel channel4 = new NoiseChannel();
    private final byte[] waveRam = new byte[16];

    private boolean powered;
    private int nr50;
    private int nr51;
    private int frameSequencerStep;
    private int sampleAccumulator;
    private double leftCapacitor;
    private double rightCapacitor;

    APU(Emulator context, boolean enableAudioOutput) {
        this.context = context;
        this.output = enableAudioOutput ? new JavaSoundAudioOutput(SAMPLE_RATE) : AudioOutput.NULL;
        resetToBootState();
    }

    private static double digitalToAnalog(int digitalSample, boolean dacEnabled) {
        if (!dacEnabled) {
            return 0.0;
        }
        return -1.0 + ((digitalSample & 0xF) * (2.0 / 15.0));
    }

    private void resetToBootState() {
        powered = true;
        nr50 = 0x77;
        nr51 = 0xF3;
        frameSequencerStep = 0;
        sampleAccumulator = 0;
        leftCapacitor = 0.0;
        rightCapacitor = 0.0;
        channel1.setBootState(0x80, 0xBF, 0xF3, 0x00, 0x00);
        channel2.setBootState(0x00, 0x3F, 0x00, 0x00, 0x00);
        channel3.setBootState(0x7F, 0xFF, 0x9F, 0x00, 0x00);
        channel4.setBootState(0xFF, 0x00, 0x00, 0x00);
        Arrays.fill(waveRam, (byte) 0x00);
    }

    public void tick() {
        if (powered) {
            channel1.tickFrequencyTimer();
            channel2.tickFrequencyTimer();
            channel3.tickFrequencyTimer(waveRam);
            channel4.tickFrequencyTimer();
        }
        if (output != AudioOutput.NULL) {
            sampleAccumulator += SAMPLE_RATE;
            while (sampleAccumulator >= MASTER_CLOCK) {
                sampleAccumulator -= MASTER_CLOCK;
                output.writeSample(toPcmSample(mixOutput(true)), toPcmSample(mixOutput(false)));
            }
        }
    }

    public void shutdown() {
        output.close();
    }

    public void clockFrameSequencer() {
        if (powered) {
            if ((frameSequencerStep & 0x1) == 0) {
                channel1.clockLength();
                channel2.clockLength();
                channel3.clockLength();
                channel4.clockLength();
            }
            if (frameSequencerStep == 2 || frameSequencerStep == 6) {
                channel1.clockSweep();
            }
            if (frameSequencerStep == 7) {
                channel1.clockEnvelope();
                channel2.clockEnvelope();
                channel4.clockEnvelope();
            }
        }
        frameSequencerStep = (frameSequencerStep + 1) & 0x7;
    }

    @Override
    public int read(int address) {
        address &= 0xFF;
        if (Commons.isBetween(address, 0x30, 0x3F)) {
            return waveRam[address - 0x30] & 0xFF;
        }
        return switch (address) {
            case 0x10 -> channel1.readNr10();
            case 0x11 -> channel1.readNr11();
            case 0x12 -> channel1.readNr12();
            case 0x13 -> 0xFF;
            case 0x14 -> channel1.readNr14();
            case 0x15 -> 0xFF;
            case 0x16 -> channel2.readNr11();
            case 0x17 -> channel2.readNr12();
            case 0x18 -> 0xFF;
            case 0x19 -> channel2.readNr14();
            case 0x1A -> channel3.readNr30();
            case 0x1B -> 0xFF;
            case 0x1C -> channel3.readNr32();
            case 0x1D -> 0xFF;
            case 0x1E -> channel3.readNr34();
            case 0x1F -> 0xFF;
            case 0x20 -> 0xFF;
            case 0x21 -> channel4.readNr42();
            case 0x22 -> channel4.readNr43();
            case 0x23 -> channel4.readNr44();
            case 0x24 -> nr50;
            case 0x25 -> nr51;
            case 0x26 -> 0x70
                    | (powered ? 0x80 : 0x00)
                    | (channel1.isEnabled() ? 0x01 : 0x00)
                    | (channel2.isEnabled() ? 0x02 : 0x00)
                    | (channel3.isEnabled() ? 0x04 : 0x00)
                    | (channel4.isEnabled() ? 0x08 : 0x00);
            default -> 0xFF;
        };
    }

    @Override
    public void write(int address, int value) {
        address &= 0xFF;
        value &= 0xFF;

        if (Commons.isBetween(address, 0x30, 0x3F)) {
            waveRam[address - 0x30] = (byte) value;
            return;
        }

        if (address == 0x26) {
            setPower((value & 0x80) != 0);
            return;
        }

        if (!powered) {
            return;
        }

        switch (address) {
            case 0x10 -> channel1.writeNr10(value);
            case 0x11 -> channel1.writeNr11(value);
            case 0x12 -> channel1.writeNr12(value);
            case 0x13 -> channel1.writeNr13(value);
            case 0x14 -> channel1.writeNr14(value);
            case 0x16 -> channel2.writeNr11(value);
            case 0x17 -> channel2.writeNr12(value);
            case 0x18 -> channel2.writeNr13(value);
            case 0x19 -> channel2.writeNr14(value);
            case 0x1A -> channel3.writeNr30(value);
            case 0x1B -> channel3.writeNr31(value);
            case 0x1C -> channel3.writeNr32(value);
            case 0x1D -> channel3.writeNr33(value);
            case 0x1E -> channel3.writeNr34(value);
            case 0x20 -> channel4.writeNr41(value);
            case 0x21 -> channel4.writeNr42(value);
            case 0x22 -> channel4.writeNr43(value);
            case 0x23 -> channel4.writeNr44(value);
            case 0x24 -> nr50 = value;
            case 0x25 -> nr51 = value;
            default -> {
            }
        }
    }

    private void setPower(boolean enabled) {
        if (powered == enabled) {
            return;
        }
        powered = enabled;
        if (!enabled) {
            nr50 = 0;
            nr51 = 0;
            leftCapacitor = 0.0;
            rightCapacitor = 0.0;
            channel1.powerOff();
            channel2.powerOff();
            channel3.powerOff();
            channel4.powerOff();
        }
    }

    private double mixOutput(boolean left) {
        if (!powered) {
            return 0.0;
        }

        double ch1 = channel1.currentAnalogSample();
        double ch2 = channel2.currentAnalogSample();
        double ch3 = channel3.currentAnalogSample();
        double ch4 = channel4.currentAnalogSample();

        int routing = left ? (nr51 >> 4) : nr51;
        double mixed = 0.0;
        if ((routing & 0x1) != 0) {
            mixed += ch1;
        }
        if ((routing & 0x2) != 0) {
            mixed += ch2;
        }
        if ((routing & 0x4) != 0) {
            mixed += ch3;
        }
        if ((routing & 0x8) != 0) {
            mixed += ch4;
        }
        mixed = (mixed / 4.0) * ((getMasterVolume(left) + 1) / 8.0);

        boolean anyDacEnabled = channel1.isDacEnabled()
                || channel2.isDacEnabled()
                || channel3.isDacEnabled()
                || channel4.isDacEnabled();

        return applyHighPass(left, mixed, anyDacEnabled);
    }

    private int getMasterVolume(boolean left) {
        return left ? ((nr50 >> 4) & 0x7) : (nr50 & 0x7);
    }

    private double applyHighPass(boolean left, double input, boolean dacConnected) {
        if (!dacConnected) {
            if (left) {
                leftCapacitor = 0.0;
            } else {
                rightCapacitor = 0.0;
            }
            return 0.0;
        }

        double capacitor = left ? leftCapacitor : rightCapacitor;
        double output = input - capacitor;
        capacitor = input - (output * HPF_CHARGE_FACTOR);

        if (left) {
            leftCapacitor = capacitor;
        } else {
            rightCapacitor = capacitor;
        }
        return output;
    }

    private short toPcmSample(double sample) {
        double clamped = Math.max(-1.0, Math.min(1.0, sample));
        return (short) Math.round(clamped * Short.MAX_VALUE);
    }

    private interface AudioOutput {
        AudioOutput NULL = new AudioOutput() {
            @Override
            public void writeSample(short left, short right) {
            }

            @Override
            public void close() {
            }
        };

        void writeSample(short left, short right);

        void close();
    }

    private abstract static class ChannelBase {
        protected boolean enabled;
        protected boolean lengthEnabled;
        protected int lengthCounter;

        public boolean isEnabled() {
            return enabled;
        }

        public void clockLength() {
            if (lengthEnabled && lengthCounter > 0 && --lengthCounter == 0) {
                enabled = false;
            }
        }

        public abstract boolean isDacEnabled();

        public abstract double currentAnalogSample();

        public abstract void powerOff();
    }

    private static final class PulseChannel extends ChannelBase {
        private final boolean sweepCapable;

        private int nr0;
        private int nr1;
        private int nr2;
        private int nr3;
        private int nr4;
        private int dutyStep;
        private int frequencyTimer;
        private int currentVolume;
        private int envelopeTimer;
        private boolean forceZeroOutput;

        private int sweepTimer;
        private int shadowPeriod;
        private boolean sweepEnabled;

        private PulseChannel(boolean sweepCapable) {
            this.sweepCapable = sweepCapable;
        }

        public void setBootState(int nr0, int nr1, int nr2, int nr3, int nr4) {
            this.nr0 = sweepCapable ? (nr0 & 0x7F) : 0;
            this.nr1 = nr1 & 0xFF;
            this.nr2 = nr2 & 0xFF;
            this.nr3 = nr3 & 0xFF;
            this.nr4 = nr4 & 0x47;
            dutyStep = 0;
            frequencyTimer = Math.max(1, periodReload());
            currentVolume = initialVolume();
            envelopeTimer = envelopePace() == 0 ? 8 : envelopePace();
            shadowPeriod = period();
            sweepTimer = sweepPace() == 0 ? 8 : sweepPace();
            sweepEnabled = false;
            forceZeroOutput = true;
            enabled = false;
            lengthEnabled = (this.nr4 & 0x40) != 0;
            lengthCounter = 64 - (this.nr1 & 0x3F);
        }

        public int readNr10() {
            return nr0 | 0x80;
        }

        public int readNr11() {
            return nr1 | 0x3F;
        }

        public int readNr12() {
            return nr2;
        }

        public int readNr14() {
            return nr4 | 0xBF;
        }

        public void writeNr10(int value) {
            if (sweepCapable) {
                nr0 = value & 0x7F;
            }
        }

        public void writeNr11(int value) {
            nr1 = value & 0xFF;
            lengthCounter = 64 - (value & 0x3F);
        }

        public void writeNr12(int value) {
            nr2 = value & 0xFF;
            if (!isDacEnabled()) {
                enabled = false;
            }
        }

        public void writeNr13(int value) {
            nr3 = value & 0xFF;
        }

        public void writeNr14(int value) {
            nr4 = value & 0x47;
            lengthEnabled = (value & 0x40) != 0;
            if ((value & 0x80) != 0) {
                trigger();
            }
        }

        public void tickFrequencyTimer() {
            if (--frequencyTimer <= 0) {
                frequencyTimer = Math.max(1, periodReload());
                dutyStep = (dutyStep + 1) & 0x7;
                forceZeroOutput = false;
            }
        }

        public void clockEnvelope() {
            int pace = envelopePace();
            if (pace == 0) {
                return;
            }
            if (--envelopeTimer <= 0) {
                envelopeTimer = pace == 0 ? 8 : pace;
                if (envelopeIncrease()) {
                    if (currentVolume < 15) {
                        currentVolume++;
                    }
                } else if (currentVolume > 0) {
                    currentVolume--;
                }
            }
        }

        public void clockSweep() {
            if (!sweepCapable) {
                return;
            }
            if (--sweepTimer <= 0) {
                sweepTimer = sweepPace() == 0 ? 8 : sweepPace();
                if (sweepEnabled && sweepPace() != 0) {
                    int newPeriod = calculateSweepPeriod();
                    if (newPeriod > 2047) {
                        enabled = false;
                        return;
                    }
                    if (sweepShift() != 0) {
                        shadowPeriod = newPeriod;
                        setPeriod(newPeriod);
                        if (calculateSweepPeriod() > 2047) {
                            enabled = false;
                        }
                    }
                }
            }
        }

        private void trigger() {
            if (lengthCounter == 0) {
                lengthCounter = 64;
            }
            frequencyTimer = Math.max(1, periodReload());
            currentVolume = initialVolume();
            envelopeTimer = envelopePace() == 0 ? 8 : envelopePace();
            forceZeroOutput = true;
            enabled = isDacEnabled();

            if (sweepCapable) {
                shadowPeriod = period();
                sweepTimer = sweepPace() == 0 ? 8 : sweepPace();
                sweepEnabled = sweepPace() != 0 || sweepShift() != 0;
                if (sweepShift() != 0 && calculateSweepPeriod() > 2047) {
                    enabled = false;
                }
            }
        }

        private int calculateSweepPeriod() {
            int delta = shadowPeriod >> sweepShift();
            return sweepDecrease() ? shadowPeriod - delta : shadowPeriod + delta;
        }

        private int periodReload() {
            return (2048 - period()) * 4;
        }

        private int period() {
            return nr3 | ((nr4 & 0x7) << 8);
        }

        private void setPeriod(int value) {
            nr3 = value & 0xFF;
            nr4 = (nr4 & 0x40) | ((value >> 8) & 0x7);
        }

        private int initialVolume() {
            return (nr2 >> 4) & 0xF;
        }

        private boolean envelopeIncrease() {
            return (nr2 & 0x08) != 0;
        }

        private int envelopePace() {
            return nr2 & 0x7;
        }

        private int sweepPace() {
            return (nr0 >> 4) & 0x7;
        }

        private boolean sweepDecrease() {
            return (nr0 & 0x08) != 0;
        }

        private int sweepShift() {
            return nr0 & 0x7;
        }

        private int dutyPattern() {
            return DUTY_PATTERNS[(nr1 >> 6) & 0x3];
        }

        @Override
        public boolean isDacEnabled() {
            return (nr2 & 0xF8) != 0;
        }

        @Override
        public double currentAnalogSample() {
            return digitalToAnalog(currentDigitalSample(), isDacEnabled());
        }

        private int currentDigitalSample() {
            if (!isDacEnabled() || !enabled || forceZeroOutput) {
                return 0;
            }
            return ((dutyPattern() >> (7 - dutyStep)) & 0x1) != 0 ? currentVolume : 0;
        }

        @Override
        public void powerOff() {
            nr0 = 0;
            nr1 = 0;
            nr2 = 0;
            nr3 = 0;
            nr4 = 0;
            dutyStep = 0;
            frequencyTimer = 0;
            currentVolume = 0;
            envelopeTimer = 0;
            sweepTimer = 0;
            shadowPeriod = 0;
            sweepEnabled = false;
            forceZeroOutput = true;
            enabled = false;
            lengthEnabled = false;
            lengthCounter = 0;
        }
    }

    private static final class WaveChannel extends ChannelBase {
        private int nr30;
        private int nr31;
        private int nr32;
        private int nr33;
        private int nr34;
        private int frequencyTimer;
        private int sampleIndex;
        private int lastSample;

        public void setBootState(int nr30, int nr31, int nr32, int nr33, int nr34) {
            this.nr30 = nr30 & 0x80;
            this.nr31 = nr31 & 0xFF;
            this.nr32 = nr32 & 0x60;
            this.nr33 = nr33 & 0xFF;
            this.nr34 = nr34 & 0x47;
            frequencyTimer = Math.max(1, periodReload());
            sampleIndex = 0;
            lastSample = 0;
            enabled = false;
            lengthEnabled = (this.nr34 & 0x40) != 0;
            lengthCounter = 256 - this.nr31;
        }

        public int readNr30() {
            return nr30 | 0x7F;
        }

        public int readNr32() {
            return nr32 | 0x9F;
        }

        public int readNr34() {
            return nr34 | 0xBF;
        }

        public void writeNr30(int value) {
            nr30 = value & 0x80;
            if (!isDacEnabled()) {
                enabled = false;
            }
        }

        public void writeNr31(int value) {
            nr31 = value & 0xFF;
            lengthCounter = 256 - nr31;
        }

        public void writeNr32(int value) {
            nr32 = value & 0x60;
        }

        public void writeNr33(int value) {
            nr33 = value & 0xFF;
        }

        public void writeNr34(int value) {
            nr34 = value & 0x47;
            lengthEnabled = (value & 0x40) != 0;
            if ((value & 0x80) != 0) {
                trigger();
            }
        }

        public void tickFrequencyTimer(byte[] waveRam) {
            if (!enabled) {
                return;
            }
            if (--frequencyTimer <= 0) {
                frequencyTimer = Math.max(1, periodReload());
                sampleIndex = (sampleIndex + 1) & 0x1F;
                lastSample = readWaveSample(waveRam, sampleIndex);
            }
        }

        private void trigger() {
            if (lengthCounter == 0) {
                lengthCounter = 256;
            }
            frequencyTimer = Math.max(1, periodReload());
            sampleIndex = 0;
            enabled = isDacEnabled();
        }

        private int periodReload() {
            return (2048 - period()) * 2;
        }

        private int period() {
            return nr33 | ((nr34 & 0x7) << 8);
        }

        private int volumeCode() {
            return (nr32 >> 5) & 0x3;
        }

        private int readWaveSample(byte[] waveRam, int index) {
            int sampleByte = waveRam[index >> 1] & 0xFF;
            return (index & 0x1) == 0 ? ((sampleByte >> 4) & 0xF) : (sampleByte & 0xF);
        }

        @Override
        public boolean isDacEnabled() {
            return (nr30 & 0x80) != 0;
        }

        @Override
        public double currentAnalogSample() {
            return digitalToAnalog(currentDigitalSample(), isDacEnabled());
        }

        private int currentDigitalSample() {
            if (!isDacEnabled() || !enabled) {
                return 0;
            }
            return switch (volumeCode()) {
                case 0 -> 0;
                case 1 -> lastSample;
                case 2 -> lastSample >> 1;
                case 3 -> lastSample >> 2;
                default -> 0;
            };
        }

        @Override
        public void powerOff() {
            nr30 = 0;
            nr31 = 0;
            nr32 = 0;
            nr33 = 0;
            nr34 = 0;
            frequencyTimer = 0;
            sampleIndex = 0;
            lastSample = 0;
            enabled = false;
            lengthEnabled = false;
            lengthCounter = 0;
        }
    }

    private static final class NoiseChannel extends ChannelBase {
        private int nr41;
        private int nr42;
        private int nr43;
        private int nr44;
        private int frequencyTimer;
        private int currentVolume;
        private int envelopeTimer;
        private int lfsr;
        private int shiftedOutBit;

        public void setBootState(int nr41, int nr42, int nr43, int nr44) {
            this.nr41 = nr41 & 0x3F;
            this.nr42 = nr42 & 0xFF;
            this.nr43 = nr43 & 0xFF;
            this.nr44 = nr44 & 0x40;
            frequencyTimer = Math.max(1, periodReload());
            currentVolume = initialVolume();
            envelopeTimer = envelopePace() == 0 ? 8 : envelopePace();
            lfsr = 0;
            shiftedOutBit = 0;
            enabled = false;
            lengthEnabled = (this.nr44 & 0x40) != 0;
            lengthCounter = 64 - this.nr41;
        }

        public int readNr42() {
            return nr42;
        }

        public int readNr43() {
            return nr43;
        }

        public int readNr44() {
            return nr44 | 0xBF;
        }

        public void writeNr41(int value) {
            nr41 = value & 0x3F;
            lengthCounter = 64 - nr41;
        }

        public void writeNr42(int value) {
            nr42 = value & 0xFF;
            if (!isDacEnabled()) {
                enabled = false;
            }
        }

        public void writeNr43(int value) {
            nr43 = value & 0xFF;
        }

        public void writeNr44(int value) {
            nr44 = value & 0x40;
            lengthEnabled = (value & 0x40) != 0;
            if ((value & 0x80) != 0) {
                trigger();
            }
        }

        public void tickFrequencyTimer() {
            if (!enabled) {
                return;
            }
            if (--frequencyTimer <= 0) {
                frequencyTimer = Math.max(1, periodReload());
                stepLfsr();
            }
        }

        public void clockEnvelope() {
            int pace = envelopePace();
            if (pace == 0) {
                return;
            }
            if (--envelopeTimer <= 0) {
                envelopeTimer = pace == 0 ? 8 : pace;
                if (envelopeIncrease()) {
                    if (currentVolume < 15) {
                        currentVolume++;
                    }
                } else if (currentVolume > 0) {
                    currentVolume--;
                }
            }
        }

        private void trigger() {
            if (lengthCounter == 0) {
                lengthCounter = 64;
            }
            frequencyTimer = Math.max(1, periodReload());
            currentVolume = initialVolume();
            envelopeTimer = envelopePace() == 0 ? 8 : envelopePace();
            lfsr = 0;
            shiftedOutBit = 0;
            enabled = isDacEnabled();
        }

        private void stepLfsr() {
            shiftedOutBit = lfsr & 0x1;
            int bit1 = (lfsr >> 1) & 0x1;
            int newBit = shiftedOutBit == bit1 ? 1 : 0;
            lfsr = (lfsr >> 1) | (newBit << 14);
            if (isWidthMode()) {
                lfsr = (lfsr & ~(1 << 6)) | (newBit << 6);
            }
        }

        private int periodReload() {
            return NOISE_DIVISORS[nr43 & 0x7] << ((nr43 >> 4) & 0xF);
        }

        private int initialVolume() {
            return (nr42 >> 4) & 0xF;
        }

        private boolean envelopeIncrease() {
            return (nr42 & 0x08) != 0;
        }

        private int envelopePace() {
            return nr42 & 0x7;
        }

        private boolean isWidthMode() {
            return (nr43 & 0x08) != 0;
        }

        @Override
        public boolean isDacEnabled() {
            return (nr42 & 0xF8) != 0;
        }

        @Override
        public double currentAnalogSample() {
            return digitalToAnalog(currentDigitalSample(), isDacEnabled());
        }

        private int currentDigitalSample() {
            if (!isDacEnabled() || !enabled) {
                return 0;
            }
            return shiftedOutBit != 0 ? currentVolume : 0;
        }

        @Override
        public void powerOff() {
            nr41 = 0;
            nr42 = 0;
            nr43 = 0;
            nr44 = 0;
            frequencyTimer = 0;
            currentVolume = 0;
            envelopeTimer = 0;
            lfsr = 0;
            shiftedOutBit = 0;
            enabled = false;
            lengthEnabled = false;
            lengthCounter = 0;
        }
    }

    private static final class JavaSoundAudioOutput implements AudioOutput {
        private static final int FRAME_BYTES = 4;
        private final int sampleRate;
        private final byte[] pending = new byte[4096];
        private SourceDataLine line;
        private int pendingSize;
        private boolean failed;

        private JavaSoundAudioOutput(int sampleRate) {
            this.sampleRate = sampleRate;
        }

        @Override
        public void writeSample(short left, short right) {
            if (!ensureOpen()) {
                return;
            }
            if (pendingSize + FRAME_BYTES > pending.length) {
                flush();
            }
            pending[pendingSize++] = (byte) (left & 0xFF);
            pending[pendingSize++] = (byte) ((left >> 8) & 0xFF);
            pending[pendingSize++] = (byte) (right & 0xFF);
            pending[pendingSize++] = (byte) ((right >> 8) & 0xFF);
            if (pendingSize >= pending.length) {
                flush();
            }
        }

        @Override
        public void close() {
            if (failed) {
                return;
            }
            flush();
            if (line != null) {
                line.drain();
                line.stop();
                line.close();
                line = null;
            }
        }

        private boolean ensureOpen() {
            if (failed) {
                return false;
            }
            if (line != null) {
                return true;
            }
            try {
                AudioFormat format = new AudioFormat(sampleRate, 16, 2, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                int bufferSize = Math.max(pending.length * 8, sampleRate * FRAME_BYTES / 8);
                line = openPreferredLine(format, info, bufferSize);
                if (line == null) {
                    failed = true;
                    return false;
                }
                line.start();
                return true;
            } catch (IllegalArgumentException | LineUnavailableException ex) {
                failed = true;
                return false;
            }
        }

        private SourceDataLine openPreferredLine(AudioFormat format, DataLine.Info info, int bufferSize)
                throws LineUnavailableException {
            List<MixerCandidate> candidates = collectMixerCandidates(info);
            LineUnavailableException unavailable = null;
            IllegalArgumentException unsupported = null;

            for (MixerCandidate candidate : candidates) {
                try {
                    Mixer mixer = AudioSystem.getMixer(candidate.info);
                    SourceDataLine chosenLine = (SourceDataLine) mixer.getLine(info);
                    chosenLine.open(format, bufferSize);
                    System.out.printf("APU audio mixer: %s (%s)%n",
                            candidate.info.getName(),
                            candidate.info.getDescription());
                    return chosenLine;
                } catch (LineUnavailableException ex) {
                    unavailable = ex;
                } catch (IllegalArgumentException ex) {
                    unsupported = ex;
                }
            }

            if (AudioSystem.isLineSupported(info)) {
                try {
                    SourceDataLine chosenLine = (SourceDataLine) AudioSystem.getLine(info);
                    chosenLine.open(format, bufferSize);
                    System.out.println("APU audio mixer: default");
                    return chosenLine;
                } catch (LineUnavailableException ex) {
                    unavailable = ex;
                } catch (IllegalArgumentException ex) {
                    unsupported = ex;
                }
            }

            if (unavailable != null) {
                throw unavailable;
            }
            if (unsupported != null) {
                throw unsupported;
            }
            return null;
        }

        private List<MixerCandidate> collectMixerCandidates(DataLine.Info info) {
            String preferredMixer = System.getProperty("gamegirl.audio.mixer", "").trim().toLowerCase(Locale.ROOT);
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            List<MixerCandidate> candidates = new ArrayList<>();

            for (int index = 0; index < mixerInfos.length; index++) {
                Mixer.Info mixerInfo = mixerInfos[index];
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                if (!mixer.isLineSupported(info)) {
                    continue;
                }
                candidates.add(new MixerCandidate(mixerInfo, scoreMixer(mixerInfo, preferredMixer, index), index));
            }

            candidates.sort(Comparator
                    .comparingInt(MixerCandidate::score).reversed()
                    .thenComparingInt(MixerCandidate::index));
            return candidates;
        }

        private int scoreMixer(Mixer.Info mixerInfo, String preferredMixer, int index) {
            String text = (mixerInfo.getName() + " " + mixerInfo.getDescription()).toLowerCase(Locale.ROOT);
            int score = 0;

            if (!preferredMixer.isBlank()) {
                if (preferredMixer.equals(String.valueOf(index)) || text.contains(preferredMixer)) {
                    score += 10_000;
                } else {
                    score -= 1_000;
                }
            }

            if (containsAny(text, "analog", "speaker", "speakers", "headphone", "headphones", "line out")) {
                score += 500;
            }
            if (containsAny(text, "hdmi", "displayport", "display port")) {
                score -= 500;
            }
            if (text.contains("nvidia") && containsAny(text, "hdmi", "displayport", "display port")) {
                score -= 250;
            }

            return score;
        }

        private boolean containsAny(String text, String... needles) {
            for (String needle : needles) {
                if (text.contains(needle)) {
                    return true;
                }
            }
            return false;
        }

        private void flush() {
            if (line == null || pendingSize == 0) {
                return;
            }
            line.write(pending, 0, pendingSize);
            pendingSize = 0;
        }

        private record MixerCandidate(Mixer.Info info, int score, int index) {
        }
    }
}
