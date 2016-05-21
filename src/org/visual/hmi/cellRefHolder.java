/*
 An interface that promises cellRef some capabilities of it's owner, which
 is usually an hmiElement.
 
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
/**
    An interface that promises cellRef some capabilities of it's owner, which
    is usually an hmiElement.
*/    

package org.visual.hmi;

public interface cellRefHolder{
/** 
    This is used to propagate communication errors to owner
*/
    void setComError(int flag);	
/** 
    Get a referrence to owner's (widget) owner(hmiViewer).
*/
    hmiList getList();
/** 
	Get a the address modifier
*/
    AddressModifier getAddressModifier();		
} 
