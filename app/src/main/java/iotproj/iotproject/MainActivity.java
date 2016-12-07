package iotproj.iotproject;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private ImageButton talkBtn;
    private TextView output;
    private static final String TAG = "iotproj.iotproject.MA";

    private String ioTGateWayIP = "192.168.1.99";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViews();
        initButtonFunctionality();
    }

    private void getViews() {
        talkBtn = (ImageButton) findViewById(R.id.talk_button);
        output = (TextView) findViewById(R.id.output);
    }


    private void initButtonFunctionality() {
        talkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, 1);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String result = "" + data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            result = result.toLowerCase();
            Log.d(TAG, "got result = " + result);
            new AsyncWebServerCall(new AsyncCallback() {
                @Override
                public void receiveAsyncResult(String result) {
                    parseResult(result);
                }
            });
        } else {
            Toast.makeText(this, "Text-Till-Tal misslyckades", Toast.LENGTH_LONG).show();
        }
    }

    private void parseResult(String result) {

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setup:
                new SetupDialog(this).createAndShow();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setIoTGateWayIP(String ip) {

    }

    public String getIoTGateWayIP() {
        return ioTGateWayIP;
    }
}
