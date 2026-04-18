package Model;

import java.util.Arrays;
import javax.sound.sampled.*;

public class APU extends BusMemory {
    private final Emulator ctx;

    public final PulseChannels pulse1 = new PulseChannels(HardwareRegisters.NR10, HardwareRegisters.NR11, HardwareRegisters.NR12, HardwareRegisters.NR13, HardwareRegisters.NR14);
    private final PulseChannels pulse2 = new PulseChannels(null,HardwareRegisters.NR21, HardwareRegisters.NR22, HardwareRegisters.NR23, HardwareRegisters.NR24);
    private final byte[] regs = new byte[0x27];
    private final byte[] waveRam = new byte[0x10];
    private final WaveChannel waveChannel = new WaveChannel(this.waveRam, HardwareRegisters.NR30, HardwareRegisters.NR31, HardwareRegisters.NR32, HardwareRegisters.NR33, HardwareRegisters.NR34);
    private final NoiseChannel noiseChannel = new NoiseChannel(null,HardwareRegisters.NR41, HardwareRegisters.NR42, HardwareRegisters.NR43, HardwareRegisters.NR44);
    private long tCycles = 0;

    private int frameSequencerCounter = 8192;
    private short frameSequenncerStep;

    private static final int UNUSED_BITS_NR10 = 0b10000000;
    private static final int UNUSED_BITS_NRX1 = 0b00111111;
    private static final int UNUSED_BITS_NRX4 = 0b10111111;
    private static final int UNUSED_BITS_NR30 = 0b01111111;
    private static final int UNUSED_BITS_NR32 = 0b10011111;
    private static final int UNUSED_BITS_NR52 = 0b01110000;

    //audio
    AudioFormat format = new AudioFormat(44100,16,2,true, false);
    SourceDataLine line;



    APU(Emulator ctx) {
        this.ctx = ctx;
        regs[HardwareRegisters.NR52.addr & 0xFF] = (byte) 0x80;
        try {
            this.line = AudioSystem.getSourceDataLine(format);
            this.line.open(format, 4096);
            this.line.start();
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unreachable");
            e.printStackTrace();
        }
    }

    private boolean isEnabled() {
        return (regs[HardwareRegisters.NR52.addr & 0xFF] & 0x80) != 0;
    }

    private void powerOff() {
        Arrays.fill(regs, (byte) 0);
        
    }

    public int read(int addr) {

        if (addr >= 0x30 && addr <= 0x3F) {
            return waveRam[addr - 0x30];
        }
        HardwareRegisters add = HardwareRegisters.findByValue(addr);
        int offset = addr & 0xFF;

            if (addr == (HardwareRegisters.NR52.addr & 0xFF)) {
                int channelFlags = 0;
                if (pulse1.isEnabled()) channelFlags |= 0x01;
                if (pulse2.isEnabled()) channelFlags |= 0x02;
                if (waveChannel.isEnabled()) channelFlags |= 0x04;
                if (noiseChannel.isEnabled()) channelFlags |= 0x08;
                return  ((regs[offset] & 0x80) | 0x70 | channelFlags);
            }

        return switch (add) {
            case NR10 -> regs[offset] | UNUSED_BITS_NR10;
            case NR11, NR21 -> regs[offset] | UNUSED_BITS_NRX1;
            case NR14, NR24, NR34, NR44 -> regs[offset] | UNUSED_BITS_NRX4;
            case NR30 -> regs[offset] | UNUSED_BITS_NR30;
            case NR32 -> regs[offset] | UNUSED_BITS_NR32;
            case NR13, NR23, NR31, NR33, NR41 -> 0xFF;
            case null, default -> regs[offset] & 0xFF; //still not sure

        };

        }

    public void write(int addr, int val) {
        int a = addr &  0xFFFF;
        if (a >= 0xFF30 && a <= 0xFF3F) {
            waveRam[a - 0xFF30] = (byte) val;
            return;
        }
        HardwareRegisters add = HardwareRegisters.findByValue(a);
        int offset = addr & 0xFF;

            if (add == HardwareRegisters.NR52) {
                boolean wasEnabled = isEnabled();
                regs[a] = (byte) (val & 0x80);
                if (wasEnabled && !isEnabled()) {
                    powerOff();
                }
                frameSequencerCounter = 8192;
                frameSequenncerStep = 0;
                return;
            }
        if(isEnabled()) {
            if (a >= 0x10 && a <= 0x19) {
                if (a <= 0x14) {
                    pulse1.write(addr, val);
                } else if (a > 0x15) {
                    pulse2.write(addr, val);
                }
            } else if (a >= 0x1A && a <= 0x1E) {
                waveChannel.write(addr, val);
            } else if (a >= 0x20 && a <= 0x23) {
                noiseChannel.write(addr, val);
            }

            if (a < 0x10 || a > 0x26) {
                return;
            }


            if (!isEnabled()) {
                return;
            }

            regs[offset] = (byte) val;
        }
    }
    public void frameSequencer(){
        if(isEnabled()) {

            frameSequencerCounter--;
            if(this.frameSequencerCounter <= 0 ){
                this.frameSequencerCounter += 8192;
                this.frameSequenncerStep = (short) ((this.frameSequenncerStep + 1) % 8);
                sequencerClock();
            }
        }

    }
    public void sequencerClock(){
        if(frameSequenncerStep % 2 == 0){
            pulse1.lengthClock();
            pulse2.lengthClock();
            waveChannel.lengthClock();
            noiseChannel.lengthClock();
        }
        if(frameSequenncerStep == 7){
            pulse1.envelopeClock();
            pulse2.envelopeClock();
            noiseChannel.envelopeClock();
        }
        if(frameSequenncerStep == 2 || frameSequenncerStep == 6){
            pulse1.sweepClock();
        }

    }
    public float[] getOutputSamples() {
        float left = 0;
        float right = 0;
        int nr51 = regs[0x25] & 0xFF;
        int nr50 = regs[0x24] & 0xFF;
        int ch1 = pulse1.getAmplitude();
        int ch2 = pulse2.getAmplitude();
        int ch3 = waveChannel.getAmplitude();
        int ch4 = noiseChannel.getAmplitude();
        if ((nr51 & 0x01) != 0) right += ch1;
        if ((nr51 & 0x02) != 0) right += ch2;
        if ((nr51 & 0x04) != 0) right += ch3;
        if ((nr51 & 0x08) != 0) right += ch4;
        if ((nr51 & 0x10) != 0) left += ch1;
        if ((nr51 & 0x20) != 0) left += ch2;
        if ((nr51 & 0x40) != 0) left += ch3;
        if ((nr51 & 0x80) != 0) left += ch4;

        float volumeLeft = ((nr50 >> 4) & 0x07) + 1;
        float volumeRight= (nr50 & 0x07) + 1;
        float sampleScale = 128.0f;

        return new float[] {(left * volumeLeft) /sampleScale,(right * volumeRight) / sampleScale};
    }

    public void tick() {
        tCycles++;
        if(tCycles >= 95){
            tCycles -= 95;
            float[] sample  = getOutputSamples();

            short left = (short) (sample[0] * 32767);
            short right = (short) (sample[1] * 32767);
            byte[] buffer =  new byte[4];
            buffer[0] = (byte)(left & 0xFF);
            buffer[1] = (byte)((left >> 8) & 0xFF);
            buffer[2] = (byte)(right & 0xFF);
            buffer[3] = (byte)((right >> 8) & 0xFF);
            if (line != null && isEnabled()) {
                line.write(buffer, 0, 4);
            }


        }

        frameSequencer();
        pulse1.tick(1);
        pulse2.tick(1);
        waveChannel.tick(1);
        noiseChannel.tick(1);

    }
}
