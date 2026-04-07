public enum InterruptSource {
    IT_VBLANK(1),
    IT_LCD_STAT(2),
    IT_TIMER(4),
    IT_SERIAL(8),
    IT_JOYPAD(16);
    private final int inter;

    InterruptSource(int inter) {
        this.inter = inter;
    }

    public int getValue() {
        return inter;
    }
}
