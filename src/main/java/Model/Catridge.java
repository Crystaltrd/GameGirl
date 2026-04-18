package Model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Catridge extends BusMemory {
    public final byte[] data;
    private final byte[] title_raw = new byte[0x143 - 0x134 + 1];
    private final byte[] new_licensee_code_raw = new byte[0x145 - 0x144 + 1];
    private final int sgb_flag_raw;
    private final int cartridge_type_raw;
    private final int ROM_size_raw;
    private final int RAM_size_raw;
    private final int region_code_raw;
    private final int old_licensee_code_raw;
    private final int ROM_ver_raw;
    private final int headerROMSizeBytes;
    private final int headerRAMSizeBytes;
    private final CatridgeLUTable.CartridgeTypeInfo cartridgeTypeInfo;
    private final MemoryBankController mbc;

    public Catridge(InputStream romFile) throws IOException {
        data = romFile.readAllBytes();
        if (data.length <= 0x14C) {
            throw new IllegalArgumentException("ROM file is too small to contain a valid Game Boy header");
        }

        System.arraycopy(data, 0x0134, title_raw, 0, title_raw.length);
        System.arraycopy(data, 0x0144, new_licensee_code_raw, 0, new_licensee_code_raw.length);
        sgb_flag_raw = Byte.toUnsignedInt(data[0x0146]);
        cartridge_type_raw = Byte.toUnsignedInt(data[0x0147]);
        ROM_size_raw = Byte.toUnsignedInt(data[0x0148]);
        RAM_size_raw = Byte.toUnsignedInt(data[0x0149]);
        region_code_raw = Byte.toUnsignedInt(data[0x014A]);
        old_licensee_code_raw = Byte.toUnsignedInt(data[0x014B]);
        ROM_ver_raw = Byte.toUnsignedInt(data[0x014C]);

        cartridgeTypeInfo = CatridgeLUTable.getCartridgeTypeInfo(cartridge_type_raw);
        if (cartridgeTypeInfo.mbcType() == null) {
            throw new IllegalArgumentException("Unsupported cartridge type: " + cartridgeTypeInfo.name());
        }

        headerROMSizeBytes = CatridgeLUTable.getROMSizeBytes(ROM_size_raw);
        headerRAMSizeBytes = CatridgeLUTable.getRAMSizeBytes(RAM_size_raw);
        mbc = MemoryBankController.getInstance(this, cartridgeTypeInfo, resolvedROMSizeBytes(), resolvedRAMSizeBytes());
    }

    public String getTitle() {
        String title = new String(title_raw, StandardCharsets.US_ASCII);
        int end = title.indexOf('\0');
        return (end >= 0 ? title.substring(0, end) : title).trim();
    }

    public String getLicensee() {
        if (old_licensee_code_raw == 0x33) {
            String code = new String(new_licensee_code_raw, StandardCharsets.US_ASCII).trim();
            try {
                return CatridgeLUTable.getNewLicensee(Integer.parseInt(code, 16));
            } catch (NumberFormatException e) {
                return code;
            }
        }
        return CatridgeLUTable.getOldLicensee(old_licensee_code_raw);
    }

    public String getType() {
        return cartridgeTypeInfo.name();
    }

    public boolean getSGBFlag() {
        return sgb_flag_raw == 0x03;
    }

    public int getROMSize() {
        return headerROMSizeBytes == 0 ? data.length / 1024 : headerROMSizeBytes / 1024;
    }

    public int getRAMSize() {
        return resolvedRAMSizeBytes() / 1024;
    }

    public String getRegion() {
        return region_code_raw == 0 ? "JP/Global" : "Global Only";
    }

    public int getROMVer() {
        return ROM_ver_raw;
    }

    @Override
    public int read(int address) {
        return mbc.read(address);
    }

    @Override
    public void write(int address, int value) {
        mbc.write(address, value);
    }

    public int readRAM(int address) {
        return mbc.readRAM(address);
    }

    public void writeRAM(int address, int value) {
        mbc.writeRAM(address, value);
    }

    private int resolvedROMSizeBytes() {
        return headerROMSizeBytes == 0 ? data.length : Math.max(headerROMSizeBytes, data.length);
    }

    private int resolvedRAMSizeBytes() {
        return switch (cartridgeTypeInfo.mbcType()) {
            case MBC_6, HUC3 -> Math.max(headerRAMSizeBytes, 32 * 1024);
            case POCKET_CAMERA -> Math.max(headerRAMSizeBytes, 128 * 1024);
            default -> cartridgeTypeInfo.hasRam() ? headerRAMSizeBytes : 0;
        };
    }
}
