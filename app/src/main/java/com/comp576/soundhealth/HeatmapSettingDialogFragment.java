package com.comp576.soundhealth;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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

import java.util.Arrays;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Sets up the settings dialog. allows user to choose how the heatmap data is filtered.
 * can select days, time, date range, user or full dataset. passes user choices back to map activity
 */
public class HeatmapSettingDialogFragment extends DialogFragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static final int START_FLAG =1;
    public static final int END_FLAG = 0;

    private CheckBox allDaysBox;
    private Button dismissBut;
    private View v;
    private MapsActivity mapActivity;
    private CheckBox[] checkDaysArr;
    private Boolean[] daysSelected = new Boolean[7];
    public EditText sDate,eDate,sTime,eTime;
    private Calendar calendar;

    private int dateFlag = 0;
    private int timeFlag = 0;
    public void setDateFlag(int i){
        dateFlag =i;
    }
    public void setTimeFlag(int i){
        timeFlag =i;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mapActivity = (MapsActivity) getActivity();
        calendar = Calendar.getInstance();
        v = inflater.inflate(R.layout.fragment_heatmap_settings_dialog, container, false);

        ((RadioButton)v.findViewById(R.id.userData)).setChecked(mapActivity.getMapUserData());
        ((RadioButton)v.findViewById(R.id.allData)).setChecked(!mapActivity.getMapUserData());
        sDate = v.findViewById(R.id.sDate);
        eDate = v.findViewById(R.id.eDate);
        sTime = v.findViewById(R.id.sTime);
        eTime = v.findViewById(R.id.eTime);

        sDate.setText(mapActivity.getsDay()+"/"+String.format("%1$" + 2 + "s", mapActivity.getsMonth()).replace(' ', '0')+"/"+mapActivity.getsYear()); // current year
        eDate.setText(mapActivity.geteDay()+"/"+String.format("%1$" + 2 + "s", mapActivity.geteMonth()).replace(' ', '0')+"/"+mapActivity.geteYear()); // current year
        sTime.setText(String.format("%1$" + 2 + "s", mapActivity.getStartHour()).replace(' ', '0')+":"+String.format("%1$" + 2 + "s", mapActivity.getStartMin()).replace(' ', '0'));
        eTime.setText(String.format("%1$" + 2 + "s", mapActivity.getStopHour()).replace(' ', '0')+":"+String.format("%1$" + 2 + "s", mapActivity.getStopMin()).replace(' ', '0'));

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
        int id = dateFlag;
        mapActivity = (MapsActivity) getActivity();
        switch(id){
            case HeatmapSettingDialogFragment.START_FLAG:
                mapActivity.setsDay(dayOfMonth);
                mapActivity.setsMonth(month+1);
                mapActivity.setsYear(year);
                sDate.setText(dayOfMonth+"/"+String.format("%1$" + 2 + "s", (month+1)).replace(' ', '0')+"/"+year);
                break;
            case HeatmapSettingDialogFragment.END_FLAG:
                mapActivity.seteDay(dayOfMonth);
                mapActivity.seteMonth(month+1);
                mapActivity.seteYear(year);
                eDate.setText(dayOfMonth+"/"+String.format("%1$" + 2 + "s", (month+1)).replace(' ', '0')+"/"+year);
                break;
            default:
                break;
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        int id = timeFlag;
        switch(id){
            case HeatmapSettingDialogFragment.START_FLAG:
                mapActivity.setStartHour(hourOfDay);
                mapActivity.setStartMin(minute);
                sTime.setText(String.format("%1$" + 2 + "s", hourOfDay).replace(' ', '0')+":"+String.format("%1$" + 2 + "s", minute).replace(' ', '0'));
                break;
            case HeatmapSettingDialogFragment.END_FLAG:
                mapActivity.setStopHour(hourOfDay);
                mapActivity.setStopMin(minute);
                eTime.setText(String.format("%1$" + 2 + "s", hourOfDay).replace(' ', '0')+":"+String.format("%1$" + 2 + "s", minute).replace(' ', '0'));
                break;
            default:
                break;
        }
    }

    public static class TimePickerFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getFragmentManager().findFragmentByTag("dialog"), hour, minute,
                    true);
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
    }
}

