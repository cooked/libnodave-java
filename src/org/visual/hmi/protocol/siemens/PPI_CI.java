/*
 Communication Plug-In to directly interface to Simatic S7-200

 Part of LIBNODAVE
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

//import javax.comm.CommPortIdentifier;
//import javax.comm.PortInUseException;
//import javax.comm.SerialPort;
//import javax.comm.UnsupportedCommOperationException;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import org.libnodave.Nodave;
import org.libnodave.PLCinterface;
import org.libnodave.PPIConnection;
import org.visual.hmi.hmiList;

public class PPI_CI extends S7_CI {
	String comPort;
	int od = 0;

	CommPortIdentifier portId;
		Enumeration portList;
	SerialPort serialPort;
	
	public void open() {
		System.out.println("This is open()");
		comPort= owner.getParameter("comPort");		
		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				// if (portId.getName().equals("COM1")) {
				if (portId.getName().equals(comPort)) {
					try {
								serialPort = (SerialPort) portId.open("PPI_CI", 2000);
							} catch (PortInUseException e) {
							}
							try {
								serialPort.setSerialPortParams(
									9600,
									SerialPort.DATABITS_8,
									SerialPort.STOPBITS_1,
									SerialPort.PARITY_EVEN);
							} catch (UnsupportedCommOperationException e) {
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
				new PLCinterface(
					oStream,
					iStream,
					"IF1",
					0,
					Nodave.PROTOCOL_PPI);
			dc = new PPIConnection(di, 2);
		}
		System.out.println("open5() ended");
	}

	public void init(hmiList owner) {
		this.owner = owner;
		comPort = owner.getParameter("comport");
	}

	public void destroy() {
	}

}
