package Model;

public class CatridgeLUTable {
    public static CartridgeTypeInfo getCartridgeTypeInfo(int code) {
        return switch (code) {
            case 0x00 -> new CartridgeTypeInfo("ROM ONLY", MBC_TYPES.NO_MBC, false, false, false, false, false);
            case 0x01 -> new CartridgeTypeInfo("MBC1", MBC_TYPES.MBC_1, false, false, false, false, false);
            case 0x02 -> new CartridgeTypeInfo("MBC1+RAM", MBC_TYPES.MBC_1, false, true, false, false, false);
            case 0x03 -> new CartridgeTypeInfo("MBC1+RAM+BATTERY", MBC_TYPES.MBC_1, true, true, false, false, false);
            case 0x05 -> new CartridgeTypeInfo("MBC2", MBC_TYPES.MBC_2, false, false, false, false, false);
            case 0x06 -> new CartridgeTypeInfo("MBC2+BATTERY", MBC_TYPES.MBC_2, true, false, false, false, false);
            case 0x08 -> new CartridgeTypeInfo("ROM+RAM", MBC_TYPES.NO_MBC, false, true, false, false, false);
            case 0x09 -> new CartridgeTypeInfo("ROM+RAM+BATTERY", MBC_TYPES.NO_MBC, true, true, false, false, false);
            case 0x0B -> new CartridgeTypeInfo("MMM01", MBC_TYPES.MMM01, false, false, false, false, false);
            case 0x0C -> new CartridgeTypeInfo("MMM01+RAM", MBC_TYPES.MMM01, false, true, false, false, false);
            case 0x0D -> new CartridgeTypeInfo("MMM01+RAM+BATTERY", MBC_TYPES.MMM01, true, true, false, false, false);
            case 0x0F -> new CartridgeTypeInfo("MBC3+TIMER+BATTERY", MBC_TYPES.MBC_3, true, false, true, false, false);
            case 0x10 ->
                    new CartridgeTypeInfo("MBC3+TIMER+RAM+BATTERY", MBC_TYPES.MBC_3, true, true, true, false, false);
            case 0x11 -> new CartridgeTypeInfo("MBC3", MBC_TYPES.MBC_3, false, false, false, false, false);
            case 0x12 -> new CartridgeTypeInfo("MBC3+RAM", MBC_TYPES.MBC_3, false, true, false, false, false);
            case 0x13 -> new CartridgeTypeInfo("MBC3+RAM+BATTERY", MBC_TYPES.MBC_3, true, true, false, false, false);
            case 0x19 -> new CartridgeTypeInfo("MBC5", MBC_TYPES.MBC_5, false, false, false, false, false);
            case 0x1A -> new CartridgeTypeInfo("MBC5+RAM", MBC_TYPES.MBC_5, false, true, false, false, false);
            case 0x1B -> new CartridgeTypeInfo("MBC5+RAM+BATTERY", MBC_TYPES.MBC_5, true, true, false, false, false);
            case 0x1C -> new CartridgeTypeInfo("MBC5+RUMBLE", MBC_TYPES.MBC_5, false, false, false, true, false);
            case 0x1D -> new CartridgeTypeInfo("MBC5+RUMBLE+RAM", MBC_TYPES.MBC_5, false, true, false, true, false);
            case 0x1E ->
                    new CartridgeTypeInfo("MBC5+RUMBLE+RAM+BATTERY", MBC_TYPES.MBC_5, true, true, false, true, false);
            case 0x20 -> new CartridgeTypeInfo("MBC6", MBC_TYPES.MBC_6, false, true, false, false, false);
            case 0x22 ->
                    new CartridgeTypeInfo("MBC7+SENSOR+RUMBLE+RAM+BATTERY", MBC_TYPES.MBC_7, true, false, false, true, true);
            case 0xFC ->
                    new CartridgeTypeInfo("POCKET CAMERA", MBC_TYPES.POCKET_CAMERA, true, true, false, false, false);
            case 0xFD -> new CartridgeTypeInfo("TAMA5", null, true, false, false, false, false);
            case 0xFE -> new CartridgeTypeInfo("HuC3", MBC_TYPES.HUC3, true, true, false, false, false);
            case 0xFF -> new CartridgeTypeInfo("HuC1+RAM+BATTERY", MBC_TYPES.HUC1, true, true, false, false, false);
            default ->
                    new CartridgeTypeInfo(String.format("UNKNOWN (0x%02X)", code), null, false, false, false, false, false);
        };
    }

    public static String getCartridgeTypeName(int code) {
        return getCartridgeTypeInfo(code).name();
    }

    public static int getROMSizeBytes(int code) {
        return switch (code) {
            case 0x00 -> 32 * 1024;
            case 0x01 -> 64 * 1024;
            case 0x02 -> 128 * 1024;
            case 0x03 -> 256 * 1024;
            case 0x04 -> 512 * 1024;
            case 0x05 -> 1024 * 1024;
            case 0x06 -> 2 * 1024 * 1024;
            case 0x07 -> 4 * 1024 * 1024;
            case 0x08 -> 8 * 1024 * 1024;
            case 0x52 -> 72 * 16 * 1024;
            case 0x53 -> 80 * 16 * 1024;
            case 0x54 -> 96 * 16 * 1024;
            default -> 0;
        };
    }

    public static int getRAMSizeBytes(int code) {
        return switch (code) {
            case 0x00 -> 0;
            case 0x01 -> 0;
            case 0x02 -> 8 * 1024;
            case 0x03 -> 32 * 1024;
            case 0x04 -> 128 * 1024;
            case 0x05 -> 64 * 1024;
            default -> 0;
        };
    }

    public record CartridgeTypeInfo(String name,
                                    MBC_TYPES mbcType,
                                    boolean hasBattery,
                                    boolean hasRam,
                                    boolean hasTimer,
                                    boolean hasRumble,
                                    boolean hasSensor) {
    }

    public static String getOldLicensee(int n) {
        return switch (n) {
            case 0x00 -> "none";
            case 0x01 -> "nintendo";
            case 0x08 -> "capcom";
            case 0x09 -> "hot-b";
            case 0x0A -> "jaleco";
            case 0x0B -> "coconuts";
            case 0x0C -> "elite systems";
            case 0x13 -> "electronic arts";
            case 0x18 -> "hudsonsoft";
            case 0x19 -> "itc entertainment";
            case 0x1A -> "yanoman";
            case 0x1D -> "clary";
            case 0x1F -> "virgin";
            case 0x24 -> "pcm complete";
            case 0x25 -> "san-x";
            case 0x28 -> "kotobuki systems";
            case 0x29 -> "seta";
            case 0x30 -> "infogrames";
            case 0x31 -> "nintendo";
            case 0x32 -> "bandai";
            case 0x34 -> "konami";
            case 0x35 -> "hector";
            case 0x38 -> "capcom";
            case 0x39 -> "banpresto";
            case 0x3C -> "*entertainment i";
            case 0x3E -> "gremlin";
            case 0x41 -> "ubi soft";
            case 0x42 -> "atlus";
            case 0x44 -> "malibu";
            case 0x46 -> "angel";
            case 0x47 -> "spectrum holoby";
            case 0x49 -> "irem";
            case 0x4A -> "virgin";
            case 0x4D -> "malibu";
            case 0x4F -> "u.s. gold";
            case 0x50 -> "absolute";
            case 0x51 -> "acclaim";
            case 0x52 -> "activision";
            case 0x53 -> "american sammy";
            case 0x54 -> "gametek";
            case 0x55 -> "park place";
            case 0x56 -> "ljn";
            case 0x57 -> "matchbox";
            case 0x59 -> "milton bradley";
            case 0x5A -> "mindscape";
            case 0x5B -> "romstar";
            case 0x5C -> "naxat soft";
            case 0x5D -> "tradewest";
            case 0x60 -> "titus";
            case 0x61 -> "virgin";
            case 0x67 -> "ocean";
            case 0x69 -> "electronic arts";
            case 0x6E -> "elite systems";
            case 0x6F -> "electro brain";
            case 0x70 -> "infogrames";
            case 0x71 -> "interplay";
            case 0x72 -> "broderbund";
            case 0x73 -> "sculptered soft";
            case 0x75 -> "the sales curve";
            case 0x78 -> "t*hq";
            case 0x79 -> "accolade";
            case 0x7A -> "triffix entertainment";
            case 0x7C -> "microprose";
            case 0x7F -> "kemco";
            case 0x80 -> "misawa entertainment";
            case 0x83 -> "lozc";
            case 0x86 -> "*tokuma shoten i";
            case 0x8B -> "bullet-proof software";
            case 0x8C -> "vic tokai";
            case 0x8E -> "ape";
            case 0x8F -> "i'max";
            case 0x91 -> "chun soft";
            case 0x92 -> "video system";
            case 0x93 -> "tsuburava";
            case 0x95 -> "varie";
            case 0x96 -> "yonezawa/s'pal";
            case 0x97 -> "kaneko";
            case 0x99 -> "arc";
            case 0x9A -> "nihon bussan";
            case 0x9B -> "tecmo";
            case 0x9C -> "imagineer";
            case 0x9D -> "banpresto";
            case 0x9F -> "nova";
            case 0xA1 -> "hori electric";
            case 0xA2 -> "bandai";
            case 0xA4 -> "konami";
            case 0xA6 -> "kawada";
            case 0xA7 -> "takara";
            case 0xA9 -> "technos japan";
            case 0xAA -> "broderbund";
            case 0xAC -> "toei animation";
            case 0xAD -> "toho";
            case 0xAF -> "namco";
            case 0xB0 -> "acclaim";
            case 0xB1 -> "ascii or nexoft";
            case 0xB2 -> "bandai";
            case 0xB4 -> "enix";
            case 0xB6 -> "hal";
            case 0xB7 -> "snk";
            case 0xB9 -> "pony canyon";
            case 0xBA -> "*culture brain o";
            case 0xBB -> "sunsoft";
            case 0xBD -> "sony imagesoft";
            case 0xBF -> "sammy";
            case 0xC0 -> "taito";
            case 0xC2 -> "kemco";
            case 0xC3 -> "squaresoft";
            case 0xC4 -> "*tokuma shoten i";
            case 0xC5 -> "data east";
            case 0xC6 -> "tonkin house";
            case 0xC8 -> "koei";
            case 0xC9 -> "ufl";
            case 0xCA -> "ultra";
            case 0xCB -> "vap";
            case 0xCC -> "use";
            case 0xCD -> "meldac";
            case 0xCE -> "*pony canyon or";
            case 0xCF -> "angel";
            case 0xD0 -> "taito";
            case 0xD1 -> "sofel";
            case 0xD2 -> "quest";
            case 0xD3 -> "sigma enterprises";
            case 0xD4 -> "ask kodansha";
            case 0xD6 -> "naxat soft";
            case 0xD7 -> "copya systems";
            case 0xD9 -> "banpresto";
            case 0xDA -> "tomy";
            case 0xDB -> "ljn";
            case 0xDD -> "ncs";
            case 0xDE -> "human";
            case 0xDF -> "altron";
            case 0xE0 -> "jaleco";
            case 0xE1 -> "towachiki";
            case 0xE2 -> "uutaka";
            case 0xE3 -> "varie";
            case 0xE5 -> "epoch";
            case 0xE7 -> "athena";
            case 0xE8 -> "asmik";
            case 0xE9 -> "natsume";
            case 0xEA -> "king records";
            case 0xEB -> "atlus";
            case 0xEC -> "epic/sony records";
            case 0xEE -> "igs";
            case 0xF0 -> "a wave";
            case 0xF3 -> "extreme entertainment";
            case 0xFF -> "ljn";
            default -> "<UNKNOWN>";
        };
    }

    public static String getNewLicensee(int n) {
        return switch (n) {
            case 0 -> "none";
            case 1 -> "nintendo";
            case 8 -> "capcom";
            case 13 -> "electronic arts";
            case 18 -> "hudsonsoft";
            case 19 -> "b-ai";
            case 20 -> "kss";
            case 22 -> "pow";
            case 24 -> "pcm complete";
            case 25 -> "san-x";
            case 28 -> "kemco japan";
            case 29 -> "seta";
            case 30 -> "viacom";
            case 31 -> "nintendo";
            case 32 -> "bandia";
            case 33 -> "ocean/acclaim";
            case 34 -> "konami";
            case 35 -> "hector";
            case 37 -> "taito";
            case 38 -> "hudson";
            case 39 -> "banpresto";
            case 41 -> "ubi soft";
            case 42 -> "atlus";
            case 44 -> "malibu";
            case 46 -> "angel";
            case 47 -> "pullet-proof";
            case 49 -> "irem";
            case 50 -> "absolute";
            case 51 -> "acclaim";
            case 52 -> "activision";
            case 53 -> "american sammy";
            case 54 -> "konami";
            case 55 -> "hi tech entertainment";
            case 56 -> "ljn";
            case 57 -> "matchbox";
            case 58 -> "mattel";
            case 59 -> "milton bradley";
            case 60 -> "titus";
            case 61 -> "virgin";
            case 64 -> "lucasarts";
            case 67 -> "ocean";
            case 69 -> "electronic arts";
            case 70 -> "infogrames";
            case 71 -> "interplay";
            case 72 -> "broderbund";
            case 73 -> "sculptured";
            case 75 -> "sci";
            case 78 -> "t*hq";
            case 79 -> "accolade";
            case 80 -> "misawa";
            case 83 -> "lozc";
            case 86 -> "tokuma shoten i*";
            case 87 -> "tsukuda ori*";
            case 91 -> "chun soft";
            case 92 -> "video system";
            case 93 -> "ocean/acclaim";
            case 95 -> "varie";
            case 96 -> "yonezawa/s'pal";
            case 97 -> "kaneko";
            case 99 -> "pack in soft";
            default -> "<UNKNOWN>";
        };
    }
}
