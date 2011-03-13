package org.bedroid.multipong.service;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;

@BusInterface(name = "org.bedroid.multipong.signal")
public interface PongSignalInterface {

	@BusSignal
	public void Move(int direction) throws BusException;

}
