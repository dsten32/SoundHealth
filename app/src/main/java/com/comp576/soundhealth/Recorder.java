package com.comp576.soundhealth;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import static android.Manifest.permission.RECORD_AUDIO;
import static androidx.core.content.ContextCompat.checkSelfPermission;

/*base code for this class used from:
https://stackoverflow.com/questions/10655703/what-does-androids-getmaxamplitude-function-for-the-mediarecorder-actually-gi
user https://stackoverflow.com/users/806920/lukas-ruge
*/
public class Recorder{

    private final String TAG = "Recorder Function: ";
    public static double REFERENCE = 0.00002;

    public double getNoiseLevel() throws NoValidNoiseLevelException
    {

        Log.i(TAG, "start new recording process");
        int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT);
        //making the buffer bigger....
        bufferSize=bufferSize*4;
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        short data [] = new short[bufferSize];
        double average = 0.0;
        recorder.startRecording();
        //recording data;
        recorder.read(data, 0, bufferSize);

        recorder.stop();
        Log.i(TAG, "stop");
        for (short s : data)
        {
            if(s>0)
            {
                average += Math.abs(s);
            }
            else
            {
                bufferSize--;
            }
        }
        //x=max;
        double x = average/bufferSize;
        Log.i(TAG, ""+x);
        recorder.release();
        Log.i(TAG, "getNoiseLevel() ");
        double db=0;
        if (x==0){
            throw new NoValidNoiseLevelException(x);
        }
        // calculating the pascal pressure based on the idea that the max amplitude (between 0 and 32767) is
        // relative to the pressure
        double pressure = x/51805.5336; //the value 51805.5336 can be derived from assuming that x=32767=0.6325 Pa and x=1 = 0.00002 Pa (the reference value)
        Log.i(TAG, "x="+pressure +" Pa");
        db = (20 * Math.log10(pressure/REFERENCE));
        Log.i(TAG, "db="+db);
        if(db>0)
        {
            return db;
        }
        throw new NoValidNoiseLevelException(x);
    }
}