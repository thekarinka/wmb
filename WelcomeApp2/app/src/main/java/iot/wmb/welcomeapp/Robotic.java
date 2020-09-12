package iot.wmb.welcomeapp;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;
import com.wowwee.robome.RoboMe;
import com.wowwee.robome.RoboMeCommands;

/**
 * Created by Martin on 3/5/2016.
 */
public class Robotic implements RoboMe.RoboMeListener {

    private RoboMe robome;
    private Context context;
    private DeviceClient deviceClient;

    public Robotic(Context context, DeviceClient client) {
        robome = new RoboMe(context, this);
        this.context = context;
        this.deviceClient = client;
    }

    public RoboMe getRob(){
        return robome;
    }

    @Override
    public void commandReceived(RoboMeCommands.IncomingRobotCommand incomingRobotCommand) {
        final String TAG = "Proximity";
        Log.d(TAG, "commandReceived: " + incomingRobotCommand);
        if(incomingRobotCommand.isSensorStatus()){
            if(incomingRobotCommand.readSensorStatus().edge)
                Log.d(TAG, "edge");
                //deviceClient.publishEvent(null, null);
            else if(incomingRobotCommand.readSensorStatus().chest_20cm) {
                Log.d(TAG, "20cm");

                JsonObject event = new JsonObject();
                event.addProperty("event", "init_start");

                int qos = 2;
                deviceClient.publishEvent("init_start", event, qos);
            }
            else if (incomingRobotCommand.readSensorStatus().chest_50cm)
                Log.d(TAG, "50cm");
                //deviceClient.publishEvent(null, null);
            else if(incomingRobotCommand.readSensorStatus().chest_100cm)
                Log.d(TAG, "100cm");
                //deviceClient.publishEvent(null, null);
        }

        //TODO implement
    }

    @Override
    public void roboMeConnected() {
//TODO implement
    }

    @Override
    public void roboMeDisconnected() {
//TODO implement
    }

    @Override
    public void headsetPluggedIn() {
//TODO implement
    }

    @Override
    public void headsetUnplugged() {
//TODO implement
    }

    @Override
    public void volumeChanged(float v) {
//TODO implement
    }

}
