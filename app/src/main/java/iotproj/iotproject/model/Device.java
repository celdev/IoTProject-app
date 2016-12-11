package iotproj.iotproject.model;

import android.util.Log;

import java.util.Arrays;

public class Device extends Retrievable {

    private State state;
    private String name;

    public Device(String line) throws IncorrectRetrievableException {
        super(RetrievableType.DEVICE, line);
    }


    @Override
    void parseValues(String line) {
        Log.d("parstag", "parsing line " + line);
        String[] parts = line.split("%");
        Log.d("parstag", Arrays.toString(parts));
        setId(Integer.parseInt(parts[1]));
        state = parts[2].equalsIgnoreCase("ON") ? State.ON : State.OFF;
        name = parts[3];
    }

    public State getState() {
        return state;
    }

    public String getName() {
        return name;
    }
}
