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

public class PiechartFragment extends DialogFragment implements RangeBar.OnRangeBarChangeListener,View.OnKeyListener, View.OnClickListener {
    private RangeBar dBRange;
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
        dBRange = view.findViewById(R.id.range_bar);
        dBRange.setOnRangeBarChangeListener(this);
        dBRange.setRangePinsByIndices(chartActivity.lowestDB,chartActivity.highestDB);
        //feedbackText = view.findViewById(R.id.feedback_text);
        //feedbackText.setOnKeyListener(this::onKey);

        Button submit = view.findViewById(R.id.feedback_submit);
        submit.setOnClickListener(this::onClick);

        return view;
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
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

    @Override
    public void onClick(View v) {
            //mainActivity.feedbackText = feedbackText.getText().toString();
            //mainActivity.feedbackRating = rateBar.getRating();
            //mainActivity.dismissSettings(v);
            dismiss();
            //Toast.makeText(getContext(),"rating= "+ mainActivity.feedbackRating +' '+mainActivity.feedbackText,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
        ((ChartActivity) getActivity()).lowestDB = leftPinIndex;
        ((ChartActivity) getActivity()).highestDB = rightPinIndex;
    }

    @Override
    public void onTouchStarted(RangeBar rangeBar) {

    }

    @Override
    public void onTouchEnded(RangeBar rangeBar) {

    }
}
