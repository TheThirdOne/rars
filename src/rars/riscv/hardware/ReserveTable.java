package rars.riscv.hardware;

import java.util.ArrayList;

/*
Copyright (c) 2021,  Siva Chowdeswar Nandipati.

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

public class ReserveTable{
	public static ArrayList<Integer> proc1 = new ArrayList<Integer>();
	public static ArrayList<Integer> proc2 = new ArrayList<Integer>();
	public static ArrayList<Integer> bool = new ArrayList<Integer>();

	public static void ResetReserve(){
		proc1.clear();
		proc2.clear();
		bool.clear();
	}
	public static void addproc1(int address){
		proc1.add(Integer.valueOf(address));
	}
	public static void addproc2(int address){
		proc1.add(Integer.valueOf(address));
	}
	public static void addbool(int address){
		bool.add(Integer.valueOf(address));
	}
	public static void lr_w1(int address){
		if(proc1.contains(Integer.valueOf(address)))
			return;
		addproc1(address);
	}
	public static void lr_w2(int address){
		if(proc2.contains(Integer.valueOf(address)))
			return;
		addproc2(address);
	}
	public static boolean sc_w1(int address){
		if(!proc1.contains(Integer.valueOf(address))){
			return false; // -------------------------------------------------> Doubt Doubt Doubt Doubt Doubt Doubt Doubt Doubt 
		}
		if(bool.contains(Integer.valueOf(address))){
			proc1.remove(Integer.valueOf(address));
			bool.remove(Integer.valueOf(address));
			return false;
		}
		if(!proc2.contains(Integer.valueOf(address))){
			proc1.remove(Integer.valueOf(address));
			return true;
		}
		proc1.remove(Integer.valueOf(address));
		addbool(address);
		return true;
	}
	public static boolean sc_w2(int address){
		if(!proc2.contains(Integer.valueOf(address))){
			return false; // -------------------------------------------------> Doubt Doubt Doubt Doubt Doubt Doubt Doubt Doubt 
		}
		if(bool.contains(Integer.valueOf(address))){
			proc2.remove(Integer.valueOf(address));
			bool.remove(Integer.valueOf(address));
			return false;
		}
		if(!proc1.contains(Integer.valueOf(address))){
			proc2.remove(Integer.valueOf(address));
			return true;
		}
		proc2.remove(Integer.valueOf(address));
		addbool(address);
		return true;
	}
}