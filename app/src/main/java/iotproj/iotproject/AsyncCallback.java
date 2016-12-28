package iotproj.iotproject;

/** Implementation of this interface means that the
 *  object can handle the result of an AsyncTask which returns a String
 * */
interface AsyncCallback {

    void receiveAsyncResult(String result);

}
