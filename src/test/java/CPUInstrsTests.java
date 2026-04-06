import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CPUInstrsTests {
    public static String prefix = "/cpu_instrs/";

    @ParameterizedTest()
    @ValueSource(strings = {"01-special", "02-interrupts", "03-op sp,hl", "04-op r,imm", "05-op rp",
            "06-ld r,r", "07-jr,jp,call,ret,rst", "08-misc instrs", "09-op r,r", "10-bit ops", "11-op a,(hl)"})
    public void cpuInstrsTest(String path) throws IOException {
        PrintStream stdout = System.out;
        String n = path.substring(0, 2);
        ProcessBuilder pb = new ProcessBuilder("python3", Objects.requireNonNull(getClass().getResource("/gameboy-doctor/gameboy-doctor")).getPath(), "-", "cpu_instrs", n);
        pb.redirectErrorStream(true);
        File errFile = new File("logs/error"+n);
        pb.redirectError(errFile);
        File logFile = new File("logs/log"+n);
        pb.redirectOutput(logFile);
        Process pr = pb.start();
        
        PrintStream processStream = new PrintStream(pr.getOutputStream());
        System.setOut(processStream);
        Emu emulator = new Emu(getClass().getResourceAsStream(prefix + path + ".gb"), true, false, false);
        while (emulator.emuStep()) {
            if (!pr.isAlive())
                break;
        }
        System.setOut(stdout);
        assertEquals(0, pr.exitValue());
    }

}

