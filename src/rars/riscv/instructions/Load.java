package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

/**
 * Base class for all Load instructions
 *
 * @author Benjamin Landers
 * @version June 2017
 */
public abstract class Load extends BasicInstruction {
    public Load(String usage, String description, String funct) {
        super(usage, description, BasicInstructionFormat.I_FORMAT,
                "ssssssssssss ttttt " + funct + " fffff 0000011");
    }
    public Load(String usage, String description, String funct, boolean rv64) {
        super(usage, description, BasicInstructionFormat.I_FORMAT,
                "ssssssssssss ttttt " + funct + " fffff 0000011",rv64);

    }


    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        operands[1] = (operands[1] << 20) >> 20;
        try {
            RegisterFile.updateRegister(operands[0], load(RegisterFile.getValue(operands[2]) + operands[1]));
        } catch (AddressErrorException e) {
            throw new SimulationException(statement, e);
        }
    }

    /**
     * @param address the address to load from
     * @return The value to store to the register
     */
    protected abstract long load(int address) throws AddressErrorException;
}