package iot.wmb.welcomeapp.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Mqtt msg data reader
 * 
 * @author Jiri Petnik (jiri_petnik@cz.ibm.com)
 *
 */
public class JsonReadD {

	public static String read(String jsonLine, String prop){
		JsonElement jelement = new JsonParser().parse(jsonLine);
	    JsonObject  jobject = jelement.getAsJsonObject();
		JsonObject d = jobject.getAsJsonObject("d");
		if (d == null) {
			return null;
		}
		
		JsonElement el = d.get(prop);
		if (el == null){
			return null;
		}
		return el.getAsString();
	}
	
	public static void main(String[] args) {
		String jsonLine = "{\"ts\":\"2016-03-05T00:28:54.838+0100\",\"d\":{\"move\":\"left\"}}";
		System.out.println(JsonReadD.read(jsonLine, "move"));
	}
}
