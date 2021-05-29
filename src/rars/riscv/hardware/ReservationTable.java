package rars.riscv.hardware;

import java.util.ArrayList;

/*
Copyright (c) 2021, Siva Chowdeswar Nandipati & Giancarlo Pernudi Segura.

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

public class ReservationTable {
	private ArrayList<Integer> table;
	public final static int capacity = 8;

	public ReservationTable() {
		table = new ArrayList<Integer>();
	}

	public void reserveAddress(int address) {
		if(table.contains(Integer.valueOf(address)))
			return;
		if (table.size() == capacity)
			table.remove(0);
		table.add(Integer.valueOf(address));
	}

	public void unreserveAddress(int address) {
		table.removeIf(val -> val == address);
	}

	public boolean contains(int address) {
		return table.contains(Integer.valueOf(address));
	}

	public Integer[] getAddresses() {
		Integer[] addresses = new Integer[capacity];
		for (int i = 0; i < capacity; i++) {
			try {
				addresses[i] = table.get(i);
			} catch (IndexOutOfBoundsException e) {
				addresses[i] = 0;
			}
		}
		return addresses;
	}
}
