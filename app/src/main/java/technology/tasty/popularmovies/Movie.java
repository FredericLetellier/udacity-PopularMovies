package technology.tasty.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Popular Movies
 * Created on 10/06/2016 by Espace de travail.
 */
public class Movie implements Parcelable{

    /**
     * Path of movie poster image
     */
    private String mPosterPath;
    /**
     * Overview of movie
     */
    private String mOverview;
    /**
     * Release date of movie
     */
    private Date mReleaseDate;
    /**
     * Title of movie
     */
    private String mOriginalTitle;
    /**
     * User rating of movie
     */
    private Double mVoteAverage;

    public Movie(String pPosterPath, String pOverview, Date pReleaseDate, String pOriginalTitle, Double pVoteAverage){
        this.mPosterPath = pPosterPath;
        this.mOverview = pOverview;
        this.mReleaseDate = pReleaseDate;
        this.mOriginalTitle = pOriginalTitle;
        this.mVoteAverage = pVoteAverage;
    }

    /**
     * Get Path of movie poster image
     * @return mPosterPath
     */
    public String getPosterPath() {
        return mPosterPath;
    }

    /**
     * Get Overview of movie
     * @return mOverview
     */
    public String getOverview() {
        return mOverview;
    }

    /**
     * Get Release date of movie
     * @return mReleaseDate
     */
    public Date getReleaseDate() {
        return mReleaseDate;
    }

    /**
     * Get Original Title
     * @return mOriginalTitle
     */
    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    /**
     * Get User rating of movie
     * @return mVoteAverage
     */
    public Double getVoteAverage() {
        return mVoteAverage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPosterPath);
        dest.writeString(mOverview);
        dest.writeLong(mReleaseDate.getTime());
        dest.writeString(mOriginalTitle);
        dest.writeDouble(mVoteAverage);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>()
    {
        @Override
        public Movie createFromParcel(Parcel source)
        {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size)
        {
            return new Movie[size];
        }
    };

    public Movie(Parcel in) {
        this.mPosterPath = in.readString();
        this.mOverview = in.readString();
        this.mReleaseDate =  new Date(in.readLong());
        this.mOriginalTitle = in.readString();
        this.mVoteAverage = in.readDouble();
    }
}