package iotproj.iotproject.model;

import android.util.Log;

import java.util.Arrays;

/** The Sensor class represents a IoT unit that
 *  has sensor capabilities and hold information about
 *  what it senses
 *
 *  In this project temperature sensors are used and they
 *  share the same headers (ID, TEMP, Updated at... etc)
 *
 *  The values of the sensor will be parsed in the parseValues-method
 *  and stored in the values-variable
 * */
public class Sensor extends Retrievable {

    public static String[] headers;
    public String[] values;

    public Sensor(String line) throws IncorrectRetrievableException{
        super(RetrievableType.SENSOR,line);
    }

    /** Takes a String in the form of
     *  ["value","value","","","value",...]
     *  converts it into an array of Strings by splitting the String by the comma sign
     *  and stores it in the values variable
     * */
    private void setValues(String values) {
        //Log.d("setting value", values);
        this.values = values.replace("[", "").replace("]", "").split(",");
        //Log.d("replaced values are = ", Arrays.toString(this.values));
    }

    /** The implementation of the parser for Sensors
     *  extracts the ID and the values of the sensor
     * */
    @Override
    void parseValues(String line) throws Exception {
        //Log.d("parstag", "parsing line " + line);
        String[] parts = line.split("%");
        //Log.d("parstag", Arrays.toString(parts));
        setId(Integer.parseInt(parts[1]));
        setValues(parts[2]);
    }

    /** Returns the value of the the field below
     *  the header i.e. if the head parameter is "TEMP" 20.1 will be returned
     *  from the sensor with the headers and values below
     *  ID      TEMP    HUMIDITY
     *  135     20.1    50%
     * */
    public String getValueOfHead(String head) {
        //Log.d("sensor", "Trying to get value of head = " + head);
        if (headers == null) {
            return null;
        }
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(head)) {
                //Log.d("returning: ", "head index = " + i + " head = " + head + " value = " + values[i]);
                return values[i];
            }
        }
        return null;
    }

    /** Sets the headers of the sensors
     *
     *  Since this project only uses temperature sensors all
     *  sensors will share the same headers
     **/
    public static void setHeaders(String headers) {
        Sensor.headers = headers.split("%")[1].replace("[", "").replace("]", "").split(",");
    }


}
