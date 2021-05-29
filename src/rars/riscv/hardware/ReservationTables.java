package rars.riscv.hardware;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

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

public class ReservationTables {
	private ReservationTable[] reservationTables;
	public int processors;
	private Collection<ReservationTablesObservable> observables = new Vector<>();

	public ReservationTables(int processors) {
		this.processors = processors;
		reset();
	}

	public void reset() {
		reservationTables = new ReservationTable[processors];
		for (int i = 0; i < reservationTables.length; i++) {
			reservationTables[i] = new ReservationTable();
		}
	}

	public void reserveAddress(int processor, int address) {
		reservationTables[processor].reserveAddress(address);
	}

	public boolean unreserveAddress(int processor, int address) {
		if (reservationTables[processor].contains(address)) {
			for (ReservationTable reservationTable : reservationTables) {
				reservationTable.unreserveAddress(address);
			}
			return true;
		}
		return false;
	}

	public Integer[][] getAllAddresses() {
		Integer[][] all = new Integer[ReservationTable.capacity][processors];
		for (int i = 0; i < ReservationTable.capacity; i++) {
			for (int j = 0; j < processors; j++) {
				Integer[] addresses = reservationTables[j].getAddresses();
				all[i][j] = addresses[j];
			}
		}
		return all;
	}

	public void addObserver(Observer obs) {
		observables.add(new ReservationTablesObservable(obs));
	}

	private class ReservationTablesObservable extends Observable {
		public ReservationTablesObservable(Observer obs) {
			this.addObserver(obs);
		}

		public void notifyObserver(MemoryAccessNotice notice) {
			this.setChanged();
			this.notifyObservers(notice);
		}
	}
}
