package com.example.android.PopularMovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Videos implements Parcelable{

    private int id;
    private List<Trailer> results;

    public Videos() {
        this.results = new ArrayList<>();
    }

    protected Videos(Parcel in) {
        id = in.readInt();
        results = in.createTypedArrayList(Trailer.CREATOR);
    }

    public static final Creator<Videos> CREATOR = new Creator<Videos>() {
        @Override
        public Videos createFromParcel(Parcel in) {
            return new Videos(in);
        }

        @Override
        public Videos[] newArray(int size) {
            return new Videos[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Trailer> getResults() {
        return results;
    }

    public void setResults(List<Trailer> results) {
        this.results = results;
    }

    public void filterBy(String site) {
        List<Trailer> list = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getSite().equals(site)) {
                list.add(results.get(i));
            }
        }
        results = list;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeTypedList(results);
    }
}
