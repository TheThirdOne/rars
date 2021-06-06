package rars.riscv.hardware;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import rars.SimulationException;

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

public class ReservationTables extends Observable {
	private ReservationTable[] reservationTables;
	public int harts;
	private Collection<ReservationTablesObservable> observables = new Vector<>();

	public ReservationTables(int harts) {
		this.harts = harts;
		reset();
	}

	public void reset() {
		reservationTables = new ReservationTable[harts];
		for (int i = 0; i < reservationTables.length; i++) {
			reservationTables[i] = new ReservationTable();
		}
	}

	public void reserveAddress(int hart, int address) {
		reservationTables[hart].reserveAddress(address);
	}

	public boolean unreserveAddress(int hart, int address) throws AddressErrorException {
		if (address % 4 != 0) {
			throw new AddressErrorException("Reservation address not aligned to word boundary ", SimulationException.STORE_ADDRESS_MISALIGNED, address);
		}
		if (reservationTables[hart].contains(address)) {
			for (ReservationTable reservationTable : reservationTables) {
				reservationTable.unreserveAddress(address);
			}
			return true;
		}
		return false;
	}

	public Integer[][] getAllAddresses() {
		Integer[][] all = new Integer[ReservationTable.capacity][harts];
		for (int i = 0; i < ReservationTable.capacity; i++) {
			for (int j = 0; j < harts; j++) {
				Integer[] addresses = reservationTables[j].getAddresses();
				all[i][j] = addresses[i];
			}
		}
		return all;
	}

	public String[][] getAllAddressesAsStrings() {
		String[][] all = new String[ReservationTable.capacity][harts];
		for (int i = 0; i < ReservationTable.capacity; i++) {
			for (int j = 0; j < harts; j++) {
				Integer[] addresses = reservationTables[j].getAddresses();
				all[i][j] = String.format("0x%08x", addresses[i]);
			}
		}
		return all;
	}

	public void addObserver(Observer obs) {
		observables.add(new ReservationTablesObservable(obs));
	}

	/**
	 * Remove specified reservation tables observer
	 *
	 * @param obs Observer to be removed
	 */
	public void deleteObserver(Observer obs) {
		for (ReservationTablesObservable o : observables) {
			o.deleteObserver(obs);
		}
	}

	/**
	 * Remove all reservation tables observers
	 */
	public void deleteObservers() {
		// just drop the collection
		observables = new Vector<>();
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
