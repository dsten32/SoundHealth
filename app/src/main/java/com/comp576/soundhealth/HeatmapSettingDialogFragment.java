package com.comp576.soundhealth;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class HeatmapSettingDialogFragment extends DialogFragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    public static final int START_DATE_FLAG=1;
    public static final int END_DATE_FLAG = 0;

    private CheckBox allDaysBox;
    private Button dismissBut;
    private View v;
    private MapsActivity mapActivity;
    private CheckBox[] checkDaysArr;
    private Boolean[] daysSelected = new Boolean[7];
    public EditText sDate,eDate;

    private int flag = 0;
    public void setFlag(int i){
        flag=i;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mapActivity = (MapsActivity) getActivity();
        v = inflater.inflate(R.layout.fragment_heatmap_settings_dialog, container, false);

        ((RadioButton)v.findViewById(R.id.userData)).setChecked(mapActivity.getMapUserData());
        ((RadioButton)v.findViewById(R.id.allData)).setChecked(!mapActivity.getMapUserData());
        sDate = v.findViewById(R.id.sDate);
        eDate = v.findViewById(R.id.eDate);

        checkDaysArr = new CheckBox[]{
                v.findViewById(R.id.monBox),
                v.findViewById(R.id.tueBox),
                v.findViewById(R.id.wedBox),
                v.findViewById(R.id.thurBox),
                v.findViewById(R.id.friBox),
                v.findViewById(R.id.satBox),
                v.findViewById(R.id.sunBox),
        };

        dismissBut = v.findViewById(R.id.frag_close);
        dismissBut.setOnClickListener(this);
        allDaysBox = (CheckBox) v.findViewById(R.id.allDaysBox);

        for(int i=0;i<daysSelected.length;i++){
            if (mapActivity.daysToMap[i]==null){
                checkDaysArr[i].setChecked(false);
            } else {
                checkDaysArr[i].setChecked(true);
            }
        }

        allDaysBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (allDaysBox.isChecked()) {
                    for (CheckBox box : checkDaysArr) {
                        box.setChecked(true);
                        box.setEnabled(false);
                    }
                } else {
                    for (CheckBox box : checkDaysArr) {
                        box.setEnabled(true);
                    }
                }
            }
        });
        return v;
    }

    @Override
    public void onClick(View view) {
        Log.d("onclIck:","yep");
        if(view.getId()==R.id.frag_close) {
            mapActivity.setMapUserData(((RadioButton) v.findViewById(R.id.userData)).isChecked());

            String[] days ={"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
            for(int i=0;i < days.length;i++){
                if(!checkDaysArr[i].isChecked()){
                    days[i]=null;
                }
            }
            Log.d("here's what frag got: ", Arrays.toString(days));
            mapActivity.setDaysToMap(days);
            mapActivity.dismissSettings(view);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        int id = flag;
        mapActivity = (MapsActivity) getActivity();
        mapActivity.setsDay(dayOfMonth);
        mapActivity.setsMonth(month);
        mapActivity.setsYear(year);
        Toast.makeText(getContext(),"hi",Toast.LENGTH_SHORT).show();
        switch(id){
            case 1:
                sDate.setText(dayOfMonth+"/"+month+"/"+year);
            case 0:
                eDate.setText(dayOfMonth+"/"+month+"/"+year);
            default:
                    break;

        }

    }

    public static class StartTimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    true);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
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
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    true);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Log.d("hour min = ",String.valueOf(hourOfDay) +" "+ String.valueOf(minute));
            MapsActivity mapActivity = (MapsActivity) getActivity();
            mapActivity.setStopHour(hourOfDay);
            mapActivity.setStopMin(minute);
        }
    }

    public static class DatePickerFragment extends DialogFragment {


        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR); // current year
            int month = c.get(Calendar.MONTH); // current month
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getFragmentManager().findFragmentByTag("dialog"),year,month,day);
        }

//        @Override
//        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//            MapsActivity mapActivity = (MapsActivity) getActivity();
//            mapActivity.setsDay(dayOfMonth);
//            mapActivity.setsMonth(month);
//            mapActivity.setsYear(year);
//        }
    }
    public static class StopDatePickerFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR); // current year
            int month = c.get(Calendar.MONTH); // current month
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getFragmentManager().findFragmentByTag("dialog"),year,month,day);
        }

//        @Override
//        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//            MapsActivity mapActivity = (MapsActivity) getActivity();
//            mapActivity.setsDay(dayOfMonth);
//            mapActivity.setsMonth(month);
//            mapActivity.setsYear(year);
//        }
    }
}

