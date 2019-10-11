package com.comp576.soundhealth;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Data object for storing in Room library and holding data related variables when used in app
 */
@Entity
public class Data implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String date,time,userId;
    public Double lat;
    public Double lng;
    public Double dB;
    public boolean isBlurred;


    public Data(String date, String time, String userId, Double lat, Double lng, Double dB, boolean isBlurred) {
        this.date = date;
        this.time = time;
        this.userId = userId;
        this.lat = lat;
        this.lng = lng;
        this.dB = dB;
        this.isBlurred = isBlurred;
    }


    public static Data createFromParcel(Parcel source) {
        return CREATOR.createFromParcel(source);
    }

    public static Data[] newArray(int size) {
        return CREATOR.newArray(size);
    }

    protected Data(Parcel in) {
        id = in.readLong();
        date = in.readString();
        time = in.readString();
        userId = in.readString();
        if (in.readByte() == 0) {
            lat = null;
        } else {
            lat = in.readDouble();
        }
        if (in.readByte() == 0) {
            lng = null;
        } else {
            lng = in.readDouble();
        }
        if (in.readByte() == 0) {
            dB = null;
        } else {
            dB = in.readDouble();
        }
        boolean[] isBlurredArray = new boolean[1];
        in.readBooleanArray(isBlurredArray);
        isBlurred = isBlurredArray[0];
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(userId);
        if (lat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lat);
        }
        if (lng == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lng);
        }
        if (dB == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(dB);
        }
        boolean[] isBlurredArray = {isBlurred};
        dest.writeBooleanArray(isBlurredArray);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

    @Override
    public String toString() {

        return this.id
                + "; \n"
                + this.date
                + "; \n"
                + this.time
                + "; \n"
                + String.valueOf(this.lat)
                + "; \n"
                + String.valueOf(this.lng)
                + "; \n"
                + String.valueOf(Math.round(this.dB*100.0)/100.0);
    }
}