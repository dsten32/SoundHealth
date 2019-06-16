package com.comp576.soundhealth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class HeatmapSettingDialogFragment extends DialogFragment implements View.OnClickListener {
    private CheckBox allDaysBox;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_heatmap_settings_dialog, container, false);

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
    public void onClick(View v) {

    }


}
