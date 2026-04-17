package Model;

import java.util.Arrays;

public class APU extends GBMemory {
    private final EmulationContext ctx;

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


    APU(EmulationContext ctx) {
        this.ctx = ctx;
        regs[HardwareRegisters.NR52.addr & 0xFF] = (byte) 0x80;
    }

    private boolean isEnabled() {
        return (regs[HardwareRegisters.NR52.addr & 0xFF] & 0x80) != 0;
    }

    private void powerOff() {
        Arrays.fill(regs, (byte) 0);
        
    }

    @Override
    public byte read(char addr) {
        int a = addr & 0xFF;
        if (a >= 0x30 && a <= 0x3F) {
            return waveRam[a - 0x30];
        }
        if (a >= 0x10 && a <= 0x26 && a != 0x15) {
            if (a == (HardwareRegisters.NR52.addr & 0xFF)) {
                int channelFlags = 0;
                if (pulse1.isEnabled()) channelFlags |= 0x01;
                if (pulse2.isEnabled()) channelFlags |= 0x02;
                if (waveChannel.isEnabled()) channelFlags |= 0x04;
                if (noiseChannel.isEnabled()) channelFlags |= 0x08;
                return (byte) ((regs[a] & 0x80) | 0x70 | channelFlags);
            }

            return (byte) switch (addr){
                   // case HardwareRegisters.NR13.addr, HardwareRegisters.NR23.addr, HardwareRegisters.NR31.addr, HardwareRegisters.NR33.addr, HardwareRegisters.NR41.addr -> 0xFF;
                    case 0x13,0x18,0x1B,0x1D,0x20 -> 0xFF;
                    case 0x10 -> regs[addr] | UNUSED_BITS_NR10;
                    case 0x11, 0x16 -> regs[addr] | UNUSED_BITS_NRX1;
                    case 0x14, 0x1E, 0x19, 0x23 -> regs[addr] | UNUSED_BITS_NRX4;
                    case 0x1A -> regs[addr] | UNUSED_BITS_NR30;
                    case 0x1C -> regs[addr] | UNUSED_BITS_NR32;
                    default -> regs[addr];

        };
    }
        return (byte) 0xFF;
        }

    @Override
    public void write(char addr, byte val) {
        int a = addr & 0xFF;
        if (a >= 0x30 && a <= 0x3F) {
            waveRam[a - 0x30] = val;
            return;
        }


            if (a == (HardwareRegisters.NR52.addr & 0xFF)) {
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

            regs[a] = val;
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
        }
        if(frameSequenncerStep == 7){
            pulse1.envelopeClock();
            pulse2.envelopeClock();
        }
        if(frameSequenncerStep == 2 || frameSequenncerStep == 6){
            pulse1.sweepClock();
        }

    }


    public void tick() {
        tCycles++;
        frameSequencer();
        pulse1.tick(1);
        pulse2.tick(1);
        waveChannel.tick(1);
        noiseChannel.tick(1);
    }
}
