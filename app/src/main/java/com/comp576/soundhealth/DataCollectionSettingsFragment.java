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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DataCollectionSettingsFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener,SeekBar.OnSeekBarChangeListener,View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private View view;
    private MainActivity mainActivity;
    private EditText interval, dataStopTimeEntry;
    private TextView showBlurValue;
    private CheckBox setDataStopTime, setBlur;
    private SeekBar blurBarValue;
    private boolean isBlurred, isStopTime;
    private String dataStopTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();

        view = inflater.inflate(R.layout.fragment_data_settings_fragment, container, false);
        interval = view.findViewById(R.id.interval);
        interval.setText(String.valueOf(mainActivity.getInterval()));

        dataStopTimeEntry = view.findViewById(R.id.dataStopTimeEntry);
        dataStopTimeEntry.setEnabled(mainActivity.isStopTime());
        dataStopTimeEntry.setText(mainActivity.getDataStopTime());

        showBlurValue = view.findViewById(R.id.showBlurValue);
        showBlurValue.setText(String.valueOf(mainActivity.getBlurValue())+"km");

        setDataStopTime = view.findViewById(R.id.setDataStopTime);
        setDataStopTime.setChecked(mainActivity.isStopTime());
        setDataStopTime.setOnCheckedChangeListener(this);

        setBlur = view.findViewById(R.id.setBlur);
        setBlur.setChecked(mainActivity.isBlurred());
        setBlur.setOnCheckedChangeListener(this);

        blurBarValue = view.findViewById(R.id.blurBarValue);
        blurBarValue.setEnabled(mainActivity.isBlurred());
        blurBarValue.setProgress((int)(mainActivity.getBlurValue()*10));
        blurBarValue.setOnSeekBarChangeListener(this);

        Button dismissBut = view.findViewById(R.id.frag_close);

        dismissBut.setOnClickListener(this);
        return view;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mainActivity.setStopHour(hourOfDay);
        mainActivity.setStopMin(minute);
        mainActivity.setDataStopTime(String.format("%1$" + 2 + "s", hourOfDay).replace(' ', '0')+":"+String.format("%1$" + 2 + "s", minute).replace(' ', '0'));
        dataStopTimeEntry.setText(mainActivity.getDataStopTime());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.frag_close:
                mainActivity.setInterval(Integer.parseInt(String.valueOf(interval.getText())));
                mainActivity.dismissSettings(view);
                Log.d("here's what frag got: ", "dialog: "+String.valueOf(setBlur.isChecked())+"mainactivity: "+String.valueOf(mainActivity.isBlurred()));
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mainActivity.setBlurValue(((float)progress/10));
        showBlurValue.setText(String.valueOf(mainActivity.getBlurValue())+"km");
        Log.d("blurval: ",String.valueOf(mainActivity.getBlurValue()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d("buttoncheck","changed");
        switch (buttonView.getId()) {
            case R.id.setDataStopTime:
                mainActivity.setStopTime(isChecked);
                dataStopTimeEntry.setEnabled(isChecked);
                break;
            case R.id.setBlur:
                mainActivity.setBlurred(isChecked);
                blurBarValue.setEnabled(isChecked);
                Log.d("click blur", "yep");
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

            return new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getFragmentManager().findFragmentByTag("dataDialog"), hour, minute,
                    true);
        }
    }

}
