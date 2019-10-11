package com.comp576.soundhealth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
/*
Class to generate the bar chart day column information dialog.
Triggered on barchart column long touch.
 */
public class BarInfoFragment extends DialogFragment{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
    /*
    Inflate the dialog layout and populate with the cahrt information.
    Retrieve the info from the chart activity via a string array.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_bar_data, container, false);
        String[] info = ((ChartActivity) getActivity()).barInfoArray;
        ((TextView)view.findViewById(R.id.numPoints)).setText(info[0]);
        ((TextView)view.findViewById(R.id.firstPoint)).setText(info[1]);
        ((TextView)view.findViewById(R.id.lastPoint)).setText(info[2]);
        ((TextView)view.findViewById(R.id.avgMins)).setText(info[3]);
        ((TextView)view.findViewById(R.id.highestPoint)).setText(info[4]);
        return view;
    }
}
