package iotproj.iotproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import iotproj.iotproject.model.ConditionThread;


/** This ArrayAdapter defines how the items in the Array will be used
 *  The Array will contain ConditionThread objects
 *  in the getView methods we define how a ConditionThread object will be displayed
 * */
public class ThreadListAdapter extends ArrayAdapter<ConditionThread> {

    public ThreadListAdapter(Context context, int resource, List<ConditionThread> objects) {
        super(context, resource, objects);
    }


    /** The Condition Threads id and voice command (that spawned it)
     *  will be shown in a TextView
     * */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ConditionThread conditionThread = getItem(position);
        if (conditionThread != null) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            TextView textView = new TextView(getContext());
            String text = "" + conditionThread.getId() + " " + conditionThread.getVoiceCommand();
            textView.setText(text);
            linearLayout.addView(textView);
            return linearLayout;
        }
        return super.getView(position, convertView, parent);
    }
}
