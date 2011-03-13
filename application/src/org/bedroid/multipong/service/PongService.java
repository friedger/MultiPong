package org.bedroid.multipong.service;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;

import android.util.Log;

/**
 * @author pjv
 * 
 *         Local implementation of the PongServiceInterface.
 * 
 */
public class PongService implements PongServiceInterface, BusObject {
	private static final String TAG = "PongService";

	public void Hello() throws BusException {
		Log.i(TAG, "HELLOOOOOOOOOO");
	}
}
