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
import java.net.Socket;

import org.libnodave.Nodave;
import org.libnodave.PLCinterface;
import org.libnodave.TCP243Connection;

public class TestISOTCP243 {
	int i, j;
	long a, b, c;
	float d, e, f;
	char buf[];
	byte buf1[];
	PLCinterface di;
	TCP243Connection dc;
	Socket sock;
	//	  daveBlockTypeEntry dbl[20];  // I never saw more then 7 entries
	//	  daveBlockEntry dbe[256];   
//	org.visual.nodave.BlockInfo dbi;
	//	  _daveOSserialType fds;
	//	  fds.rfd=setPort("/dev/ttyS2","9600",'E');
	//	  fds.wfd=fds.rfd;

	void waitKey() {
		char c;
		System.out.println("Press return to continue.\n");
		//		System.in.read(c,1);
	}

	TestISOTCP243(String host) {
		Nodave.Debug=Nodave.DEBUG_ALL;
		buf = new char[Nodave.OrderCodeSize];
		buf1 = new byte[Nodave.PartnerListSize];
		try {
			sock = new Socket(host, 102);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
//	Nodave.debug=DebugAll; //^(Nodave.DEBUG_IFACE|Nodave.DEBUG_SPECIALCHARS);
 
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
			di = new PLCinterface(
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

			dc = new TCP243Connection(di, 0, slot);
			// insert your PPI address here

			int res = dc.connectPLC();
			if (0 == res) {
				System.out.println(
					"Trying to read 64 bytes (32 words) from data block 1.");
				waitKey();
				dc.readBytes(Nodave.DB, 1, 0, 64, null);
				a = dc.getWORD();
				System.out.println("DB1:DW0:" + a);
				a = dc.getWORD();
				System.out.println("DB1:DW1: " + a);
				a = dc.getWORD(62);
				System.out.println("DB1:DW32: " + a);

				System.out.println("Trying to read 16 bytes from FW0.\n");
				waitKey();
				dc.readBytes(Nodave.FLAGS, 0, 0, 16, null);
				a = dc.getU32();
				b = dc.getU32();
				c = dc.getU32();
				d = dc.getFloat();
				System.out.println("3 DWORDS " + a + " " + b + " " + c);
				System.out.println("1 Float: " + d);
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
					t1 = System.currentTimeMillis();
					for (i = 0; i < 10000; i++)
						dc.readBytes(Nodave.FLAGS, 0, 0, 1, null);
					t2 = System.currentTimeMillis();
					double usec = (t2 - t1) * 0.001;

					System.out.println("10000 reads took " + usec + "secs.");

					System.out.println(
						"Now going to do read benchmark with shurely supported block length 200.\n");
					waitKey();
					t1 = System.currentTimeMillis();
					for (i = 0; i < 10000; i++)
						dc.readBytes(Nodave.FLAGS, 0, 0, 200, null);
					t2 = System.currentTimeMillis();
					usec = (t2 - t1) * 0.001;

					System.out.println("10000 reads took " + usec + "secs.");
					waitKey();
					if (doWrite) {
						System.out.println(
							"Now going to do write benchmark with minimum block length of 1.\n");
						waitKey();
						t1 = System.currentTimeMillis();
						by = Nodave.bswap_32(123);
						for (i = 0; i < 10000; i++)
							dc.writeBytes(Nodave.FLAGS, 0, 0, 1, by);
						t2 = System.currentTimeMillis();
						usec = (t2 - t1) * 0.001;
						System.out.println(
							"10000 writes took " + usec + "secs.");

						System.out.println(
							"Now going to do write benchmark with shurely supported block length 200.\n");
						waitKey();
						t1 = System.currentTimeMillis();
						for (i = 0; i < 10000; i++)
							dc.writeBytes(Nodave.FLAGS, 0, 0, 200, null);
						t2 = System.currentTimeMillis();
						usec = (t2 - t1) * 0.001;
						System.out.println(
							"10000 writes took " + usec + "secs.");

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
			System.out.println("Couldn't open connection");
	}

	static boolean doWrite = false;
	static boolean doBenchmark = false;
	static boolean doLentest = false;
	static int useProtocol = Nodave.PROTOCOL_ISOTCP;
	static int slot;

	public static void main(String[] args) {
		int adrPos = 0;
		if (args.length <= adrPos) {
			usage();
			System.exit(-1);
		}

		Nodave.Debug=Nodave.DEBUG_ALL^(Nodave.DEBUG_IFACE|Nodave.DEBUG_SPECIALCHARS);

		while (args[adrPos].startsWith("-")) {
			if (args[adrPos].equals("-d")) {
				Nodave.Debug = Nodave.DEBUG_ALL;
			} else if (args[adrPos].equals("-w")) {
				doWrite = true;
			} else if (args[adrPos].equals("-b")) {
				doBenchmark = true;
			} else if (args[adrPos].equals("-l")) {
				doLentest = true;
			} else if (args[adrPos].equals("-2")) {
				useProtocol = Nodave.PROTOCOL_ISOTCP243;
			}
			if (args[adrPos].startsWith("--slot=")) {
	    		    slot=Integer.parseInt(args[adrPos].substring(7));
	    		    System.out.println("using slot "+slot);
			}
	
			adrPos++;
			if (args.length <= adrPos) {
				usage();
				System.exit(-1);
			}
		}
		TestISOTCP243 tp = new TestISOTCP243(args[adrPos]);
		tp.run();
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
		System.out.println("Usage: testISO_TCP [-d] [-w] IP-Address_of_CP");
		System.out.println(
			"-w will try to write to Flag words. It will overwrite FB0 to FB15 (MB0 to MB15) !");
		System.out.println("-d will produce a lot of debug messages.");
		System.out.println(
			"-b will run benchmarks. Specify -b and -w to run write benchmarks.");
		System.out.println(
			"-l will run a test to determine maximum length of a block in read.");
		System.out.println("Example: testISO_TCP -w 192.168.19.1");
	}
	/*
		int run2(String[] args) {
			int a,b,c,i,adrPos,doWrite,doBenchmark,doLentest,res;
			float d;
			double usec;
			daveInterface * di;
			daveConnection * dc;
			_daveOSserialType fds;
			struct timeval t1, t2;
		#ifdef extendedFunctions
			uc blockBuffer[20000], *bb;
			int j,more,fd,len, uploadID;
			char buf [daveOrderCodeSize];
			char buf1 [davePartnerListSize];
			daveBlockTypeEntry dbl[20];  // I never saw more then 7 entries
			daveBlockEntry dbe[256];   
			daveBlockInfo dbi;  
		#endif
			adrPos=1;
			doWrite=0;
			doBenchmark=0;
			doLentest=0;
			if (argc<2) {
			usage();
			exit(-1);
			}    
	    
			while (argv[adrPos][0]=='-') {
			if (strcmp(argv[adrPos],"-d")==0) {
				daveDebug=daveDebugAll;
			} else
			if (strcmp(argv[adrPos],"-w")==0) {
				doWrite=1;
			} else
			if (strcmp(argv[adrPos],"-b")==0) {
				doBenchmark=1;
			} else
			if (strcmp(argv[adrPos],"-l")==0) {
				doLentest=1;
			} 
			adrPos++;
			if (argc<=adrPos) {
				usage();
				exit(-1);
			}	
			}    
	    
	    
			fds.rfd=openSocket(102, argv[adrPos]);
			fds.wfd=fds.rfd;
	    
			if (fds.rfd>0) { 
			di =daveNewInterface(fds,"IF1",0,daveProtoISOTCP);
			di->timeout=1500000;
			dc =daveNewConnection(di,2,0,0);  // insert your rack and slot here
			if (0==daveConnectPLC(dc)) {
				System.out.println("Connected.\n");
	//			di->timeout=1000;
			}	
			System.out.println("Trying to read 1 byte from system data,address 0x1e3 like MicroWin does.\n");
			daveReadBytes(dc,3,0,0x1e3,1,NULL);
		
			System.out.println("Trying to read 20 bytes from system data,address 0 like MicroWin does.\n");
			daveReadBytes(dc,3,0,0,20,NULL);
		
			System.out.println("Trying to read 64 bytes (32 words) from data block 1.\n");
			wait();
				daveReadBytes(dc,daveDB,1,0,64,NULL);
			a=daveGetWORD(dc);
			System.out.println("DB1:DW0: %d\n",a);
			a=daveGetWORD(dc);
			System.out.println("DB1:DW1: %d\n...\n",a);
		
			a=daveGetWORDat(dc,62);
			System.out.println("DB1:DW32: %d\n",a);
		
			System.out.println("Trying to read 16 bytes from FW0.\n");
			wait();
			daveReadBytes(dc,daveFlags,0,0,16,NULL);
				a=daveGetDWORD(dc);
				b=daveGetDWORD(dc);
				c=daveGetDWORD(dc);
				d=daveGetFloat(dc);
			System.out.println("FD0: %d\n",a);
			System.out.println("FD4: %d\n",b);
			System.out.println("FD8: %d\n",c);
			System.out.println("FD12: %f\n",d);
			if(doWrite) {
					System.out.println("Now we write back these data after incrementing the first 3 by 1,2,3 and the float by 1.1.\n");
				wait();
					a=bswap_32(a+1);
					daveWriteBytes2(dc,daveFlags,0,0,4,&a);
					b=bswap_32(b+1);
					daveWriteBytes2(dc,daveFlags,0,4,4,&b);
					c=bswap_32(c+1);
				daveWriteBytes2(dc,daveFlags,0,8,4,&c);
					d=toPLCfloat(d+1.1);
					daveWriteBytes2(dc,daveFlags,0,12,4,&d);
					daveReadBytes(dc,daveFlags,0,0,16,NULL);
				a=daveGetDWORD(dc);
					b=daveGetDWORD(dc);
					c=daveGetDWORD(dc);
					d=daveGetFloat(dc);
				System.out.println("FD0: %d\n",a);
				System.out.println("FD4: %d\n",b);
				System.out.println("FD8: %d\n",c);
				System.out.println("FD12: %f\n",d);
					wait();
			} // doWrite
		
			if(doBenchmark) {
					System.out.println("Now going to do read benchmark with minimum block length of 1.\n");
				wait();
				gettimeofday(&t1, NULL);
				for (i=0;i<10000;i++)
					daveReadBytes(dc,daveFlags,0,0,1,NULL);
				gettimeofday(&t2, NULL);
				usec = 1e6 * (t2.tv_sec - t1.tv_sec) + t2.tv_usec - t1.tv_usec;
				usec/=1e6;
				System.out.println("10000 reads took %g secs. \n",usec);
		    
				System.out.println("Now going to do read benchmark with shurely supported block length 200.\n");
				wait();
				gettimeofday(&t1, NULL);
				for (i=0;i<10000;i++)
					daveReadBytes(dc,daveFlags,0,0,200,NULL);
				gettimeofday(&t2, NULL);
				usec = 1e6 * (t2.tv_sec - t1.tv_sec) + t2.tv_usec - t1.tv_usec;	
				usec/=1e6;
				System.out.println("10000 reads took %g secs. \n",usec);
					wait();
				if(doWrite) {
				System.out.println("Now going to do write benchmark with minimum block length of 1.\n");
				wait();
				gettimeofday(&t1, NULL);
				for (i=0;i<10000;i++)
						daveWriteBytes2(dc,daveFlags,0,0,1,&c);
				gettimeofday(&t2, NULL);
				usec = 1e6 * (t2.tv_sec - t1.tv_sec) + t2.tv_usec - t1.tv_usec;
				usec/=1e6;
				System.out.println("10000 writes took %g secs. \n",usec);
		    
				System.out.println("Now going to do write benchmark with shurely supported block length 200.\n");
				wait();
				gettimeofday(&t1, NULL);
				for (i=0;i<10000;i++)
						daveWriteBytes2(dc,daveFlags,0,0,200,&c);
				gettimeofday(&t2, NULL);
				usec = 1e6 * (t2.tv_sec - t1.tv_sec) + t2.tv_usec - t1.tv_usec;	
				usec/=1e6;
				System.out.println("10000 writes took %g secs. \n",usec);
					wait();
				} // doWrite
			} // doBenchmark
			if(doLentest) {
					System.out.println("Now going to do try increasing block lengths.\n");
				wait();
				for (i=200;i<1020;i++) {
					res=daveReadBytes(dc,daveFlags,0,0,i,NULL);
				System.out.println("Length: %d result code: %d result length: %d.\n",i,res,dc->AnswLen);
				}	    	
			}  // doLentest  
			System.out.println("Finished.\n");
			return 0;
			} else {
			System.out.println("Couldn't open TCP port. \nPlease make sure a CP is connected and the IP address is ok. \n");	
				return -1;
			}    
		}
	*/
}