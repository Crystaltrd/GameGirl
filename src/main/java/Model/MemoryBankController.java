package Model;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Setter
@Getter
public abstract class MemoryBankController {
    protected static final int OPEN_BUS = 0xFF;
    protected static final int ROM_BANK_SIZE_16K = 0x4000;
    protected static final int ROM_BANK_SIZE_8K = 0x2000;
    protected static final int RAM_BANK_SIZE_8K = 0x2000;

    protected final int ROMsz;
    protected final int RAMsz;
    protected final boolean battery;
    protected final Catridge catridge;
    protected final CatridgeLUTable.CartridgeTypeInfo cartridgeType;
    protected boolean RAMEnabled = false;
    protected final int[] externalRAM;

    MemoryBankController(Catridge catridge, CatridgeLUTable.CartridgeTypeInfo cartridgeType, int ROMsz, int RAMsz) {
        this.ROMsz = ROMsz;
        this.RAMsz = RAMsz;
        this.battery = cartridgeType.hasBattery();
        this.catridge = catridge;
        this.cartridgeType = cartridgeType;
        this.externalRAM = new int[Math.max(RAMsz, 0)];
    }

    public static MemoryBankController getInstance(Catridge catridge,
                                                   CatridgeLUTable.CartridgeTypeInfo cartridgeType,
                                                   int ROMsz,
                                                   int RAMsz) {
        return switch (cartridgeType.mbcType()) {
            case MBC_1 -> new MBC1(catridge, cartridgeType, ROMsz, RAMsz);
            case MBC_2 -> new MBC2(catridge, cartridgeType, ROMsz, RAMsz);
            case MBC_3 -> new MBC3(catridge, cartridgeType, ROMsz, RAMsz);
            case MBC_5 -> new MBC5(catridge, cartridgeType, ROMsz, RAMsz);
            default -> new NoMBC(catridge, cartridgeType, ROMsz, RAMsz);
        };
    }

    protected static int bitLength(int value) {
        int positive = Math.max(value, 0);
        return positive == 0 ? 1 : Integer.SIZE - Integer.numberOfLeadingZeros(positive);
    }

    protected static int bitMask(int bits) {
        return bits <= 0 ? 0 : (1 << bits) - 1;
    }

    protected boolean ramEnableMatches(int value) {
        return (value & 0x0F) == 0x0A;
    }

    protected int readRawRom(int absoluteAddress) {
        if (absoluteAddress < 0 || absoluteAddress >= catridge.data.length) {
            return OPEN_BUS;
        }
        return catridge.data[absoluteAddress] & 0xFF;
    }

    protected int get16kRomBankCount() {
        return Math.max(1, (catridge.data.length + ROM_BANK_SIZE_16K - 1) / ROM_BANK_SIZE_16K);
    }

    protected int get8kRomBankCount() {
        return Math.max(1, (catridge.data.length + ROM_BANK_SIZE_8K - 1) / ROM_BANK_SIZE_8K);
    }

    protected int normalizeBank(int bank, int bankCount) {
        return bankCount <= 0 ? 0 : Math.floorMod(bank, bankCount);
    }

    protected int readRom16kBank(int bank, int address) {
        int absolute = normalizeBank(bank, get16kRomBankCount()) * ROM_BANK_SIZE_16K + (address & 0x3FFF);
        return readRawRom(absolute);
    }

    protected int readExternalRamWindow(int bank, int address, boolean enabled) {
        if (!enabled || externalRAM.length == 0) {
            return OPEN_BUS;
        }
        int absolute = Math.floorMod(bank * MemoryBankController.RAM_BANK_SIZE_8K + ((address - 40960) & (MemoryBankController.RAM_BANK_SIZE_8K - 1)), externalRAM.length);
        return externalRAM[absolute] & 0xFF;
    }

    protected void writeExternalRamWindow(int bank, int address, int value, boolean enabled) {
        if (!enabled || externalRAM.length == 0) {
            return;
        }
        int absolute = Math.floorMod(bank * MemoryBankController.RAM_BANK_SIZE_8K + ((address - 40960) & (MemoryBankController.RAM_BANK_SIZE_8K - 1)), externalRAM.length);
        externalRAM[absolute] = value & 0xFF;
    }

    public abstract int readRAM(int address);

    public abstract void writeRAM(int address, int value);

    public abstract int read(int address);

    public abstract void write(int address, int value);
}

class NoMBC extends MemoryBankController {
    NoMBC(Catridge catridge, CatridgeLUTable.CartridgeTypeInfo cartridgeType, int ROMsz, int RAMsz) {
        super(catridge, cartridgeType, ROMsz, RAMsz);
    }

    @Override
    public int readRAM(int address) {
        return readExternalRamWindow(0, address, externalRAM.length > 0);
    }

    @Override
    public void writeRAM(int address, int value) {
        writeExternalRamWindow(0, address, value, externalRAM.length > 0);
    }

    @Override
    public int read(int address) {
        return readRawRom(address);
    }

    @Override
    public void write(int address, int value) {
    }
}

class MBC1 extends MemoryBankController {
    private final boolean multicart;
    private final int lowBankMask;
    private int romBankRegister = 0;
    private int upperBankRegister = 0;
    private int bankingMode = 0;

    MBC1(Catridge catridge, CatridgeLUTable.CartridgeTypeInfo cartridgeType, int ROMsz, int RAMsz) {
        super(catridge, cartridgeType, ROMsz, RAMsz);
        this.multicart = detectMbc1Multicart(catridge.data);
        int maxLowBits = multicart ? 4 : 5;
        this.lowBankMask = bitMask(Math.min(maxLowBits, bitLength(get16kRomBankCount() - 1)));
    }

    private static boolean detectMbc1Multicart(byte[] romData) {
        int candidateOffset = 0x10 * ROM_BANK_SIZE_16K + 0x0104;
        if (romData.length < candidateOffset + (0x0134 - 0x0104)) {
            return false;
        }
        byte[] logo = Arrays.copyOfRange(romData, 0x0104, 0x0134);
        byte[] candidate = Arrays.copyOfRange(romData, candidateOffset, candidateOffset + logo.length);
        return Arrays.equals(logo, candidate);
    }

    @Override
    public int readRAM(int address) {
        return readExternalRamWindow(selectedRamBank(), address, RAMEnabled);
    }

    @Override
    public void writeRAM(int address, int value) {
        writeExternalRamWindow(selectedRamBank(), address, value, RAMEnabled);
    }

    @Override
    public int read(int address) {
        if (Commons.isBetween(address, 0x0000, 0x3FFF)) {
            return readRom16kBank(selectedFixedRomBank(), address);
        }
        return readRom16kBank(selectedBankedRomBank(), address);
    }

    @Override
    public void write(int address, int value) {
        value &= 0xFF;
        if (Commons.isBetween(address, 0x0000, 0x1FFF)) {
            RAMEnabled = ramEnableMatches(value);
        } else if (Commons.isBetween(address, 0x2000, 0x3FFF)) {
            romBankRegister = value & 0x1F;
        } else if (Commons.isBetween(address, 0x4000, 0x5FFF)) {
            upperBankRegister = value & 0x03;
        } else if (Commons.isBetween(address, 0x6000, 0x7FFF)) {
            bankingMode = value & 0x01;
        }
    }

    private boolean upperBitsDriveRom() {
        return multicart ? get16kRomBankCount() > 16 : get16kRomBankCount() > 32;
    }

    private boolean upperBitsDriveRam() {
        return RAMsz >= 32 * 1024 && !upperBitsDriveRom();
    }

    private int translatedRomBankRegister() {
        int raw = romBankRegister & 0x1F;
        return raw == 0 ? 1 : raw;
    }

    private int selectedFixedRomBank() {
        if (bankingMode == 1 && upperBitsDriveRom()) {
            return multicart ? (upperBankRegister << 4) : (upperBankRegister << 5);
        }
        return 0;
    }

    private int selectedBankedRomBank() {
        int low = translatedRomBankRegister() & lowBankMask;
        if (multicart) {
            return ((upperBankRegister & 0x03) << 4) | (low & 0x0F);
        }
        int high = upperBitsDriveRom() ? (upperBankRegister & 0x03) << 5 : 0;
        return high | low;
    }

    private int selectedRamBank() {
        if (upperBitsDriveRam() && bankingMode == 1) {
            return upperBankRegister & 0x03;
        }
        return 0;
    }
}

class MBC2 extends MemoryBankController {
    private final int[] builtInRAM = new int[0x200];
    private int romBank = 1;

    MBC2(Catridge catridge, CatridgeLUTable.CartridgeTypeInfo cartridgeType, int ROMsz, int RAMsz) {
        super(catridge, cartridgeType, ROMsz, RAMsz);
    }

    @Override
    public int readRAM(int address) {
        if (!RAMEnabled) {
            return OPEN_BUS;
        }
        return 0xF0 | (builtInRAM[address & 0x01FF] & 0x0F);
    }

    @Override
    public void writeRAM(int address, int value) {
        if (!RAMEnabled) {
            return;
        }
        builtInRAM[address & 0x01FF] = value & 0x0F;
    }

    @Override
    public int read(int address) {
        if (Commons.isBetween(address, 0x0000, 0x3FFF)) {
            return readRawRom(address);
        }
        return readRom16kBank(romBank, address);
    }

    @Override
    public void write(int address, int value) {
        value &= 0xFF;
        if (Commons.isBetween(address, 0x0000, 0x1FFF) && (address & 0x0100) == 0) {
            RAMEnabled = ramEnableMatches(value);
        } else if (Commons.isBetween(address, 0x2000, 0x3FFF) && (address & 0x0100) != 0) {
            romBank = value & 0x0F;
            if (romBank == 0) {
                romBank = 1;
            }
        }
    }
}

class MBC3 extends MemoryBankController {
    private final Mbc3Rtc rtc = new Mbc3Rtc();
    private final boolean hasTimer;
    private int romBank = 1;
    private int ramOrRtcSelect = 0;
    private int latchWrite = 0xFF;

    MBC3(Catridge catridge, CatridgeLUTable.CartridgeTypeInfo cartridgeType, int ROMsz, int RAMsz) {
        super(catridge, cartridgeType, ROMsz, RAMsz);
        this.hasTimer = cartridgeType.hasTimer();
    }

    @Override
    public int readRAM(int address) {
        if (!RAMEnabled) {
            return OPEN_BUS;
        }
        if (ramOrRtcSelect <= 0x07) {
            return readExternalRamWindow(ramOrRtcSelect, address, true);
        }
        if (hasTimer && Commons.isBetween(ramOrRtcSelect, 0x08, 0x0C)) {
            return rtc.readLatched(ramOrRtcSelect);
        }
        return OPEN_BUS;
    }

    @Override
    public void writeRAM(int address, int value) {
        value &= 0xFF;
        if (!RAMEnabled) {
            return;
        }
        if (ramOrRtcSelect <= 0x07) {
            writeExternalRamWindow(ramOrRtcSelect, address, value, true);
        } else if (hasTimer && Commons.isBetween(ramOrRtcSelect, 0x08, 0x0C)) {
            rtc.write(ramOrRtcSelect, value);
        }
    }

    @Override
    public int read(int address) {
        if (Commons.isBetween(address, 0x0000, 0x3FFF)) {
            return readRawRom(address);
        }
        return readRom16kBank(romBank, address);
    }

    @Override
    public void write(int address, int value) {
        value &= 0xFF;
        if (Commons.isBetween(address, 0x0000, 0x1FFF)) {
            RAMEnabled = ramEnableMatches(value);
        } else if (Commons.isBetween(address, 0x2000, 0x3FFF)) {
            romBank = value & 0x7F;
            if (romBank == 0) {
                romBank = 1;
            }
        } else if (Commons.isBetween(address, 0x4000, 0x5FFF)) {
            ramOrRtcSelect = value & 0x0F;
        } else if (Commons.isBetween(address, 0x6000, 0x7FFF)) {
            int latchBit = value & 0x01;
            if (latchWrite == 0 && latchBit == 1) {
                rtc.latch();
            }
            latchWrite = latchBit;
        }
    }
}

class MBC5 extends MemoryBankController {
    private int romBank = 1;
    private int ramBank = 0;
    private boolean rumbleEnabled = false;

    MBC5(Catridge catridge, CatridgeLUTable.CartridgeTypeInfo cartridgeType, int ROMsz, int RAMsz) {
        super(catridge, cartridgeType, ROMsz, RAMsz);
    }

    @Override
    public int readRAM(int address) {
        return readExternalRamWindow(ramBank, address, RAMEnabled);
    }

    @Override
    public void writeRAM(int address, int value) {
        writeExternalRamWindow(ramBank, address, value, RAMEnabled);
    }

    @Override
    public int read(int address) {
        if (Commons.isBetween(address, 0x0000, 0x3FFF)) {
            return readRawRom(address);
        }
        return readRom16kBank(romBank, address);
    }

    @Override
    public void write(int address, int value) {
        value &= 0xFF;
        if (Commons.isBetween(address, 0x0000, 0x1FFF)) {
            RAMEnabled = ramEnableMatches(value);
        } else if (Commons.isBetween(address, 0x2000, 0x2FFF)) {
            romBank = (romBank & 0x100) | value;
        } else if (Commons.isBetween(address, 0x3000, 0x3FFF)) {
            romBank = (romBank & 0x0FF) | ((value & 0x01) << 8);
        } else if (Commons.isBetween(address, 0x4000, 0x5FFF)) {
            rumbleEnabled = cartridgeType.hasRumble() && (value & 0x08) != 0;
            ramBank = value & (cartridgeType.hasRumble() ? 0x07 : 0x0F);
        }
    }
}

class Mbc3Rtc {
    private static final long SECONDS_PER_DAY = 24L * 60 * 60;
    private static final long WRAP_SECONDS = 512L * SECONDS_PER_DAY;
    private final int[] latchedRegisters = new int[5];
    private long totalSeconds = 0;
    private long lastUpdateMillis = System.currentTimeMillis();
    private boolean halted = false;
    private boolean carry = false;

    Mbc3Rtc() {
        refreshLatch();
    }

    public int readLatched(int registerId) {
        return latchedRegisters[registerId - 0x08] & 0xFF;
    }

    public void write(int registerId, int value) {
        value &= 0xFF;
        sync();

        int[] regs = snapshotRegisters();
        switch (registerId) {
            case 0x08 -> regs[0] = value % 60;
            case 0x09 -> regs[1] = value % 60;
            case 0x0A -> regs[2] = value % 24;
            case 0x0B -> regs[3] = value;
            case 0x0C -> regs[4] = value & 0xC1;
            default -> {
                return;
            }
        }

        long days = ((regs[4] & 0x01) << 8) | regs[3];
        totalSeconds = (days * SECONDS_PER_DAY)
                + (long) regs[2] * 3600
                + (long) regs[1] * 60
                + regs[0];
        if (totalSeconds >= WRAP_SECONDS) {
            totalSeconds %= WRAP_SECONDS;
            carry = true;
        } else {
            carry = (regs[4] & 0x80) != 0;
        }
        halted = (regs[4] & 0x40) != 0;
        lastUpdateMillis = System.currentTimeMillis();
        refreshLatch();
    }

    public void latch() {
        sync();
        refreshLatch();
    }

    private void sync() {
        if (halted) {
            return;
        }

        long now = System.currentTimeMillis();
        long elapsedSeconds = Math.max(0, (now - lastUpdateMillis) / 1000);
        if (elapsedSeconds == 0) {
            return;
        }

        totalSeconds += elapsedSeconds;
        if (totalSeconds >= WRAP_SECONDS) {
            totalSeconds %= WRAP_SECONDS;
            carry = true;
        }
        lastUpdateMillis += elapsedSeconds * 1000;
    }

    private void refreshLatch() {
        int[] regs = snapshotRegisters();
        System.arraycopy(regs, 0, latchedRegisters, 0, latchedRegisters.length);
    }

    private int[] snapshotRegisters() {
        long days = totalSeconds / SECONDS_PER_DAY;
        long remainder = totalSeconds % SECONDS_PER_DAY;
        return new int[]{
                (int) (remainder % 60),
                (int) ((remainder / 60) % 60),
                (int) (remainder / 3600),
                (int) (days & 0xFF),
                (int) (((days >> 8) & 0x01) | (halted ? 0x40 : 0) | (carry ? 0x80 : 0))
        };
    }
}

