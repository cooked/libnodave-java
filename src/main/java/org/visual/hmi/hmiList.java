/*
 This interface contains what single widgets can expect from their owner.
 
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.image.ImageProducer;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import org.visual.hmi.protocol.hmiCommunicationInterface;
/**
    This gives the widgets access to services provided by the hmiViewer.
*/
public interface hmiList{
    /**
	Return a reference to the variable-value hashtable.
    */
    abstract public Hashtable getHash();
    /**
		Set variable identified by key k to string v.
    */
    abstract public void put (String k,String v);
	/**
		Set variable identified by key k to value v.
		This is used by widgets that set a value.
    */
    abstract public void putVal(String k,double v);
	/**
		Get a single line answer for a single line request
	*/
	abstract public String getAnswer(String request);
    /**
		Temporarily set variable identified by key k to 1.
		This is useful with pushbuttons to make them "spring back".
		Viewer and server are together responsible to set it back to zero.
    */
    abstract public void tPutVal(String k);
    /**
	Show document d in same window, i.e. goto this document.
    */
    abstract public void showDocument(String d);
    /**
	Show document d int window target.
    */
    abstract public void showDocument(String d,String target);
    /**
	Return the value for key k from variable-value hash.
    */
    abstract public String get (String k);
    /**
	Calls a URL that should display a trend diagram based on:
	<li>Script name from parameter "trends".</li>
	<li>Machine name from parameter "machine".</li>
	<li>The variables assigned to the main value of the widget, that triggered this call
	and all marked widgets.</li>
    */
    abstract public void callTrend();
    /**
	Returns the state of the blink flag.
    */
    abstract public boolean getBlink();
    /**
	Returns whether a corresponding right is set in rights as given in parameter "rights".
    */
    abstract public boolean getRights(int mask);
    /**
	Returns FontMetrics for standard font when scaled to size i.
    */
    abstract public FontMetrics getFontMetrics(int i); 
    /**
	Returns Font instance of standard font scaled to size i.
    */
    abstract public Font getFont(int i); 
    /**
	Returns Image identified by name s.
    */
    abstract public Image getImage(String s); 
    /**
	<table><tr><td bgcolor=0000A0>
	<font  color=white>the EGA dark blue., color number 1</font>
	</td></tr></table>
    */
    final static Color myblue=new Color(0,0,0xa0);
    /**
	<table><tr><td bgcolor=00AF00>
	<font  color=white>The EGA dark green, color number 2</font>
	</td></tr></table>
    */
    final static Color mygreen=new Color(0,0xaf,0);
    /**
	<table><tr><td bgcolor=B00000>
	<font  color=white>The EGA dark red, color number 4</font>
	</td></tr></table>
    */
    final static Color myred=new Color(0xb0,0,0);
    /**
	<table><tr><td bgcolor=ffc000>
	<font  color=black>The 1st color for blinking fields, color number 3 replaces EGA dark cyan.</font>
	</td></tr></table>
    */
    final static Color myblink1=new Color(0xff,0xc0,0);
    /**
	<table><tr><td bgcolor=20ff20>
	<font  color=black>The 2nd color for blinking fields, color number 3 replaces EGA dark cyan.</font>
	</td></tr></table>
    */
    final static Color myblink2=new Color(0x20,0xff,0x20);
    /**
	<table><tr><td bgcolor=7f007f>
	<font  color=white>The EGA dark magenta, color number 5</font>
	</td></tr></table>
    */
    final static Color mymagenta=new Color(0x7f,0,0x7f);
    /**
	Return an integer derived from the parameter s or defaultValue if not possible.
    */
    abstract public int getIntParam(String s,int defaultValue);
    /**
	Return the value ofe parameter s.
    */
    abstract public String getParameter(String s);
    /**
	Return a reference to the communication interface. This is used by hmiTrend to do it's own communication.
    */    
    abstract public hmiCommunicationInterface getComInt();
	/**
		Return the code base
	*/	  
    abstract public URL getCodeBase();
	/**
		Return the document base
	*/
    public URL getDocumentBase();
	/**
		load an Image
	*/
    abstract public Image getImage(URL u, String s);
	/**
		get the drawing panel size
	*/
    abstract public Dimension getSize();
	/**
		create an an Image with given dimensions
	*/
    abstract public Image createImage(int w,int h);
	/**
		create an an Image from a given source
	*/
	abstract public Image createImage(ImageProducer ip);
	/**
		access the widgets list
	*/
    abstract public Vector getWidgets();
	/**
		access the AddressModifier
	*/
    abstract public AddressModifier getAddressModifier();
}
/** Changes
  * 11/25/2003	Introduced address modifiers
  * 12/07/2003	Added creteImage(ImageProducer)
*/