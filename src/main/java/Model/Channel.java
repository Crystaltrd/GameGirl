package Model;

public class Channel {
    private HardwareRegisters[] channelRegisters;
    public Channel(HardwareRegisters... registers){
        int i = 0 ;
        this.channelRegisters = new HardwareRegisters[registers.length];
        this.channelRegisters= registers.clone();
    }
}
