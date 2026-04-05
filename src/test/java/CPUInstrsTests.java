import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CPUInstrsTests {
    public static String prefix = "/cpu_instrs/";

    public void cpuInstrsTest(String path, int n) throws IOException {
        PrintStream stdout = System.out;
        ProcessBuilder pb = new ProcessBuilder("python3", Objects.requireNonNull(getClass().getResource("/gameboy-doctor/gameboy-doctor")).getPath(), "-", "cpu_instrs", Integer.toString(n));
        pb.redirectErrorStream(true);
        File errFile = new File("logs/error"+n);
        pb.redirectError(errFile);
        File logFile = new File("logs/log"+n);
        pb.redirectOutput(logFile);
        Process pr = pb.start();
        
        PrintStream processStream = new PrintStream(pr.getOutputStream());
        System.setOut(processStream);
        Emu emulator = new Emu(getClass().getResourceAsStream(prefix + path), true, false, false);
        while (emulator.emuStep()) {
            if (!pr.isAlive())
                break;
        }
        System.setOut(stdout);
        assertEquals(0, pr.exitValue());
    }

    @Test
    @Order(0)
    public void t01_special() throws IOException {
        cpuInstrsTest("01-special.gb",1);
    }

    @Test
    @Order(1)
    public void t02_interrupts() throws IOException {
        cpuInstrsTest("02-interrupts.gb",2);
    }

    @Test
    @Order(2)
    public void t03_op_sp_hl() throws IOException {
        cpuInstrsTest("03-op sp,hl.gb",3);
    }

    @Test
    @Order(3)
    public void t04_op_r_imm() throws IOException {
        cpuInstrsTest("04-op r,imm.gb",4);
    }

    @Test
    @Order(4)
    public void t05_op_rp() throws IOException {
        cpuInstrsTest("05-op rp.gb",5);
    }

    @Test
    @Order(5)
    public void t06_ld_r_r() throws IOException {
        cpuInstrsTest("06-ld r,r.gb",6);
    }

    @Test
    @Order(6)
    public void t07_jr_jp_call_ret_rst() throws IOException {
        cpuInstrsTest("07-jr,jp,call,ret,rst.gb",7);
    }

    @Test
    @Order(7)
    public void t08_misc_instrs() throws IOException {
        cpuInstrsTest("08-misc instrs.gb",8);
    }

    @Test
    @Order(8)
    public void t09_op_r_r() throws IOException {
        cpuInstrsTest("09-op r,r.gb",9);
    }

    @Test
    @Order(9)
    public void t10_bit_ops() throws IOException {
        cpuInstrsTest("10-bit ops.gb",10);
    }

    @Test
    @Order(10)
    public void t11_op_a_hl() throws IOException {
        cpuInstrsTest("ROMs/11-op a,(hl).gb", 11);
    }
}

