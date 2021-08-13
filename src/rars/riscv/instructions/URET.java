package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

/*
Copyright (c) 2017,  Benjamin Landers

Developed by Benjamin Landers (benjaminrlanders@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */
public class URET extends BasicInstruction {
    public URET() {
        super("uret", "Return from handling an interrupt or exception (to uepc)",
                BasicInstructionFormat.I_FORMAT, "000000000010 00000 000 00000 1110011");
    }

    public void simulate(ProgramStatement statement) {
        int hart = statement.getCurrentHart();
        if (hart == -1) {
            boolean upie = (ControlAndStatusRegisterFile.getValue("ustatus") & 0x10) == 0x10;
            ControlAndStatusRegisterFile.clearRegister("ustatus", 0x10); // Clear UPIE
            if (upie) { // Set UIE to UPIE
                ControlAndStatusRegisterFile.orRegister("ustatus", 0x1);
            } else {
                ControlAndStatusRegisterFile.clearRegister("ustatus", 0x1);
            }
            RegisterFile.setProgramCounter(ControlAndStatusRegisterFile.getValue("uepc"));
        } else {
            boolean upie = (ControlAndStatusRegisterFile.getValue("ustatus", hart) & 0x10) == 0x10;
            ControlAndStatusRegisterFile.clearRegister("ustatus", 0x10, hart); // Clear UPIE
            if (upie) { // Set UIE to UPIE
                ControlAndStatusRegisterFile.orRegister("ustatus", 0x1, hart);
            } else {
                ControlAndStatusRegisterFile.clearRegister("ustatus", 0x1, hart);
            }
            RegisterFile.setProgramCounter(ControlAndStatusRegisterFile.getValue("uepc", hart), hart);
        }
    }
}
