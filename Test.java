import rars.AssemblyException;
import rars.ErrorList;
import rars.ErrorMessage;
import rars.SimulationException;
import rars.api.Options;
import rars.api.Program;
import rars.simulator.Simulator;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

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
        int[] errorlines = null;
        String stdin = "", stdout = "", stderr ="";
        // TODO: better config system
        // This is just a temporary solution that should work for the tests I want to write
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = br.readLine();
            while(line.startsWith("#")){
                if (line.startsWith("#error on lines:")) {
                    String[] linenumbers = line.replaceFirst("#error on lines:", "").split(",");
                    errorlines = new int[linenumbers.length];
                    for(int i = 0; i < linenumbers.length; i++){
                        errorlines[i] = Integer.parseInt(linenumbers[i].trim());
                    }
                } else if (line.startsWith("stdin:")) {
                    stdin = line.replaceFirst("#stdin:", "").replaceAll("\\\\n","\n");
                } else if (line.startsWith("#stdout:")) {
                    stdout = line.replaceFirst("#stdout:", "").replaceAll("\\\\n","\n");
                } else if (line.startsWith("#stderr:")) {
                    stderr = line.replaceFirst("#stderr:", "").replaceAll("\\\\n","\n");
                }
                line = br.readLine();
            }
        }catch(FileNotFoundException fe){
            return "Could not find " + path;
        }catch(IOException io){
            return "Error reading " + path;
        }
        try {
            p.assemble(path);
            if(errorlines != null){
                return "Expected asssembly error, but successfully assembled " + path;
            }
            p.setup(null,stdin);
            Simulator.Reason r = p.simulate();
            if(r != Simulator.Reason.NORMAL_TERMINATION){
                return "Ended abnormally while executing " + path;
            }else{
                if(p.getExitCode() != 42) {
                    return "Final exit code was wrong for " + path;
                }
                if(!p.getSTDOUT().equals(stdout)){
                    return "STDOUT was wrong for " + path + "\n Expected \""+stdout+"\" got \""+p.getSTDOUT()+"\"";
                }
                if(!p.getSTDERR().equals(stderr)){
                    return "STDERR was wrong for " + path;
                }
                return "";
            }
        } catch (AssemblyException ae){
            if(errorlines == null) {
                return "Failed to assemble " + path;
            }
            if(ae.errors().errorCount() != errorlines.length){
                return "Mismatched number of assembly errors in" + path;
            }
            Iterator<ErrorMessage> errors = ae.errors().getErrorMessages().iterator();
            for(int number : errorlines){
                ErrorMessage error = errors.next();
                while(error.isWarning()) error = errors.next();
                if(error.getLine() != number){
                    return "Expected error on line " + number + ". Found error on line " + error.getLine()+" in " + path;
                }
            }
            return "";
        } catch (SimulationException se){
            return "Crashed while executing " + path;
        }
    }
}
