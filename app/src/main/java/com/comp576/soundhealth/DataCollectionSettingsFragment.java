package com.comp576.soundhealth;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Sets up the continuous colletion settings and blur setting. Handle passing these user settings
 * back to the main activity. Does this by accessing the activity directly. This could be replaced
 * with sharedpreferences?
 */
public class DataCollectionSettingsFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnKeyListener {
    private View view;
    private MainActivity mainActivity;
    private EditText interval, dataStopTimeEntry;
    private TextView showBlurValue;
    private CheckBox setDataStopTime, setBlur;
    private SeekBar blurBarValue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();

        view = inflater.inflate(R.layout.fragment_data_settings_fragment, container, false);
        view.setClipToOutline(true);

        interval = view.findViewById(R.id.interval);
        interval.setText(String.valueOf(mainActivity.getInterval()));
        interval.setOnKeyListener(this);
        interval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(interval);
                }
            }
        });

        dataStopTimeEntry = view.findViewById(R.id.dataStopTimeEntry);
        dataStopTimeEntry.setEnabled(mainActivity.isStopTime());
        dataStopTimeEntry.setText(mainActivity.getDataStopTime());

        showBlurValue = view.findViewById(R.id.showBlurValue);
        showBlurValue.setText(String.valueOf(mainActivity.getBlurValue()) + "km");

        setDataStopTime = view.findViewById(R.id.setDataStopTime);
        setDataStopTime.setChecked(mainActivity.isStopTime());
        setDataStopTime.setOnCheckedChangeListener(this);

        setBlur = view.findViewById(R.id.setBlur);
        setBlur.setChecked(mainActivity.isBlurred());
        setBlur.setOnCheckedChangeListener(this);

        blurBarValue = view.findViewById(R.id.blurBarValue);
        blurBarValue.setEnabled(mainActivity.isBlurred());
        blurBarValue.setProgress((int) (mainActivity.getBlurValue() * 10));
        blurBarValue.setOnSeekBarChangeListener(this);

        Button dismissBut = view.findViewById(R.id.frag_close);

        dismissBut.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mainActivity.setStopHour(hourOfDay);
        mainActivity.setStopMin(minute);
        mainActivity.setDataStopTime(String.format("%1$" + 2 + "s", hourOfDay).replace(' ', '0') + ":" + String.format("%1$" + 2 + "s", minute).replace(' ', '0'));
        dataStopTimeEntry.setText(mainActivity.getDataStopTime());
    }

    @Override
    public void onClick(View v) {
        if(String.valueOf(interval.getText()).contains(".")){
            interval.setError("Sorry, interval value has to be a whole number");
        }
        if (interval.getError() == null) {
            mainActivity.dismissSettings(v);
            mainActivity.setInterval(Integer.parseInt(String.valueOf(interval.getText())));

            if (!mainActivity.isCollecting && mainActivity.continuousSwitch.isChecked()) {
                mainActivity.scheduleDataCollection();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        hideKeyboard(seekBar);
        mainActivity.setBlurValue(((float) progress / 10));
        showBlurValue.setText(String.valueOf(mainActivity.getBlurValue()) + "km");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        hideKeyboard(buttonView);
        switch (buttonView.getId()) {
            case R.id.setDataStopTime:
                mainActivity.setStopTime(isChecked);
                dataStopTimeEntry.setEnabled(isChecked);
                dataStopTimeEntry.requestFocus();
                if (isChecked) {
                    mainActivity.showPickerDialog(buttonView);
                }
                break;
            case R.id.setBlur:
                mainActivity.setBlurred(isChecked);
                blurBarValue.setEnabled(isChecked);
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v.getId() == R.id.interval && keyCode == KeyEvent.KEYCODE_ENTER) {
            setDataStopTime.requestFocus();
            hideKeyboard(v);
            return true;
        }
        return false;
    }

    private void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
