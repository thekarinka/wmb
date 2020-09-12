package iot.wmb.welcomeapp.callback;

import android.util.Log;

import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.wowwee.robome.RoboMe;
import com.wowwee.robome.RoboMeCommands;

import iot.wmb.welcomeapp.VoiceCommunicator;
import iot.wmb.welcomeapp.misc.JsonReadD;

public class WMBCallback implements CommandCallback{

	private static final String TAG = "WMBCallback";

	private RoboMe robome;
	private VoiceCommunicator voiceCom;

	public WMBCallback(RoboMe robome, VoiceCommunicator voiceCom){
		this.robome = robome;
		this.voiceCom = voiceCom;
	}

	@Override
	public void processCommand(Command command) {
			
		String cmd = command.getCommand();
		String payload = command.getPayload();
		
		Log.d(TAG, "COMMAND RECEIVED = '" + cmd + "'\twith Payload = '" + payload + "'");
		
		switch(cmd) {
			case "movement":
				Log.d(TAG, "MOVEMENT: " + JsonReadD.read(payload, "move")
								+ "STEP: " + JsonReadD.read(payload, "step")
				);
				processMovement(JsonReadD.read(payload, "move"), JsonReadD.read(payload, "step"));
				break;

			case "dialog":
				Log.d(TAG, "DIALOG: " + JsonReadD.read(payload, "text"));
				voiceCom.textToSpeech(JsonReadD.read(payload, "text"));
				break;
		}
	}


	private void processMovement(String move, String step){
		int stepCount = 0;
		try {
			stepCount = Integer.parseInt(step);
		}catch(NumberFormatException ex){
			Log.w(TAG, "Illegal step count: ", ex);
			return;
		}
		RoboMeCommands.RobotCommand moveCommand = RoboMeCommands.RobotCommand.kRobot_Stop;
		switch(move){
			case "F":
				moveCommand = RoboMeCommands.RobotCommand.kRobot_MoveForwardSpeed1;
				break;
			case "B":
				moveCommand = RoboMeCommands.RobotCommand.kRobot_MoveBackwardSpeed1;
				break;
			case "L":
				moveCommand = RoboMeCommands.RobotCommand.kRobot_TurnLeftSpeed1;
				break;
			case "R":
				moveCommand = RoboMeCommands.RobotCommand.kRobot_TurnRightSpeed1;
				break;
			case "HD":
				moveCommand = RoboMeCommands.RobotCommand.kRobot_HeadTiltDown2;
				break;
			case "HU":
				moveCommand = RoboMeCommands.RobotCommand.kRobot_HeadTiltUp2;
				break;
			case "HHU":
				moveCommand = RoboMeCommands.RobotCommand.kRobot_HeadTiltAllUp;
				break;
			case "HHD":
				moveCommand = RoboMeCommands.RobotCommand.kRobot_HeadTiltAllDown;
				break;
		}

		for(int i = 0; i < stepCount; i++) {
			robome.sendCommand(moveCommand);
		}
	}
}
