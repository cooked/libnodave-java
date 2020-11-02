/*
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
import org.libnodave.S7Connection;
import org.visual.hmi.protocol.plcdirect.PLCConnection;

import org.libnodave.*;
/**
 * @author thomas
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ConnectionWrapper implements PLCConnection {

	S7Connection s7c;

	public ConnectionWrapper(S7Connection s7c) {
		this.s7c = s7c;
	}

	/* (non-Javadoc)
	 * @see org.visual.hmi.protocol.plc.PLCConnection#readByteBlock(int, int, int, int)
	 */
	public int readByteBlock(int areaCode, int areaNumber, int first, int byteCount) {
		return s7c.readBytes(areaCode, areaNumber, first, byteCount,null);
	}

	/* (non-Javadoc)
	 * @see org.visual.hmi.protocol.plc.PLCConnection#getFloat(int)
	 */
	public float getFloat(int pos) {
		return s7c.getFloat(pos);
	}

	/* (non-Javadoc)
	 * @see org.visual.hmi.protocol.plc.PLCConnection#getUS8(int)
	 */
	public int getUS8(int pos) {
		return s7c.getUS8(pos);
	}

	/* (non-Javadoc)
	 * @see org.visual.hmi.protocol.plc.PLCConnection#getS8(int)
	 */
	public int getS8(int pos) {
		return s7c.getS8(pos);
	}

	/* (non-Javadoc)
	 * @see org.visual.hmi.protocol.plc.PLCConnection#getUS16(int)
	 */
	public int getUS16(int pos) {
		return s7c.getUS16(pos);
	}

	/* (non-Javadoc)
	 * @see org.visual.hmi.protocol.plc.PLCConnection#getS16(int)
	 */
	public int getS16(int pos) {
		return s7c.getS16(pos);
	}

	/* (non-Javadoc)
	 * @see org.visual.hmi.protocol.plc.PLCConnection#getUS32(int)
	 */
	public long getUS32(int pos) {
		return s7c.getUS32(pos);
	}

	/* (non-Javadoc)
	 * @see org.visual.hmi.protocol.plc.PLCConnection#getS32(int)
	 */
	public long getS32(int pos) {
		return s7c.getS32(pos);
	}
	
}
