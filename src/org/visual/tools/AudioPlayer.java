//package kaffe.applet;
package org.visual.tools;
import java.applet.AudioClip;
import java.net.URL;

/**
    This class is stolen from Kaffe's library to stisfy the needs of the appletviewer stolen from Kaffe.
*/
public class AudioPlayer
  implements AudioClip
{
	boolean stop;
	URL     url;

public AudioPlayer( URL url) {
	this.url = url;
}

public void loop() {
	stop = false;
	for (; !stop;) {
		play();
		try { Thread.sleep( 100); }
		catch ( InterruptedException _x ) {}
	}
}

public void play() {
	playFile( url.getFile() );
}

native static void playFile( String file );

public void stop() {
	stop = true;
}
}
