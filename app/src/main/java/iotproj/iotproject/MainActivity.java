package iotproj.iotproject;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import iotproj.iotproject.model.ConditionThread;
import iotproj.iotproject.model.Device;
import iotproj.iotproject.model.GetRunner;
import iotproj.iotproject.model.IncorrectRetrievableException;
import iotproj.iotproject.model.Retrievable;
import iotproj.iotproject.model.Sensor;
import iotproj.iotproject.model.State;


public class MainActivity extends AppCompatActivity {

    private ImageButton talkBtn;
    private TextView output;
    private ListView threadList;
    private ImageView lamp1View, lamp2View;
    private TextView lamp1OnOffStateText, lamp2OnOffStateText;
    private TextView tempTextView;

    private static final String TAG = "iotproj.iotproject.MA";

    private String ioTGateWayIP = "81.230.190.13";
    public static final int IoT_GATEWAY_PORT = 4091;

    private ArrayList<ConditionThread> conditionThreads;
    private ThreadListAdapter threadListAdapter;

    private GetRunner getRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conditionThreads = new ArrayList<>();
        try {
            conditionThreads.add(new ConditionThread("T%1%[hello world]%true"));
        } catch (IncorrectRetrievableException e) {
            e.printStackTrace();
        }
        getViews();
        initButtonFunctionality();
        initList();
        getRunner = new GetRunner(ioTGateWayIP, IoT_GATEWAY_PORT, this);
    }

    private void initList() {
        threadListAdapter = new ThreadListAdapter(this, -1, conditionThreads);
        threadList.setAdapter(threadListAdapter);
    }

    private void getViews() {
        talkBtn = (ImageButton) findViewById(R.id.talk_button);
        output = (TextView) findViewById(R.id.output);
        threadList = (ListView) findViewById(R.id.thread_list);
        lamp1View = (ImageView) findViewById(R.id.lamp_1_image);
        lamp2View = (ImageView) findViewById(R.id.lamp_2_image);
        lamp1OnOffStateText = (TextView) findViewById(R.id.lamp_1_state_text);
        lamp2OnOffStateText = (TextView) findViewById(R.id.lamp_2_state_text);
        tempTextView = (TextView) findViewById(R.id.temperature_value_text);
    }


    private void initButtonFunctionality() {
        talkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, 1);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String result = "" + data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            result = result.toLowerCase().replace(" ", "_");
            Log.d(TAG, "got result = " + result);
            new AsyncWebServerCall(new AsyncCallback() {
                @Override
                public void receiveAsyncResult(String result) {
                    parseResult(result);
                }
            }).execute("http://" + ioTGateWayIP + ":" + IoT_GATEWAY_PORT + "/command/" + result);
        } else {
            Toast.makeText(this, "Text-Till-Tal misslyckades", Toast.LENGTH_LONG).show();
        }
    }

    public void updateList(List<Retrievable> newList) {
        for (Retrievable retrievable : newList) {
            if (retrievable instanceof ConditionThread) {
                int index = conditionThreads.indexOf(retrievable);
                if (index == -1) {
                    conditionThreads.add((ConditionThread) retrievable);
                }
            } else if (retrievable instanceof Device) {
                Device device = (Device) retrievable;
                if (device.getId() == 1) {
                    lamp1OnOffStateText.setText(device.getState().equals(State.ON) ? R.string.on : R.string.off);
                } else if (device.getId() == 2) {
                    lamp2OnOffStateText.setText(device.getState().equals(State.ON) ? R.string.on : R.string.off);
                }
            } else if (retrievable instanceof Sensor) {
                Log.d(TAG, "retreviable is sensor");
                Sensor sensor = (Sensor) retrievable;
                Log.d(TAG, "sensor id = " + sensor.getId());
                if (sensor.getId() == 135) {
                    String temp = sensor.getValueOfHead("TEMP");
                    Log.d(TAG, "temperature is = " + temp);
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

    }

    private void parseResult(String result) {
        System.out.println("got result from server: " + result);
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

    }

    public String getIoTGateWayIP() {
        return ioTGateWayIP;
    }
}
