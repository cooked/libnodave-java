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

public class Variable {
	final static int debug = 0;

	String name;
	public int first;
	public int size;
	protected char typeID;
	public int areaCode;
	public int areaNumber;
	protected String areaName;

	public void areaFromName(String s) {
	}

	public String getAreaName() {
		return areaName;
	}
	
	public String getName() {
		return name;
	}

	public char getTypeID() {
		return typeID;
	}

	public Variable(String name) {
		this.name = name;
		int pos = name.indexOf(":");
		if (debug > 2)
				System.out.println("pos of ':' " + pos);
		String memArea = name.substring(0, pos);
		areaName = name.substring(0, pos);
		areaFromName(areaName);
		if (debug > 2)
			    System.out.println("mem area "+memArea);
		String address = name.substring(pos + 1);
		if (debug > 2)
			    System.out.println("Address "+address);
		typeID = address.charAt(0);
		address = address.substring(1);
		switch (typeID) {
			case 'C' :
			case 'B' :
				size = 1;
				break;
			case 'R' :
			case 'F' :
				size = 4;
				break;
			case 'U' :
			case 'L' :
				size = 4;
				break;
			case 'I' :
			case 'W' :
				size = 2;
				break;
			case 'D' :
				size = 8;
				break;
		}
		if (debug > 2)
				System.out.println("size: " + size);
		first = Integer.parseInt(address);
		if (debug > 2)
				System.out.println("numeric part: " + address + "=" + first);
	}
}
/**
    Here we have all the variables that occur in this page or hmiGroup.
*/
