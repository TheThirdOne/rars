import rars.AssemblyException;
import rars.SimulationException;
import rars.api.Options;
import rars.api.Program;
import rars.simulator.Simulator;

import java.io.File;

public class Test {
    public static void main(String[] args){
        Options opt = new Options();
        opt.startAtMain = true;
        opt.maxSteps = 500;
        Program p = new Program(opt);
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
    public static String run(String path, Program p){
        try {
            p.assemble(path);
            p.setup(null,"");
            Simulator.Reason r = p.simulate();
            if(r != Simulator.Reason.NORMAL_TERMINATION){
                return "Ending abnormally while executing " + path;
            }else{
                if(p.getExitCode() == 42){
                    return "";
                }else{
                    return "Final exit code was wrong for " + path;
                }
            }
        } catch (AssemblyException ae){
            return "Failed to assemble " + path;
        } catch (SimulationException se){
            return "Crashed while executing " + path;
        }
    }
}
