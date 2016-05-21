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

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.Image;
import java.awt.Label;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

/*
    Suppose we will only load files. Otherwise use a full fleshed browser and you can take
    advantage of a fully fleshed http server.
*/
// Our class loader
/*    
    private static class AppletClassLoader extends URLClassLoader {
	AppletClassLoader(URL codebase, String archive) throws IOException {
	    super(new URL[0]);
	    if (archive.equals("")) {
		addURL(codebase);
	    } else {
		for (StringTokenizer t = new StringTokenizer(archive, ", ");
			t.hasMoreTokens(); ) {
		    addURL(new URL(codebase, t.nextToken()));
		}
	    }
	}
    }
*/
public class ViewerAppletContext implements AppletContext {
	private Vector apps = new Vector();
	Label statusBar;
	PageViewer viewer;
	
	public ViewerAppletContext(Label statusBar, PageViewer viewer) {
		this(new Vector(), statusBar, viewer);
	}
	public ViewerAppletContext(
		Vector apps,
		Label statusBar,
		PageViewer viewer) {
		this.statusBar = statusBar;
		this.apps = apps;
		this.viewer = viewer;
	}
	public void addApplet(Applet app) {
		apps.addElement(app);
	}
	public Applet getApplet(String name) {
		for (int i = 0; i < apps.size(); i++) {
			Applet app = (Applet)apps.elementAt(i);
			if (app.getName().equals(name))
				return (app);
		}
		return (null);
	}
	public Enumeration getApplets() {
		return (apps.elements());
	}
	public AudioClip getAudioClip(URL url) {
		return (new AudioPlayer(url));
	}
	public Image getImage(URL url) {
		return (Toolkit.getDefaultToolkit().getImage(url));
	}
	public void showDocument(URL url) {
		showDocument(url, "_self");
	}
	public void showDocument(URL url, String target) {
		//	System.err.println("Not implemented in PageViewer:\n"
		//	    + "showDocument("  + url + ", " + target + ")");
		String[] s = new String[1];
		s[0] = url.getFile();
		try {
			PageViewer.startPage(s);
			System.out.println("target: " + target);
			if (target.equals("_self"))
				viewer.quit();
		}
		catch (Exception e) {
			System.err.println("Exception " + e);
		}
	}
	public synchronized void showStatus(String str) {
		statusBar.setText(" " + str + " ");
		//	statusBar.text=str;
	}
	public void setStream(String s, java.io.InputStream i) {
	}
/**
 * remove this if you want to compile it with older versions of JAVA
 */	

	public java.io.InputStream getStream(String s) {
		return null;
	}
	
	/**
	 * remove this if you want to compile it with older versions of JAVA
	 */	
	
	public java.util.Iterator getStreamKeys() {
		return null;
	}
	
}