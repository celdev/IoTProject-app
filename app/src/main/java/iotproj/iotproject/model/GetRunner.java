package iotproj.iotproject.model;

import java.util.ArrayList;
import java.util.List;

import iotproj.iotproject.AsyncCallback;
import iotproj.iotproject.AsyncWebServerCall;
import iotproj.iotproject.MainActivity;

public class GetRunner extends Thread {

    public String url;
    private boolean alive;

    private String lastGet;
    private MainActivity mainActivity;

    public GetRunner(String ip, int port, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        url = "http://" + ip + ":" + port + "/get";
        start();
    }

    @Override
    public void run() {
        alive = true;
        while (alive) {
            try {
                new AsyncWebServerCall(new AsyncCallback() {
                    @Override
                    public void receiveAsyncResult(String result) {
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
                sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                kill();
            }
        }
    }

    public void parseResult(String result) throws Exception {
        result = result.replace("<pre>", "").replace("</pre>", "");
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
        mainActivity.updateList(iotResults);
    }

    public void kill() {
        alive = false;
    }
}
