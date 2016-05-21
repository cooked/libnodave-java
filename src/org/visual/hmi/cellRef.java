/*
 A class that provides a fixed or variable value for HMI elements.
 The rules are:
    1. Create it with a string that can be converted to a number and it
    will be number from then.
    2. Create it with a string that cannot be converted to a number and it
    will put it's String into owner getList()'s hashtable in the hope
    that owner's hmiList will provide a number for the string (usually
    a cell coordinate).
    3. Create it with a string that can or cannot be converted to a number 
    and set isText to true amd promise only to retrieve it's coord
    in textual context. This is for thingies that want to store a text.
    Thus it provides a uniform interface to hmiD(esigner). (There is a 
    cellRef for each parameter line) 
    
 
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
package org.visual.hmi;

import java.util.Hashtable;

/**
 A class that provides a fixed or variable value for HMI elements.
*/
public class cellRef {
/**
 * For the functionality of cellRef, a boolean flag would be sufficient:
 * If something is treated as a number, cellRef tries to convert the given string
 * to a numeric value. If this fails, the string is used as a variable name.
 * If something is not treated as a number, the string is just stored.
 * The other constants are used to provide additional information to the design
 * tools about what the widget wants to do with the value or what it might expect.
 */	
	public final static int VTINT=6;		// an integer
	public final static int VTCOLOR=7;	// a Color, special case of an integer
	public final static int VTANGLE=8;	// an angle, special case of an integer
	public final static int VTNUMBER=9; // any number, float
	public final static int VTTEXT=11;
	public final static int VTIMAGE=12;
	public final static int VTEXTPARM=13;
	public final static int VTCLASS=14;
	public final static int VTLINK=15;
	public final static int VTKEYNAME=16;
	
	private static Hashtable localList=new Hashtable(); 
	/**
	    means this is a fixed number.
	*/
	public boolean isNum;
	/**
			Means this is a local var. Local vars are	only used within an applet to.
			communicate among widgets.
	*/
	public boolean isLocal;
	/**
			This string marks local vars.
			Every variable which has a name beginning with this String is treated as a local.
			It has a default value of "$$".  You can change it using a parameter to hmiViewer.
			You may need to do so, if your server?s naming conventions conflict with the  
			default (which is quite unlikely).
	*/
	static String localPrefix="$$";
	/**
	    means this is a fixed text, NOT a variable name.
	*/
	public int varType; // it's a fixed text
	/**
	    the numerical value.
	*/
	public double value = 0; // default numerical value
	/**
	    The variable name:<br> a spreadsheet coordinate for VISUAL<br> whatever the CI needs for other communication interfaces<br>
	*/
	public String coord; // my textual contents
	/**
	    The widget this variable belongs to.
	*/
	cellRefHolder owner;
	/**
	    Constructs the cellRef from a String. 
	    If the string can be converted to a number,
	    then it IS a number, else it depends on the hmiElement. It can tell
	    "this is fixed text". Else it's a cell coordinate.
	     The rules are:<br>
	    1. Create it with a string that can be converted to a number and it
	    will be number from then.<br>
	    2. Create it with a string that cannot be converted to a number and it
	    will put it's String (usually a cell coordinate) as a key into owner's hashtable in the hope
	    that owner's hmiCommunicationInterface will provide a number for the string .<br>
	    3. Create it with a string that can or cannot be converted to a number 
	    and set isText to true. That means you promise to retrieve it's contents
	    in textual context only. This is for thingies that want to store a text.
	    Thus it provides a uniform interface to hmiDesigner. (There is a 
	    cellRef for each parameter, even if it is a fixed String).
	*/
	public cellRef(String t, cellRefHolder owner, int varType) {
		this.owner = owner;
/*
 * replace null names with empty Strings
 */		
		if ((varType>VTNUMBER) && (t == null))
			t = " ";
			
		coord = t;
		try {
			if (t.startsWith("0x")) {
//				System.out.println("trying hex on "+t);
				value = Integer.parseInt(t.substring(2), 16); // try to convert
//				System.out.println("success: "+value);
				isNum = true; // ok,remember being a number
			}
			else {
				value = new Double(t).doubleValue(); // try to convert
				isNum = true; // ok,remember being a number
			}
		}
		catch (NumberFormatException e1) {
			isNum = false;
			if (varType<=VTNUMBER) { 
				// if text, do not try to convert this
				coord=owner.getAddressModifier().getTranslatedAddress(t);
				if (coord.startsWith(localPrefix)) {
//						owner.getList().putLocal(coord,  "0");
						isLocal=true;
						localList.put(coord,"0");
				} else
						owner.getList().put(coord,  "?");
				 // Mark as error, overwritten when
				// actual value successfully fetched.
				//		owner.getList().incSCount();	// puts an upper limit to the count		
				// of values owner must get.
			}
		}
		this.varType = varType;
	}
	/**
	 Retrieve numeric value as integer.
	*/
	public int getInt() {
		return ((int)getNum());
	}
	/**
	 Retrieve numeric value as double.
	*/
	public double getNum() {
		String S1;
		if (!isNum) {
			if (isLocal) {
					S1 =(String)localList.get(coord);
					try {
							value = new Double(S1).doubleValue();
							return value;
					}
					catch (NumberFormatException e1) {
							return (0);
					}
			}
			
			if (owner.getList().getHash() != null) {
				S1 = (String) (owner.getList().getHash().get(coord));
				//		System.out.println("get(): "+S1);
			}
			else {
				S1 = "??";
			}
			if (S1.equals("??")) {
				owner.setComError(4); // server said "invalid var name"
				return (0);
			}
			if (S1.equals("???")) {
				owner.setComError(5); // server said "value is uncertain"    
				return (value);
			}
			if (S1.equals("?")) {
				owner.setComError(3); // no host
				//		System.out.println("get(): "+S1+ " ,no host");
				return (0);
			}
			try {
				value = new Double(S1).doubleValue();
				//		System.out.println(S1+" =? "+value);
			}
			catch (NumberFormatException e1) {
				//	    	System.out.println("getNum: Could not convert "+S1+" for "+coord);
				owner.setComError(2);
				// server returned contents, but not numeric
				return (0);
			}
		}
		return (value);
	}
	/**
	 retrieve textual contents, either from owner's table or by converting 
	 a number to text:
	*/
	public String getCont() {
		if (!isNum) {
			if (isLocal) {
					return (String)localList.get(coord);
			}
			
			//   if (owner.getHash()!=null) {
			if (owner.getList().getHash() != null) {
				return ((String) (owner.getList().getHash().get(coord)));
			}
			else {
				return ("????");
			}
		}
		else {
			return (String.valueOf(value));
		}
	}
} /* cellRef */
