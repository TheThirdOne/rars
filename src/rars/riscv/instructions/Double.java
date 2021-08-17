package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float64;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;

public abstract class Double extends BasicInstruction {
    protected Double(String name, String description, String funct) {
        super(name + " f1, f2, f3, dyn", description, BasicInstructionFormat.R_FORMAT, funct + "ttttt sssss qqq fffff 1010011");
    }

    protected Double(String name, String description, String funct, String rm) {
        super(name + " f1, f2, f3", description, BasicInstructionFormat.R_FORMAT, funct + "ttttt sssss " + rm + " fffff 1010011");
    }
    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[3],statement);
        Float64 result = compute(
                new Float64((hart == -1)
                        ? FloatingPointRegisterFile.getValueLong(operands[1])
                        : FloatingPointRegisterFile.getValueLong(operands[1], hart)
                    ),
                new Float64((hart == -1)
                        ? FloatingPointRegisterFile.getValueLong(operands[2])
                        : FloatingPointRegisterFile.getValueLong(operands[2], hart)),
                e);
        Floating.setfflags(e, hart);
        if (hart == -1)
            FloatingPointRegisterFile.updateRegisterLong(operands[0], result.bits);
        else
            FloatingPointRegisterFile.updateRegisterLong(operands[0], result.bits, hart);
    }

    public abstract Float64 compute(Float64 f1, Float64 f2, Environment e);

    public static Float64 getDouble(int num) {
        return new Float64(FloatingPointRegisterFile.getValueLong(num));
    }

    public static Float64 getDouble(int num, int hart) {
        return new Float64(FloatingPointRegisterFile.getValueLong(num, hart));
    }
}
