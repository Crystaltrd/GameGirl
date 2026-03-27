import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

class CartridgeHeader {

    public byte[] entry_point_raw = new byte[0x103 - 0x100 + 1];
    public byte[] logo_raw = new byte[0x133 - 0x104 + 1];
    public byte[] title_raw = new byte[0x143 - 0x134 + 1];
    public byte[] new_licensee_code_raw = new byte[0x145 - 0x144 + 1];
    public byte sgb_flag_raw;
    public byte cartridge_type_raw;
    public byte ROM_size_raw;
    public byte RAM_size_raw;
    public byte region_code_raw;
    public byte old_licensee_code_raw;
    public byte ROM_ver_raw;
    public byte header_checksum_raw;
    public boolean checksum_passed;
    public byte[] global_checksum_raw = new byte[0x014F - 0x014E + 1];


    CartridgeHeader(byte[] data) {
        System.arraycopy(data, 0x0100, entry_point_raw, 0, entry_point_raw.length);
        System.arraycopy(data, 0x0104, logo_raw, 0, logo_raw.length);
        System.arraycopy(data, 0x0134, title_raw, 0, title_raw.length);
        System.arraycopy(data, 0x0144, new_licensee_code_raw, 0, new_licensee_code_raw.length);
        sgb_flag_raw = data[0x0146];
        cartridge_type_raw = data[0x0147];
        ROM_size_raw = data[0x0148];
        RAM_size_raw = data[0x0149];
        region_code_raw = data[0x014A];
        old_licensee_code_raw = data[0x014B];
        ROM_ver_raw = data[0x014C];
        header_checksum_raw = data[0x014D];
        System.arraycopy(data, 0x014E, global_checksum_raw, 0, global_checksum_raw.length);
        byte checksum = 0;
        for (int address = 0x0134; address <= 0x014C; address++) {
            checksum = (byte) (checksum - data[address] - 1);
        }
        checksum_passed = checksum == header_checksum_raw;
    }


    @Override
    public String toString() {
        HexFormat commaFormat = HexFormat.ofDelimiter(" ").withPrefix("");
        HexFormat hex = HexFormat.of();
        return "CartridgeHeader{" +
                "entry_point_raw=" + commaFormat.formatHex(entry_point_raw) +
                ", logo_raw=" + commaFormat.formatHex(logo_raw) +
                ", title_raw=" + commaFormat.formatHex(title_raw) +
                ", new_licensee_code_raw=" + commaFormat.formatHex(new_licensee_code_raw) +
                ", sgb_flag_raw=" + hex.toHexDigits(sgb_flag_raw) +
                ", cartridge_type_raw=" + hex.toHexDigits(cartridge_type_raw) +
                ", ROM_size_raw=" + hex.toHexDigits(ROM_size_raw) +
                ", RAM_size_raw=" + hex.toHexDigits(RAM_size_raw) +
                ", region_code_raw=" + hex.toHexDigits(region_code_raw) +
                ", old_licensee_code_raw=" + hex.toHexDigits(old_licensee_code_raw) +
                ", ROM_ver_raw=" + hex.toHexDigits(ROM_ver_raw) +
                ", header_checksum_raw=" + hex.toHexDigits(header_checksum_raw) +
                ", global_checksum_raw=" + commaFormat.formatHex(global_checksum_raw) +
                '}';
    }

    public String humanReadable() {
        String licensee;
        String licensee_code;
        if (old_licensee_code_raw == 0x33) {
            String new_licensee_str = new String(new_licensee_code_raw, StandardCharsets.US_ASCII);
            licensee_code = new_licensee_str;
            licensee = EmuLookupTables.new_licensees.getOrDefault(new_licensee_str, "<UNKNOWN>");
        } else {
            licensee_code = String.format("0x%1$02X", old_licensee_code_raw);
            licensee = EmuLookupTables.old_licensees.getOrDefault(licensee_code, "<UNKNOWN>");
        }
        return ("Title: " + new String(title_raw, StandardCharsets.US_ASCII) +
                "\nLicensee: " + licensee + "(" + licensee_code + ")" +
                "\nCartridge Type: " + EmuLookupTables.cartridge_types[cartridge_type_raw] + "(0x" + String.format("%1$02X", cartridge_type_raw) + ")" +
                "\nSGB flag: " + (sgb_flag_raw == 0x3 ? "SET" : "NOT SET") +
                "\nROM Size: " + (32 * (1 << ROM_size_raw)) + " KiB (" + ((1 << ROM_size_raw) * 2) + " Banks)" +
                "\nRAM Size: " + EmuLookupTables.RAM_sizes.getOrDefault((int) RAM_size_raw, 0) + " KiB" +
                "\nRegion: " + ((int) region_code_raw == 0 ? "JP/Global" : "Global Only") +
                "\nROM Ver: " + (int) ROM_ver_raw + " (0x" + String.format("%1$02X", ROM_ver_raw) + ")" +
                "\nHeader Checksum: " + "0x" + String.format("%1$02X", header_checksum_raw) + (checksum_passed ? " (PASSED)" : "(FAILED)")).replace("\0","");
    }
}

public class Cartridge {
    CartridgeHeader header;
    public byte[] data;

    Cartridge(File rom_file) throws IOException {
        data = Files.readAllBytes(rom_file.toPath());
        header = new CartridgeHeader(data);
    }
    
    public byte read(char address){
        return data[address];
    }
    public void write(char address,byte value){
        data[address] = value;
    } 
}
