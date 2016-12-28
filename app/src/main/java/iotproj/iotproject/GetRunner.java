package iotproj.iotproject;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import iotproj.iotproject.model.ConditionThread;
import iotproj.iotproject.model.Device;
import iotproj.iotproject.model.Retrievable;
import iotproj.iotproject.model.Sensor;

/** This class is responsible for querying the web server using the /get path
 *
 *  This will cause the web server to respond with a message containing the
 *  state and values of the IoT devices and sensors and Condition Threads running on the server
 *
 *  There's two different kinds of GetRunners, one that is running continuously and one that is
 *  run once
 *
 *  In the run method a AsyncWebServerCall object will be created and it will execute the /get command
 * */
class GetRunner extends Thread {

    private String url;
    private boolean alive;
    private boolean runOnce;

    private String lastGet;
    private MainActivity mainActivity;

    /** Constructor for the continuous GetRunner
     *  will run until the Activity is hidden/destroyed or until a connection
     *  error occurs in which the thread will be killed
     * */
    GetRunner(String ip, int port, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        url = "http://" + ip + ":" + port + "/get";
        runOnce = false;
        start();
    }

    /** Constructor for the GetRunner which will run once
     * */
    GetRunner(String ip, int port, MainActivity mainActivity, boolean runOnce) {
        this.runOnce = true;
        this.mainActivity = mainActivity;
        url = "http://" + ip + ":" + port + "/get";
        start();
    }

    /** Creates an AsyncWebServerCall
     *  if the result from the AsyncTask is same as the last one nothing will happen
     *  else the result will parsed and the information will be extracted into
     *  Retrievable objects which will then be passed to the MainActivity which will
     *  manipulate the graphical interface using the information of the
     *  Retrievable objects
     *
     *  If the GetRunner is the continuous GetRunner it will run every five second
     *
     *  If a connection error occurs the Thread will be killed
     *  and the disconnected state will be set in the MainActivity
     * */
    @Override
    public void run() {
        alive = true;
        while (alive) {
            try {
                Log.i(MainActivity.TIME_TAG, "GetRunner Thread start " + System.currentTimeMillis());
                new AsyncWebServerCall(new AsyncCallback() {
                    @Override
                    public void receiveAsyncResult(String result) {
                        Log.i(MainActivity.TIME_TAG, "GetRunner Thread stop (get result) " + System.currentTimeMillis());
                        if (result == null) {
                            kill();
                            return;
                        }
                        if (!result.equals(lastGet)) {
                            mainActivity.setConnected();
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

    /** Parse the result into a list of Retrievable objects
     *
     *  A line starting with    D is a Device information line
     *                          T is a Condition Thread information line
     *                          S is a Sensor information line
     *                          P is a Protocol information line
     *
     *  The list will then be returned to the MainActivity for manipulating the
     *  graphical interface
     * */
    private void parseResult(String result) throws Exception {
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

    /** Kills the thread and sets the
     *  state to the disconnected state in the MainActivity
     * */
    void kill() {
        alive = false;
        mainActivity.setDisconnected();
    }
}
