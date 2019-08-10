package com.comp576.soundhealth;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class BarInfoFragment extends DialogFragment{
    //    private TextView numPoints,firstPoint,lastPoint,avgMins,highestDB;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
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
