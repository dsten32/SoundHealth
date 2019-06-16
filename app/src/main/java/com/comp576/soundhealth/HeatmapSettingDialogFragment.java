package com.comp576.soundhealth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
//            ((RadioButton) v.findViewById(R.id.userData)).isChecked();
//
//            mapActivity.setMapUserData(((RadioButton) v.findViewById(R.id.userData)).isChecked());

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

            mapActivity.dismissSettings(view);

        }
    }


}
