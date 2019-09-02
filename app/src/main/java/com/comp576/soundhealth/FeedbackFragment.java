package com.comp576.soundhealth;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class FeedbackFragment extends DialogFragment implements View.OnKeyListener, View.OnClickListener {
    private MainActivity mainActivity;
    private EditText messageText;
    private TextView path;

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
        View view = inflater.inflate(R.layout.fragment_feedback_dialog, container, false);
        mainActivity = (MainActivity) getActivity();
//        rateBar = view.findViewById(R.id.rating_bar);

        messageText = view.findViewById(R.id.feedback_text);
        messageText.setText(mainActivity.shareMessage);
        messageText.setMovementMethod(new ScrollingMovementMethod());
        messageText.setOnKeyListener(this::onKey);
        path = view.findViewById(R.id.csvPath);
        path.setText(mainActivity.file.toString());

        Button share = view.findViewById(R.id.feedback_submit);
        share.setOnClickListener(this::onClick);
        Button close = view.findViewById(R.id.feedback_close);
        close.setOnClickListener(this::onClick);

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
        if(v.getId()==R.id.feedback_submit){
            mainActivity.dismissSettings(v);
            mainActivity.shareCSV();
//            mainActivity.exportShare();
        } else if(v.getId()==R.id.feedback_close){
            mainActivity.dismissSettings(v);
        }
    }
}
