package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.RoundingMode;
import jsoftfloat.types.Float32;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

/**
 * Helper class for 4 argument floating point instructions
 */
public abstract class FusedFloat extends BasicInstruction {
    public FusedFloat(String usage, String description, String op) {
        super(usage+", dyn", description, BasicInstructionFormat.R4_FORMAT,
                "qqqqq 00 ttttt sssss " + "ppp" + " fffff 100" + op + "11");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[4],statement);
        Float32 result = compute(new Float32(FloatingPointRegisterFile.getValue(operands[1])),
                new Float32(FloatingPointRegisterFile.getValue(operands[2])),
                new Float32(FloatingPointRegisterFile.getValue(operands[3])),e);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegister(operands[0],result.bits);
    }

    public static void flipRounding(Environment e){
        if(e.mode == RoundingMode.max){
            e.mode = RoundingMode.min;
        }else if(e.mode == RoundingMode.min){
            e.mode = RoundingMode.max;
        }
    }
    /**
     * @param r1 The first register
     * @param r2 The second register
     * @param r3 The third register
     * @return The value to store to the destination
     */
    protected abstract Float32 compute(Float32 r1, Float32 r2, Float32 r3,Environment e);
}
