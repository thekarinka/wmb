package iot.wmb.welcomeapp.misc;

import java.util.Properties;

/**
 * WMB specific properties for connection to Watson IoT Platform
 *  
 * @author Jiri Petnik (jiri_petnik@cz.ibm.com)
 *
 */
public class WMBDeviceProperties extends Properties {

	public WMBDeviceProperties() {
		super();
		this.setProperty("org", "j0kpev");
		this.setProperty("type", "wmb-app");
		this.setProperty("id", "wmb-device");
		this.setProperty("auth-method", "token");
		this.setProperty("auth-token", "wmb-device-token");
	}
}
