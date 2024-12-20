package rars.assembler;

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