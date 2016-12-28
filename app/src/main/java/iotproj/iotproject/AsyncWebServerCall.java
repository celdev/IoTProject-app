package iotproj.iotproject;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class AsyncWebServerCall extends AsyncTask<String, Void, String> {

    private static final String TAG = "iotproj.async" ;
    private AsyncCallback asyncCallback;

    public AsyncWebServerCall(AsyncCallback asyncCallback) {
        this.asyncCallback = asyncCallback;
    }


    @Override
    protected String doInBackground(String... params) {
        return urlResponse(params[0]);
    }

    private String urlResponse(String urlStr) {
        try {
            Log.i(MainActivity.TIME_TAG, "urlResponse start " + System.currentTimeMillis());
            URL url = new URL(urlStr);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            in.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.i(MainActivity.TIME_TAG, "urlResponse stop " + System.currentTimeMillis());
        asyncCallback.receiveAsyncResult(s);
    }
}
