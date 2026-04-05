import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SampleTest {
    @Test
    public void sampleTest() {
        int a = 1;
        int b = 1;

        int c = a + b;
        assertEquals(c, 2);
    }
}
