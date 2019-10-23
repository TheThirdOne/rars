package rars.assembler;

/*
Copyright (c) 2003-2006,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

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

/**
 * Represents a program identifier to be stored in the symbol table.
 *
 * @author Jason Bumgarner, Jason Shrewsbury
 * @version June 2003
 **/

public class Symbol {
    private String name;
    private int address;
    private boolean data; // boolean true if data symbol false if text symbol.

    /**
     * Basic constructor, creates a symbol object.
     *
     * @param name    The name of the Symbol.
     * @param address The memroy address that the Symbol refers to.
     * @param data    true if it represents data, false if code.
     **/

    public Symbol(String name, int address, boolean data) {
        this.name = name;
        this.address = address;
        this.data = data;
    }

    /**
     * Returns the address of the the Symbol.
     *
     * @return The address of the Symbol.
     **/

    public int getAddress() {
        return this.address;
    }

    /**
     * Returns the label of the the Symbol.
     *
     * @return The label of the Symbol.
     **/

    public String getName() {
        return this.name;
    }

    /**
     * Finds the type of symbol, text or data.
     *
     * @return The type of the data.
     **/

    public boolean getType() {
        return this.data;
    }


    /**
     * Sets (replaces) the address of the the Symbol.
     *
     * @param newAddress The revised address of the Symbol.
     **/

    public void setAddress(int newAddress) {
        this.address = newAddress;
    }
}