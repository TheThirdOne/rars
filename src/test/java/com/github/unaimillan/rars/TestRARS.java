package com.github.unaimillan.rars;

import com.github.unaimillan.rars.api.Options;
import com.github.unaimillan.rars.api.Program;
import com.github.unaimillan.rars.riscv.*;
import com.github.unaimillan.rars.simulator.Simulator;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class TestRARS {

    @Test
    public void FullCoverage() {
        Globals.initialize();
        Globals.getSettings().setBooleanSettingNonPersistent(Settings.Bool.RV64_ENABLED, false);
        InstructionSet.rv64 = false;
        Globals.instructionSet.populate();
        Options opt = new Options();
        opt.startAtMain = true;
        opt.maxSteps = 1000;
        Program p = new Program(opt);

        File[] basicTests = new File(this.getClass().getResource("/basic").getFile()).listFiles();
        File[] riscvTests = new File(this.getClass().getResource("/riscv-tests").getFile()).listFiles();
        File[] riscvTests64 = new File(this.getClass().getResource("/riscv-tests-64").getFile()).listFiles();

        if (basicTests == null) {
            System.out.println("resources/test doesn't exist");
            return;
        }
        StringBuilder total = new StringBuilder("\n");
        for (File test : basicTests) {
            if (test.isFile() && test.getName().endsWith(".s")) {
                String errors = run(test.getPath(), p);
                if (errors.equals("")) {
                    System.out.print('.');
                } else {
                    System.out.print('X');
                    total.append(errors).append('\n');
                }
            }
        }
        if (riscvTests == null) {
            System.out.println("resources/riscv-tests doesn't exist");
            return;
        }
        for (File test : riscvTests) {
            if (test.isFile() && test.getName().endsWith(".s")) {
                String errors = run(test.getPath(), p);
                if (errors.isEmpty()) {
                    System.out.print('.');
                } else {
                    System.out.print('X');
                    total.append(errors).append('\n');
                }
            }
        }


        if (riscvTests64 == null) {
            System.out.println("resources/riscv-tests-64 doesn't exist");
            return;
        }
        Globals.getSettings().setBooleanSettingNonPersistent(Settings.Bool.RV64_ENABLED, true);
        InstructionSet.rv64 = true;
        Globals.instructionSet.populate();
        for (File test : riscvTests64) {
            if (test.isFile() && test.getName().toLowerCase().endsWith(".s")) {
                String errors = run(test.getPath(), p);
                if (errors.isEmpty()) {
                    System.out.print('.');
                } else {
                    System.out.print('X');
                    total.append(errors).append('\n');
                }
            }
        }
        System.out.println(total);
        checkBinary();
        checkPseudo();
    }

    public static String run(String path, Program p) {
        int[] errorlines = null;
        String stdin = "", stdout = "", stderr = "";
        // TODO: better config system
        // This is just a temporary solution that should work for the tests I want to write
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = br.readLine();
            while (line.startsWith("#")) {
                if (line.startsWith("#error on lines:")) {
                    String[] linenumbers = line.replaceFirst("#error on lines:", "").split(",");
                    errorlines = new int[linenumbers.length];
                    for (int i = 0; i < linenumbers.length; i++) {
                        errorlines[i] = Integer.parseInt(linenumbers[i].trim());
                    }
                } else if (line.startsWith("stdin:")) {
                    stdin = line.replaceFirst("#stdin:", "").replaceAll("\\\\n", "\n");
                } else if (line.startsWith("#stdout:")) {
                    stdout = line.replaceFirst("#stdout:", "").replaceAll("\\\\n", "\n");
                } else if (line.startsWith("#stderr:")) {
                    stderr = line.replaceFirst("#stderr:", "").replaceAll("\\\\n", "\n");
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException fe) {
            return "Could not find " + path;
        } catch (IOException io) {
            return "Error reading " + path;
        }
        try {
            p.assemble(path);
            if (errorlines != null) {
                return "Expected asssembly error, but successfully assembled " + path;
            }
            p.setup(null, stdin);
            Simulator.Reason r = p.simulate();
            if (r != Simulator.Reason.NORMAL_TERMINATION) {
                return "Ended abnormally while executing " + path;
            } else {
                if (p.getExitCode() != 42) {
                    return "Final exit code was wrong for " + path;
                }
                if (!p.getSTDOUT().equals(stdout)) {
                    return "STDOUT was wrong for " + path + "\n Expected \"" + stdout + "\" got \"" + p.getSTDOUT() + "\"";
                }
                if (!p.getSTDERR().equals(stderr)) {
                    return "STDERR was wrong for " + path;
                }
                return "";
            }
        } catch (AssemblyException ae) {
            if (errorlines == null) {
                return "Failed to assemble " + path;
            }
            if (ae.errors().errorCount() != errorlines.length) {
                return "Mismatched number of assembly errors in" + path;
            }
            Iterator<ErrorMessage> errors = ae.errors().getErrorMessages().iterator();
            for (int number : errorlines) {
                ErrorMessage error = errors.next();
                while (error.isWarning()) error = errors.next();
                if (error.getLine() != number) {
                    return "Expected error on line " + number + ". Found error on line " + error.getLine() + " in " + path;
                }
            }
            return "";
        } catch (SimulationException se) {
            return "Crashed while executing " + path;
        }
    }

    public static void checkBinary() {
        Options opt = new Options();
        opt.startAtMain = true;
        opt.maxSteps = 500;
        opt.selfModifyingCode = true;
        Program p = new Program(opt);
        Globals.getSettings().setBooleanSettingNonPersistent(Settings.Bool.SELF_MODIFYING_CODE_ENABLED, true);

        ArrayList<Instruction> insts = Globals.instructionSet.getInstructionList();
        for (Instruction inst : insts) {
            if (inst instanceof BasicInstruction binst) {
                if (binst.getInstructionFormat() == BasicInstructionFormat.B_FORMAT ||
                        binst.getInstructionFormat() == BasicInstructionFormat.J_FORMAT)
                    continue;

                String program = inst.getExampleFormat();
                try {
                    p.assembleString(program);
                    p.setup(null, "");
                    int word = p.getMemory().getWord(0x400000);
                    ProgramStatement assembled = p.getMemory().getStatement(0x400000);
                    ProgramStatement ps = new ProgramStatement(word, 0x400000);
                    if (ps.getInstruction() == null) {
                        System.out.println("Error 1 on: " + program);
                        continue;
                    }
                    if (ps.getPrintableBasicAssemblyStatement().contains("invalid")) {
                        System.out.println("Error 2 on: " + program);
                        continue;
                    }
                    String decompiled = ps.getPrintableBasicAssemblyStatement();

                    p.assembleString(program);
                    p.setup(null, "");
                    int word2 = p.getMemory().getWord(0x400000);
                    if (word != word2) {
                        System.out.println("Error 3 on: " + program);
                    }


                    if (!ps.getInstruction().equals(binst)) {
                        System.out.println("Error 4 on: " + program);
                    }

/*
                    if (assembled.getInstruction() == null) {
                        System.out.println("Error 5 on: " + program);
                        continue;
                    }
                    if (assembled.getOperands().length != ps.getOperands().length){
                        System.out.println("Error 6 on: " + program);
                        continue;
                    }
                    for (int i = 0; i < assembled.getOperands().length; i++){
                        if(assembled.getOperand(i) != ps.getOperand(i)){
                            System.out.println("Error 7 on: " + program);
                        }
                    }*/

                    /*
                    // Not currently used
                    // Do a bit of trial and error to test out variations
                    decompiled = decompiled.replaceAll("x6","t1").replaceAll("x7","t2").replaceAll("x28","t3").trim();
                    String spaced_out = decompiled.replaceAll(",",", ");
                    if(!program.equals(decompiled) && !program.equals(spaced_out)){
                        Globals.getSettings().setBooleanSetting(Settings.Bool.DISPLAY_VALUES_IN_HEX,false);
                        decompiled = ps.getPrintableBasicAssemblyStatement();
                        String decompiled2 = decompiled.replaceAll("x6","t1").replaceAll("x7","t2").replaceAll("x28","t3").trim();
                        String spaced_out2 = decompiled2.replaceAll(",",", ");
                        if(!program.equals(decompiled2) && !program.equals(spaced_out2)) {
                            System.out.println("Error 5 on: " + program;
                        }

                        Globals.getSettings().setBooleanSetting(Settings.Bool.DISPLAY_VALUES_IN_HEX,true);
                    }
                    */
                } catch (Exception e) {
                    System.out.println("Error 5 on: " + program);
                }
            }
        }
    }

    public static void checkPseudo() {
        Options opt = new Options();
        opt.startAtMain = true;
        opt.maxSteps = 500;
        opt.selfModifyingCode = true;
        Program p = new Program(opt);
        Globals.getSettings().setBooleanSettingNonPersistent(Settings.Bool.SELF_MODIFYING_CODE_ENABLED, true);

        ArrayList<Instruction> insts = Globals.instructionSet.getInstructionList();
        int skips = 0;
        for (Instruction inst : insts) {
            if (inst instanceof ExtendedInstruction) {
                String program = "label:" + inst.getExampleFormat();
                try {
                    p.assembleString(program);
                    p.setup(null, "");
                    int first = p.getMemory().getWord(0x400000);
                    int second = p.getMemory().getWord(0x400004);
                    ProgramStatement ps = new ProgramStatement(first, 0x400000);
                    if (ps.getInstruction() == null) {
                        System.out.println("Error 11 on: " + program);
                        continue;
                    }
                    if (ps.getPrintableBasicAssemblyStatement().contains("invalid")) {
                        System.out.println("Error 12 on: " + program);
                        continue;
                    }
                    if (program.contains("t0") || program.contains("t1") || program.contains("t2") || program.contains("f1")) {
                        // TODO: test that each register individually is meaningful and test every register.
                        // Currently this covers all instructions and is an alert if I made a trivial mistake.
                        String register_substitute = program.replaceAll("t0", "x0").replaceAll("t1", "x0").replaceAll("t2", "x0").replaceAll("f1", "f0");
                        p.assembleString(register_substitute);
                        p.setup(null, "");
                        int word1 = p.getMemory().getWord(0x400000);
                        int word2 = p.getMemory().getWord(0x400004);
                        if (word1 == first && word2 == second) {
                            System.out.println("Error 13 on: " + program);
                        }
                    } else {
                        skips++;
                    }
                } catch (Exception e) {
                    System.out.println("Error 14 on: " + program);
                }
            }
        }
        // 12 was the value when this test was written, if instructions are added that intentionally
        // don't have those registers in them add to the register list above or add to the count.
        // Updated to 10: because fsrmi and fsflagsi were removed
        if (skips != 10) System.out.println("Unexpected number of psuedo-instructions skipped.");
    }
}
