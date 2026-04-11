package Model;

import lombok.Getter;
import lombok.Setter;

enum IN_TYPE {
    IN_NONE,
    IN_CP,
    IN_EI,
    IN_DI,
    IN_DEC,
    IN_INC,
    IN_JP,
    IN_JR,
    IN_LD,
    IN_LDH,
    IN_NOP,
    IN_OR,
    IN_PREFIX,
    IN_XOR,
    IN_AND,
    IN_CPL,
    IN_CCF,
    IN_SCF,
    IN_ADD,
    IN_POP,
    IN_PUSH,
    IN_RET,
    IN_RETI,
    IN_CALL,
    IN_RST,
    IN_SUB,
    IN_RL,
    IN_RLA,
    IN_RLC,
    IN_RLCA,
    IN_SBC,
    IN_ADC,
    IN_STOP, // WONT IMPLEMENT
    IN_HALT,
    IN_SRL,
    IN_RR,
    IN_RRA,
    IN_SET,
    IN_RRC,
    IN_RRCA,
    IN_RES,
    IN_SWAP,
    IN_SLA,
    IN_SRA,
    IN_BIT,
    IN_DAA,
    IN_ILLEGAL_D3,
    IN_ILLEGAL_DB,
    IN_ILLEGAL_DD,
    IN_ILLEGAL_E3,
    IN_ILLEGAL_E4,
    IN_ILLEGAL_EB,
    IN_ILLEGAL_EC,
    IN_ILLEGAL_ED,
    IN_ILLEGAL_F4,
    IN_ILLEGAL_FC,
    IN_ILLEGAL_FD,
}

enum ADDR_MODE {
    AM_R_D16,
    AM_R_R,
    AM_MR_R,
    AM_R,
    AM_R_D8,
    AM_R_MR,
    AM_R_HLI,
    AM_R_HLD,
    AM_HLI_R,
    AM_HLD_R,
    AM_R_A8,
    AM_A8_R,
    AM_HL_SPR,
    AM_D16,
    AM_D8,
    AM_IMP,
    AM_D16_R,
    AM_MR_D8,
    AM_MR,
    AM_A16_R,
    AM_R_A16
}

enum REG_TYPE {
    RT_NONE,
    RT_A,
    RT_F,
    RT_B,
    RT_C,
    RT_D,
    RT_E,
    RT_H,
    RT_L,
    RT_AF,
    RT_BC,
    RT_DE,
    RT_HL,
    RT_PC,
    RT_SP
}

enum COND_TYPE {
    CT_NONE, CT_NZ, CT_Z, CT_NC, CT_C
}

@Setter
@Getter
public class Instruction {
    private IN_TYPE inType;
    private ADDR_MODE addrMode;
    private REG_TYPE reg1;
    private REG_TYPE reg2;
    private COND_TYPE condType;
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
