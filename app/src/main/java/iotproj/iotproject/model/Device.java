package iotproj.iotproject.model;

import android.util.Log;

import java.util.Arrays;

/** This class represents the IoT unit that can have a On/Off state
 *
 *  The device has a name and a state
 * */
public class Device extends Retrievable {

    private State state;
    private String name;

    public Device(String line) throws IncorrectRetrievableException {
        super(RetrievableType.DEVICE, line);
    }

    /** The implementation of the parser for Devices
     *  extracts the ID and and the Device information (state and name)
     * */
    @Override
    void parseValues(String line) throws Exception {
        //Log.d("parstag", "parsing line " + line);
        String[] parts = line.split("%");
        //Log.d("parstag", Arrays.toString(parts));
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
