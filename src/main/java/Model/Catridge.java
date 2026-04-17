package Model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Catridge extends BusMemory {
    public final byte[] data;
    private final byte[] title_raw = new byte[0x143 - 0x134 + 1];
    private final byte[] new_licensee_code_raw = new byte[0x145 - 0x144 + 1];
    private final byte sgb_flag_raw;
    private final byte cartridge_type_raw;
    private final byte ROM_size_raw;
    private final byte RAM_size_raw;
    private final byte region_code_raw;
    private final byte old_licensee_code_raw;
    private final byte ROM_ver_raw;
    private MemoryBankController mbc;


    public Catridge(InputStream romFile) throws IOException {
        data = romFile.readAllBytes();
        System.arraycopy(data, 0x0134, title_raw, 0, title_raw.length);
        System.arraycopy(data, 0x0144, new_licensee_code_raw, 0, new_licensee_code_raw.length);
        sgb_flag_raw = data[0x0146];
        cartridge_type_raw = data[0x0147];
        ROM_size_raw = data[0x0148];
        RAM_size_raw = data[0x0149];
        region_code_raw = data[0x014A];
        old_licensee_code_raw = data[0x014B];
        ROM_ver_raw = data[0x014C];
        if (cartridge_type_raw == 0)
            mbc = MemoryBankController.getInstance(this, MBC_TYPES.NO_MBC, ROM_size_raw, RAM_size_raw, false);
        else if (Commons.isBetween(cartridge_type_raw, 0x19, 0x1E)) {
            mbc = MemoryBankController.getInstance(this, MBC_TYPES.MBC_5, RAM_size_raw, ROM_size_raw, false);
        }
    }

    public String getTitle() {
        return new String(title_raw, StandardCharsets.US_ASCII).replaceAll("[^a-zA-Z]", "");
    }

    public String getLicensee() {
        String licensee;
        if (old_licensee_code_raw == 0x33) {
            try {
                licensee = CatridgeLUTable.getNewLicensee(Integer.parseInt(new String(new_licensee_code_raw, StandardCharsets.US_ASCII)));
            } catch (NumberFormatException e) {
                licensee = new String(new_licensee_code_raw, StandardCharsets.US_ASCII);
            }
        } else {
            licensee = CatridgeLUTable.getOldLicensee(old_licensee_code_raw);
        }
        return licensee;
    }

    public String getType() {
        return CatridgeLUTable.cartridge_types[cartridge_type_raw];
    }

    public boolean getSGBFlag() {
        return sgb_flag_raw == 0x3;
    }

    public int getROMSize() {
        return 32 * (1 << ROM_size_raw);
    }

    public int getRAMSize() {
        return CatridgeLUTable.RAMSizes[RAM_size_raw];
    }

    public String getRegion() {
        return region_code_raw == 0 ? "JP/Global" : "Global Only";
    }

    public int getROMVer() {
        return ROM_ver_raw;
    }

    public int read(int address) {
        return mbc.read(address);
    }

    public void write(int address, int value) {
        mbc.write(address, value);
    }

}
