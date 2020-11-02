/*
 Communication Plug-In to directly interface to Simatic S7-300/400 via MPI Adapter

 Part of VISUAL, a human machine interface and data acquisition program
 
 (C) Thomas Hergenhahn (thomas.hergenhahn@web.de) 2001, 2004

 VISUAL and LIBNODAVE are free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

//import javax.comm.CommPortIdentifier;
//import javax.comm.PortInUseException;
//import javax.comm.SerialPort;
//import javax.comm.UnsupportedCommOperationException;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.libnodave.MPIConnection;
import org.libnodave.MPIinterface;
import org.libnodave.Nodave;
import org.libnodave.PLCinterface;
import org.libnodave.S7Connection;
import org.visual.hmi.hmiList;
import org.visual.hmi.protocol.hmiCommunicationInterface;

import org.visual.hmi.protocol.plcdirect.FetchRange;
import org.visual.hmi.protocol.plcdirect.Variable;

public class MPI_CI implements hmiCommunicationInterface {
	static final int debug = 1;

	String iface;
	String varname;
	byte buffer[];

	int hostError;
	hmiList owner;

	String comPort;
	Hashtable rangeTable;
	Hashtable varTable;
	int od = 0;
	PLCinterface di;
	S7Connection dc = null;

	public int getHostError() {
		return hostError;
	}

	public void putVal(String k, double v) {
		S7Variable va = (S7Variable) varTable.get(k);
		va.write(dc, v);
	}

	public void tPutVal(String k) {
		System.out.println("tPutVal not iplemented");
	}

	public void stop() {
		//		System.out.println("hmiSimaticCI.stop()");
	}

	CommPortIdentifier portId;
		Enumeration portList;
	SerialPort serialPort;
	
	public void open() {
		System.out.println("This is open()");
		
		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				// if (portId.getName().equals("COM1")) {
				if (portId.getName().equals(comPort)) {
					try {
								serialPort = (SerialPort) portId.open("MPI_CI", 2000);
							} catch (PortInUseException e) {
							}
							try {
								serialPort.setSerialPortParams(
									owner.getIntParam("baud",38400),
									SerialPort.DATABITS_8,
									SerialPort.STOPBITS_1,
									SerialPort.PARITY_ODD);
							} catch (UnsupportedCommOperationException e) {
									System.out.println(e);
							}

				}
			}
		}

		hostError = 0;
		OutputStream oStream = null;
		InputStream iStream = null;

		if (serialPort != null) {
			try {
				oStream = serialPort.getOutputStream();
			} catch (IOException e) {
			}
			try {
				iStream = serialPort.getInputStream();
			} catch (IOException e) {
			}
			di =
				new MPIinterface(
					oStream,
					iStream,
					"IF1",
					owner.getIntParam("localMPI",0),
					Nodave.PROTOCOL_MPI);
			dc = new MPIConnection(di, owner.getIntParam("MPI",2));
		}
		System.out.println("open5() ended");
	}

	public void init(hmiList owner) {
		this.owner = owner;
		comPort = owner.getParameter("comport");
	}

	/**
	    FetchRange implements a block of bytes containing multiple variables that can be read
	    in a single call to the underlying protocol. This minimizes the amount of 
	*/
	class S7Variable extends Variable {
		public S7Variable(String name) {
			super(name);
		}
		public void write(S7Connection dc, double value) {
			dc.writeBytes(areaCode, areaNumber, first, size, getBytes(value));
		}
		byte[] b;
		public byte[] getBytes(double value) {
			float f;
			if (b == null)
				b = new byte[size];
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
			System.out.println(
				"Setting up Variables for FetchRange " + range.getArea());
			System.out.println(
				"We need byte " + range.first + " to " + range.last);
		}

	}

	public void destroy() {
	}

	public void getValTab() {
		/*		
				if (od == 2) {
					open5();
				}
		*/
		//	if (serialPort==null) open5();
		od++;
		Enumeration en = rangeTable.keys();
		// Start communication for all memory ranges to read
		while (en.hasMoreElements()) {
			String area = (String) en.nextElement();
			FetchRange range = (FetchRange) rangeTable.get(area);
			range.plcRead();
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
