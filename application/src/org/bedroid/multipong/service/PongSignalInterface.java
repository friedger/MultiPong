package org.bedroid.multipong.service;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;
import org.bedroid.multipong.BallMove;

@BusInterface(name = "org.bedroid.multipong.signal")
public interface PongSignalInterface {

	@BusSignal
	public void Move(BallMove ballMove) throws BusException;

}
