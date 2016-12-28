package iotproj.iotproject;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import iotproj.iotproject.AsyncCallback;
import iotproj.iotproject.AsyncWebServerCall;
import iotproj.iotproject.MainActivity;
import iotproj.iotproject.model.ConditionThread;
import iotproj.iotproject.model.Device;
import iotproj.iotproject.model.Retrievable;
import iotproj.iotproject.model.Sensor;

public class GetRunner extends Thread {

    public String url;
    private boolean alive;
    private boolean paused = false;
    private boolean runOnce;

    private String lastGet;
    private MainActivity mainActivity;

    public GetRunner(String ip, int port, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        url = "http://" + ip + ":" + port + "/get";
        runOnce = false;
        start();
    }

    public GetRunner(String ip, int port, MainActivity mainActivity, boolean runOnce) {
        this.runOnce = true;
        this.mainActivity = mainActivity;
        url = "http://" + ip + ":" + port + "/get";
        start();
    }

    @Override
    public void run() {
        alive = true;
        while (alive) {
            if (!paused) {
                try {
                    Log.i(MainActivity.TIME_TAG, "GetRunner Thread start " + System.currentTimeMillis());
                    new AsyncWebServerCall(new AsyncCallback() {
                        @Override
                        public void receiveAsyncResult(String result) {
                            Log.i(MainActivity.TIME_TAG, "GetRunner Thread stop (get result) " + System.currentTimeMillis());
                            if (result != null && !result.equals(lastGet)) {
                                lastGet = result;
                                try {
                                    parseResult(result);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).execute(url);
                    if (runOnce) {
                        kill();
                    } else {
                        sleep(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                    kill();
                }
            }
        }
    }

    public void onPause() {
        paused = true;
    }

    public void onResume() {
        paused = false;
    }

    public void parseResult(String result) throws Exception {
        Log.i(MainActivity.TIME_TAG, "parseResult: start " + System.currentTimeMillis());
        String[] lines = result.split("\n");
        List<Retrievable> iotResults = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("D")) {
                iotResults.add(new Device(line));
            } else if (line.startsWith("T")) {
                iotResults.add(new ConditionThread(line));
            } else if (line.startsWith("S")) {
                iotResults.add(new Sensor(line));
            } else if (line.startsWith("P")) {
                Sensor.setHeaders(line);
            }
        }
        Log.i(MainActivity.TIME_TAG, "parseResult: stop " + System.currentTimeMillis());
        mainActivity.updateList(iotResults);
    }

    public void kill() {
        alive = false;
    }
}
