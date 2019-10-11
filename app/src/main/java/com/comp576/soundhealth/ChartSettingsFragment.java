package com.comp576.soundhealth;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Switch;

import com.appyvet.materialrangebar.RangeBar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

/**
 *set up chart settings dialog and update chart class with user chosen variables
 */
public class ChartSettingsFragment extends DialogFragment implements RangeBar.OnRangeBarChangeListener, View.OnClickListener, OnCheckedChangeListener {
    ChartActivity chartActivity;
    View view;

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
        view = inflater.inflate(R.layout.fragment_chart_settings_dialog, container, false);
        chartActivity = (ChartActivity) getActivity();
        RangeBar dBRange = view.findViewById(R.id.range_bar);
        dBRange.setOnRangeBarChangeListener(this);
        dBRange.setRangePinsByIndices(chartActivity.lowestDB, chartActivity.highestDB - 1);
        RadioButton absButton = view.findViewById(R.id.dataAbsolute);
        absButton.setChecked(chartActivity.isAbsolute);
        absButton.setOnCheckedChangeListener(this);
        RadioButton relButton = view.findViewById(R.id.dataRelative);
        relButton.setOnCheckedChangeListener(this);
        RadioButton timeButton = view.findViewById(R.id.dataTimeline);
        timeButton.setChecked(chartActivity.isTimeline);
        timeButton.setOnCheckedChangeListener(this);
        Switch colourBlindSwitch = view.findViewById(R.id.cBlind);
        colourBlindSwitch.setOnCheckedChangeListener(this);
        colourBlindSwitch.setChecked(chartActivity.isColourBlind);

        return view;
    }


    @Override
    public void onClick(View v) {
        dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
        ((ChartActivity) getActivity()).lowestDB = leftPinIndex;
        ((ChartActivity) getActivity()).highestDB = rightPinIndex + 1;
        ((ChartActivity) getActivity()).pieChartAddData();
    }

    @Override
    public void onTouchStarted(RangeBar rangeBar) {}

    @Override
    public void onTouchEnded(RangeBar rangeBar) {}

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.dataRelative:
                chartActivity.isRelative = ((RadioButton) view.findViewById(R.id.dataRelative)).isChecked();
                chartActivity.barChartAddData();
                break;
            case R.id.dataAbsolute:
                chartActivity.isAbsolute = ((RadioButton) view.findViewById(R.id.dataAbsolute)).isChecked();
                chartActivity.barChartAddData();
                break;
            case R.id.dataTimeline:
                chartActivity.isTimeline = ((RadioButton) view.findViewById(R.id.dataTimeline)).isChecked();
                chartActivity.barChartAddData();
                break;
            case R.id.cBlind:

                if(isChecked){
                    ((ChartActivity) getActivity()).isColourBlind = true;
                } else {
                    ((ChartActivity) getActivity()).isColourBlind = false;
                }
                ((ChartActivity) getActivity()).setChartColours();
                ((ChartActivity) getActivity()).pieChartAddData();
                ((ChartActivity) getActivity()).barChartAddData();
                break;
        }
    }
}
