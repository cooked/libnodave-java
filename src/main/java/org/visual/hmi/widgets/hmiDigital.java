/*
 A class that provides digital display for numeric values.
 Extra parameters:	Name (displayed before value)
			unit (displayed after value)
			fractional digits (number of frac.digits displayed)    
 
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
package org.visual.hmi.widgets;
import org.visual.hmi.cellRef;

import java.awt.Graphics;
import java.text.NumberFormat;
import java.util.Locale;

import org.visual.hmi.hmiElement;
/** 
    <table><tr><th colspan=2>
    Widget class that provides a digital display.
    Extra parameters:
    <ul>A name to put before the value.
    <ul>A unit to display behind the value.
    <ul>The number of fractional digits.

    </th></tr>
    <tr><th>Demo widget</th><th>Parameters</th></tr>
    <tr><td>
    <applet code="org.visual.hmi.designer.hmiDesigner.class" name="viewer" archive="hmi.jar" width=250 height=250>
    <param name="communicator" value="org.visual.hmi.protocol.hmiNoCI">
    <param name="rights" value="1">
    <param name="font" value="Times">
    <param name="0" value="hmiDigital,0,0,230,40,14,0,9,0,1,15,9,0,7,55.555555,0,100,some Name , units ,1">
    <param name="1" value="hmiDigital,0,42,230,40,14,0,9,0,1,15,9,0,7,55.555555,0,100,speed , rpm ,1">
    
    </applet>
    </td><td>
    <applet code="org.visual.hmi.designer.hmiParameterView.class" name="toolbar" archive="hmi.jar" width=250 height=480>
    <param name="0hmiElement" value="X-Pos">
    <param name="1hmiElement" value="Y-Pos">
    <param name="2hmiElement" value="Width">
    <param name="3hmiElement" value="Height">
    <param name="4hmiElement" value="1. Foreground Color">
    <param name="5hmiElement" value="2. Foreground Color">
    <param name="6hmiElement" value="Background Color">
    <param name="7hmiElement" value="1. Foreground Color">
    <param name="8hmiElement" value="2. Foreground Color">
    <param name="9hmiElement" value="Background Color">
    <param name="10hmiElement" value="1. Foreground Color">
    <param name="11hmiElement" value="2. Foreground Color">
    <param name="12hmiElement" value="Background color">
    <param name="13hmiElement" value="Main value">
    <param name="14hmiElement" value="Minimum">
    <param name="15hmiElement" value="Maximum">
    <param name="16hmiDigital" value="Name">
    <param name="17hmiDigital" value="Unit">
    <param name="18hmiDigital" value="Decimals">
    </applet>
    </td></tr></table>
*/
public class hmiDigital extends hmiElement {
	//String s="0000000000000";
/**
 * dummy variables for XML loader/saver
 */	
	int name;
	int unit;
/**
 * number of decimals to display
 */	
	public int decimals;
	static public String[] varNames2(){
		return new String []{"Name","Unit","Decimals"};	
	};
	static Locale L = Locale.US;
	public static NumberFormat nf = NumberFormat.getInstance(L);

	public int needCells() {
		return super.needCells()+3;
	}

	public void getVal() {
		super.getVal();
		decimals = Cells[18].getInt();
	}

	public void setMore(String s, int n) {
		super.setMore(s, n);
		if ((n == 17)||(n == 18)) {		
			Cells[n-1] = new cellRef(s, this, cellRef.VTTEXT);
		}
		if (n == 19) {
				Cells[18] = new cellRef(s, this, cellRef.VTNUMBER);
		}
	}
	public void paintmore(Graphics g) {
		nf.setMaximumFractionDigits(decimals);
		nf.setMinimumFractionDigits(decimals);
		nf.setGroupingUsed(false);
		String s = nf.format(mainValue);
		int l = s.length();
		if (Cells[needCells()-2].coord != null)
			fitText(g, Cells[needCells()-3].coord + s.substring(0, l) + Cells[needCells()-2].coord);
		else
			fitText(g, Cells[needCells()-3].coord + s.substring(0, l));
	}
}
/**
    changes:
    01-15-2003	do not use grouping
    02-19-2003	we have an instance of numberFormat from start already. Do not get another one.
    05/21/2003	Example: <param> tag must be in a single line or PageViewer doesn't work.
*/