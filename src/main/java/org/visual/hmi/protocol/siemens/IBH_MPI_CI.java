/*
 Communication Plug-In to directly interface to Simatic S7-300 via IBH NetLink

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

import org.libnodave.IBH_MPIConnection;
import org.libnodave.Nodave;
import org.libnodave.PLCinterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class IBH_MPI_CI extends S7_CI {

	Socket sock=null;

	public void open() {
		System.out.println("This is open()");
		Nodave.Debug=Nodave.DEBUG_ALL & ~Nodave.DEBUG_IFACE;
		OutputStream oStream;
				InputStream iStream;
	if (sock==null) {	
	
		try {
			sock =
				new Socket(
					owner.getParameter("host"),
					owner.getIntParam("port", 1099));
		} catch (IOException e) {
			System.out.println("open() " + e);
		}
		hostError = 0;
//		OutputStream oStream = null;
//		InputStream iStream = null;
//		Nodave.Debug=Nodave.DEBUG_ALL & ~Nodave.DEBUG_IFACE;
//	}
		if (sock != null) {
			try {
				oStream = sock.getOutputStream();
			} catch (IOException e) {
				oStream =null;
			}
			try {
				iStream = sock.getInputStream();
			} catch (IOException e) {
				iStream = null;
			}
			di = new PLCinterface(oStream, iStream, "IF1", 0, Nodave.PROTOCOL_MPI_IBH);
			dc = new IBH_MPIConnection(di, owner.getIntParam("mpi", 2));
		}
	}	
			dc.connectPLC();
//		}
		
		Nodave.Debug=0;
		System.out.println("open() ended");
	}
	
	int second=0;
	public void close() {
			if (second>0) System.exit(1);
			dc.disconnectPLC();
			dc.negPDUlengthRequest();
//			dc.packetNumber=0;
//			dc.messageNumber=0;
//			sock.close();
			
			second++;
	/*		
			 try{
			 	sock.close();
			 	sock=null;
			 } catch (IOException e) {
				System.out.println("close() " + e);
			}
		*/		
	}	

}
