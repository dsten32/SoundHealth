package com.comp576.soundhealth;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Data implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String date,time,userId;
    public Long lati,longi,dB;


    public Data(long id, String date, String time, String userId, Long lati, Long longi, Long dB) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.userId = userId;
        this.lati = lati;
        this.longi = longi;
        this.dB = dB;
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
            lati = null;
        } else {
            lati = in.readLong();
        }
        if (in.readByte() == 0) {
            longi = null;
        } else {
            longi = in.readLong();
        }
        if (in.readByte() == 0) {
            dB = null;
        } else {
            dB = in.readLong();
        }
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(userId);
        if (lati == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(lati);
        }
        if (longi == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(longi);
        }
        if (dB == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(dB);
        }
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
}