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
    public Double lati;
    public Double longi;
    public Double dB;


    public Data(String date, String time, String userId, Double lati, Double longi, Double dB) {
//        this.id = id;
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
            lati = in.readDouble();
        }
        if (in.readByte() == 0) {
            longi = null;
        } else {
            longi = in.readDouble();
        }
        if (in.readByte() == 0) {
            dB = null;
        } else {
            dB = in.readDouble();
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
            dest.writeDouble(lati);
        }
        if (longi == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longi);
        }
        if (dB == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(dB);
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

    @Override
    public String toString() {

        return this.id
                + "; \n"
                + this.date
                + "; \n"
                + this.time
                + "; \n"
                + String.valueOf(this.lati)
                + "; \n"
                + String.valueOf(this.longi)
                + "; \n"
                + String.valueOf(Math.round(this.dB*100.0)/100.0);
    }
}