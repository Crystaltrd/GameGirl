package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class MemoryBankController {
    protected int ROMsz;
    protected int RAMsz;
    protected boolean battery;
    protected Catridge catridge;
    protected boolean RAMEnabled = false;

    MemoryBankController(Catridge catridge, int ROMsz, int RAMsz, boolean battery) {
        this.ROMsz = ROMsz;
        this.RAMsz = RAMsz;
        this.battery = battery;
        this.catridge = catridge;

    }

    public static MemoryBankController getInstance(Catridge catridge, MBC_TYPES mbcType, int ROMsz, int RAMsz, boolean battery) {
        return switch (mbcType) {
            case NO_MBC -> new NoMBC(catridge, ROMsz, RAMsz, battery);
            case MBC_1 -> null;
            case MBC_2 -> null;
            case MBC_3 -> null;
            case MBC_5 -> new MBC5(catridge, ROMsz, RAMsz, battery);
            case MBC_6 -> null;
            case MBC_7 -> null;
            case MMM01 -> null;
            case HUC1 -> null;
            case HUC3 -> null;
        };

    }

    public abstract int read(int address);

    public abstract void write(int address, int value);
}

class NoMBC extends MemoryBankController {

    NoMBC(Catridge catridge, int ROMsz, int RAMsz, boolean battery) {
        super(catridge, ROMsz, RAMsz, battery);
    }

    @Override
    public void write(int address, int value) {
    }

    @Override
    public int read(int address) {
        return catridge.data[address] & 0xFF;
    }
}

class MBC1 extends MemoryBankController {
    MBC1(Catridge catridge, int ROMsz, int RAMsz, boolean battery) {
        super(catridge, ROMsz, RAMsz, battery);
    }

    @Override
    public int read(int addr) {
        return 0;
    }

    @Override
    public void write(int addr, int value) {

    }
}

class MBC5 extends MemoryBankController {
    private int ROMBankNumber = 1;
    private int ninthBit = 0;

    MBC5(Catridge catridge, int ROMsz, int RAMsz, boolean battery) {
        super(catridge, ROMsz, RAMsz, battery);
    }

    @Override
    public int read(int address) {
        if (Commons.isBetween(address, 0, 0x3FFF)) {
            return catridge.data[address] & 0xFF;
        } else {
            int addr = address & 0x3FFF;
            addr |= (ROMBankNumber | ninthBit << 9) << 14;
            return catridge.data[addr] & 0xFF;
        }
    }

    @Override
    public void write(int address, int value) {
        if (Commons.isBetween(address, 0, 0x1FFF))
            RAMEnabled = value == 0x0A;
        else if (Commons.isBetween(address, 0x2000, 0x2FFF)) {
            ROMBankNumber = value & 0xFF;
        } else if (Commons.isBetween(address, 0x3000, 0x3FFF)) {
            ninthBit = value == 0 ? 0 : 1;
        }
    }
}