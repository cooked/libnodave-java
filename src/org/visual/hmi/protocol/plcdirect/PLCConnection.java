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

package org.visual.hmi.protocol.plcdirect;
/*
    Abstract connection to a PLC
*/

public interface PLCConnection {
    public int readByteBlock(int areaCode, int areaNumber, int first, int byteCount);
/**
    Extract a float variable from position pos of the received block:
*/    
    public float getFloat(int pos);
/**
    Extract an unsigned byte variable from position pos of the received block:
*/
    public int getUS8(int pos);
/**
    Extract a signed byte variable from position pos of the received block:
*/    
    public int getS8(int pos);
/**
    Extract an unsigned worde variable from position pos of the received block:
*/    
    public int getUS16(int pos);
/**
    Extract a signed word variable from position pos of the received block:
*/    
    public int getS16(int pos);
/**
    Extract an unsigned int (32 bit) variable from position pos of the received block:
*/    
    public long getUS32(int pos);
/**
    Extract a signed int variable (32 bit) from position pos of the received block:
*/    
    public long getS32(int pos);
    
}    