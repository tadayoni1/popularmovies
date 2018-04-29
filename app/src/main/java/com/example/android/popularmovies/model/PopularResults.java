package com.example.android.PopularMovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PopularResults implements Parcelable {

    private int last_page;
    private int total_results;
    private int total_pages;
    private List<Movie> results;

    public int getLastPage() {
        return last_page;
    }

    public void setLastPage(int last_page) {
        this.last_page = last_page;
    }

    public int getTotalResults() {
        return total_results;
    }

    public void setTotalResults(int total_results) {
        this.total_results = total_results;
    }

    public int getTotalPages() {
        return total_pages;
    }

    public void setTotalPages(int total_pages) {
        this.total_pages = total_pages;
    }

    public List<Movie> getResults() {
        return results;
    }

    public void setResults(List<Movie> results) {
        this.results = results;
    }

    public void add(PopularResults popularResults) {
        this.last_page = popularResults.getLastPage();
        this.total_results = popularResults.getTotalResults();
        this.total_pages = popularResults.getTotalPages();
        if (this.results == null) {
            this.results = new ArrayList<>();
        }
        this.results.addAll(popularResults.getResults());
    }

    public PopularResults() {
    }

    public PopularResults(Parcel in) {
        last_page = in.readInt();
        total_results = in.readInt();
        total_pages = in.readInt();
        results = new ArrayList<>();
        in.readTypedList(results , Movie.CREATOR);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PopularResults createFromParcel(Parcel in) {
            return new PopularResults(in);
        }

        public PopularResults[] newArray(int size) {
            return new PopularResults[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(last_page);
        dest.writeInt(total_results);
        dest.writeInt(total_pages);
        dest.writeTypedList(results);
    }
}
