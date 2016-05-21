/*
 A class that provides a coded switch. This one respects a lower limit.
  
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
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import org.visual.hmi.cellRef;
import org.visual.hmi.hmiElement;
/** 
    <table><tr><th colspan=2>
    Widget class that provides a digitally coded switch. Combine them to get a multidigit switch.
    </th></tr>
    <tr><th>Demo widget</th><th>Parameters</th></tr>
    <tr><td>
    <applet code="org.visual.hmi.designer.hmiDesigner.class" archive="hmi.jar" name="viewer" width=90 height=480>
    <param name="communicator" value="org.visual.hmi.protocol.hmiNoCI">
    <param name="rights" value="1">
    <param name="font" value="Times">
    <param name="0" value="hmiRollSwitch2,0,0,30,120,15,0,9,15,0,9,15,0,9,A56,124,999,2">
    <param name="1" value="hmiRollSwitch2,30,0,30,120,15,0,9,15,0,9,15,0,9,A56,124,999,1">
    <param name="2" value="hmiRollSwitch2,60,0,30,120,15,0,9,15,0,9,15,0,9,A56,124,999,0">
    </applet>
    </td><td>
    <applet code="org.visual.hmi.designer.hmiParameterView.class" archive="hmi.jar" name="toolbar" width=220 height=480>
     </applet>
    </td></tr></table>
*/
public class hmiRollSwitch2 extends hmiElement {
	double increment;
	int xmlDigit;
	public int needCells() {
		return super.needCells() + 1;
	}
	public void setMore(String s, int n) {
		super.setMore(s, n);
		if (n == 17) {
			Cells[16] = new cellRef(s, this, cellRef.VTNUMBER);
		}
	}
	public void getVal() {
		super.getVal();
		increment = 1.0;
		xmlDigit = Cells[16].getInt();
		int i;
		//,digit=(int)minimum;
		if (xmlDigit > 0)
			for (i = 0; i < xmlDigit; i++) {
				increment *= 10.0;
			}
		if (xmlDigit < 0)
			for (i = 0; i > xmlDigit; i--) {
				increment /= 10.0;
			}
	}
/*	
	public String hintString(MouseEvent e) {
		return super.hintString(e) + increment;
	}
*/	
	public void action(MouseEvent e) {
		int relY = e.getY() - y;
		if (owner.getRights(0x01)) {
			if (relY < sy / 2)
				mainValue = mainValue + increment;
			else {
				mainValue = mainValue - increment;
			}
			if (mainValue < 0)
				mainValue = 0;
			if (mainValue > maximum)
				mainValue = maximum;
			if (mainValue < minimum)
				mainValue = minimum;
			owner.putVal(Cells[13].coord, mainValue);
		}
	}
	
	public void paint(Graphics g) {
		background(g);
		double ww = mainValue, w = mainValue;
		int i;
		if (xmlDigit > 0)
			for (i = 0; i < xmlDigit; i++) {
				w /= 10.0;
			}
		if (xmlDigit < 0)
			for (i = 0; i > xmlDigit; i--) {
				w *= 10.0;
			}
		int k = ((int)w) % 10;
		setGroupedColor(1, g);
		int y8 = sy / 8;
		int x5 = sx / 5;
		fitText(
			g,
			String.valueOf(k),
			x + x5,
			y + y8,
			sx - 2 * x5,
			sy - 2 * y8);
		setGroupedColor(2, g);
		g.fillRect(x + x5, y + y8, sx - 2 * x5, y8);
		g.fillRect(
			x + x5,
			y + sy - 3 * y8,
			sx - 2 * x5,
			y8);
		mainValue = ww;
	}
}
