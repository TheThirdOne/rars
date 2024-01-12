package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.SimulationException;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;

public abstract class Double extends BasicInstruction {
    protected Double(String name, String description, String funct) {
        super(name + " f1, f2, f3, dyn", description, BasicInstructionFormat.R_FORMAT, funct + "ttttt sssss qqq fffff 1010011");
    }

    protected Double(String name, String description, String funct, String rm) {
        super(name + " f1, f2, f3", description, BasicInstructionFormat.R_FORMAT, funct + "ttttt sssss " + rm + " fffff 1010011");
    }
    public void simulate(ProgramStatement statement) throws SimulationException{
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[3],statement);
        Float64 result = compute(new Float64(FloatingPointRegisterFile.getValueLong(operands[1])),new Float64(FloatingPointRegisterFile.getValueLong(operands[2])),e);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegisterLong(operands[0], result.bits);
    }

    public abstract Float64 compute(Float64 f1, Float64 f2, Environment e);

    public static Float64 getDouble(int num){
        return new Float64(FloatingPointRegisterFile.getValueLong(num));
    }
}