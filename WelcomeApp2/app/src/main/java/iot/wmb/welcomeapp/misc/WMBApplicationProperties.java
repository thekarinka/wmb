package iot.wmb.welcomeapp.misc;

import java.util.Properties;

/**
 * WMB specific properties for connection to Watson IoT Platform
 *  
 * @author Jiri Petnik (jiri_petnik@cz.ibm.com)
 *
 */
public class WMBApplicationProperties extends Properties {

	public WMBApplicationProperties() {
		super();
		this.setProperty("org", "j0kpev");
		this.setProperty("id", "push app test");
		this.setProperty("Authentication-Method","apikey");
		this.setProperty("API-Key", "a-j0kpev-myjzu4tmtx");
		this.setProperty("Authentication-Token", "1Jkf+)1zkG_7EnC0U7");
	}
}
