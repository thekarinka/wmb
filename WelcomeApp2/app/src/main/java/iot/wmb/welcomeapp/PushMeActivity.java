package iot.wmb.welcomeapp;

import android.os.AsyncTask;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.SpeechToText;
import com.wowwee.robome.RoboMe;
import com.wowwee.robome.RoboMeCommands;

import java.util.Properties;

import iot.wmb.welcomeapp.callback.WMBCallback;
import iot.wmb.welcomeapp.misc.WMBDeviceProperties;


public class PushMeActivity extends AppCompatActivity {

    private static final String TAG = "PushMeActivity";
    private RoboMe robome;
    private DeviceClient deviceClient;
    private VoiceCommunicator voiceCom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_me);

        try {
            Properties options = new WMBDeviceProperties();
            deviceClient = new DeviceClient(options);
            deviceClient.connect();
            Log.i(TAG, "Device connected!");
            robome = new Robotic(this, deviceClient).getRob();
            voiceCom = new VoiceCommunicator(this, deviceClient);
            deviceClient.setCommandCallback(new WMBCallback(robome, voiceCom));
        }catch(Exception ex){
            Log.e(TAG, "Exception: ", ex);
            Toast.makeText(this, "Ilegall state of app!", Toast.LENGTH_LONG).show();
        }

       // voiceCom.startRecognizing();
    }


    /** Start listening to events from the gun when the app starts or resumes from background */
    @Override
    public void onResume(){
        super.onResume();
        // set media volume to 12
        robome.setVolume(15);
        robome.startListening();
    }

    public void onEnd(View view){
        robome.stopListening();
        deviceClient.disconnect();
        //this.onDestroy();
        this.finish();
        onBackPressed();
    }


    public void onPushMeClick(View view){
        Log.d(TAG, "onPushMeClick - start: view=" + view.toString());

        JsonObject event = new JsonObject();
        event.addProperty("event", "init_start");

        int qos = 2;
        deviceClient.publishEvent("init_start", event, qos);

        Log.d(TAG, "onPushMeClick - end");
    }

    public void onTestVoice(View view){
        VoiceCommunicator voiceCom = new VoiceCommunicator(this, deviceClient);
    }

    @Deprecated
    public void onRoboAction(View view){
        Log.d(TAG, "onRoboAction - view.getId=" + view.getId());
        if (view.getId() == R.id.buttonForward) {
            robome.sendCommand(RoboMeCommands.RobotCommand.kRobot_MoveForwardSpeed5);
        }else if (view.getId() == R.id.buttonBack) {
            robome.sendCommand(RoboMeCommands.RobotCommand.kRobot_MoveBackwardSpeed5);
        } else if (view.getId() == R.id.buttonLeft) {
            robome.sendCommand(RoboMeCommands.RobotCommand.kRobot_TurnLeft90Degrees);
        } else if (view.getId() == R.id.buttonRight) {
            robome.sendCommand(RoboMeCommands.RobotCommand.kRobot_TurnRight90Degrees);
        }
    }
}