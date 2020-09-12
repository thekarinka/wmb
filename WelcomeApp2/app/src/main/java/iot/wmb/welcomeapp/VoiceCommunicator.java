package iot.wmb.welcomeapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.watson.developer_cloud.android.speech_common.v1.TokenProvider;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.ISpeechDelegate;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.dto.SpeechConfiguration;
import com.ibm.watson.developer_cloud.android.text_to_speech.v1.TextToSpeech;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Martin on 3/4/2016.
 */
public class VoiceCommunicator implements ISpeechDelegate {

    private static final long NANOSEC = 1000000000;

    private enum ConnectionState {
        IDLE, CONNECTING, CONNECTED
    }

    private PushMeActivity activity;
    ProgressBar bar;
    private ConnectionState state = ConnectionState.IDLE;
    private static final String TAG = "VoiceCommunicator";
    private DeviceClient client;
    private String lastMsg;
    private long startTime;

    private static String mRecognitionResults = "";

    public VoiceCommunicator(PushMeActivity activity, DeviceClient client){
        this.activity = activity;
        this.client = client;
        initTextToSpeech();
        initSTT();
        bar = (ProgressBar)activity.findViewById(R.id.progressBarRec);
    }


    public void startSTT(){
        Log.d(TAG, "startSTT: start: state=" + state);
        if (state == ConnectionState.IDLE) {
            state = ConnectionState.CONNECTING;
            Log.d(TAG, "startSTT: IDLE -> CONNECTING");
            SpeechToText.sharedInstance().setModel("en-US_BroadbandModel");
            // start recognition
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... none) {
                    SpeechToText.sharedInstance().recognize();
                    return null;
                }
            }.execute();
            startTime = System.nanoTime();
            bar.setProgress(1000);
        }
        else if (state == ConnectionState.CONNECTED) {
            state = ConnectionState.IDLE;
            Log.d(TAG, "startSTT: CONNECTED -> IDLE");
            SpeechToText.sharedInstance().stopRecognition();
            bar.setProgress(0);
        }
        Log.d(TAG, "startSTT: stop");
    }

    private void initSTT(){
        // DISCLAIMER: please enter your credentials or token factory in the lines below
        String username = "244e4a08-0fd3-4aaf-af81-d827361a90f9";
        String password = "aUIGUbym40va";

        String tokenFactoryURL = activity.getString(R.string.defaultTokenFactory);
        String serviceURL = "wss://stream.watsonplatform.net/speech-to-text/api";

        SpeechConfiguration sConfig = new SpeechConfiguration(SpeechConfiguration.AUDIO_FORMAT_OGGOPUS);
        //SpeechConfiguration sConfig = new SpeechConfiguration(SpeechConfiguration.AUDIO_FORMAT_DEFAULT);

        SpeechToText.sharedInstance().initWithContext(this.getHost(serviceURL), activity.getApplicationContext(), sConfig);

        // token factory is the preferred authentication method (service credentials are not distributed in the client app)
        if (tokenFactoryURL.equals(activity.getString(R.string.defaultTokenFactory)) == false) {
            SpeechToText.sharedInstance().setTokenProvider(new MyTokenProvider(tokenFactoryURL));
        }
        // Basic Authentication
        else if (username.equals(activity.getString(R.string.defaultUsername)) == false) {
            SpeechToText.sharedInstance().setCredentials(username, password);
        } else {
            // no authentication method available
            throw new IllegalArgumentException("No auth method");
        }

        SpeechToText.sharedInstance().setModel(activity.getString(R.string.modelDefault));
        SpeechToText.sharedInstance().setDelegate(this);
    }

    public void onOpen() {
        Log.d(TAG, "onOpen");
        state = ConnectionState.CONNECTED;
    }

    public void onError(String error) {
        Log.e(TAG, error);
        state = ConnectionState.IDLE;
    }

    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose, code: " + code + " reason: " + reason);
        Log.i(TAG, "connection closed");
        bar.setProgress(0);
        state = ConnectionState.IDLE;
    }

    public void onMessage(String message) {
        Log.d(TAG, "onMessage, message: " + message);

        lastMsg = message;

/*        try {
            JSONObject jObj = new JSONObject(message);
            // state message
            if(jObj.has("state")) {
                Log.d(TAG, "Status message: " + jObj.getString("state"));
            }
            // results message
            else if (jObj.has("results")) {
                //if has result
                Log.d(TAG, "Results message: ");
                JSONArray jArr = jObj.getJSONArray("results");
                for (int i=0; i < jArr.length(); i++) {
                    JSONObject obj = jArr.getJSONObject(i);
                    JSONArray jArr1 = obj.getJSONArray("alternatives");
                    String str = jArr1.getJSONObject(0).getString("transcript");
                    // remove whitespaces if the language requires it
                    Log.d("recieved", mRecognitionResults);
                    String strFormatted = Character.toUpperCase(str.charAt(0)) + str.substring(1);
                    if (obj.getString("final").equals("true")) {

                        mRecognitionResults += strFormatted.substring(0,strFormatted.length()-1) + ".";

                        // výpis přijatého textu
                        Log.d("recieved", mRecognitionResults);
                    } else {
                        Log.d("recieved", mRecognitionResults);
                    }
                    break;
                }
            } else {
                Log.d("recieved", "bad bad bad");
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
        }*/
    }

    public void onAmplitude(double amplitude, double volume) {
        Log.e(TAG, "amplitude=" + amplitude + ", volume=" + volume);

        if(System.nanoTime() - startTime > 10 * NANOSEC /*&& volume < 70*/
                || System.nanoTime() - startTime > 15 * NANOSEC){
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... none) {
                    Log.i(TAG, "onAmplitude: I send msg to server: " + lastMsg);
                    client.publishEvent("dialog", lastMsg);
                    activity.stopSpeechToText();
                    Log.d(TAG, "onAmplitude: end");
                    return null;
                }
            }.execute();
        }
    }



    private void initTextToSpeech(){
        // DISCLAIMER: please enter your credentials or token factory in the lines below

        String username = "6aa53958-f9dd-46c0-aa97-ffad523802fb";
        String password = "vB1nOytd4npN";
        String tokenFactoryURL = activity.getString(R.string.defaultTokenFactory);
        String serviceURL = "https://stream.watsonplatform.net/text-to-speech/api";

        TextToSpeech.sharedInstance().initWithContext(this.getHost(serviceURL));

        // token factory is the preferred authentication method (service credentials are not distributed in the client app)
        if (tokenFactoryURL.equals(activity.getString(R.string.defaultTokenFactory)) == false) {
            TextToSpeech.sharedInstance().setTokenProvider(new MyTokenProvider(tokenFactoryURL));
        }
        // Basic Authentication
        else if (username.equals(activity.getString(R.string.defaultUsername)) == false) {
            TextToSpeech.sharedInstance().setCredentials(username, password);
        } else {
            // no authentication method available
            throw new IllegalStateException("no authentication method available");
        }

        TextToSpeech.sharedInstance().setVoice(activity.getString(R.string.voiceDefault));
    }

    public URI getHost(String url){
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void textToSpeech(String msg){
        Log.d(TAG, "textToSpeech - start: msg=" + msg);

        TextToSpeech.sharedInstance().setVoice("en-US_MichaelVoice");
        TextToSpeech.sharedInstance().synthesize(msg);

        Log.d(TAG, "textToSpeech - end");
    }


    static class MyTokenProvider implements TokenProvider {

        String m_strTokenFactoryURL = null;

        public MyTokenProvider(String strTokenFactoryURL) {
            m_strTokenFactoryURL = strTokenFactoryURL;
        }

        public String getToken() {

            Log.d(TAG, "attempting to get a token from: " + m_strTokenFactoryURL);
            try {
                // DISCLAIMER: the application developer should implement an authentication mechanism from the mobile app to the
                // server side app so the token factory in the server only provides tokens to authenticated clients
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(m_strTokenFactoryURL);
                HttpResponse executed = httpClient.execute(httpGet);
                InputStream is = executed.getEntity().getContent();
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer, "UTF-8");
                String strToken = writer.toString();
                Log.d(TAG, strToken);
                return strToken;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
