package iotproj.iotproject;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ScrollingTabContainerView;
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


public class MainActivity extends AppCompatActivity {

    private ImageButton talkBtn;
    private ListView threadList;
    private ImageView lamp1View, heating1View;
    private TextView lamp1OnOffStateText, heating1OnOffStateText;
    private TextView tempTextView;

    private static final String TAG = "iotproj.iotproject.MA";

    public static final String TIME_TAG = "iotproj.time";

    private String ioTGateWayIP = "81.230.190.13";
    public static final int IoT_GATEWAY_PORT = 4091;

    private String commandLanguage = "SV";

    private ArrayList<ConditionThread> conditionThreads;
    private ThreadListAdapter threadListAdapter;

    private GetRunner getRunner;

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
                        break;
                    case "English":
                        commandLanguage = "EN";
                        break;
                    case "ไทย":
                        commandLanguage = "TH";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initList() {
        threadListAdapter = new ThreadListAdapter(this, -1, conditionThreads);
        threadList.setAdapter(threadListAdapter);
    }

    private void getViews() {
        talkBtn = (ImageButton) findViewById(R.id.talk_button);
        threadList = (ListView) findViewById(R.id.thread_list);
        lamp1View = (ImageView) findViewById(R.id.lamp_1_image);
        heating1View = (ImageView) findViewById(R.id.lamp_2_image);
        lamp1OnOffStateText = (TextView) findViewById(R.id.lamp_1_state_text);
        heating1OnOffStateText = (TextView) findViewById(R.id.lamp_2_state_text);
        tempTextView = (TextView) findViewById(R.id.temperature_value_text);
    }


    private void initButtonFunctionality() {
        talkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                Log.d(TAG, "starting voice command intent at " + System.currentTimeMillis());
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getRunner.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRunner.onResume();
    }

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
            Toast.makeText(this, "Text-Till-Tal misslyckades", Toast.LENGTH_LONG).show();
        }
        Log.i(TIME_TAG, "onActivityResult: stop " + System.currentTimeMillis());
    }

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

    private void parseResult(String result) {
        if (result == null) {
            Toast.makeText(this, R.string.error_ip, Toast.LENGTH_LONG).show();
        } else if (result.equalsIgnoreCase("error")) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }
        new GetRunner(ioTGateWayIP, IoT_GATEWAY_PORT, this, true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setup:
                new SetupDialog(this).createAndShow();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setIoTGateWayIP(String ip) {
        ioTGateWayIP = ip;
        startGetRunner();
    }

    private void killGetRunner() {
        if (getRunner != null) {
            getRunner.kill();
        }
    }

    private void startGetRunner() {
        killGetRunner();
        getRunner = new GetRunner(ioTGateWayIP, IoT_GATEWAY_PORT, this);
    }

    public String getIoTGateWayIP() {
        return ioTGateWayIP;
    }
}
