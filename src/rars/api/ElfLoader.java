package rars.api;

import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.Memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class ElfLoader {
    public enum Reason {
        NOTELF,
        NOT32,
        NOTLITTLE,
        DYNAMIC,
        BADVERSION,
        NOTEXEC,
        NOTRISCV,
        BADSEGMENTS,
        TOOBIG,
        UNSUPPORTEDSEGMENT,
        INTERNAL,
        SUCCESS
    }
    private static int toInt(byte[] a, int offset, int length){
        int out = 0;
        for(int i = offset+length-1; i >=offset; i--){
            out <<= 8;
            out += a[i] & 0x000000FF;
        }
        return out;
    }
    private static final int MAXSIZE = 0x1000000;
    public static Reason load(Program p, byte[] f){
        if (f.length < 52 || f[0] != 0x7F || f[1] != 'E' || f[2] != 'L' || f[3] != 'F'){
            return Reason.NOTELF;
        }
        if (f.length > MAXSIZE){
            return Reason.TOOBIG;
        }
        if (f[4] != 1){
            return Reason.NOT32;
        }
        if (f[5] != 1){
            return Reason.NOTLITTLE;
        }
        if(f[6] != 1 || f[7] != 0 || toInt(f,8,4) != 0 || toInt(f,12,4) != 0){
            return Reason.BADVERSION;
        }
        if (toInt(f,16,2) != 2){
            return Reason.NOTEXEC;
        }
        if (toInt(f,18,2) != 0xF3){
            return Reason.NOTRISCV;
        }
        if (toInt(f,20,4) != 1){
            return Reason.BADVERSION;
        }
        int entry = toInt(f,24,4);
        int progs =  toInt(f,28,4);
        int sects =  toInt(f,32,4);
        int flags = toInt(f,36,4);
        //skip eh_size
        int progs_size =  toInt(f,42,2);
        int progs_num =  toInt(f,44,2);
        // skip sects_num/size and shstri
        if (progs_num > 20 || progs_num < 0 || progs_size != 32 || progs < 0 || progs > MAXSIZE || progs + progs_num*progs_size > f.length){
            return Reason.BADSEGMENTS;
        }

        p.setEntryPoint(entry);
        p.setup(null,"");
        Memory m = p.getMemory();
        try {
            for (int i = 0; i < progs_num; i++) {
                int base = progs + i * progs_size;
                if (toInt(f, base, 4) != 1) {
                    return Reason.UNSUPPORTEDSEGMENT;
                }

                int offset = toInt(f, base + 4, 4);
                int vaddr = toInt(f, base + 8, 4);
                int filesz = toInt(f, base + 16, 4);
                int memsz = toInt(f, base + 20, 4);
                int pflags = toInt(f, base + 24, 4);
                if (offset < 0 || memsz < 0 || filesz < 0 || vaddr < 0 || offset > f.length || filesz > f.length || offset + filesz > f.length) {
                    return Reason.BADSEGMENTS;
                }
                if (pflags == 5) {
                    if (vaddr < Memory.textBaseAddress || vaddr + memsz > Memory.textLimitAddress) {
                        return Reason.UNSUPPORTEDSEGMENT;
                    }
                    for (int k = 0; k < filesz; k++) {
                        m.set(vaddr + k, f[offset + k], 1);
                    }
                }else if(pflags == 6){
                    if (vaddr < Memory.dataSegmentBaseAddress|| vaddr + memsz > Memory.dataSegmentLimitAddress) {
                        return Reason.UNSUPPORTEDSEGMENT;
                    }
                    for (int k = 0; k < filesz; k++) {
                        m.set(vaddr + k, f[offset + k], 1);
                    }
                }else{
                    //return Reason.UNSUPPORTEDSEGMENT;
                }
            }
        }catch(AddressErrorException aee){
            return Reason.INTERNAL;
        }
        return Reason.SUCCESS;
    }
}
