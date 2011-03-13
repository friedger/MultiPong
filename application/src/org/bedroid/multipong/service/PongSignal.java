package org.bedroid.multipong.service;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.annotation.BusSignalHandler;
import org.bedroid.multipong.BallMove;
import org.bedroid.multipong.PongActivity;

import android.os.Handler;
import android.os.Message;

/**
 * Signal interface (for emitter) and signal handler in one class
 * 
 * @author friedger
 * 
 */
public class PongSignal implements PongSignalInterface, BusObject {

	private static final String TAG = "PongSignal";

	private Handler handler;

	/**
	 * 
	 */
	public PongSignal(Handler handler) {
		super();
		this.handler = handler;
	}

	@Override
	public void Move(BallMove ballMove) throws BusException {
		// no code here
	}

	@BusSignalHandler(iface = "org.bedroid.multipong.signal", signal = "Move")
	public void MoveReceived(BallMove ballMove) {
		Message msg = handler.obtainMessage(PongActivity.MESSAGE_MOVE_RECEIVED);
		handler.sendMessage(msg);
	}

}
