package rars.extras;

import rars.Globals;
import rars.assembler.Directives;
import rars.riscv.*;
import rars.riscv.hardware.Memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Small class for automatically generating documentation.
 *
 * Currently it makes some Markdown tables, but in the future it could do something
 * with javadocs or generate a website with all of the help information
 */
public class Documentation {

    public static void main(String[] args){
        Globals.initialize(false);
        System.out.println(createDirectiveMarkdown());
        System.out.println(createSyscallMarkdown());
        System.out.println(createInstructionMarkdown(BasicInstruction.class));
        System.out.println(createInstructionMarkdown(ExtendedInstruction.class));
    }

    public static String createDirectiveMarkdown(){
        ArrayList<Directives> directives = Directives.getDirectiveList();
        directives.sort(Comparator.comparing(Directives::getName));
        StringBuilder output = new StringBuilder("| Name | Description|\n|------|------------|");
        for (Directives direct: directives) {
            output.append("\n|");
            output.append(direct.getName());
            output.append('|');
            output.append(direct.getDescription());
            output.append('|');
        }
        return output.toString();
    }

    public static String createSyscallMarkdown(){
        ArrayList<AbstractSyscall> list = SyscallLoader.getSyscallList();
        Collections.sort(list);
        StringBuilder output = new StringBuilder("| Name | Call Number (a7) | Description | Inputs | Outputs |\n|------|------------------|-------------|--------|---------|");
        for (AbstractSyscall syscall : list) {
            output.append("\n|");
            output.append(syscall.getName());
            output.append('|');
            output.append(syscall.getNumber());
            output.append('|');
            output.append(syscall.getDescription());
            output.append('|');
            output.append(syscall.getInputs());
            output.append('|');
            output.append(syscall.getOutputs());
            output.append('|');
        }

        return output.toString();
    }

    public static String createInstructionMarkdown(Class<? extends Instruction> instructionClass){
        ArrayList<Instruction> instructionList = Globals.instructionSet.getInstructionList();
        instructionList.sort(Comparator.comparing(Instruction::getExampleFormat));
        StringBuilder output = new StringBuilder("| Example Usage | Description |\n|---------------|-------------|");
        for (Instruction instr : instructionList) {
            if (instructionClass.isInstance(instr)) {
                output.append("\n|");
                output.append(instr.getExampleFormat());
                output.append('|');
                output.append(instr.getDescription());
                output.append('|');
            }
        }
        return output.toString();
    }
}
