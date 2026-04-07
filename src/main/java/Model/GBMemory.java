package Model;

public abstract class GBMemory {
    public byte[] data;

    abstract public void write(char addr, byte val);

    abstract public byte read(char addr);
}
