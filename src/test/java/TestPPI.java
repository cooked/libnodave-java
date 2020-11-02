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

public class TestPPI {
	int i, j;
	long a, b, c;
	float d, e, f;
	char buf[];
	byte buf1[];
	PLCinterface di;
	PPIConnection dc;
	SerialPort serialPort;
//	org.visual.nodave.BlockInfo dbi;

	void waitKey() {
		char c;
		System.out.println("Press return to continue.\n");
		//		System.in.read();
	}

	SerialPort sp;
	TestPPI() {
		buf = new char[Nodave.OrderCodeSize];
		buf1 = new byte[Nodave.PartnerListSize];
		try {
			serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
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

	void run() {
		byte[] by;
		OutputStream oStream = null;
		InputStream iStream = null;

		if (serialPort != null) {
			try {
				oStream = serialPort.getOutputStream();
			} catch (IOException e) {
				System.out.println(e);
			}
			try {
				iStream = serialPort.getInputStream();
			} catch (IOException e) {
				System.out.println(e);
			}
			di =
				new PLCinterface(
					oStream,
					iStream,
					"IF1",
					0,
					Nodave.PROTOCOL_PPI);
			//			for (int i = 0; i < 3; i++) {
			if (0 == di.initAdapter()) {
				//						a = di.listReachablePartners(buf1);
				//					System.out.println("Success " + a);
				if (a > 0) {
					for (j = 0; j < a; j++) {
						if (buf1[j] == Nodave.MPIReachable)
							System.out.println("PLC at " + j);
					}
				}
				//					break;
			}
			//			}

			dc = new PPIConnection(di, 2); // insert your PPI address here

			int res = dc.connectPLC();
			if (0 == res) {

				System.out.println(
					"Trying to read 64 bytes (32 words) from data block 1.");
				System.out.println(
									"This is V area in the 200 family.");	
				waitKey();
				dc.readBytes(Nodave.DB, 1, 0, 64, null);
				a = dc.getWORD();
				System.out.println("VW0:" + a);
				a = dc.getWORD();
				System.out.println("VW2: " + a);
				a = dc.getWORD(62);
				System.out.println("V31: " + a);

				System.out.println("Trying to read 16 bytes from FW0.\n");
				waitKey();
				dc.readBytes(Nodave.FLAGS, 0, 0, 16, null);
				a = dc.getU32();
				b = dc.getU32();
				c = dc.getU32();
				d = dc.getFloat();
				System.out.println("FD0: " + a);
				System.out.println("FD4:" + b);
				System.out.println("FD8:" + c);
				System.out.println("FD12: " + d);
				if (doWrite) {
					System.out.println(
						"Now we write back these data after incrementing the first 3 by 1,2,3 and the float by 1.1.\n");
					waitKey();
					by = Nodave.bswap_32(a + 1);
					dc.writeBytes(Nodave.FLAGS, 0, 0, 4, by);
					by = Nodave.bswap_32(b + 1);
					dc.writeBytes(Nodave.FLAGS, 0, 4, 4, by);
					by = Nodave.bswap_32(c + 1);
					dc.writeBytes(Nodave.FLAGS, 0, 8, 4, by);
					by = Nodave.toPLCfloat(d + 1.1);
					dc.writeBytes(Nodave.FLAGS, 0, 12, 4, by);
					dc.readBytes(Nodave.FLAGS, 0, 0, 16, null);
					a = dc.getU32();
					b = dc.getU32();
					c = dc.getU32();
					d = dc.getFloat();
					System.out.println("FD0: " + a);
					System.out.println("FD4:" + b);
					System.out.println("FD8:" + c);
					System.out.println("FD12: " + d);
					//					wait();
				} // doWrite
				long t1, t2;
				if (doBenchmark) {
					System.out.println(
						"Now going to do read benchmark with minimum block length of 1.\n");
					waitKey();
					dc.retries2 = 0;
					dc.retries3 = 0;
					t1 = System.currentTimeMillis();
					for (i = 0; i < 100; i++) {
						dc.readBytes(Nodave.FLAGS, 0, 0, 1, null);
						if ((i % 10) == 0)
							System.out.print("... " + i);
					}
					System.out.println("");
					t2 = System.currentTimeMillis();
					double usec = (t2 - t1) * 0.001;
					System.out.println(
						"100 reads took "
							+ usec
							+ "secs. tried repeats 2nds: "
							+ dc.retries2
							+ " 3rds: "
							+ dc.retries3);
					System.out.println(
						"Now going to do read benchmark with shurely supported block length 16.\n");
					waitKey();
					dc.retries2 = 0;
					dc.retries3 = 0;
					t1 = System.currentTimeMillis();
					for (i = 0; i < 100; i++) {
						dc.readBytes(Nodave.FLAGS, 0, 0, 16, null);
						if ((i % 10) == 0)
							System.out.print("... " + i);
					}
					System.out.println("");
					t2 = System.currentTimeMillis();
					usec = (t2 - t1) * 0.001;

					System.out.println(
						"100 reads took "
							+ usec
							+ "secs. tried repeats 2nds: "
							+ dc.retries2
							+ " 3rds: "
							+ dc.retries3);
					waitKey();
					if (doWrite) {
						System.out.println(
							"Now going to do write benchmark with minimum block length of 1.\n");
						waitKey();
						dc.retries2 = 0;
						dc.retries3 = 0;

						t1 = System.currentTimeMillis();
						by = Nodave.bswap_32(123);
						for (i = 0; i < 100; i++) {
							dc.writeBytes(Nodave.FLAGS, 0, 0, 1, by);
							if ((i % 10) == 0)
								System.out.print("... " + i);
						}
						System.out.println("");
						t2 = System.currentTimeMillis();
						usec = (t2 - t1) * 0.001;
						System.out.println(
							"100 writes took "
								+ usec
								+ "secs.. tried repeats 2nds: "
								+ dc.retries2
								+ " 3rds: "
								+ dc.retries3);

						System.out.println(
							"Now going to do write benchmark with shurely supported block length 16.\n");
						waitKey();
						dc.retries2 = 0;
						dc.retries3 = 0;

						t1 = System.currentTimeMillis();
						for (i = 0; i < 100; i++) {
							dc.writeBytes(Nodave.FLAGS, 0, 0, 16, by);
							if ((i % 10) == 0)
								System.out.print("... " + i);
						}
						System.out.println("");
						t2 = System.currentTimeMillis();
						usec = (t2 - t1) * 0.001;
						System.out.println(
							"100 writes took "
								+ usec
								+ "secs. tried repeats 2nds: "
								+ dc.retries2
								+ " 3rds: "
								+ dc.retries3);

						waitKey();
					} // doWrite
				} // doBenchmark
				//		 wait();
				System.out.println("Now disconnecting\n");
				dc.disconnectPLC();
				di.disconnectAdapter();
			} else
				System.out.println("Couldn't connect to PLC. Error:" + res);
		} else
			System.out.println("Couldn't open serial port");
	}

	static boolean doWrite = false;
	static boolean doBenchmark = false;
	static boolean doLentest = false;

	static CommPortIdentifier portId;
	static Enumeration portList;

	public static void main(String[] args) {
		int adrPos = 0;
		if (args.length <= adrPos) {
			usage();
			System.exit(-1);
		}

		while (args[adrPos].startsWith("-")) {
			Nodave.Debug = Nodave.DEBUG_PRINT_ERRORS;
			if (args[adrPos].equals("-d")) {
				Nodave.Debug = Nodave.DEBUG_ALL;
			} else if (args[adrPos].equals("-w")) {
				doWrite = true;
			} else if (args[adrPos].equals("-b")) {
				doBenchmark = true;
			} else if (args[adrPos].equals("-l")) {
				doLentest = true;
			}
			adrPos++;
			if (args.length <= adrPos) {
				usage();
				System.exit(-1);
			}
		}

		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				// if (portId.getName().equals("COM1")) {
				if (portId.getName().equals(args[adrPos])) {
					TestPPI tp = new TestPPI();
					tp.run();
				}
			}
		}
		System.out.println("Done...");
	}

	/*
		void wait() {
			uc c;
			printf("Press return to continue.\n");
			read(0,&c,1);
		}    
	*/
	static void usage() {
		System.out.println("Usage: testPPI [-d] [-w] serial port");
		System.out.println(
			"-w will try to write to Flag words. It will overwrite FB0 to FB15 (MB0 to MB15) !");
		System.out.println("-d will produce a lot of debug messages.");
		System.out.println(
			"-b will run benchmarks. Specify -b and -w to run write benchmarks.");
		//		System.out.println(
		//			"-l will run a test to determine maximum length of a block in read.");
		System.out.println("Example: testPPI -w /dev/ttyS0");
	}

}