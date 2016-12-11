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

public class ThreadListAdapter extends ArrayAdapter<ConditionThread> {

    public ThreadListAdapter(Context context, int resource, List<ConditionThread> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
