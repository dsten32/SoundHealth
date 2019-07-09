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

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class FeedbackFragment extends DialogFragment implements RatingBar.OnRatingBarChangeListener,View.OnKeyListener, View.OnClickListener {
    private MainActivity mainActivity;
    private EditText feedbackText;
    private RatingBar rateBar;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback_dialog, container, false);
        mainActivity = (MainActivity) getActivity();
        rateBar = view.findViewById(R.id.rating_bar);

        feedbackText = view.findViewById(R.id.feedback_text);
        feedbackText.setOnKeyListener(this::onKey);

        Button submit = view.findViewById(R.id.feedback_submit);
        submit.setOnClickListener(this::onClick);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

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
            mainActivity.feedbackText = feedbackText.getText().toString();
            mainActivity.feedbackRating = rateBar.getRating();
            mainActivity.dismissSettings(v);
            sendMess();
            Toast.makeText(getContext(),"rating= "+ mainActivity.feedbackRating +' '+mainActivity.feedbackText,Toast.LENGTH_LONG).show();
        }
    }

    public void sendMess(){
        MyFirebaseMessagingService fms = new MyFirebaseMessagingService();
        fms.sendUpstream();
    }
}
