package com.comp576.soundhealth;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.appyvet.materialrangebar.RangeBar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

public class PiechartFragment extends DialogFragment implements RangeBar.OnRangeBarChangeListener, View.OnClickListener, Switch.OnCheckedChangeListener {
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
        View view = inflater.inflate(R.layout.fragment_piechart_dialog, container, false);
        ChartActivity chartActivity= (ChartActivity) getActivity();
        RangeBar dBRange = view.findViewById(R.id.range_bar);
        dBRange.setOnRangeBarChangeListener(this);
        dBRange.setRangePinsByIndices(chartActivity.lowestDB,chartActivity.highestDB-1);
        Switch colourBlindSwitch = view.findViewById(R.id.cBlind);
        colourBlindSwitch.setOnCheckedChangeListener(this);
        colourBlindSwitch.setChecked(chartActivity.isColourBlind);
        //shareMessage = view.findViewById(R.id.feedback_text);
        //shareMessage.setOnKeyListener(this::onKey);

//        Button submit = view.findViewById(R.id.feedback_submit);
//        submit.setOnClickListener(this::onClick);

        return view;
    }



    @Override
    public void onClick(View v) {
            dismiss();
    }

    @Override
    public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
        ((ChartActivity) getActivity()).lowestDB = leftPinIndex;
        ((ChartActivity) getActivity()).highestDB = rightPinIndex+1;
        ((ChartActivity) getActivity()).pieChartAddData();
    }

    @Override
    public void onTouchStarted(RangeBar rangeBar) {

    }

    @Override
    public void onTouchEnded(RangeBar rangeBar) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(isChecked){
            ((ChartActivity) getActivity()).isColourBlind = true;
        } else {
            ((ChartActivity) getActivity()).isColourBlind = false;
        }
        ((ChartActivity) getActivity()).setChartColours();
        ((ChartActivity) getActivity()).pieChartAddData();
        ((ChartActivity) getActivity()).barChartAddData();
    }
}
