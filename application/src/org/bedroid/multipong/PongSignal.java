package org.bedroid.multipong;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
import org.bedroid.multipong.service.PongSignalInterface;

public class PongSignal implements PongSignalInterface, BusObject {

	@Override
	public void Move(int direction) throws BusException {
		// no code here
	}

}
