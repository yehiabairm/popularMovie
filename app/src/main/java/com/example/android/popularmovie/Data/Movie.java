package com.example.android.popularmovie.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by susanoo on 22/03/16.
 */
public class Movie implements Parcelable{
    private String originalTitle;
    private String posterImgURL;
    private String overview;
    private String voteAverage;
    private String releaseDate;
    private String backdropImg;
    private String id;

    public Movie(String originalTitle, String posterImgURL, String overview, String releaseDate,
                 String voteAverage, String backdropImg, String id) {
        setOriginalTitle(originalTitle);
        setPosterImgURL(posterImgURL);
        setOverview(overview);
        setReleaseDate(releaseDate);
        setVoteAverage(voteAverage);
        setBackdropImg(backdropImg);
        setId(id);
    }

    private Movie(Parcel in){
            this.originalTitle = in.readString();
            this.posterImgURL = in.readString();
            this.overview = in.readString();
            this.releaseDate = in.readString();
            this.voteAverage = in.readString();
            this.backdropImg = in.readString();
            this.id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return getOriginalTitle() + "-" + getPosterImgURL() + "-" + getReleaseDate();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i ) {
        parcel.writeString(this.originalTitle);
        parcel.writeString(this.posterImgURL);
        parcel.writeString(this.overview);
        parcel.writeString(this.releaseDate);
        parcel.writeString(this.voteAverage);
        parcel.writeString(this.backdropImg);
        parcel.writeString(this.id);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }

    };
    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterImgURL() {
        return posterImgURL;
    }

    public void setPosterImgURL(String posterImgURL) {
        this.posterImgURL = posterImgURL;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getBackdropImg() {
        return backdropImg;
    }

    public void setBackdropImg(String backdropImg){
        this.backdropImg = backdropImg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



}
