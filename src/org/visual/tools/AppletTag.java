/**
 * This class is stolen from Kaffe's library to satisfy the needs of 
 * appletviewer also stolen from Kaffe. Original copyright notice:
 * 
 * Copyright (c) 1998
 *	Transvirtual Technologies, Inc.  All rights reserved.
 *
 * See the file "license.terms" for information on usage and redistribution 
 * of this file. 
 *
 * @author J.Mehlitz, G.Back
 */
package org.visual.tools;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
/**
    This class is stolen from Kaffe's library to satisfy the needs of 
    appletviewer stolen from Kaffe.
*/
public class AppletTag {
	/**
	    Compile with !=0 to enable debug output.
	*/
	static final int debug = 0;
	private Hashtable paramDict = new Hashtable();
	private String codebase = "";
	private URL codebaseURL;
	private String archive = "";
	public String code;
	private String name;
	private Dimension appletSize = new Dimension(200, 200);
	public AppletTag(
		String code,
		String codebase,
		String archive,
		String name,
		Hashtable paramDict,
		Dimension appletSize) {
		this.code = code;
		this.codebase = codebase;
		this.name = name;
		this.archive = archive;
		this.paramDict = paramDict;
		this.appletSize = appletSize;
	}
	public Hashtable getParameters() {
		return (paramDict);
	}
	public Dimension getAppletSize() {
		return (appletSize);
	}
	public void setAppletWidth(int width) {
		appletSize.width = width;
	}
	public void setAppletHeight(int height) {
		appletSize.height = height;
	}
	public String getName() {
		return (name);
	}
	public String getCodeTag() {
		return (code);
	}
	public String getCodebaseTag() {
		return (codebase);
	}
	public URL getCodebaseURL() {
		// if computeCodeBaseURL has not been invoked, construct
		// a URL directly from the codebase
		if (codebaseURL == null) {
			try {
				codebaseURL = new URL(codebase);
			}
			catch (MalformedURLException e) {
			}
		}
		return (codebaseURL);
	}
	public String getArchiveTag() {
		return (archive);
	}
	public String toString() {
		return "applet tag: codebase="
			+ codebase
			+ " archive= "
			+ archive
			+ " code= "
			+ code
			+ " appletSize= "
			+ appletSize;
	}
	/**
	 * given a documentbase and the current (parsed) value of codebase 
	 * --- if one was given --- compute the full url for codebase as
	 * retrieved by getCodebaseURL()
	 */
	public void computeCodeBaseURL(URL documentBase)
		throws MalformedURLException {
		// no codebase given, default to documentbase
		String tcb = codebase;
		if (tcb == null || tcb.equals("")) {
			if (documentBase.getFile().endsWith("/")) {
				codebaseURL = documentBase;
			}
			else {
				String s = documentBase.getFile();
				// determine basename for file by stripping everything
				// past the last slash
				int spos = s.lastIndexOf('/');
				if (spos != -1) {
					s = s.substring(0, spos + 1);
				}
				codebaseURL =
					new URL(
						documentBase.getProtocol(),
						documentBase.getHost(),
						documentBase.getPort(),
						s);
			}
		}
		else {
			// codebase was given, put it in context to documentBase
			if (!tcb.endsWith("/")) {
				codebaseURL = new URL(documentBase, tcb + "/");
			}
			else {
				codebaseURL = new URL(documentBase, tcb);
			}
		}
	}
	public void parseParam(StreamTokenizer st) throws IOException {
//		System.out.println("parseParam");
		String key = null;
		String val = null;
		while (st.nextToken() != '>') {
//			System.out.println("param "+key+" "+st.sval);
			if (st.sval == null) {
				continue;
			}
			if (st.sval.equals("name")) {
				st.nextToken();
				st.nextToken();
				key = new String(st.sval);
			}
			else if (st.sval.equals("value")) {
				st.nextToken();
				st.lowerCaseMode(false);
				st.nextToken();
				/*			
							if ( st.ttype == StreamTokenizer.TT_NUMBER) {
								System.out.println("NUmber! "+key+" "+st.sval);
								int r = (int)st.nval;
								if (Math.abs(r - st.nval) < 1e-7) {	// hmmm
									val = Integer.toString(r);
								} else {
									val = Double.toString( st.nval);
								}
							}
							else {
				*/
				val = new String(st.sval);
//				if (debug > 0)
//					System.out.println("got string: " + val);
				//			}
				st.lowerCaseMode(true);
			}
			else {
				val = new String(st.sval);
				System.out.println(
					"got extra string: "
						+ val
						+ ". Please make sure that a parameter definition is on a single line!");
			}
		}
		if (key != null && val != null) {
			key = key.toLowerCase();
			//System.out.println( "put: " + key + " " + val);
			paramDict.put(key, val);
		}
	}
	/**
	 * 
	 * 
	 * @param st
	 * @throws IOException
	 */
	public AppletTag(StreamTokenizer st) throws IOException {
		while (st.nextToken() != '>') {
			if (st.sval == null) {
				continue;
			}
			switch (st.ttype) {
				case StreamTokenizer.TT_WORD :
					if (st.sval.equals("codebase")) {
						st.lowerCaseMode(false);
						st.nextToken();
						st.nextToken();
						this.codebase = new String(st.sval);
						st.lowerCaseMode(true);
					}
					else if (st.sval.equals("archive")) {
						st.lowerCaseMode(false);
						st.nextToken();
						st.nextToken();
						this.archive = new String(st.sval);
						st.lowerCaseMode(true);
					}
					else if (st.sval.equals("name")) {
						st.lowerCaseMode(false);
						st.nextToken();
						st.nextToken();
						this.name = new String(st.sval);
						st.lowerCaseMode(true);
					}
					else if (st.sval.equals("code")) {
						st.lowerCaseMode(false);
						st.nextToken();
						st.nextToken();
						this.code = new String(st.sval);
						if (code.endsWith(".class")) {
							code = code.substring(0, code.length() - 6);
						}
						st.lowerCaseMode(true);
					}
					else if (st.sval.equals("height")) {
						st.nextToken();
						st.nextToken();
						/*				
										if (st.ttype == StreamTokenizer.TT_NUMBER) {
											System.out.println("NUmber! "+st.sval);
											appletSize.height = (int)st.nval;
										}
										else {
											System.out.println("NoNUmber! "+st.sval);
						*/
						appletSize.height = Integer.parseInt(st.sval);
						//				}
					}
					else if (st.sval.equals("width")) {
//						System.out.println("w NUmber! " + st.sval);
						st.nextToken();
						st.nextToken();
						/*				
										if (st.ttype == StreamTokenizer.TT_NUMBER) {
											System.out.println("NUmber! "+st.sval);
											appletSize.width = (int)st.nval;
										}
										else {
											System.out.println("NoNUmber! "+st.sval);
						*/
						appletSize.width = Integer.parseInt(st.sval);
						//				}
					}
					break;
				default :
					break;
			}
		}
	}
	/**
	 * Construct a tokenizer to parse applet tags.
	 * do  not treat numbers as something special
	 * I think this cleaner than converting back an forth from numeric 
	 * values.
	 */
	public static StreamTokenizer getTokenizer(InputStream is) {
		Reader r = new BufferedReader(new InputStreamReader(is));
		StreamTokenizer st = new StreamTokenizer(r);
		/* this doesn't help when parameter tag spans mutiple lines. */
		//    st.eolIsSignificant(false);
		st.lowerCaseMode(true);
		st.ordinaryChar('/');
		st.ordinaryChar('\'');
		st.ordinaryChar('-');
		st.ordinaryChar('.');
		st.ordinaryChars('0', '9');
		st.wordChars('-', '-');
		st.wordChars('.', '.');
		st.wordChars('0', '9');
		return st;
	}
	/**
	 * parse a given stream for <applet> tags and construct an array of
	 * all tags found.
	 *
	 * Closes stream after parsing.
	 */
	public static Vector parseForApplets(InputStream is) throws IOException {
		AppletTag currentTag = null;
		Vector tags = new Vector();
		int ttype;
		StreamTokenizer st=getTokenizer(is);
		while ((ttype = st.nextToken()) != StreamTokenizer.TT_EOF) {
			if (ttype == '<') {
				ttype = st.nextToken();
				if (ttype == StreamTokenizer.TT_WORD) {
					if (st.sval.equals("applet")) {
						currentTag = new AppletTag(st);
					}
					else if (st.sval.equals("param")) {
						if (currentTag != null) {
							currentTag.parseParam(st);
						} // ignore <PARAM> outside of <APPLET>
					}
				}
				else if (ttype == '/') {
					ttype = st.nextToken();
					if (ttype == StreamTokenizer.TT_WORD
						&& st.sval.equals("applet")) {
						tags.addElement(currentTag);
						currentTag = null;
					}
				}
			}
		}
		is.close();
		return tags;
	}
}
/**
    05/21/2003	<param> tag must be in a single line or PageViewer doesn't work. Give a hint if
		param tag spans multiple lines.
	12/06/2003	changed StreamTkenizer setup to treat digits as normal 
				alpha chars.
	12/06/2003	return the AppletTags in a vector saves some code and time.
	12/06/2003	introduced getTokenizer() to reuse the code from the outside
	
*/