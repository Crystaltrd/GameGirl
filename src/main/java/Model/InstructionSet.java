package Model;

import static Model.REG_TYPE.*;
import static Model.IN_TYPE.*;
import static Model.ADDR_MODE.*;
import static Model.COND_TYPE.*;

public class InstructionSet {
    public static Instruction getInstr(int opcode) {
        return switch (opcode) {
            case 0x00 -> new Instruction(IN_NOP, AM_IMP);
            case 0x01 -> new Instruction(IN_LD, AM_R_D16, RT_BC);
            case 0x02 -> new Instruction(IN_LD, AM_MR_R, RT_BC, RT_A);
            case 0x03 -> new Instruction(IN_INC, AM_R, RT_BC);
            case 0x04 -> new Instruction(IN_INC, AM_R, RT_B);
            case 0x05 -> new Instruction(IN_DEC, AM_R, RT_B);
            case 0x06 -> new Instruction(IN_LD, AM_R_D8, RT_B);
            case 0x07 -> new Instruction(IN_RLCA);
            case 0x08 -> new Instruction(IN_LD, AM_A16_R, RT_NONE, RT_SP);
            case 0x09 -> new Instruction(IN_ADD, AM_R_R, RT_HL, RT_BC);
            case 0x0A -> new Instruction(IN_LD, AM_R_MR, RT_A, RT_BC);
            case 0x0B -> new Instruction(IN_DEC, AM_R, RT_BC);
            case 0x0C -> new Instruction(IN_INC, AM_R, RT_C);
            case 0x0D -> new Instruction(IN_DEC, AM_R, RT_C);
            case 0x0E -> new Instruction(IN_LD, AM_R_D8, RT_C);
            case 0x0F -> new Instruction(IN_RRCA);

            //0x1X
            case 0x10 -> new Instruction(IN_STOP);
            case 0x11 -> new Instruction(IN_LD, AM_R_D16, RT_DE);
            case 0x12 -> new Instruction(IN_LD, AM_MR_R, RT_DE, RT_A);
            case 0x13 -> new Instruction(IN_INC, AM_R, RT_DE);
            case 0x14 -> new Instruction(IN_INC, AM_R, RT_D);
            case 0x15 -> new Instruction(IN_DEC, AM_R, RT_D);
            case 0x16 -> new Instruction(IN_LD, AM_R_D8, RT_D);
            case 0x17 -> new Instruction(IN_RLA);
            case 0x18 -> new Instruction(IN_JR, AM_D8);
            case 0x19 -> new Instruction(IN_ADD, AM_R_R, RT_HL, RT_DE);
            case 0x1A -> new Instruction(IN_LD, AM_R_MR, RT_A, RT_DE);
            case 0x1B -> new Instruction(IN_DEC, AM_R, RT_DE);
            case 0x1C -> new Instruction(IN_INC, AM_R, RT_E);
            case 0x1D -> new Instruction(IN_DEC, AM_R, RT_E);
            case 0x1E -> new Instruction(IN_LD, AM_R_D8, RT_E);
            case 0x1F -> new Instruction(IN_RRA);

            //0x2X
            case 0x20 -> new Instruction(IN_JR, AM_D8, RT_NONE, RT_NONE, CT_NZ);
            case 0x21 -> new Instruction(IN_LD, AM_R_D16, RT_HL);
            case 0x22 -> new Instruction(IN_LD, AM_HLI_R, RT_HL, RT_A);
            case 0x23 -> new Instruction(IN_INC, AM_R, RT_HL);
            case 0x24 -> new Instruction(IN_INC, AM_R, RT_H);
            case 0x25 -> new Instruction(IN_DEC, AM_R, RT_H);
            case 0x26 -> new Instruction(IN_LD, AM_R_D8, RT_H);
            case 0x27 -> new Instruction(IN_DAA);
            case 0x28 -> new Instruction(IN_JR, AM_D8, RT_NONE, RT_NONE, CT_Z);
            case 0x29 -> new Instruction(IN_ADD, AM_R_R, RT_HL, RT_HL);
            case 0x2A -> new Instruction(IN_LD, AM_R_HLI, RT_A, RT_HL);
            case 0x2B -> new Instruction(IN_DEC, AM_R, RT_HL);
            case 0x2C -> new Instruction(IN_INC, AM_R, RT_L);
            case 0x2D -> new Instruction(IN_DEC, AM_R, RT_L);
            case 0x2E -> new Instruction(IN_LD, AM_R_D8, RT_L);
            case 0x2F -> new Instruction(IN_CPL);

            //0x3X
            case 0x30 -> new Instruction(IN_JR, AM_D8, RT_NONE, RT_NONE, CT_NC);
            case 0x31 -> new Instruction(IN_LD, AM_R_D16, RT_SP);
            case 0x32 -> new Instruction(IN_LD, AM_HLD_R, RT_HL, RT_A);
            case 0x33 -> new Instruction(IN_INC, AM_R, RT_SP);
            case 0x34 -> new Instruction(IN_INC, AM_MR, RT_HL);
            case 0x35 -> new Instruction(IN_DEC, AM_MR, RT_HL);
            case 0x36 -> new Instruction(IN_LD, AM_MR_D8, RT_HL);
            case 0x37 -> new Instruction(IN_SCF);
            case 0x38 -> new Instruction(IN_JR, AM_D8, RT_NONE, RT_NONE, CT_C);
            case 0x39 -> new Instruction(IN_ADD, AM_R_R, RT_HL, RT_SP);
            case 0x3A -> new Instruction(IN_LD, AM_R_HLD, RT_A, RT_HL);
            case 0x3B -> new Instruction(IN_DEC, AM_R, RT_SP);
            case 0x3C -> new Instruction(IN_INC, AM_R, RT_A);
            case 0x3D -> new Instruction(IN_DEC, AM_R, RT_A);
            case 0x3E -> new Instruction(IN_LD, AM_R_D8, RT_A);
            case 0x3F -> new Instruction(IN_CCF);

            //0x4X
            case 0x40 -> new Instruction(IN_LD, AM_R_R, RT_B, RT_B);
            case 0x41 -> new Instruction(IN_LD, AM_R_R, RT_B, RT_C);
            case 0x42 -> new Instruction(IN_LD, AM_R_R, RT_B, RT_D);
            case 0x43 -> new Instruction(IN_LD, AM_R_R, RT_B, RT_E);
            case 0x44 -> new Instruction(IN_LD, AM_R_R, RT_B, RT_H);
            case 0x45 -> new Instruction(IN_LD, AM_R_R, RT_B, RT_L);
            case 0x46 -> new Instruction(IN_LD, AM_R_MR, RT_B, RT_HL);
            case 0x47 -> new Instruction(IN_LD, AM_R_R, RT_B, RT_A);
            case 0x48 -> new Instruction(IN_LD, AM_R_R, RT_C, RT_B);
            case 0x49 -> new Instruction(IN_LD, AM_R_R, RT_C, RT_C);
            case 0x4A -> new Instruction(IN_LD, AM_R_R, RT_C, RT_D);
            case 0x4B -> new Instruction(IN_LD, AM_R_R, RT_C, RT_E);
            case 0x4C -> new Instruction(IN_LD, AM_R_R, RT_C, RT_H);
            case 0x4D -> new Instruction(IN_LD, AM_R_R, RT_C, RT_L);
            case 0x4E -> new Instruction(IN_LD, AM_R_MR, RT_C, RT_HL);
            case 0x4F -> new Instruction(IN_LD, AM_R_R, RT_C, RT_A);

            //0x5X
            case 0x50 -> new Instruction(IN_LD, AM_R_R, RT_D, RT_B);
            case 0x51 -> new Instruction(IN_LD, AM_R_R, RT_D, RT_C);
            case 0x52 -> new Instruction(IN_LD, AM_R_R, RT_D, RT_D);
            case 0x53 -> new Instruction(IN_LD, AM_R_R, RT_D, RT_E);
            case 0x54 -> new Instruction(IN_LD, AM_R_R, RT_D, RT_H);
            case 0x55 -> new Instruction(IN_LD, AM_R_R, RT_D, RT_L);
            case 0x56 -> new Instruction(IN_LD, AM_R_MR, RT_D, RT_HL);
            case 0x57 -> new Instruction(IN_LD, AM_R_R, RT_D, RT_A);
            case 0x58 -> new Instruction(IN_LD, AM_R_R, RT_E, RT_B);
            case 0x59 -> new Instruction(IN_LD, AM_R_R, RT_E, RT_C);
            case 0x5A -> new Instruction(IN_LD, AM_R_R, RT_E, RT_D);
            case 0x5B -> new Instruction(IN_LD, AM_R_R, RT_E, RT_E);
            case 0x5C -> new Instruction(IN_LD, AM_R_R, RT_E, RT_H);
            case 0x5D -> new Instruction(IN_LD, AM_R_R, RT_E, RT_L);
            case 0x5E -> new Instruction(IN_LD, AM_R_MR, RT_E, RT_HL);
            case 0x5F -> new Instruction(IN_LD, AM_R_R, RT_E, RT_A);

            //0x6X
            case 0x60 -> new Instruction(IN_LD, AM_R_R, RT_H, RT_B);
            case 0x61 -> new Instruction(IN_LD, AM_R_R, RT_H, RT_C);
            case 0x62 -> new Instruction(IN_LD, AM_R_R, RT_H, RT_D);
            case 0x63 -> new Instruction(IN_LD, AM_R_R, RT_H, RT_E);
            case 0x64 -> new Instruction(IN_LD, AM_R_R, RT_H, RT_H);
            case 0x65 -> new Instruction(IN_LD, AM_R_R, RT_H, RT_L);
            case 0x66 -> new Instruction(IN_LD, AM_R_MR, RT_H, RT_HL);
            case 0x67 -> new Instruction(IN_LD, AM_R_R, RT_H, RT_A);
            case 0x68 -> new Instruction(IN_LD, AM_R_R, RT_L, RT_B);
            case 0x69 -> new Instruction(IN_LD, AM_R_R, RT_L, RT_C);
            case 0x6A -> new Instruction(IN_LD, AM_R_R, RT_L, RT_D);
            case 0x6B -> new Instruction(IN_LD, AM_R_R, RT_L, RT_E);
            case 0x6C -> new Instruction(IN_LD, AM_R_R, RT_L, RT_H);
            case 0x6D -> new Instruction(IN_LD, AM_R_R, RT_L, RT_L);
            case 0x6E -> new Instruction(IN_LD, AM_R_MR, RT_L, RT_HL);
            case 0x6F -> new Instruction(IN_LD, AM_R_R, RT_L, RT_A);

            //0x7X
            case 0x70 -> new Instruction(IN_LD, AM_MR_R, RT_HL, RT_B);
            case 0x71 -> new Instruction(IN_LD, AM_MR_R, RT_HL, RT_C);
            case 0x72 -> new Instruction(IN_LD, AM_MR_R, RT_HL, RT_D);
            case 0x73 -> new Instruction(IN_LD, AM_MR_R, RT_HL, RT_E);
            case 0x74 -> new Instruction(IN_LD, AM_MR_R, RT_HL, RT_H);
            case 0x75 -> new Instruction(IN_LD, AM_MR_R, RT_HL, RT_L);
            case 0x76 -> new Instruction(IN_HALT);
            case 0x77 -> new Instruction(IN_LD, AM_MR_R, RT_HL, RT_A);
            case 0x78 -> new Instruction(IN_LD, AM_R_R, RT_A, RT_B);
            case 0x79 -> new Instruction(IN_LD, AM_R_R, RT_A, RT_C);
            case 0x7A -> new Instruction(IN_LD, AM_R_R, RT_A, RT_D);
            case 0x7B -> new Instruction(IN_LD, AM_R_R, RT_A, RT_E);
            case 0x7C -> new Instruction(IN_LD, AM_R_R, RT_A, RT_H);
            case 0x7D -> new Instruction(IN_LD, AM_R_R, RT_A, RT_L);
            case 0x7E -> new Instruction(IN_LD, AM_R_MR, RT_A, RT_HL);
            case 0x7F -> new Instruction(IN_LD, AM_R_R, RT_A, RT_A);

            //0x8X
            case 0x80 -> new Instruction(IN_ADD, AM_R_R, RT_A, RT_B);
            case 0x81 -> new Instruction(IN_ADD, AM_R_R, RT_A, RT_C);
            case 0x82 -> new Instruction(IN_ADD, AM_R_R, RT_A, RT_D);
            case 0x83 -> new Instruction(IN_ADD, AM_R_R, RT_A, RT_E);
            case 0x84 -> new Instruction(IN_ADD, AM_R_R, RT_A, RT_H);
            case 0x85 -> new Instruction(IN_ADD, AM_R_R, RT_A, RT_L);
            case 0x86 -> new Instruction(IN_ADD, AM_R_MR, RT_A, RT_HL);
            case 0x87 -> new Instruction(IN_ADD, AM_R_R, RT_A, RT_A);
            case 0x88 -> new Instruction(IN_ADC, AM_R_R, RT_A, RT_B);
            case 0x89 -> new Instruction(IN_ADC, AM_R_R, RT_A, RT_C);
            case 0x8A -> new Instruction(IN_ADC, AM_R_R, RT_A, RT_D);
            case 0x8B -> new Instruction(IN_ADC, AM_R_R, RT_A, RT_E);
            case 0x8C -> new Instruction(IN_ADC, AM_R_R, RT_A, RT_H);
            case 0x8D -> new Instruction(IN_ADC, AM_R_R, RT_A, RT_L);
            case 0x8E -> new Instruction(IN_ADC, AM_R_MR, RT_A, RT_HL);
            case 0x8F -> new Instruction(IN_ADC, AM_R_R, RT_A, RT_A);

            //0x9X
            case 0x90 -> new Instruction(IN_SUB, AM_R_R, RT_A, RT_B);
            case 0x91 -> new Instruction(IN_SUB, AM_R_R, RT_A, RT_C);
            case 0x92 -> new Instruction(IN_SUB, AM_R_R, RT_A, RT_D);
            case 0x93 -> new Instruction(IN_SUB, AM_R_R, RT_A, RT_E);
            case 0x94 -> new Instruction(IN_SUB, AM_R_R, RT_A, RT_H);
            case 0x95 -> new Instruction(IN_SUB, AM_R_R, RT_A, RT_L);
            case 0x96 -> new Instruction(IN_SUB, AM_R_MR, RT_A, RT_HL);
            case 0x97 -> new Instruction(IN_SUB, AM_R_R, RT_A, RT_A);
            case 0x98 -> new Instruction(IN_SBC, AM_R_R, RT_A, RT_B);
            case 0x99 -> new Instruction(IN_SBC, AM_R_R, RT_A, RT_C);
            case 0x9A -> new Instruction(IN_SBC, AM_R_R, RT_A, RT_D);
            case 0x9B -> new Instruction(IN_SBC, AM_R_R, RT_A, RT_E);
            case 0x9C -> new Instruction(IN_SBC, AM_R_R, RT_A, RT_H);
            case 0x9D -> new Instruction(IN_SBC, AM_R_R, RT_A, RT_L);
            case 0x9E -> new Instruction(IN_SBC, AM_R_MR, RT_A, RT_HL);
            case 0x9F -> new Instruction(IN_SBC, AM_R_R, RT_A, RT_A);


            //0xAX
            case 0xA0 -> new Instruction(IN_AND, AM_R_R, RT_A, RT_B);
            case 0xA1 -> new Instruction(IN_AND, AM_R_R, RT_A, RT_C);
            case 0xA2 -> new Instruction(IN_AND, AM_R_R, RT_A, RT_D);
            case 0xA3 -> new Instruction(IN_AND, AM_R_R, RT_A, RT_E);
            case 0xA4 -> new Instruction(IN_AND, AM_R_R, RT_A, RT_H);
            case 0xA5 -> new Instruction(IN_AND, AM_R_R, RT_A, RT_L);
            case 0xA6 -> new Instruction(IN_AND, AM_R_MR, RT_A, RT_HL);
            case 0xA7 -> new Instruction(IN_AND, AM_R_R, RT_A, RT_A);
            case 0xA8 -> new Instruction(IN_XOR, AM_R_R, RT_A, RT_B);
            case 0xA9 -> new Instruction(IN_XOR, AM_R_R, RT_A, RT_C);
            case 0xAA -> new Instruction(IN_XOR, AM_R_R, RT_A, RT_D);
            case 0xAB -> new Instruction(IN_XOR, AM_R_R, RT_A, RT_E);
            case 0xAC -> new Instruction(IN_XOR, AM_R_R, RT_A, RT_H);
            case 0xAD -> new Instruction(IN_XOR, AM_R_R, RT_A, RT_L);
            case 0xAE -> new Instruction(IN_XOR, AM_R_MR, RT_A, RT_HL);
            case 0xAF -> new Instruction(IN_XOR, AM_R_R, RT_A, RT_A);

            //0xBX
            case 0xB0 -> new Instruction(IN_OR, AM_R_R, RT_A, RT_B);
            case 0xB1 -> new Instruction(IN_OR, AM_R_R, RT_A, RT_C);
            case 0xB2 -> new Instruction(IN_OR, AM_R_R, RT_A, RT_D);
            case 0xB3 -> new Instruction(IN_OR, AM_R_R, RT_A, RT_E);
            case 0xB4 -> new Instruction(IN_OR, AM_R_R, RT_A, RT_H);
            case 0xB5 -> new Instruction(IN_OR, AM_R_R, RT_A, RT_L);
            case 0xB6 -> new Instruction(IN_OR, AM_R_MR, RT_A, RT_HL);
            case 0xB7 -> new Instruction(IN_OR, AM_R_R, RT_A, RT_A);
            case 0xB8 -> new Instruction(IN_CP, AM_R_R, RT_A, RT_B);
            case 0xB9 -> new Instruction(IN_CP, AM_R_R, RT_A, RT_C);
            case 0xBA -> new Instruction(IN_CP, AM_R_R, RT_A, RT_D);
            case 0xBB -> new Instruction(IN_CP, AM_R_R, RT_A, RT_E);
            case 0xBC -> new Instruction(IN_CP, AM_R_R, RT_A, RT_H);
            case 0xBD -> new Instruction(IN_CP, AM_R_R, RT_A, RT_L);
            case 0xBE -> new Instruction(IN_CP, AM_R_MR, RT_A, RT_HL);
            case 0xBF -> new Instruction(IN_CP, AM_R_R, RT_A, RT_A);

            case 0xC0 -> new Instruction(IN_RET, AM_IMP, RT_NONE, RT_NONE, CT_NZ);
            case 0xC1 -> new Instruction(IN_POP, AM_R, RT_BC);
            case 0xC2 -> new Instruction(IN_JP, AM_D16, RT_NONE, RT_NONE, CT_NZ);
            case 0xC3 -> new Instruction(IN_JP, AM_D16);
            case 0xC4 -> new Instruction(IN_CALL, AM_D16, RT_NONE, RT_NONE, CT_NZ);
            case 0xC5 -> new Instruction(IN_PUSH, AM_R, RT_BC);
            case 0xC6 -> new Instruction(IN_ADD, AM_R_D8, RT_A);
            case 0xC7 -> new Instruction(IN_RST, AM_IMP, RT_NONE, RT_NONE, CT_NONE, 0x00);
            case 0xC8 -> new Instruction(IN_RET, AM_IMP, RT_NONE, RT_NONE, CT_Z);
            case 0xC9 -> new Instruction(IN_RET);
            case 0xCA -> new Instruction(IN_JP, AM_D16, RT_NONE, RT_NONE, CT_Z);
            case 0xCB -> new Instruction(IN_PREFIX, AM_D8);
            case 0xCC -> new Instruction(IN_CALL, AM_D16, RT_NONE, RT_NONE, CT_Z);
            case 0xCD -> new Instruction(IN_CALL, AM_D16);
            case 0xCE -> new Instruction(IN_ADC, AM_R_D8, RT_A);
            case 0xCF -> new Instruction(IN_RST, AM_IMP, RT_NONE, RT_NONE, CT_NONE, 0x08);

            case 0xD0 -> new Instruction(IN_RET, AM_IMP, RT_NONE, RT_NONE, CT_NC);
            case 0xD1 -> new Instruction(IN_POP, AM_R, RT_DE);
            case 0xD2 -> new Instruction(IN_JP, AM_D16, RT_NONE, RT_NONE, CT_NC);
            case 0xD4 -> new Instruction(IN_CALL, AM_D16, RT_NONE, RT_NONE, CT_NC);
            case 0xD5 -> new Instruction(IN_PUSH, AM_R, RT_DE);
            case 0xD6 -> new Instruction(IN_SUB, AM_R_D8, RT_A);
            case 0xD7 -> new Instruction(IN_RST, AM_IMP, RT_NONE, RT_NONE, CT_NONE, 0x10);
            case 0xD8 -> new Instruction(IN_RET, AM_IMP, RT_NONE, RT_NONE, CT_C);
            case 0xD9 -> new Instruction(IN_RETI);
            case 0xDA -> new Instruction(IN_JP, AM_D16, RT_NONE, RT_NONE, CT_C);
            case 0xDC -> new Instruction(IN_CALL, AM_D16, RT_NONE, RT_NONE, CT_C);
            case 0xDE -> new Instruction(IN_SBC, AM_R_D8, RT_A);
            case 0xDF -> new Instruction(IN_RST, AM_IMP, RT_NONE, RT_NONE, CT_NONE, 0x18);

            //0xEX
            case 0xE0 -> new Instruction(IN_LDH, AM_A8_R, RT_NONE, RT_A);
            case 0xE1 -> new Instruction(IN_POP, AM_R, RT_HL);
            case 0xE2 -> new Instruction(IN_LD, AM_MR_R, RT_C, RT_A);
            case 0xE5 -> new Instruction(IN_PUSH, AM_R, RT_HL);
            case 0xE6 -> new Instruction(IN_AND, AM_R_D8, RT_A);
            case 0xE7 -> new Instruction(IN_RST, AM_IMP, RT_NONE, RT_NONE, CT_NONE, 0x20);
            case 0xE8 -> new Instruction(IN_ADD, AM_R_D8, RT_SP);
            case 0xE9 -> new Instruction(IN_JP, AM_R, RT_HL);
            case 0xEA -> new Instruction(IN_LD, AM_A16_R, RT_NONE, RT_A);
            case 0xEE -> new Instruction(IN_XOR, AM_R_D8, RT_A);
            case 0xEF -> new Instruction(IN_RST, AM_IMP, RT_NONE, RT_NONE, CT_NONE, 0x28);


            //0xFX
            case 0xF0 -> new Instruction(IN_LDH, AM_R_A8, RT_A);
            case 0xF1 -> new Instruction(IN_POP, AM_R, RT_AF);
            case 0xF2 -> new Instruction(IN_LD, AM_R_MR, RT_A, RT_C);
            case 0xF3 -> new Instruction(IN_DI);
            case 0xF5 -> new Instruction(IN_PUSH, AM_R, RT_AF);
            case 0xF6 -> new Instruction(IN_OR, AM_R_D8, RT_A);
            case 0xF7 -> new Instruction(IN_RST, AM_IMP, RT_NONE, RT_NONE, CT_NONE, 0x30);
            case 0xF8 -> new Instruction(IN_LD, AM_HL_SPR, RT_HL, RT_SP);
            case 0xF9 -> new Instruction(IN_LD, AM_R_R, RT_SP, RT_HL);
            case 0xFA -> new Instruction(IN_LD, AM_R_A16, RT_A);
            case 0xFB -> new Instruction(IN_EI);
            case 0xFE -> new Instruction(IN_CP, AM_R_D8, RT_A);
            case 0xFF -> new Instruction(IN_RST, AM_IMP, RT_NONE, RT_NONE, CT_NONE, 0x38);
            default -> new Instruction(IN_NONE);
        };
    }

}
