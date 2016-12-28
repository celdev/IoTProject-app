package iotproj.iotproject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/** This custom dialog provides the functionality to change
 *  the IP of the IoT Web server
 *
 *  if the ip is correct (if the web server returns "ok") the dialog will be closed
 *  otherwise a Toast with an error message will be shown
 * */
public class SetupDialog extends AlertDialog.Builder {

    private AlertDialog alertDialog;

    /** Creates the dialog using the layout file setup_dialog_layout
     *  and initializes the functionality of the button
     * */
    public SetupDialog(final MainActivity mainActivity) {
        super(mainActivity);
        setTitle(R.string.setup);
        final View view = mainActivity.getLayoutInflater().inflate(R.layout.setup_dialog_layout, null);
        setView(view);
        final EditText ipField = (EditText) view.findViewById(R.id.serverip_field);
        ipField.setText(mainActivity.getIoTGateWayIP());

        ((Button) view.findViewById(R.id.check_ip_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                progressDialog.setMessage(mainActivity.getString(R.string.checking_ip));
                progressDialog.show();
                new AsyncWebServerCall(new AsyncCallback() {
                    @Override
                    public void receiveAsyncResult(String result) {
                        progressDialog.dismiss();
                        if (result != null && result.trim().equals("ok")) {
                            mainActivity.setIoTGateWayIPandRestartGetRunner(ipField.getText().toString());
                            if (alertDialog != null) {
                                alertDialog.dismiss();
                            }
                        } else {
                            Toast.makeText(mainActivity,R.string.error_ip,Toast.LENGTH_LONG).show();
                        }
                    }
                }).execute("http://" + ipField.getText().toString() + ":" + MainActivity.IoT_GATEWAY_PORT + "/test");
            }
        });

        setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public void createAndShow() {
        alertDialog = create();
        alertDialog.show();
    }
}
