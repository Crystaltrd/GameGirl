package Model;

import java.util.Arrays;

public class APU extends GBMemory {
    private final EmulationContext ctx;

    private final PulseChannels pulse1 = new PulseChannels(HardwareRegisters.NR10, HardwareRegisters.NR11, HardwareRegisters.NR12, HardwareRegisters.NR13, HardwareRegisters.NR14);
    private final PulseChannels pulse2 = new PulseChannels(null,HardwareRegisters.NR21, HardwareRegisters.NR22, HardwareRegisters.NR23, HardwareRegisters.NR24);
    private final byte[] regs = new byte[0x27];
    private final byte[] waveRam = new byte[0x10];
    private final WaveChannel waveChannel = new WaveChannel(this.waveRam, HardwareRegisters.NR30, HardwareRegisters.NR31, HardwareRegisters.NR32, HardwareRegisters.NR33, HardwareRegisters.NR34);
    private final NoiseChannel noiseChannel = new NoiseChannel(null,HardwareRegisters.NR41, HardwareRegisters.NR42, HardwareRegisters.NR43, HardwareRegisters.NR44);
    private long tCycles = 0;

    APU(EmulationContext ctx) {
        this.ctx = ctx;
        regs[HardwareRegisters.NR52.addr & 0xFF] = (byte) 0x80;
    }

    private boolean isEnabled() {
        return (regs[HardwareRegisters.NR52.addr & 0xFF] & 0x80) != 0;
    }

    private void powerOff() {
        Arrays.fill(regs, (byte) 0);
        Arrays.fill(waveRam, (byte) 0);
    }

    @Override
    public byte read(char addr) {
        int a = addr & 0xFF;
        if (a >= 0x30 && a <= 0x3F) {
            return waveRam[a - 0x30];
        }
        if (a >= 0x10 && a <= 0x26) {
            if (a == (HardwareRegisters.NR52.addr & 0xFF)) {
                int channelFlags = 0;
                if (pulse1.isEnabled()) channelFlags |= 0x01;
                if (pulse2.isEnabled()) channelFlags |= 0x02;
                if (waveChannel.isEnabled()) channelFlags |= 0x04;
                if (noiseChannel.isEnabled()) channelFlags |= 0x08;
                return (byte) ((regs[a] & 0x80) | 0x70 | channelFlags);
            }
            return regs[a];
        }
        return (byte) 0xFF;
    }

    @Override
    public void write(char addr, byte val) {
        int a = addr & 0xFF;
        if (a >= 0x30 && a <= 0x3F) {
            if (isEnabled()) {
                waveRam[a - 0x30] = val;
            }
            return;
        }
        if( a >= 0x10 && a<= 0x19){
            if(a<= 0x14){pulse1.write(addr,val);}
            else {pulse2.write(addr,val);}
        }
        else if (a >= 0x1A && a <= 0x1E) {
            waveChannel.write(addr, val);
        }
        else if (a >= 0x20 && a <= 0x23) {
            noiseChannel.write(addr, val);
        }

        if (a < 0x10 || a > 0x26) {
            return;
        }

        if (a == (HardwareRegisters.NR52.addr & 0xFF)) {
            boolean wasEnabled = isEnabled();
            regs[a] = (byte) (val & 0x80);
            if (wasEnabled && !isEnabled()) {
                powerOff();
            }
            return;
        }

        if (!isEnabled()) {
            return;
        }

        regs[a] = val;
    }

    public void tick() {
        tCycles++;
        pulse1.tick(1);
        pulse2.tick(1);
        waveChannel.tick(1);
        noiseChannel.tick(1);
    }
}
