package iotproj.iotproject.model;


import android.util.Log;

import java.util.Arrays;

/** This class represents a Condition Thread that is running on the web server
 *
 *  A condition thread has an ID and a message that caused the condition thread to
 *  be created.
 *
 *  i.e. the command "turn off the lights when it's less than 20 degrees"
 *  will spawn a condition thread that will execute the action "turn off the lights"
 *  when the temperature is less than 20 degrees
 * */
public class ConditionThread extends Retrievable {


    private String voiceCommand;

    public ConditionThread(String line) throws IncorrectRetrievableException {
        super(RetrievableType.THREAD, line);
    }

    /** The implementation of the parser for Condition Threads
     *  extracts the ID and the message of the condition
     * */
    @Override
    void parseValues(String line) throws Exception {
        Log.d("parstag", "parsing line " + line);
        String[] parts = line.split("%");
        Log.d("parstag", Arrays.toString(parts));
        setId(Integer.parseInt(parts[1]));
        voiceCommand = parts[2].replace("[", "").replace("]", "");
    }

    public String getVoiceCommand() {
        return voiceCommand;
    }
}
