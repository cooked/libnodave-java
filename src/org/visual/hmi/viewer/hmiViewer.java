/*
 The hmiViewer.
 
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
package org.visual.hmi.viewer;
import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.visual.hmi.AddressModifier;
import org.visual.hmi.hmiElement;
import org.visual.hmi.hmiList;
import org.visual.hmi.protocol.hmiCommunicationInterface;
/**
    The main applet for an HMI page.
    It loads the widgets given on it's parameter lines, fetches values for their variables
    and calls their display methods.
*/
public class hmiViewer
	extends Applet
	implements hmiList, MouseListener, MouseMotionListener, Runnable //,KeyListener
{
	protected AddressModifier addressModifier = null;
	public AddressModifier getAddressModifier() {
		return addressModifier;
	}
	/**
	    Compile with !=0 to enable debug output.
	*/
	static final int debug = 0;
	/**
	    A background image.
	*/
	public Image bgImage;
	/**
	    Name of the background image as given by parameter "bkgnd".
	*/
	public String background;
	/**
	    The color blink flag.
	*/
	boolean blink;
	/**
	    A reference to the communication Interface.
	*/
	protected hmiCommunicationInterface comInt = null;
	/**
			A reference to the event Translator.
		*/
	//	protected EventTranslator eTranslator = null;
	/**
	    Our applet context.
	*/
	public AppletContext context;
	/**
	    Graphics from dbImage.
	*/
	protected Graphics dbGraphics;
	/**
	    Image for double buffering of the screen.
	*/
	public Image dbImage;
	/**
	    Name of the standard font as given by the parameter "font".
	*/
	public String useFont;
	/**
	    Array of prepared requests used to retrieve variable values from server.
	*/
	//    String cellfh[];
	/**
	    Array of prepared fonts to avoid "new Font". Whenever an instance of the standard font
	    of a specific size is required, it's stored Here. We can assume it will be used again.
	*/
	Font fonts[] = new Font[100];
	/**
	    Stores variable name / value pairs.
	*/
	public Hashtable kHash;
	/**
			Stores variable name / value pairs.
		*/
	public Hashtable localHash;
	/**
	    Used when calling a trend diagram.
	    The URL is formed as <trend>?machine=machine&sources=<var1>,var2>.
	    The purpose is to reuse the same hmi pages for mutiple identical machines with
	    a different machine name. The (CGI) script that delivers the trend page should
	    derive the data storage location from this parameter.
	*/
	public String machine;
	/**
	    Status to show, when there is no valid hint.
	*/
	public String defaultHint;
	/**
	    Refresh time in millliseconds. Defaults to 1000 if not given by the parameter "refresh".
	*/
	public int refresh = 1000;
	/**
	    Our dimensions.
	*/
	public Dimension size;
	/**
	    Does a cyclic screen update.
	*/
	Thread timer = null;
	/**
	    Name of the trend script as given by parameter "trends".
	*/
	public String trendScriptName;
	/**
			Prefix for all images
	*/
	public String imageDir;
	/**
	    Rights as given by the parameter "rights".
	    Currently only the lsbit is used meaning "user may change settings".
	*/
	public int userRights;
	/**
	    Stores the widgets.
	*/
	protected Vector widgets = new Vector();
	/**
	    Calls the trend script.
	*/
	public void callTrend() {
		if (trendScriptName == null)
			return;
		int i;
		boolean first = true;
		String cells = "";
		for (i = 0; i < widgets.size(); i++) {
			if ((((hmiElement)widgets.elementAt(i)).flags & 0x01) != 0) {
				if (!first)
					cells = cells + ',';
				cells =
					cells + ((hmiElement)widgets.elementAt(i)).Cells[13].coord;
				first = false;
			}
		}
		//    try{
		//     System.out.println("Files: "+cells+"\n");
		//     context.showDocument(new URL("http://"+host+trendScriptName+
		//                                "?machine="+machine+
		//                                "&source="+cells));
		//     }
		//     catch (MalformedURLException e){
		//         System.err.println("Malformed URL:"+cells);
		//     }
		showDocument(
			trendScriptName + "?machine=" + machine + "&source=" + cells);
		//     }
	}
	/**
	    Calls C.I.'s destroy() to clean up lesftovers from communication.
	*/
	public void destroy() {
		//  System.out.println("this is destroy()");
		if (comInt != null)
			comInt.destroy();
		comInt = null;
	}
	/**
	    Get the textual contents of variable named k.
	*/
	public String get(String k) {
		return (String) (kHash.get(k));
	}
	public boolean getBlink() {
		return (blink);
	}
	public hmiCommunicationInterface getComInt() {
		return comInt;
	}
	/**
	 Return an istance of the standard Font of size fh either from table or 
	 after creating it. Stores it for later use.
	*/
	public Font getFont(int fh) {
		//  	System.out.println("getFont "+fh);
		if (fonts[fh] == null) {
			fonts[fh] = new Font(useFont, Font.BOLD, fh);
		}
		/*  
			for (int i=0;i<=99;i++) {
			    if (fonts[i]!=null)System.out.println(fonts[i]);
			}
		*/
		return (fonts[fh]);
	}
	public FontMetrics getFontMetrics(int i) {
		Font f = getFont(i);
		//	System.out.println("getFontMetrics Font "+f);
		//	System.out.println("before getFontMetrics");
		FontMetrics fm = getFontMetrics(f);
		//	System.out.println("getFontMetrics done.");
		//	System.out.println("getFontMetrics FontMetr. "+fm);
		return (fm);
	}
	public Hashtable getHash() {
		return (kHash);
	}
	public Image getImage(String s) {
//		System.out.println("hmiViewer.getImage "+s);
		if (imageDir==null) imageDir="";
//		System.out.println("hmiViewer"+ imageDir +s+"getImage");
  		return (getImage(getDocumentBase(), imageDir + s));
//		return (getImage(getDocumentBase(), s));
	}
	
	public int getIntParam(String s, int defaultv) {
		int i;
		try {
			i = Integer.parseInt(getParameter(s));
		}
		catch (NumberFormatException e) {
			i = defaultv;
		}
		return (i);
	}
	
	public boolean getRights(int mask) {
		if ((userRights & mask) == 0) {
			context.showStatus(
				"keine Berechtigung zum Aendern der Einstellung !");
		}
		return ((userRights & mask) != 0);
	}
	public Vector getWidgets() {
		return widgets;
	}
	
	public boolean hasHash() {
		return (kHash != null);
	}
	
	
	
	/**
	    Loads the communication interface. Initializes it using it's init().
	*/
	public void initCom() {
		String s = getParameter("addressModifier");
		if (s!=null)
		try {
			System.out.println("load address modifier: "+s);
			addressModifier =
				(AddressModifier)Class.forName(s).newInstance();
		}
		catch (Exception e) {
			System.out.println("Cannot load address modifier: " +s+": "+e);
		}
		if (addressModifier==null) {
			addressModifier=new AddressModifier();
//			System.out.println("Using default address modifier: " );
		}
		addressModifier.init(this);
		
		s = getParameter("communicator");
		try {
			comInt =
				(hmiCommunicationInterface)Class.forName(s).newInstance();
		}
		catch (Exception e) {
			System.out.println("Cannot load communication interface: " + e);
		}
		if (comInt != null)
			comInt.init(this);
	}	
	
	/**
			Initialization. Loads the communication interface. Initializes it using it's init().
			Parses parameter lines and loads widgets according to them.
			Further Initializes the C.I. using it's init2().
		*/
public void init() {			
		System.out.println("This is hmiViewer.init(). Version 0.0.23");
		System.out.println("   (C) Thomas Hergenhahn  2004");
		initCom();
		/*			
				fontName = getParameter("eventTranslator");
				if (fontName != null) {
					try {
						eTranslator =
							(EventTranslator)Class.forName(fontName).newInstance();
					}
					catch (Exception e) {
						System.out.println("Cannot load event translator " + e);
					}
					if (eTranslator == null) {
						eTranslator = new NoTranslator();
						System.out.println("Using standard event translator ");
					}
				}
				if (eTranslator == null) {
					eTranslator = new NoTranslator();
					System.out.println("Using standard event translator ");
				}
				if (eTranslator != null)
					eTranslator.init(this);
		*/
		defaultHint = getParameter("defaultHint");
		imageDir = getParameter("imageDir");
		useFont = getParameter("font");
		if (useFont == null)
			useFont = "SansSerif";
		//	if (cellfh==null) cellfh=new String[10];
		if (kHash == null)
			kHash = new Hashtable();
		context = getAppletContext();
		if (debug > 0)
			System.out.println("got Context: " + context);
		String s;
		userRights = getIntParam("rights", 0);
		refresh = getIntParam("refresh", 1000);
		int scanUntil = getIntParam("scanUntil", -1);
		machine = getParameter("machine");
		trendScriptName = getParameter("trends");
		if (comInt != null)
			comInt.open();
		background = getParameter("bkgnd");
		//		if (debug > 1)
		//			System.out.println("hmiViewer before getImage");
		if (background != null) {
			bgImage = getImage(getDocumentBase(), background);
			if (bgImage == null)
				System.out.println(
					"hmiViewer: cannot load image: " + background);
		}
		else
			bgImage = null;
		//		if (debug > 1)
		//			System.out.println("hmiViewer after getImage");
		if (scanUntil < 0) {
			do {
				s = getParameter(String.valueOf(widgets.size()));
				if (s != null)
					widgets.addElement(
						hmiElement.newElement(s, this, widgets.size()));
			}
			while (s != null);
		}
		else {
			for (int sc = 0; sc <= scanUntil; sc++) {
				s = getParameter(String.valueOf(sc));
				if (s != null)
					widgets.addElement(hmiElement.newElement(s, this, sc));
			}
		}
		if (comInt != null)
			comInt.initVariableTable();
	}
	/**
	    Search a widget that can handle the mouse click and call it's action method.
	    I think this is less code and time than making each widget an eventListener.
	*/
	public void mouseClicked(java.awt.event.MouseEvent e) {
		int i, j = -1;
		for (i = 0; i < widgets.size(); i++) {
			if (((hmiElement)widgets.elementAt(i)).canHandleEvent(e))
				j = i;
		}
		if (j >= 0)
			 ((hmiElement)widgets.elementAt(j)).action(e);
	}
	/**
		    Do nothing.
	*/
	public void mouseDragged(java.awt.event.MouseEvent e) {
	}
	public void mouseEntered(java.awt.event.MouseEvent evt) {
	}
	public void mouseExited(java.awt.event.MouseEvent evt) {
	}
	/**
	    Displays a hint in status bar, if mouse is in a widget's area.
	*/
	public void mouseMoved(java.awt.event.MouseEvent evt) {
		hmiElement he, ce = null;
		/*
			for (i=0; i<widgets.size(); i++){
			    if (((hmiElement)widgets.elementAt(i)).canHandleEvent(evt )) 
				ce=(hmiElement)widgets.elementAt(i);
		        }
		*/
		for (Enumeration e = widgets.elements(); e.hasMoreElements();) {
			he = (hmiElement)e.nextElement();
//			if (he.canHandleEvent(evt))
			if (he.isEventInArea(evt))
				ce = he;
		}
		if (ce != null) {
			String s = ce.hintString(evt);
			if (debug > 2)
				System.out.println(s);
			if (s != null) {
				context.showStatus(s);
			}
			else {
				context.showStatus(defaultHint);
			}
		}
		else {
			context.showStatus(defaultHint);
		}
	}
	public void mousePressed(java.awt.event.MouseEvent evt) {
	}
	public void mouseReleased(java.awt.event.MouseEvent evt) {
	}
	/**
	    Paint the applet. This paint is called when printing.
	    This is NOT perfect, because image in dbImage may not yet be ready.
	*/
	public void paint(Graphics g) {
		if (dbImage != null)
			g.drawImage(dbImage, 0, 0, this);
	}
	/**
	    Paints background, gray rectangle and background image.
	*/
	public void paintBg(Graphics g) { // copies background image to buffer
		//    System.out.println("hmiViewer.paintBg()");
		g.setColor(Color.lightGray);
		//    System.out.println("hmiViewer.paintBg() after setColor");    
		size = getSize();
		//    System.out.println("hmiViewer.paintBg() "+size.width+" "+size.height);
		//    Long l=System.getTime();
		g.fillRect(0, 0, size.width, size.height);
		//System.out.println("hmiViewer.paintBg() after fillRect");    
		if (bgImage != null)
			g.drawImage(bgImage, 0, 0, this);
		//System.out.println("hmiViewer.paintBg() after drawImage");    
		//System.out.println("hmiViewer.paintBg() ends");	
	}
	/*  
	    public void keyTyped(java.awt.event.KeyEvent e)
	    {
	//       int lk1=e.getKeyCode();
	//       lastKey=e.getKeyText(lk1);
	//       System.out.println("typed");
	//       repaint();
	  }
	*/
	/**
	    Searches a widget that can handle the KeyEvent.
	    May change in future for mouseless operation.
	*/
	public void processKeyEvent(KeyEvent e) {
		int lkc = e.getKeyCode();
		//		int lcc = e.getKeyChar();
		String lastKey = KeyEvent.getKeyText(lkc);
		System.out.println("Pressed: " + lastKey);
		//       switch(lkc){
		//       case KeyEvent.VK_DOWN:  curY++;break;
		//       case 109: next(-1); break;
		//       case 107: next(1);  break;
		//       case KeyEvent.VK_RIGHT: CursX++;break;
		//       case KeyEvent.VK_LEFT:  CursX--;break; 
		//       case KeyEvent.VK_UP:    curY--;break;
		//       case KeyEvent.VK_HOME:  curY=1;curX=1;curY=1;startX=1;startY=1;break;
		//       case KeyEvent.VK_E:     edit();break;
		//       case KeyEvent.VK_G:     gotoK();break;
		//       case KeyEvent.VK_END:   curY=1;curX=xFields;curY=yFields;
		//               startX=10000-xFields;
		//               startY=10000-yFields;
		//               break;                  //end
		//       }
		//       repaint();
		/*
		    for (int i=0; i<widgets.size(); i++){
			if ( ((hmiElement) widgets.elementAt(i)).usesKey(e)) {
			    break;
			}    
		    }
		*/
		for (Enumeration en = widgets.elements(); en.hasMoreElements();) {
			hmiElement he = (hmiElement)en.nextElement();
			if (he.usesKey(e)) {
				break;
			}
		}
	}
	/**
	    Set the variable named k to value v. This is NOT propageted to the server.
	*/
	public void put(String k, String v) {
		kHash.put(k, v);
	}
	
	/*
	    public void keyReleased(java.awt.event.KeyEvent evt) {}
	*/
	/**
	    Let the communication Interface set the variable named k to value v.
	*/
	public synchronized void putVal(String k, double v) {
		if (comInt != null)
			comInt.putVal(k, v);
	}
	/**
	    Runs refresh. Inverts blink flag. Waits till next refresh.
	*/
	public void run() {
		while (timer != null) {
			blink = !blink;
//			if (comInt != null)
//				comInt.getValTab(); // update values
			repaint();
			try {
				Thread.sleep(refresh);
			}
			catch (InterruptedException e) {
				System.out.println("interrupted: " + e);
			}
			//			    	System.out.println("total: "+Runtime.getRuntime().totalMemory()+" free: "+Runtime.getRuntime().freeMemory());
			//    Runtime.getRuntime().gc();
			//    System.out.println("total: "+Runtime.getRuntime().totalMemory()+" free: "+Runtime.getRuntime().freeMemory());
		}
		timer = null;
	}
	public void showDocument(String d) {
		showDocument(d, "_self");
	}
	public void showDocument(String d, String target) {
		try {
			URL u;
			//    	    u= new URL("http",host+"/",d);
			System.out.println("DB:" + getDocumentBase());
			System.out.println("CB:" + getCodeBase());
			u = new URL(getCodeBase() + d);
			System.out.println("URL:" + u);
			context.showDocument(u, target);
		}
		catch (MalformedURLException e) {
			System.out.println("Malformed URL:" + d);
		}
	}
	/**
	    Opens communication link via C.I.'s open().
	    Starts the refresh thread.
	*/
	public void start() {
		//	if (comInt!=null) comInt.open();
		//    	addKeyListener(this);	
		addMouseMotionListener(this);
		addMouseListener(this);
		//		addMouseListener(eTranslator);
		enableEvents(java.awt.AWTEvent.KEY_EVENT_MASK);
		requestFocus();
		if (timer == null) {
			timer = new Thread(this);
			timer.start();
		}
//		System.out.println("this is start()");
	}
	/**
	    Stops communication link via C.I.'s stop().
	    Ends the refresh thread.
	*/
	public void stop() {
		if (comInt != null)
			comInt.stop();
		timer = null;
		//	removeKeyListener(this);	
		removeMouseMotionListener(this);
		//		removeMouseListener(this);
		//	System.out.println("this is stop()");
	}
	/**
	    Let the communication Interface set the variable named k temporary to 1.
	*/
	public void tPutVal(String k) {
		if (comInt != null)
			comInt.tPutVal(k);
	}
	
	/**
		Let the communication Interface get the answer to a request.
	*/
	public String getAnswer(String what) {
		if (comInt != null)
			return comInt.getAnswer(what);
		else
			return "???";			
	}
		
	/**
	    Updates variables and makes the widgets repaint themselves.
	*/
	public void update(Graphics g) {
		//    System.out.println("hmiViewer.update()");
		if (dbImage == null) {
			//	    Dimension size;
			size = getSize();
			//	System.out.println("hmiViewer size for dbImage "+size);
			dbImage = createImage(size.width, size.height);
			//	System.out.println("dbImage "+dbImage);
			dbGraphics = dbImage.getGraphics();
		}
		//    System.out.println("dbGraphics "+dbGraphics);
		//    System.out.println("update before getValTab");
				if (comInt != null)
					comInt.getValTab(); // update values
		//    System.out.println("update after getValTab");
		paintBg(dbGraphics);
		//    System.out.println("update after paintBg");
		/*	
			for (i=0; i<widgets.size(); i++){
			    if (hostError>10) open();
			    ((hmiElement)widgets.elementAt(i)).getval();
		//	System.out.println("hmiViewer fields "+i+" comerror "+widgets.elementAt(i).comError);     
			    if (((hmiElement)widgets.elementAt(i)).comError!=0) 
				((hmiElement)widgets.elementAt(i)).ErrorPaint(dbGraphics);
			else
			    ((hmiElement)widgets.elementAt(i)).paint(dbGraphics);
			}
		*/
		for (Enumeration en = widgets.elements(); en.hasMoreElements();) {
			if (comInt != null)
				if (comInt.getHostError() > 10) {
					comInt.close();
					comInt.open();
				}	
			hmiElement he = (hmiElement)en.nextElement();
			he.getVal();
			if (he.comError != 0)
				he.ErrorPaint(dbGraphics);
			else
				he.paint(dbGraphics);
		}
		//  System.out.println("hmiViewer paint loop ended. "+dbImage);
		//  System.out.println("hmiViewer paint loop ended. Graphics: "+g);
		g.drawImage(dbImage, 0, 0, this);
		//  System.out.println("hmiViewer g.drawImage(dbImage,0,0,this) done.");
		/*
		    if (wasModified){
			System.out.println(get("savesettings",""));
		        wasModified=false;
		    }
		*/
	}
}
/** Changes
 * 07/17/2002	works now without background image
		size is fetched only once in init() // this doesn't work under all circumstances
 * 10/04/2002	put the communication stuff to separate classes.
 * 10/10/2002	fixed a bug in mouseClicked reported by Juan Carlos Orozco.
 * 11/14/2002	callTrend does nothing if no sricpt name is given.
 * 11/14/2002	scanUntil parameter allows you to specify the number of the highest widget.
		This is useful for the following: If you edit a page with a text editor and renove 
		a line, you had to renumber all widgets.
		If gaps were allowed, the viewer would not know where to end.
		You need not to specify scanUntil, but if you do, gaps are allowed.
 * 05/21/2003	Changes to make printing work.
 * 11/21/2003	Implemented Suggestions by Eclipse
*/
