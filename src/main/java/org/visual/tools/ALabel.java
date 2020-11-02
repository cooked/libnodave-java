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
/*
    This is a replacement for Label. It is used as the StatusBar in PageViewer.
    This became necessary because the Applet gets bo more keyEvents after the
    original Label's setText() has been called.
*/
package org.visual.tools;

import java.awt.*;

/**
    This is a replacement for Label. It is used as the StatusBar in PageViewer.
    This became necessary because the Applet gets no more keyEvents after the
    original Label's setText() has been called. This seems to be different with
    Kaffe's Label class.
*/
public class ALabel
  extends Label
{
    String s;
    public void setText(String s) {
	if (s!=null){
	    Graphics g=getGraphics();
	    this.s=s;	
	    if (g!=null) paint(g);
	}
    }
    
/**
    This was necessary to make Kaffe start. Otherwise it could not initialize ALabel.
    It cannot be replaced with non-deprecated getPreferredSize to be compatible with
    Kaffe 1.0.6, because it calls preferredSize().
*/    
    public Dimension preferredSize() {
	int cx = 40;
	int cy = 20;
	return new Dimension( cx, cy);
    }

/**
    This became necessary because Kaffe wants to repaint this class on window events
    and then throwed null pointer exceptions. (beeep!, beeep!)
*/    
    public void paint( Graphics g) {
	    Dimension d=getSize();
	    g.setColor( getBackground() );
	    g.fillRect( 0, 0, d.width, d.height);
	    if (s!=null){
		g.setColor( getForeground() );
		g.drawString(s,0,18);
	    }	
    }	

}

