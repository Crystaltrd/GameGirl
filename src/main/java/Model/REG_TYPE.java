package Model;

public enum REG_TYPE {
    RT_A,
    RT_AF,
    RT_B,
    RT_BC,
    RT_C,
    RT_D,
    RT_DE,
    RT_E,
    RT_F,
    RT_H,
    RT_HL,
    RT_L,
    RT_NONE,
    RT_PC,
    RT_SP;

    public static REG_TYPE decodeReg(int val) {
        return switch (val) {
            case 0 -> RT_B;
            case 1 -> RT_C;
            case 2 -> RT_D;
            case 3 -> RT_E;
            case 4 -> RT_H;
            case 5 -> RT_L;
            case 6 -> RT_HL;
            case 7 -> RT_A;
            default -> RT_NONE;
        };
    }
}
