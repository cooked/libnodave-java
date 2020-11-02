/*
 Test and demo program for Libnodave, a free communication libray for Siemens S7.
 
 **********************************************************************
 * WARNING: This and other test programs overwrite data in your PLC.  *
 * DO NOT use it on PLC's when anything is connected to their outputs.*
 * This is alpha software. Use entirely on your own risk.             * 
 **********************************************************************
 
 (C) Thomas Hergenhahn (thomas.hergenhahn@web.de) 2002, 2004.

 This is free software; you can redistribute it and/or modify
 it under the terms of the GNU Library General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 This is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU Library General Public License
 along with Visual; see the file COPYING.  If not, write to
 the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.  
*/

import org.libnodave.Nodave;
import org.libnodave.PLCinterface;
import org.libnodave.TCPConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TestISO extends Test {
	static int slot=2;
	static int rack=2;
	
	public TestISO(String host) {
	    System.out.println("This is TestISO("+host+")\n");		
	    init(host);
	}
	
	public void init(String host) {
		buf = new char[Nodave.OrderCodeSize];
		buf1 = new byte[Nodave.PartnerListSize];
		try {
		    sock = new Socket(host, 102);
		} catch (IOException e) {
		    System.out.println(e);
		}
	}

	void run() {
		OutputStream oStream = null;
		InputStream iStream = null;

		byte[] by;
		if (sock != null) {
			try {
				oStream = sock.getOutputStream();
			} catch (IOException e) {
			}
			try {
				iStream = sock.getInputStream();
			} catch (IOException e) {
			}
			di =
				new PLCinterface(
					oStream,
					iStream,
					"IF1",
					0,
					Nodave.PROTOCOL_ISOTCP);

			for (int i = 0; i < 3; i++) {
				if (0 == di.initAdapter()) {
					//						a = di.listReachablePartners(buf1);
					System.out.println("Success " + a);
					if (a > 0) {
						for (j = 0; j < a; j++) {
							if (buf1[j] == Nodave.MPIReachable)
								System.out.println("PLC at " + j);
						}
					}
					break;
				}
			}

			dc = new TCPConnection(di, rack, slot);
			super.run();
		} else
			System.out.println("Couldn't open connection");
	}
	
	public static void main(String[] args) {
		if (args.length <= adrPos) {
			usage();
			System.exit(-1);
		}
		evalArgs(args);
		TestISO tp = new TestISO(args[adrPos]);
		tp.run();
		System.out.println("Done...");
	}

	public static void usage() {
		System.out.println("Usage: testISO [-d] [-w] IP-Address_of_CP");
		Test.usage();
		System.out.println("Example: testISO -w 192.168.19.1");
	}
	
	public static void extraTests(String[] args) {
	    if (args[adrPos].startsWith("--slot=")) {
	        slot=Integer.parseInt(args[adrPos].substring(7));
	        System.out.println("using slot "+slot);
	    }
	}


}