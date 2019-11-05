package rars.riscv.instructions;

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

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;

public class FNMADDS extends FusedFloat {
    public FNMADDS() {
        super("fnmadd.s f1, f2, f3, f4", "Fused Negate Multiply Add: Assigns -(f2*f3+f4) to f1", "11");
    }

    public Float32 compute(Float32 f1, Float32 f2, Float32 f3, Environment e){
        // TODO: test if this is the right behaviour
        flipRounding(e);
        return jsoftfloat.operations.Arithmetic.fusedMultiplyAdd(f1,f2,f3,e).negate();
    }
}
