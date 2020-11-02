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
package org.visual.tools;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Panel;
import java.net.URL;
import java.util.Hashtable;

public class ViewerAppletStub extends Panel implements AppletStub {
	AppletContext context;
	URL codebase;
	URL documentBase;
	Hashtable paramDict;
	Dimension preferredSize;
	static final boolean debug=false;
	public ViewerAppletStub(
		URL documentBase,
		AppletTag tag,
		AppletContext context) {
		this.paramDict = tag.getParameters();
		this.context = context;
		this.codebase = tag.getCodebaseURL();
		this.documentBase = documentBase;
		// we are doing our own layout, see doLayout()
		setLayout(null);
	}
	
	public Dimension getMinimumSize() {
		return (getPreferredSize());
	}
	public Dimension getPreferredSize() {
		return (preferredSize);
	}
	public AppletContext getAppletContext() {
		return (context);
	}
	public URL getCodeBase() {
		return (codebase);
	}
	public URL getDocumentBase() {
		return (documentBase);
	}
	public String getParameter(String name) {
		if (paramDict == null) {
			return (null);
		}
		String key = name.toLowerCase();
		String val = (String)paramDict.get(key);
		if (debug) {
			System.out.println("AV: getP: " + key + " " + val);
		}
		return (val);
	}
	public boolean isActive() {
		return (true);
	}
	/**
	 * Resize applet to a given width/height
	 *
	 * We simply set our own size to the desired size.
	 */
	public void appletResize(int width, int height) {
		preferredSize = new Dimension(width, height);
		if (debug) {
			System.out.println("AV: resizing app to " + preferredSize);
		}
		setSize(width, height);
	}
	public void doLayout() {
		super.doLayout();
		// assume applet is our only child, give it all the space we have
		Component c = this.getComponent(0);
		if (c != null) {
			Dimension cs = preferredSize;
			if (cs == null) {
				if (debug)
					System.out.println(
						"AV: no pref size, using current size");
				cs = getSize();
			}
			if (debug) {
				System.out.println("AV: setting child to " + cs);
				System.out.println("AV: my insets " + getInsets());
			}
			c.setBounds(0, 0, cs.width, cs.height);
		}
	}
}