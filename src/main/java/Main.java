
public class Main {
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                new Emu(Main.class.getResourceAsStream(args[0]),false,true,false);
            } else {
                new Emu(Main.class.getResourceAsStream("/ROMs/tetris.gb"),false,true,false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}