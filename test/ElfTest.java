import rars.*;
import rars.api.ElfLoader;
import rars.api.Options;
import rars.api.Program;
import rars.simulator.Simulator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ElfTest {
    public static void main(String[] args) {
        Options opt = new Options();
        opt.maxSteps = 500;
        Program p = new Program(opt);
        //workout(p);
        File[] tests = new File("./test").listFiles(), riscv_tests = new File("./test/riscv-tests").listFiles();
        if(tests == null){
            System.out.println("./test doesn't exist");
            return;
        }
        StringBuilder total = new StringBuilder("\n");
        for(File test : tests){
            if(test.isFile() && test.getName().endsWith(".s")){
                String errors = run(test.getPath(),p);
                if(errors.equals("")) {
                    System.out.print('.');
                }else{
                    System.out.print('X');
                    total.append(errors).append('\n');
                }
            }
        }
        if(riscv_tests == null){
            System.out.println("./test/riscv-tests doesn't exist");
            return;
        }
        for(File test : riscv_tests){
            if(test.isFile() && test.getName().endsWith(".s")){
                String errors = run(test.getPath(),p);
                if(errors.equals("")) {
                    System.out.print('.');
                }else{
                    System.out.print('X');
                    total.append(errors).append('\n');
                }
            }
        }
        System.out.println(total);
    }
    public static String run(String path, Program p) {
        try {
            Process proc = Runtime.getRuntime().exec("sh compile.sh " + path);
            proc.waitFor();

            byte[] bytes = Files.readAllBytes(Paths.get("./testobject"));
            String out = run(bytes,p);
            if(!out.equals("")){
                out += ": " + path;
            }
            return out;
        } catch (IOException|InterruptedException ioe) {
            return "Error reading/compiling file" + ioe.getMessage() + ": " + path;
        }
    }

    public static String run(byte[] bytes, Program p) {
        int[] errorlines = null;
        String stdin = "", stdout = "", stderr = "";
        try {
            ElfLoader.Reason elfr = ElfLoader.load(p, bytes);
            if (elfr != ElfLoader.Reason.SUCCESS) {
                return "Failed to load because of reason: " + elfr.toString();
            }
            Simulator.Reason r = p.simulate();
            if (r != Simulator.Reason.NORMAL_TERMINATION) {
                return "Ended abnormally while executing ";
            } else {
                if (p.getExitCode() != 42) {
                    return "Final exit code was wrong for ";
                }
                return "";
            }
        } catch (SimulationException se) {
            return "Crashed while executing ";
        }
    }

    public static void workout(Program p){
        try {
            p.assembleString("addi t0, t0, -1");
            p.setup(null,"");
            int word = p.getMemory().getWord(0x400000);
            ProgramStatement ps = new ProgramStatement(word,0x400000);
            System.out.println(ps.toString());
        } catch (Exception e) {
        }
    }
}
