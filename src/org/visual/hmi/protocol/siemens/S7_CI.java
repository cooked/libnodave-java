/*
 Communication Plug-In to directly interface to Simatic S7 CPUs. This abstract class
 implements what is common to S7 family.

 Part of VISUAL, a human machine interface and data acquisition program
 
 (C) Thomas Hergenhahn (thomas.hergenhahn@web.de) 2001, 2004

 VISUAL and LIBNODAVE are free software; you can redistribute it and/or modify
 it under the terms of the GNU Library General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 VISUAL and LIBNODAVE are distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this; see the file COPYING.  If not, write to
 the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.  
*/

package org.visual.hmi.protocol.siemens;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.libnodave.Nodave;
import org.libnodave.PLCinterface;
import org.libnodave.S7Connection;
import org.visual.hmi.hmiList;
import org.visual.hmi.protocol.hmiCommunicationInterface;
import org.visual.hmi.protocol.plcdirect.FetchRange;
import org.visual.hmi.protocol.plcdirect.Variable;

public abstract class S7_CI implements hmiCommunicationInterface {
	final static int debug = 1;

	int hostError;
	hmiList owner;

	Hashtable rangeTable;
	Hashtable varTable;
	int od = 0;
	PLCinterface di;
	S7Connection dc = null;

	public int getHostError() {
		return hostError;
	}

	public void putVal(String k, double v) {
//		if (debug > 0)
			System.out.println("this is PutVal "+k+ " "+v);
		S7Variable va = (S7Variable) varTable.get(k);
		va.write(dc, v);
	}

	public void tPutVal(String k) {
		if (debug > -1000)
			System.out.println("tPutVal not iplemented");
	}

	public void stop() {
		if (debug > 0)
			System.out.println("hmiSimaticCI.stop()");
	}

	public abstract void open();

	public void init(hmiList owner) {
		this.owner = owner;
	}

	/**
	    FetchRange implements a block of bytes containing multiple variables that can 
	    be read in a single call to the underlying protocol. This minimizes the amount 
	    of  read requests to the PLC.
	*/
	class S7Variable extends Variable {
		public S7Variable(String name) {
			super(name);
		}

		public void write(S7Connection dc, double value) {
			if (debug > 1)
				System.out.println(
					"writeBytes("
						+ areaCode
						+ ", "
						+ areaNumber
						+ ", "
						+ first
						+ ", "
						+ size
						+ ", "
						+ getBytes(value));
			if (debug > 3)
				Nodave.Debug = 0xffff;
			dc.writeBytes(areaCode, areaNumber, first, size, getBytes(value));
			if (debug > 3)
				Nodave.Debug = 0;
		}

		byte[] b;

		public byte[] getBytes(double value) {
			float f;
			if (b == null) {
				b = new byte[size];
				if (debug > 0)
					System.out.println("size:" + size);
			}
			switch (typeID) {

				case 'F' :
					return Nodave.toPLCfloat(value);
				case 'C' :
				case 'B' :
					return Nodave.bswap_8((int) value);
				case 'R' :
					break;
				case 'U' :
				case 'L' :
					return Nodave.bswap_32((long) value);
				case 'I' :
				case 'W' :
					return Nodave.bswap_16((int) value);
				case 'D' :
					size = 8;
					break;
			}

			return b;
		}
		public void areaFromName(String s) {
			int l = 1;
			if (areaName.startsWith("DB")) {
				areaCode = Nodave.DB;
				l = 2;
			} else if (areaName.startsWith("F")) {
				areaCode = Nodave.FLAGS;
			} else if (areaName.startsWith("M")) {
				areaCode = Nodave.FLAGS;
			} else if (areaName.startsWith("Q")) {
				areaCode = Nodave.OUTPUTS;
			} else if (areaName.startsWith("O")) {
				areaCode = Nodave.OUTPUTS;
			} else if (areaName.startsWith("A")) {
				areaCode = Nodave.OUTPUTS;
			} else if (areaName.startsWith("I")) {
				areaCode = Nodave.INPUTS;
			} else if (areaName.startsWith("E")) {
				areaCode = Nodave.INPUTS;
			}
			if (areaName.length() <= l) {
				areaNumber = 0;
			} else {
				String numPart = areaName.substring(l);
				areaNumber = Integer.parseInt(numPart);
			}
		}
	}
	/**
	    Here we have all the variables that occur in this page or hmiGroup.
	*/
	public void initVariableTable() {
		rangeTable = new Hashtable();
		varTable = new Hashtable();
		Enumeration en = owner.getHash().keys();
		while (en.hasMoreElements()) {
			String name = (String) en.nextElement(); // the "coordinate"
			S7Variable va = new S7Variable(name);
			varTable.put(name, va);
			FetchRange range = (FetchRange) rangeTable.get(va.getAreaName());
			if (range == null) {
				if (debug > 0)
					System.out.println(
						"no fetch range for mem area "
							+ va.getAreaName()
							+ " must create one.");
				range = new FetchRange(new ConnectionWrapper(dc), owner, va);
				rangeTable.put(va.getAreaName(), range);
			} else {
				range.addVariable(va);
			}
		}

		en = rangeTable.keys();
		while (en.hasMoreElements()) {
			String area = (String) en.nextElement();
			FetchRange range = (FetchRange) rangeTable.get(area);
			if (debug > 0)
				System.out.println(
					"Setting up Variables for FetchRange " + range.getArea());
			if (debug > 0)
				System.out.println(
					"We need byte " + range.first + " to " + range.last);
		}

	}

	public void destroy() {
	}

	public void getValTab() {
		od++;
		Enumeration en = rangeTable.keys();
		// Start communication for all memory ranges to read
		while (en.hasMoreElements()) {
			String area = (String) en.nextElement();
			FetchRange range = (FetchRange) rangeTable.get(area);
			if (range.plcRead()<0) {
				hostError+=5;
//				System.out.println("hostError "+hostError);
			} 
		}
	}

	/*    
	    Get pairs of time and value for trend diagrams.
	    Return the last time stamp as a String. The return value is used for parameter last
	    in the next call. The first call should use "0". The behaviour is to get all trend data
	    kept by the server in the first call and only newer data in subsequent calls.
	*/
	public String getTrendData(
		String k,
		String last,
		Vector timeValuePairs,
		int maxLen) {
		return "0";
	};

	/**
	    Get the alarm messages from a variable range given in alarmRange. Not yet implemented in any CI.
	*/
	public void getAlarmMessages(String request, Vector lines, int maxLines) {
	}

	public String getAnswer(String s) {
		return "not implemented";
	}
	
	public void close() {
		dc.disconnectPLC();
	}	

}
