/*
 Definition of an interface that lets the hmiViewer talk to a server.
 There can be different plug ins which implement this interface in order 
 to communicate to different servers.
 
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

package org.visual.hmi.protocol;
import org.visual.hmi.hmiList;

import java.util.Vector;

/**
 Definition of an interface that lets the hmiViewer talk to a server.
 There can be different plug ins which implement this interface in order 
 to communicate to different servers.
*/ 
public interface hmiCommunicationInterface {
/**   
    Set variable indentified by k to value v.
*/    
    public void putVal(String k,double v);	
/**
    This is special for pushbuttons: The variable indentified by k is first
    set to 1, then to 0 again.
    With visual, the server puts a 1 into the spreadsheet
    and the rest is handled by the plc driver module.
*/
    public void tPutVal(String k);

/**    
    Basic initialization before owner got variable names from applet parameters.
*/    
    public void init(hmiList owner);

/**
    Establish a connection. Also used to reestablish a broken one.
*/    
    public void open();
/**
	Close a connection. Used before trying to reestablish a broken one.
*/    
	public void close();
/**    
    Second initialization step forming requests for variables the widgets need.
*/    
    public void initVariableTable();
/**    
    Fetch values. Trigggers communication and may or may not wait for results, depending on
    type of communication.
*/    
    public void getValTab();    

/**
    Close connection on applet destroy().
*/    
    public void destroy();
/**   
    Close connection on applet stop()
*/    
    public void stop();

/**
    Return 0 if connection ok.
    If the result is >=10 the applet will use open() to reconnect.
*/    
    public int getHostError();

/**
    Get pairs of time and value for trend diagrams.
    Return the last time stamp as a String. The return value is used for parameter last
    in the next call. The first call should use "0". The behaviour is to get all trend data
    kept by the server in the first call and only newer data in subsequent calls. If the
    number of entries exceeds maxInt, oldest entries are removed.
*/    
    public String getTrendData(String k, String last, Vector timeValuePairs, int maxLen);

/**
    Get the alarm messages from a variable range given in alarmRange. Not yet implemented in any CI.
*/    
    public void getAlarmMessages(String request, Vector lines, int maxLines);
    
/**
	Get a single line answer for a single line request.
*/
	public String getAnswer(String what);
	
    
}

/* Changes
 * 09/27/2002	first version
 */
