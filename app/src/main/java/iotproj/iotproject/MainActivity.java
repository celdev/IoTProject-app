package iotproj.iotproject;

import android.content.Intent;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import iotproj.iotproject.model.ConditionThread;
import iotproj.iotproject.model.Device;
import iotproj.iotproject.model.Retrievable;
import iotproj.iotproject.model.Sensor;
import iotproj.iotproject.model.State;


/** This class contains the MainActivity for the IoT Android Application
 *  It provides the functionality to send Speech-To-Text messages to the
 *  IoT Gateway and display the state and values of the devices, sensors and
 *  ConditionThreads of the IoT gateway
 * */
public class MainActivity extends AppCompatActivity {

    //The views that will be manipulated
    private ImageButton talkBtn;
    private ListView threadList;
    private ImageView lamp1View, heating1View;
    private TextView lamp1OnOffStateText, heating1OnOffStateText;
    private TextView tempTextView, connectedText;

    //Tags used when logging
    private static final String TAG = "iotproj.iotproject.MA";
    public static final String TIME_TAG = "iotproj.time";

    //the default ip and port of the IoT GateWay
    private String ioTGateWayIP = "81.230.190.13";
    public static final int IoT_GATEWAY_PORT = 4091;

    //the speech language used (default swedish)
    private String commandLanguage = "SV";
    private String voiceRecognitionLanguage = "sv-SE";

    //ArrayList and ArrayAdapter for the Thread ListView
    private ArrayList<ConditionThread> conditionThreads;
    private ThreadListAdapter threadListAdapter;

    //The GetRunner which will continuously fetch the information from the IoT gateway
    private GetRunner getRunner;

    /** This method is called when the activity is created
     *
     *  Initializes the functionality of the activity
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conditionThreads = new ArrayList<>();
        getViews();
        initButtonFunctionality();
        initList();
        initSpinner();
        startGetRunner();
    }

    /** Fetches a reference to the Spinner-view element
     *
     *  Creates the ArrayAdapter for the spinner with the items (Swedish, English and Thai)
     *  and creates an OnItemSelectedListener which will fire when a selection
     *  is made in the spinner.
     *  When a selection is made the commandLanguage and voiceRecognitionLanguage variables
     *  are updated to the chosen language
     * */
    private void initSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Svenska", "English", "ไทย"});
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String select = (String) parent.getItemAtPosition(position);
                switch (select) {
                    case "Svenska":
                        commandLanguage = "SV";
                        voiceRecognitionLanguage = "sv-SE";
                        break;
                    case "English":
                        commandLanguage = "EN";
                        voiceRecognitionLanguage = "en-US";
                        break;
                    case "ไทย":
                        commandLanguage = "TH";
                        voiceRecognitionLanguage = "th-TH";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /** Initializes the ArrayAdapter that the Thread-ListView will use
     *  and sets the ListViews adapter to the newly created ArrayAdapter
     * */
    private void initList() {
        threadListAdapter = new ThreadListAdapter(this, -1, conditionThreads);
        threadList.setAdapter(threadListAdapter);
    }

    /** Fetches references to the views that will need to be
     *  manipulated
     * */
    private void getViews() {
        talkBtn = (ImageButton) findViewById(R.id.talk_button);
        threadList = (ListView) findViewById(R.id.thread_list);
        lamp1View = (ImageView) findViewById(R.id.lamp_1_image);
        heating1View = (ImageView) findViewById(R.id.lamp_2_image);
        lamp1OnOffStateText = (TextView) findViewById(R.id.lamp_1_state_text);
        heating1OnOffStateText = (TextView) findViewById(R.id.lamp_2_state_text);
        tempTextView = (TextView) findViewById(R.id.temperature_value_text);
        connectedText = (TextView) findViewById(R.id.connectedText);
    }

    /** Sets the text of the ocnnection state text to
     *  connected and the text color to green
     * */
    public void setConnected() {
        connectedText.setText(R.string.connected);
        connectedText.setTextColor(Color.GREEN);
    }

    /** Sets the text of the connection state text to
     *  loading and the text color to black
     * */
    public void setLoading() {
        connectedText.setText(R.string.loading);
        connectedText.setTextColor(Color.BLACK);
    }

    /** Sets the text of the connection state text to
     *  disconnected and the text color to red
     * */
    public void setDisconnected() {
        connectedText.setText(R.string.disconected);
        connectedText.setTextColor(Color.RED);
    }

    /** Initializes the functionality of the Talk-button
     *
     *  When the talk button is pressed a RecognizerIntent is created
     *  with the Model Free Form and the language
     *  specified in the voiceRecognitionLanguage variable
     *  The intent is then used as a parameter in the startActivityForResult which
     *  will cause the started activity to return a result to this Activity
     * */
    private void initButtonFunctionality() {
        talkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceRecognitionLanguage);
                Log.d(TAG, "starting voice command intent at " + System.currentTimeMillis());
                startActivityForResult(intent, 1);
            }
        });
    }

    /** This method is called when the activity goes into a hidden state
     *  kills the GetRunner
     * */
    @Override
    protected void onPause() {
        super.onPause();
        killGetRunner();
    }

    /** This method is called when the activty is resumed from a hidden state
     *  and when the activity is run for the first time (it's called after the onCreate method)
     *
     *  sets the graphical state of the application to loading
     *  and starts the GetRunner
     * */
    @Override
    protected void onResume() {
        super.onResume();
        setLoading();
        startGetRunner();
    }

    /** This method is called when the Speech-To-Text intent returns a result
     *
     *  If the result is OK it tries to extract the message from the Speech-To-Text result
     *  replaces space with underscore and sends the message to the IoTGateWay
     *
     *  else an message that Speech To Text failed
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TIME_TAG, "onActivityResult: start " + System.currentTimeMillis());
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String result = "" + data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            result = result.toLowerCase().replace(" ", "_");
            Log.d(TAG, "got result = " + result);
            new AsyncWebServerCall(new AsyncCallback() {
                @Override
                public void receiveAsyncResult(String result) {
                    parseResult(result);
                }
            }).execute("http://" + ioTGateWayIP + ":" + IoT_GATEWAY_PORT + "/" + commandLanguage + "/command/" + result);
        } else {
            Toast.makeText(this, R.string.speechtotextfailed, Toast.LENGTH_LONG).show();
        }
        Log.i(TIME_TAG, "onActivityResult: stop " + System.currentTimeMillis());
    }

    /** Takes a list of Retrievable objects as a parameter
     *  A Receivable can either be a Sensor, Device or a ConditionThread
     *
     *  Depending on the class of the object different information is extracted and
     *  used to manipulate the graphical interface
     * */
    public void updateList(List<Retrievable> newList) {
        Log.i(TIME_TAG, "updateList start: " + System.currentTimeMillis());
        for (Retrievable retrievable : newList) {
            if (retrievable instanceof ConditionThread) {
                if (!conditionThreads.contains(retrievable)) {
                    conditionThreads.add((ConditionThread) retrievable);
                }
            } else if (retrievable instanceof Device) {
                Device device = (Device) retrievable;
                State state = device.getState();
                boolean stateIsOn = state.equals(State.ON);
                if (device.getId() == 1) {
                    lamp1OnOffStateText.setText(stateIsOn ? R.string.on : R.string.off);
                    lamp1View.setImageDrawable(getDrawable(stateIsOn ? R.drawable.ic_lightbulb_on : R.drawable.ic_lightbulb));
                } else if (device.getId() == 2) {
                    heating1OnOffStateText.setText(stateIsOn ? R.string.on : R.string.off);
                    heating1View.setImageDrawable(getDrawable(stateIsOn ? R.drawable.ic_heating_on : R.drawable.ic_heating_off));
                }
            } else if (retrievable instanceof Sensor) {
                Sensor sensor = (Sensor) retrievable;
                if (sensor.getId() == 135) {
                    String temp = sensor.getValueOfHead("TEMP");
                    tempTextView.setText(temp);
                }
            }
        }
        Iterator<ConditionThread> iterator = conditionThreads.iterator();
        while (iterator.hasNext()) {
            ConditionThread conditionThread = iterator.next();
            if (!newList.contains(conditionThread)) {
                iterator.remove();
            }
        }
        threadListAdapter.notifyDataSetChanged();
        Log.i(TIME_TAG, "updateList stop: " + System.currentTimeMillis());
    }

    /** Starts parsing a result retreived from the IoT Gateway
     *
     *  If the result is null (i.e. in case of a connection error) or
     *  if the result retrieved is "error" an error-Toast will be shown
     *
     *  otherwise a new GetRunner will be created that will run once
     *  and refresh the states and values of the devices and sensors
     * */
    private void parseResult(String result) {
        if (result == null) {
            Toast.makeText(this, R.string.error_ip, Toast.LENGTH_LONG).show();
        } else if (result.equalsIgnoreCase("error")) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }
        new GetRunner(ioTGateWayIP, IoT_GATEWAY_PORT, this, true);
    }


    /** This method is called when the menu is created
     *
     *  Specifies which file should be used to create the menu
     *  In this case the file /res/menu/menu.xml will be used
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** This method is called when the user presses an
     *  item in the menu.
     *
     *  Opens the SetupDialog if the menu item with
     *  the id menu_setup is pressed
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setup:
                new SetupDialog(this).createAndShow();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Sets the IoTGateWayIP to the ip passed as a parameter
     *  and restarts the GetRunner
     * */
    public void setIoTGateWayIPandRestartGetRunner(String ip) {
        ioTGateWayIP = ip;
        startGetRunner();
    }

    /** Kills the GetRunner if it isn't null
     *  Sets the getRunner field to null
     * */
    private void killGetRunner() {
        if (getRunner != null) {
            getRunner.kill();
            getRunner = null;
        }
    }

    /** Starts a new GetRunner Thread using the current IP
     *  Kills the getRunner-Thread if it isn't dead (or null)
     * */
    private void startGetRunner() {
        killGetRunner();
        getRunner = new GetRunner(ioTGateWayIP, IoT_GATEWAY_PORT, this);
    }

    /** Returns the ioTGateWayIP
     * */
    public String getIoTGateWayIP() {
        return ioTGateWayIP;
    }
}
