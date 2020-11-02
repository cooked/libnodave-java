/*
 Communication Plug-In to directly interface to Simatic S7 CPx43

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

import org.libnodave.Nodave;
import org.libnodave.PLCinterface;
import org.libnodave.TCPConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ISOTCP_CI extends S7_CI {

	Socket sock;

	public void open() {
		System.out.println("This is open()");
		Nodave.Debug=owner.getIntParam("davedebug", 0);
		try {
			sock =
				new Socket(
					owner.getParameter("host"),
					owner.getIntParam("port", 102));
		} catch (IOException e) {
			System.out.println("open() " + e);
		}
		hostError = 0;
		OutputStream oStream = null;
		InputStream iStream = null;

		if (sock != null) {
			try {
				oStream = sock.getOutputStream();
			} catch (IOException e) {
			}
			try {
				iStream = sock.getInputStream();
			} catch (IOException e) {
			}
			String typ = owner.getParameter("family");
			int protocol = Nodave.PROTOCOL_ISOTCP;
			if ((typ != null) && typ.equals("200"))
				protocol = Nodave.PROTOCOL_ISOTCP243;
			di = new PLCinterface(oStream, iStream, "IF1", 0, protocol);
			dc =
				new TCPConnection(
					di,
			//		2,
					owner.getIntParam("rack", 0),
					owner.getIntParam("slot", 2));
			dc.connectPLC();		
		}
		System.out.println("open() ended");
	}

}
