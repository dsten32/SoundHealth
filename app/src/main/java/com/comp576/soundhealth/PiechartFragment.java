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
import android.widget.Toast;

import com.appyvet.materialrangebar.RangeBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class PiechartFragment extends DialogFragment implements RangeBar.OnRangeBarChangeListener, View.OnClickListener {
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
        //feedbackText = view.findViewById(R.id.feedback_text);
        //feedbackText.setOnKeyListener(this::onKey);

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
}
