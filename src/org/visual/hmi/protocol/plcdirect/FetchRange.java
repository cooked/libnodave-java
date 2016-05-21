/*
 Part of VISUAL, a human machine interface and data acquisition program
 
 (C) Thomas Hergenhahn (thomas.hergenhahn@web.de) 2001, 2002

 VISUAL is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 VISUAL is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this; see the file COPYING.  If not, write to
 the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.  
*/

package org.visual.hmi.protocol.plcdirect;

import java.util.Vector;
import java.util.Enumeration;
import org.visual.hmi.hmiList;

/**
    FetchRange implements a block of bytes containing multiple variables that can be read
    in a single call to the underlying protocol. This minimizes the amount of transport 
    requests.
*/
public class FetchRange {
	final static int debug = 0;

	public int first;
	public int last;
	Vector vars;
	int areaCode; // specifies memory area or type
	int areaNumber;
	PLCConnection conn;
	hmiList owner;

	public String getArea() {
		return String.valueOf(areaCode) + " " + String.valueOf(areaNumber);
	}

	public FetchRange(PLCConnection conn, hmiList owner, Variable v) {
		this.conn = conn;
		this.owner = owner;
		vars = new Vector();
		areaCode = v.areaCode;
		areaNumber = v.areaNumber;
		first = v.first;
		last = v.first + v.size - 1;
		vars.addElement(v);
	}
	/**
	    Add a Variable to this FetchRange.  
	*/
	public void addVariable(Variable v) {
		if (first > v.first)
			first = v.first;
		int newLast = v.first + v.size - 1;
		if (last < newLast)
			last = newLast;
		vars.addElement(v);
	}

	public int plcRead() {
		int res = 0;
		if (debug > 0)
			System.out.println(
				"get "
					+ areaCode
					+ ","
					+ areaNumber
					+ " 1st: "
					+ first
					+ " last: "
					+ last);

		if (conn != null) {
			res =
				conn.readByteBlock(
					areaCode,
					areaNumber,
					first,
					last - first + 1);
					
			if (res > 0) {
					Enumeration en = vars.elements();
					while (en.hasMoreElements()) {
						Variable va = (Variable) en.nextElement();
						owner.getHash().put(va.name, "??");
					}
			}						
			if (res == 0) {
				Enumeration en = vars.elements();
				while (en.hasMoreElements()) {
					Variable va = (Variable) en.nextElement();
					if (debug > 0)
						System.out.println("setting value of " + va.name);
					if (debug > 0)
						System.out.println(
							"value of "
								+ va.name
								+ "first: "
								+ first
								+ " va.first:"
								+ va.first);
					if (va.typeID == 'L') {
						long l = conn.getS32(va.first - first);
						if (debug > 1)
							System.out.println("value of " + va.name + "=" + l);
						owner.getHash().put(va.name, String.valueOf(l));
					}
					if (va.typeID == 'U') {
						long l = conn.getUS32(va.first - first);
						if (debug > 1)
							System.out.println("value of " + va.name + "=" + l);
						owner.getHash().put(va.name, String.valueOf(l));
					}

					if (va.typeID == 'W') {
						int l = conn.getUS16(va.first - first);
						if (debug > 1)
							System.out.println("value of " + va.name + "=" + l);
						owner.getHash().put(va.name, String.valueOf(l));
					}
					if (va.typeID == 'I') {
						int l = conn.getS16(va.first - first);
						if (debug > 1)
							System.out.println("value of " + va.name + "=" + l);
						owner.getHash().put(va.name, String.valueOf(l));
					}
					if (va.typeID == 'C') {
						int l = conn.getUS8(va.first - first);
						if (debug > 1)
							System.out.println(
								"value of "
									+ va.name
									+ "="
									+ l
									+ "first: "
									+ first
									+ " va.first:"
									+ va.first);
						owner.getHash().put(va.name, String.valueOf(l));
					}
					if (va.typeID == 'B') {
						int l = conn.getS8(va.first - first);
						if (debug > 1)
							System.out.println("value of " + va.name + "=" + l);
						owner.getHash().put(va.name, String.valueOf(l));
					}
					if (va.typeID == 'F') {
						float l = conn.getFloat(va.first - first);
						if (debug > 1)
							System.out.println("value of " + va.name + "=" + l);
						owner.getHash().put(va.name, String.valueOf(l));
					}
				}
			}
		} else {
			System.out.println("no connection ");
			res = -1;
		}
//		System.out.println("plcRead returns "+res);
		return res;
	}

}
