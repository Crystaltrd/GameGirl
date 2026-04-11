package Model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Instruction {
    private IN_TYPE inType;
    private ADDR_MODE addrMode = ADDR_MODE.AM_IMP;
    private REG_TYPE reg1 = REG_TYPE.RT_NONE;
    private REG_TYPE reg2 = REG_TYPE.RT_NONE;
    private COND_TYPE condType = COND_TYPE.CT_NONE;
    private int param;

    Instruction(IN_TYPE inType) {
        this.inType = inType;
    }

    Instruction(IN_TYPE inType, ADDR_MODE addrMode) {
        this.inType = inType;
        this.addrMode = addrMode;
    }

    Instruction(IN_TYPE inType, ADDR_MODE addrMode, REG_TYPE reg1) {
        this.inType = inType;
        this.addrMode = addrMode;
        this.reg1 = reg1;
    }

    Instruction(IN_TYPE inType, ADDR_MODE addrMode, REG_TYPE reg1, REG_TYPE reg2) {
        this.inType = inType;
        this.addrMode = addrMode;
        this.reg1 = reg1;
        this.reg2 = reg2;
    }

    Instruction(IN_TYPE inType, ADDR_MODE addrMode, REG_TYPE reg1, REG_TYPE reg2, COND_TYPE condType) {
        this.inType = inType;
        this.addrMode = addrMode;
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.condType = condType;
    }

    Instruction(IN_TYPE inType, ADDR_MODE addrMode, REG_TYPE reg1, REG_TYPE reg2, COND_TYPE condType, int param) {
        this.inType = inType;
        this.addrMode = addrMode;
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.condType = condType;
        this.param = param;
    }   
}
