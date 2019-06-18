package com.comp576.soundhealth;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TimePicker;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HeatmapSettingDialogFragment extends DialogFragment implements View.OnClickListener {
    private CheckBox allDaysBox;
    private Button dismissBut;
    private View v;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_heatmap_settings_dialog, container, false);
        dismissBut = v.findViewById(R.id.frag_close);
        dismissBut.setOnClickListener(this);

        allDaysBox = (CheckBox) v.findViewById(R.id.allDaysBox);

        allDaysBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CheckBox[] checkArr = {
                        v.findViewById(R.id.monBox),
                        v.findViewById(R.id.tueBox),
                        v.findViewById(R.id.wedBox),
                        v.findViewById(R.id.thurBox),
                        v.findViewById(R.id.friBox),
                        v.findViewById(R.id.satBox),
                        v.findViewById(R.id.sunBox),
                };

                if (allDaysBox.isChecked()) {
                    for (CheckBox box : checkArr) {
                        box.setChecked(true);
                        box.setEnabled(false);
                    }
                } else {
                    for (CheckBox box : checkArr) {
                        box.setEnabled(true);
                    }
                }


            }
        });

        return v;
    }

    @Override
    public void onClick(View view) {
        Log.d("onclock:","yep");
        if(view.getId()==R.id.frag_close) {
            MapsActivity mapActivity = (MapsActivity) getActivity();
            mapActivity.setMapUserData(((RadioButton) v.findViewById(R.id.userData)).isChecked());

            CheckBox[] checkArr = {
                    v.findViewById(R.id.monBox),
                    v.findViewById(R.id.tueBox),
                    v.findViewById(R.id.wedBox),
                    v.findViewById(R.id.thurBox),
                    v.findViewById(R.id.friBox),
                    v.findViewById(R.id.satBox),
                    v.findViewById(R.id.sunBox),
            };

            String[] days ={"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
            for(int i=0;i < days.length;i++){
                if(!checkArr[i].isChecked()){
                    days[i]=null;
                }
            }
            Log.d("here's what frag got: ", Arrays.toString(days));

            mapActivity.setDaysToMap(days);

            try {
                mapActivity.dismissSettings(view);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

    }

    public static class StartTimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            Log.d("hour min = ",String.valueOf(hourOfDay) +" "+ String.valueOf(minute));
            MapsActivity mapActivity = (MapsActivity) getActivity();
            mapActivity.setStartHour(hourOfDay);
            mapActivity.setStartMin(minute);
        }
    }

    public static class StopTimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            Log.d("hour min = ",String.valueOf(hourOfDay) +" "+ String.valueOf(minute));
            MapsActivity mapActivity = (MapsActivity) getActivity();
            mapActivity.setStopHour(hourOfDay);
            mapActivity.setStopMin(minute);
        }
    }
}

