/**
 *  This is a modified AppletViewer. Modifications:
 *  1. Handle page changes via showDocument().
 *  2. Show "VISUAL's Applet Viewer" in title Bar.
 *  3. Do not show a menu bar. 
 *  
 * Most of the code was taken from Kaffe 1.0.6.
 * The original copyright message follows.
 * I do not include "license.terms" from Kaffe here, because it IS the GPL.
 * Thomas Hergenhahn (thomas.hergenhahn@web.de)
 *  
 * Copyright (c) 1998, 1999
 *	Transvirtual Technologies, Inc.  All rights reserved.
 *
 * See the file "license.terms" for information on usage and redistribution 
 * of this file. 
 *
 * @author J.Mehlitz, G.Back
 *
 * These classes have been rewritten in a way that allow applications
 * to easily embed applets in their own containers
 */
package org.visual.tools;
import java.applet.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
/**
    PageViewer is an extended applet viewer that can handle page changes via showDocument method.
*/
public class PageViewer extends Frame implements //  ActionListener,
WindowListener {
	public static boolean debug = false;
	//    public static boolean debug = true;
	//    static boolean showMenuBar = true;
	static boolean showStatusBar = true;
	Label statusBar = new ALabel();
	Applet app;
	AppletTag tag;
	AppletContext context;
	private static Vector applets = new Vector();
	
	public PageViewer(URL documentBase, AppletTag tag) throws IOException {
		super(/*tag.toString()*/
		"VISUAL's Applet Viewer");
		setLayout(new BorderLayout());
		//    setMenus();
		statusBar.setFont(new Font("SansSerif", Font.BOLD, 12));
		if (showStatusBar) {
			add(BorderLayout.SOUTH, statusBar);
		}
		addWindowListener(this);
		this.tag = tag;
		//
		// we're  creating a new applet context for each applet so that each
		// applet gets its own status bar
		// this is different from a browser page, where multiple applets
		// on the same page would share the same context and status bar
		// to account for that, we tell the context a vector to keep track
		// of all applets that logically belong to the same context
		//
		ViewerAppletContext dcontext =
			new ViewerAppletContext(applets, statusBar, this);
		context = dcontext;
		ViewerAppletStub stub =
			new ViewerAppletStub(documentBase, tag, context);
		app = createApplet(this.tag, stub);
		if (app == null) {
			context.showStatus("Cannot start applet");
			return;
		}
		stub.add(app);
		add(BorderLayout.CENTER, stub);
		//    addNotify(); // a hack, in case the applet creates Graphics during init (no joke)
		Dimension appletSize = tag.getAppletSize();
		stub.appletResize(appletSize.width, appletSize.height);
		app.init();
		app.validate();
		app.start();
		pack();
		setVisible(true);
		if (debug) {
			System.out.println("my size " + getSize());
			System.out.println("stub size " + stub.getSize());
			System.out.println("label size " + statusBar.getSize());
		}
		context.showStatus("Applet started");
	}
	public final static Applet createApplet(AppletTag tag, AppletStub stub) {
		Applet app = null;
		try {
			String code = tag.getCodeTag();
			if (code == null) {
				System.out.println("didn't find code tag");
				System.exit(-1);
			}
			/*
				AppletClassLoader loader = 
				    new AppletClassLoader(tag.getCodebaseURL(), tag.getArchiveTag());
			
				Class c = loader.loadClass(code);
				app = (Applet) c.newInstance();
			*/
			Class c = Class.forName(code);
			app = (Applet)c.newInstance();
			// set applet.name according to <applet name= > tag
			// used in AppletContext.getApplet()
			String appName = tag.getName();
			if (appName != null) {
				app.setName(appName);
			}
			app.setStub(stub);
			// Convenience:
			// if the applet's stub context is one of our default contexts
			// add it to that context.
			AppletContext ctxt = stub.getAppletContext();
			if (ctxt instanceof ViewerAppletContext) {
				((ViewerAppletContext)ctxt).addApplet(app);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return (app);
	}
	public void quit() {
		app.stop();
		app.destroy();
		dispose();
	}
	//private final String TagMenu	= "Tag";
	//private final String QuitMenu	= "Quit";
	//private final String StopMenu	= "Stop";
	//private final String StartMenu	= "Start";
	/*
	public void actionPerformed ( ActionEvent e ) {
		String cmd = e.getActionCommand();
		if ( QuitMenu.equals( cmd)) {
			if (app != null) {
				app.stop();
				app.destroy();
			}
			dispose();
		}
		else if ( StopMenu.equals( cmd)) {
			if (app != null) {
				app.stop();
			}
			context.showStatus( "Applet stopped");
		}
		else if ( StartMenu.equals( cmd)) {
			if (app != null)
				app.start();
			context.showStatus( "Applet started");
		}
		
		else if ( TagMenu.equals( cmd)) {
			System.out.println("printing tags: ");
			System.out.println(tag);
			System.out.println(tag.getParameters());
		}
		
	}
	*/
	public static void startPage(String[] args) throws Exception {
		int width = -1;
		int height = -1;
		String loc = null;
		//    showMenuBar = true;
		showStatusBar = true;
		for (int i = 0; i < args.length; i++) {
			System.out.println("Arg " + args[i]);
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-d")) {
				debug = true;
			}
			else //	if (args[i].equals("-nomenu")) {
				//	    showMenuBar = false;
				//	} else
				if (args[i].equals("-nostatus")) {
					showStatusBar = false;
				}
				else if (args[i].startsWith("-w")) {
					width = Integer.parseInt(args[++i]);
				}
				else if (args[i].startsWith("-h")) {
					height = Integer.parseInt(args[++i]);
				}
				else {
					loc = args[i];
					break;
				}
		}
		if (loc == null) {
			System.out.println("Usage: PageViewer [-debug] <url|file>");
			System.exit(0);
		}
		URLConnection uc = openAppletURLConnection(loc);
		URL documentBase = uc.getURL();
		//		AppletTag[] appletTags = AppletTag.parseForApplets(uc.getInputStream());
		Vector appletTags = AppletTag.parseForApplets(uc.getInputStream());
		//		if (appletTags.length == 0) {
		if (appletTags.size() == 0) {
			System.err.println(
				"Warning: no applets were found."
					+ " Make sure the input contains an <applet> tag");
			System.exit(0);
		}
		//		for (int i = 0; i < appletTags.length; i++) {
		for (int i = 0; i < appletTags.size(); i++) {
			//			AppletTag currentTag = appletTags[i];
			AppletTag currentTag = (AppletTag)appletTags.elementAt(i);
			currentTag.computeCodeBaseURL(documentBase);
			if (debug) {
				System.out.println(
					"AV: effective codebase= " + currentTag.getCodebaseURL());
			}
			// override width/height if user says so
			if (width != -1) {
				currentTag.setAppletWidth(width);
			}
			if (height != -1) {
				currentTag.setAppletHeight(height);
			}
			new PageViewer(documentBase, currentTag);
		}
	}
	public static void main(String[] args) throws Exception {
		startPage(args);
		//    System.out.println("Here we are"); 
	}
	/**
	 * open a url connection to a specified location, file or url.
	 * Use the obtained URLConnection to get the effective documentbase for
	 * the applet (getURL()),  and use getInputStream() to get the html
	 * input to parse for applet tags
	 */
	public static URLConnection openAppletURLConnection(String loc)
		throws IOException {
		URL documentBase;
		try {
			//
			// normally, doing "new URL()" should cause a
			// malformed exception if there's no "protocol:"
			// field.  Currently, Kaffe doesn't, fake one for now
			//
			if (!(loc.startsWith("http:") || loc.startsWith("file:"))) {
				throw new MalformedURLException();
			}
			documentBase = new URL(loc);
		}
		catch (MalformedURLException e) {
			// if it's not a well-formed URL, we assume it's
			// a filename and see where this gets us
			String fullpath;
			// note that getCanonicalPath is not fully implemented
			// yet (as of 7/26/99), but once it is, it should do
			// what we want (I hope)
			if (loc.startsWith(File.separator)) {
				fullpath = new File(loc).getCanonicalPath();
			}
			else {
				fullpath =
					new File(
						System.getProperty("user.dir") + File.separator + loc)
						.getCanonicalPath();
			}
			documentBase = new URL("file", "", fullpath);
		}
		if (debug) {
			System.out.println("AV: reading from URL: " + documentBase);
		}
		// open a connection to determine the real documentBase URL
		return (documentBase.openConnection());
	}
	/*
	void setMenus () {
		// user said -nomenu
		if (showMenuBar == false) {
			return;
		}
		MenuBar mb = new MenuBar();
	
		Menu m = new Menu( "VISUAL's modified AppletViewer");
	//	m.add( "Restart (dummy)");
	//	m.add( "Reload (dummy)");
		m.add( "Stop");
	//	m.add( "Save... (dummy)");
		m.add( "Start");
	//	m.add( "Clone... (dummy)");
		m.addSeparator();
	//	m.add( TagMenu);
		m.add( "Info... (dummy)");
	//	m.add( "Edit (dummy)");
	//	m.add( "Character Encoding (dummy)");
		m.addSeparator();
	//	m.add( "Print... (dummy)");
		m.addSeparator();
	//	m.add( "Properties... (dummy)");
		m.addSeparator();
	//	m.add( "Close (dummy)");
		m.add( "Quit");
	
		m.addActionListener( this);
		
		mb.add( m);
	
		setMenuBar( mb);
	}
	*/
	public void windowActivated(WindowEvent evt) {
	}
	public void windowClosed(WindowEvent evt) {
	}
	public void windowClosing(WindowEvent e) {
		if (app != null) {
			app.stop();
			app.destroy();
		}
		dispose();
	}
	public void windowDeactivated(WindowEvent evt) {
	}
	public void windowDeiconified(WindowEvent evt) {
	}
	public void windowIconified(WindowEvent evt) {
	}
	public void windowOpened(WindowEvent evt) {
	}
}
