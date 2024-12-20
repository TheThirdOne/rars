package rars.riscv;

/**
 * These are seven RISCV-defined formats of basic machine instructions:
 * R, R4, I, S, B, U, and J. Examples of each respectively would be add, fmadd, addi, sw, beq, lui, and jal.
 *
 * @author Benjamin Landers
 * @version April 2019
 */
public enum BasicInstructionFormat {
    R_FORMAT, // 3 register instructions
    R4_FORMAT,// 4 registers instructions
    I_FORMAT, // 1 dst and 1 src register + small immediate
    S_FORMAT, // 2 src registers + small immediate
    B_FORMAT, // 2 src registers + small immediate shifted left
    U_FORMAT, // 1 dst register  + large immediate
    J_FORMAT  // 1 dst register  + large immediate for jumping
}
