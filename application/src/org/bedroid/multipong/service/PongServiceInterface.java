package org.bedroid.multipong.service;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;

@BusInterface(name = "org.bedroid.multipong.service")
public interface PongServiceInterface {
	/*
	 * @BusSignal public void Enter() throws BusException;
	 * 
	 * @BusSignal public void Leave() throws BusException;
	 * 
	 * @BusMethod public Config GetConfig() throws BusException;
	 */

	@BusMethod
	public String Hello() throws BusException;

}
