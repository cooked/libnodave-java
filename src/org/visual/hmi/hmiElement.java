/*
 This class is the mother of all HMI elements.
 
 Part of VISUAL, a human machine interface and data acquisition program
 
 (C) Thomas Hergenhahn (thomas.hergenhahn@web.de) 2001..2004

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
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;
/**
     hmiElement is the prototype for all dynamic widgets in an HMI page.
*/
public class hmiElement implements cellRefHolder {
	public AddressModifier getAddressModifier() {
		return owner.getAddressModifier();
	}

	/** change to choose debug level */
	static final int debug = 0;
	/** 
	    A Number from the parameter name,
	    used to retrieve individual hint texts.
	*/
	public int elNumber;
	/** 
		Specific needs for EventTranslation,
	*/
	public int eventNeeds = 0;
	/** 
	    Upper left corner.
	*/
	public int x;
	public int y;
	/** 
	    Extension to the right and down.
	*/
	public int sx;
	public int sy;
	/** Colors, -1 meaning don't draw, 0-15 are EGA colors, everything else is suposed to represent 256^2*red +256*green +blue.    
		<table>
		<tr><th>value</td><th>meaning/color</th></tr>
		<tr><td>-1</td><td><do not draw></td></tr>
		<tr><td>0</td><td bgcolor=000000><font  color=white>black</font></td></tr>
		<tr><td>1</td><td bgcolor=0000A0><font  color=white>the EGA dark blue</font></td></tr>
		<tr><td>2</td><td bgcolor=00A000><font  color=white>the EGA dark green</font></td></tr>
		<tr><td>3</td><td bgcolor=ffc000>
		<font  color=black>Blinking between this</font></td>
		<td bgcolor=20ff20><font color=black> and this color</font></td></tr>
		<tr><td>4</td><td bgcolor=B00000><font  color=white>the EGA dark red</font></td></tr>
		<tr><td>5</td><td bgcolor=B000B0><font  color=white>the EGA dark magenta</font></td></tr>
		<tr><td>6</td><td bgcolor=orange><font  color=white>JAVA orange near EGA orange/brown</font></td></tr>
		<tr><td>7</td><td bgcolor=lightgray><font  color=black>JAVA/EGA lightgray</font></td></tr>
		<tr><td>8</td><td bgcolor=darkgray><font  color=white>JAVA/EGA darkgray</font></td></tr>
		<tr><td>9</td><td bgcolor=blue><font  color=white>JAVA/EGA blue</font></td></tr>
		<tr><td>10</td><td bgcolor=green><font  color=white>JAVA/EGA green</font></td></tr>
		<tr><td>11</td><td bgcolor=cyan><font  color=black>JAVA/EGA cyan</font></td></tr>
		<tr><td>12</td><td bgcolor=red><font  color=black>JAVA/EGA red</font></td></tr>
		<tr><td>13</td><td bgcolor=magenta><font  color=black>JAVA/EGA magenta</font></td></tr>
		<tr><td>14</td><td bgcolor=yellow><font  color=black>JAVA/EGA yellow</font></td></tr>
		<tr><td>15</td><td bgcolor=white><font  color=black>JAVA/EGA white</font></td></tr>
		<tr><td>FF8020</td><td bgcolor=FF8020><font  color=black>treated as RGB triple</font></td></tr>
		</table>
	*/
	public int colorCodes[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	/**
	    Error state. 0=o.k.    
	*/
	public int comError;
	/** currently, only one bit is used to mark that the main value will be included into the next trend screen. */
	public int flags = 0;
	/** The cellRefs, one for each parameter */
	public cellRef Cells[];
	/** limit for color change */
	/** Value to display */
	public double mainValue; // value do display

	public double minimum;
	public double maximum;
	/** something like a hmiViewer applet that provides the environment */
	public hmiList owner;
	/** a "do not paint" flag */
	public boolean nopaint;
	/**
	Tell whether event occured in our area.
	*/
	public boolean canHandleEvent(MouseEvent evt) {
		return isEventInArea(evt);
	}
	/**
	Tell whether event occured in our area.
	*/
	public boolean isEventInArea(MouseEvent evt) {
		int ex = evt.getX() - x;
		int ey = evt.getY() - y;
		return ((ex > 0) && (ex < sx) && (ey > 0) && (ey < sy));
	}
	/**
	* Creates a derived class from the widgets name and initializes it.
	*/
	public static hmiElement newElement(String s, hmiList owner, int number) {
		String t;
		int tcount = 0;
		hmiElement he = null;
		StringTokenizer st = new StringTokenizer(s, ",");
		while (st.hasMoreTokens()) {
			t = st.nextToken();
			if (tcount == 0) {
				if (t.indexOf(".") < 0) {
					t = "org.visual.hmi.widgets." + t;
				}
				try {
					he = (hmiElement) Class.forName(t).newInstance();
				} catch (Exception e) {
					System.out.println(e);
					he = new hmiElement();
				}
				he.owner = owner;
				he.colorCodes = new int[9];
				he.elNumber = number;
				if (debug > 0)
					System.out.println("needCells: " + he.needCells());
				he.Cells = new cellRef[he.needCells()];
			} else {
				//
				// Call parameter digestion. Derived classes may implement more.
				//	    
				he.setMore(t, tcount);
			}
			tcount++;
		}
		if (tcount <= he.needCells())
			System.out.println(
				he.getClass().getName()
					+ " "
					+ number
					+ ": "
					+ (he.needCells() - tcount + 1)
					+ " parameters missing ");
		return he;
	}
	/**
	    Tell owner, how many cellRefs we will need.<br>
	    Overwrite this method in classes that need more than the 16 
	    standard parameters. 
	    See hmiLimited for an example.
	 */
	public int needCells() {
		return (16);
	}
	/**
	    Get any extra parameters.<br>
	    Overwrite this method in classes that need more than the 16 
	    standard parameters. 
	    Call super.setMore() first.
	    See hmiLimited for an example.
	 */
	public void setMore(String s, int n) {
		//	System.out.println(this+" "+n+":"+s)
		if (n <= 16) {
			;
			int t;
			if (n <= 4)
				t = cellRef.VTINT;
			else if (n <= 13)
				t = cellRef.VTCOLOR;
			else
				t = cellRef.VTNUMBER;
			Cells[n - 1] = new cellRef(s, this, t);
		}
		if (n > needCells())
			System.out.println(
				"Hint: "
					+ this.getClass().getName()
					+ " "
					+ elNumber
					+ ":   superfluos parameter "
					+ n
					+ ":"
					+ s);
	}
	/**
	  Get a hint string.
	  Overwrite this in classes that give hints to the user.
	  This looks in applet parameters first for the combination
	  <className>+"Hint"+<element Number>. This allows you to assign individual hints
	  for each widget. If this is not found, It looks for <className>+"Hint"
	  which should be a generic hint for that widget type. Example:<br>
	  If you put in your applet tag:<br>
	  name="hmiPotiHint" value="Click left turn left, right to turn right", this
	  text will be shown for each poti.<br>
	  If you put in your applet tag:<br>
	  name="Hint12" value="Click left to decrease speed of mixer, right to increase it"
	  , this text will be shown for the element created with name="12".<br>
	  
	*/
	public String hintString(MouseEvent e) {
		Class cs = this.getClass();
		String hint;
		hint = owner.getParameter("Hint" + elNumber);
		//	System.out.println("try : hint"+elNumber);
		if (hint == null) {
			String className = cs.getName();
			if (className.startsWith("org.visual.hmi.widgets"))
				className = className.substring(className.lastIndexOf(".") + 1);
			//	    System.out.println("try :"+ cs.getName()+"Hint");
			hint = owner.getParameter(className + "Hint");
		}
		if (debug > 0)
			System.out.println("hmiElement hint: " + hint + " " + elNumber);
		return hint;
	}
	/**
	    Sets communication error
	*/
	public void setComError(int flag) {
		comError = flag;
		//	comError|=flag;	
	}
	/**
	    Returns a reference to owner's Variable/Value Hashtable.
	*/
	public hmiList getList() {
		return owner;
	}
	/**
	    This draws a knob (ellipsis with border) with color value numbers given in "body" and "border".
	    It's here because all sorts of round switches and potentiometers
	     can use it.
	*/
	public void knob(Graphics g, int body, int border) {
		setGroupedColor(body, g);
		g.fillOval(x + sx / 10, y + sy / 10, sx * 4 / 5, sy * 4 / 5);
		setGroupedColor(border, g);
		g.drawOval(x + sx / 10, y + sy / 10, sx * 4 / 5, sy * 4 / 5);
	}
	/**
	     Places a text into the greatest possible rectangle:
	 */
	public void fitText(Graphics g, String S) {
		fitText(g, S, x, y, sx, sy);
	}
	/**
	 *    Places a text into a given rectangle. This is currently the standard method
	 *   of drwaing text in VISUAL?s HMIs.   
	 */
	public void fitText(Graphics g, String S, int x, int y, int xs, int ys) {
		if (S == null)
			return;
		if (S.length() == 0)
			return;
		int xfs, yfs, i, ffs = 99;
		// 	System.out.println("fitText: "+x+","+y+" "+xs+","+ys);
		FontMetrics FM = owner.getFontMetrics(ffs);
		//  	System.out.println("fitText: "+FM);
		i = FM.stringWidth(S);
		//  	System.out.println("stringWidth: "+i);
		xfs = ffs * (xs - 2) / i;
		i = FM.getHeight();
		yfs = ffs * (ys - 0) / i;
		if (xfs > yfs)
			xfs = yfs;
		if (xfs > 99)
			xfs = 99;
		if (xfs <= 0)
			xfs = 1;
		FM = owner.getFontMetrics(xfs);
		g.setFont(owner.getFont(xfs));
		g.drawString(S, x + 2, y + FM.getAscent() + (ys - FM.getHeight()) / 2);
	};
	/**
	    This sets one out of 9 color that come from the widgets nine
	    color parameters.
	*/
	public void setParameterColor(int c, Graphics g) {
		int cc = 1;
		cc = colorCodes[c - 1];
		setCodedColor(cc, g);
	}

	/**
			This sets one out of the 9 color that come from the widgets nine
			color parameters.
	*/
	public Color getParameterColor(int c) {
		int cc = 1;
		cc = colorCodes[c - 1];
		return getCodedColor(cc);
	}
	/**
	  Sets one out of 16 colors (0..15) or -1 for don't paint.
	  The 16 colors are the standard EGA palette, only the dark cyan 
	  is replaced 
	  by blinking lightgreen/orange.
	*/
	public void setCodedColor(int c, Graphics g) {
		nopaint = false; //default
		if (c == -1)
			nopaint = true;
		else
			g.setColor(getCodedColor(c));
	}
	/**
	 Returns one out of 16 colors (0..15) or -1 for don't paint.
	  The 16 colors are the standard EGA palette, only the dark cyan 
	  is replaced 
	  by blinking lightgreen/orange.
	*/
	public Color getCodedColor(int c) {
		switch (c) {
			case -1 :
				return null;
			case 0 :
				return Color.black;
			case 1 :
				return hmiList.myblue;
			case 2 :
				return hmiList.mygreen;
			case 4 :
				return hmiList.myred;
			case 3 :
				if (owner.getBlink())
					return hmiList.myblink1;
				else
					return hmiList.myblink2;
			case 5 :
				return hmiList.mymagenta;
			case 6 :
				return Color.orange;
			case 7 :
				return Color.lightGray;
			case 8 :
				return Color.darkGray;
			case 9 :
				return Color.blue;
			case 10 :
				return Color.green;
			case 11 :
				return Color.cyan;
			case 12 :
				return Color.red;
			case 13 :
				return Color.magenta;
			case 14 :
				return Color.yellow;
			case 15 :
				return Color.white;
			default :
				return new Color(c);
		}
	}

	/**
	  this is the standard methode for all elements that use the standard
	  color scheme:
	  Color nr 1 foreground ,2 frame/scale, 3 background for values below minimum
	  Color nr 4 foreground ,5 frame/scale, 6 background for values in normal range 
	  Color nr 7 foreground ,8 frame/scale, 9 background for values above maximum
	*/
	public void setGroupedColor(int c, Graphics g) {
		if (mainValue >= minimum)
			c = c + 3;
		if (mainValue > maximum)
			c = c + 3;
		setParameterColor(c, g);
	}
	/**
	  * getval transfers the updated values into the specific variables: Widgets that use
	  * additional parameters must override it and should call their anchestors getval first,
	*/
	public void getVal() {
		comError = 0;
		x = Cells[0].getInt();
		y = Cells[1].getInt();
		sx = Cells[2].getInt();
		sy = Cells[3].getInt();

		for (int i = 0; i < 9; i++)
			colorCodes[i] = Cells[i + 4].getInt();

		mainValue = Cells[13].getNum();
		minimum = Cells[14].getNum();
		maximum = Cells[15].getNum();
	}
	/**
	  The paint methode. As most elements have a unicolor background and an
	  enclosing rectangle "frame" and draw there private parts with foreground
	  color, we do here as much as possible for most elements: Draw background,
	  set foreground color, paint the rest with paintmore().
	    Overwrite this
	  if you dont want that, overwrite paint().
	 */
	public void paint(Graphics g) {
		background(g);
		setGroupedColor(1, g);
		paintmore(g);
	}
	/**
	    Overwrite this to draw your widget's special appearence on the prepared
	    background rectangle.
	*/
	public void paintmore(Graphics g) {
	}
	/**
	    Overwrite this to draw something else than the standard background 
	    rectangle.
	*/
	public void background(Graphics g) {
		setGroupedColor(3, g);
		if (!nopaint)
			g.fillRect(x, y, sx, sy);
		setGroupedColor(2, g);
		if (!nopaint) {
			g.drawRect(x, y, sx, sy);
		}
	}
	/**
	    Called in case of communication error. 
	*/
	public void ErrorPaint(Graphics g) {
		g.setColor(Color.red);
		g.fillRect(x, y, sx, sy);
		g.setColor(Color.white);
		g.drawRect(x, y, sx, sy);
		switch (comError) {
			case 1 :
				fitText(g, "timeout", x, y, sx, sy);
				break;
			case 2 :
				fitText(g, "not a number", x, y, sx, sy);
				break;
			case 3 :
				fitText(g, "host!", x, y, sx, sy);
				break;
			case 4 :
				fitText(g, Cells[13].coord+ " unknown!", x, y, sx, sy);
				break;
			case 5 :
				fitText(g, "???", x, y, sx, sy);
				break;
		}
	}
	/**
	    This performs actions. Standard is
	    to call the trend script for left button and to mark the element
	    to be included into next trend call for right button.
	 */
	public void action(MouseEvent e) {
		if (e.isMetaDown())
			flags = flags ^ 0x01;
		else {
			flags = flags | 0x01;
			owner.callTrend();
		}
	}
	/**
	    Returns true, if the widget can perform an action on a given KeyEvent.
	*/
	public boolean usesKey(KeyEvent e) {
		return false;
	}
} /*class hmiElement */
/**
    Changes:
    02-19-2003	1. Because the parameter name is a unique number, the class name is not
		necessary to retrieve a hint name. The naming rule for hints was changed from
		classname+Hint+number to Hint+number.
		2.1. Print the exception text, when a class cannot be loaded.
		2.2. Return hmiElement instead of that class, so the applet can start.
    11-20-2003	This Version is devided in packages as usual in JAVA projects. Hence classnames
		become fully qualified names. If nothing would have been changed, hint 
		parameter names	would automatically be derived from fully qualified names.
		For widgets from the standard path, this is abbreviated to the short form.
	11-27-2003	fitText() returns without painting anything, if the string 
		is of zero length. Better than throwin division by zero.
	04-30-2004	Some things renamed to more "speaking" names.	
*/