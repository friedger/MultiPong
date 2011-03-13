package org.bedroid.multipong.service;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.annotation.BusSignalHandler;

import android.util.Log;

/**
 * Signal interface (for emitter) and signal handler in one class
 * 
 * @author friedger
 * 
 */
public class PongSignal implements PongSignalInterface, BusObject {

	private static final String TAG = "PongSignal";

	@Override
	public void Move(int direction) throws BusException {
		// no code here
	}

	@BusSignalHandler(iface = "org.bedroid.multipong.signal", signal = "Move")
	public void MoveReceived(int direction) {
		Log.i(TAG, "Signal received " + direction);

	}

}
