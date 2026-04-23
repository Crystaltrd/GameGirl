package Controller;

public class LookAndFeelController {
    public static int scale = 3;


    public static int[][] tetrisPalette = {
            {0xFFFFFF, 0xFFFF00, 0xFF0000, 0x000000},
            {0xFFFFFF, 0xFFFF00, 0xFF0000, 0x000000},
            {0xFFFFFF, 0xFFFF00, 0xFF0000, 0x000000},
    };
    public static int[][] monochromePalette = {
            {0xFFFFFF, 0xAAAAAA, 0x555555, 0x000000},
            {0xFFFFFF, 0xAAAAAA, 0x555555, 0x000000},
            {0xFFFFFF, 0xAAAAAA, 0x555555, 0x000000},
    };

    public static int[][] kirbyPalette = {
            {0xA59CFF, 0xFFFF00, 0x006300, 0x000000},
            {0xFF6352, 0xD60000, 0x630000, 0x000000},
            {0x0000FF, 0xFFFFFF, 0xFFFF7B, 0x0084FF},
    };
    public static int[][] defaultPalette = monochromePalette;
    public static Palettes currPalette = Palettes.PAL_MONOCHROME;

    public static void cycleLeft() {
        switch (currPalette) {
            case PAL_MONOCHROME -> {
                currPalette = Palettes.PAL_TETRIS;
                defaultPalette = tetrisPalette;
            }
            case PAL_KIRBY -> {
                currPalette = Palettes.PAL_MONOCHROME;
                defaultPalette = monochromePalette;
            }
            case PAL_TETRIS -> {
                currPalette = Palettes.PAL_KIRBY;
                defaultPalette = kirbyPalette;
            }
        }
    }

    public static void cycleRight() {
        switch (currPalette) {
            case PAL_MONOCHROME -> {
                currPalette = Palettes.PAL_KIRBY;
                defaultPalette = kirbyPalette;
            }
            case PAL_KIRBY -> {
                currPalette = Palettes.PAL_TETRIS;
                defaultPalette = tetrisPalette;
            }
            case PAL_TETRIS -> {
                currPalette = Palettes.PAL_MONOCHROME;
                defaultPalette = monochromePalette;
            }
        }
    }

    enum Palettes {
        PAL_MONOCHROME,
        PAL_KIRBY,
        PAL_TETRIS
    }
}
