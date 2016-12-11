package iotproj.iotproject.model;

import android.util.Log;

import java.util.Arrays;

public class Sensor extends Retrievable {

    public static String[] headers;
    public String[] values;

    public Sensor(String line) throws IncorrectRetrievableException{
        super(RetrievableType.SENSOR,line);
    }

    private void setValues(String values) {
        Log.d("setting value", values);
        this.values = values.replace("[", "").replace("]", "").split(",");
        Log.d("replaced values are = ", Arrays.toString(this.values));
    }

    @Override
    void parseValues(String line) {
        Log.d("parstag", "parsing line " + line);
        String[] parts = line.split("%");
        Log.d("parstag", Arrays.toString(parts));
        setId(Integer.parseInt(parts[1]));
        setValues(parts[2]);
    }

    public String getValueOfHead(String head) {
        Log.d("sensor", "Trying to get value of head = " + head);
        if (headers == null) {
            Log.d("test", "head is null");
            return null;
        }
        Log.d("tag", "headers lengths = " + headers.length);
        Log.d("headers", Arrays.toString(headers));
        Log.d("values", Arrays.toString(values));
        for (int i = 0; i < headers.length; i++) {
            Log.d("checking head", "currect check = " + headers[i] + " index = " + i + " check is " + head + " they are equal " + head.equalsIgnoreCase(headers[i]));
            if (headers[i].trim().equalsIgnoreCase(head)) {
                Log.d("returning: ", "head index = " + i + " head = " + head + " value = " + values[i]);
                return values[i];
            }
        }
        return null;
    }

    public static void setHeaders(String headers) {
        Sensor.headers = headers.split("%")[1].replace("[", "").replace("]", "").split(",");
    }


}
