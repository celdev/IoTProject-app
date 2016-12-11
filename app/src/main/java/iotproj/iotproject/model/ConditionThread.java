package iotproj.iotproject.model;


import android.util.Log;

import java.util.Arrays;

public class ConditionThread extends Retrievable {


    private String voiceCommand;

    public ConditionThread(String line) throws IncorrectRetrievableException {
        super(RetrievableType.THREAD, line);
    }

    @Override
    void parseValues(String line) {
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
