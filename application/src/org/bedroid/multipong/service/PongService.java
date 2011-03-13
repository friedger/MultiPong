package org.bedroid.multipong.service;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.annotation.BusSignalHandler;

import android.os.Build;
import android.util.Log;

/**
 * @author pjv
 * 
 *         Local implementation of the PongServiceInterface.
 * 
 */
public class PongService implements PongServiceInterface, BusObject {
	private static final String TAG = "PongService";

	public String Hello() throws BusException {
		Log.i(TAG, "LOCAAAAAAAL " + Build.VERSION.SDK_INT);
		return "HELLOOOOOOO " + Build.VERSION.SDK_INT;
	}

	public void Move(int direction) throws BusException {

	}

	@BusSignalHandler(iface = "org.bedroid.multipong.signal", signal = "Move")
	public void MoveReceived(int direction) {
		Log.i(TAG, "Signal received " + direction);

	}
}
