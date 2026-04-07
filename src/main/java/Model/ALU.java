package Model;

public class ALU {


    public static ALUResult addByteToReg(char op1, byte op2, boolean signed) {
        int result = (op1 & 0xFFFF) + (signed ? op2 : op2 & 0xFF);
        boolean carry = result >> 16 != 0;
        ALUResult operationResult = new ALUResult();
        operationResult.Carry = carry ? FlagOperation.SET : FlagOperation.RESET;
        operationResult.result = (char) (result & 0xFFFF);
        return operationResult;
    }

    public static ALUResult addByteToByte(byte op1, byte op2, boolean signed) {
        int result = (op1 & 0xFF) + (signed ? op2 : op2 & 0xFF);
        ALUResult operationResult = new ALUResult();
        operationResult.result = result;
        return operationResult;
    }

    public static ALUResult XOR(byte b1, byte b2) {

        byte result = (byte) (b1 ^ b2);
        return getAluResult(result, result == 0 ? FlagOperation.SET : FlagOperation.RESET,
                FlagOperation.RESET,
                FlagOperation.RESET,
                FlagOperation.RESET);
    }

    public static ALUResult OR(byte b1, byte b2) {

        byte result = (byte) (b1 | b2);
        return getAluResult(result, result == 0 ? FlagOperation.SET : FlagOperation.RESET,
                FlagOperation.RESET,
                FlagOperation.RESET,
                FlagOperation.RESET);

    }

    public static ALUResult AND(byte b1, byte b2) {

        byte result = (byte) (b1 & b2);
        return getAluResult(result, result == 0 ? FlagOperation.SET : FlagOperation.RESET,
                FlagOperation.SET,
                FlagOperation.RESET,
                FlagOperation.RESET);

    }

    public static ALUResult DEC(byte b1) {
        byte result = (byte) (b1 - 1);
        return getAluResult(result, result == 0 ? FlagOperation.SET : FlagOperation.RESET,
                (result & 0x0F) == 0x0F ? FlagOperation.SET : FlagOperation.RESET,
                FlagOperation.SET,
                FlagOperation.UNCHANGED);
    }

    public static ALUResult INC(byte b1) {
        byte result = (byte) (b1 + 1);
        return getAluResult(result, result == 0 ? FlagOperation.SET : FlagOperation.RESET,
                (result & 0xF) == 0 ? FlagOperation.SET : FlagOperation.RESET,
                FlagOperation.RESET,
                FlagOperation.UNCHANGED);
    }

    private static ALUResult getAluResult(byte result, FlagOperation Z, FlagOperation H, FlagOperation N, FlagOperation C) {
        ALUResult operationResult = new ALUResult();
        operationResult.result = result;
        operationResult.Dec = N;
        operationResult.Carry = C;
        operationResult.HalfCarry = H;
        operationResult.Zero = Z;
        return operationResult;
    }

}
